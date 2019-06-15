package br.com.main;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import br.com.main.model.CreditTransaction;
import br.com.main.util.FileUtils;

public class OfxGenerator {

	private static final String MAIN_TEMPLATE = "./ofx_main_template";
	private static final String TRANSACTIONS_TEMPLATE = "./ofx_trn_template";
	private static final int DESC_LENGTH = 40;
	
	private List<CreditTransaction> creditTransactionEntries;
	private String outputPath;
	
	private String mainOfx;
	private String transactionOfx;
	
	private LocalDate today = LocalDate.now();
	private DateTimeFormatter ofxDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	public OfxGenerator(List<CreditTransaction> creditTransactionEntries, String outputPath) throws IOException {
		this.creditTransactionEntries = creditTransactionEntries;
		this.outputPath = outputPath;
		this.mainOfx = FileUtils.getFileContent(MAIN_TEMPLATE);
		this.transactionOfx = FileUtils.getFileContent(TRANSACTIONS_TEMPLATE);
	}
	
	private String getMainTemplate() {
		return this.mainOfx;
	}
	
	private String getTransactionTemplate() {
		return this.transactionOfx;
	}
	
	public void generate() throws IOException {
		
		String mainOfxBody = this.getMainTemplate();
		mainOfxBody = mainOfxBody.replaceAll("\\{\\{CURRENT_DATE\\}\\}", ofxDateFormatter.format(today));
		
		Optional<LocalDate> min = this.creditTransactionEntries.parallelStream().map(transaction -> transaction.getDate()).min(Comparator.comparing( LocalDate::toEpochDay ));
		Optional<LocalDate> max = this.creditTransactionEntries.parallelStream().map(transaction -> transaction.getDate()).max(Comparator.comparing( LocalDate::toEpochDay ));
		mainOfxBody = mainOfxBody.replaceAll("\\{\\{DATE_START\\}\\}", ofxDateFormatter.format(min.get()));
		mainOfxBody = mainOfxBody.replaceAll("\\{\\{DATE_END\\}\\}", ofxDateFormatter.format(max.get()));
		
		StringBuilder transactionsContent = new StringBuilder();
		int dayCounter = -1;
		LocalDate lastDate = LocalDate.MIN;
		for(CreditTransaction transaction : this.creditTransactionEntries) {
			if(transaction.getDate().isAfter(lastDate)) {
				dayCounter = 0;
				lastDate = transaction.getDate();
			}
			
			String transactionOfxBody = getTransactionTemplate();
			transactionOfxBody = transactionOfxBody.replaceAll("\\{\\{TRANSACTION_TYPE\\}\\}", transaction.getAmount().longValue() >= 0? "CREDIT" : "DEBIT");
			transactionOfxBody = transactionOfxBody.replaceAll("\\{\\{TRANSACTION_DATE\\}\\}", ofxDateFormatter.format(transaction.getDate()));
			transactionOfxBody = transactionOfxBody.replaceAll("\\{\\{TRANSACTION_DAY_COUNTER\\}\\}", String.format("%03d", dayCounter++));
			transactionOfxBody = transactionOfxBody.replaceAll("\\{\\{TRANSACTION_AMOUNT\\}\\}", transaction.getAmount().toString());
			
			String transactionDescritption = StringUtils.rightPad(transaction.getDescription().substring(0, transaction.getDescription().length() > DESC_LENGTH? DESC_LENGTH : transaction.getDescription().length()), DESC_LENGTH, ' ');
			transactionDescritption = transactionDescritption.replaceAll("\\$", "\\\\\\$");
			
			transactionOfxBody = transactionOfxBody.replaceAll("\\{\\{TRANSACTION_DESCRIPTION\\}\\}", transactionDescritption);
			transactionsContent.append(transactionOfxBody+"\n");
		};
		
		String transactionsContentString = transactionsContent.toString().replaceAll("\\$", "\\\\\\$");
		mainOfxBody = mainOfxBody.replaceAll("\\{\\{TRANSACTIONS\\}\\}", transactionsContentString);
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(this.outputPath);
			fw.write(mainOfxBody);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			fw.close();
		}
		
	}

}

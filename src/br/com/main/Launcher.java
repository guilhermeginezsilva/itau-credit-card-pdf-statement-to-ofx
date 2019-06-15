package br.com.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import br.com.main.model.CreditTransaction;
import br.com.main.util.CurrencyUtils;
import br.com.main.util.FileUtils;

public class Launcher {

	public static void main(String[] args) throws IOException, SAXException, TikaException {
		File[] inputFiles = FileUtils.getInputFiles();

		for (File inputFile : inputFiles) {
			System.out.println(inputFile);

			if (!inputFile.getName().endsWith(".pdf")) {
				continue;
			}

			List<CreditTransaction> creditTransactionEntries = parsePdfCreditFile(inputFile);

			OfxGenerator ofxGenerator = new OfxGenerator(creditTransactionEntries, "./output/"+inputFile.getName().replaceFirst(".pdf", "")+".ofx");
			ofxGenerator.generate();
			
			
		}
	}

	private static List<CreditTransaction> parsePdfCreditFile(File inputFile)
			throws IOException, SAXException, TikaException {

		List<CreditTransaction> CreditTransactions = new ArrayList<CreditTransaction>();
		BufferedReader br = null;
		try {
			PdfReader pdfReader = new PdfReader();
			String pdfContent = pdfReader.parseText(inputFile.getAbsolutePath());

			br = new BufferedReader(new StringReader(pdfContent));
			String lineRead;
			int order = 0;
			while ((lineRead = br.readLine()) != null) {
				if (!lineRead.matches("([0-9]{2}/[0-9]{2}) .*?([^ ].*) ([0-9|-].*)")) {
					continue;
				}
				int currentYear = LocalDate.now().getYear();

				Matcher regexMatcher = Pattern.compile("([0-9]{2}/[0-9]{2}) .*?([^ ].*) ([0-9|-].*)").matcher(lineRead);
				if (regexMatcher.matches()) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

					String dayMonth = regexMatcher.group(1);

					CreditTransaction creditTransaction = CreditTransaction.builder()
							.date(LocalDate.parse(dayMonth + "/" + currentYear, formatter))
							.description(regexMatcher.group(2))
							.amount(CurrencyUtils.brStringToBigDecimal(regexMatcher.group(3)).negate())
							.order(order++).build();
					CreditTransactions.add(creditTransaction);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(br != null) {
				br.close();
			}
		}
		postProcessmentDataFixing(CreditTransactions);

		return CreditTransactions;
	}

	private static void postProcessmentDataFixing(List<CreditTransaction> creditTransactions) {
		fixYearWhenJanuaryChange(creditTransactions);
	}

	private static void fixYearWhenJanuaryChange(List<CreditTransaction> creditTransactions) {
		boolean isYearChange = creditTransactions.parallelStream()
				.anyMatch(transaction -> transaction.getDate().getMonth() == Month.JANUARY);

		if (isYearChange) {
			creditTransactions.parallelStream()
					.filter(transaction -> transaction.getDate().getMonth() == Month.DECEMBER)
					.forEach(transaction -> transaction.setDate(transaction.getDate().minusYears(1)));
		}
	}

}

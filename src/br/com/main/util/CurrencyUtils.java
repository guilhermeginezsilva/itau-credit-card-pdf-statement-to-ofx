package br.com.main.util;

import java.math.BigDecimal;

public class CurrencyUtils {

	public static BigDecimal brStringToBigDecimal(String brCurrencyValue) {
		brCurrencyValue = brCurrencyValue.replaceAll("\\.", "");
		brCurrencyValue = brCurrencyValue.replaceAll(",", ".");
		
		return new BigDecimal(brCurrencyValue);
	}
	
}

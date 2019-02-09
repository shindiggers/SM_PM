package com.catamount.pocketmoney.misc;

import android.util.Log;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.database.AccountDB;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;

public class CurrencyExt {
    public static String amountAsCurrency(double amount) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        currencyFormatter.setCurrency(Currency.getInstance(Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
        return currencyFormatter.format(amount);
    }

    public static String amountAsCurrencyWithoutCents(double amount) {
        return amountAsCurrencyWithoutCents(amount, null);
    }

    public static String amountAsCurrencyWithoutCents(double amount, String currencyCode) {
        if (currencyCode == null) {
            currencyCode = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        try {
            currencyFormatter.setCurrency(Currency.getInstance(currencyCode));
            currencyFormatter.setMaximumFractionDigits(0);
            return currencyFormatter.format(amount);
        } catch (IllegalArgumentException e) {
            currencyFormatter.setCurrency(Currency.getInstance("USD"));
            currencyFormatter.setMaximumFractionDigits(0);
            return currencyFormatter.format(amount).replace("$", currencyCode);
        }
    }

    public static String amountAsCurrency(double amount, String currencyCode) {
        if (currencyCode == null) {
            currencyCode = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        try {
            currencyFormatter.setCurrency(Currency.getInstance(currencyCode));
            return currencyFormatter.format(amount);
        } catch (IllegalArgumentException e) {
            currencyFormatter.setCurrency(Currency.getInstance("USD"));
            return currencyFormatter.format(amount).replace("$", currencyCode);
        }
    }

    public static String amountAsString(double amount) {
        NumberFormat currencyFormatter = NumberFormat.getInstance();
        currencyFormatter.setCurrency(Currency.getInstance(Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
        return currencyFormatter.format(amount);
    }

    public static double amountFromString(String amount) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        try {
            currencyFormatter.setCurrency(Currency.getInstance(Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
        } catch (IllegalArgumentException e) {
            if (amount.length() > 0) {
                while (Character.isLetter(amount.charAt(0))) {
                    amount = amount.substring(1);
                }
            }
        }
        Number aNum = 0.0d;
        try {
            aNum = currencyFormatter.parse(amount.trim());
        } catch (ParseException e2) {
            try {
                aNum = NumberFormat.getNumberInstance().parse(amount);
            } catch (ParseException e3) {
                try {
                    aNum = Double.parseDouble(amount);
                } catch (Exception e4) {
                    Log.i(PocketMoney.TAG, "ParseException " + e2.getMessage());
                }
            }
        }
        return aNum.doubleValue();
    }

    public static double amountFromStringWithCurrency(String amount, String currency) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        String code = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        boolean multiplieCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        if (currency == null || !multiplieCurrencies) {
            currency = code;
        }
        try {
            currencyFormatter.setCurrency(Currency.getInstance(currency));
        } catch (IllegalArgumentException e) {
            if (amount.length() > 0) {
                while (Character.isLetter(amount.charAt(0))) {
                    amount = amount.substring(1);
                }
            }
        }
        Number aNum = 0.0d;
        try {
            aNum = currencyFormatter.parse(amount.trim());
        } catch (ParseException e2) {
            try {
                aNum = NumberFormat.getNumberInstance().parse(amount);
            } catch (ParseException e3) {
                try {
                    aNum = Double.parseDouble(amount);
                } catch (Exception e4) {
                    Log.i(PocketMoney.TAG, "ParseException " + e2.getMessage());
                }
            }
        }
        return aNum.doubleValue();
    }

    public static String exchangeRateAsString(double xrate) {
        return new DecimalFormat("#.0#######").format(xrate);
    }

    public static String[] getCurrencies() {
        return new String[]{"AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTN", "BWP", "BYR", "BZD", "CAD", "CDF", "CHF", "CLP", "CNY", "COP", "CRC", "CUC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EEK", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GHS", "GIP", "GMD", "GNF", "GTQ", "GWP", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "INR", "IQD", "IRR", "ISK", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LTL", "LVL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MUR", "MVR", "MWK", "MXN", "MYR", "MZE", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SKK", "SLL", "SOS", "SRD", "STD", "SVC", "SYP", "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VEF", "VND", "VUV", "WST", "XAF", "XCD", "XOF", "XPF", "YER", "ZAR", "ZMK", "ZWL"};
    }

    public static String[] getCurrenciesWithSymbols() {
        int i = 0;
        String[] codes = getCurrencies();
        String[] otherCodes = AccountDB.usedCurrencyCodes();
        ArrayList<String> nameList = new ArrayList();
        for (String loc : otherCodes) {
            try {
                String loc2 = "";
                nameList.add(new StringBuilder(String.valueOf(Currency.getInstance(loc2).getCurrencyCode())).append(" \ufffd ").append(Currency.getInstance(loc2).getSymbol()).toString());
            } catch (Exception e) {
            }
        }
        int length = codes.length;
        while (i < length) {
            String loc2 = codes[i];
            try {
                nameList.add(new StringBuilder(String.valueOf(Currency.getInstance(loc2).getCurrencyCode())).append(" \ufffd ").append(Currency.getInstance(loc2).getSymbol()).toString());
            } catch (Exception e2) {
            }
            i++;
        }
        String[] retVal = new String[nameList.size()];
        nameList.toArray(retVal);
        return retVal;
    }
}

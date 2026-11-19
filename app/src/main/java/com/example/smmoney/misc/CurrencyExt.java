package com.example.smmoney.misc;

import android.content.Context;
import android.util.Log;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class CurrencyExt {
    public static String amountAsCurrency(double amount) {
        return amountAsCurrency(amount, null);
    }

    public static String amountAsCurrencyWithoutCents(double amount) {
        return amountAsCurrencyWithoutCents(amount, null);
    }

    public static String amountAsCurrencyWithoutCents(double amount, String currencyCode) {
        String code = currencyCode;
        if (!Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES) || code == null || code.trim().isEmpty() || code.isEmpty()) {
            code = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        if (code.trim().isEmpty() || code.isEmpty()) {
            code = "USD";
        }
        code = code.trim();
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        try {
            currencyFormatter.setCurrency(Currency.getInstance(code));
            currencyFormatter.setMaximumFractionDigits(0);
            return currencyFormatter.format(amount);
        } catch (IllegalArgumentException e) {
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            numberFormatter.setMaximumFractionDigits(0);
            return code + numberFormatter.format(amount);
        }
    }

    public static String amountAsCurrency(double amount, String currencyCode) {
        String code = currencyCode;
        if (!Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES) || code == null || code.trim().isEmpty() || "Blue".equals(code)) {
            code = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        if (code.trim().isEmpty() || "Blue".equals(code)) {
            code = "USD";
        }
        code = code.trim();
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        try {
            currencyFormatter.setCurrency(Currency.getInstance(code));
            return currencyFormatter.format(amount);
        } catch (IllegalArgumentException e) {
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            numberFormatter.setMinimumFractionDigits(2);
            numberFormatter.setMaximumFractionDigits(2);
            return code + numberFormatter.format(amount);
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
            if (!amount.isEmpty()) {
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
                    try {
                        String cleaned = amount.replaceAll("[^0-9.,-]", "").trim();
                        aNum = NumberFormat.getNumberInstance().parse(cleaned);
                    } catch (Exception e5) {
                        try {
                            String cleaned = amount.replaceAll("[^0-9.,-]", "").trim();
                            aNum = Double.parseDouble(cleaned);
                        } catch (Exception e6) {
                            Log.i(SMMoney.TAG, "ParseException " + e2.getMessage());
                        }
                    }
                }
            }
        }
        return aNum != null ? aNum.doubleValue() : 0.0d;
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
            if (!amount.isEmpty()) {
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
                    try {
                        String cleaned = amount.replaceAll("[^0-9.,-]", "").trim();
                        aNum = NumberFormat.getNumberInstance().parse(cleaned);
                    } catch (Exception e5) {
                        try {
                            String cleaned = amount.replaceAll("[^0-9.,-]", "").trim();
                            aNum = Double.parseDouble(cleaned);
                        } catch (Exception e6) {
                            Log.i(SMMoney.TAG, "ParseException " + e2.getMessage());
                        }
                    }
                }
            }
        }
        return aNum != null ? aNum.doubleValue() : 0.0d;
    }

    public static String exchangeRateAsString(double xrate) {
        return new DecimalFormat("#.0#######").format(xrate);
    }

    public static String[] getCurrencies() {
        return new String[]{"AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTN", "BWP", "BYR", "BZD", "CAD", "CDF", "CHF", "CLP", "CNY", "COP", "CRC", "CUC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EEK", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GHS", "GIP", "GMD", "GNF", "GTQ", "GWP", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "INR", "IQD", "IRR", "ISK", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LTL", "LVL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MUR", "MVR", "MWK", "MXN", "MYR", "MZE", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SKK", "SLL", "SOS", "SRD", "STD", "SVC", "SYP", "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VEF", "VND", "VUV", "WST", "XAF", "XCD", "XOF", "XPF", "YER", "ZAR", "ZMK", "ZWL"};
    }

    public record CurrencyPickerItem(boolean isHeader, String displayText, String currencyCode) {

        public static CurrencyPickerItem createHeader(String title) {
            return new CurrencyPickerItem(true, title, null);
        }

        public static CurrencyPickerItem createItem(String currencyCode, String displayText) {
            return new CurrencyPickerItem(false, displayText, currencyCode);
        }
    }

    public static List<String> getRecentCurrencies() {
        String saved = Prefs.getStringPref(Prefs.RECENT_CURRENCY_CODES);
        List<String> recentList = new ArrayList<>();
        if (!saved.trim().isEmpty()) {
            String[] split = saved.split(",");
            for (String s : split) {
                String code = s.trim();
                if (!code.isEmpty() && !recentList.contains(code)) {
                    recentList.add(code);
                }
            }
        }

        // Ensure defaults are populated if empty or missing
        String homeCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        if (homeCurrency.trim().isEmpty()) {
            homeCurrency = "USD";
        }
        homeCurrency = homeCurrency.trim();

        if (recentList.isEmpty()) {
            recentList.add(homeCurrency);
            String[] defaultCommons = new String[]{"GBP", "USD", "EUR", "CAD", "AUD"};
            for (String def : defaultCommons) {
                if (!recentList.contains(def) && recentList.size() < 5) {
                    recentList.add(def);
                }
            }
        } else {
            // If homeCurrency is not already in the list, ensure it's available
            if (!recentList.contains(homeCurrency)) {
                recentList.add(0, homeCurrency);
                if (recentList.size() > 5) {
                    recentList.remove(recentList.size() - 1);
                }
            }
        }
        return recentList;
    }

    public static void addRecentCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return;
        }
        String code = currencyCode.trim();
        List<String> recents = new ArrayList<>(getRecentCurrencies());
        recents.remove(code);
        recents.add(0, code);
        while (recents.size() > 5) {
            recents.remove(recents.size() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recents.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(recents.get(i));
        }
        Prefs.setPref(Prefs.RECENT_CURRENCY_CODES, sb.toString());
    }

    public static List<CurrencyPickerItem> getSegmentedCurrencyList() {
        List<CurrencyPickerItem> items = new ArrayList<>();
        Context context = SMMoney.getAppContext();

        // 1. Common currencies section
        items.add(CurrencyPickerItem.createHeader(context.getString(R.string.CURRENCIES_COMMON_TITLE)));
        List<String> recentCodes = getRecentCurrencies();
        for (String code : recentCodes) {
            try {
                Currency c = Currency.getInstance(code);
                items.add(CurrencyPickerItem.createItem(code, c.getCurrencyCode() + " - " + c.getSymbol()));
            } catch (Exception e) {
                items.add(CurrencyPickerItem.createItem(code, code));
            }
        }

        // 2. All currencies section
        items.add(CurrencyPickerItem.createHeader(context.getString(R.string.CURRENCIES_ALL_TITLE)));
        String[] allCodes = getCurrencies();
        for (String code : allCodes) {
            try {
                Currency c = Currency.getInstance(code);
                items.add(CurrencyPickerItem.createItem(code, c.getCurrencyCode() + " - " + c.getSymbol()));
            } catch (Exception e) {
                items.add(CurrencyPickerItem.createItem(code, code));
            }
        }
        return items;
    }


}

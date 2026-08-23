package com.example.smmoney.importexport;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.HandlerActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;

public class ImportExportCSV {
    public String CSVPath;
    private final Context context;
    private int currentLine;
    private FilterClass filter;
    private boolean importFileExists = false;
    private final ArrayList<String> lines = new ArrayList<>();
    private int numberOfLines;
    private int oldNumber = -1;

    public ImportExportCSV(Context context) {
        this.context = context;
    }

    public ImportExportCSV(String filePath, Context context) {
        this.context = context;
        this.CSVPath = filePath;
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        if (encodingStr.isEmpty()) {
            encodingStr = "UTF-8";
        }
        try (BufferedReader csvReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filePath)), encodingStr))) {
            String readLine;
            while ((readLine = csvReader.readLine()) != null) {
                this.lines.add(readLine);
            }
            this.numberOfLines = this.lines.size();
            this.currentLine = 0;
            this.importFileExists = true;
        } catch (FileNotFoundException e) {
            displayError("Error reading CSV file: " + e);
            Log.v("FileReader", "File Not Found");
        } catch (UnsupportedEncodingException e2) {
            Log.e(SMMoney.TAG, "UnsupportedEncodingException in ImportExportCSV constructor: " + encodingStr, e2);
            displayError("Unsupported file encoding: " + encodingStr);
        } catch (IOException e3) {
            displayError("Error reading CSV file: " + e3);
            Log.e(SMMoney.TAG, "IOException in ImportExportCSV constructor", e3);
        }
    }

    public boolean hasFile() {
        return this.importFileExists;
    }

    public void setFilter(FilterClass newFilter) {
        this.filter = newFilter;
    }

    private void updateProgressBar() {
        if (this.numberOfLines > 30 && (this.currentLine * 100) / this.numberOfLines != this.oldNumber) {
            this.oldNumber = (this.currentLine * 100) / this.numberOfLines;
            ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 4, (this.currentLine * 100) / this.numberOfLines, 0));
        }
    }

    // Suppress resource warning: SQLiteDatabase is an app-wide singleton managed by Database helper and should not be closed here
    @SuppressWarnings("resource")
    public void importIntoDatabase(HandlerActivity act) {
        if (this.lines.isEmpty()) {
            act.getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "Import Complete"));
            return;
        }
        String line = this.lines.get(this.currentLine);
        if (line.startsWith("Account") || line.startsWith("\"Account\"")) {
            this.currentLine++;
        }
        SQLiteDatabase db = Database.currentDB();
        db.beginTransaction();
        try {
            while (this.currentLine < this.numberOfLines) {
                updateProgressBar();
                String[] lineTokens = parseLine(this.lines.get(this.currentLine));
                if (lineTokens != null && lineTokens.length >= 8) {
                    try {
                        importTransaction(lineTokens);
                    } catch (Exception e) {
                        Log.e(SMMoney.TAG, "Error importing transaction at line " + this.currentLine, e);
                        displayError("Error processing csv file.\n Please ensure that the file encoding is set correctly.");
                    }
                }
                this.currentLine++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        act.getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "Import Complete"));
        AccountDB.setLastExportTimestampForAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
    }

    private void importTransaction(String[] tokens) {
        if (tokens[0] == null || tokens[0].trim().isEmpty()) {
            return;
        }
        String accountName = tokens[0].trim();
        ensureAccountExists(accountName);

        TransactionClass transaction = new TransactionClass();
        transaction.setAccount(accountName);
        transaction.setDate(parseDate(tokens[1]));
        transaction.setCheckNumber(tokens[2]);

        String payeeOrTransfer = tokens[3] != null ? tokens[3].trim() : "";
        boolean isTransfer = payeeOrTransfer.startsWith("<") && payeeOrTransfer.endsWith(">") && payeeOrTransfer.length() > 2;
        String transferToAccountName = null;
        if (isTransfer) {
            transferToAccountName = payeeOrTransfer.substring(1, payeeOrTransfer.length() - 1).trim();
            ensureAccountExists(transferToAccountName);
            transaction.setTransferToAccount(transferToAccountName);
            transaction.setPayee("");
        } else {
            transaction.setPayee(payeeOrTransfer);
        }

        transaction.setCategory(tokens[4]);
        transaction.setClassName(tokens[5]);
        transaction.setMemo(tokens[6]);
        double amount = amountFromCSV(tokens[7]);
        transaction.setSubTotal(amount);
        transaction.setAmount(amount);

        if (tokens.length > 8) {
            transaction.setCleared("*".equals(tokens[8].trim()));
        }
        if (tokens.length > 9 && tokens[9] != null && !tokens[9].trim().isEmpty()) {
            transaction.setCurrencyCode(tokens[9].trim());
        } else {
            transaction.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
        }
        if (tokens.length > 10 && tokens[10] != null) {
            try {
                String xrateStr = tokens[10].trim();
                transaction.setXrate(Double.parseDouble(xrateStr));
            } catch (Exception e) {
                transaction.setXrate(1.0d);
            }
        }
        transaction.initType();
        if (Prefs.getBooleanPref(Prefs.AUTOADD_LOOKUPS)) {
            Database.autoAddLookupItemsFromTransaction(transaction);
        }
        transaction.saveToDatabase();

        if (isTransfer && !transferToAccountName.isEmpty()) {
            handleTransferCounterpart(transaction, accountName, transferToAccountName);
        }
    }

    private void ensureAccountExists(String accountName) {
        if (accountName == null || accountName.isEmpty()) {
            return;
        }
        int accountId = AccountClass.idForAccount(accountName);
        if (accountId == 0) {
            accountId = AccountClass.idForAccountElseAddIfMissing(accountName, true);
            if (accountId != 0) {
                AccountClass account = new AccountClass(accountId);
                account.setAccount(accountName);
                account.setTotalWorth(true);
                account.setNoLimit(true);
                account.saveToDatabase();
            }
        }
    }

    private void handleTransferCounterpart(TransactionClass sourceTransaction, String fromAccount, String toAccount) {
        AccountClass toAct = AccountDB.recordFor(toAccount);
        boolean regularTransfer = toAct != null && sourceTransaction.getCurrencyCode().equals(toAct.getCurrencyCode());
        double counterpartAmount = regularTransfer
                ? (-1.0d * sourceTransaction.getAmount()) / sourceTransaction.getXrate()
                : -1.0d * sourceTransaction.getAmount();
        String counterpartCurrency = regularTransfer ? null : sourceTransaction.getCurrencyCode();

        com.example.smmoney.misc.TransactionTransferRetVals ret = new com.example.smmoney.misc.TransactionTransferRetVals();
        TransactionDB.transactionGetTransfer(toAccount, fromAccount, sourceTransaction.getDate(), counterpartAmount, counterpartCurrency, ret);

        if (ret.transferRecID == 0) {
            // Counterpart does not exist yet; create and save it
            TransactionClass counterpart = new TransactionClass();
            counterpart.setAccount(toAccount);
            counterpart.setTransferToAccount(fromAccount);
            counterpart.setDate((java.util.GregorianCalendar) sourceTransaction.getDate().clone());
            counterpart.setCheckNumber(sourceTransaction.getCheckNumber());
            counterpart.setCategory(sourceTransaction.getCategory());
            counterpart.setClassName(sourceTransaction.getClassName());
            counterpart.setMemo(sourceTransaction.getMemo());
            counterpart.setSubTotal(counterpartAmount);
            counterpart.setAmount(counterpartAmount);
            counterpart.setCleared(sourceTransaction.getCleared());
            if (toAct != null && toAct.getCurrencyCode() != null) {
                counterpart.setCurrencyCode(toAct.getCurrencyCode());
            } else {
                counterpart.setCurrencyCode(sourceTransaction.getCurrencyCode());
            }
            counterpart.setXrate(sourceTransaction.getXrate());
            counterpart.initType();
            counterpart.saveToDatabase();
        }
    }

    private java.util.GregorianCalendar parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return new java.util.GregorianCalendar();
        }
        String cleaned = dateStr.trim();
        // 1. Try ISO 8601
        java.util.GregorianCalendar cal = CalExt.dateFromDescriptionWithISO861Date(cleaned);
        if (cal != null) {
            return cal;
        }

        // 2. Try Date + Time formats (e.g. "MMM d, yyyy h:mm:ss a", "MMM d, yyyy HH:mm", "d MMM yyyy HH:mm:ss")
        String[] dateTimePatterns = new String[]{
                "MMM d, yyyy h:mm:ss a",
                "MMM d, yyyy HH:mm:ss",
                "MMM d, yyyy h:mm a",
                "MMM d, yyyy HH:mm",
                "d MMM yyyy h:mm:ss a",
                "d MMM yyyy HH:mm:ss",
                "d MMM yyyy h:mm a",
                "d MMM yyyy HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy HH:mm",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm"
        };
        for (String pattern : dateTimePatterns) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault());
                java.util.Date parsed = sdf.parse(cleaned);
                if (parsed != null) {
                    java.util.GregorianCalendar g = new java.util.GregorianCalendar();
                    g.setTime(parsed);
                    return g;
                }
            } catch (java.text.ParseException ignored) {
            }
        }

        // 3. Try standard system DateFormat parsers
        try {
            java.util.Date parsed = android.text.format.DateFormat.getMediumDateFormat(SMMoney.getAppContext()).parse(cleaned);
            if (parsed != null) {
                java.util.GregorianCalendar g = new java.util.GregorianCalendar();
                g.setTime(parsed);
                return g;
            }
        } catch (Exception ignored) {
        }

        try {
            java.util.Date parsed = android.text.format.DateFormat.getDateFormat(SMMoney.getAppContext()).parse(cleaned);
            if (parsed != null) {
                java.util.GregorianCalendar g = new java.util.GregorianCalendar();
                g.setTime(parsed);
                return g;
            }
        } catch (Exception ignored) {
        }

        // 4. Try common date-only patterns
        String[] datePatterns = new String[]{
                "d MMM yyyy",
                "MMM d, yyyy",
                "yyyy-MM-dd",
                "MM/dd/yyyy",
                "dd/MM/yyyy"
        };
        for (String pattern : datePatterns) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault());
                java.util.Date parsed = sdf.parse(cleaned);
                if (parsed != null) {
                    java.util.GregorianCalendar g = new java.util.GregorianCalendar();
                    g.setTime(parsed);
                    return g;
                }
            } catch (java.text.ParseException ignored) {
            }
        }

        return new java.util.GregorianCalendar();
    }

    @NonNull
    private DecimalFormat getNumberFormatter() {
        String numberFormat = Prefs.getStringPref(Prefs.QIF_NUMBERFORMAT);
        if (numberFormat.isEmpty() || numberFormat.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            numberFormat = "1,000.00";
        }
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        switch (numberFormat) {
            case "1,000.00":
                formatSymbols.setDecimalSeparator('.');
                formatSymbols.setGroupingSeparator(',');
                break;
            case "1.000,00":
                formatSymbols.setDecimalSeparator(',');
                formatSymbols.setGroupingSeparator('.');
                break;
            case "1'000.00":
                formatSymbols.setDecimalSeparator('.');
                formatSymbols.setGroupingSeparator('\'');
                break;
            case "1'000,00":
                formatSymbols.setDecimalSeparator(',');
                formatSymbols.setGroupingSeparator('\'');
                break;
            case "1 000,00":
                formatSymbols.setDecimalSeparator(',');
                formatSymbols.setGroupingSeparator(' ');
                break;
        }
        return new DecimalFormat("#,##0.00#", formatSymbols);
    }

    private double amountFromCSV(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0d;
        }
        String cleaned = text.trim();
        DecimalFormat numberFormatter = getNumberFormatter();
        Number number = null;
        try {
            number = numberFormatter.parse(cleaned);
        } catch (ParseException e) {
            Log.e(SMMoney.TAG, "ParseException in amountFromCSV: " + cleaned, e);
        }
        if (number == null && cleaned.startsWith("-")) {
            try {
                number = numberFormatter.parse(cleaned.substring(1).trim());
                if (number != null) {
                    return number.doubleValue() * -1.0d;
                }
            } catch (ParseException e2) {
                Log.e(SMMoney.TAG, "ParseException in amountFromCSV (negative): " + cleaned, e2);
            }
        }
        if (number == null && cleaned.startsWith("(") && cleaned.endsWith(")")) {
            try {
                number = numberFormatter.parse(cleaned.substring(1, cleaned.length() - 1).trim());
                if (number != null) {
                    return number.doubleValue() * -1.0d;
                }
            } catch (ParseException e22) {
                Log.e(SMMoney.TAG, "ParseException in amountFromCSV (parentheses): " + cleaned, e22);
            }
        }
        if (number == null) {
            try {
                return Double.parseDouble(cleaned.replace(",", ""));
            } catch (NumberFormatException ignored) {
            }
        }
        return number == null ? 0.0d : number.doubleValue();
    }

    private String escapeDoubleQuote(String text) {
        return text == null ? "" : text.replace("\"", "\"\"");
    }

    private static String[] parseLine(String line) {
        if (line == null) {
            return null;
        }
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++; // Skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private String generateData() {
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        StringBuilder returnStr = new StringBuilder("\"Account\",\"Date\",\"ChkNum\",\"Payee\",\"Category\",\"Class\",\"Memo\",\"Amount\",\"Cleared\",\"CurrencyCode\",\"ExchangeRate\"\n");
        ArrayList<TransactionClass> transactions = TransactionDB.queryWithFilter(new FilterClass());
        this.currentLine = 0;
        this.oldNumber = -1;
        this.numberOfLines = transactions.size();
        if (this.numberOfLines == 0) {
            return "";
        }
        for (TransactionClass transactionClass : transactions) {
            String exchangeRateAsString;
            this.currentLine++;
            updateProgressBar();
            String CSVData = "";
            for (SplitsClass split : transactionClass.getSplits()) {
                if (this.filter != null && this.filter.isValidSplit(split)) {
                    StringBuilder stringBuilder;
                    stringBuilder = new StringBuilder(
                            "\""
                                    + CSVData + escapeDoubleQuote(transactionClass.getAccount())
                                    + "\",\""
                                    + escapeDoubleQuote(Prefs.getBooleanPref(Prefs.SHOWTIME) ? CalExt.descriptionWithDateTime(transactionClass.getDate()) : CalExt.descriptionWithShortDate(transactionClass.getDate()))
                                    + "\",\""
                                    + escapeDoubleQuote(transactionClass.getCheckNumber())
                                    + "\",\""
                                    + escapeDoubleQuote(split.isTransfer() ? "<" + split.getTransferToAccount() + ">" : transactionClass.getPayee())
                                    + "\",\""
                                    + escapeDoubleQuote(split.getCategory())
                                    + "\",\""
                                    + escapeDoubleQuote(split.getClassName())
                                    + "\",\""
                                    + escapeDoubleQuote(split.getMemo())
                                    + "\",\""
                                    + escapeDoubleQuote(CurrencyExt.amountAsString(split.getAmount()))
                                    + "\",\""
                                    + (transactionClass.getCleared() ? "*" : "")
                                    + "\",\""
                                    + escapeDoubleQuote(split.getCurrencyCode())
                                    + "\",\""
                    );
                    if (multipleCurrencies) {
                        exchangeRateAsString = CurrencyExt.exchangeRateAsString(split.getXrate());
                    } else {
                        exchangeRateAsString = "1.0";
                    }
                    CSVData = stringBuilder.append(escapeDoubleQuote(exchangeRateAsString)).append("\"\n").toString();
                }
            }
            returnStr.append(CSVData);
        }
        return returnStr.toString();
    }

    // Suppress warning: keeping boolean return value for interface consistency across exporters and caller error checking
    @SuppressWarnings("UnusedReturnValue")
    public boolean exportRecords() {
        String CSVData = generateData();
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        String fileName = "SMMoney.csv";
        try {
            String filePath = pmExternalPath + fileName;
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            BufferedWriter csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr));
            try {
                csvWriter.write(CSVData);
                csvWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + fileName + "' placed in Download/PocketMoneyBackup"));
                return true;
            } catch (IOException e) {
                Log.v("Export writing error", e.toString());
                displayError(e.toString());
                return false;
            }
        } catch (IOException e) {
            Log.v("Export writing error", e.toString());
            displayError(e.toString());
            return false;
        }
    }

    private String generateData(ArrayList<TransactionClass> transactions) {
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        StringBuilder returnStr = new StringBuilder("\"Account\",\"Date\",\"ChkNum\",\"Payee\",\"Category\",\"Class\",\"Memo\",\"Amount\",\"Cleared\",\"CurrencyCode\",\"ExchangeRate\"\n");
        this.currentLine = 0;
        this.oldNumber = -1;
        this.numberOfLines = transactions.size();
        for (TransactionClass transactionClass : transactions) {
            String exchangeRateAsString;
            this.currentLine++;
            updateProgressBar();
            String CSVData = "";
            for (SplitsClass split : transactionClass.getSplits()) {
                if (this.filter != null && this.filter.isValidSplit(split)) {
                    StringBuilder stringBuilder = new StringBuilder("\"" + CSVData + escapeDoubleQuote(transactionClass.getAccount()) + "\",\"" + escapeDoubleQuote(Prefs.getBooleanPref(Prefs.SHOWTIME) ? CalExt.descriptionWithDateTime(transactionClass.getDate()) : CalExt.descriptionWithShortDate(transactionClass.getDate())) + "\",\"" + escapeDoubleQuote(transactionClass.getCheckNumber()) + "\",\"" + escapeDoubleQuote(split.isTransfer() ? "<" + split.getTransferToAccount() + ">" : transactionClass.getPayee()) + "\",\"" + escapeDoubleQuote(split.getCategory()) + "\",\"" + escapeDoubleQuote(split.getClassName()) + "\",\"" + escapeDoubleQuote(split.getMemo()) + "\",\"" + escapeDoubleQuote(CurrencyExt.amountAsString(split.getAmount())) + "\",\"" + (transactionClass.getCleared() ? "*" : "") + "\",\"" + escapeDoubleQuote(split.getCurrencyCode()) + "\",\"");
                    if (multipleCurrencies) {
                        exchangeRateAsString = CurrencyExt.exchangeRateAsString(split.getXrate());
                    } else {
                        exchangeRateAsString = "1.0";
                    }
                    CSVData = stringBuilder.append(escapeDoubleQuote(exchangeRateAsString)).append("\"\n").toString();
                }
            }
            returnStr.append(CSVData);
        }
        return returnStr.toString();
    }

    // Suppress resource warning: SQLiteDatabase is an app-wide singleton managed by Database helper and should not be closed here
    @SuppressWarnings("resource")
    private void displayError(String msg) {
        try {
            Database.currentDB().endTransaction();
        } catch (IllegalStateException e) {
            Log.e(SMMoney.TAG, "IllegalStateException in displayError", e);
        }
        ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 6, msg));
    }

    // Suppress warning: keeping boolean return value for interface consistency across exporters and caller error checking
    @SuppressWarnings("UnusedReturnValue")
    public boolean exportRecords(ArrayList<TransactionClass> transactions) {
        String CSVData = generateData(transactions);
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        String fileName = "SMMoney.csv";
        try {
            String filePath = pmExternalPath + fileName;
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            BufferedWriter csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr));
            try {
                csvWriter.write(CSVData);
                csvWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + fileName + "' placed in Download/PocketMoneyBackup"), 500);
                return true;
            } catch (IOException e) {
                Log.v("Export writing error", e.toString());
                displayError(e.toString());
                return false;
            }
        } catch (IOException e) {
            Log.v("Export writing error", e.toString());
            displayError(e.toString());
            return false;
        }
    }
}

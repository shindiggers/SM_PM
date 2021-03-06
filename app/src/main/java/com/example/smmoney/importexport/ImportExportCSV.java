package com.example.smmoney.importexport;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

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
import java.io.File;
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
import java.util.Vector;

public class ImportExportCSV {
    public String CSVPath;
    //String accountNameBeingImported;
    private final Context context;
    //Boolean csvOld = Boolean.FALSE;
    private int currentLine;
    private FilterClass filter;
    private boolean importFileExists = false;
    //boolean invalidCSV;
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
        BufferedReader CSVReader = null;
        try {
            CSVReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filePath)), encodingStr));
        } catch (FileNotFoundException e) {
            displayError("Error reading CSV file: " + e.toString(), false);
            Log.v("FileReader", "File Not Found");
            return;
        } catch (UnsupportedEncodingException e2) {
            Log.e(SMMoney.TAG, "import encoding " + encodingStr + " not supported");
            e2.printStackTrace();
        }
        try {
            String readLine;
            while (true) {
                readLine = CSVReader.readLine();
                if (readLine == null) {
                    break;
                }
                this.lines.add(readLine);
            }
            this.numberOfLines = this.lines.size();
            this.currentLine = 0;
        } catch (IOException e3) {
            displayError("Error reading CSV file: " + e3.toString(), false);
            e3.printStackTrace();
        }
        this.importFileExists = true;
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

    public void importIntoDatabase(HandlerActivity act) {
        String line = this.lines.get(this.currentLine);
        if (line.startsWith("Account") || line.startsWith("\"Account\"")) {
            this.currentLine++;
        }
        SQLiteDatabase db = Database.currentDB();
        db.beginTransaction();
        while (this.currentLine < this.numberOfLines) {
            updateProgressBar();
            String[] lineTokens = parseLine(this.lines.get(this.currentLine));
            if (lineTokens.length >= 8) {
                try {
                    importTransaction(lineTokens);
                } catch (Exception e) {
                    Log.e("", e.getLocalizedMessage());
                    displayError("Error processing csv file.\n Please ensure that the file encoding is set correctly.", true);
                }
            }
            try {
                this.currentLine++;
            } catch (Exception e2) {
                Log.e("", e2.getLocalizedMessage());
                displayError("Error processing csv file.\n Please ensure that the file encoding is set correctly.", true);
                return;
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        act.getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "Import Complete"));
        AccountDB.setLastExportTimestampForAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
    }

    private void importTransaction(String[] tokens) {
        if (tokens[0].length() != 0) {
            if (AccountClass.idForAccount(tokens[0]) == 0) {
                AccountClass account = new AccountClass();
                int accountID = AccountClass.idForAccountElseAddIfMissing(tokens[0], true);
                account.setTotalWorth(true);
                account.setNoLimit(true);
                account.saveToDatabase();
            }
            TransactionClass transaction = new TransactionClass();
            transaction.setAccount(tokens[0]);
            //todo: Date does not parse correctly. Need to handle all differently formatted date/time combos. Current parser gets date from dd MMM YYYY but does not get time
            //transaction.setDate(CalExt.dateFromDescriptionWithShortDate(tokens[1]));
            transaction.setDate(CalExt.dateFromDescriptionWithMediumDate(tokens[1]));
            //transaction.setDate(CalExt.dateFromDescriptionWithTime(tokens[1]));
            transaction.setCheckNumber(tokens[2]);
            transaction.setPayee(tokens[3]);
            if (tokens[3].endsWith(">") && tokens[3].startsWith("<")) {
                transaction.setTransferToAccount(tokens[3].substring(1, tokens[3].length() - 1));
                transaction.setPayee("");
            }
            transaction.setCategory(tokens[4]);
            transaction.setClassName(tokens[5]);
            transaction.setMemo(tokens[6]);
            transaction.setSubTotal(amountFromCSV(tokens[7]));
            transaction.setAmount(amountFromCSV(tokens[7]));
            if (tokens.length > 8) {
                transaction.setCleared(tokens[8].equals("*"));
            }
            if (tokens.length > 9) {
                transaction.setCurrencyCode(tokens[9]);
            } else {
                transaction.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
            }
            if (tokens.length > 10) {
                try {
                    transaction.setXrate(Double.parseDouble(tokens[10].substring(0, tokens[10].length() - 1)));
                } catch (Exception e) {
                    transaction.setXrate(1.0d);
                }
            }
            transaction.initType();
            if (Prefs.getBooleanPref(Prefs.AUTOADD_LOOKUPS)) {
                Database.autoAddLookupItemsFromTransaction(transaction);
            }
            transaction.saveToDatabase();
        }
    }

    private double amountFromCSV(String text) {
        String numberFormat = "Default";
        if (text == null) {
            return 0.0d;
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
        DecimalFormat numberFormatter = new DecimalFormat("#,##0.00#", formatSymbols);
        Number number = null;
        try {
            number = numberFormatter.parse(text);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (number == null && text.startsWith("-")) {
            try {
                number = numberFormatter.parse(text.substring(1));
                return number.doubleValue() * -1.0d;
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
        }
        if (number == null && text.startsWith("(") && text.endsWith(")")) {
            try {
                number = numberFormatter.parse(text.substring(1, text.length() - 1));
                return number.doubleValue() * -1.0d;
            } catch (ParseException e22) {
                e22.printStackTrace();
            }
        }
        return number == null ? 0.0d : number.doubleValue();
    }

    private String formatCSVString(String input) {
        if (input.equals("\"\"")) {
            return "";
        }
        return input.substring(1, input.length() - 1);
    }

    private String escapeDoubleQuote(String text) {
        return text == null ? "" : text.replace("\"", "\"\"");
    }

    private static String[] parseLine(String line) {
        if (line == null) {
            return null;
        }
        int i;
        Vector<String> store = new Vector<>();
        StringBuffer curVal = new StringBuffer();
        boolean inquotes = false;
        for (i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inquotes) {
                if (ch == '\"') {
                    inquotes = false;
                } else {
                    curVal.append(ch);
                }
            } else if (ch == '\"') {
                inquotes = true;
                if (curVal.length() > 0) {
                    curVal.append('\"');
                }
            } else if (ch == ',') {
                store.add(curVal.toString());
                curVal = new StringBuffer();
            } else {
                curVal.append(ch);
            }
        }
        store.add(curVal.toString());
        String[] retVal = new String[store.size()];
        i = 0;
        for (String s : store) {
            int i2 = i + 1;
            retVal[i] = s;
            i = i2;
        }
        return retVal;
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
            exchangeRateAsString = "";
        }
        return returnStr.toString();
    }

    public boolean exportRecords() {
        IOException e;
        String CSVData = generateData();
        //String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        String pmExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            String filePath = pmExternalPath + "/PocketMoneyBackup/" + "SMMoney.csv";
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            File dir = new File(filePath.substring(0, filePath.indexOf("/SMMoney/") + "/SMMoney/".length()));
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            BufferedWriter csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr));
            try {
                csvWriter.write(CSVData);
                csvWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + filePath.substring(pmExternalPath.length()) + "' placed in Download/PocketMoneyBackup"));
                return true;
            } catch (IOException e2) {
                e = e2;
                Log.v("Export writing error", e.toString());
                displayError(e.toString(), false);
                return false;
            }
        } catch (IOException e3) {
            e = e3;
            Log.v("Export writing error", e.toString());
            displayError(e.toString(), false);
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
            exchangeRateAsString = "";
        }
        return returnStr.toString();
    }

    private void displayError(String msg, boolean fromBackgroundThread) {
        try {
            Database.currentDB().endTransaction();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 6, msg));
    }

    public boolean exportRecords(ArrayList<TransactionClass> transactions) {
        IOException e;
        String CSVData = generateData(transactions);
        //String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        String pmExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            String filePath = pmExternalPath + "/PocketMoneyBackup/" + "SMMoney.csv";
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            File dir = new File(filePath.substring(0, filePath.indexOf("/SMMoney/") + "/SMMoney/".length()));
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            BufferedWriter csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr));
            try {
                csvWriter.write(CSVData);
                csvWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + filePath.substring(pmExternalPath.length()) + "' placed in Download/PocketMoneyBackup"), 500);
                return true;
            } catch (IOException e2) {
                e = e2;
                Log.v("Export writing error", e.toString());
                displayError(e.toString(), false);
                return false;
            }
        } catch (IOException e3) {
            e = e3;
            Log.v("Export writing error", e.toString());
            displayError(e.toString(), false);
            return false;
        }
    }
}

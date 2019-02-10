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
import java.util.Iterator;

public class ImportExportTDF {
    public String CSVPath;
    String accountNameBeingImported;
    Context context;
    Boolean csvOld = Boolean.FALSE;
    int currentLine;
    FilterClass filter;
    boolean importFileExists = false;
    boolean invalidCSV;
    ArrayList<String> lines = new ArrayList();
    int numberOfLines;
    int oldNumber = -1;

    public ImportExportTDF(Context context) {
        this.context = context;
    }

    public ImportExportTDF(String filePath, Context context) {
        this.context = context;
        this.CSVPath = filePath;
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        BufferedReader CSVReader = null;
        try {
            CSVReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(new StringBuilder(String.valueOf(pmExternalPath)).append(filePath).toString())), encodingStr));
        } catch (FileNotFoundException e) {
            displayError("Error reading QIF file: " + e.toString(), false);
            Log.v("FileReader", "File Not Found");
            return;
        } catch (UnsupportedEncodingException e2) {
            Log.e(SMMoney.TAG, "import encoding " + encodingStr + " not supported");
            e2.printStackTrace();
        }
        try {
            String readLine = "";
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
            displayError("Error reading QIF file: " + e3.toString(), false);
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

    public void updateProgressBar() {
        if (this.numberOfLines > 30 && (this.currentLine * 100) / this.numberOfLines != this.oldNumber) {
            this.oldNumber = (this.currentLine * 100) / this.numberOfLines;
            ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 4, (this.currentLine * 100) / this.numberOfLines, 0));
        }
    }

    public void importIntoDatabase(HandlerActivity act) {
        try {
            String line = this.lines.get(this.currentLine);
            if (line.startsWith("Account") || line.startsWith("\"Account\"")) {
                this.currentLine++;
            }
            SQLiteDatabase db = Database.currentDB();
            db.beginTransaction();
            this.oldNumber = -1;
            while (this.currentLine < this.numberOfLines) {
                String[] lineTokens = this.lines.get(this.currentLine).split("\t");
                if (lineTokens.length >= 8) {
                    try {
                        importTransaction(lineTokens);
                    } catch (Exception e) {
                        displayError("Error processing tdf file.\n Please ensure that the file encoding is set correctly.", true);
                        return;
                    }
                }
                updateProgressBar();
                this.currentLine++;
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            act.getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "Import Complete"));
            AccountDB.setLastExportTimestampForAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
        } catch (Exception e2) {
            displayError("Error processing tdf file.\n Please ensure that the file encoding is set correctly.", true);
        }
    }

    private void importTransaction(String[] tokens) {
        if (AccountClass.idForAccount(tokens[0]) == 0) {
            AccountClass account = new AccountClass();
            int accountID = AccountClass.idForAccountElseAddIfMissing(tokens[0], true);
            account.setTotalWorth(true);
            account.setNoLimit(true);
            account.saveToDatabase();
        }
        TransactionClass transaction = new TransactionClass();
        transaction.setAccount(tokens[0]);
        transaction.setDate(CalExt.dateFromDescriptionWithShortDate(tokens[1]));
        transaction.setCheckNumber(tokens[2]);
        transaction.setPayee(tokens[3]);
        if (tokens[3].endsWith(">") && tokens[3].startsWith("<")) {
            transaction.setTransferToAccount(tokens[3].substring(1, tokens[3].length() - 2));
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
        transaction.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
        transaction.initType();
        Prefs.getBooleanPref(Prefs.AUTOADD_LOOKUPS);
        transaction.saveToDatabase();
    }

    private double amountFromCSV(String text) {
        String numberFormat = "Default";
        if (text == null) {
            return 0.0d;
        }
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        if (numberFormat.equals("1,000.00")) {
            formatSymbols.setDecimalSeparator('.');
            formatSymbols.setGroupingSeparator(',');
        } else if (numberFormat.equals("1.000,00")) {
            formatSymbols.setDecimalSeparator(',');
            formatSymbols.setGroupingSeparator('.');
        } else if (numberFormat.equals("1'000.00")) {
            formatSymbols.setDecimalSeparator('.');
            formatSymbols.setGroupingSeparator('\'');
        } else if (numberFormat.equals("1'000,00")) {
            formatSymbols.setDecimalSeparator(',');
            formatSymbols.setGroupingSeparator('\'');
        } else if (numberFormat.equals("1 000,00")) {
            formatSymbols.setDecimalSeparator(',');
            formatSymbols.setGroupingSeparator(' ');
        }
        DecimalFormat numberFormatter = new DecimalFormat("#,##0.00#", formatSymbols);
        Number number = null;
        try {
            number = numberFormatter.parse(text);
        } catch (ParseException e) {
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
        return number.doubleValue();
    }

    private String formatCSVString(String input) {
        if (input.equals("\"\"")) {
            return "";
        }
        return input.substring(1, input.length() - 1);
    }

    public String generateData() {
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        String returnStr = "Account\tDate\tChkNum\tPayee\tCategory\tClass\tMemo\tAmount\tCleared\n";
        ArrayList<TransactionClass> transactions = TransactionDB.queryWithFilter(new FilterClass());
        this.numberOfLines = transactions.size();
        this.currentLine = 0;
        this.oldNumber = -1;
        Iterator it = transactions.iterator();
        while (it.hasNext()) {
            TransactionClass transaction = (TransactionClass) it.next();
            this.currentLine++;
            updateProgressBar();
            String TDFData = "";
            Iterator it2 = transaction.getSplits().iterator();
            while (it2.hasNext()) {
                SplitsClass split = (SplitsClass) it2.next();
                if (this.filter != null && this.filter.isValidSplit(split)) {
                    String str;
                    StringBuilder stringBuilder = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(TDFData)).append(transaction.getAccount()).append("\t").toString())).append(Prefs.getBooleanPref(Prefs.SHOWTIME) ? CalExt.descriptionWithDateTime(transaction.getDate()) : CalExt.descriptionWithShortDate(transaction.getDate())).append("\t").toString())).append(transaction.getCheckNumber()).append("\t").toString())).append(split.isTransfer() ? "<" + split.getTransferToAccount() + ">" : transaction.getPayee()).append("\t").toString())).append(split.getCategory()).append("\t").append(split.getClassName()).append("\t").toString())).append(split.getMemo().replace("\n", "<br>")).append("\t").toString())).append(multipleCurrencies ? split.amountAsCurrency() : CurrencyExt.amountAsString(split.getAmount())).append("\t").toString()));
                    if (transaction.getCleared()) {
                        str = "*";
                    } else {
                        str = "";
                    }
                    TDFData = stringBuilder.append(str).append("\n").toString();
                }
            }
            returnStr = new StringBuilder(String.valueOf(returnStr)).append(TDFData).toString();
        }
        return returnStr;
    }

    public boolean exportRecords() {
        BufferedWriter bufferedWriter;
        IOException e;
        String TDFData = generateData();
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            String filePath = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append("SMMoney.txt").toString();
            File dir = new File(filePath.substring(0, filePath.indexOf("/SMMoney/") + "/SMMoney/".length()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BufferedWriter TDFWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr));
            try {
                TDFWriter.write(TDFData);
                TDFWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + filePath.substring(pmExternalPath.length()) + "' placed in Download/PocketMoneyBackup"));
                bufferedWriter = TDFWriter;
                return true;
            } catch (IOException e2) {
                e = e2;
                bufferedWriter = TDFWriter;
                Log.i("Export writing error-0", e.toString());
                displayError(e.toString(), false);
                return false;
            }
        } catch (IOException e3) {
            e = e3;
            Log.i("Export writing error-0", e.toString());
            displayError(e.toString(), false);
            return false;
        }
    }

    public String generateData(ArrayList<TransactionClass> transactions) {
        boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
        String returnStr = "Account\tDate\tChkNum\tPayee\tCategory\tClass\tMemo\tAmount\tCleared\n";
        this.numberOfLines = transactions.size();
        this.currentLine = 0;
        this.oldNumber = -1;
        Iterator it = transactions.iterator();
        while (it.hasNext()) {
            TransactionClass transaction = (TransactionClass) it.next();
            this.currentLine++;
            updateProgressBar();
            String TDFData = "";
            Iterator it2 = transaction.getSplits().iterator();
            while (it2.hasNext()) {
                SplitsClass split = (SplitsClass) it2.next();
                if (this.filter != null && this.filter.isValidSplit(split)) {
                    String str;
                    StringBuilder stringBuilder = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(TDFData)).append(transaction.getAccount()).append("\t").toString())).append(Prefs.getBooleanPref(Prefs.SHOWTIME) ? CalExt.descriptionWithDateTime(transaction.getDate()) : CalExt.descriptionWithShortDate(transaction.getDate())).append("\t").toString())).append(transaction.getCheckNumber()).append("\t").toString())).append(split.isTransfer() ? "<" + split.getTransferToAccount() + ">" : transaction.getPayee()).append("\t").toString())).append(split.getCategory()).append("\t").append(split.getClassName()).append("\t").toString())).append(split.getMemo().replace("\n", "<br>")).append("\t").toString())).append(multipleCurrencies ? split.amountAsCurrency() : CurrencyExt.amountAsString(split.getAmount())).append("\t").toString()));
                    if (transaction.getCleared()) {
                        str = "*";
                    } else {
                        str = "";
                    }
                    TDFData = stringBuilder.append(str).append("\n").toString();
                }
            }
            returnStr = new StringBuilder(String.valueOf(returnStr)).append(TDFData).toString();
        }
        return returnStr;
    }

    public boolean exportRecords(ArrayList<TransactionClass> transactions) {
        BufferedWriter bufferedWriter;
        IOException e;
        String TDFData = generateData(transactions);
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            String filePath = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append("SMMoney.txt").toString();
            File dir = new File(filePath.substring(0, filePath.indexOf("/SMMoney/") + "/SMMoney/".length()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BufferedWriter TDFWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr));
            try {
                TDFWriter.write(TDFData);
                TDFWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + filePath.substring(pmExternalPath.length()) + "' placed in Download/PocketMoneyBackup"), 500);
                bufferedWriter = TDFWriter;
                return true;
            } catch (IOException e2) {
                e = e2;
                bufferedWriter = TDFWriter;
                Log.i("Export writing error", e.toString());
                displayError(e.toString(), false);
                return false;
            }
        } catch (IOException e3) {
            e = e3;
            Log.i("Export writing error", e.toString());
            displayError(e.toString(), false);
            return false;
        }
    }

    private void displayError(String msg, boolean fromBackgroundThread) {
        try {
            Database.currentDB().endTransaction();
        } catch (IllegalStateException e) {
        }
        ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 6, msg));
    }
}

package com.example.smmoney.importexport;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.HandlerActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportExportQIF {
    public String QIFPath;
    public String accountNameBeingImported;
    HandlerActivity act;
    Context context;
    int currentLine;
    FilterClass filter;
    boolean importFileExists = false;
    boolean invalidQIF;
    ArrayList<String> lines = new ArrayList();
    int numberOfLines;
    int oldNumber = -1;
    Boolean qifOld = Boolean.FALSE;

    public ImportExportQIF(Context context) {
        this.context = context;
        this.filter = new FilterClass();
    }

    public ImportExportQIF(String filePath, Context context) {
        Log.i("*** FilePath = ", filePath);
        this.context = context;
        this.QIFPath = filePath;
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        BufferedReader QIFReader = null;
        try {
            QIFReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filePath)), encodingStr));
        } catch (FileNotFoundException e) {
            displayError("Error reading QIF file: " + e.toString(), false);
            Log.v("FileReader", "File Not Found - 01: " + filePath);
        } catch (UnsupportedEncodingException e2) {
            Log.e(SMMoney.TAG, "import encoding " + encodingStr + " not supported");
            e2.printStackTrace();
        }
        try {
            String readLine = "";
            while (true) {
                readLine = QIFReader.readLine();
                if (readLine == null) {
                    break;
                }
                this.lines.add(readLine);
            }
            this.numberOfLines = this.lines.size();
            if (this.numberOfLines == 0) {
                displayError("Error reading QIF file: File is Empty", false);
                Log.v("FileReader", "Empty File");
                return;
            }
            this.currentLine = 0;
            this.importFileExists = true;
        } catch (IOException e3) {
            displayError("Error reading QIF file: " + e3.toString(), false);
            e3.printStackTrace();
        }
    }

    public boolean hasFile() {
        return this.importFileExists;
    }

    public void setFilter(FilterClass newFilter) {
        this.filter = newFilter;
    }

    public static String[] dateFormats() {
        return new String[]{Locales.kLOC_GENERAL_DEFAULT, "mm/dd'yy", "mm/dd'yyyy", "mm/dd/yy", "mm/dd/yyyy", "dd/mm'yy", "dd/mm'yyyy", "dd/mm/yy", "dd/mm/yyyy", "yyyy/mm/dd"};
    }

    public static String[] dateSeparators() {
        return new String[]{Locales.kLOC_GENERAL_DEFAULT, "/", ".", "-"};
    }

    public static String[] numberFormats() {
        return new String[]{Locales.kLOC_GENERAL_DEFAULT, "1,000.00", "1.000,00", "1'000.00", "1'000,00", "1 000,00"};
    }

    private String filename() {
        String filename = new File(this.QIFPath).getName();
        return filename.substring(0, filename.length() - 4);
    }

    private void updateProgressBar() {
        if (this.numberOfLines > 50 && (this.currentLine * 100) / this.numberOfLines != this.oldNumber) {
            this.oldNumber = (this.currentLine * 100) / this.numberOfLines;
            Handler h = ((HandlerActivity) this.context).getHandler();
            h.sendMessage(Message.obtain(h, 4, (this.currentLine * 100) / this.numberOfLines, 0));
        }
    }

    public void importIntoDatabase(HandlerActivity act) {
        this.act = act;
        this.oldNumber = -1;
        try {
            String line = this.lines.get(this.currentLine);
            while (line.length() == 0) {
                this.currentLine++;
                line = this.lines.get(this.currentLine);
            }
            if (line.startsWith("!Type:Bank") || line.startsWith("!Type:Cash") || line.startsWith("!Type:CCard") || line.startsWith("!Type:Asset") || line.startsWith("!Type:Oth A") || line.startsWith("!Type:Oth L") || line.startsWith("!Type:Liability") || line.startsWith("!Type:Invst")) {
                this.qifOld = Boolean.TRUE;
                this.invalidQIF = false;
            }
            SQLiteDatabase db = Database.currentDB();
            db.beginTransaction();
            this.accountNameBeingImported = filename();
            while (this.currentLine < this.numberOfLines) {
                updateProgressBar();
                line = this.lines.get(this.currentLine);
                if (line.startsWith("!Account")) {
                    this.invalidQIF = false;
                    processAccounts();
                } else if (line.startsWith("!Type:Cat")) {
                    this.invalidQIF = false;
                    processCategories();
                } else if (!line.startsWith("!Type:Budget")) {
                    if (line.startsWith("!Type:Class") || line.startsWith("!Type:Tag")) {
                        this.invalidQIF = false;
                        processClasses();
                    } else if (line.startsWith("!Type:Cash")) {
                        this.invalidQIF = false;
                        processTransactions();
                    } else if (line.startsWith("!Type:CCard")) {
                        this.invalidQIF = false;
                        processTransactions();
                    } else if (line.startsWith("!Type:Bank")) {
                        this.invalidQIF = false;
                        processTransactions();
                    } else if (line.startsWith("!Type:Oth A")) {
                        this.invalidQIF = false;
                        processTransactions();
                    } else if (line.startsWith("!Type:Oth L")) {
                        this.invalidQIF = false;
                        processTransactions();
                    } else if (line.startsWith("!Type:Invst")) {
                        this.invalidQIF = false;
                        processTransactions();
                    }
                }
                this.currentLine++;
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            act.getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "Import Complete"));
            if (this.invalidQIF) {
                displayError(Locales.kLOC_QIF_TYPEENCODINGERROR, true);
            } else if (this.qifOld) {
                AccountDB.setLastExportTimestampForAccount(this.accountNameBeingImported);
            } else {
                AccountDB.setLastExportTimestampForAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
            }
        } catch (Exception e) {
            displayError(Locales.kLOC_QIF_TYPEENCODINGERROR + "\n\n" + e.getMessage(), true);
        }
    }

    private void processAccounts() {
        String notes = "";
        double limit = 0.0d;
        boolean noLimit = true;
        int accountType = 0;
        this.currentLine++;
        while (this.currentLine < this.numberOfLines) {
            updateProgressBar();
            String line = this.lines.get(this.currentLine);
            if (line != null) {
                if (line.startsWith("^")) {
                    if (AccountClass.idForAccount(this.accountNameBeingImported) == 0) {
                        String nullCheck = new String();
                        ContentValues content = new ContentValues();
                        content.put("timestamp", System.currentTimeMillis() / 1000);
                        content.put("account", this.accountNameBeingImported);
                        content.put("type", accountType);
                        content.put("notes", notes);
                        content.put("limitAmount", limit);
                        content.put("noLimit", noLimit);
                        content.put("totalWorth", Boolean.TRUE);
                        content.put("displayOrder", 0);
                        Database.currentDB().insert(Database.ACCOUNTS_TABLE_NAME, nullCheck, content);
                    }
                    notes = "";
                    limit = 0.0d;
                    noLimit = true;
                    accountType = 0;
                } else if (line.startsWith("N")) {
                    this.accountNameBeingImported = line.substring(1);
                } else if (line.startsWith("D")) {
                    notes = line.substring(1);
                } else if (line.startsWith("T")) {
                    accountType = QIFTypeToAccountType(line.substring(1));
                } else if (line.startsWith("L")) {
                    limit = amountFromQIF(line.substring(1));
                    noLimit = false;
                } else if (line.startsWith("!")) {
                    this.currentLine--;
                    return;
                }
            }
            this.currentLine++;
        }
    }

    private void processClasses() {
        String className = null;
        this.currentLine++;
        while (this.currentLine < this.numberOfLines) {
            updateProgressBar();
            if (this.lines.get(this.currentLine) != null) {
                String line = this.lines.get(this.currentLine);
                if (line.startsWith("^")) {
                    if (className != null && ClassNameClass.idForClass(className) == 0) {
                        ClassNameClass.insertIntoDatabase(className);
                    }
                } else if (line.startsWith("N")) {
                    className = line.substring(1);
                } else if (!line.startsWith("D") && line.startsWith("!")) {
                    this.currentLine--;
                    return;
                }
            }
            this.currentLine++;
        }
    }

    private void processCategories() {
        String category = null;
        boolean income = false;
        double budget = 0.0d;
        this.currentLine++;
        while (this.currentLine < this.numberOfLines) {
            updateProgressBar();
            String line = this.lines.get(this.currentLine);
            if (line != null) {
                if (line.startsWith("^")) {
                    CategoryClass categoryRecord = new CategoryClass(CategoryClass.idForCategoryElseAddIfMissing(category, true));
                    if (Prefs.getBooleanPref(Prefs.QIF_IMPORT_BUDGETS)) {
                        categoryRecord.setBudgetPeriod(2);
                        categoryRecord.setBudgetLimit(Math.abs(budget));
                        categoryRecord.setType(income ? 1 : 0);
                    }
                    categoryRecord.saveToDatabase();
                    budget = 0.0d;
                    income = false;
                } else if (line.startsWith("N")) {
                    category = line.substring(1);
                } else if (line.startsWith("I")) {
                    income = true;
                } else if (line.startsWith("E")) {
                    income = false;
                } else if (line.startsWith("!")) {
                    this.currentLine--;
                    return;
                } else if (line.startsWith("B")) {
                    budget = amountFromQIF(line.substring(1));
                }
            }
            this.currentLine++;
        }
    }

    private void processTransactions() {
        String splitFlags = "";
        TransactionClass transaction = new TransactionClass();
        if (AccountClass.idForAccount(this.accountNameBeingImported) == 0) {
            AccountClass account = new AccountClass();
            int accountID = AccountClass.idForAccountElseAddIfMissing(this.accountNameBeingImported, true);
            account.setTotalWorth(true);
            account.setNoLimit(true);
            account.saveToDatabase();
        }
        this.currentLine++;
        while (this.currentLine < this.numberOfLines) {
            updateProgressBar();
            String line = this.lines.get(this.currentLine);
            if (line != null) {
                if (line.startsWith("^")) {
                    transaction.setAccount(this.accountNameBeingImported);
                    transaction.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
                    transaction.initType();
                    if (Prefs.getBooleanPref(Prefs.QIF_MARKALLCLEARED)) {
                        transaction.setCleared(true);
                    }
                    if (Prefs.getBooleanPref(Prefs.QIF_IMPORT_DUPS) || !TransactionClass.importedTransactionExists(transaction)) {
                        if (Prefs.getBooleanPref(Prefs.AUTOADD_LOOKUPS)) {
                            Database.autoAddLookupItemsFromTransaction(transaction);
                        }
                        transaction.saveToDatabase();
                    }
                    transaction = new TransactionClass();
                    splitFlags = "";
                } else if (line.startsWith("D")) {
                    transaction.setDate(dateFromQIFDate(line.substring(1)));
                } else if (line.startsWith("C")) {
                    if (line.endsWith("*") || line.endsWith("X") || line.endsWith("x")) {
                        transaction.setCleared(true);
                    } else {
                        transaction.setCleared(false);
                    }
                } else if (line.startsWith("N")) {
                    transaction.setCheckNumber(line.substring(1));
                } else if (line.startsWith("P")) {
                    transaction.setPayee(line.substring(1));
                } else if (line.startsWith("T")) {
                    transaction.setSubTotal(amountFromQIF(line.substring(1)));
                    transaction.setAmount(amountFromQIF(line.substring(1)));
                } else if (line.startsWith("M")) {
                    transaction.setMemo(line.substring(1));
                } else if (line.startsWith("L")) {
                    String tempBuff = line.substring(1);
                    if (tempBuff.contains("/")) {
                        transaction.setClassName(tempBuff.substring(tempBuff.indexOf("/") + 1));
                        if (tempBuff.indexOf("/") == 0) {
                            tempBuff = "";
                        } else {
                            tempBuff = tempBuff.substring(0, tempBuff.indexOf("/"));
                        }
                    }
                    if (!tempBuff.startsWith("[")) {
                        transaction.setCategory(tempBuff);
                    } else if (tempBuff.contains("]")) {
                        transaction.setTransferToAccount(tempBuff.substring(1, tempBuff.indexOf("]")));
                    }
                } else if (line.startsWith("S")) {
                    if (splitFlags.contains("S")) {
                        splitFlags = "S";
                        SplitsClass newSplit = new SplitsClass();
                        newSplit.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
                        ArrayList<SplitsClass> splits = transaction.getSplits();
                        splits.add(newSplit);
                        transaction.setSplits(splits);
                    } else {
                        splitFlags = new StringBuilder(String.valueOf(splitFlags)).append("S").toString();
                    }
                    String tempBuff = line.substring(1);
                    if (tempBuff.contains("/")) {
                        transaction.setClassNameAtIndex(tempBuff.substring(tempBuff.indexOf("/") + 1), transaction.getNumberOfSplits() - 1);
                        if (tempBuff.indexOf("/") == 0) {
                            tempBuff = "";
                        } else {
                            tempBuff = tempBuff.substring(0, tempBuff.indexOf("/"));
                        }
                    }
                    if (!tempBuff.startsWith("[")) {
                        transaction.setCategoryAtIndex(tempBuff, transaction.getNumberOfSplits() - 1);
                    } else if (tempBuff.contains("]")) {
                        transaction.setTransferToAccountAtIndex(tempBuff.substring(1, tempBuff.indexOf("]")), transaction.getNumberOfSplits() - 1);
                    }
                } else if (line.startsWith("E")) {
                    if (splitFlags.contains("E")) {
                        splitFlags = "E";
                        SplitsClass newSplit = new SplitsClass();
                        newSplit.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
                        ArrayList<SplitsClass> splits = transaction.getSplits();
                        splits.add(newSplit);
                        transaction.setSplits(splits);
                    } else {
                        splitFlags = new StringBuilder(String.valueOf(splitFlags)).append("E").toString();
                    }
                    transaction.setMemoAtIndex(line.substring(1), transaction.getNumberOfSplits() - 1);
                } else if (line.startsWith("$")) {
                    if (splitFlags.contains("$")) {
                        splitFlags = "$";
                        SplitsClass newSplit = new SplitsClass();
                        newSplit.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
                        ArrayList<SplitsClass> splits = transaction.getSplits();
                        splits.add(newSplit);
                        transaction.setSplits(splits);
                    } else {
                        splitFlags = new StringBuilder(String.valueOf(splitFlags)).append("$").toString();
                    }
                    transaction.setAmountAtIndex(amountFromQIF(line.substring(1)), transaction.getNumberOfSplits() - 1);
                } else if (line.startsWith("!")) {
                    this.currentLine--;
                    return;
                }
            }
            this.currentLine++;
        }
    }

    private int QIFTypeToAccountType(String type) {
        if (type.equals("Bank")) {
            return 0;
        }
        if (type.equals("Cash")) {
            return 1;
        }
        if (type.equals("CCard")) {
            return 2;
        }
        if (type.equals("Oth A")) {
            return 3;
        }
        if (type.equals("Oth L")) {
            return 4;
        }
        if (type.equals("Invst") || type.equals("Port")) {
            return 9;
        }
        return 1;
    }

    private double amountFromQIF(String text) {
        String numberFormat = Prefs.getStringPref(Prefs.QIF_NUMBERFORMAT);
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
            e.printStackTrace();
            Log.e("Error", "Error parsing number");
        }
        if (number == null && text.startsWith("-")) {
            try {
                number = numberFormatter.parse(text.substring(1));
            } catch (ParseException e2) {
                e2.printStackTrace();
            }
        }
        return number.doubleValue();
    }

    private GregorianCalendar dateFromQIFDate(String dateString) {
        Date theDate = null;
        String dateFormat = Prefs.getStringPref(Prefs.QIF_DATEFORMAT);
        String dateSeparator = Prefs.getStringPref(Prefs.QIF_DATESEPARATOR);
        SimpleDateFormat dateFormatter = new SimpleDateFormat();
        if (dateSeparator.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            Matcher matcher = Pattern.compile("[^\\w]").matcher(new SimpleDateFormat().toPattern());
            dateSeparator = matcher.find() ? matcher.group(0) : "/";
        }
        if (!dateFormat.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            if (dateFormat.equals("mm/dd'yy") || dateFormat.equals("mm/dd'yyyy")) {
                dateFormatter.applyPattern(dateFormat.replaceAll("/", dateSeparator).replaceAll("mm", "MM").replaceAll("'", "''"));
            } else if (dateFormat.equals("dd/mm'yy") || dateFormat.equals("dd/mm'yyyy")) {
                dateFormatter.applyPattern(dateFormat.replaceAll("/", dateSeparator).replaceAll("mm", "MM").replaceAll("'", "''"));
            } else {
                dateFormatter.applyPattern(dateFormat.replaceAll("mm", "MM").replaceAll("/", dateSeparator));
            }
        }
        try {
            theDate = dateFormatter.parse(dateString);
        } catch (ParseException e) {
        }
        if (theDate == null) {
            boolean dayFirst = Prefs.getStringPref(Prefs.QIF_DATEFORMAT).startsWith("dd");
            ArrayList<String> possibleFormats = new ArrayList();
            if (dayFirst) {
                possibleFormats.add("dd/MM''yy");
                possibleFormats.add("dd/MM''yyyy");
                possibleFormats.add("dd.MM''yy");
                possibleFormats.add("dd.MM''yyyy");
                possibleFormats.add("dd/MM/yy");
                possibleFormats.add("dd/MM/yyyy");
                possibleFormats.add("dd.MM.yy");
                possibleFormats.add("dd.MM.yyyy");
                possibleFormats.add("dd-MM-yy");
                possibleFormats.add("dd-MM-yyyy");
                possibleFormats.add("MM/dd''yy");
                possibleFormats.add("MM/dd''yyyy");
                possibleFormats.add("MM.dd''yy");
                possibleFormats.add("MM.dd''yyyy");
                possibleFormats.add("MM/dd/yy");
                possibleFormats.add("MM/dd/yyyy");
                possibleFormats.add("MM.dd.yy");
                possibleFormats.add("MM.dd.yyyy");
                possibleFormats.add("MM-dd-yy");
                possibleFormats.add("MM-dd-yyyy");
                possibleFormats.add("yyyy/MM/dd");
                possibleFormats.add("yyyy-MM-dd");
            } else {
                possibleFormats.add("MM/dd''yy");
                possibleFormats.add("MM/dd''yyyy");
                possibleFormats.add("MM.dd''yy");
                possibleFormats.add("MM.dd''yyyy");
                possibleFormats.add("MM/dd/yy");
                possibleFormats.add("MM/dd/yyyy");
                possibleFormats.add("MM.dd.yy");
                possibleFormats.add("MM.dd.yyyy");
                possibleFormats.add("MM-dd-yy");
                possibleFormats.add("MM-dd-yyyy");
                possibleFormats.add("dd/MM''yy");
                possibleFormats.add("dd/MM''yyyy");
                possibleFormats.add("dd.MM''yy");
                possibleFormats.add("dd.MM''yyyy");
                possibleFormats.add("dd/MM/yy");
                possibleFormats.add("dd/MM/yyyy");
                possibleFormats.add("dd.MM.yy");
                possibleFormats.add("dd.MM.yyyy");
                possibleFormats.add("dd-MM-yy");
                possibleFormats.add("dd-MM-yyyy");
                possibleFormats.add("yyyy/MM/dd");
                possibleFormats.add("yyyy-MM-dd");
            }
            Iterator it = possibleFormats.iterator();
            while (it.hasNext()) {
                dateFormatter.applyPattern((String) it.next());
                try {
                    theDate = dateFormatter.parse(dateString);
                    if (theDate != null) {
                        break;
                    }
                    continue;
                } catch (ParseException e2) {
                    continue;
                }
            }
        }
        if (theDate == null) {
            return null;
        }
        GregorianCalendar returnCal = new GregorianCalendar();
        returnCal.setTime(theDate);
        return returnCal;
    }

    private void displayError(String msg, boolean fromBackgroundThread) {
        try {
            Database.currentDB().endTransaction();
        } catch (IllegalStateException e) {
        }
        ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 6, msg));
    }

    public boolean exportRecords(String fileName) {
        BufferedWriter bufferedWriter;
        IOException e;
        String QIFData = generateData();
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            String fileDir = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append(fileName).toString();
            File dir = new File(fileDir.substring(0, fileDir.indexOf("/SMMoney/") + "/SMMoney/".length()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BufferedWriter QIFWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), encodingStr));
            try {
                QIFWriter.write(QIFData);
                QIFWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + fileDir.substring(pmExternalPath.length()) + "' placed in Download/PocketMoneyBackup"), 500);
                bufferedWriter = QIFWriter;
                return true;
            } catch (IOException e2) {
                e = e2;
                bufferedWriter = QIFWriter;
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

    public boolean exportRecords() {
        IOException e;
        String QIFData = generateData();
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            String fileDir = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append("SMMoney.qif").toString();
            File dir = new File(fileDir.substring(0, fileDir.indexOf("/SMMoney/") + "/SMMoney/".length()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BufferedWriter QIFWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), encodingStr));
            BufferedWriter bufferedWriter;
            try {
                QIFWriter.write(QIFData);
                QIFWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + fileDir.substring(pmExternalPath.length()) + "' placed in Download/PocketMoneyBackup"), 500);
                bufferedWriter = QIFWriter;
                return true;
            } catch (IOException e2) {
                e = e2;
                bufferedWriter = QIFWriter;
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

    private String generateData() {
        String QIFData = "";
        this.numberOfLines = 0;
        this.currentLine = 0;
        this.oldNumber = -1;
        ArrayList<TransactionClass> transactions = TransactionDB.queryWithFilterOrderByAccount(this.filter);
        this.numberOfLines += transactions.size();
        if (this.numberOfLines == 0) {
            return "";
        }
        if (!this.qifOld) {
            ArrayList<AccountClass> accounts = AccountDB.queryOnViewType(0);
            ArrayList<CategoryClass> categories = CategoryClass.allCategoriesInDatabase();
            ArrayList<String> classes = ClassNameClass.allClassNamesInDatabase();
            this.numberOfLines += accounts.size();
            this.numberOfLines += categories.size();
            this.numberOfLines += classes.size();
            QIFData = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(QIFData)).append(formatAccounts(AccountDB.queryOnViewType(0))).toString())).append(formatCategories(CategoryClass.allCategoriesInDatabase())).toString())).append(formatClasses(ClassNameClass.allClassNamesInDatabase())).toString();
        }
        return new StringBuilder(String.valueOf(QIFData)).append(formatTransactions(transactions)).toString();
    }

    public boolean exportRecords(ArrayList<TransactionClass> transactions) {
        BufferedWriter bufferedWriter;
        IOException e;
        Log.i("** IO-QIF", "IO-QIF");
        String QIFData = generateData(transactions);
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {
            String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
            String fileDir = this.QIFPath == null ? new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append("SMMoney").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).append(".qif").toString() : this.QIFPath;
            Log.i("** Made it here - 1", "1");
            FileOutputStream fos = new FileOutputStream(fileDir);
            Log.i("** Made it here - 2", "2");
            OutputStreamWriter out = new OutputStreamWriter(fos, encodingStr);
            Log.i("** Made it here - 3", "3");
            BufferedWriter QIFWriter = new BufferedWriter(out);
            try {
                QIFWriter.write(QIFData);
                QIFWriter.close();
                ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + fileDir.substring(pmExternalPath.length()) + "' placed in Download/PocketMoneyBackup"), 500);
                bufferedWriter = QIFWriter;
                return true;
            } catch (IOException e2) {
                e = e2;
                bufferedWriter = QIFWriter;
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

    private String generateData(ArrayList<TransactionClass> transactions) {
        String QIFData = "";
        this.numberOfLines = 0;
        this.currentLine = 0;
        this.oldNumber = -1;
        this.numberOfLines += transactions.size();
        updateProgressBar();
        if (this.numberOfLines == 0) {
            return "";
        }
        if (!this.qifOld) {
            ArrayList<AccountClass> accounts = AccountDB.queryOnViewType(0);
            ArrayList<CategoryClass> categories = CategoryClass.allCategoriesInDatabase();
            ArrayList<String> classes = ClassNameClass.allClassNamesInDatabase();
            this.numberOfLines += accounts.size();
            this.numberOfLines += categories.size();
            this.numberOfLines += classes.size();
            QIFData = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(QIFData)).append(formatAccounts(accounts)).toString())).append(formatCategories(categories)).toString())).append(formatClasses(classes)).toString();
        }
        return new StringBuilder(String.valueOf(QIFData)).append(formatTransactions(transactions)).toString();
    }

    private String formatAccounts(ArrayList<AccountClass> accounts) {
        String accountStr = "!Option:AutoSwitch\n!Account\n";
        Iterator it = accounts.iterator();
        while (it.hasNext()) {
            AccountClass account = (AccountClass) it.next();
            this.currentLine++;
            updateProgressBar();
            accountStr = new StringBuilder(String.valueOf(accountStr)).append(formatAccount(account)).toString();
        }
        return new StringBuilder(String.valueOf(accountStr)).append("!Clear:AutoSwitch\n").toString();
    }

    private String formatAccount(AccountClass account) {
        String creditLimit;
        String accountStr = "";
        if (2 != account.getType() || account.getNoLimit()) {
            creditLimit = "";
        } else {
            creditLimit = qifFormatAmount(account.getLimit());
        }
        return new StringBuilder(String.valueOf(accountStr)).append("N").append(account.getAccount()).append("\nD\nT").append(accountTypeToQIFType(account.getType())).append(creditLimit).append("\n^\n").toString();
    }

    private String formatCategories(ArrayList<CategoryClass> categories) {
        String categoryStr = "!Type:Cat\n";
        Iterator it = categories.iterator();
        while (it.hasNext()) {
            String budget;
            CategoryClass category = (CategoryClass) it.next();
            this.currentLine++;
            updateProgressBar();
            if (category.getBudgetLimit() == 0.0d || !Prefs.getBooleanPref(Prefs.QIF_IMPORT_BUDGETS)) {
                budget = "";
            } else {
                String amountString = qifFormatAmount(category.budgetLimitForPeriod(2, new GregorianCalendar()) * ((double) (category.getType() == 0 ? -1 : 1)));
                budget = "B" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\n";
            }
            categoryStr = new StringBuilder(String.valueOf(categoryStr)).append("N").append(category.getCategory()).append("\nD\n").append(category.getType() == 0 ? "E" : "I").append("\n").append(budget).append("^\n").toString();
        }
        return categoryStr;
    }

    private String formatClasses(ArrayList<String> classes) {
        String classStr = "!Type:Class\n";
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            String className = (String) it.next();
            this.currentLine++;
            updateProgressBar();
            classStr = new StringBuilder(String.valueOf(classStr)).append("N").append(className).append("\nD\n^\n").toString();
        }
        return classStr;
    }

    private void addToStringBuffer(StringBuffer strBuff, String... strings) {
        for (String s : strings) {
            if (s != null) {
                strBuff.append(s);
            }
        }
    }

    private String formatTransactions(ArrayList<TransactionClass> transactions) {
        String lastAccount = "";
        String returnStr = "";
        StringBuffer buffBuff = new StringBuffer();
        StringBuffer retBuff = new StringBuffer();
        StringBuffer strBuff = new StringBuffer();
        StringBuffer splitBuff = new StringBuffer();
        long startTime = System.currentTimeMillis();
        Iterator it = transactions.iterator();
        while (it.hasNext()) {
            TransactionClass transaction = (TransactionClass) it.next();
            this.currentLine++;
            updateProgressBar();
            strBuff.setLength(0);
            transaction.hydrate();
            try {
                if (!lastAccount.equals(transaction.getAccount())) {
                    String accountQIFType = accountTypeToQIFType(AccountDB.recordFor(transaction.getAccount()).getType());
                    if (!this.qifOld) {
                        AccountClass account = (AccountClass) it.next();
                        addToStringBuffer(strBuff, "!Account\n");
                        addToStringBuffer(strBuff, formatAccount(account));
                    }
                    addToStringBuffer(strBuff, "!Type:", accountQIFType, "\n");
                    lastAccount = transaction.getAccount();
                }
                addToStringBuffer(strBuff, "D", qifFormatDate(transaction.getDate()), "\nT", qifFormatAmount(transaction.getSubTotal()));
                if (transaction.getCleared()) {
                    String[] strArr = new String[2];
                    strArr[0] = "\nC";
                    strArr[1] = transaction.getCleared() ? "*" : "";
                    addToStringBuffer(strBuff, strArr);
                }
                addToStringBuffer(strBuff, "\n");
                buffBuff.setLength(0);
                if (transaction.getTransferToAccount() == null) {
                    addToStringBuffer(buffBuff, transaction.getCategory());
                } else if (transaction.getTransferToAccount().length() > 0) {
                    addToStringBuffer(buffBuff, "[" + transaction.getTransferToAccount(), "]");
                }
                if (transaction.getClassName() != null && transaction.getClassName().length() > 0) {
                    addToStringBuffer(buffBuff, "/", transaction.getClassName());
                }
                addToStringBuffer(strBuff, "L", buffBuff.toString(), "\n");
                if (transaction.getPayee() != null && transaction.getPayee().length() > 0) {
                    addToStringBuffer(strBuff, "P", transaction.getPayee(), "\n");
                }
                if (transaction.getCheckNumber() != null && transaction.getCheckNumber().length() > 0) {
                    addToStringBuffer(strBuff, "N", transaction.getCheckNumber(), "\n");
                }
                if (transaction.getMemo() != null && transaction.getMemo().length() > 0) {
                    addToStringBuffer(strBuff, "M", transaction.getMemo().replace("\n", "<br>"), "\n");
                }
                if (transaction.getNumberOfSplits() > 1) {
                    splitBuff.setLength(0);
                    Iterator it2 = transaction.getSplits().iterator();
                    while (it2.hasNext()) {
                        SplitsClass split = (SplitsClass) it2.next();
                        buffBuff.setLength(0);
                        if (split.getTransferToAccount() != null && split.getTransferToAccount().length() > 0) {
                            addToStringBuffer(buffBuff, "[", split.getTransferToAccount(), "]");
                        } else if (split.getCategory() != null && split.getCategory().length() > 0) {
                            addToStringBuffer(buffBuff, split.getCategory());
                        }
                        if (split.getClassName() != null && split.getClassName().length() > 0) {
                            addToStringBuffer(buffBuff, "/", split.getClassName());
                        }
                        addToStringBuffer(splitBuff, "S", buffBuff.toString(), "\n");
                        if (transaction.getMemo() != null && transaction.getMemo().length() > 0) {
                            addToStringBuffer(splitBuff, "E", split.getMemo().replace("\n", "<br>"), "\n");
                        }
                        addToStringBuffer(splitBuff, "$", qifFormatAmount(split.getAmount()), "\n");
                    }
                    addToStringBuffer(strBuff, splitBuff.toString());
                }
                addToStringBuffer(strBuff, "^\n");
                transaction.dehydrate();
                retBuff.append(strBuff.toString());
            } catch (NullPointerException e) {
                Log.e(SMMoney.TAG, "Null pointer in format transaction QIFimportexport");
            }
        }
        return retBuff.toString();
    }

    private String qifFormatDate(GregorianCalendar dateCalendar) {
        Date date = new Date(dateCalendar.getTimeInMillis());
        String dateFormat = Prefs.getStringPref(Prefs.QIF_DATEFORMAT);
        String dateSeparator = Prefs.getStringPref(Prefs.QIF_DATESEPARATOR);
        SimpleDateFormat dateFormatter = new SimpleDateFormat();
        if (dateSeparator.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            dateSeparator = "/";
        }
        if (dateFormat.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            dateSeparator = "/";
            dateFormatter.applyPattern("MM/dd/yy");
        } else if (dateFormat.equals("mm/dd'yy") || dateFormat.equals("mm/dd'yyyy")) {
            dateFormatter.applyPattern(dateFormat.replaceAll("/", dateSeparator).replaceAll("mm", "MM").replaceAll("'", "''"));
        } else if (dateFormat.equals("dd/mm'yy") || dateFormat.equals("dd/mm'yyyy")) {
            dateFormatter.applyPattern(dateFormat.replaceAll("/", dateSeparator).replaceAll("mm", "MM").replaceAll("'", "''"));
        } else {
            dateFormatter.applyPattern(dateFormat.replaceAll("mm", "MM").replaceAll("/", dateSeparator));
        }
        String testStr = new StringBuilder(String.valueOf(dateFormatter.format(date))).toString();
        return dateFormatter.format(date);
    }

    private String qifFormatAmount(double amount) {
        String numberFormat = Prefs.getStringPref(Prefs.QIF_NUMBERFORMAT);
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        if (!numberFormat.equals("1,000.00")) {
            if (numberFormat.equals("1.000,00")) {
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
        }
        return new DecimalFormat("#,##0.00#", formatSymbols).format(amount);
    }

    private String accountTypeToQIFType(int type) {
        switch (type) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
            case LookupsListActivity.ID_LOOKUP /*7*/:
                return "Bank";
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return "Cash";
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                return "CCard";
            case SplitsActivity.REQUEST_EDIT /*3*/:
                return "Oth A";
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                return "Oth L";
            case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                return "Invst";
            default:
                return "Bank";
        }
    }
}
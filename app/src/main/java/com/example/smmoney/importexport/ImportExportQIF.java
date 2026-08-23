package com.example.smmoney.importexport;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportExportQIF {
    public String QIFPath;
    public String accountNameBeingImported;
    private final Context context;
    private final ArrayList<String> lines = new ArrayList<>();
    private int currentLine;
    private FilterClass filter;
    private boolean importFileExists = false;
    private boolean invalidQIF;
    private int numberOfLines;
    private int oldNumber = -1;
    private Boolean qifOld = Boolean.FALSE;

    public ImportExportQIF(Context context) {
        this.context = context;
        this.filter = new FilterClass();
    }

    public ImportExportQIF(String filePath, Context context) {
        Log.i("*** FilePath = ", filePath);
        this.context = context;
        this.QIFPath = filePath;
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        try (BufferedReader QIFReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filePath)), encodingStr))) {
            String readLine;
            while ((readLine = QIFReader.readLine()) != null) {
                this.lines.add(readLine);
            }
            this.numberOfLines = this.lines.size();
            if (this.numberOfLines == 0) {
                displayError("Error reading QIF file: File is Empty");
                Log.v("FileReader", "Empty File");
                return;
            }
            this.currentLine = 0;
            this.importFileExists = true;
        } catch (FileNotFoundException e) {
            displayError("Error reading QIF file: " + e);
            Log.v("FileReader", "File Not Found - 01: " + filePath);
        } catch (UnsupportedEncodingException e2) {
            Log.e(SMMoney.TAG, "ImportExportQIF: import encoding " + encodingStr + " not supported", e2);
        } catch (IOException e3) {
            displayError("Error reading QIF file: " + e3);
            Log.e(SMMoney.TAG, "ImportExportQIF: IOException in constructor", e3);
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

    @SuppressWarnings("resource")
    public void importIntoDatabase(HandlerActivity act) {
        this.oldNumber = -1;
        if (this.lines.isEmpty()) {
            return;
        }
        
        try {
            String line = this.lines.get(this.currentLine);
            while (line.isEmpty() && this.currentLine < this.numberOfLines - 1) {
                this.currentLine++;
                line = this.lines.get(this.currentLine);
            }
            if (line.startsWith("!Type:Bank") || line.startsWith("!Type:Cash") || line.startsWith("!Type:CCard") || line.startsWith("!Type:Asset") || line.startsWith("!Type:Oth A") || line.startsWith("!Type:Oth L") || line.startsWith("!Type:Liability") || line.startsWith("!Type:Invst")) {
                this.qifOld = Boolean.TRUE;
                this.invalidQIF = false;
            }
            
            // Suppress resource warning: SQLiteDatabase is an app-wide singleton managed by Database helper and should not be closed here
            @SuppressWarnings("resource")
            SQLiteDatabase db = Database.currentDB();
            db.beginTransaction();
            try {
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
                        } else if (line.startsWith("!Type:Cash") || line.startsWith("!Type:CCard") || line.startsWith("!Type:Bank") || line.startsWith("!Type:Oth A") || line.startsWith("!Type:Oth L") || line.startsWith("!Type:Invst")) {
                            this.invalidQIF = false;
                            processTransactions();
                        }
                    }
                    this.currentLine++;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            act.getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "Import Complete"));
            if (this.invalidQIF) {
                displayError(Locales.kLOC_QIF_TYPEENCODINGERROR);
            } else if (this.qifOld) {
                AccountDB.setLastExportTimestampForAccount(this.accountNameBeingImported);
            } else {
                AccountDB.setLastExportTimestampForAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
            }
        } catch (Exception e) {
            displayError(Locales.kLOC_QIF_TYPEENCODINGERROR + "\n\n" + e.getMessage());
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
                    int accId = AccountClass.idForAccountElseAddIfMissing(this.accountNameBeingImported, true);
                    if (accId != 0) {
                        AccountClass account = new AccountClass(accId);
                        account.setAccount(this.accountNameBeingImported);
                        account.setType(accountType);
                        account.setNotes(notes);
                        account.setLimit(limit);
                        account.setNoLimit(noLimit);
                        account.setTotalWorth(true);
                        account.saveToDatabase();
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
        TransactionClass transaction = new TransactionClass();
        ensureAccountExists(this.accountNameBeingImported);

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
                        
                        // Handle transfer counterpart
                        if (transaction.getTransferToAccount() != null && !transaction.getTransferToAccount().isEmpty()) {
                            ensureAccountExists(transaction.getTransferToAccount());
                            handleTransferCounterpart(transaction, this.accountNameBeingImported, transaction.getTransferToAccount());
                        } else if (transaction.getNumberOfSplits() > 1) {
                            for (SplitsClass split : transaction.getSplits()) {
                                if (split.getTransferToAccount() != null && !split.getTransferToAccount().isEmpty()) {
                                    ensureAccountExists(split.getTransferToAccount());
                                    // Need to adapt handleTransferCounterpart for splits or ignore for now as per legacy behavior
                                }
                            }
                        }
                    }
                    transaction = new TransactionClass();
                } else if (line.startsWith("D")) {
                    transaction.setDate(dateFromQIFDate(line.substring(1)));
                } else if (line.startsWith("C")) {
                    transaction.setCleared(line.endsWith("*") || line.endsWith("X") || line.endsWith("x"));
                } else if (line.startsWith("N")) {
                    transaction.setCheckNumber(line.substring(1));
                } else if (line.startsWith("P")) {
                    transaction.setPayee(line.substring(1));
                } else if (line.startsWith("T") || line.startsWith("U")) {
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
                        transaction.setCategory("");
                    }
                } else if (line.startsWith("S")) {
                    SplitsClass newSplit = new SplitsClass();
                    newSplit.setCurrencyCode(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
                    
                    String tempBuff = line.substring(1);
                    if (tempBuff.contains("/")) {
                        newSplit.setClassName(tempBuff.substring(tempBuff.indexOf("/") + 1));
                        if (tempBuff.indexOf("/") == 0) {
                            tempBuff = "";
                        } else {
                            tempBuff = tempBuff.substring(0, tempBuff.indexOf("/"));
                        }
                    }
                    if (!tempBuff.startsWith("[")) {
                        newSplit.setCategory(tempBuff);
                    } else if (tempBuff.contains("]")) {
                        newSplit.setTransferToAccount(tempBuff.substring(1, tempBuff.indexOf("]")));
                    }
                    
                    ArrayList<SplitsClass> splits = transaction.getSplits();
                    splits.add(newSplit);
                    transaction.setSplits(splits);
                } else if (line.startsWith("E")) {
                    if (transaction.getNumberOfSplits() > 0) {
                        transaction.setMemoAtIndex(line.substring(1), transaction.getNumberOfSplits() - 1);
                    }
                } else if (line.startsWith("$")) {
                    if (transaction.getNumberOfSplits() > 0) {
                        transaction.setAmountAtIndex(amountFromQIF(line.substring(1)), transaction.getNumberOfSplits() - 1);
                    }
                } else if (line.startsWith("!")) {
                    this.currentLine--;
                    return;
                }
            }
            this.currentLine++;
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

    private int QIFTypeToAccountType(String type) {
        return switch (type) {
            case "Cash" -> 1;
            case "CCard" -> 2;
            case "Oth A" -> 3;
            case "Oth L" -> 4;
            case "Invst", "Port" -> 9;
            default -> 0; // Default is Bank (Checking/Savings)
        };
    }

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

    private double amountFromQIF(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0d;
        }
        String cleaned = text.trim();
        DecimalFormat numberFormatter = getNumberFormatter();
        Number number = null;
        try {
            number = numberFormatter.parse(cleaned);
        } catch (ParseException e) {
            Log.e(SMMoney.TAG, "ImportExportQIF: ParseException in amountFromQIF: " + cleaned, e);
        }
        if (number == null && cleaned.startsWith("-")) {
            try {
                number = numberFormatter.parse(cleaned.substring(1).trim());
                if (number != null) {
                    return number.doubleValue() * -1.0d;
                }
            } catch (ParseException e2) {
                Log.e(SMMoney.TAG, "ImportExportQIF: ParseException in amountFromQIF (negative): " + cleaned, e2);
            }
        }
        if (number == null && cleaned.startsWith("(") && cleaned.endsWith(")")) {
            try {
                number = numberFormatter.parse(cleaned.substring(1, cleaned.length() - 1).trim());
                if (number != null) {
                    return number.doubleValue() * -1.0d;
                }
            } catch (ParseException e22) {
                Log.e(SMMoney.TAG, "ImportExportQIF: ParseException in amountFromQIF (parentheses): " + cleaned, e22);
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

    private ArrayList<String> getPossibleDateFormats(boolean dayFirst) {
        ArrayList<String> possibleFormats = new ArrayList<>();
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
        return possibleFormats;
    }

    private GregorianCalendar dateFromQIFDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return new GregorianCalendar();
        }
        String cleaned = dateString.trim();

        // 1. Try ISO 8601
        GregorianCalendar cal = CalExt.dateFromDescriptionWithISO861Date(cleaned);
        if (cal != null) {
            return cal;
        }

        // 2. Try User Preference Format
        Date theDate = null;
        String dateFormat = Prefs.getStringPref(Prefs.QIF_DATEFORMAT);
        String dateSeparator = Prefs.getStringPref(Prefs.QIF_DATESEPARATOR);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("", java.util.Locale.getDefault());
        
        if (dateSeparator.isEmpty() || dateSeparator.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            Matcher matcher = Pattern.compile("\\W").matcher(new SimpleDateFormat("", java.util.Locale.getDefault()).toPattern());
            dateSeparator = matcher.find() ? matcher.group(0) : "/";
        }
        if (dateSeparator == null) {
            dateSeparator = "/";
        }
        
        if (!dateFormat.isEmpty() && !dateFormat.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            final String pattern = dateFormat.replace("/", dateSeparator).replace("mm", "MM").replace("'", "''");
            if (dateFormat.equals("mm/dd'yy") || dateFormat.equals("mm/dd'yyyy")) {
                dateFormatter.applyPattern(pattern);
            } else if (dateFormat.equals("dd/mm'yy") || dateFormat.equals("dd/mm'yyyy")) {
                dateFormatter.applyPattern(pattern);
            } else {
                dateFormatter.applyPattern(dateFormat.replace("mm", "MM").replace("/", dateSeparator));
            }
            try {
                theDate = dateFormatter.parse(cleaned);
            } catch (ParseException e) {
                Log.e(SMMoney.TAG, "ImportExportQIF: ParseException in dateFromQIFDate: " + cleaned, e);
            }
        }
        
        if (theDate == null) {
            boolean dayFirst = Prefs.getStringPref(Prefs.QIF_DATEFORMAT).startsWith("dd");
            ArrayList<String> possibleFormats = getPossibleDateFormats(dayFirst);
            for (String possibleFormat : possibleFormats) {
                dateFormatter.applyPattern(possibleFormat);
                try {
                    theDate = dateFormatter.parse(cleaned);
                    if (theDate != null) {
                        break;
                    }
                } catch (ParseException e2) {
                    // Ignore, try next format
                }
            }
        }
        
        // 3. Fallback to standard system parsers
        if (theDate == null) {
            try {
                theDate = android.text.format.DateFormat.getMediumDateFormat(SMMoney.getAppContext()).parse(cleaned);
            } catch (Exception ignored) {
            }
        }
        if (theDate == null) {
            try {
                theDate = android.text.format.DateFormat.getDateFormat(SMMoney.getAppContext()).parse(cleaned);
            } catch (Exception ignored) {
            }
        }

        if (theDate == null) {
            return new GregorianCalendar();
        }
        GregorianCalendar returnCal = new GregorianCalendar();
        returnCal.setTime(theDate);
        return returnCal;
    }

    private void displayError(String msg) {
        // Suppress resource warning: SQLiteDatabase is an app-wide singleton managed by Database helper and should not be closed here
        @SuppressWarnings("resource")
        SQLiteDatabase db = Database.currentDB();
        try {
            db.endTransaction();
        } catch (IllegalStateException e) {
            Log.e(SMMoney.TAG, "ImportExportQIF: IllegalStateException in displayError", e);
        }
        ((HandlerActivity) this.context).getHandler().sendMessage(Message.obtain(((HandlerActivity) this.context).getHandler(), 6, msg));
    }

    // Suppress warning: Boolean return value might be useful for unit tests or downstream consumers in the future
    @SuppressWarnings("UnusedReturnValue")
    public boolean exportRecords(String fileName) {
        String QIFData = generateData();
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        String filePath = pmExternalPath + fileName;
        
        try (BufferedWriter QIFWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr))) {
            QIFWriter.write(QIFData);
            ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + fileName + "' placed in Download/PocketMoneyBackup"), 500);
            return true;
        } catch (IOException e) {
            Log.v("Export writing error", e.toString());
            displayError(e.toString());
            return false;
        }
    }

    // Suppress warning: Boolean return value might be useful for unit tests or downstream consumers in the future
    @SuppressWarnings("UnusedReturnValue")
    public boolean exportRecords() {
        return exportRecords("SMMoney.qif");
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
            QIFData = QIFData + formatAccounts(AccountDB.queryOnViewType(0)) + formatCategories(CategoryClass.allCategoriesInDatabase()) + formatClasses(ClassNameClass.allClassNamesInDatabase());
        }
        return QIFData + formatTransactions(transactions);
    }

    // Suppress warning: Boolean return value might be useful for unit tests or downstream consumers in the future
    @SuppressWarnings("UnusedReturnValue")
    public boolean exportRecords(ArrayList<TransactionClass> transactions) {
        Log.i("** IO-QIF", "IO-QIF");
        String QIFData = generateData(transactions);
        String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        String fileName = "SMMoney" + CalExt.descriptionWithTimestamp(new GregorianCalendar()) + ".qif";
        String filePath = this.QIFPath == null ? pmExternalPath + fileName : this.QIFPath;
        
        try (BufferedWriter qifWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encodingStr))) {
            qifWriter.write(QIFData);
            if (!Prefs.getBooleanPref(Prefs.QIF_EXPORT_SEPERATELY)) {
                ((HandlerActivity) this.context).getHandler().sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).getHandler(), 5, "File '" + (this.QIFPath == null ? fileName : filePath) + "' placed in Download/PocketMoneyBackup"), 500);
            }
            return true;
        } catch (IOException e) {
            Log.i("Export writing error", e.toString());
            displayError(e.toString());
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
            QIFData = QIFData + formatAccounts(accounts) + formatCategories(categories) + formatClasses(classes);
        }
        return QIFData + formatTransactions(transactions);
    }

    private String formatAccounts(ArrayList<AccountClass> accounts) {
        StringBuilder accountStr = new StringBuilder("!Option:AutoSwitch\n!Account\n");
        for (AccountClass account : accounts) {
            this.currentLine++;
            updateProgressBar();
            accountStr.append(formatAccount(account));
        }
        return accountStr + "!Clear:AutoSwitch\n";
    }

    private String formatAccount(AccountClass account) {
        String creditLimit = "";
        if (2 == account.getType() && !account.getNoLimit()) {
            creditLimit = qifFormatAmount(account.getLimit());
        }
        
        String notes = account.getNotes();
        return "N" + account.getAccount() + "\n"
                + "D" + (notes != null ? notes : "") + "\n"
                + "T" + accountTypeToQIFType(account.getType()) + "\n"
                + (creditLimit.isEmpty() ? "" : "L" + creditLimit + "\n")
                + "^\n";
    }

    private String formatCategories(ArrayList<CategoryClass> categories) {
        StringBuilder categoryStr = new StringBuilder("!Type:Cat\n");
        for (CategoryClass categoryClass : categories) {
            String budget;
            this.currentLine++;
            updateProgressBar();
            if ((categoryClass.getBudgetLimit() == 0.0d) || !Prefs.getBooleanPref(Prefs.QIF_IMPORT_BUDGETS)) {
                budget = "";
            } else {
                String amountString = qifFormatAmount(categoryClass.budgetLimitForPeriod(2, new GregorianCalendar()) * ((double) (categoryClass.getType() == 0 ? -1 : 1)));
                budget = "B" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\nB" + amountString + "\n";
            }
            categoryStr.append("N").append(categoryClass.getCategory()).append("\nD\n").append(categoryClass.getType() == 0 ? "E" : "I").append("\n").append(budget).append("^\n");
        }
        return categoryStr.toString();
    }

    private String formatClasses(ArrayList<String> classes) {
        StringBuilder classStr = new StringBuilder("!Type:Class\n");
        for (String className : classes) {
            this.currentLine++;
            updateProgressBar();
            classStr.append("N").append(className).append("\nD\n^\n");
        }
        return classStr.toString();
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
        //String returnStr = "";
        StringBuffer buffBuff = new StringBuffer();
        StringBuilder retBuff = new StringBuilder();
        StringBuffer strBuff = new StringBuffer();
        StringBuffer splitBuff = new StringBuffer();
        //long startTime = System.currentTimeMillis();
        for (TransactionClass transaction : transactions) {
            this.currentLine++;
            updateProgressBar();
            strBuff.setLength(0);
            transaction.hydrate();
            try {
                if (!lastAccount.equals(transaction.getAccount())) {
                    AccountClass accRecord = AccountDB.recordFor(transaction.getAccount());
                    String accountQIFType = accountTypeToQIFType(accRecord != null ? accRecord.getType() : 0);
                    if (!this.qifOld) {
                        String accountName = transaction.getAccount();
                        AccountClass account = new AccountClass(AccountClass.idForAccount(accountName));
                        //AccountClass account = (AccountClass) it.next();
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
                } else if (!transaction.getTransferToAccount().isEmpty()) {
                    addToStringBuffer(buffBuff, "[" + transaction.getTransferToAccount(), "]");
                }
                if (transaction.getClassName() != null && !transaction.getClassName().isEmpty()) {
                    addToStringBuffer(buffBuff, "/", transaction.getClassName());
                }
                addToStringBuffer(strBuff, "L", buffBuff.toString(), "\n");
                if (transaction.getPayee() != null && !transaction.getPayee().isEmpty()) {
                    addToStringBuffer(strBuff, "P", transaction.getPayee(), "\n");
                }
                if (transaction.getCheckNumber() != null && !transaction.getCheckNumber().isEmpty()) {
                    addToStringBuffer(strBuff, "N", transaction.getCheckNumber(), "\n");
                }
                if (transaction.getMemo() != null && !transaction.getMemo().isEmpty()) {
                    addToStringBuffer(strBuff, "M", transaction.getMemo().replace("\n", "<br>"), "\n");
                }
                if (transaction.getNumberOfSplits() > 1) {
                    splitBuff.setLength(0);
                    for (SplitsClass split : transaction.getSplits()) {
                        buffBuff.setLength(0);
                        if (split.getTransferToAccount() != null && !split.getTransferToAccount().isEmpty()) {
                            addToStringBuffer(buffBuff, "[", split.getTransferToAccount(), "]");
                        } else if (split.getCategory() != null && !split.getCategory().isEmpty()) {
                            addToStringBuffer(buffBuff, split.getCategory());
                        }
                        if (split.getClassName() != null && !split.getClassName().isEmpty()) {
                            addToStringBuffer(buffBuff, "/", split.getClassName());
                        }
                        addToStringBuffer(splitBuff, "S", buffBuff.toString(), "\n");
                        if (split.getMemo() != null && !split.getMemo().isEmpty()) {
                            addToStringBuffer(splitBuff, "E", split.getMemo().replace("\n", "<br>"), "\n");
                        }
                        addToStringBuffer(splitBuff, "$", qifFormatAmount(split.getAmount()), "\n");
                    }
                    addToStringBuffer(strBuff, splitBuff.toString());
                }
                addToStringBuffer(strBuff, "^\n");
                transaction.dehydrate();
                retBuff.append(strBuff);
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
        SimpleDateFormat dateFormatter = new SimpleDateFormat("", java.util.Locale.getDefault());
        
        if (dateSeparator.isEmpty() || dateSeparator.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            dateSeparator = "/";
        }
        
        if (dateFormat.equals(Locales.kLOC_GENERAL_DEFAULT)) {
            dateFormatter.applyPattern("MM/dd/yy");
        } else {
            final String pattern = dateFormat.replace("/", dateSeparator).replace("mm", "MM").replace("'", "''");
            if (dateFormat.equals("mm/dd'yy") || dateFormat.equals("mm/dd'yyyy")) {
                dateFormatter.applyPattern(pattern);
            } else if (dateFormat.equals("dd/mm'yy") || dateFormat.equals("dd/mm'yyyy")) {
                dateFormatter.applyPattern(pattern);
            } else {
                dateFormatter.applyPattern(dateFormat.replace("mm", "MM").replace("/", dateSeparator));
            }
        }
        return dateFormatter.format(date);
    }

    private String qifFormatAmount(double amount) {
        DecimalFormat numberFormatter = getNumberFormatter();
        return numberFormatter.format(amount);
    }

    private String accountTypeToQIFType(int type) {
        return switch (type) { /*0*//*6*/
            case Enums.kAccountTypeChecking, Enums.kAccountTypeSavings,
                 Enums.kAccountTypeMoneyMarket /*7*/ -> "Bank";
            case Enums.kAccountTypeCash /*1*/ -> "Cash";
            case Enums.kAccountTypeCreditCard /*2*/ -> "CCard";
            case Enums.kAccountTypeAsset /*3*/ -> "Oth A"; /*4*/
            case Enums.kAccountTypeLiability, Enums.kAccountTypeCreditLine /*8*/ -> "Oth L";
            case Enums.kAccountTypeInvestment /*9*/ -> "Invst";
            default -> "Bank";
        };
    }
}

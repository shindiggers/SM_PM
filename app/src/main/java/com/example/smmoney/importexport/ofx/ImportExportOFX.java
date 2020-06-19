package com.example.smmoney.importexport.ofx;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ImportExportOFX {
    public String accountNameBeingImported;
    private final Context context;
    @SuppressWarnings("unused")
    int currentLine;
    @SuppressWarnings({"unused", "rawtypes"})
    List data;
    @SuppressWarnings({"unused", "rawtypes"})
    List lines;
    @SuppressWarnings("unused")
    int numberOfLines;
    @SuppressWarnings("unused")
    private DateFormat dateFormatter;
    public FilterClass filter;
    @SuppressWarnings("unused")
    private String defaultCurrencyCode;
    @SuppressWarnings("unused")
    private HandlerActivity act;
    @SuppressWarnings("unused")
    private NumberFormat numberFormatter;
    private OFXClass ofxData;
    public String path;

    @SuppressWarnings("unused")
    public ImportExportOFX(Context context, FilterClass filter) {
        this.context = context;
        this.filter = filter;
        this.defaultCurrencyCode = Prefs.getStringPref("prefscurrencyhomecurrency");
        this.dateFormatter = null;
        this.numberFormatter = null;
    }

    public ImportExportOFX(Context context, String path) {
        this.context = context;
        this.path = path;
        this.defaultCurrencyCode = Prefs.getStringPref("prefscurrencyhomecurrency");
        this.dateFormatter = null;
        this.numberFormatter = null;
    }

    @SuppressWarnings("unused")
    public ImportExportOFX(Context context, String urlPath, boolean notUsed) {
        this.context = context;
        boolean isURL = true;
        this.path = urlPath;
        this.defaultCurrencyCode = Prefs.getStringPref("prefscurrencyhomecurrency");
        this.dateFormatter = null;
        this.numberFormatter = null;
    }

    @SuppressWarnings("unused")
    public static List<String> dateFormats() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_GENERAL_DEFAULT);
        list.add("mm/dd'yy");
        list.add("mm/dd'yyyy");
        list.add("mm/dd/yy");
        list.add("mm/dd/yyyy");
        list.add("dd/mm'yy");
        list.add("dd/mm'yyyy");
        list.add("dd/mm/yy");
        list.add("dd/mm/yyyy");
        list.add("yyyy/mm/dd");
        return list;
    }

    @SuppressWarnings("unused")
    public static List<String> dateSeparators() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_GENERAL_DEFAULT);
        list.add("/");
        list.add(".");
        list.add("-");
        return list;
    }

    @SuppressWarnings("unused")
    public static List<String> numberFormats() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_GENERAL_DEFAULT);
        list.add("1,000.00");
        list.add("1.000,00");
        list.add("1'000.00");
        list.add("1'000,00");
        list.add("1 000,00");
        return list;
    }

    @SuppressWarnings("unused")
    private int OFXTypeToAccountType(String type) {
        switch (type) {
            case "Bank":
                return 0;
            case "CCard":
                return 2;
            case "Oth A":
                return 3;
            case "Oth L":
                return 4;
            case "Cash":
            default:
                return 1;
        }
    }

    private void displayError(String error) {
        Builder builder = new Builder(SMMoney.getAppContext());
        builder.setTitle(error);
        builder.setPositiveButton(Locales.kLOC_GENERAL_OK, null);
        builder.create().show();
    }

    @SuppressWarnings("unused")
    private String fileName() {
        if (this.path.endsWith("/")) {
            this.path = this.path.substring(0, -1 + this.path.length());
        }

        return this.path.substring(this.path.lastIndexOf("/") + 1);
    }

    private void processAccounts() {
        int accountID = AccountClass.idForAccountNumber(this.ofxData.statement.account.accountID, this.ofxData.statement.account.bankID);
        if (accountID != 0) {
            this.accountNameBeingImported = new AccountClass(accountID).getAccount();
        } else {
            String bankID;
            if (this.ofxData.statement.account.bankID != null && this.ofxData.statement.account.bankID.length() > 0) {
                bankID = this.ofxData.statement.account.bankID;
            } else {
                bankID = "";
            }

            StringBuilder bankIdPlusAccountId = new StringBuilder(bankID);
            String separator;
            if (this.ofxData.statement.account.bankID == null || this.ofxData.statement.account.bankID.length() <= 0) {
                separator = "";
            } else {
                separator = "-";
            }

            this.accountNameBeingImported = bankIdPlusAccountId.append(separator).append(this.ofxData.statement.account.accountID).toString();
            AccountClass account = new AccountClass(accountID);
            account.setAccount(this.accountNameBeingImported);
            account.setTotalWorth(true);
            account.setNoLimit(true);
            account.setLimit(0.0D);
            account.setType(this.ofxData.statement.account.ofxAccountTypeAsSMMoneyAccountType());
            account.setAccountNumber(this.ofxData.statement.account.accountID);
            account.setRoutingNumber(this.ofxData.statement.account.bankID);
            account.setCurrencyCode(this.ofxData.statement.defaultCurrency);
            account.saveToDatabase();
        }
    }

    private void processTransactions() {
        // $FF: Couldn't be decompiled
        TransactionClass transaction;
        for (OFX_TransactionClass record : this.ofxData.statement.ofxtransactions) {
            int transactionID = TransactionDB.transactionIDForOFXID(record.fitID);
            if (transactionID == 0) {
                try {
                    Integer.parseInt(record.checknum);
                    transactionID = TransactionDB.transactionIDForCheckNumber(record.checknum, record.amount, record.dtuser != null ? record.dtuser : record.dtposted, this.accountNameBeingImported);
                } catch (NumberFormatException e) {
                    transactionID = TransactionDB.transactionIDForAmount(record.amount, record.dtuser != null ? record.dtuser : record.dtposted, this.accountNameBeingImported);
                }
            }
            if (transactionID != 0) {
                transaction = new TransactionClass(transactionID);
                transaction.hydrate();
            } else {
                transaction = new TransactionClass();
                transaction.setAccount(this.accountNameBeingImported);
                transaction.setSubTotal(record.amount);
                transaction.setAmount(record.amount);
                transaction.setDate(record.dtuser != null ? record.dtuser : record.dtposted);
            }
            transaction.setOfxID(record.fitID);
            transaction.setCurrencyCode(this.ofxData.statement.defaultCurrency);
            if (transaction.getPayee() == null || transaction.getPayee().length() == 0) {
                transaction.setPayee(record.name);
            }
            if (transaction.getMemo() == null || transaction.getMemo().length() == 0) {
                transaction.setMemo(record.memo);
            }
            if (transaction.getCheckNumber() == null || transaction.getCheckNumber().length() == 0) {
                transaction.setCheckNumber((record.checknum == null || record.checknum.length() <= 0) ? record.transactionTypeAsString() : record.checknum);
            }
            transaction.setCleared(true);
            transaction.initType();
            if (transaction.transactionID == 0 && transaction.getPayee() != null && transaction.getPayee().length() > 0) {
                TransactionClass foundMatchingTransaction = TransactionDB.closestTransactionMatchFor(transaction.getPayee(), transaction.getAccount());
                if (foundMatchingTransaction != null) {
                    transaction.setCategory(foundMatchingTransaction.getCategory());
                    transaction.setClassName(foundMatchingTransaction.getClassName());
                }
            }
            transaction.saveToDatabase();
        }
    }

    public boolean exportRecords(List<TransactionClass> transactions) {
        String ofxData = this.generateData(transactions);
        String fileDir = this.path;
        IOException ioException;
        BufferedWriter bufferedWriter;
        try {
            String ofxEncoding = Prefs.getStringPref("prefsdatatransfersfileencoding");
            File file = new File(fileDir.substring(0, 1 + fileDir.lastIndexOf("/")));
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.mkdirs();
            }

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), ofxEncoding));
            try {
                bufferedWriter.write(ofxData);
                bufferedWriter.close();
                ((HandlerActivity) this.context).getHandler().
                        sendMessageDelayed(Message.obtain(((HandlerActivity) this.context).
                                        getHandler(),
                                5,
                                "File '" + fileDir.substring(fileDir.lastIndexOf("/") + 1) + "' placed in Download/PocketMoneyBackup"),
                                500);
                return true;
            } catch (IOException e2) {
                ioException = e2;
                Log.v("Export writing error", ioException.toString());
                displayError(e2.toString());
                return false;
            }
        } catch (IOException e3) {
            ioException = e3;
            Log.v("Export writing error", ioException.toString());
            displayError(e3.toString());
            return false;
        }
    }

    private String generateData(List<TransactionClass> transactions) {
        if (transactions.size() > 0) {
            OFXClass ofxClass = new OFXClass();
            ofxClass.transactions = transactions;
            int accountID = AccountClass.idForAccount((transactions.get(0)).getAccount());
            if (accountID != 0) {
                ofxClass.account = new AccountClass(accountID);
                ofxClass.account.hydrate();
                return ofxClass.toString();
            }
        }
        return "";
    }

    public void importIntoDatabase() {
        // $FF: Couldn't be decompiled
        Database.currentDB().beginTransaction();
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        BufferedReader QIFReader = null;
        try {
            QIFReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(this.path)), encodingStr));
        } catch (FileNotFoundException e) {
            displayError("Error reading QIF file: " + e.toString());
            Log.v("FileReader", "File Not Found");
            return;
        } catch (UnsupportedEncodingException e2) {
            Log.e(SMMoney.TAG, "import encoding " + encodingStr + " not supported");
            e2.printStackTrace();
        }
        StringBuilder strBuff = new StringBuilder(10000);
        while (true) {
            try {
                String readLine = null;
                if (QIFReader != null) {
                    readLine = QIFReader.readLine();
                }
                if (readLine == null) {
                    break;
                }
                strBuff.append(readLine);
                strBuff.append("\n");
            } catch (IOException e3) {
                displayError("Error reading QIF file: " + e3.toString());
                e3.printStackTrace();
            }
        }
        if (strBuff.length() == 0) {
            displayError("Empty file : " + this.path);
        }
        this.ofxData = new OFXClass(strBuff.toString());
        processAccounts();
        processTransactions();
        Database.currentDB().setTransactionSuccessful();
        Database.currentDB().endTransaction();
    }
}

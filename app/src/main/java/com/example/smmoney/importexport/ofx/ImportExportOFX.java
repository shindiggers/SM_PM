package com.example.smmoney.importexport.ofx;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
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
import java.util.List;

// The app's architecture uses a managed singleton database instance via Database.currentDB() that remains open across the app lifecycle.
@SuppressWarnings({"resource"})
public class ImportExportOFX {
    public String accountNameBeingImported;
    private final Context context;
    public FilterClass filter;
    private OFXClass ofxData;
    public String path;

    public ImportExportOFX(Context context, String path) {
        this.context = context;
        this.path = path;
    }

    private void displayError(String error) {
        Builder builder = new Builder(SMMoney.getAppContext(), PocketMoneyThemes.dialogTheme());
        builder.setTitle(error);
        builder.setPositiveButton(Locales.kLOC_GENERAL_OK, null);
        builder.create().show();
    }

    private void processAccounts() {
        if (this.ofxData == null || this.ofxData.statement == null || this.ofxData.statement.account == null) {
            return;
        }
        int accountID = AccountClass.idForAccountNumber(this.ofxData.statement.account.accountID, this.ofxData.statement.account.bankID);
        if (accountID != 0) {
            this.accountNameBeingImported = new AccountClass(accountID).getAccount();
        } else {
            String bankID = (this.ofxData.statement.account.bankID != null && !this.ofxData.statement.account.bankID.isEmpty())
                    ? this.ofxData.statement.account.bankID
                    : "";

            String separator = bankID.isEmpty() ? "" : "-";
            String accountNum = this.ofxData.statement.account.accountID != null ? this.ofxData.statement.account.accountID : "OFXAccount";
            this.accountNameBeingImported = bankID + separator + accountNum;

            AccountClass account = new AccountClass();
            account.setAccount(this.accountNameBeingImported);
            account.setTotalWorth(true);
            account.setNoLimit(true);
            account.setLimit(0.0D);
            account.setType(this.ofxData.statement.account.ofxAccountTypeAsSMMoneyAccountType());
            account.setAccountNumber(this.ofxData.statement.account.accountID);
            account.setRoutingNumber(this.ofxData.statement.account.bankID);
            String currency = this.ofxData.statement.defaultCurrency;
            if (currency == null || currency.isEmpty()) {
                currency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
            }
            account.setCurrencyCode(currency);
            account.saveToDatabase();
        }
    }

    private void processTransactions() {
        if (this.ofxData == null || this.ofxData.statement == null || this.ofxData.statement.ofxtransactions == null) {
            return;
        }

        for (OFX_TransactionClass record : this.ofxData.statement.ofxtransactions) {
            int transactionID = TransactionDB.transactionIDForOFXID(record.fitID);
            if (transactionID == 0) {
                if (record.checknum != null && !record.checknum.isEmpty()) {
                    try {
                        Integer.parseInt(record.checknum);
                        transactionID = TransactionDB.transactionIDForCheckNumber(record.checknum, record.amount, record.dtuser != null ? record.dtuser : record.dtposted, this.accountNameBeingImported);
                    } catch (NumberFormatException ignored) {
                        transactionID = TransactionDB.transactionIDForAmount(record.amount, record.dtuser != null ? record.dtuser : record.dtposted, this.accountNameBeingImported);
                    }
                } else {
                    transactionID = TransactionDB.transactionIDForAmount(record.amount, record.dtuser != null ? record.dtuser : record.dtposted, this.accountNameBeingImported);
                }
            }

            TransactionClass transaction;
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
            String currency = this.ofxData.statement.defaultCurrency;
            if (currency == null || currency.isEmpty()) {
                currency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
            }
            transaction.setCurrencyCode(currency);

            if (transaction.getPayee() == null || transaction.getPayee().isEmpty()) {
                transaction.setPayee(record.name);
            }
            if (transaction.getMemo() == null || transaction.getMemo().isEmpty()) {
                transaction.setMemo(record.memo);
            }
            if (transaction.getCheckNumber() == null || transaction.getCheckNumber().isEmpty()) {
                transaction.setCheckNumber((record.checknum == null || record.checknum.isEmpty()) ? record.transactionTypeAsString() : record.checknum);
            }
            transaction.setCleared(true);
            transaction.initType();

            if (transaction.transactionID == 0 && transaction.getPayee() != null && !transaction.getPayee().isEmpty()) {
                TransactionClass foundMatchingTransaction = TransactionDB.closestTransactionMatchFor(transaction.getPayee(), transaction.getAccount());
                if (foundMatchingTransaction != null) {
                    transaction.setCategory(foundMatchingTransaction.getCategory());
                    transaction.setClassName(foundMatchingTransaction.getClassName());
                }
            }
            transaction.saveToDatabase();
        }
    }

    // Suppress warning: keeping boolean return value for interface consistency across exporters and caller error checking
    @SuppressWarnings("UnusedReturnValue")
    public boolean exportRecords(List<TransactionClass> transactions) {
        String data = this.generateData(transactions);
        String fileDir = this.path;
        try {
            String ofxEncoding = Prefs.getStringPref("prefsdatatransfersfileencoding");
            if (ofxEncoding.isEmpty()) {
                ofxEncoding = "US-ASCII";
            }
            File file = new File(fileDir);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                parentDir.mkdirs();
            }

            try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ofxEncoding))) {
                bufferedWriter.write(data);
            }

            String fileName = file.getName();
            if (this.context instanceof HandlerActivity handlerActivity) {
                handlerActivity.getHandler().
                        sendMessageDelayed(Message.obtain(handlerActivity.getHandler(),
                                        5,
                                        "File '" + fileName + "' placed in Download/PocketMoneyBackup"),
                                500);
            }
            return true;
        } catch (IOException e) {
            Log.v("Export writing error", e.toString());
            displayError(e.toString());
            return false;
        }
    }

    private String generateData(List<TransactionClass> transactions) {
        if (transactions != null && !transactions.isEmpty()) {
            OFXClass exportOfx = new OFXClass();
            exportOfx.transactions = transactions;
            int accountID = AccountClass.idForAccount(transactions.get(0).getAccount());
            if (accountID != 0) {
                exportOfx.account = new AccountClass(accountID);
                exportOfx.account.hydrate();
                return exportOfx.toString();
            }
        }
        return "";
    }

    public void importIntoDatabase() {
        Database.currentDB().beginTransaction();
        String encodingStr = Prefs.getStringPref(Prefs.ENCODING);
        if (encodingStr.isEmpty()) {
            encodingStr = "UTF-8";
        }
        StringBuilder strBuff = new StringBuilder(10000);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(this.path)), encodingStr))) {
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                strBuff.append(readLine).append("\n");
            }
        } catch (FileNotFoundException e) {
            displayError("Error reading OFX file: " + e);
            Log.v("FileReader", "File Not Found: " + this.path);
            Database.currentDB().endTransaction();
            return;
        } catch (UnsupportedEncodingException e2) {
            Log.e(SMMoney.TAG, "ImportExportOFX: import encoding " + encodingStr + " not supported", e2);
        } catch (IOException e3) {
            displayError("Error reading OFX file: " + e3);
            Log.e(SMMoney.TAG, "ImportExportOFX: IOException in importIntoDatabase", e3);
        }

        if (strBuff.length() == 0) {
            displayError("Empty file : " + this.path);
            Database.currentDB().endTransaction();
            return;
        }

        try {
            this.ofxData = new OFXClass(strBuff.toString());
            processAccounts();
            processTransactions();
            Database.currentDB().setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "ImportExportOFX: Error parsing/importing OFX data", e);
            displayError("Error importing OFX file: " + e.getMessage());
        } finally {
            Database.currentDB().endTransaction();
        }
    }
}

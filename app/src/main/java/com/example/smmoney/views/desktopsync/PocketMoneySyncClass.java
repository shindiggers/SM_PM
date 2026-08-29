package com.example.smmoney.views.desktopsync;

import android.util.Base64;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.CategoryBudgetClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.IDClass;
import com.example.smmoney.records.PayeeClass;
import com.example.smmoney.records.PocketMoneyRecordClass;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;

import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PocketMoneySyncClass extends DefaultHandler {
    Socket asyncSocket;
    int currentState = 0;
    private byte[] data = null;
    PocketMoneySyncActivity delegate;
    String host;
    ArrayList<String> imageFilenames = new ArrayList<>();
    int imageSentCounter;
    private long lastSyncTime;
    ServerSocket listeningSocket;
    int port;
    boolean restoreFromServer;
    boolean server;
    int syncVersion = 0;
    String udid;

    public static void printToFile(String sData, String file) {
        try {
            File dir = new File(SMMoney.getExternalPocketMoneyDirectory());
            if (!dir.exists() && !dir.mkdirs()) {
                Log.w(SMMoney.TAG, "PocketMoneySyncClass: Failed to create directory: " + dir.getAbsolutePath());
            }
            PrintWriter out = new PrintWriter(new FileWriter(SMMoney.getExternalPocketMoneyDirectory() + file));
            out.write(sData, 0, sData.length());
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in printToFile", e);
        }
    }




    void setCurrentState(int state) {
        this.currentState = state;
        if (this.delegate != null) {
            this.delegate.desktopSyncWithState(state);
        }
    }

    void getRecentChanges() {
        setCurrentState(Enums.kDesktopSyncStateReceivingRecentChanges/*50*/);
        if (this.asyncSocket == null || this.asyncSocket.isClosed()) {
            return;
        }
        float totalSize = (float) sizeFromHeader();
        try {
            System.gc();
            byte[] newData = new byte[1024];
            int readIn = this.asyncSocket.getInputStream().read(newData, 0, "DATA:".length());
            if (readIn != -1) {
                int totalReadIn = readIn;
                deleteTempFile();
                BufferedOutputStream fr = new BufferedOutputStream(new FileOutputStream(new File(SMMoney.getTempFile()).getAbsoluteFile()));
                while (((float) totalReadIn) < totalSize) {
                    readIn = this.asyncSocket.getInputStream().read(newData, 0, 1024);
                    if (readIn == -1) {
                        break;
                    }
                    fr.write(newData, 0, readIn);
                    totalReadIn += readIn;
                }
                Log.i("DEBUGTAG123", "getRecentChanges totalReadIn=" + totalReadIn);
                fr.flush();
                fr.close();
                
                File downloadedFile = new File(SMMoney.getTempFile());
                if (!downloadedFile.exists() || downloadedFile.length() == 0) {
                    Log.e(SMMoney.TAG, "getRecentChanges: downloaded temp file is empty or missing!");
                    setCurrentState(Enums.kDesktopSyncStateError/*69*/);
                    return;
                }
                
                //noinspection IfStatementWithIdenticalBranches
                if (this.server) {
                    setCurrentState(Enums.kDesktopSyncStateRecentChangesReceived/*51*/);
                } else {
                    setCurrentState(Enums.kDesktopSyncStateRecentChangesReceived/*51*/);
                }
            } else {
                Log.e(SMMoney.TAG, "getRecentChanges readIn == -1 on first block");
                setCurrentState(Enums.kDesktopSyncStateError/*69*/);
            }
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in getRecentChanges", e);
            setCurrentState(Enums.kDesktopSyncStateError/*69*/);
        }
    }

    private void deleteTempFile() {
        if (!new File(SMMoney.getTempFile()).delete()) {
            Log.i("com.catamount.com", "unable to delete tempfile");
        }
    }

    void disconnect() {
        setCurrentState(Enums.kDesktopSyncStateDisconnected/*67*/);
        if (this.asyncSocket != null) {
            try {
                this.asyncSocket.close();
            } catch (IOException e) {
                Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in disconnect (async)", e);
            }
        }
        if (this.listeningSocket != null) {
            try {
                this.listeningSocket.close();
            } catch (IOException e2) {
                Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in disconnect (listening)", e2);
            }
        }
        deleteTempFile();
        this.imageSentCounter = 0;
        this.imageFilenames = new ArrayList<>();
        this.listeningSocket = null;
        this.asyncSocket = null;
        final PocketMoneySyncActivity del = this.delegate;
        this.delegate.runOnUiThread(del::desktopSyncComplete);
    }

    void reset() {
        if (this.delegate != null) {
            this.delegate.photoCount = 0;
        }
        disconnect();
    }



    private void readInHeaderSize(int tag) {
        readInSize(4, tag);
    }

    private void readInSize(int size, int tag) {
        if (this.asyncSocket == null || this.asyncSocket.isClosed()) {
            return;
        }
        try {
            this.data = null;
            System.gc();
            this.data = new byte[size];
            int totalReadIn = 0;
            int readIn = 0;
            while (totalReadIn < size && readIn != -1) {
                readIn = this.asyncSocket.getInputStream().read(this.data, totalReadIn, size - totalReadIn);
                totalReadIn += readIn;
            }
            System.gc();
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException", e);
        }
        setCurrentState(tag);
    }

    void writeData(String sData, int tag) {
        if (this.asyncSocket == null || this.asyncSocket.isClosed()) {
            return;
        }
        byte[] data = packageData(sData);
        try {
            this.asyncSocket.getOutputStream().write(data, 0, data.length);
            this.asyncSocket.getOutputStream().flush();
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in writeData", e);
        }
        setCurrentState(tag);
    }

    private void recentChangesTransactions(BufferedWriter out) throws IOException {
        String listTag = TransactionClass.XML_LISTTAG_TRANSACTIONS;
        out.write("<" + listTag + ">\n");
        PocketMoneyRecordClass[] query = Database.queryServerSyncTableWithPKandClassAndTime(Database.TRANSACTIONS_TABLE_NAME, "transactionID", TransactionClass.class, this.lastSyncTime);
        if (query != null) {
            for (int i = 0; i < query.length; i++) {
                TransactionClass record = (TransactionClass) query[i];
                out.write(record.XMLStringWithImages(false) + '\n');
                if (!record.getDeleted()) {
                    this.imageFilenames.addAll(record.imageFileNames());
                }
                query[i] = null;
            }
        }
        out.write("</" + listTag + ">\n");
    }

    private void recentChanges(BufferedWriter out, String table, String primaryKey, Class<? extends PocketMoneyRecordClass> classOf, String listTag) throws IOException {
        out.write("<" + listTag + ">\n");
        Database.queryAndWriteServerSyncTableWithPKandClassAndTime(out, table, primaryKey, classOf, this.lastSyncTime);
        out.write("</" + listTag + ">\n");
    }

    private void recentChangesAccounts(BufferedWriter out) throws IOException {
        recentChanges(out, Database.ACCOUNTS_TABLE_NAME, "accountID", AccountClass.class, AccountClass.XML_LISTTAG_ACCOUNTS);
    }

    private void recentChangesCategories(BufferedWriter out) throws IOException {
        recentChanges(out, Database.CATEGORIES_TABLE_NAME, "categoryID", CategoryClass.class, CategoryClass.XML_LISTTAG_CATEGORIES);
    }

    private void recentChangesPayees(BufferedWriter out) throws IOException {
        recentChanges(out, Database.PAYEES_TABLE_NAME, "payeeID", PayeeClass.class, PayeeClass.XML_LISTTAG_PAYEES);
    }

    private void recentChangesClasses(BufferedWriter out) throws IOException {
        recentChanges(out, Database.CLASSES_TABLE_NAME, "classID", ClassNameClass.class, ClassNameClass.XML_LISTTAG_CLASSES);
    }

    private void recentChangesIDs(BufferedWriter out) throws IOException {
        recentChanges(out, Database.IDS_TABLE_NAME, "idID", IDClass.class, IDClass.XML_LISTTAG_IDS);
    }

    private void recentChangesFilters(BufferedWriter out) throws IOException {
        recentChanges(out, Database.FILTERS_TABLE_NAME, "filterID", FilterClass.class, FilterClass.XML_LISTTAG_FILTERS);
    }

    private void recentChangesRepeatingTransactions(BufferedWriter out) throws IOException {
        recentChanges(out, Database.REPEATINGTRANSACTIONS_TABLE_NAME, "repeatingID", RepeatingTransactionClass.class, RepeatingTransactionClass.XML_LISTTAG_REPEATINGTRANSACTIONS);
    }

    private void recentChangesCategoryBudgets(BufferedWriter out) throws IOException {
        recentChanges(out, "categoryBudgets", "categoryBudgetID", CategoryBudgetClass.class, CategoryBudgetClass.XML_LISTTAG_CATEGORYBUDGETS);
    }



    private void newRecentDatabaseChanges() {
        long j;
        if (this.restoreFromServer) {
            j = 0;
        } else {
            j = Database.lastSyncTimeForUDID(this.udid);
        }
        this.lastSyncTime = j;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(SMMoney.getTempFile()).getAbsolutePath()));
            boolean multipleCurrencies = Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES);
            String homeCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
            boolean updateExchangeRates = Prefs.getBooleanPref(Prefs.UPDATEEXCHANGERATES);
            out.write("DATA:<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<SMMoney xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" DBID=\"" + Database.databaseID + "\" DBVER=\"" + 34 + "\" MULTIPLE_CURRENCIES=\"" + multipleCurrencies + "\" HOME_CURRENCY=\"" + homeCurrency + "\" UPDATE_EXCHANGE_RATES=\"" + updateExchangeRates + "\"> ");
            out.write("<DATA>\n");
            recentChangesAccounts(out);
            recentChangesTransactions(out);
            recentChangesCategories(out);
            recentChangesPayees(out);
            recentChangesIDs(out);
            recentChangesClasses(out);
            recentChangesFilters(out);
            recentChangesRepeatingTransactions(out);
            recentChangesCategoryBudgets(out);
            out.write("</DATA>\n");
            out.write(" </SMMoney>");
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in getRecentChanges", e);
        }
    }

    void processRecentAccount(AccountClass rec) {
        if (rec != null) {
            if (rec.serverID == null || rec.serverID.isEmpty()) {
                rec.serverID = Database.newServerID();
                rec.saveToDataBaseAndUpdateTimeStamp(true);
                return;
            }
            AccountClass oldTransaction = AccountClass.recordWithServerID(rec.serverID);
            if (oldTransaction != null) {
                oldTransaction.hydrate();
                if (!oldTransaction.timestamp.after(rec.timestamp)) {
                    rec.accountID = oldTransaction.accountID;
                } else {
                    return;
                }
            } else {
                rec.accountID = 0;
            }
            rec.saveToDataBaseAndUpdateTimeStamp(true);
        }
    }


    void processRecentTransaction(TransactionClass rec) {
        if (rec != null) {
            if (rec.serverID == null || rec.serverID.isEmpty()) {
                rec.serverID = Database.newServerID();
                rec.saveToDataBaseAndUpdateTimeStamp(true);
                return;
            }
            TransactionClass oldTransaction = TransactionClass.recordWithServerID(rec.serverID);
            if (oldTransaction != null) {
                oldTransaction.hydrate();
                oldTransaction.deleteSplitsfromDatabasePermentantly();
                rec.transactionID = oldTransaction.transactionID;
            } else {
                rec.transactionID = 0;
            }
            rec.saveToDataBaseAndUpdateTimeStamp(true);
        }
    }

    void processRecentChange(PocketMoneyRecordClass record, Class<? extends PocketMoneyRecordClass> theClass, String primaryKeyField) {
        if (record != null) {
            if (record.serverID == null || record.serverID.isEmpty()) {
                try {
                    Field f = theClass.getDeclaredField(primaryKeyField);
                    f.setAccessible(true);
                    f.set(record, 0);
                } catch (Exception e) {
                    Log.e(SMMoney.TAG, "PocketMoneySyncClass: Exception in processRecentChange (primaryKeyField)", e);
                }
                record.serverID = Database.newServerID();
                record.saveToDataBaseAndUpdateTimeStamp(true);
                return;
            }
            try {
                PocketMoneyRecordClass oldTransaction = (PocketMoneyRecordClass) theClass.getMethod("recordWithServerID", String.class).invoke(record, record.serverID);
                if (oldTransaction != null) {
                    oldTransaction.hydrate();
                    if (!oldTransaction.timestamp.after(record.timestamp)) {
                        Field f = theClass.getDeclaredField(primaryKeyField);
                        f.setAccessible(true);
                        f.set(record, f.get(oldTransaction));
                    } else {
                        return;
                    }
                } else {
                    Field f = theClass.getDeclaredField(primaryKeyField);
                    f.setAccessible(true);
                    f.set(record, 0);
                }
            } catch (Exception e2) {
                Log.e(SMMoney.TAG, "PocketMoneySyncClass: Exception in processRecentChange (invoke)", e2);
            }
            record.saveToDataBaseAndUpdateTimeStamp(true);
        }
    }

    void processRecentRepeatingTransaction(RepeatingTransactionClass rep) {
        if (rep.serverID == null || rep.transactionServerID == null) {
            rep.serverID = Database.newServerID();
            rep.saveToDataBaseAndUpdateTimeStamp(true);
            return;
        }
        rep.setTransaction(TransactionClass.recordWithServerID(rep.transactionServerID));
        RepeatingTransactionClass oldTransaction = RepeatingTransactionClass.recordWithServerID(rep.serverID);
        if (oldTransaction != null) {
            oldTransaction.hydrate();
            if (!oldTransaction.timestamp.after(rep.timestamp)) {
                rep.repeatingID = oldTransaction.repeatingID;
            } else {
                return;
            }
        } else {
            rep.repeatingID = 0;
        }
        rep.saveToDataBaseAndUpdateTimeStamp(true);
    }

    void sendRecentChanges() {
        setCurrentState(Enums.kDesktopSyncStateSendingRecentChanges/*46*/);
        newRecentDatabaseChanges();
        try {
            File f = new File(SMMoney.getTempFile());
            BufferedInputStream fr = new BufferedInputStream(new FileInputStream(f.getAbsoluteFile()));
            int totalSize = (int) f.length();
            byte[] chunk = new byte[1024];
            int totalRead = 0;
            while (totalRead < totalSize) {
                int read = fr.read(chunk, 0, 1024);
                if (read == -1) {
                    break;
                }
                if (totalRead == 0) {
                    byte[] outData = packageDataWithHeader(chunk, totalSize);
                    this.asyncSocket.getOutputStream().write(outData, 0, outData.length);
                } else {
                    this.asyncSocket.getOutputStream().write(chunk, 0, read);
                }
                totalRead += read;
            }
            Log.i("DEBUGTAG123", "sendRecentChanges totalRead=" + totalRead);
            fr.close();
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in getRecentChanges", e);
        }
        //noinspection IfStatementWithIdenticalBranches
        if (this.server) {
            setCurrentState(Enums.kDesktopSyncStateSentRecentChanges/*47*/);
        } else {
            setCurrentState(Enums.kDesktopSyncStateSentRecentChanges/*47*/);
        }
    }


    void getPhotoHeader() {
        setCurrentState(Enums.kDesktopSyncStateReceivingPhotoHeader/*33*/);
        readInHeaderSize(Enums.kDesktopSyncStatePhotoHeaderReceived/*34*/);
    }

    void sendPhoto() {
        if (this.delegate != null) {
            PocketMoneySyncActivity pocketMoneySyncActivity = this.delegate;
            pocketMoneySyncActivity.photoCount++;
        }
        setCurrentState(Enums.kDesktopSyncStateSendingPhoto/*31*/);
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(this.asyncSocket.getOutputStream());
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in sendPhoto getting output stream", e);
        }
        String fileName = this.imageFilenames.get(this.imageSentCounter);
        String start = "PHOTO:<image><imagedata>";
        String end = "</imagedata><filename>" + fileName + "</filename></image>";
        File photoDir = new File(SMMoney.getAppContext().getFilesDir(), "photos");
        File f = new File(photoDir, fileName);
        if (f.exists()) {
            BufferedInputStream fin = null;
            try {
                fin = new BufferedInputStream(new FileInputStream(f.getAbsolutePath()));
            } catch (FileNotFoundException e) {
                Log.e(SMMoney.TAG, "PocketMoneySyncClass: FileNotFoundException in sendPhoto for file: " + f.getAbsolutePath(), e);
            }
            int totalRead = 0;
            int read = 0;
            int size = (int) f.length();
            byte[] outData = new byte[size];
            byte[] startData = start.getBytes(StandardCharsets.UTF_8);
            byte[] endData = end.getBytes(StandardCharsets.UTF_8);
            while (totalRead < size && read != -1) {
                try {
                    if (fin != null) {
                        read = fin.read(outData, totalRead, size - totalRead);
                    }
                    if (read != -1) {
                        totalRead += read;
                    }
                } catch (Exception e) {
                    Log.e(SMMoney.TAG, "PocketMoneySyncClass: Exception in sendPhoto reading photo bytes", e);
                }
            }
            byte[] b64Data = Base64.encode(outData, 0, totalRead, Base64.NO_WRAP);
            int totalMessageSize = (startData.length + b64Data.length) + endData.length;
            byte[] retData = new byte[totalMessageSize];
            for (int i = 0; i < totalMessageSize; i++) {
                if (i < startData.length) {
                    retData[i] = startData[i];
                } else if (i < startData.length + b64Data.length) {
                    retData[i] = b64Data[i - startData.length];
                } else {
                    retData[i] = endData[(i - startData.length) - b64Data.length];
                }
            }
            retData = packageData(retData);
            try {
                if (out != null) {
                    out.write(retData, 0, retData.length);
                }
            } catch (IOException e) {
                Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in sendPhoto writing data to output stream", e);
            }
            try {
                if (out != null) {
                    out.flush();
                }
            } catch (IOException e) {
                Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in sendPhoto flushing output stream", e);
            }
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException in sendPhoto closing FileInputStream", e);
            }
        }
        setCurrentState(Enums.kDesktopSyncStateSentPhoto/*32*/);
    }

    boolean processPhotosFailed() {
        if (this.data != null) {
            return true;
        }
        File f = new File(SMMoney.getTempFile());
        try {
            byte[] someData = new byte["PHOTO:END".length()];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f.getAbsoluteFile()));
            if (in.read(someData) > 0) {
                String sData = new String(someData);
                if (sData.startsWith("END")) {
                    in.close();
                    return true;
                }
                in.close();
                new TransactionClass().updateWithXMLFile(f);
                setCurrentState(Enums.kDesktopSyncStatePhotoProcessed/*37*/);
                return false;
            }
            in.close();
            return true;
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: Exception in processPhotosFailed", e);
            return true;
        }
    }

    void sendPhotoACK() {
        setCurrentState(Enums.kDesktopSyncStateSendingPhotoACK/*38*/);
        writeData("PHOTO:OK", 39);
    }

    void getPhotoACKHeader() {
        setCurrentState(Enums.kDesktopSyncStateReceivingPhotoACKHeader/*40*/);
        readInHeaderSize(41);
    }

    void getPhotoACK() {
        setCurrentState(Enums.kDesktopSyncStateReceivingPhotoACK/*42*/);
        readInSize(sizeFromHeader(), 43);
    }

    boolean processPhotoACKFailed() {
        if (stringFromDataExcluding("").equals("PHOTO:OK")) {
            setCurrentState(Enums.kDesktopSyncStatePhotoACKProcessed/*45*/);
            return false;
        }
        setCurrentState(Enums.kDesktopSyncStateError/*69*/);
        return true;
    }

    void sendSyncVersion() {
        String syncVersionMsg;
        setCurrentState(Enums.kDesktopSyncStateSendingUDID/*17*/);
        if (this.syncVersion != 0) {
            syncVersionMsg = "PMSYNC:" + this.syncVersion;
        } else {
            syncVersionMsg = "PMSYNC:2";
        }
        writeData(syncVersionMsg, 10);
    }

    void getSyncVersionHeader() {
        setCurrentState(Enums.kDesktopSyncStateReceivingSyncVersion/*13*/);
        readInHeaderSize(12);
    }

    void getSyncVersion() {
        setCurrentState(Enums.kDesktopSyncStateSendingSyncVersion/*9*/);
        readInSize(sizeFromHeader(), 14);
    }

    boolean processSyncVersion() {
        String sData = new String(this.data, StandardCharsets.UTF_8);
        this.data = null;
        this.syncVersion = Integer.parseInt(sData.substring("PMSYNC:".length()));
        if (this.syncVersion > 2) {
            this.syncVersion = 2;
        } else if (this.syncVersion < 2) {
            this.delegate.runOnUiThread(() -> {
                PocketMoneySyncActivity pocketMoneySyncActivity = PocketMoneySyncClass.this.delegate;
                PocketMoneySyncClass.this.delegate.getClass();
                pocketMoneySyncActivity.showUpgradeDialog();
                PocketMoneySyncClass.this.delegate.stopSyncing();
            });
        }
        setCurrentState(Enums.kDesktopSyncStateSyncVersionProcessed/*16*/);
        return true;
    }

    void sendUDID() {
        setCurrentState(Enums.kDesktopSyncStateSendingUDID/*17*/);
        if (!this.server && this.restoreFromServer) {
            writeData("UDID:" + SMMoney.getID() + ":RESTORE", 18);
        } else {
            writeData("UDID:" + SMMoney.getID(), 18);
        }
    }

    void getUDIDHeader() {
        setCurrentState(Enums.kDesktopSyncStateReceivingUDIDHeader/*19*/);
        readInHeaderSize(20);
    }

    void getUDID() {
        setCurrentState(Enums.kDesktopSyncStateReceivingUDID/*21*/);
        readInSize(sizeFromHeader(), 22);
    }

    private final Object udidLock = new Object();
    private volatile boolean udidActionChosen = false;

    boolean processUDID() {
        if (this.data == null) {
            setCurrentState(Enums.kDesktopSyncStateError/*69*/);
            return false;
        }
        String rawUdid = stringFromDataExcluding("UDID:");
        if (rawUdid.endsWith(":RESTORE")) {
            this.restoreFromServer = true;
            this.udid = rawUdid.substring(0, rawUdid.length() - ":RESTORE".length());
        } else {
            this.udid = rawUdid;
        }
        if (this.restoreFromServer || Database.lastSyncTimeForUDID(this.udid) != 0) {
            setCurrentState(Enums.kDesktopSyncStateUDIDProcessed/*24*/);
            return true;
        }
        setCurrentState(Enums.kDesktopSyncStateUDIDProcessing/*23*/);
        this.udidActionChosen = false;
        this.delegate.pocketMoneySyncRequestActionForFirstSyncUDID(this.udid);
        
        // Wait on the network thread until user makes a choice on the dialog
        synchronized (this.udidLock) {
            while (!this.udidActionChosen && this.asyncSocket != null && !this.asyncSocket.isClosed()) {
                try {
                    this.udidLock.wait(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        return this.currentState == Enums.kDesktopSyncStateUDIDProcessed;
    }

    void firstUDIDSyncAction(int action) {
        switch (action) {
            case Enums.kDesktopSyncFirstSyncActionReplaceDataOnServer /*1*/:
                break;
            case Enums.kDesktopSyncFirstSyncActionRestoreFromServer /*2*/:
                this.restoreFromServer = true;
                Database.wipeDatabase();
                setCurrentState(Enums.kDesktopSyncStateUDIDProcessed/*24*/);
                break;
            case Enums.kDesktopSyncFirstSyncActionSync /*3*/:
                setCurrentState(Enums.kDesktopSyncStateUDIDProcessed/*24*/);
                break;
            default:
                setCurrentState(Enums.kDesktopSyncStateDisconnecting/*66*/);
                Database.sqlite3_rollback();
                sendFail();
                reset();
                break;
        }
        synchronized (this.udidLock) {
            this.udidActionChosen = true;
            this.udidLock.notifyAll();
        }
    }

    void sendACK() {
        setCurrentState(Enums.kDesktopSyncStateSendingACK/*54*/);
        writeData("DATA:OK", 55);
    }

    void getACKHeader() {
        setCurrentState(Enums.kDesktopSyncStateReceivingACK/*58*/);
        readInHeaderSize(57);
    }

    void getACK() {
        setCurrentState(Enums.kDesktopSyncStateReceivingACK/*58*/);
        readInSize(sizeFromHeader(), 59);
    }

    void processACK() {
        if (this.data == null) {
            setCurrentState(Enums.kDesktopSyncStateError/*69*/);
            return;
        }
        String sData = stringFromDataExcluding("");
        if (sData.equals("DATA:OK")) {
            setCurrentState(Enums.kDesktopSyncStateACKProcessed/*61*/);
            return;
        }
        setCurrentState(Enums.kDesktopSyncStateError/*69*/);
        Log.e(SMMoney.TAG, "Error: processAck: Invalid response. Expected [DATA:OK], got [" + sData + "]");
    }

    void sendTheEnd() {
        setCurrentState(Enums.kDesktopSyncStateSendingTheEnd/*62*/);
        writeData("PHOTO:END", 65);
    }

    void getRecentChangesHeader() {
        Database.sqlite3_begin();
        if (!this.server && this.restoreFromServer) {
            Database.wipeDatabase();
        }
        setCurrentState(Enums.kDesktopSyncStateReceivingRecentChangesHeader/*48*/);
        readInHeaderSize(Enums.kDesktopSyncStateRecentChangesHeaderReceived/*49*/);
    }

    void getPhotos() {
        if (this.delegate != null) {
            PocketMoneySyncActivity pocketMoneySyncActivity = this.delegate;
            pocketMoneySyncActivity.photoCount++;
        }
        setCurrentState(Enums.kDesktopSyncStateReceivingPhoto/*35*/);
        int totalSize = sizeFromHeader();
        try {
            byte[] newData = new byte[1024];
            byte[] replacedData = new byte[1024];
            int bytesReplaced = 0;
            int readIn = this.asyncSocket.getInputStream().read(newData, 0, "PHOTO:".length());
            if (readIn != -1) {
                int totalReadIn = readIn;
                int i;
                if (new String(newData).startsWith("UDID")) {
                    // Intentionally suppressing unused variable warning: this exceptional UDID flow
                    // reads the remainder of the packet into the newData buffer. We copy totalSize
                    // bytes and return immediately, so checking the exact bytes read count is not needed.
                    @SuppressWarnings("unused")
                    int ignored = this.asyncSocket.getInputStream().read(newData, readIn, totalSize - readIn);
                    this.data = new byte[totalSize];
                    for (i = 0; i < totalSize; i++) {
                        this.data[i] = newData[i];
                    }
                    setCurrentState(Enums.kDesktopSyncStatePhotoReceived/*36*/);
                    return;
                }
                deleteTempFile();
                BufferedOutputStream fr = new BufferedOutputStream(new FileOutputStream(new File(SMMoney.getTempFile()).getAbsoluteFile()));
                while (totalReadIn < totalSize) {
                    readIn = this.asyncSocket.getInputStream().read(newData, 0, 1024);
                    if (readIn == -1) {
                        break;
                    }
                    i = 0;
                    while (i < readIn) {
                        if (newData[i] == (byte) 13 || newData[i] == (byte) 10) {
                            bytesReplaced++;
                        } else {
                            replacedData[i - bytesReplaced] = newData[i];
                        }
                        i++;
                    }
                    fr.write(replacedData, 0, readIn - bytesReplaced);
                    totalReadIn += readIn;
                    bytesReplaced = 0;
                }
                fr.flush();
                fr.close();
                setCurrentState(Enums.kDesktopSyncStatePhotoReceived/*36*/);
            }
        } catch (IOException e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClass: IOException", e);
        }
    }

    void processAccounts(ArrayList<AccountClass> accounts) {
        ArrayList<TempAccountRecord> overdraftAccounts = new ArrayList<>();
        ArrayList<TempAccountRecord> keepTheChangeAccounts = new ArrayList<>();
        for (AccountClass act : accounts) {
            if (act.getOverdraftAccount() != null && !act.getOverdraftAccount().isEmpty()) {
                overdraftAccounts.add(new TempAccountRecord(act.serverID, act.getOverdraftAccount()));
            }
            if (act.getKeepTheChangeAccount() != null && !act.getKeepTheChangeAccount().isEmpty()) {
                keepTheChangeAccounts.add(new TempAccountRecord(act.serverID, act.getKeepTheChangeAccount()));
            }
        }
        for (TempAccountRecord tempAct : keepTheChangeAccounts) {
            AccountClass act = AccountClass.recordWithServerID(tempAct.serverID);
            if (act != null) {
                act.hydrate();
                act.setKeepTheChangeAccount(tempAct.account);
                act.saveToDatabase();
            }
        }
        for (TempAccountRecord tempAct : overdraftAccounts) {
            AccountClass act = AccountClass.recordWithServerID(tempAct.serverID);
            if (act != null) {
                act.hydrate();
                act.setOverdraftAccount(tempAct.account);
                act.saveToDatabase();
            }
        }
    }




    private String stringFromDataExcluding(String substring) {
        String sData;
        sData = new String(this.data, StandardCharsets.UTF_8);
        this.data = null;
        return sData.substring(substring.length());
    }



    private byte[] packageData(String data) {
        byte[] plainData;
        plainData = data.getBytes(StandardCharsets.UTF_8);
        int headerLength = plainData.length;
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(headerLength);
        byte[] headerArray = buf.array();
        byte[] packagedData = new byte[(headerArray.length + headerLength)];
        for (int i = 0; i < headerArray.length + headerLength; i++) {
            if (i < 4) {
                packagedData[i] = headerArray[i];
            } else {
                packagedData[i] = plainData[i - headerArray.length];
            }
        }
        return packagedData;
    }

    void sendFail() {
        writeData("DATA:FAIL", 10);
    }

    private byte[] packageDataWithHeader(byte[] data, int size) {
        int dataLength = Math.min(data.length, size);
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(size);
        byte[] headerArray = buf.array();
        byte[] packagedData = new byte[(headerArray.length + dataLength)];
        for (int i = 0; i < headerArray.length + dataLength; i++) {
            if (i < 4) {
                packagedData[i] = headerArray[i];
            } else {
                packagedData[i] = data[i - headerArray.length];
            }
        }
        return packagedData;
    }

    private int sizeFromHeader() {
        ByteBuffer buf = ByteBuffer.wrap(this.data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.data = null;
        return buf.getInt(0);
    }

    private byte[] packageData(byte[] plainData) {
        int headerLength = plainData.length;
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(headerLength);
        byte[] headerArray = buf.array();
        byte[] packagedData = new byte[(headerArray.length + headerLength)];
        for (int i = 0; i < headerArray.length + headerLength; i++) {
            if (i < 4) {
                packagedData[i] = headerArray[i];
            } else {
                packagedData[i] = plainData[i - headerArray.length];
            }
        }
        return packagedData;
    }

    record TempAccountRecord(String serverID, String account) {
    }
}

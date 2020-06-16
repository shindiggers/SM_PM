package com.example.smmoney.views.desktopsync;

import android.os.Environment;
import android.util.Log;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.iAP.util.Base64;
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
import java.util.Iterator;

public class PocketMoneySyncClass extends DefaultHandler {
    Socket asyncSocket;
    int currentState = 0;
    private byte[] data = null;
    PocketMoneySyncActivity delegate;
    String host;
    ArrayList<String> imageFilenames = new ArrayList<>();
    private int imageRecieveCounter;
    int imageSentCounter;
    private long lastSyncTime;
    ServerSocket listeningSocket;
    int packetSize;
    int port;
    boolean restoreFromServer;
    boolean server;
    int syncVersion = 0;
    String udid;

    static class AnonymousClass1TempTransAccountClass {
        String account;
        String serverID;

        AnonymousClass1TempTransAccountClass(String serverID, String account) {
            this.serverID = serverID;
            this.account = account;
        }
    }

    static class AnonymousClass2TempTransAccountClass {
        String account;
        String serverID;

        AnonymousClass2TempTransAccountClass(String serverID, String account) {
            this.serverID = serverID;
            this.account = account;
        }
    }

    static class AnonymousClass3TempTransAccountClass {
        String account;
        String serverID;

        AnonymousClass3TempTransAccountClass(String serverID, String account) {
            this.serverID = serverID;
            this.account = account;
        }
    }

    void setCurrentState(int state) {
        this.currentState = state;
        if (this.delegate != null) {
            this.delegate.desktopSyncWithState(this, state);
        }
    }

    void disconnect() {
        setCurrentState(Enums.kDesktopSyncStateDisconnected/*67*/);
        if (this.asyncSocket != null) {
            try {
                this.asyncSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.listeningSocket != null) {
            try {
                this.listeningSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        deleteTempFile();
        this.imageSentCounter = 0;
        this.imageFilenames = new ArrayList<>();
        this.listeningSocket = null;
        this.asyncSocket = null;
        final PocketMoneySyncActivity del = this.delegate;
        this.delegate.runOnUiThread(new Runnable() {
            public void run() {
                del.desktopSyncComplete(PocketMoneySyncClass.this);
            }
        });
    }

    private void deleteTempFile() {
        if (!new File(SMMoney.getTempFile()).delete()) {
            Log.i("com.catamount.com", "unable to delete tempfile");
        }
    }

    protected void copyTempFileToSDCard() {
        try {
            Prefs.copyFile(new File(SMMoney.getTempFile()), new File(SMMoney.getExternalPocketMoneyDirectory(), "temp.data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void reset() {
        if (this.delegate != null) {
            this.delegate.photoCount = 0;
        }
        disconnect();
    }

    private void readInSize(int size, int tag) {
        readInSize(size, tag, false);
    }

    private void readInSize(int size, int tag, boolean processState) {
        try {
            this.data = null;
            System.gc();
            this.data = new byte[size];
            int rawr = this.asyncSocket.getInputStream().available();
            if (size > 100000) {
                int i = 1;
            }
            int totalReadIn = 0;
            int readIn = 0;
            while (totalReadIn < size && readIn != -1) {
                readIn = this.asyncSocket.getInputStream().read(this.data, totalReadIn, size - totalReadIn);
                totalReadIn += readIn;
            }
            System.gc();
        } catch (IOException e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        setCurrentState(tag);
    }

    private void readInHeaderSize(int tag) {
        readInHeaderSize(tag, false);
    }

    private void readInHeaderSize(int tag, boolean processState) {
        readInSize(4, tag, processState);
    }

    void writeData(String sData, int tag) {
        writeData(sData, tag, false);
    }

    private void writeData(String sData, int tag, boolean processState) {
        byte[] data = packageData(sData);
        try {
            this.asyncSocket.getOutputStream().write(data, 0, data.length);
            this.asyncSocket.getOutputStream().flush();
        } catch (IOException e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
            e.printStackTrace();
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

    private String recentChanges(BufferedWriter out, String table, String primaryKey, Class<? extends PocketMoneyRecordClass> classOf, String listTag) throws IOException {
        out.write("<" + listTag + ">\n");
        Database.queryAndWriteServerSyncTableWithPKandClassAndTime(out, table, primaryKey, classOf, this.lastSyncTime);
        out.write("</" + listTag + ">\n");
        return "";
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
        StringBuilder sb = new StringBuilder();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(SMMoney.getTempFile()).getAbsolutePath()));
            out.write("DATA:<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<SMMoney xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" DBID=\"" + Database.databaseID + "\" DBVER=\"" + 34 + "\"> ");
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
            e.printStackTrace();
        }
    }

    protected void processRecentAccounts(PocketMoneyRecordClass[] recentChanges) {
        if (recentChanges != null) {
            AnonymousClass1TempTransAccountClass tempAct;
            AccountClass act;
            ArrayList<AnonymousClass1TempTransAccountClass> overdraftAccounts = new ArrayList<>();
            ArrayList<AnonymousClass1TempTransAccountClass> keepTheChangeAccounts = new ArrayList<>();
            for (PocketMoneyRecordClass rec : recentChanges) {
                AccountClass record = (AccountClass) rec;
                if (record.serverID == null || record.serverID.length() <= 0) {
                    record.serverID = Database.newServerID();
                    record.saveToDataBaseAndUpdateTimeStamp(true);
                } else {
                    AccountClass oldTransaction = AccountClass.recordWithServerID(record.serverID);
                    if (oldTransaction != null) {
                        oldTransaction.hydrate();
                        if (!oldTransaction.timestamp.after(record.timestamp)) {
                            record.accountID = oldTransaction.accountID;
                        }
                    } else {
                        record.accountID = 0;
                    }
                    if (record.getOverdraftAccount() != null && record.getOverdraftAccount().length() > 0) {
                        overdraftAccounts.add(new AnonymousClass1TempTransAccountClass(record.serverID, record.getOverdraftAccount()));
                    }
                    if (record.getKeepTheChangeAccount() != null && record.getKeepTheChangeAccount().length() > 0) {
                        keepTheChangeAccounts.add(new AnonymousClass1TempTransAccountClass(record.serverID, record.getKeepTheChangeAccount()));
                    }
                    record.saveToDataBaseAndUpdateTimeStamp(true);
                }
            }
            Iterator it = keepTheChangeAccounts.iterator();
            while (it.hasNext()) {
                tempAct = (AnonymousClass1TempTransAccountClass) it.next();
                act = AccountClass.recordWithServerID(tempAct.serverID);
                if (act != null) {
                    act.hydrate();
                    act.setKeepTheChangeAccount(tempAct.account);
                    act.saveToDatabase();
                }
            }
            it = overdraftAccounts.iterator();
            while (it.hasNext()) {
                tempAct = (AnonymousClass1TempTransAccountClass) it.next();
                act = AccountClass.recordWithServerID(tempAct.serverID);
                if (act != null) {
                    act.hydrate();
                    act.setOverdraftAccount(tempAct.account);
                    act.saveToDatabase();
                }
            }
        }
    }

    void processRecentAccount(AccountClass rec) {
        if (rec != null) {
            if (rec.serverID == null || rec.serverID.length() <= 0) {
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
            }
            rec.accountID = 0;
            rec.saveToDataBaseAndUpdateTimeStamp(true);
        }
    }

    protected void processRecentTransactions(PocketMoneyRecordClass[] recentChanges) {
        if (recentChanges != null) {
            for (PocketMoneyRecordClass rec : recentChanges) {
                TransactionClass record = (TransactionClass) rec;
                if (record.serverID == null || record.serverID.length() <= 0) {
                    record.serverID = Database.newServerID();
                    record.saveToDataBaseAndUpdateTimeStamp(true);
                } else {
                    TransactionClass oldTransaction = TransactionClass.recordWithServerID(record.serverID);
                    if (oldTransaction != null) {
                        oldTransaction.hydrate();
                        oldTransaction.deleteSplitsfromDatabasePermentantly();
                        if (!oldTransaction.timestamp.after(record.timestamp)) {
                            record.transactionID = oldTransaction.transactionID;
                        }
                    } else {
                        record.transactionID = 0;
                    }
                    record.saveToDataBaseAndUpdateTimeStamp(true);
                }
            }
        }
    }

    void processRecentTransaction(TransactionClass rec) {
        if (rec != null) {
            if (rec.serverID == null || rec.serverID.length() <= 0) {
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
        }
        rep.repeatingID = 0;
        rep.saveToDataBaseAndUpdateTimeStamp(true);
    }

    protected void processRecentChanges(PocketMoneyRecordClass[] recentChanges, Class theClass, String primaryKeyField) {
        if (recentChanges != null) {
            for (PocketMoneyRecordClass record : recentChanges) {
                if (record.serverID == null || record.serverID.length() <= 0) {
                    try {
                        theClass.getField(primaryKeyField).set(record, 0);
                    } catch (Exception e) {
                        Log.e(SMMoney.TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                    record.saveToDataBaseAndUpdateTimeStamp(true);
                } else {
                    try {
                        PocketMoneyRecordClass oldTransaction = (PocketMoneyRecordClass) theClass.getMethod("recordWithServerID", new Class[]{String.class}).invoke(record, new Object[]{record.serverID});
                        if (oldTransaction != null) {
                            oldTransaction.hydrate();
                            if (!oldTransaction.timestamp.after(record.timestamp)) {
                                Field f = theClass.getField(primaryKeyField);
                                f.set(record, f.get(oldTransaction));
                            }
                        } else {
                            theClass.getField(primaryKeyField).set(record, 0);
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    record.saveToDataBaseAndUpdateTimeStamp(true);
                }
            }
        }
    }

    void processRecentChange(PocketMoneyRecordClass record, Class theClass, String primaryKeyField) {
        if (record != null) {
            if (record.serverID == null || record.serverID.length() <= 0) {
                try {
                    theClass.getField(primaryKeyField).set(record, 0);
                } catch (Exception e) {
                    Log.e(SMMoney.TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                }
                record.saveToDataBaseAndUpdateTimeStamp(true);
                return;
            }
            try {
                PocketMoneyRecordClass oldTransaction = (PocketMoneyRecordClass) theClass.getMethod("recordWithServerID", new Class[]{String.class}).invoke(record, new Object[]{record.serverID});
                if (oldTransaction != null) {
                    oldTransaction.hydrate();
                    if (!oldTransaction.timestamp.after(record.timestamp)) {
                        Field f = theClass.getField(primaryKeyField);
                        f.set(record, f.get(oldTransaction));
                    } else {
                        return;
                    }
                }
                theClass.getField(primaryKeyField).set(record, 0);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            record.saveToDataBaseAndUpdateTimeStamp(true);
        }
    }

    protected void stackSafePhotoProcess() {
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
            e.printStackTrace();
        }
        String fileName = this.imageFilenames.get(this.imageSentCounter);
        String start = "PHOTO:<image><imagedata>";
        String end = "</imagedata><filename>" + fileName + "</filename></image>";
        File f = new File(Environment.getDataDirectory() + "/data/" + SMMoney.getAppContext().getPackageName() + "/photos/" + fileName);
        if (f.exists()) {
            BufferedInputStream fin = null;
            try {
                fin = new BufferedInputStream(new FileInputStream(f.getAbsolutePath()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int totalRead = 0;
            int read = 0;
            int size = (int) f.length();
            byte[] outData = new byte[size];
            byte[] startData = new byte[0];
            startData = start.getBytes(StandardCharsets.UTF_8);
            byte[] endData = new byte[0];
            endData = end.getBytes(StandardCharsets.UTF_8);
            while (totalRead < size && read != -1) {
                try {
                    if (fin != null) {
                        read = fin.read(outData, totalRead, size - totalRead);
                    }
                    totalRead += read;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            byte[] b64Data = Base64.encode(outData, 0, read, Base64.ALPHABET, Integer.MAX_VALUE);
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
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setCurrentState(Enums.kDesktopSyncStateSentPhoto/*32*/);
    }

    void getPhotoHeader() {
        setCurrentState(Enums.kDesktopSyncStateReceivingPhotoHeader/*33*/);
        readInHeaderSize(Enums.kDesktopSyncStatePhotoHeaderReceived/*34*/);
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
                    readIn = this.asyncSocket.getInputStream().read(newData, readIn, totalSize - readIn);
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
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    boolean processPhotos() {
        if (this.data != null) {
            return false;
        }
        File f = new File(SMMoney.getTempFile());
        try {
            byte[] someData = new byte["PHOTO:END".length()];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f.getAbsoluteFile()));
            if (in.read(someData) > 0) {
                String sData = new String(someData);
                if (sData == null || sData.startsWith("END")) {
                    in.close();
                    return false;
                }
                in.close();
                new TransactionClass().updateWithXMLFile(f);
                this.imageRecieveCounter++;
                setCurrentState(Enums.kDesktopSyncStatePhotoProcessed/*37*/);
                return true;
            }
            in.close();
            return false;
        } catch (Exception e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
            e.printStackTrace();
            return false;
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

    boolean processPhotoACK() {
        if (stringFromDataExcluding("").equals("PHOTO:OK")) {
            setCurrentState(Enums.kDesktopSyncStatePhotoACKProcessed/*45*/);
            return true;
        }
        setCurrentState(Enums.kDesktopSyncStateError/*69*/);
        return false;
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
        this.syncVersion = intFromDataExcluding("PMSYNC:");
        if (this.syncVersion > 2) {
            this.syncVersion = 2;
        } else if (this.syncVersion < 2) {
            this.delegate.runOnUiThread(new Runnable() {
                public void run() {
                    PocketMoneySyncActivity pocketMoneySyncActivity = PocketMoneySyncClass.this.delegate;
                    PocketMoneySyncClass.this.delegate.getClass();
                    pocketMoneySyncActivity.showDialog(8);
                    PocketMoneySyncClass.this.delegate.stopSyncing();
                }
            });
        }
        setCurrentState(Enums.kDesktopSyncStateSyncVersionProcessed/*16*/);
        return true;
    }

    void sendUDID() {
        setCurrentState(Enums.kDesktopSyncStateSendingUDID/*17*/);
        writeData("UDID:" + SMMoney.getID(), 18);
    }

    void getUDIDHeader() {
        setCurrentState(Enums.kDesktopSyncStateReceivingUDIDHeader/*19*/);
        readInHeaderSize(20);
    }

    void getUDID() {
        setCurrentState(Enums.kDesktopSyncStateReceivingUDID/*21*/);
        readInSize(sizeFromHeader(), 22);
    }

    boolean processUDID() {
        this.udid = stringFromDataExcluding("UDID:");
        if (Database.lastSyncTimeForUDID(this.udid) != 0) {
            setCurrentState(Enums.kDesktopSyncStateUDIDProcessed/*24*/);
            return true;
        }
        setCurrentState(Enums.kDesktopSyncStateUDIDProcessing/*23*/);
        return this.delegate.pocketMoneySyncRequestActionForFirstSyncUDID(this, this.udid);
    }

    void firstUDIDSyncAction(int action) {
        switch (action) {
            case Enums.kDesktopSyncFirstSyncActionReplaceDataOnServer /*1*/:
                break;
            case Enums.kDesktopSyncFirstSyncActionRestoreFromServer /*2*/:
                this.restoreFromServer = true;
                Database.wipeDatabase();
                break;
            case Enums.kDesktopSyncFirstSyncActionSync /*3*/:
                setCurrentState(Enums.kDesktopSyncStateUDIDProcessed/*24*/);
                break;
            default:
                setCurrentState(Enums.kDesktopSyncStateDisconnecting/*66*/);
                Database.sqlite3_rollback();
                sendFail();
                reset();
                this.delegate.udidFirstActionBlock = -1;
                return;
        }
        this.delegate.udidFirstActionBlock = 0;
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
        if (this.restoreFromServer) {
            Database.wipeDatabase();
        }
        setCurrentState(Enums.kDesktopSyncStateReceivingRecentChangesHeader/*48*/);
        readInHeaderSize(Enums.kDesktopSyncStateRecentChangesHeaderReceived/*49*/);
    }

    void getRecentChanges() {
        setCurrentState(Enums.kDesktopSyncStateReceivingRecentChanges/*50*/);
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
                if (this.server) {
                    setCurrentState(Enums.kDesktopSyncStateRecentChangesReceived/*51*/);
                } else {
                    setCurrentState(Enums.kDesktopSyncStateRecentChangesReceived/*51*/);
                }
            }
        } catch (IOException e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
        if (this.server) {
            setCurrentState(Enums.kDesktopSyncStateSentRecentChanges/*47*/);
        } else {
            setCurrentState(Enums.kDesktopSyncStateSentRecentChanges/*47*/);
        }
    }

    protected String parseXMLHeader(String sData) {
        String singleQuote = "\"";
        String DBID = "DBID=";
        int currentIndex = (sData.indexOf(DBID) + DBID.length()) + singleQuote.length();
        int bdid = Integer.parseInt(sData.substring(currentIndex, sData.indexOf(singleQuote, currentIndex)));
        String DBVersion = "DBVER=";
        currentIndex = (sData.indexOf(DBVersion, currentIndex) + DBVersion.length()) + singleQuote.length();
        int dbVersion = Integer.parseInt(sData.substring(currentIndex, sData.indexOf(singleQuote, currentIndex)));
        return sData.substring(sData.indexOf(singleQuote, currentIndex) + singleQuote.length());
    }

    void parseTransactionXML(String sData, String listTag, String recordTag, Class classOf) {
        String xmlBlock = "";
        String startTag = "<" + recordTag + ">";
        String endTag = "</" + recordTag + ">";
        String startList = "<" + listTag + ">";
        String endList = "</" + listTag + ">";
        int currentIndex = sData.indexOf(startList);
        int endIndex = sData.indexOf(endList);
        if (currentIndex == -1 || endIndex == -1) {
            Log.e(SMMoney.TAG, "couldnt find " + startList);
            return;
        }
        xmlBlock = sData.substring(currentIndex, endIndex);
        String transactionXML = "";
        currentIndex = xmlBlock.indexOf(startTag) + startTag.length();
        int cancel = startTag.length() - 1;
        while (currentIndex != cancel) {
            endIndex = xmlBlock.indexOf(endTag, currentIndex);
            try {
                String taggedString = startTag + xmlBlock.substring(currentIndex, endIndex) + endTag;
                TransactionClass record = new TransactionClass();
                record.updateWithXML(taggedString);
                processRecentTransaction(record);
            } catch (Exception e) {
                Log.e(SMMoney.TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
            currentIndex = xmlBlock.indexOf(startTag, endIndex) + startTag.length();
        }
    }

    void processAccounts(ArrayList<AccountClass> accounts) {
        ArrayList<AnonymousClass2TempTransAccountClass> overdraftAccounts = new ArrayList<>();
        ArrayList<AnonymousClass2TempTransAccountClass> keepTheChangeAccounts = new ArrayList<>();
        Iterator it = accounts.iterator();
        while (it.hasNext()) {
            AccountClass act = (AccountClass) it.next();
            if (act.getOverdraftAccount() != null && act.getOverdraftAccount().length() > 0) {
                overdraftAccounts.add(new AnonymousClass2TempTransAccountClass(act.serverID, act.getOverdraftAccount()));
            }
            if (act.getKeepTheChangeAccount() != null && act.getKeepTheChangeAccount().length() > 0) {
                keepTheChangeAccounts.add(new AnonymousClass2TempTransAccountClass(act.serverID, act.getKeepTheChangeAccount()));
            }
        }
        it = keepTheChangeAccounts.iterator();
        while (it.hasNext()) {
            AnonymousClass2TempTransAccountClass tempAct = (AnonymousClass2TempTransAccountClass) it.next();
            AccountClass act = AccountClass.recordWithServerID(tempAct.serverID);
            if (act != null) {
                act.hydrate();
                act.setKeepTheChangeAccount(tempAct.account);
                act.saveToDatabase();
            }
        }
        it = overdraftAccounts.iterator();
        while (it.hasNext()) {
            AnonymousClass2TempTransAccountClass tempAct = (AnonymousClass2TempTransAccountClass) it.next();
            AccountClass act = AccountClass.recordWithServerID(tempAct.serverID);
            if (act != null) {
                act.hydrate();
                act.setOverdraftAccount(tempAct.account);
                act.saveToDatabase();
            }
        }
    }

    void parseAccountsXML(String sData, String listTag, String recordTag, Class classOf) {
        String xmlBlock = "";
        String startTag = "<" + recordTag + ">";
        String endTag = "</" + recordTag + ">";
        String startList = "<" + listTag + ">";
        String endList = "</" + listTag + ">";
        ArrayList<AnonymousClass3TempTransAccountClass> overdraftAccounts = new ArrayList<>();
        ArrayList<AnonymousClass3TempTransAccountClass> keepTheChangeAccounts = new ArrayList<>();
        int currentIndex = sData.indexOf(startList);
        int endIndex = sData.indexOf(endList);
        if (currentIndex == -1 || endIndex == -1) {
            Log.e(SMMoney.TAG, "couldnt find " + startList);
            return;
        }
        xmlBlock = sData.substring(currentIndex, endIndex);
        String transactionXML = "";
        currentIndex = xmlBlock.indexOf(startTag) + startTag.length();
        int cancel = startTag.length() - 1;
        while (currentIndex != cancel) {
            endIndex = xmlBlock.indexOf(endTag, currentIndex);
            try {
                String taggedString = startTag + xmlBlock.substring(currentIndex, endIndex) + endTag;
                AccountClass record = new AccountClass();
                record.updateWithXML(taggedString);
                if (record.getOverdraftAccount() != null && record.getOverdraftAccount().length() > 0) {
                    overdraftAccounts.add(new AnonymousClass3TempTransAccountClass(record.serverID, record.getOverdraftAccount()));
                }
                if (record.getKeepTheChangeAccount() != null && record.getKeepTheChangeAccount().length() > 0) {
                    keepTheChangeAccounts.add(new AnonymousClass3TempTransAccountClass(record.serverID, record.getKeepTheChangeAccount()));
                }
                processRecentAccount(record);
            } catch (Exception e) {
                Log.e(SMMoney.TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
            currentIndex = xmlBlock.indexOf(startTag, endIndex) + startTag.length();
        }
        Iterator it = keepTheChangeAccounts.iterator();
        while (it.hasNext()) {
            AnonymousClass3TempTransAccountClass tempAct = (AnonymousClass3TempTransAccountClass) it.next();
            AccountClass act = AccountClass.recordWithServerID(tempAct.serverID);
            if (act != null) {
                act.hydrate();
                act.setKeepTheChangeAccount(tempAct.account);
                act.saveToDatabase();
            }
        }
        it = overdraftAccounts.iterator();
        while (it.hasNext()) {
            AnonymousClass3TempTransAccountClass tempAct = (AnonymousClass3TempTransAccountClass) it.next();
            AccountClass act = AccountClass.recordWithServerID(tempAct.serverID);
            if (act != null) {
                act.hydrate();
                act.setOverdraftAccount(tempAct.account);
                act.saveToDatabase();
            }
        }
    }

    void parseRepeatingTransactionXML(String sData, String listTag, String recordTag, Class classOf) {
        String xmlBlock = "";
        String startTag = "<" + recordTag + ">";
        String endTag = "</" + recordTag + ">";
        String startList = "<" + listTag + ">";
        String endList = "</" + listTag + ">";
        int currentIndex = sData.indexOf(startList);
        int endIndex = sData.indexOf(endList);
        if (currentIndex == -1 || endIndex == -1) {
            Log.e(SMMoney.TAG, "couldnt find " + startList);
            return;
        }
        xmlBlock = sData.substring(currentIndex, endIndex);
        String transactionXML = "";
        currentIndex = xmlBlock.indexOf(startTag) + startTag.length();
        int cancel = startTag.length() - 1;
        while (currentIndex != cancel) {
            endIndex = xmlBlock.indexOf(endTag, currentIndex);
            try {
                String taggedString = startTag + xmlBlock.substring(currentIndex, endIndex) + endTag;
                RepeatingTransactionClass record = new RepeatingTransactionClass();
                record.updateWithXML(taggedString);
                processRecentRepeatingTransaction(record);
            } catch (Exception e) {
                Log.e(SMMoney.TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
            currentIndex = xmlBlock.indexOf(startTag, endIndex) + startTag.length();
        }
    }

    void parseXML(String sData, String listTag, String recordTag, Class classOf, String primaryKeyField) {
        String xmlBlock = "";
        String startTag = "<" + recordTag + ">";
        String endTag = "</" + recordTag + ">";
        String startList = "<" + listTag + ">";
        String endList = "</" + listTag + ">";
        int currentIndex = sData.indexOf(startList);
        int endIndex = sData.indexOf(endList);
        if (currentIndex == -1 || endIndex == -1) {
            Log.e(SMMoney.TAG, "couldnt find " + startList);
            return;
        }
        xmlBlock = sData.substring(currentIndex, endIndex);
        String transactionXML = "";
        currentIndex = xmlBlock.indexOf(startTag) + startTag.length();
        int cancel = startTag.length() - 1;
        while (currentIndex != cancel) {
            endIndex = xmlBlock.indexOf(endTag, currentIndex);
            try {
                PocketMoneyRecordClass record = (PocketMoneyRecordClass) classOf.newInstance();
                record.updateWithXML(startTag + xmlBlock.substring(currentIndex, endIndex) + endTag);
                processRecentChange(record, classOf, primaryKeyField);
            } catch (Exception e) {
                Log.e(SMMoney.TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
            currentIndex = xmlBlock.indexOf(startTag, endIndex) + startTag.length();
        }
    }

    void sendFail() {
        writeData("DATA:FAIL", 10);
    }

    private String stringFromDataExcluding(String substring) {
        String sData = "";
        sData = new String(this.data, 0, this.data.length, StandardCharsets.UTF_8);
        this.data = null;
        return sData.substring(substring.length());
    }

    private int intFromDataExcluding(String substring) {
        String sData = "";
        sData = new String(this.data, 0, this.data.length, StandardCharsets.UTF_8);
        this.data = null;
        return Integer.parseInt(sData.substring(substring.length()));
    }

    private int sizeFromHeader() {
        ByteBuffer buf = ByteBuffer.wrap(this.data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.data = null;
        return buf.getInt(0);
    }

    private byte[] packageData(String data) {
        byte[] plainData = null;
        plainData = data.getBytes(StandardCharsets.UTF_8);
        int headerLength = plainData.length;
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(headerLength);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        byte[] headerArray = buf.array();
        int i = 0;
        for (int j = headerArray.length - 1; i < j; j--) {
            byte b = headerArray[i];
            headerArray[i] = headerArray[j];
            headerArray[j] = b;
            i++;
        }
        byte[] packagedData = new byte[(headerArray.length + headerLength)];
        for (i = 0; i < headerArray.length + headerLength; i++) {
            if (i < 4) {
                packagedData[i] = headerArray[i];
            } else {
                packagedData[i] = plainData[i - headerArray.length];
            }
        }
        return packagedData;
    }

    private byte[] packageData(byte[] plainData) {
        int headerLength = plainData.length;
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(headerLength);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        byte[] headerArray = buf.array();
        int i = 0;
        for (int j = headerArray.length - 1; i < j; j--) {
            byte b = headerArray[i];
            headerArray[i] = headerArray[j];
            headerArray[j] = b;
            i++;
        }
        byte[] packagedData = new byte[(headerArray.length + headerLength)];
        for (i = 0; i < headerArray.length + headerLength; i++) {
            if (i < 4) {
                packagedData[i] = headerArray[i];
            } else {
                packagedData[i] = plainData[i - headerArray.length];
            }
        }
        return packagedData;
    }

    private byte[] packageDataWithHeader(byte[] data, int size) {
        int dataLength = data.length > size ? size : data.length;
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(size);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        byte[] headerArray = buf.array();
        int i = 0;
        for (int j = headerArray.length - 1; i < j; j--) {
            byte b = headerArray[i];
            headerArray[i] = headerArray[j];
            headerArray[j] = b;
            i++;
        }
        byte[] packagedData = new byte[(headerArray.length + dataLength)];
        for (i = 0; i < headerArray.length + dataLength; i++) {
            if (i < 4) {
                packagedData[i] = headerArray[i];
            } else {
                packagedData[i] = data[i - headerArray.length];
            }
        }
        return packagedData;
    }

    public static void printToFile(String sData, String file) {
        try {
            new File(SMMoney.getExternalPocketMoneyDirectory()).mkdirs();
            PrintWriter out = new PrintWriter(new FileWriter(SMMoney.getExternalPocketMoneyDirectory() + file));
            int length = sData.length();
            out.write(sData, 0, sData.length());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.example.smmoney.views.desktopsync;

import android.util.Log;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.CategoryBudgetClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.IDClass;
import com.example.smmoney.records.PayeeClass;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.accounts.AccountsActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class PocketMoneySyncClientClass extends PocketMoneySyncClass {
    private ArrayList<AccountClass> accounts;
    private String currentElementValue;

    boolean connectToServer() {
        if (this.asyncSocket != null) {
            return false;
        }
        this.currentState = 3;
        this.delegate.desktopSyncWithState(this, this.currentState);
        try {
            this.asyncSocket = new Socket(this.host, this.port);
            processStateLoop();
            return true;
        } catch (IOException e) {
            this.delegate.runOnUiThread(new Runnable() {
                public void run() {
                    PocketMoneySyncActivity pocketMoneySyncActivity = PocketMoneySyncClientClass.this.delegate;
                    PocketMoneySyncClientClass.this.delegate.getClass();
                    pocketMoneySyncActivity.showDialog(1);
                }
            });
            e.printStackTrace();
            return false;
        }
    }

    private void processStateLoop() {
        while (true) {
            Log.i("PMSYNCSTATETAG", "client state: " + this.currentState);
            switch (this.currentState) {
                case Enums.kDesktopSyncStateConnecting /*3*/:
                    getSyncVersionHeader();
                    break;
                case Enums.kDesktopSyncStateSentSyncVersion /*10*/:
                    getUDIDHeader();
                    break;
                case Enums.kDesktopSyncStateSyncVersionHeaderReceived /*12*/:
                    try {
                        getSyncVersion();
                        break;
                    } catch (Exception e) {
                        this.delegate.runOnUiThread(new Runnable() {
                            public void run() {
                                PocketMoneySyncClientClass.this.delegate.stopSyncing();
                            }
                        });
                        e.printStackTrace();
                        return;
                    }
                case Enums.kDesktopSyncStateSyncVersionReceived /*14*/:
                    if (!processSyncVersion()) {
                        break;
                    }
                    sendSyncVersion();
                    break;
                case Enums.kDesktopSyncStateSentUDID /*18*/:
                    getRecentChangesHeader();
                    break;
                case Enums.kDesktopSyncStateUDIDHeaderReceived /*20*/:
                    getUDID();
                    break;
                case Enums.kDesktopSyncStateUDIDReceived /*22*/:
                    if (!processUDID()) {
                        break;
                    }
                    sendRecentChanges();
                    break;
                case Enums.kDesktopSyncStateSendPhotos /*30*/:
                    if (this.imageSentCounter >= this.imageFilenames.size()) {
                        sendUDID();
                        break;
                    } else {
                        sendPhoto();
                        break;
                    }
                case Enums.kDesktopSyncStateSentPhoto /*32*/:
                    this.imageSentCounter++;
                    getPhotoACKHeader();
                    break;
                case Enums.kDesktopSyncStatePhotoHeaderReceived /*34*/:
                    getPhotos();
                    break;
                case Enums.kDesktopSyncStatePhotoReceived /*36*/:
                    if (!processPhotos()) {
                        Database.setLastSyncTime(System.currentTimeMillis() / 1000, this.udid);
                        Database.sqlite3_commit();
                        setCurrentState(Enums.kDesktopSyncStateDisconnecting/*66*/);
                        disconnect();
                        break;
                    }
                    sendPhotoACK();
                    break;
                case Enums.kDesktopSyncStateSentPhotoACK /*39*/:
                    getPhotoHeader();
                    break;
                case Enums.kDesktopSyncStatePhotoACKHeaderReceived /*41*/:
                    getPhotoACK();
                    break;
                case Enums.kDesktopSyncStatePhotoACKReceived /*43*/:
                    processPhotoACK();
                    setCurrentState(Enums.kDesktopSyncStateSendPhotos/*30*/);
                    break;
                case Enums.kDesktopSyncStateSentRecentChanges /*47*/:
                    getACKHeader();
                    break;
                case Enums.kDesktopSyncStateRecentChangesHeaderReceived /*49*/:
                    getRecentChanges();
                    break;
                case Enums.kDesktopSyncStateRecentChangesReceived /*51*/:
                    if (!processRecentChanges()) {
                        setCurrentState(Enums.kDesktopSyncStateError /*69*/);
                        sendFail();
                        break;
                    }
                    setCurrentState(Enums.kDesktopSyncStateRecentChangesProcessed /*53*/);
                    sendACK();
                    break;
                case Enums.kDesktopSyncStateSentACK /*55*/:
                    if (this.syncVersion != 1) {
                        getPhotoHeader();
                        break;
                    }
                    Database.setLastSyncTime(System.currentTimeMillis() / 1000, this.udid);
                    Database.sqlite3_commit();
                    setCurrentState(Enums.kDesktopSyncStateDisconnecting/*66*/);
                    disconnect();
                    break;
                case Enums.kDesktopSyncStateACKHeaderReceived /*57*/:
                    getACK();
                    break;
                case Enums.kDesktopSyncStateACKReceived /*59*/:
                    processACK();
                    if (this.syncVersion != 1) {
                        setCurrentState(Enums.kDesktopSyncStateSendPhotos/*30*/);
                        break;
                    } else {
                        sendUDID();
                        break;
                    }
                case Enums.kDesktopSyncStateDisconnecting /*66*/:
                case Enums.kDesktopSyncStateDisconnected /*67*/:
                    return;
                default:
                    break;
            }
        }
    }

    protected void sendRecentChanges() {
        if (this.restoreFromServer) {
            writeData("DATA:RESTORE", 47);
        } else {
            super.sendRecentChanges();
        }
    }

    public boolean processRecentChangesOLD() {
        System.gc();
        String sData = "";
        boolean z = AccountsActivity.DEBUG;
        System.gc();
        parseAccountsXML(sData, AccountClass.XML_LISTTAG_ACCOUNTS, AccountClass.XML_RECORDTAG_ACCOUNT, AccountClass.class);
        System.gc();
        parseTransactionXML(sData, TransactionClass.XML_LISTTAG_TRANSACTIONS, TransactionClass.XML_RECORDTAG_TRANSACTION, TransactionClass.class);
        System.gc();
        parseXML(sData, CategoryClass.XML_LISTTAG_CATEGORIES, CategoryClass.XML_RECORDTAG_CATEGORY, CategoryClass.class, "categoryID");
        System.gc();
        parseXML(sData, PayeeClass.XML_LISTTAG_PAYEES, PayeeClass.XML_RECORDTAG_PAYEE, PayeeClass.class, "payeeID");
        System.gc();
        parseXML(sData, IDClass.XML_LISTTAG_IDS, IDClass.XML_RECORDTAG_ID, IDClass.class, "idID");
        System.gc();
        parseXML(sData, ClassNameClass.XML_LISTTAG_CLASSES, ClassNameClass.XML_RECORDTAG_CLASS, ClassNameClass.class, "classID");
        System.gc();
        parseXML(sData, FilterClass.XML_LISTTAG_FILTERS, FilterClass.XML_RECORDTAG_FILTER, FilterClass.class, "filterID");
        System.gc();
        parseRepeatingTransactionXML(sData, RepeatingTransactionClass.XML_LISTTAG_REPEATINGTRANSACTIONS, RepeatingTransactionClass.XML_RECORDTAG_REPEATINGTRANSACTION, RepeatingTransactionClass.class);
        System.gc();
        parseXML(sData, CategoryBudgetClass.XML_LISTTAG_CATEGORYBUDGETS, CategoryBudgetClass.XML_RECORDTAG_CATEGORYBUDGET, CategoryBudgetClass.class, "categoryBudgetID");
        return true;
    }

    private boolean processRecentChanges() {
        try {
            FileInputStream fi = new FileInputStream(new File(SMMoney.getTempFile()).getAbsolutePath());
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(fi);
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        if (localName.equals(AccountClass.XML_RECORDTAG_ACCOUNT) || localName.equals(TransactionClass.XML_RECORDTAG_TRANSACTION) || localName.equals(CategoryClass.XML_RECORDTAG_CATEGORY) || localName.equals(PayeeClass.XML_RECORDTAG_PAYEE) || localName.equals(IDClass.XML_RECORDTAG_ID) || localName.equals(ClassNameClass.XML_RECORDTAG_CLASS) || localName.equals(FilterClass.XML_RECORDTAG_FILTER) || localName.equals(RepeatingTransactionClass.XML_RECORDTAG_REPEATINGTRANSACTION) || localName.equals(CategoryBudgetClass.XML_RECORDTAG_CATEGORYBUDGET)) {
            this.currentElementValue = new String("<" + localName + ">");
        } else if (localName.equals(AccountClass.XML_LISTTAG_ACCOUNTS)) {
            this.accounts = new ArrayList<>();
        } else if (this.currentElementValue == null) {
            this.currentElementValue = new String("<" + localName + ">");
        } else {
            this.currentElementValue += "<" + localName + ">";
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        if (this.currentElementValue == null) {
            this.currentElementValue = new String("</" + localName + ">");
        } else {
            this.currentElementValue += "</" + localName + ">";
        }
        if (localName.equals(AccountClass.XML_RECORDTAG_ACCOUNT)) {
            AccountClass act = new AccountClass();
            act.updateWithXML(this.currentElementValue);
            processRecentAccount(act);
            this.accounts.add(act);
        } else if (localName.equals(TransactionClass.XML_RECORDTAG_TRANSACTION)) {
            TransactionClass t = new TransactionClass();
            t.updateWithXML(this.currentElementValue);
            processRecentTransaction(t);
        } else if (localName.equals(CategoryClass.XML_RECORDTAG_CATEGORY)) {
            CategoryClass c = new CategoryClass();
            c.updateWithXML(this.currentElementValue);
            processRecentChange(c, CategoryClass.class, "categoryID");
        } else if (localName.equals(PayeeClass.XML_RECORDTAG_PAYEE)) {
            PayeeClass p = new PayeeClass(0);
            p.updateWithXML(this.currentElementValue);
            processRecentChange(p, PayeeClass.class, "payeeID");
        } else if (localName.equals(IDClass.XML_RECORDTAG_ID)) {
            IDClass c2 = new IDClass(0);
            c2.updateWithXML(this.currentElementValue);
            processRecentChange(c2, IDClass.class, "idID");
        } else if (localName.equals(ClassNameClass.XML_RECORDTAG_CLASS)) {
            ClassNameClass c3 = new ClassNameClass(0);
            c3.updateWithXML(this.currentElementValue);
            processRecentChange(c3, ClassNameClass.class, "classID");
        } else if (localName.equals(FilterClass.XML_RECORDTAG_FILTER)) {
            FilterClass c4 = new FilterClass();
            c4.updateWithXML(this.currentElementValue);
            processRecentChange(c4, FilterClass.class, "filterID");
        } else if (localName.equals(RepeatingTransactionClass.XML_RECORDTAG_REPEATINGTRANSACTION)) {
            RepeatingTransactionClass c5 = new RepeatingTransactionClass();
            c5.updateWithXML(this.currentElementValue);
            processRecentRepeatingTransaction(c5);
        } else if (localName.equals(CategoryBudgetClass.XML_RECORDTAG_CATEGORYBUDGET)) {
            CategoryBudgetClass c6 = new CategoryBudgetClass();
            c6.updateWithXML(this.currentElementValue);
            processRecentChange(c6, CategoryBudgetClass.class, "categoryBudgetID");
        } else if (localName.equals(AccountClass.XML_LISTTAG_ACCOUNTS)) {
            processAccounts(this.accounts);
            this.accounts = null;
        } else {
            return;
        }
        this.currentElementValue = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (this.currentElementValue == null) {
            this.currentElementValue = new String(new String(ch, start, length).trim());
        } else {
            this.currentElementValue += new String(ch, start, length);
        }
    }
}

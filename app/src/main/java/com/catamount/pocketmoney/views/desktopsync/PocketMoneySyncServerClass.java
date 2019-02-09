package com.catamount.pocketmoney.views.desktopsync;

import android.util.Log;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.database.Database;
import com.catamount.pocketmoney.misc.Enums;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.CategoryBudgetClass;
import com.catamount.pocketmoney.records.CategoryClass;
import com.catamount.pocketmoney.records.ClassNameClass;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.records.IDClass;
import com.catamount.pocketmoney.records.PayeeClass;
import com.catamount.pocketmoney.records.RepeatingTransactionClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class PocketMoneySyncServerClass extends PocketMoneySyncClass {
    private ArrayList<AccountClass> accounts;
    private String currentElementValue;

    public boolean startServer() {
        if (this.listeningSocket != null) {
            Log.i(PocketMoney.TAG, "server already started");
            return false;
        }
        try {
            this.listeningSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread() {
            public void run() {
                if (PocketMoneySyncServerClass.this.listeningSocket != null) {
                    try {
                        Thread.sleep(100);
                        try {
                            Socket s = PocketMoneySyncServerClass.this.listeningSocket.accept();
                            if (PocketMoneySyncServerClass.this.asyncSocket == null) {
                                PocketMoneySyncServerClass.this.asyncSocket = s;
                                PocketMoneySyncServerClass.this.setCurrentState(4);
                                PocketMoneySyncServerClass.this.processStateLoop();
                                return;
                            }
                            PocketMoneySyncActivity pocketMoneySyncActivity = PocketMoneySyncServerClass.this.delegate;
                            PocketMoneySyncServerClass.this.delegate.getClass();
                            pocketMoneySyncActivity.showDialog(4);
                        } catch (SocketException e) {
                        } catch (InterruptedIOException e2) {
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            Database.sqlite3_rollback();
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }.start();
        this.restoreFromServer = false;
        setCurrentState(7);
        return true;
    }

    public void stopServer() {
        if (this.asyncSocket != null) {
            try {
                this.asyncSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.asyncSocket = null;
        if (this.listeningSocket != null) {
            try {
                this.listeningSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        this.listeningSocket = null;
        setCurrentState(67);
    }

    public void reset() {
        this.restoreFromServer = false;
        stopServer();
        startServer();
    }

    public void processStateLoop() {
        while (true) {
            Log.i("PMSYNCSTATETAG", "server state: " + this.currentState);
            switch (this.currentState) {
                case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                    sendSyncVersion();
                    break;
                case LookupsListActivity.FILTER_DATES /*10*/:
                    try {
                        getSyncVersionHeader();
                        break;
                    } catch (Exception e) {
                        this.delegate.runOnUiThread(new Runnable() {
                            public void run() {
                                PocketMoneySyncServerClass.this.delegate.stopSyncing();
                            }
                        });
                        e.printStackTrace();
                        return;
                    }
                case LookupsListActivity.FILTER_IDS /*12*/:
                    getSyncVersion();
                    break;
                case LookupsListActivity.FILTER_CATEGORIES /*14*/:
                    if (!processSyncVersion()) {
                        break;
                    }
                    sendUDID();
                    break;
                case LookupsListActivity.ACCOUNT_LOOKUP_WITH_NONE /*18*/:
                    getRecentChangesHeader();
                    break;
                case LookupsListActivity.BUDGET_PERIOD /*20*/:
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
                        sendTheEnd();
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
                        setCurrentState(22);
                        break;
                    } else {
                        sendPhotoACK();
                        break;
                    }
                case Enums.kDesktopSyncStateSentPhotoACK /*39*/:
                    getPhotoHeader();
                    break;
                case Enums.kDesktopSyncStatePhotoACKHeaderReceived /*41*/:
                    getPhotoACK();
                    break;
                case Enums.kDesktopSyncStatePhotoACKReceived /*43*/:
                    if (!processPhotoACK()) {
                        Database.sqlite3_rollback();
                        reset();
                        break;
                    }
                    setCurrentState(30);
                    break;
                case Enums.kDesktopSyncStateSentRecentChanges /*47*/:
                    getACKHeader();
                    break;
                case Enums.kDesktopSyncStateRecentChangesHeaderReceived /*49*/:
                    getRecentChanges();
                    break;
                case Enums.kDesktopSyncStateRecentChangesReceived /*51*/:
                    if (!processRecentChanges()) {
                        setCurrentState(69);
                        reset();
                        break;
                    }
                    setCurrentState(53);
                    sendACK();
                    break;
                case Enums.kDesktopSyncStateSentACK /*55*/:
                    if (this.syncVersion != 1) {
                        getPhotoHeader();
                        break;
                    } else {
                        getUDIDHeader();
                        break;
                    }
                case Enums.kDesktopSyncStateACKHeaderReceived /*57*/:
                    getACK();
                    break;
                case Enums.kDesktopSyncStateACKReceived /*59*/:
                    processACK();
                    if (this.syncVersion != 1) {
                        setCurrentState(30);
                        break;
                    }
                    if (this.currentState == 61) {
                        Database.setLastSyncTime(System.currentTimeMillis() / 1000, this.udid);
                        Database.sqlite3_commit();
                    }
                    reset();
                    break;
                case Enums.kDesktopSyncStateSentTheEnd /*65*/:
                    Database.setLastSyncTime(System.currentTimeMillis() / 1000, this.udid);
                    Database.sqlite3_commit();
                    reset();
                    break;
                case Enums.kDesktopSyncStateDisconnecting /*66*/:
                case Enums.kDesktopSyncStateDisconnected /*67*/:
                    return;
                default:
                    break;
            }
        }
    }

    private boolean checkForRestoreAndIsFail() {
        try {
            byte[] buff = new byte["RESTORE".length()];
            FileInputStream fi = new FileInputStream(new File(PocketMoney.getTempFile()).getAbsolutePath());
            fi.read(buff, 0, "RESTORE".length());
            fi.close();
            String sData = new String(buff, 0, buff.length, "UTF-8");
            this.restoreFromServer = sData.startsWith("RESTORE");
            if (sData.startsWith("FAIL")) {
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return true;
    }

    public boolean processRecentChanges() {
        try {
            if (!checkForRestoreAndIsFail()) {
                return false;
            }
            BufferedInputStream fi = new BufferedInputStream(new FileInputStream(new File(PocketMoney.getTempFile()).getAbsolutePath()));
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(fi);
            xr.setContentHandler(this);
            xr.parse(is);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        if (localName.equals(AccountClass.XML_RECORDTAG_ACCOUNT) || localName.equals(TransactionClass.XML_RECORDTAG_TRANSACTION) || localName.equals(CategoryClass.XML_RECORDTAG_CATEGORY) || localName.equals(PayeeClass.XML_RECORDTAG_PAYEE) || localName.equals(IDClass.XML_RECORDTAG_ID) || localName.equals(ClassNameClass.XML_RECORDTAG_CLASS) || localName.equals(FilterClass.XML_RECORDTAG_FILTER) || localName.equals(RepeatingTransactionClass.XML_RECORDTAG_REPEATINGTRANSACTION) || localName.equals(CategoryBudgetClass.XML_RECORDTAG_CATEGORYBUDGET)) {
            this.currentElementValue = new String("<" + localName + ">");
        } else if (localName.equals(AccountClass.XML_LISTTAG_ACCOUNTS)) {
            this.accounts = new ArrayList();
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

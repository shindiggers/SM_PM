package com.example.smmoney.views.desktopsync;

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
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import org.xml.sax.Attributes;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

public class PocketMoneySyncClientClass extends PocketMoneySyncClass {
    private ArrayList<AccountClass> accounts;
    private String currentElementValue;

    void connectToServer() {
        if (this.asyncSocket != null) {
            return;
        }
        this.currentState = Enums.kDesktopSyncStateTriggerManual/*3*/;
        this.delegate.desktopSyncWithState(this.currentState);
        try {
            this.asyncSocket = new Socket(this.host, this.port);
            processStateLoop();
        } catch (IOException e) {
            this.delegate.runOnUiThread(() -> {
                PocketMoneySyncActivity pocketMoneySyncActivity = PocketMoneySyncClientClass.this.delegate;
                PocketMoneySyncClientClass.this.delegate.getClass();
                pocketMoneySyncActivity.showNoHostDialog();
            });
            Log.e(SMMoney.TAG, "PocketMoneySyncClientClass: IOException in connectToServer", e);
        }
    }

    private void processStateLoop() {
        while (true) {
            Log.i("PMSYNCSTATETAG", "client state: " + this.currentState);
            if (this.asyncSocket == null || this.asyncSocket.isClosed()) {
                Log.w(SMMoney.TAG, "PocketMoneySyncClientClass: Socket closed/null, exiting state loop.");
                if (this.currentState != Enums.kDesktopSyncStateDisconnected && this.currentState != Enums.kDesktopSyncStateError) {
                    disconnect();
                }
                return;
            }
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
                        this.delegate.runOnUiThread(() -> PocketMoneySyncClientClass.this.delegate.stopSyncing());
                        Log.e(SMMoney.TAG, "PocketMoneySyncClientClass: Exception in processStateLoop (getSyncVersion)", e);
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
                    if (processUDID()) {
                        sendRecentChanges();
                    }
                    break;
                case Enums.kDesktopSyncStateSendPhotos /*30*/:
                    if (this.imageSentCounter >= this.imageFilenames.size()) {
                        sendUDID();
                    } else {
                        sendPhoto();
                    }
                    break;
                case Enums.kDesktopSyncStateSentPhoto /*32*/:
                    this.imageSentCounter++;
                    getPhotoACKHeader();
                    break;
                case Enums.kDesktopSyncStatePhotoHeaderReceived /*34*/:
                    getPhotos();
                    break;
                case Enums.kDesktopSyncStatePhotoReceived /*36*/:
                    if (processPhotosFailed()) {
                        Database.setLastSyncTime(System.currentTimeMillis() / 1000, this.udid);
                        Database.sqlite3_commit();
                        setCurrentState(Enums.kDesktopSyncStateDisconnecting/*66*/);
                        disconnect();
                    } else {
                        sendPhotoACK();
                    }
                    break;
                case Enums.kDesktopSyncStateSentPhotoACK /*39*/:
                    getPhotoHeader();
                    break;
                case Enums.kDesktopSyncStatePhotoACKHeaderReceived /*41*/:
                    getPhotoACK();
                    break;
                case Enums.kDesktopSyncStatePhotoACKReceived /*43*/:
                    if (processPhotoACKFailed()) {
                        Database.sqlite3_rollback();
                        setCurrentState(Enums.kDesktopSyncStateError/*69*/);
                        disconnect();
                    } else {
                        setCurrentState(Enums.kDesktopSyncStateSendPhotos/*30*/);
                    }
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
                    } else {
                        setCurrentState(Enums.kDesktopSyncStateRecentChangesProcessed /*53*/);
                        sendACK();
                    }
                    break;
                case Enums.kDesktopSyncStateSentACK /*55*/:
                    if (this.syncVersion != 1) {
                        getPhotoHeader();
                    } else {
                        Database.setLastSyncTime(System.currentTimeMillis() / 1000, this.udid);
                        Database.sqlite3_commit();
                        setCurrentState(Enums.kDesktopSyncStateDisconnecting/*66*/);
                        disconnect();
                    }
                    break;
                case Enums.kDesktopSyncStateACKHeaderReceived /*57*/:
                    getACK();
                    break;
                case Enums.kDesktopSyncStateACKReceived /*59*/:
                    processACK();
                    if (this.currentState == Enums.kDesktopSyncStateACKProcessed/*61*/) {
                        if (this.syncVersion != 1) {
                            setCurrentState(Enums.kDesktopSyncStateSendPhotos/*30*/);
                        } else {
                            sendUDID();
                        }
                    }
                    break;
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

    private boolean processRecentChanges() {
        try {
            File tempFile = new File(SMMoney.getTempFile());
            if (!tempFile.exists() || tempFile.length() == 0) {
                return false;
            }
            FileInputStream fi = new FileInputStream(tempFile.getAbsolutePath());
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(fi);
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "PocketMoneySyncClientClass: Exception in processRecentChanges", e);
            return false;
        }
        return true;
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        if (localName.equals("SMMoney")) {
            String multCurr = atts.getValue("MULTIPLE_CURRENCIES");
            if (multCurr != null && Boolean.parseBoolean(multCurr)) {
                Prefs.setPref(Prefs.MULTIPLECURRENCIES, true);
                Database.setMultipleCurrencies(true);
            }
            String homeCurr = atts.getValue("HOME_CURRENCY");
            if (homeCurr != null && !homeCurr.isEmpty()) {
                Prefs.setPref(Prefs.HOMECURRENCYCODE, homeCurr);
                Database.setHomeCurrency(homeCurr);
            }
            String updExch = atts.getValue("UPDATE_EXCHANGE_RATES");
            if (updExch != null && Boolean.parseBoolean(updExch)) {
                Prefs.setPref(Prefs.UPDATEEXCHANGERATES, true);
            }
        }
        if (localName.equals(AccountClass.XML_RECORDTAG_ACCOUNT) || localName.equals(TransactionClass.XML_RECORDTAG_TRANSACTION) || localName.equals(CategoryClass.XML_RECORDTAG_CATEGORY) || localName.equals(PayeeClass.XML_RECORDTAG_PAYEE) || localName.equals(IDClass.XML_RECORDTAG_ID) || localName.equals(ClassNameClass.XML_RECORDTAG_CLASS) || localName.equals(FilterClass.XML_RECORDTAG_FILTER) || localName.equals(RepeatingTransactionClass.XML_RECORDTAG_REPEATINGTRANSACTION) || localName.equals(CategoryBudgetClass.XML_RECORDTAG_CATEGORYBUDGET)) {
            this.currentElementValue = "<" + localName + ">";
        } else if (localName.equals(AccountClass.XML_LISTTAG_ACCOUNTS)) {
            this.accounts = new ArrayList<>();
        } else if (this.currentElementValue == null) {
            this.currentElementValue = "<" + localName + ">";
        } else {
            this.currentElementValue += "<" + localName + ">";
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        if (this.currentElementValue == null) {
            this.currentElementValue = "</" + localName + ">";
        } else {
            this.currentElementValue += "</" + localName + ">";
        }
        switch (localName) {
            case AccountClass.XML_RECORDTAG_ACCOUNT:
                AccountClass act = new AccountClass();
                act.updateWithXML(this.currentElementValue);
                processRecentAccount(act);
                this.accounts.add(act);
                break;
            case TransactionClass.XML_RECORDTAG_TRANSACTION:
                TransactionClass t = new TransactionClass();
                t.updateWithXML(this.currentElementValue);
                processRecentTransaction(t);
                break;
            case CategoryClass.XML_RECORDTAG_CATEGORY:
                CategoryClass c = new CategoryClass();
                c.updateWithXML(this.currentElementValue);
                processRecentChange(c, CategoryClass.class, "categoryID");
                break;
            case PayeeClass.XML_RECORDTAG_PAYEE:
                PayeeClass p = new PayeeClass(0);
                p.updateWithXML(this.currentElementValue);
                processRecentChange(p, PayeeClass.class, "payeeID");
                break;
            case IDClass.XML_RECORDTAG_ID:
                IDClass c2 = new IDClass(0);
                c2.updateWithXML(this.currentElementValue);
                processRecentChange(c2, IDClass.class, "idID");
                break;
            case ClassNameClass.XML_RECORDTAG_CLASS:
                ClassNameClass c3 = new ClassNameClass(0);
                c3.updateWithXML(this.currentElementValue);
                processRecentChange(c3, ClassNameClass.class, "classID");
                break;
            case FilterClass.XML_RECORDTAG_FILTER:
                FilterClass c4 = new FilterClass();
                c4.updateWithXML(this.currentElementValue);
                processRecentChange(c4, FilterClass.class, "filterID");
                break;
            case RepeatingTransactionClass.XML_RECORDTAG_REPEATINGTRANSACTION:
                RepeatingTransactionClass c5 = new RepeatingTransactionClass();
                c5.updateWithXML(this.currentElementValue);
                processRecentRepeatingTransaction(c5);
                break;
            case CategoryBudgetClass.XML_RECORDTAG_CATEGORYBUDGET:
                CategoryBudgetClass c6 = new CategoryBudgetClass();
                c6.updateWithXML(this.currentElementValue);
                processRecentChange(c6, CategoryBudgetClass.class, "categoryBudgetID");
                break;
            case AccountClass.XML_LISTTAG_ACCOUNTS:
                processAccounts(this.accounts);
                this.accounts = null;
                break;
            default:
                return;
        }
        this.currentElementValue = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (this.currentElementValue == null) {
            this.currentElementValue = new String(ch, start, length);
        } else {
            this.currentElementValue += new String(ch, start, length);
        }
    }
}

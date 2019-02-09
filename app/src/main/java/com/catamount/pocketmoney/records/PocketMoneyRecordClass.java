package com.catamount.pocketmoney.records;

import android.util.Log;
import com.catamount.pocketmoney.PocketMoney;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PocketMoneyRecordClass extends DefaultHandler implements Serializable {
    public boolean deleted = false;
    public boolean dirty;
    public boolean hydrated;
    public String serverID;
    public GregorianCalendar timestamp;

    public void setDeleted(boolean deleteIt) {
        if (this.deleted != deleteIt) {
            this.dirty = true;
            this.deleted = deleteIt;
        }
    }

    public boolean getDeleted() {
        hydrate();
        return this.deleted;
    }

    public void setServerID(String aString) {
        if (this.serverID != null || aString != null) {
            if (this.serverID == null || aString == null || !this.serverID.equals(aString)) {
                this.dirty = true;
                this.serverID = aString;
            }
        }
    }

    public String getServerID() {
        hydrate();
        return this.serverID;
    }

    public void dehydrate() {
        dehydrateAndUpdateTimeStamp(true);
    }

    public void saveToDatabase() {
        saveToDataBaseAndUpdateTimeStamp(true);
    }

    public String encode(String aString) {
        return URLEncoder.encode(aString).replace("+", "%20");
    }

    public void dehydrateAndUpdateTimeStamp(boolean updateTimeStamp) {
        Log.i(PocketMoney.TAG, "dehydrateAndUpdateTimeStamp notOverriden");
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        Log.i(PocketMoney.TAG, "saveToDataBaseAndUpdateTimeStamp notOverriden");
    }

    public void deleteFromDatabase() {
        Log.i(PocketMoney.TAG, "deleteFromDatabase notOverriden");
    }

    public void hydrate() {
        Log.i(PocketMoney.TAG, "hydrate notOverriden");
    }

    public String XMLString() {
        return "Not overridden";
    }

    public void updateWithXML(String xmlString) {
        Log.i(PocketMoney.TAG, "updateWithXml notOverriden");
    }

    public void startDocument() {
        Log.i(PocketMoney.TAG, "startDocument notOverriden");
    }

    public void endDocument() {
        Log.i(PocketMoney.TAG, "endDocument notOverriden");
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        Log.i(PocketMoney.TAG, "startElement notOverriden");
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        Log.i(PocketMoney.TAG, "endElement notOverriden");
    }

    public void characters(char[] ch, int start, int length) {
        Log.i(PocketMoney.TAG, "characters notOverriden");
    }
}

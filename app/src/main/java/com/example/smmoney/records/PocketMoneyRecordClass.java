package com.example.smmoney.records;

import android.util.Log;

import com.example.smmoney.SMMoney;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.GregorianCalendar;

public class PocketMoneyRecordClass extends DefaultHandler implements Serializable {
    boolean deleted = false;
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

    void setServerID(String aString) {
        if (this.serverID != null || aString != null) {
            if (this.serverID == null || !this.serverID.equals(aString)) {
                this.dirty = true;
                this.serverID = aString;
            }
        }
    }

    String getServerID() {
        hydrate();
        return this.serverID;
    }

    public void dehydrate() {
        dehydrateAndUpdateTimeStamp(true);
    }

    public void saveToDatabase() {
        saveToDataBaseAndUpdateTimeStamp(true);
    }

    String encode(String aString) throws UnsupportedEncodingException {
        return URLEncoder.encode(aString, java.nio.charset.StandardCharsets.UTF_8.toString()).replace("+", "%20");
    }

    void dehydrateAndUpdateTimeStamp(boolean updateTimeStamp) {
        Log.i(SMMoney.TAG, "dehydrateAndUpdateTimeStamp notOverriden");
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        Log.i(SMMoney.TAG, "saveToDataBaseAndUpdateTimeStamp notOverriden");
    }

    public void deleteFromDatabase() {
        Log.i(SMMoney.TAG, "deleteFromDatabase notOverriden");
    }

    public void hydrate() {
        Log.i(SMMoney.TAG, "hydrate notOverriden");
    }

    public String XMLString() {
        return "Not overridden";
    }

    public void updateWithXML(String xmlString) {
        Log.i(SMMoney.TAG, "updateWithXml notOverriden");
    }

    public void startDocument() {
        Log.i(SMMoney.TAG, "startDocument notOverriden");
    }

    public void endDocument() {
        Log.i(SMMoney.TAG, "endDocument notOverriden");
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        Log.i(SMMoney.TAG, "startElement notOverriden");
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        Log.i(SMMoney.TAG, "endElement notOverriden");
    }

    public void characters(char[] ch, int start, int length) {
        Log.i(SMMoney.TAG, "characters notOverriden");
    }
}

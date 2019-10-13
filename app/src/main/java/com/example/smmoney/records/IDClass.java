package com.example.smmoney.records;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Xml;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CalExt;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

public class IDClass extends PocketMoneyRecordClass {
    public static String XML_LISTTAG_IDS = "IDS";
    public static String XML_RECORDTAG_ID = "IDCLASS";
    private String currentElementValue;
    private int idID;
    private String idName;

    private void setIDName(String aString) {
        if (this.idName != null || aString != null) {
            if (this.idName == null || !this.idName.equals(aString)) {
                this.dirty = true;
                this.idName = aString;
            }
        }
    }

    private String getIDName() {
        hydrate();
        return this.idName;
    }

    public IDClass(int pk) {
        this.idID = pk;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.IDS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"id"}, "idID=" + pk, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            String cat = curs.getString(0);
            if (cat != null) {
                this.idName = cat;
            } else {
                this.idName = "";
            }
        } else {
            this.idName = "";
        }
        this.dirty = false;
        curs.close();
    }

    public void deleteFromDatabase() {
        ContentValues values = new ContentValues();
        values.put("timestamp", System.currentTimeMillis());
        values.put("deleted", Boolean.TRUE);
        Database.update(Database.IDS_TABLE_NAME, values, "idID=" + this.idID, null);
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.IDS_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"deleted", "timestamp", "id", "serverID"}, "idID=" + this.idID, null, null, null, null);
            if (curs.getCount() != 0) {
                curs.moveToFirst();
                boolean wasDirty = this.dirty;
                int col = 1;
                setDeleted(curs.getInt(0) == 1);
                this.timestamp = new GregorianCalendar();
                int col2 = col + 1;
                this.timestamp.setTimeInMillis(((long) curs.getDouble(col)) * 1000);
                col = col2 + 1;
                String str = curs.getString(col2);
                if (str == null) {
                    str = "";
                }
                setIDName(str);
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setServerID(str);
                if (!wasDirty && this.dirty) {
                    this.dirty = false;
                }
            }
            this.hydrated = true;
            curs.close();
        }
    }

    public void dehydrateAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            ContentValues content = new ContentValues();
            content.put("deleted", this.deleted);
            String str = "timestamp";
            long currentTimeMillis = (updateTimeStamp || this.timestamp == null) ? System.currentTimeMillis() / 1000 : this.timestamp.getTimeInMillis() / 1000;
            content.put(str, currentTimeMillis);
            content.put("id", this.idName);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            Database.update(Database.IDS_TABLE_NAME, content, "idID=" + this.idID, null);
            this.dirty = false;
        }
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.idID == 0) {
                this.idID = insertIntoDatabase(this.idName);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
    }

    public static void renameFromToInDatabase(String fromText, String toText, boolean changeInTransactions) {
        int toIDID = idForID(toText);
        int fromIDID = idForID(fromText);
        if (toIDID != 0) {
            if (!fromText.equalsIgnoreCase(toText)) {
                new IDClass(fromIDID).deleteFromDatabase();
            }
            IDClass toClassNameRecord = new IDClass(toIDID);
            toClassNameRecord.hydrate();
            toClassNameRecord.setIDName(toText);
            toClassNameRecord.saveToDatabase();
        } else {
            IDClass fromClassNameRecord = new IDClass(fromIDID);
            fromClassNameRecord.hydrate();
            fromClassNameRecord.setIDName(toText);
            fromClassNameRecord.saveToDatabase();
        }
        if (changeInTransactions) {
            TransactionClass.renameIDFromTo(fromText, toText);
        }
    }

    public static void renameFromToInDatabase(String fromText, String toText) {
        if (fromText == null) {
            fromText = "";
        }
        if (toText == null) {
            toText = "";
        }
        ContentValues content = new ContentValues();
        content.put("id", toText);
        content.put("timestamp", System.currentTimeMillis() / 1000);
        try {
            Database.update(Database.IDS_TABLE_NAME, content, "id LIKE " + Database.SQLFormat(fromText), null);
        } catch (Exception e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
        }
    }

    public static int insertIntoDatabase(String newClass) {
        if (newClass == null) {
            newClass = "";
        }
        ContentValues content = new ContentValues();
        content.put("timestamp", System.currentTimeMillis() / 1000);
        content.put("id", newClass);
        content.put("serverID", Database.newServerID());
        content.put("timestamp", System.currentTimeMillis() / 1000);
        long id = Database.insert(Database.IDS_TABLE_NAME, null, content);
        if (id == -1) {
            return 0;
        }
        return (int) id;
    }

    public static int idForID(String aClass) {
        if (aClass == null || aClass.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.IDS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"idID"}, "deleted=0 AND id LIKE " + Database.SQLFormat(aClass), null, null, null, null);
        int categoryID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            categoryID = curs.getInt(0);
        }
        curs.close();
        return categoryID;
    }

    public static IDClass recordWithServerID(String serverID) {
        IDClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT idID FROM ids WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new IDClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public static ArrayList<String> allCategoriesInDatabase() {
        ArrayList<String> array = new ArrayList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.IDS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"id"}, "deleted=0", null, null, null, "UPPER(id)");
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            do {
                array.add(curs.getString(0));
            } while (curs.moveToNext());
            curs.close();
        }
        return array;
    }

    public static String closestRecordMatchForInDatabase(String aClass) {
        String aClassName = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.IDS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"id"}, "deleted=0 AND id LIKE " + aClass, null, null, null, "id ASC");
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            aClassName = curs.getString(0);
        }
        curs.close();
        return aClassName;
    }

    public void updateWithXML(String xmlTransaction) {
        try {
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(new StringReader(xmlTransaction));
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error parsing xml");
        }
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        this.currentElementValue = null;
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        if (this.currentElementValue == null) {
            this.currentElementValue = "";
        }
        switch (localName) {
            case "idID":
                this.idID = Integer.valueOf(this.currentElementValue);
                break;
            case "timestamp":
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
                break;
            case "deleted":
                boolean z = this.currentElementValue.equals("Y") || this.currentElementValue.equals("1");
                setDeleted(z);
                break;
            case "id":
                setIDName(URLDecoder.decode(this.currentElementValue));
                break;
            case "serverID":
                setServerID(this.currentElementValue);
                break;
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

    private void addText(XmlSerializer body, String text) throws IOException {
        if (text == null) {
            text = "";
        }
        body.text(text);
    }

    private void addTextWithEncoding(XmlSerializer body, String text) throws IOException {
        body.text(text == null ? "" : encode(text));
    }

    public String XMLString() {
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            public void write(int b) {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };
        XmlSerializer body = Xml.newSerializer();
        try {
            body.setOutput(output, "UTF-8");
            body.startTag(null, XML_RECORDTAG_ID);
            body.startTag(null, "idID");
            addText(body, Integer.toString(this.idID));
            body.endTag(null, "idID");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            body.startTag(null, "deleted");
            addText(body, getDeleted() ? "Y" : "N");
            body.endTag(null, "deleted");
            body.startTag(null, "timestamp");
            addText(body, this.timestamp == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(this.timestamp));
            body.endTag(null, "timestamp");
            body.startTag(null, "id");
            addTextWithEncoding(body, getIDName());
            body.endTag(null, "id");
            body.endTag(null, XML_RECORDTAG_ID);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}

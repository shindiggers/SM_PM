package com.example.smmoney.records;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Xml;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CalExt;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.parsers.SAXParserFactory;

public class ClassNameClass extends PocketMoneyRecordClass {
    public static String XML_LISTTAG_CLASSES = "CLASSES";
    public static String XML_RECORDTAG_CLASS = "CLASSCLASS";
    private int classID;
    private String className;
    private String currentElementValue;

    private void setClassName(String aString) {
        if (this.className != null || aString != null) {
            if (this.className == null || !this.className.equals(aString)) {
                this.dirty = true;
                this.className = aString;
            }
        }
    }

    private String getClassName() {
        hydrate();
        return this.className;
    }

    public ClassNameClass(int pk) {
        this.classID = pk;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CLASSES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"class"}, "classID=" + pk, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            String cat = curs.getString(0);
            if (cat != null) {
                this.className = cat;
            } else {
                this.className = "";
            }
        } else {
            this.className = "";
        }
        this.dirty = false;
        curs.close();
    }

    public void deleteFromDatabase() {
        ContentValues values = new ContentValues();
        values.put("timestamp", System.currentTimeMillis());
        values.put("deleted", Boolean.TRUE);
        Database.update(Database.CLASSES_TABLE_NAME, values, "classID=" + this.classID, null);
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.CLASSES_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"deleted", "timestamp", "class", "serverID"}, "classID=" + this.classID, null, null, null, null);
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
                setClassName(str);
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
            content.put("class", this.className);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            Database.update(Database.CLASSES_TABLE_NAME, content, "classID=" + this.classID, null);
            this.dirty = false;
        }
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.classID == 0) {
                this.classID = insertIntoDatabase(this.className);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
    }

    public static void renameFromToInDatabase(String fromText, String toText, boolean changeInTransactions) {
        int toClassNameID = idForClass(toText);
        int fromClassNameID = idForClass(fromText);
        if (toClassNameID != 0) {
            if (!fromText.equalsIgnoreCase(toText)) {
                new ClassNameClass(fromClassNameID).deleteFromDatabase();
            }
            ClassNameClass toClassNameRecord = new ClassNameClass(toClassNameID);
            toClassNameRecord.hydrate();
            toClassNameRecord.setClassName(toText);
            toClassNameRecord.saveToDatabase();
        } else {
            ClassNameClass fromClassNameRecord = new ClassNameClass(fromClassNameID);
            fromClassNameRecord.hydrate();
            fromClassNameRecord.setClassName(toText);
            fromClassNameRecord.saveToDatabase();
        }
        if (changeInTransactions) {
            TransactionClass.renameClassFromTo(fromText, toText);
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
        content.put("class", toText);
        content.put("timestamp", System.currentTimeMillis() / 1000);
        try {
            Database.update(Database.CLASSES_TABLE_NAME, content, "class LIKE " + Database.SQLFormat(fromText), null);
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
        content.put("class", newClass);
        content.put("serverID", Database.newServerID());
        content.put("timestamp", System.currentTimeMillis() / 1000);
        long id = Database.insert(Database.CLASSES_TABLE_NAME, null, content);
        if (id == -1) {
            return 0;
        }
        return (int) id;
    }

    public static int idForClassNameElseAddIfMissing(String aClass, boolean addIt) {
        int id = idForClass(aClass);
        if (id == 0 && addIt) {
            return insertIntoDatabase(aClass);
        }
        return id;
    }

    public static int idForClass(String aClass) {
        if (aClass == null || aClass.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CLASSES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"classID"}, "deleted=0 AND class LIKE " + Database.SQLFormat(aClass), null, null, null, null);
        int categoryID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            categoryID = curs.getInt(0);
        }
        curs.close();
        return categoryID;
    }

    public static ClassNameClass recordWithServerID(String serverID) {
        ClassNameClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT classID FROM classes WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new ClassNameClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public static String classForID(int pk) {
        if (pk == 0) {
            return null;
        }
        return new ClassNameClass(pk).getClassName();
    }

    public static ArrayList<String> allClassNamesInDatabase() {
        ArrayList<String> array = new ArrayList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CLASSES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"class"}, "deleted=0", null, null, null, "UPPER(class)");
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            do {
                array.add(curs.getString(0));
            } while (curs.moveToNext());
            curs.close();
        }
        return array;
    }

    public static int countOfClassInDatabase(String aClass) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.SPLITS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"count(classID)"}, "classID=" + aClass, null, null, null, null);
        int count = curs.getInt(0);
        curs.close();
        return count;
    }

    public static String closestRecordMatchForInDatabase(String aClass) {
        String aClassName = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CLASSES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"class"}, "deleted=0 AND class LIKE " + aClass, null, null, null, "class ASC");
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
            case "classID":
                this.classID = Integer.parseInt(this.currentElementValue);
                break;
            case "timestamp":
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
                break;
            case "deleted":
                boolean z = this.currentElementValue.equals("Y") || this.currentElementValue.equals("1");
                setDeleted(z);
                break;
            case "class":
                setClassName(URLDecoder.decode(this.currentElementValue));
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
            body.startTag(null, XML_RECORDTAG_CLASS);
            body.startTag(null, "classID");
            addText(body, Integer.toString(this.classID));
            body.endTag(null, "classID");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            body.startTag(null, "deleted");
            addText(body, getDeleted() ? "Y" : "N");
            body.endTag(null, "deleted");
            body.startTag(null, "timestamp");
            addText(body, this.timestamp == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(this.timestamp));
            body.endTag(null, "timestamp");
            body.startTag(null, "class");
            addTextWithEncoding(body, getClassName());
            body.endTag(null, "class");
            body.endTag(null, XML_RECORDTAG_CLASS);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}

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

public class PayeeClass extends PocketMoneyRecordClass {
    public static final String XML_LISTTAG_PAYEES = "PAYEES";
    public static final String XML_RECORDTAG_PAYEE = "PAYEECLASS";
    private static String catpayee_statement = null;
    private String currentElementValue;
    private String payee;
    private int payeeID;

    private void setPayee(String aString) {
        if (this.payee != null || aString != null) {
            if (this.payee == null || !this.payee.equals(aString)) {
                this.dirty = true;
                this.payee = aString;
            }
        }
    }

    private String getPayee() {
        hydrate();
        return this.payee;
    }

    public PayeeClass(int pk) {
        this.payeeID = pk;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.PAYEES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"payee"}, "payeeID=" + pk, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            String cat = curs.getString(0);
            if (cat != null) {
                this.payee = cat;
            } else {
                this.payee = "";
            }
        } else {
            this.payee = "";
        }
        this.dirty = false;
        curs.close();
    }

    public void deleteFromDatabase() {
        ContentValues values = new ContentValues();
        values.put("timestamp", System.currentTimeMillis());
        values.put("deleted", Boolean.TRUE);
        Database.update(Database.PAYEES_TABLE_NAME, values, "payeeID=" + this.payeeID, null);
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.PAYEES_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"deleted", "timestamp", "payee", "serverID"}, "payeeID=" + this.payeeID, null, null, null, null);
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
                setPayee(str);
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
            content.put("payee", this.payee);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            Database.update(Database.PAYEES_TABLE_NAME, content, "payeeID=" + this.payeeID, null);
            this.dirty = false;
        }
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.payeeID == 0) {
                this.payeeID = insertIntoDatabase(this.payee);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
    }

    public static void renameFromToInDatabase(String fromText, String toText, boolean changeInTransactions) {
        int toPayeeID = idForPayee(toText);
        int fromPayeeID = idForPayee(fromText);
        if (toPayeeID != 0) {
            if (!fromText.equalsIgnoreCase(toText)) {
                new PayeeClass(fromPayeeID).deleteFromDatabase();
            }
            PayeeClass toPayeeRecord = new PayeeClass(toPayeeID);
            toPayeeRecord.hydrate();
            toPayeeRecord.setPayee(toText);
            toPayeeRecord.saveToDatabase();
        } else {
            PayeeClass fromPayeeRecord = new PayeeClass(fromPayeeID);
            fromPayeeRecord.hydrate();
            fromPayeeRecord.setPayee(toText);
            Log.i("", "updating (" + fromText + ") to (" + toText + ")");
            fromPayeeRecord.saveToDatabase();
        }
        if (changeInTransactions) {
            TransactionClass.renamePayeeFromTo(fromText, toText);
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
        content.put("payee", toText);
        content.put("timestamp", System.currentTimeMillis() / 1000);
        try {
            Database.update(Database.PAYEES_TABLE_NAME, content, "payee LIKE " + Database.SQLFormat(fromText), null);
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
        content.put("payee", newClass);
        content.put("serverID", Database.newServerID());
        content.put("timestamp", System.currentTimeMillis() / 1000);
        long id = Database.insert(Database.PAYEES_TABLE_NAME, null, content);
        if (id == -1) {
            return 0;
        }
        return (int) id;
    }

    public static int idForPayeeElseAddIfMissing(String aClass, boolean addIt) {
        int id = idForPayee(aClass);
        if (id == 0 && addIt) {
            return insertIntoDatabase(aClass);
        }
        return id;
    }

    public static int idForPayee(String aClass) {
        if (aClass == null || aClass.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.PAYEES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"payeeID"}, "deleted=0 AND payee LIKE " + Database.SQLFormat(aClass), null, null, null, null);
        int categoryID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            categoryID = curs.getInt(0);
        }
        curs.close();
        return categoryID;
    }

    public static String payeeForID(int pk) {
        if (pk == 0) {
            return null;
        }
        return new PayeeClass(pk).getPayee();
    }

    public static PayeeClass recordWithServerID(String serverID) {
        PayeeClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT payeeID FROM payees WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new PayeeClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public static ArrayList<String> allPayeesInDatabase() {
        ArrayList<String> array = new ArrayList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.PAYEES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"payee"}, "deleted=0", null, null, null, "UPPER(payee)");
        if (curs.getCount() == 0) {
            curs.close();
        } else {
            curs.moveToFirst();
            do {
                array.add(curs.getString(0));
            } while (curs.moveToNext());
            curs.close();
        }
        return array;
    }

    public static ArrayList<String> allPayeesInDatabaseForCategory(String payee) {
        ArrayList<String> array = new ArrayList<>();
        if (catpayee_statement == null) {
            catpayee_statement = "SELECT DISTINCT t.payee FROM splits s INNER JOIN transactions t WHERE s.transactionID = t.transactionID AND t.deleted = 0 AND s.categoryID LIKE ? ORDER BY UPPER(t.payee)";
        }
        Cursor c = Database.rawQuery(catpayee_statement, new String[]{payee});
        while (c.moveToNext()) {
            array.add(c.getString(0));
        }
        c.close();
        return array;
    }

    public static String closestRecordMatchForInDatabase(String aClass) {
        String aPayee = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.PAYEES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"payee"}, "deleted=0 AND payee LIKE " + Database.SQLFormat(aClass), null, null, null, "payee ASC");
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            aPayee = curs.getString(0);
        }
        curs.close();
        return aPayee;
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
            case "payeeID":
                this.payeeID = Integer.valueOf(this.currentElementValue);
                break;
            case "timestamp":
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
                break;
            case "deleted":
                boolean z = this.currentElementValue.equals("Y") || this.currentElementValue.equals("1");
                setDeleted(z);
                break;
            case "serverID":
                setServerID(this.currentElementValue);
                break;
            case "payee":
                Class<?> c = getClass();
                try {
                    c.getDeclaredField(localName).set(this, URLDecoder.decode(this.currentElementValue));
                } catch (Exception e) {
                    Log.i(SMMoney.TAG, "Invalid tag parsing " + c.getName() + " xml[" + localName + "]");
                }
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
            body.startTag(null, XML_RECORDTAG_PAYEE);
            body.startTag(null, "payeeID");
            addText(body, Integer.toString(this.payeeID));
            body.endTag(null, "payeeID");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            body.startTag(null, "deleted");
            addText(body, getDeleted() ? "Y" : "N");
            body.endTag(null, "deleted");
            body.startTag(null, "timestamp");
            addText(body, this.timestamp == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(this.timestamp));
            body.endTag(null, "timestamp");
            body.startTag(null, "payee");
            addTextWithEncoding(body, getPayee());
            body.endTag(null, "payee");
            body.endTag(null, XML_RECORDTAG_PAYEE);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}

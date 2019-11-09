package com.example.smmoney.records;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Xml;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CalExt;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.jar.Attributes;

import javax.xml.parsers.SAXParserFactory;

public class CategoryBudgetClass extends PocketMoneyRecordClass {
    public static String XML_LISTTAG_CATEGORYBUDGETS = "CATEGORYBUDGETS";
    public static String XML_RECORDTAG_CATEGORYBUDGET = "CATEGORYBUDGETCLASS";
    private static String deletecatbudget_statement = null;
    private double budgetLimit;
    public int categoryBudgetID;
    private String categoryName;
    private String currentElementValue;
    private SQLiteDatabase database;
    private GregorianCalendar date;
    private boolean resetRollover;

    private CategoryBudgetClass(int pk) {
        this.categoryBudgetID = pk;
        this.resetRollover = false;
        this.budgetLimit = 0.0d;
        this.deleted = false;
        this.dirty = false;
        this.hydrated = false;
    }

    public CategoryBudgetClass() {
        this.resetRollover = false;
        this.budgetLimit = 0.0d;
        this.deleted = false;
        this.dirty = false;
    }

    public double getBudgetLimit() {
        hydrate();
        return this.budgetLimit;
    }

    public void setBudgetLimit(double limit) {
        if (this.budgetLimit != limit) {
            this.dirty = true;
            this.budgetLimit = limit;
        }
    }

    public boolean getResetRollover() {
        hydrate();
        return this.resetRollover;
    }

    public void setResetRollover(boolean rollover) {
        if (this.resetRollover != rollover) {
            this.dirty = true;
            this.resetRollover = rollover;
        }
    }

    public GregorianCalendar getDate() {
        hydrate();
        return this.date;
    }

    public void setDate(GregorianCalendar dateFromDescriptionWithISO861Date) {
        if (this.date != dateFromDescriptionWithISO861Date) {
            this.dirty = true;
            this.date = dateFromDescriptionWithISO861Date;
        }
    }

    private String getCategoryName() {
        hydrate();
        return this.categoryName;
    }

    public void setCategoryName(String category) {
        if (this.categoryName == null || !this.categoryName.equals(category)) {
            this.dirty = true;
            this.categoryName = category;
        }
    }

    public static CategoryBudgetClass recordWithServerID(String serverID) {
        CategoryBudgetClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT categoryBudgetID FROM categoryBudgets WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new CategoryBudgetClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables("categoryBudgets");
            Cursor curs = Database.query(qb, new String[]{"deleted", "timestamp", "categoryName", "date", "budgetLimit", "resetRollover", "serverID"}, "categoryBudgetID=" + this.categoryBudgetID, null, null, null, null);
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
                setCategoryName(str);
                this.date = new GregorianCalendar();
                col2 = col + 1;
                this.date.setTimeInMillis(((long) curs.getDouble(col)) * 1000);
                col = col2 + 1;
                this.budgetLimit = curs.getDouble(col2);
                col2 = col + 1;
                this.resetRollover = curs.getInt(col) == 1;
                str = curs.getString(col2);
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
            content.put("categoryName", this.categoryName);
            content.put("budgetLimit", this.budgetLimit);
            content.put("date", this.date == null ? System.currentTimeMillis() / 1000 : this.date.getTimeInMillis() / 1000);
            content.put("resetRollover", this.resetRollover);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            Database.update("categoryBudgets", content, "categoryBudgetID=" + this.categoryBudgetID, null);
            this.dirty = false;
        }
        this.hydrated = false;
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.categoryBudgetID == 0) {
                this.categoryBudgetID = insertNewBudgetItemWithCategoryNameIntoDatabase(this.categoryName);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
    }

    private static int insertNewBudgetItemWithCategoryNameIntoDatabase(String cat) {
        if (cat == null || cat.length() == 0) {
            return 0;
        }
        ContentValues content = new ContentValues();
        content.put("timestamp", System.currentTimeMillis() / 1000);
        content.put("categoryName", cat);
        content.put("serverID", Database.newServerID());
        long id = Database.replace("categoryBudgets", null, content);
        if (id != -1) {
            return (int) id;
        }
        return 0;
    }

    static double limitPrior(GregorianCalendar toDate, double originalLimit, String category) {
        Cursor curs = Database.rawQuery("SELECT budgetLimit FROM categoryBudgets WHERE deleted=0 AND date < " + toDate.getTimeInMillis() / 1000 + " AND categoryName LIKE " + Database.SQLFormat(category) + " ORDER BY date DESC", null);
        if (curs.getCount() > 0) {
            curs.moveToFirst();
            double limit = curs.getDouble(0);
            curs.close();
            return limit;
        }
        curs.close();
        return originalLimit;
    }

    static GregorianCalendar firstRolloverDatePriorTo(GregorianCalendar date, String category) {
        Cursor curs = Database.rawQuery("SELECT date FROM categoryBudgets WHERE deleted=0 AND date < " + date.getTimeInMillis() / 1000 + " AND resetRollover=1 AND categoryName LIKE " + Database.SQLFormat(category) + " ORDER BY date DESC", null);
        GregorianCalendar cal = null;
        if (curs.getCount() > 0) {
            curs.moveToFirst();
            cal = new GregorianCalendar();
            cal.setTimeInMillis(((long) curs.getDouble(0)) * 1000);
        }
        curs.close();
        return cal;
    }

    static GregorianCalendar firstDateOfTransactionPriorTo(GregorianCalendar date, String category) {
        Cursor curs = Database.rawQuery("SELECT date FROM transactions WHERE deleted=0 AND date < " + date.getTimeInMillis() + " AND transactionID IN( SELECT transactionID FROM splits WHERE categoryID LIKE " + Database.SQLFormat(category) + " ) ORDER BY date", null);
        GregorianCalendar cal = null;
        if (curs.getCount() > 0) {
            curs.moveToFirst();
            cal = new GregorianCalendar();
            cal.setTimeInMillis(((long) curs.getDouble(0)) * 1000);
        }
        curs.close();
        return cal;
    }

    public static List<CategoryBudgetClass> budgetItemsWithRolloverForCategory(String category, GregorianCalendar startDate, GregorianCalendar endDate) {
        Cursor curs = Database.rawQuery("SELECT categoryBudgetID FROM categoryBudgets WHERE deleted=0 AND resetRollover=1 AND date >= " + startDate.getTimeInMillis() + " AND date <= " + endDate.getTimeInMillis() + " AND categoryName LIKE " + Database.SQLFormat(category) + " ORDER BY date", null);
        int count = curs.getCount();
        curs.moveToFirst();
        ArrayList<CategoryBudgetClass> budgets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            budgets.add(new CategoryBudgetClass(curs.getInt(0)));
            curs.moveToNext();
        }
        curs.close();
        return budgets;
    }

    static List<CategoryBudgetClass> budgetItems(String forCategory, GregorianCalendar startDate, GregorianCalendar endDate) {
        Cursor curs = Database.rawQuery("SELECT categoryBudgetID FROM categoryBudgets WHERE deleted=0 AND date >= " + startDate.getTimeInMillis() / 1000 + " AND date <= " + endDate.getTimeInMillis() / 1000 + " AND categoryName LIKE " + Database.SQLFormat(forCategory) + " ORDER BY date", null);
        int count = curs.getCount();
        curs.moveToFirst();
        ArrayList<CategoryBudgetClass> budgets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            budgets.add(new CategoryBudgetClass(curs.getInt(0)));
            curs.moveToNext();
        }
        curs.close();
        return budgets;
    }

    public static void renameBudgetItems(String oldCategory, String newCategory) {
        Database.rawQuery("UPDATE categoryBudgets SET categoryName=" + Database.SQLFormat(oldCategory) + ", timestamp=" + System.currentTimeMillis() + " WHERE categoryName=" + Database.SQLFormat(newCategory), null);
    }

    public static ArrayList<CategoryBudgetClass> budgetItemsForCategory(String category) {
        Cursor curs = Database.rawQuery("SELECT categoryBudgetID FROM categoryBudgets WHERE deleted=0 AND categoryName LIKE " + Database.SQLFormat(category) + " ORDER BY date", null);
        int count = curs.getCount();
        curs.moveToFirst();
        ArrayList<CategoryBudgetClass> cats = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cats.add(new CategoryBudgetClass(curs.getInt(0)));
            curs.moveToNext();
        }
        curs.close();
        return cats;
    }

    public static void deleteCategoryBudgetItemsForCateory(String cat) {
        if (deletecatbudget_statement == null) {
            deletecatbudget_statement = "UPDATE categoryBudgets SET deleted='1', timestamp=? WHERE categoryName=?";
        }
        Database.execSQL(deletecatbudget_statement, new String[]{String.valueOf(((double) System.currentTimeMillis()) / 1000.0d), cat});
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

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        this.currentElementValue = null;
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        boolean z = false;
        if (this.currentElementValue == null) {
            this.currentElementValue = "";
        }
        switch (localName) {
            case "timestamp":
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
                break;
            case "deleted":
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setDeleted(z);
                break;
            case "categoryName":
                setCategoryName(this.currentElementValue);
                break;
            case "date":
                setDate(CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue));
                break;
            case "resetRollover":
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setResetRollover(z);
                break;
            case "serverID":
                setServerID(this.currentElementValue);
                break;
            case "budgetLimit":
                setBudgetLimit(Double.valueOf(this.currentElementValue));
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
            body.startTag(null, XML_RECORDTAG_CATEGORYBUDGET);
            body.startTag(null, "categoryName");
            addText(body, getCategoryName());
            body.endTag(null, "categoryName");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            if (this.deleted) {
                body.startTag(null, "deleted");
                addText(body, getDeleted() ? "Y" : "N");
                body.endTag(null, "deleted");
            }
            body.startTag(null, "budgetLimit");
            addText(body, Double.toString(this.budgetLimit));
            body.endTag(null, "budgetLimit");
            body.startTag(null, "resetRollover");
            addText(body, this.resetRollover ? "Y" : "N");
            body.endTag(null, "resetRollover");
            body.startTag(null, "timestamp");
            addText(body, this.timestamp == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(this.timestamp));
            body.endTag(null, "timestamp");
            body.startTag(null, "date");
            addText(body, this.date == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(this.date));
            body.endTag(null, "date");
            body.endTag(null, XML_RECORDTAG_CATEGORYBUDGET);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}

package com.example.smmoney.records;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Xml;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.splits.SplitsActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

public class FilterClass extends PocketMoneyRecordClass implements Serializable {
    public static final String XML_LISTTAG_FILTERS = "FILTERS";
    public static final String XML_RECORDTAG_FILTER = "FILTERCLASS";
    private String account = "";
    private String category = "";
    private String checkNumber = "";
    private String className = "";
    private int cleared;
    private boolean currentAccount;
    private String currentElementValue;
    private boolean customFilter;
    private String date = "";
    private GregorianCalendar dateFrom = null;
    private GregorianCalendar dateTo = null;
    private int filterID = 0;
    private String filterName = "";
    private double internalFromDate = 0.0d;
    private double internalToDate = 0.0d;
    private String payee = "";
    private String spotlight = "";
    private int type;

    public static ArrayList<String> transactionTypes() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_GENERAL_WITHDRAWAL);
        list.add(Locales.kLOC_GENERAL_DEPOSIT);
        list.add(Locales.kLOC_GENERAL_TRANSFER);
        list.add(Locales.kLOC_PREFERENCES_SHOW_ALL);
        return list;
    }

    public static ArrayList<String> clearedTypes() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_GENERAL_CLEARED);
        list.add(Locales.kLOC_FILTER_UNCLEARED);
        list.add(Locales.kLOC_FILTER_DOESNTMATTER);
        return list;
    }

    public static ArrayList<String> dateRanges() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_FILTER_DATES_ALL);
        list.add(Locales.kLOC_FILTER_DATES_CUSTOM);
        list.add(Locales.kLOC_FILTER_DATES_MODIFIEDTODAY);
        list.add(Locales.kLOC_FILTER_DATES_TODAY);
        list.add(Locales.kLOC_FILTER_DATES_YESTERDAY);
        list.add(Locales.kLOC_FILTER_DATES_LAST30DAYS);
        list.add(Locales.kLOC_FILTER_DATES_LAST60DAYS);
        list.add(Locales.kLOC_FILTER_DATES_LAST90DAYS);
        list.add(Locales.kLOC_FILTER_DATES_THISWEEK);
        list.add(Locales.kLOC_FILTER_DATES_LASTWEEK);
        list.add(Locales.kLOC_FILTER_DATES_THISMONTH);
        list.add(Locales.kLOC_FILTER_DATES_LASTMONTH);
        list.add(Locales.kLOC_FILTER_DATES_THISQUARTER);
        list.add(Locales.kLOC_FILTER_DATES_LASTQUARTER);
        list.add(Locales.kLOC_FILTER_DATES_THISYEAR);
        list.add(Locales.kLOC_FILTER_DATES_LASTYEAR);
        return list;
    }

    public FilterClass() {
        setCleared(Enums.kClearedDoesntMatter /*2*/);
        setType(Enums.kTransactionTypeAll /*4*/);
        this.customFilter = false;
    }

    public FilterClass(String act) {
        setCleared(Enums.kClearedDoesntMatter /*2*/);
        setType(Enums.kTransactionTypeAll /*4*/);
        this.customFilter = false;
        setAccount(act);
    }

    public FilterClass(int pk) {
        this.filterID = pk;
        this.hydrated = false;
        this.dirty = false;
    }

    public boolean allAccounts() {
        return getAccount() == null || getAccount().length() == 0 || this.account.equals(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
    }

    public void setClearedFromString(String aString) {
        if (aString.equals(Locales.kLOC_GENERAL_CLEARED)) {
            setCleared(Enums.kClearedCleared /*1*/);
        } else if (aString.equals(Locales.kLOC_FILTER_UNCLEARED)) {
            setCleared(Enums.kClearedUncleared/*0*/);
        } else {
            setCleared(Enums.kClearedDoesntMatter /*2*/);
        }
    }

    public String clearedAsString() {
        switch (getCleared()) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return Locales.kLOC_FILTER_UNCLEARED;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return Locales.kLOC_GENERAL_CLEARED;
            default:
                return Locales.kLOC_FILTER_DOESNTMATTER;
        }
    }

    public void setTypeFromString(String aString) {
        if (aString.equals(Locales.kLOC_GENERAL_WITHDRAWAL)) {
            setType(Enums.kTransactionTypeWithdrawal /*0*/);
        } else if (aString.equals(Locales.kLOC_GENERAL_DEPOSIT)) {
            setType(Enums.kTransactionTypeDeposit /*1*/);
        } else if (aString.equals(Locales.kLOC_GENERAL_TRANSFER)) {
            setType(Enums.kTransactionTypeTransferFrom /*3*/);
        } else {
            setType(Enums.kTransactionTypeAll /*4*/);
        }
    }

    public String typeAsString() {
        switch (getType()) {
            case Enums.kTransactionTypeWithdrawal /*0*/:
                return Locales.kLOC_GENERAL_WITHDRAWAL;
            case Enums.kTransactionTypeDeposit /*1*/:
                return Locales.kLOC_GENERAL_DEPOSIT;
            case Enums.kTransactionTypeTransferTo /*2*/:
            case Enums.kTransactionTypeTransferFrom /*3*/:
                return Locales.kLOC_GENERAL_TRANSFER;
            default:
                return Locales.kLOC_PREFERENCES_SHOW_ALL;
        }
    }

    public void setCustomFilter(boolean aBool) {
        this.customFilter = aBool;
    }

    public boolean customFilter() {
        return this.customFilter || getAccount() == null || this.spotlight.length() > 0;
    }

    public void setFilterName(String aString) {
        if (this.filterName != null || aString != null) {
            if (this.filterName == null || !this.filterName.equals(aString)) {
                this.dirty = true;
                this.filterName = aString;
            }
        }
    }

    public String getFilterName() {
        hydrate();
        return this.filterName;
    }

    public void setType(int atype) {
        if (this.type != atype) {
            this.dirty = true;
            this.type = atype;
            if (this.type != 4) {
                this.customFilter = true;
            }
        }
    }

    public int getType() {
        hydrate();
        return this.type;
    }

    private double internalDateAsDateUsingFromDate(boolean isFromDate) {
        if (Locales.kLOC_FILTER_DATES_ALL.equals(this.date)) {
            return (double) Enums.kDateRangeNone; /*0.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_TODAY.equals(this.date)) {
            return (double) Enums.kDateRangeToday; /*1.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_YESTERDAY.equals(this.date)) {
            return (double) Enums.kDateRangeYesterday ; /*2.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_LAST30DAYS.equals(this.date)) {
            return (double) Enums.kDateRangeLast30Days; /*15.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_LAST60DAYS.equals(this.date)) {
            return (double) Enums.kDateRangeLast60Days; /*16.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_LAST90DAYS.equals(this.date)) {
            return (double) Enums.kDateRangeLast90Days; /*17.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_THISWEEK.equals(this.date)) {
            return (double) Enums.kDateRangeCurrentWeek; /*3.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_LASTWEEK.equals(this.date)) {
            return (double) Enums.kDateRangeLastWeek; /*4.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_THISMONTH.equals(this.date)) {
            return (double) Enums.kDateRangeCurrentMonth; /*5.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_LASTMONTH.equals(this.date)) {
            return (double) Enums.kDateRangeLastMonth; /*6.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_THISQUARTER.equals(this.date)) {
            return (double) Enums.kDateRangeCurrentQuarter; /*7.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_LASTQUARTER.equals(this.date)) {
            return (double) Enums.kDateRangeLastQuarter; /*8.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_THISYEAR.equals(this.date)) {
            return (double) Enums.kDateRangeCurrentYear; /*9.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_LASTYEAR.equals(this.date)) {
            return (double) Enums.kDateRangeLastYear; /*10.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_RECENTLYCHANGED.equals(this.date)) {
            return (double) Enums.kDateRangeRecentChanges; /*11.0d*/
        }
        if (Locales.kLOC_FILTER_DATES_MODIFIEDTODAY.equals(this.date)) {
            return (double) Enums.kDateRangeModifiedToday; /*14.0d*/
        }
        if (isFromDate) {
            if (this.dateFrom != null) {
                return (double) (this.dateFrom.getTimeInMillis() / 1000);
            }
            return (double) Enums.kDateRangeNoFromDate; /*12.0d*/
        } else if (this.dateTo != null) {
            return (double) (this.dateTo.getTimeInMillis() / 1000);
        } else {
            return (double) Enums.kDateRangeNoToDate; /*13.0d*/
        }
    }

    private GregorianCalendar dateFromInternalDate(double internalDate) {
        if (Enums.kDateRangeOther /*18.0d*/ >= internalDate) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(((long) internalDate) * 1000);
        return cal;
    }

    private String dateStringFromDBDate(double internalDate) {
        switch ((int) internalDate) {
            case Enums.kDateRangeNone /*0*/:
                return Locales.kLOC_FILTER_DATES_ALL;
            case Enums.kDateRangeToday /*1*/:
                return Locales.kLOC_FILTER_DATES_TODAY;
            case Enums.kDateRangeYesterday /*2*/:
                return Locales.kLOC_FILTER_DATES_YESTERDAY;
            case Enums.kDateRangeCurrentWeek /*3*/:
                return Locales.kLOC_FILTER_DATES_THISWEEK;
            case Enums.kDateRangeLastWeek /*4*/:
                return Locales.kLOC_FILTER_DATES_LASTWEEK;
            case Enums.kDateRangeCurrentMonth /*5*/:
                return Locales.kLOC_FILTER_DATES_THISMONTH;
            case Enums.kDateRangeLastMonth /*6*/:
                return Locales.kLOC_FILTER_DATES_LASTMONTH;
            case Enums.kDateRangeCurrentQuarter /*7*/:
                return Locales.kLOC_FILTER_DATES_THISQUARTER;
            case Enums.kDateRangeLastQuarter /*8*/:
                return Locales.kLOC_FILTER_DATES_LASTQUARTER;
            case Enums.kDateRangeCurrentYear /*9*/:
                return Locales.kLOC_FILTER_DATES_THISYEAR;
            case Enums.kDateRangeLastYear /*10*/:
                return Locales.kLOC_FILTER_DATES_LASTYEAR;
            case Enums.kDateRangeRecentChanges /*11*/:
                return Locales.kLOC_FILTER_DATES_RECENTLYCHANGED;
            case Enums.kDateRangeModifiedToday /*14*/:
                return Locales.kLOC_FILTER_DATES_MODIFIEDTODAY;
            case Enums.kDateRangeLast30Days /*15*/:
                return Locales.kLOC_FILTER_DATES_LAST30DAYS;
            case Enums.kDateRangeLast60Days /*16*/:
                return Locales.kLOC_FILTER_DATES_LAST60DAYS;
            case Enums.kDateRangeLast90Days /*17*/:
                return Locales.kLOC_FILTER_DATES_LAST90DAYS;
            default:
                return Locales.kLOC_FILTER_DATES_CUSTOM;
        }
    }

    public boolean isCustomDate() {
        return Locales.kLOC_FILTER_DATES_CUSTOM.equals(this.date);
    }

    public String customDateString() {
        return (getDateFrom() != null ? CalExt.descriptionWithMediumDate(getDateFrom()) : "*") + "<->" + (getDateTo() != null ? CalExt.descriptionWithMediumDate(getDateTo()) : "*");
    }

    public void setDate(String aString) {
        if (this.date != null || aString != null) {
            if (this.date == null || !this.date.equals(aString)) {
                this.dirty = true;
                this.date = aString;
                if (this.date.length() > 0 && !this.date.equals(Locales.kLOC_FILTER_DATES_ALL)) {
                    this.customFilter = true;
                }
            }
        }
    }

    public String getDate() {
        hydrate();
        if (this.date != null) {
            return this.date;
        }
        return Locales.kLOC_FILTER_DATES_ALL;
    }

    public void setDateFrom(GregorianCalendar aDate) {
        if (this.dateFrom != null || aDate != null) {
            if (this.dateFrom == null || !this.dateFrom.equals(aDate)) {
                this.dirty = true;
                this.dateFrom = aDate;
            }
        }
    }

    public GregorianCalendar getDateFrom() {
        hydrate();
        return this.dateFrom;
    }

    public void setDateTo(GregorianCalendar aDate) {
        if (this.dateTo != null || aDate != null) {
            if (this.dateTo == null || !this.dateTo.equals(aDate)) {
                this.dirty = true;
                this.dateTo = aDate;
            }
        }
    }

    public GregorianCalendar getDateTo() {
        hydrate();
        return this.dateTo;
    }

    public void setAccount(String aString) {
        if (this.account != null || aString != null) {
            if (this.account == null || !this.account.equals(aString)) {
                if (this.account == null) {
                    this.account = "";
                } else {
                    this.account = aString;
                }
                this.dirty = true;
                if (this.account == null || this.account.length() == 0 || this.account.equals(Locales.kLOC_FILTERS_ALL_ACCOUNTS)) {
                    this.customFilter = true;
                }
            }
        }
    }

    public String getAccount() {
        hydrate();
        return this.account;
    }

    public void setCategory(String aString) {
        if (this.category != null || aString != null) {
            if (this.category == null || !this.category.equals(aString)) {
                if (aString == null) {
                    this.category = "";
                } else {
                    this.category = aString;
                }
                this.dirty = true;
                if (this.category.length() > 0 && !this.category.equals(Locales.kLOC_FILTERS_ALL_CATEGORIES)) {
                    this.customFilter = true;
                }
            }
        }
    }

    public String getCategory() {
        hydrate();
        return this.category;
    }

    public void setPayee(String aString) {
        if (this.payee != null || aString != null) {
            if (this.payee == null || !this.payee.equals(aString)) {
                this.dirty = true;
                this.payee = aString;
                if (this.payee.length() > 0) {
                    this.customFilter = true;
                }
            }
        }
    }

    public String getPayee() {
        hydrate();
        return this.payee;
    }

    public void setCheckNumber(String aString) {
        if (this.checkNumber != null || aString != null) {
            if (this.checkNumber == null || !this.checkNumber.equals(aString)) {
                if (aString == null) {
                    this.checkNumber = "";
                } else {
                    this.checkNumber = aString;
                }
                this.dirty = true;
                if (this.checkNumber.length() > 0) {
                    this.customFilter = true;
                }
            }
        }
    }

    public String getCheckNumber() {
        hydrate();
        return this.checkNumber;
    }

    public void setClassName(String aString) {
        if (this.className != null || aString != null) {
            if (this.className == null || !this.className.equals(aString)) {
                if (aString == null) {
                    this.className = "";
                } else {
                    this.className = aString;
                }
                this.dirty = true;
                if (this.className.length() > 0 && !this.className.equals(Locales.kLOC_FILTERS_ALL_CLASSES)) {
                    this.customFilter = true;
                }
            }
        }
    }

    public String getClassName() {
        hydrate();
        return this.className;
    }

    public void setCleared(int clearIt) {
        if (this.cleared != clearIt) {
            this.dirty = true;
            this.cleared = clearIt;
            if (this.cleared != Enums.kClearedDoesntMatter /*2*/) {
                this.customFilter = true;
            }
        }
    }

    public int getCleared() {
        hydrate();
        return this.cleared;
    }

    public void setSpotlight(String aString) {
        if (this.spotlight != null || aString != null) {
            if (this.spotlight == null || !this.spotlight.equals(aString)) {
                this.dirty = true;
                this.spotlight = aString;
            }
        }
    }

    public String getSpotlight() {
        hydrate();
        return this.spotlight;
    }

    public FilterClass copy() {
        FilterClass dup = new FilterClass();
        dup.timestamp = (GregorianCalendar) this.timestamp.clone();
        dup.setFilterName(getFilterName());
        dup.setType(getType());
        dup.setDate(getDate());
        dup.setDateFrom(getDateFrom());
        dup.setDateTo(getDateTo());
        dup.setAccount(getAccount());
        dup.currentAccount = this.currentAccount;
        dup.setCategory(getCategory());
        dup.setPayee(getPayee());
        dup.setCheckNumber(getCheckNumber());
        dup.setClassName(getClassName());
        dup.setCleared(getCleared());
        dup.setSpotlight(getSpotlight());
        dup.customFilter = this.customFilter;
        return dup;
    }

    public boolean validTransaction(TransactionClass transaction) {
        if (getAccount().compareToIgnoreCase(transaction.getAccount()) != 0) {
            return false;
        }
        if (getCleared() != 2) {
            if ((this.cleared == 1) != transaction.getCleared()) {
                return false;
            }
        }
        if (getPayee().length() > 0 && this.payee.compareToIgnoreCase(transaction.getPayee()) != 0) {
            return false;
        }
        return getCategory().length() <= 0 || this.category.compareToIgnoreCase(transaction.getCategory()) == 0;
    }

    public boolean isValidSplit(SplitsClass split) {
        return TransactionDB.splitIDExistsWithinFilter(split.splitID, this);
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            String where = "filterID=" + this.filterID;
            String[] projection = new String[]{"deleted", "timestamp", "filterName", "type", "dateFrom", "dateTo", "accountID", "categoryID", "payee", "classID", "checkNumber", "cleared", "spotlight", "serverID"};
            qb.setTables(Database.FILTERS_TABLE_NAME);
            Cursor curs = Database.query(qb, projection, where, null, null, null, null);
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
                setFilterName(str);
                col2 = col + 1;
                setType(curs.getInt(col));
                col = col2 + 1;
                long dateFrom = (long) curs.getDouble(col2);
                col2 = col + 1;
                long dateTo = (long) curs.getDouble(col);
                if (12 == dateFrom && 13 == dateTo) {
                    setDate(Locales.kLOC_FILTER_DATES_ALL);
                } else {
                    setDate(dateStringFromDBDate((double) dateFrom));
                }
                setDateFrom(dateFromInternalDate((double) dateFrom));
                setDateTo(dateFromInternalDate((double) dateTo));
                col = col2 + 1;
                int accountID = curs.getInt(col2);
                if (accountID == -2) {
                    setAccount(Locales.kLOC_FILTERS_CURRENT_ACCOUNT);
                } else {
                    setAccount(AccountClass.accountForID(accountID));
                }
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setCategory(str);
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    str = "";
                }
                setPayee(str);
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setClassName(str);
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    str = "";
                }
                setCheckNumber(str);
                col2 = col + 1;
                setCleared(curs.getInt(col));
                col = col2 + 1;
                str = curs.getString(col2);
                if (str == null) {
                    str = "";
                }
                setSpotlight(str);
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setServerID(str);
                if (!wasDirty && this.dirty) {
                    this.dirty = false;
                }
            } else {
                this.timestamp = new GregorianCalendar();
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
            content.put("filterName", this.filterName);
            content.put("type", this.type);
            content.put("dateFrom", internalDateAsDateUsingFromDate(true));
            content.put("dateTo", internalDateAsDateUsingFromDate(false));
            int acctID = AccountClass.idForAccount(this.account);
            if (this.account.equals(Locales.kLOC_FILTERS_CURRENT_ACCOUNT)) {
                acctID = -2;
            }
            content.put("accountID", acctID);
            content.put("categoryID", this.category);
            content.put("payee", this.payee);
            content.put("checkNumber", this.checkNumber);
            content.put("classID", this.className);
            content.put("cleared", this.cleared);
            content.put("spotlight", this.spotlight);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            Database.update(Database.FILTERS_TABLE_NAME, content, "filterID=" + this.filterID, null);
            this.dirty = false;
        }
        this.hydrated = false;
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.filterID == 0) {
                this.filterID = insertIntoDatabase(this.filterName);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
    }

    public void deleteFromDatabase() {
        ContentValues values = new ContentValues();
        values.put("timestamp", System.currentTimeMillis());
        values.put("deleted", Boolean.TRUE);
        Database.update(Database.FILTERS_TABLE_NAME, values, "filterID=" + this.filterID, null);
    }

    private static int insertIntoDatabase(String name) {
        if (name == null) {
            name = "";
        }
        ContentValues content = new ContentValues();
        content.put("filterName", name);
        content.put("deleted", 0);
        content.put("serverID", Database.newServerID());
        content.put("timestamp", System.currentTimeMillis() / 1000);
        long id = Database.insert(Database.FILTERS_TABLE_NAME, null, content);
        if (id == -1) {
            id = 0;
        }
        return (int) id;
    }

    public static ArrayList<FilterClass> query() {
        ArrayList<FilterClass> theList = new ArrayList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String where = "deleted=0 AND filterName NOT IN (select account from accounts where deleted=0)  AND filterName <> '" + Locales.kLOC_FILTERS_ALL_ACCOUNTS + "'";
        String[] projection = new String[]{"filterID"};
        qb.setTables(Database.FILTERS_TABLE_NAME);
        Cursor curs = Database.query(qb, projection, where, null, null, null, "UPPER(filterName)");
        if (curs.getCount() > 0) {
            curs.moveToFirst();
            do {
                theList.add(new FilterClass(curs.getInt(0)));
            } while (curs.moveToNext());
            curs.close();
        }
        return theList;
    }

    public static int idForFilter(String filter) {
        if (filter == null || filter.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.FILTERS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"filterID"}, "deleted=0 AND filter LIKE " + Database.SQLFormat(filter), null, null, null, null);
        int filterID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            filterID = curs.getInt(0);
        }
        curs.close();
        return filterID;
    }

    public static FilterClass recordWithServerID(String serverID) {
        FilterClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT filterID FROM filters WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new FilterClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public static long convertFilterDateIsFromDate(String fromDateString, boolean isFromDate) {
        long retDate = 0;
        GregorianCalendar cal;
        if (isFromDate) {
            cal = CalExt.beginningOfToday();
            cal.setLenient(true);
            if (fromDateString.equals(Locales.kLOC_FILTER_DATES_ALL)) {
                retDate = 0;
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_TODAY)) {
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_YESTERDAY)) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LAST30DAYS)) {
                cal.add(Calendar.DAY_OF_YEAR, -30);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LAST60DAYS)) {
                cal.add(Calendar.DAY_OF_YEAR, -60);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LAST90DAYS)) {
                cal.add(Calendar.DAY_OF_YEAR, -90);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISWEEK)) {
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTWEEK)) {
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.add(Calendar.DAY_OF_MONTH, -7);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISMONTH)) {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTMONTH)) {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.MONTH, -1);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISYEAR)) {
                cal.set(Calendar.DAY_OF_YEAR, 1);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTYEAR)) {
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.add(Calendar.YEAR, -1);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISQUARTER)) {
                cal.set(Calendar.MONTH, ((((cal.get(Calendar.MONTH) - 1) / 3) * 3) + 1) - 1);
                cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTQUARTER)) {
                cal.set(Calendar.MONTH, ((((cal.get(Calendar.MONTH) - 1) / 3) * 3) + 1) - 1);
                cal.add(Calendar.MONTH, -3);
                cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));
                retDate = cal.getTimeInMillis();
            }
        } else {
            cal = CalExt.endOfToday();
            cal.setLenient(true);
            if (fromDateString.equals(Locales.kLOC_FILTER_DATES_ALL)) {
                retDate = 0;
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_TODAY)) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_YESTERDAY)) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
                retDate = CalExt.endOfDay(cal).getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LAST30DAYS)) {
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LAST60DAYS)) {
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LAST90DAYS)) {
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISWEEK)) {
                cal.set(Calendar.DAY_OF_WEEK, ((cal.getFirstDayOfWeek() + 5) % 7) + 1);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTWEEK)) {
                cal.set(Calendar.DAY_OF_WEEK, ((cal.getFirstDayOfWeek() + 5) % 7) + 1);
                cal.add(Calendar.DAY_OF_YEAR, -7);
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISMONTH)) {
                cal.set(Calendar.DAY_OF_MONTH, getMaxDayForMonth(cal));
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTMONTH)) {
                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.DAY_OF_MONTH, getMaxDayForMonth(cal));
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISYEAR)) {
                cal.set(Calendar.DAY_OF_YEAR, getMaxDayForYear(cal.get(Calendar.YEAR)));
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTYEAR)) {
                cal.add(Calendar.YEAR, -1);
                cal.set(Calendar.DAY_OF_YEAR, getMaxDayForYear(cal.get(Calendar.YEAR)));
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_THISQUARTER)) {
                cal.set(Calendar.MONTH, ((((cal.get(Calendar.MONTH) - 1) / 3) * 3) + 3) - 1);
                cal.set(Calendar.DAY_OF_MONTH, getMaxDayForMonth(cal));
                retDate = cal.getTimeInMillis();
            } else if (fromDateString.equals(Locales.kLOC_FILTER_DATES_LASTQUARTER)) {
                cal.set(Calendar.MONTH, ((((cal.get(Calendar.MONTH) - 1) / 3) * 3) + 3) - 1);
                cal.add(Calendar.MONTH, -3);
                cal.set(Calendar.DAY_OF_MONTH, getMaxDayForMonth(cal));
                retDate = cal.getTimeInMillis();
            }
        }
        return retDate / 1000;
    }

    private static int getMaxDayForMonth(GregorianCalendar cal) {
        switch (cal.get(Calendar.MONTH)) {
            case 0 /*Jan*/:
            case 2 /*Mar*/:
            case 4 /*May*/:
            case 6 /*Ju;*/:
            case 7 /*Aug*/:
            case 9 /*Oct*/:
            case 11 /*Dec*/:
                return 31;
            case 1 /*Feb*/:
                int year = cal.get(Calendar.YEAR);
                if ((year % 4 != 0 || year % 100 == 0) && year % 400 != 0) {
                    return 28;
                }
                return 29;
            case 3 /*Apr*/:
            case 5 /*Jun*/:
            case 8 /*Sep*/:
            case 10 /*Nov*/:
                return 30;
            default:
                return 0;
        }
    }

    private static int getMaxDayForYear(int year) {
        if ((year % 4 != 0 || year % 100 == 0) && year % 400 != 0) {
            return 365;
        }
        return 366;
    }

    public void updateWithXML(String xmlTransaction) {
        this.internalFromDate = 0.0d;
        this.internalToDate = 0.0d;
        try {
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(new StringReader(xmlTransaction));
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        if (0.0d == this.internalFromDate && 0.0d == this.internalToDate) {
            setDate(Locales.kLOC_FILTER_DATES_ALL);
        } else {
            setDate(dateStringFromDBDate(this.internalFromDate));
        }
        setDateFrom(dateFromInternalDate(this.internalFromDate));
        setDateTo(dateFromInternalDate(this.internalToDate));
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
            case "filterID":
                this.filterID = Integer.valueOf(this.currentElementValue);
                break;
            case "timestamp":
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
                break;
            case "deleted":
                boolean z = this.currentElementValue.equals("Y") || this.currentElementValue.equals("1");
                setDeleted(z);
                break;
            case "dateFrom":
                this.internalFromDate = Double.valueOf(this.currentElementValue);
                break;
            case "dateTo":
                this.internalToDate = Double.valueOf(this.currentElementValue);
                break;
            case "type":
                setType(Integer.valueOf(this.currentElementValue));
                break;
            case "cleared":
                setCleared(Integer.valueOf(this.currentElementValue));
                break;
            case "account":
                if (this.currentElementValue == null) {
                    setAccount(Locales.kLOC_FILTERS_ALL_ACCOUNTS);
                } else {
                    setAccount(URLDecoder.decode(this.currentElementValue));
                }
                break;
            case "categoryID":
                setCategory(URLDecoder.decode(this.currentElementValue == null ? "" : this.currentElementValue));
                break;
            case "classID":
                setClassName(URLDecoder.decode(this.currentElementValue == null ? "" : this.currentElementValue));
                break;
            case "serverID":
                setServerID(this.currentElementValue);
                break;
            case "payee":
            case "checkNumber":
            case "spotlight":
            case "selectedFilterName":
            case "filterName":
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
            String descriptionWithISO861Date;
            body.setOutput(output, "UTF-8");
            body.startTag(null, XML_RECORDTAG_FILTER);
            body.startTag(null, "filterID");
            addText(body, Integer.toString(this.filterID));
            body.endTag(null, "filterID");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            body.startTag(null, "deleted");
            addText(body, getDeleted() ? "Y" : "N");
            body.endTag(null, "deleted");
            body.startTag(null, "timestamp");
            if (this.timestamp == null) {
                descriptionWithISO861Date = CalExt.descriptionWithISO861Date(new GregorianCalendar());
            } else {
                descriptionWithISO861Date = CalExt.descriptionWithISO861Date(this.timestamp);
            }
            addText(body, descriptionWithISO861Date);
            body.endTag(null, "timestamp");
            body.startTag(null, "filterName");
            addTextWithEncoding(body, getFilterName());
            body.endTag(null, "filterName");
            body.startTag(null, "type");
            addText(body, Integer.toString(getType()));
            body.endTag(null, "type");
            body.startTag(null, "dateFrom");
            addText(body, Double.toString(internalDateAsDateUsingFromDate(true)));
            body.endTag(null, "dateFrom");
            body.startTag(null, "dateTo");
            addText(body, Double.toString(internalDateAsDateUsingFromDate(false)));
            body.endTag(null, "dateTo");
            body.startTag(null, "account");
            addTextWithEncoding(body, getAccount());
            body.endTag(null, "account");
            body.startTag(null, "categoryID");
            addTextWithEncoding(body, getCategory());
            body.endTag(null, "categoryID");
            body.startTag(null, "payee");
            addTextWithEncoding(body, getPayee());
            body.endTag(null, "payee");
            body.startTag(null, "checkNumber");
            addTextWithEncoding(body, getCheckNumber());
            body.endTag(null, "checkNumber");
            body.startTag(null, "classID");
            addTextWithEncoding(body, getClassName());
            body.endTag(null, "classID");
            body.startTag(null, "cleared");
            addText(body, Integer.toString(getCleared()));
            body.endTag(null, "cleared");
            body.startTag(null, "spotlight");
            addTextWithEncoding(body, getSpotlight());
            body.endTag(null, "spotlight");
            body.endTag(null, XML_RECORDTAG_FILTER);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}

package com.example.smmoney.records;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Xml;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.budgets.BudgetsRowAdapter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

public class CategoryClass extends PocketMoneyRecordClass implements Serializable {
    public static final String XML_LISTTAG_CATEGORIES = "CATEGORIES";
    public static final String XML_RECORDTAG_CATEGORY = "CATEGORYCLASS";
    private static String catpayee_statement = null;
    private static int currentViewType;
    private static String query_spent_stmt = null;
    public double budget;
    private double budgetLimit;
    private int budgetPeriod;
    private String category;
    public int categoryID;
    private String currentElementValue;
    private boolean hydratedSpent;
    private boolean includeSubcategories;
    private boolean rollover;
    public double spent;
    private int type;

    private static class BudgetPeriodInfo {
        GregorianCalendar date;
        int daysInPeriod;
        int daysLeft;

        BudgetPeriodInfo(int daysLeft, int daysInPeriod, GregorianCalendar date) {
            this.daysLeft = daysLeft;
            this.daysInPeriod = daysInPeriod;
            this.date = date;
        }
    }

    public CategoryClass() {
        this.budgetPeriod = Enums.kBudgetPeriodMonth /*2*/;
        this.type = Enums.kTransactionTypeWithdrawal /*0*/;
    }

    public CategoryClass(int pk) {
        this.categoryID = pk;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CATEGORIES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"category"}, "categoryID=" + pk, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            String cat = curs.getString(0);
            if (cat != null) {
                this.category = cat;
            } else {
                this.category = "";
            }
        } else {
            this.category = "";
        }
        this.dirty = false;
        curs.close();
    }

    private int getBudgetPeriod() {
        hydrate();
        return this.budgetPeriod;
    }

    public void setBudgetPeriod(int budgetPeriod) {
        if (this.budgetPeriod != budgetPeriod) {
            this.dirty = true;
            this.budgetPeriod = budgetPeriod;
        }
    }

    public static ArrayList<String> periods() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_REPEATING_FREQUENCY_DAILY);
        list.add(Locales.kLOC_REPEATING_FREQUENCY_WEEKLY);
        list.add(Locales.kLOC_REPEATING_FREQUENCY_MONTHLY);
        list.add(Locales.kLOC_REPEATING_FREQUENCY_QUARTERLY);
        list.add(Locales.kLOC_REPEATING_FREQUENCY_YEARLY);
        list.add(Locales.kLOC_BUDGETS_BIWEEKLY);
        list.add(Locales.kLOC_BUDGETS_4WEEKS);
        list.add(Locales.kLOC_BUDGETS_BIMONTHLY);
        list.add(Locales.kLOC_BUDGETS_HALFYEAR);
        return list;
    }

    public String periodAsString() {
        switch (getBudgetPeriod()) {
            case Enums.kBudgetPeriodDay /*0*/:
                return Locales.kLOC_REPEATING_FREQUENCY_DAILY;
            case Enums.kBudgetPeriodWeek /*1*/:
                return Locales.kLOC_REPEATING_FREQUENCY_WEEKLY;
            case Enums.kBudgetPeriodMonth /*2*/:
                return Locales.kLOC_REPEATING_FREQUENCY_MONTHLY;
            case Enums.kBudgetPeriodQuarter /*3*/:
                return Locales.kLOC_REPEATING_FREQUENCY_QUARTERLY;
            case Enums.kBudgetPeriodYear /*4*/:
                return Locales.kLOC_REPEATING_FREQUENCY_YEARLY;
            case Enums.kBudgetPeriodBiweekly /*5*/:
                return Locales.kLOC_BUDGETS_BIWEEKLY;
            case Enums.kBudgetPeriodBimonthly /*6*/:
                return Locales.kLOC_BUDGETS_BIMONTHLY;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                return Locales.kLOC_BUDGETS_HALFYEAR;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                return Locales.kLOC_BUDGETS_4WEEKS;
            default:
                return "";
        }
    }

    public void setPeriodFromString(String period) {
        ArrayList<String> periods = new ArrayList<>();
        periods.add(Locales.kLOC_REPEATING_FREQUENCY_DAILY);
        periods.add(Locales.kLOC_REPEATING_FREQUENCY_WEEKLY);
        periods.add(Locales.kLOC_REPEATING_FREQUENCY_MONTHLY);
        periods.add(Locales.kLOC_REPEATING_FREQUENCY_QUARTERLY);
        periods.add(Locales.kLOC_REPEATING_FREQUENCY_YEARLY);
        periods.add(Locales.kLOC_BUDGETS_BIWEEKLY);
        periods.add(Locales.kLOC_BUDGETS_BIMONTHLY);
        periods.add(Locales.kLOC_BUDGETS_HALFYEAR);
        periods.add(Locales.kLOC_BUDGETS_4WEEKS);
        for (int i = 0; i < periods.size(); i++) {
            if (periods.get(i).equalsIgnoreCase(period)) {
                setBudgetPeriod(i);
                return;
            }
        }
    }

    public double getBudgetLimit() {
        hydrate();
        return this.budgetLimit;
    }

    public void setBudgetLimit(double budgetLimit) {
        if (this.budgetLimit != budgetLimit) {
            this.dirty = true;
            this.budgetLimit = budgetLimit;
        }
    }

    public static ArrayList<String> budgetTypes() {
        ArrayList<String> list = new ArrayList<>();
        list.add(Locales.kLOC_BUDGETS_EXPENSES);
        list.add(Locales.kLOC_BUDGETS_INCOME);
        return list;
    }

    public void setIncludeSubcategories(boolean deleteIt) {
        if (this.includeSubcategories != deleteIt) {
            this.dirty = true;
            this.includeSubcategories = deleteIt;
        }
    }

    public boolean getIncludeSubcategories() {
        hydrate();
        return this.includeSubcategories;
    }

    public void setRollover(boolean theRollover) {
        if (this.rollover != theRollover) {
            this.dirty = true;
            this.rollover = theRollover;
        }
    }

    public boolean getRollover() {
        hydrate();
        return this.rollover;
    }

    public void setCategory(String aString) {
        if (this.category != null || aString != null) {
            if (this.category == null || !this.category.equals(aString)) {
                this.dirty = true;
                this.category = aString;
            }
        }
    }

    public String getCategory() {
        hydrate();
        return this.category;
    }

    public void setType(int atype) {
        if (this.type != atype) {
            this.dirty = true;
            this.type = atype;
        }
    }

    public int getType() {
        hydrate();
        return this.type;
    }

    public String typeAsString() {
        switch (getType()) {
            case Enums.kCategoryExpense /*0*/:
                return Locales.kLOC_BUDGETS_EXPENSES;
            case Enums.kCategoryIncome /*1*/:
                return Locales.kLOC_BUDGETS_INCOME;
            default:
                return "";
        }
    }

    public void setTypeFromString(String aType) {
        ArrayList<String> list = budgetTypes();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(aType)) {
                setType(i);
            }
        }
    }

    public static int insertIntoDatabase(String cat) {
        if (cat == null || cat.length() == 0) {
            return 0;
        }
        ContentValues content = new ContentValues();
        content.put("timestamp", System.currentTimeMillis() / 1000);
        content.put("category", cat);
        content.put("type", Enums.kTransactionTypeWithdrawal /*0*/);
        content.put(Prefs.DISPLAY_BUDGETPERIOD,Enums.kBudgetPeriodMonth /*2*/);
        content.put("serverID", Database.newServerID());
        long id = Database.replace(Database.CATEGORIES_TABLE_NAME, null, content);
        if (id != -1) {
            return (int) id;
        }
        return 0;
    }

    public static int idForCategoryElseAddIfMissing(String cat, boolean addIt) {
        int id = idForCategory(cat);
        if (id == 0 && addIt) {
            return insertIntoDatabase(cat);
        }
        return id;
    }

    public static int idForCategory(String cat) {
        if (cat == null || cat.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CATEGORIES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"categoryID"}, "deleted=0 AND category LIKE " + Database.SQLFormat(cat), null, null, null, null);
        int categoryID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            categoryID = curs.getInt(0);
        }
        curs.close();
        return categoryID;
    }

    private static int idForCategoryIncludeDeleted(String cat) {
        if (cat == null || cat.length() == 0) {
            return 0;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CATEGORIES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"categoryID"}, "category LIKE " + Database.SQLFormat(cat), null, null, null, null);
        int categoryID = 0;
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            categoryID = curs.getInt(0);
        }
        curs.close();
        return categoryID;
    }

    public static CategoryClass recordWithServerID(String serverID) {
        CategoryClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT categoryID FROM categories WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new CategoryClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public static String categoryForID(int pk) {
        if (pk == 0) {
            return null;
        }
        return new CategoryClass(pk).category;
    }

    private static CategoryClass categoryClassForID(int pk) {
        if (pk == 0) {
            return null;
        }
        return new CategoryClass(pk);
    }

    public static ArrayList<String> allCategoryNamesInDatabase() {
        ArrayList<String> array = new ArrayList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CATEGORIES_TABLE_NAME);
        String[] projection = new String[]{"category"};
        qb.setDistinct(true);
        Cursor curs = Database.query(qb, projection, "deleted=0", null, null, null, "UPPER(category)");
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

    public static ArrayList<String> allCategoryNamesInDatabaseForPayee(String payee) {
        ArrayList<String> array = new ArrayList<>();
        if (catpayee_statement == null) {
            catpayee_statement = "SELECT DISTINCT s.categoryID FROM splits s INNER JOIN transactions t WHERE s.transactionID = t.transactionID AND t.deleted = 0 AND t.payee LIKE ? ORDER BY UPPER(s.categoryID)";
        }
        Cursor c = Database.rawQuery(catpayee_statement, new String[]{payee});
        while (c.moveToNext()) {
            array.add(c.getString(0));
        }
        c.close();
        return array;
    }

    public static ArrayList<CategoryClass> allCategoriesInDatabase() {
        ArrayList<CategoryClass> array = new ArrayList<>();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.CATEGORIES_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"categoryID"}, null, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            do {
                array.add(categoryClassForID(curs.getInt(0)));
            } while (curs.moveToNext());
            curs.close();
        }
        return array;
    }

    public static void renameFromToInDatabase(String fromText, String toText, boolean changeInTransactions) {
        if (fromText == null) {
            fromText = "";
        }
        if (toText == null) {
            toText = "";
        }
        for (CategoryClass tempRecord : allCategoriesInDatabase()) {
            if (fromText.length() <= tempRecord.getCategory().length() && (tempRecord.getCategory() + ":").substring(0, fromText.length()).equalsIgnoreCase(fromText)) {
                String toTextTemp = toText + tempRecord.getCategory().substring(fromText.length());
                int toCategoryID = idForCategoryIncludeDeleted(toText);
                int fromCategoryID = idForCategoryIncludeDeleted(tempRecord.getCategory());
                if (toCategoryID != 0) {
                    if (!tempRecord.getCategory().equalsIgnoreCase(toTextTemp)) {
                        new CategoryClass(fromCategoryID).deleteFromDatabase();
                    }
                    CategoryClass toCategoryRecord = new CategoryClass(toCategoryID);
                    if (changeInTransactions) {
                        TransactionClass.renameCategoryFromTo(tempRecord.getCategory(), toTextTemp);
                    }
                    toCategoryRecord.hydrate();
                    CategoryBudgetClass.renameBudgetItems(toCategoryRecord.getCategory(), toTextTemp);
                    toCategoryRecord.setCategory(toTextTemp);
                    toCategoryRecord.saveToDatabase();
                } else {
                    CategoryClass fromCategoryRecord = new CategoryClass(fromCategoryID);
                    if (changeInTransactions) {
                        TransactionClass.renameCategoryFromTo(fromCategoryRecord.getCategory(), toTextTemp);
                    }
                    fromCategoryRecord.hydrate();
                    CategoryBudgetClass.renameBudgetItems(fromCategoryRecord.getCategory(), toTextTemp);
                    fromCategoryRecord.setCategory(toTextTemp);
                    fromCategoryRecord.saveToDatabase();
                }
            }
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
        content.put("category", toText);
        content.put("timestamp", System.currentTimeMillis() / 1000);
        try {
            Database.update(Database.CATEGORIES_TABLE_NAME, content, "category LIKE " + Database.SQLFormat(fromText), null);
        } catch (Exception e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
        }
    }

    public static double querySpentInCategory(String category, boolean includeSubcategories, GregorianCalendar startDate, GregorianCalendar endDate) {
        currentViewType = Enums.kViewAccountsAll /*0*/;
        String startTime = Long.toString(CalExt.beginningOfDay(startDate).getTimeInMillis() / 1000);
        String endTime = Long.toString(CalExt.endOfDay(endDate).getTimeInMillis() / 1000);
        String str = includeSubcategories ? "%" : "";
        String plainCategoryString = category;
        String[] bindArgs = new String[]{category + str, startTime, endTime};
        if (query_spent_stmt == null || ((Prefs.getBooleanPref(Prefs.BUDGETSHOWALLACCOUNTS) && currentViewType != 0) || currentViewType != Prefs.getIntPref(Prefs.VIEWACCOUNTS))) {
            String exchangeRateLookup = "";
            if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
                exchangeRateLookup = " / (SELECT CASE WHEN exchangeRate >0 THEN exchangeRate ELSE 1.0 END FROM accounts WHERE accountID = (SELECT accountID FROM transactions WHERE transactionID = splits.transactionID))";
            }
            String transactionsLookup = "";
            currentViewType = Prefs.getIntPref(Prefs.VIEWACCOUNTS);
            if (Prefs.getBooleanPref(Prefs.BUDGETSHOWALLACCOUNTS)) {
                currentViewType = Enums.kViewAccountsAll /*0*/;
            }
            switch (currentViewType) {
                case Enums.kViewAccountsNonZero /*1*/:
                    transactionsLookup = "(SELECT transactionID FROM transactions WHERE deleted=0 AND date >= ? AND date <= ? AND type <> 5 AND transactions.accountID IN (SELECT accountID FROM transactions WHERE deleted=0 AND type<>5 GROUP BY accountID HAVING (sum(subTotal) < -0.005) OR (sum(subTotal) > 0.005)))";
                    break;
                case Enums.kViewAccountsTotalWorth /*2*/:
                    transactionsLookup = "(SELECT transactionID FROM transactions WHERE deleted=0 AND date >= ? AND date <= ? AND type <> 5 AND transactions.accountID IN (SELECT accountID FROM accounts WHERE deleted=0 AND totalWorth=1))";
                    break;
                default:
                    transactionsLookup = "(SELECT transactionID FROM transactions WHERE deleted=0 AND date >= ? AND date <= ? AND type <> 5)";
                    break;
            }
            String expenseTypeLookup = "(0 = (SELECT type FROM categories WHERE category LIKE ? ESCAPE '\\'))";
            String transfersString = "(1)";
            if (Prefs.getBooleanPref(Prefs.BUDGETINCLUDETRANSFERS)) {
                transfersString = "(transferToAccountID = 0 OR (((amount < 0) AND (" + expenseTypeLookup + ")) OR ((amount > 0) AND NOT (" + expenseTypeLookup + "))))";
            }
            query_spent_stmt = "SELECT sum(amount " + exchangeRateLookup + ") FROM splits WHERE categoryID LIKE ? ESCAPE '\\' AND (splits.transactionID IN " + transactionsLookup + ") AND " + transfersString;
        }
        Cursor curs = Database.rawQuery(query_spent_stmt, bindArgs);
        if (curs.moveToFirst()) {
            double spentTemp = curs.getDouble(0);
            curs.close();
            return spentTemp;
        }
        curs.close();
        return 0.0d;
    }

    private double budgetLimitDailyForDate(GregorianCalendar aDate) {
        switch (this.budgetPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                return this.budgetLimit;
            case Enums.kBudgetPeriodWeek /*1*/:
                return this.budgetLimit / 7.0d;
            case Enums.kBudgetPeriodMonth /*2*/:
                return this.budgetLimit / ((double) aDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            case Enums.kBudgetPeriodQuarter /*3*/:
                return this.budgetLimit / ((double) (aDate.getActualMaximum(Calendar.DAY_OF_YEAR) / 4));
            case Enums.kBudgetPeriodYear /*4*/:
                return this.budgetLimit / ((double) aDate.getActualMaximum(Calendar.DAY_OF_YEAR));
            case Enums.kBudgetPeriodBiweekly /*5*/:
                return this.budgetLimit / 14.0d;
            case Enums.kBudgetPeriodBimonthly /*6*/:
                return this.budgetLimit / ((double) (aDate.getActualMaximum(Calendar.DAY_OF_MONTH) * 2));
            case Enums.kBudgetPeriodHalfYear /*7*/:
                return this.budgetLimit / ((double) (aDate.getActualMaximum(Calendar.DAY_OF_YEAR) / 2));
            case Enums.kBudgetPeriod4Weeks /*8*/:
                return this.budgetLimit / 28.0d;
            default:
                return 0.0d;
        }
    }

    private double budgetLimitMonthlyForDate(GregorianCalendar aDate) {
        switch (this.budgetPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                return this.budgetLimit * ((double) aDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            case Enums.kBudgetPeriodWeek /*1*/:
                return (this.budgetLimit / 7.0d) * ((double) aDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            case Enums.kBudgetPeriodMonth /*2*/:
                return this.budgetLimit;
            case Enums.kBudgetPeriodQuarter /*3*/:
                return this.budgetLimit / 3.0d;
            case Enums.kBudgetPeriodYear /*4*/:
                return this.budgetLimit / 12.0d;
            case Enums.kBudgetPeriodBiweekly /*5*/:
                return (this.budgetLimit / 14.0d) * ((double) aDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            case Enums.kBudgetPeriodBimonthly /*6*/:
                return this.budgetLimit / 2.0d;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                return this.budgetLimit / 6.0d;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                return (this.budgetLimit / 28.0d) * ((double) aDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            default:
                return 0.0d;
        }
    }

    public double budgetLimitForPeriod(int aPeriod, GregorianCalendar aDate) {
        switch (aPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                if (this.budgetPeriod == 0) {
                    return this.budgetLimit;
                }
                return budgetLimitDailyForDate(aDate);
            case Enums.kBudgetPeriodWeek /*1*/:
                if (1 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                return budgetLimitDailyForDate(aDate) * 7.0d;
            case Enums.kBudgetPeriodMonth /*2*/:
                if (2 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                return budgetLimitMonthlyForDate(aDate);
            case Enums.kBudgetPeriodQuarter /*3*/:
                if (3 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                if (isMonthlyBasedBudget(this.budgetPeriod)) {
                    return budgetLimitMonthlyForDate(aDate) * 3.0d;
                }
                return budgetLimitDailyForDate(aDate) * ((double) (aDate.getActualMaximum(Calendar.DAY_OF_YEAR) / 4));
            case Enums.kBudgetPeriodYear /*4*/:
                if (4 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                if (isMonthlyBasedBudget(this.budgetPeriod)) {
                    return budgetLimitMonthlyForDate(aDate) * 12.0d;
                }
                return budgetLimitDailyForDate(aDate) * ((double) aDate.getActualMaximum(Calendar.DAY_OF_YEAR));
            case Enums.kBudgetPeriodBiweekly /*5*/:
                if (5 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                return budgetLimitDailyForDate(aDate) * 14.0d;
            case Enums.kBudgetPeriodBimonthly /*6*/:
                if (6 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                if (isMonthlyBasedBudget(this.budgetPeriod)) {
                    return budgetLimitMonthlyForDate(aDate) * 2.0d;
                }
                return (budgetLimitDailyForDate(aDate) * ((double) aDate.getActualMaximum(Calendar.DAY_OF_MONTH))) * 2.0d;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                if (7 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                if (isMonthlyBasedBudget(this.budgetPeriod)) {
                    return budgetLimitMonthlyForDate(aDate) * 6.0d;
                }
                return budgetLimitDailyForDate(aDate) * ((double) (aDate.getActualMaximum(Calendar.DAY_OF_YEAR) / 2));
            case Enums.kBudgetPeriod4Weeks /*8*/:
                if (8 == this.budgetPeriod) {
                    return this.budgetLimit;
                }
                return budgetLimitDailyForDate(aDate) * 28.0d;
            default:
                return 0.0d;
        }
    }

    private BudgetPeriodInfo endOfBudgetPeriod(int aPeriod, GregorianCalendar atDate) {
        GregorianCalendar endDate = null;
        GregorianCalendar startDate = null;
        switch (aPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                startDate = CalExt.beginningOfDay(atDate);
                endDate = CalExt.endOfDay(atDate);
                break;
            case Enums.kBudgetPeriodWeek /*1*/:
                startDate = CalExt.beginningOfWeek(atDate);
                endDate = CalExt.endOfWeek(atDate);
                break;
            case Enums.kBudgetPeriodMonth /*2*/:
                startDate = CalExt.beginningOfMonth(atDate);
                endDate = CalExt.endOfMonth(atDate);
                break;
            case Enums.kBudgetPeriodQuarter /*3*/:
                startDate = CalExt.beginningOfQuarter(atDate);
                endDate = CalExt.endOfQuarter(atDate);
                break;
            case Enums.kBudgetPeriodYear /*4*/:
                startDate = CalExt.beginningOfYear(atDate);
                endDate = CalExt.endOfYear(atDate);
                break;
            case Enums.kBudgetPeriodBiweekly /*5*/:
                startDate = CalExt.beginningOfWeek(atDate);
                endDate = CalExt.addWeeks(CalExt.endOfWeek(atDate), 1);
                break;
            case Enums.kBudgetPeriodBimonthly /*6*/:
                startDate = CalExt.beginningOfMonth(atDate);
                endDate = CalExt.endOfMonth(CalExt.addMonth(CalExt.beginningOfMonth(atDate)));
                break;
            case Enums.kBudgetPeriodHalfYear /*7*/:
                if (!atDate.before(CalExt.middleOfYear())) {
                    startDate = CalExt.endOfMonth(CalExt.addMonths(CalExt.beginningOfYear(atDate), 5));
                    endDate = CalExt.endOfYear(atDate);
                    break;
                }
                startDate = CalExt.beginningOfYear(atDate);
                endDate = CalExt.endOfMonth(CalExt.addMonths(CalExt.beginningOfYear(atDate), 5));
                break;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                startDate = CalExt.beginningOfWeek(atDate);
                endDate = CalExt.addWeeks(CalExt.endOfWeek(atDate), 3);
                break;
        }
        return new BudgetPeriodInfo(CalExt.daysBetween(CalExt.subtractSecond(atDate), endDate), CalExt.daysBetween(CalExt.subtractSecond(startDate), endDate), endDate);
    }

    private double budgetLimitWithoutRollover(GregorianCalendar startDate, GregorianCalendar endDate) {
        double daysInPeriod;
        BudgetPeriodInfo periodInfo;
        GregorianCalendar nextDate;
        double newBudgetLimit = 0.0d;
        GregorianCalendar currentDate = CalExt.beginningOfDay((GregorianCalendar) startDate.clone());
        double limit = CategoryBudgetClass.limitPrior(startDate, this.budgetLimit, this.category);
        for (CategoryBudgetClass budgetItem : CategoryBudgetClass.budgetItems(this.category, startDate, endDate)) {
            double days;
            if (isNonVariableBudgetPeriod(this.budgetPeriod)) {
                daysInPeriod = daysInNonvariableBudgetPeriod(this.budgetPeriod);
            } else {
                periodInfo = endOfBudgetPeriod(this.budgetPeriod, currentDate);
                days = periodInfo.daysLeft;
                daysInPeriod = periodInfo.daysInPeriod;
                nextDate = periodInfo.date;
                while (nextDate.before(CalExt.beginningOfDay(budgetItem.getDate()))) {
                    newBudgetLimit += (limit / daysInPeriod) * days;
                    currentDate = CalExt.addSecond((GregorianCalendar) nextDate.clone());
                    periodInfo = endOfBudgetPeriod(this.budgetPeriod, currentDate);
                    days = periodInfo.daysLeft;
                    daysInPeriod = periodInfo.daysInPeriod;
                    nextDate = periodInfo.date;
                }
            }
            newBudgetLimit += (limit / daysInPeriod) * ((double) CalExt.daysBetween(currentDate, CalExt.beginningOfDay(budgetItem.getDate())));
            currentDate = CalExt.beginningOfDay((GregorianCalendar) budgetItem.getDate().clone());
            limit = budgetItem.getBudgetLimit();
        }
        if (isNonVariableBudgetPeriod(this.budgetPeriod)) {
            daysInPeriod = daysInNonvariableBudgetPeriod(this.budgetPeriod);
        } else {
            periodInfo = endOfBudgetPeriod(this.budgetPeriod, currentDate);
            double days = periodInfo.daysLeft;
            daysInPeriod = periodInfo.daysInPeriod;
            nextDate = periodInfo.date;
            while (nextDate.before(CalExt.beginningOfDay(endDate))) {
                newBudgetLimit += (limit / daysInPeriod) * days;
                currentDate = CalExt.addSecond((GregorianCalendar) nextDate.clone());
                periodInfo = endOfBudgetPeriod(this.budgetPeriod, currentDate);
                days = periodInfo.daysLeft;
                daysInPeriod = periodInfo.daysInPeriod;
                nextDate = periodInfo.date;
            }
        }
        return newBudgetLimit + ((limit / daysInPeriod) * ((double) CalExt.daysBetween(CalExt.subtractSecond(CalExt.beginningOfDay(currentDate)), CalExt.endOfDay(endDate))));
    }

    public double budgetLimit(GregorianCalendar startDate, GregorianCalendar endDate) {
        if (!this.rollover) {
            return budgetLimitWithoutRollover(startDate, endDate);
        }
        GregorianCalendar rolloverStartDate = CategoryBudgetClass.firstRolloverDatePriorTo(CalExt.endOfDay(startDate), this.category);
        if (rolloverStartDate == null) {
            rolloverStartDate = CategoryBudgetClass.firstDateOfTransactionPriorTo(startDate, this.category);
            if (rolloverStartDate == null) {
                rolloverStartDate = startDate;
            }
            rolloverStartDate = BudgetsRowAdapter.startOfPeriod(rolloverStartDate, this.budgetPeriod);
        } else {
            rolloverStartDate = CalExt.beginningOfDay(rolloverStartDate);
        }
        List<CategoryBudgetClass> budgetItems = CategoryBudgetClass.budgetItems(this.category, CalExt.endOfDay(startDate), CalExt.endOfDay(endDate));
        double spentPrior = querySpentInCategory(this.category, this.includeSubcategories, rolloverStartDate, startDate);
        double newBudgetLimit = 0.0d;
        GregorianCalendar currentStartDate = CalExt.subtractSecond((GregorianCalendar) rolloverStartDate.clone());
        for (CategoryBudgetClass budgetItem : budgetItems) {
            if (currentStartDate == null) {
                currentStartDate = CalExt.subtractSecond(budgetItem.getDate());
            } else {
                newBudgetLimit += budgetLimitWithoutRollover(currentStartDate, CalExt.endOfDay(budgetItem.getDate()));
                currentStartDate = CalExt.subtractSecond(budgetItem.getDate());
            }
        }
        newBudgetLimit += budgetLimitWithoutRollover(CalExt.addSecond(currentStartDate), CalExt.endOfDay(endDate));
        if (this.type == 1) {
            return newBudgetLimit - spentPrior;
        }
        return newBudgetLimit + spentPrior;
    }

    private boolean isNonVariableBudgetPeriod(int aPeriod) {
        return aPeriod == 8 || aPeriod == 5 || aPeriod == 1 || aPeriod == 0;
    }

    private int daysInNonvariableBudgetPeriod(int aPeriod) {
        switch (aPeriod) {
            case Enums.kBudgetPeriodDay /*0*/:
                return 1;
            case Enums.kBudgetPeriodWeek /*1*/:
                return 7;
            case Enums.kBudgetPeriodBiweekly /*5*/:
                return 14;
            case Enums.kBudgetPeriod4Weeks /*8*/:
                return 28;
            default:
                return 0;
        }
    }

    public static List<CategoryClass> queryIncomeCategoriesWithBudgets() {
        ArrayList<CategoryClass> elements = new ArrayList<>();
        Cursor c = Database.rawQuery("SELECT DISTINCT categoryID FROM categories c LEFT JOIN (SELECT * FROM categoryBudgets WHERE deleted=0 or deleted ISNULL) AS b ON c.category LIKE b.categoryName  WHERE c.deleted=0 AND  (c.budgetLimit <> 0 OR b.budgetLimit<>0) AND type = 1 ORDER BY UPPER(c.category)", null);
        int count = c.getCount();
        c.moveToFirst();
        for (int i = 0; i < count; i++) {
            elements.add(new CategoryClass(c.getInt(0)));
            c.moveToNext();
        }
        c.close();
        return elements;
    }

    public static List<CategoryClass> queryExpenseCategoriesWithBudgets() {
        ArrayList<CategoryClass> elements = new ArrayList<>();
        Cursor c = Database.rawQuery("SELECT categoryID FROM categories WHERE deleted=0 AND budgetLimit <> 0  AND type <> 1 ORDER BY UPPER(category)", null);
        int count = c.getCount();
        c.moveToFirst();
        for (int i = 0; i < count; i++) {
            elements.add(new CategoryClass(c.getInt(0)));
            c.moveToNext();
        }
        c.close();
        return elements;
    }

    public static List<CategoryClass> queryNonBudgettedCategories() {
        ArrayList<CategoryClass> elements = new ArrayList<>();
        Cursor c = Database.rawQuery("SELECT c.categoryID FROM categories c WHERE c.deleted=0 AND (type ISNULL OR type=3 OR ( (c.budgetLimit ISNULL OR c.budgetLimit=0) AND UPPER(c.category) NOT IN(SELECT UPPER(b.categoryName) FROM categoryBudgets b WHERE b.deleted=0 AND b.budgetLimit <> 0) ) )", null);
        int count = c.getCount();
        c.moveToFirst();
        for (int i = 0; i < count; i++) {
            elements.add(new CategoryClass(c.getInt(0)));
            c.moveToNext();
        }
        c.close();
        return elements;
    }

    private boolean isMonthlyBasedBudget(int aPeriod) {
        switch (aPeriod) {
            case Enums.kBudgetPeriodMonth /*2*/:
            case Enums.kBudgetPeriodQuarter /*3*/:
            case Enums.kBudgetPeriodYear /*4*/:
            case Enums.kBudgetPeriodBimonthly /*6*/:
            case Enums.kBudgetPeriodHalfYear /*7*/:
                return true;
            default:
                return false;
        }
    }

    public void deleteFromDatabase() {
        ContentValues values = new ContentValues();
        values.put("timestamp", System.currentTimeMillis());
        values.put("deleted", Boolean.TRUE);
        Database.update(Database.CATEGORIES_TABLE_NAME, values, "categoryID=" + this.categoryID, null);
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.CATEGORIES_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"deleted", "timestamp", "category", "type", Prefs.DISPLAY_BUDGETPERIOD, "budgetLimit", "includeSubcategories", "serverID", "rollover"}, "categoryID=" + this.categoryID, null, null, null, null);
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
                setCategory(str);
                col2 = col + 1;
                setType(curs.getInt(col));
                col = col2 + 1;
                this.budgetPeriod = curs.getInt(col2);
                col2 = col + 1;
                this.budgetLimit = curs.getDouble(col);
                col = col2 + 1;
                this.includeSubcategories = curs.getInt(col2) == 1;
                col2 = col + 1;
                str = curs.getString(col);
                if (str == null) {
                    str = "";
                }
                setServerID(str);
                col = col2 + 1;
                this.rollover = curs.getInt(col2) == 1;
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
            content.put("category", this.category);
            content.put("type", this.type);
            content.put(Prefs.DISPLAY_BUDGETPERIOD, this.budgetPeriod);
            content.put("budgetLimit", this.budgetLimit);
            content.put("includeSubcategories", this.includeSubcategories);
            content.put("timestamp", System.currentTimeMillis() / 1000);
            content.put("rollover", this.rollover);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            content.put("serverID", this.serverID);
            Database.update(Database.CATEGORIES_TABLE_NAME, content, "categoryID=" + this.categoryID, null);
            this.dirty = false;
        }
        this.hydrated = false;
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.categoryID == 0) {
                this.categoryID = insertIntoDatabase(this.category);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
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
        boolean z = false;
        if (this.currentElementValue == null) {
            this.currentElementValue = "";
        }
        switch (localName) {
            case "categoryID":
                this.categoryID = Integer.parseInt(this.currentElementValue);
                break;
            case "timestamp":
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
                break;
            case "deleted":
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setDeleted(z);
                break;
            case "rollover":
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setRollover(z);
                break;
            case "type":
                setType(Integer.parseInt(this.currentElementValue));
                break;
            case Prefs.DISPLAY_BUDGETPERIOD:
                setBudgetPeriod(Integer.parseInt(this.currentElementValue));
                break;
            case "budgetLimit":
                setBudgetLimit(Double.parseDouble(this.currentElementValue));
                break;
            case "includeSubcategories":
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setIncludeSubcategories(z);
                break;
            case "serverID":
                setServerID(this.currentElementValue);
                break;
            case "category":
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
            body.startTag(null, XML_RECORDTAG_CATEGORY);
            body.startTag(null, "categoryID");
            addText(body, Integer.toString(this.categoryID));
            body.endTag(null, "categoryID");
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
            body.startTag(null, "category");
            addTextWithEncoding(body, getCategory());
            body.endTag(null, "category");
            body.startTag(null, "type");
            addText(body, Integer.toString(getType()));
            body.endTag(null, "type");
            body.startTag(null, Prefs.DISPLAY_BUDGETPERIOD);
            addText(body, Integer.toString(getBudgetPeriod()));
            body.endTag(null, Prefs.DISPLAY_BUDGETPERIOD);
            body.startTag(null, "budgetLimit");
            addText(body, Double.toString(getBudgetLimit()));
            body.endTag(null, "budgetLimit");
            body.startTag(null, "includeSubcategories");
            addText(body, getIncludeSubcategories() ? "Y" : "N");
            body.endTag(null, "includeSubcategories");
            body.startTag(null, "rollover");
            addText(body, getRollover() ? "Y" : "N");
            body.endTag(null, "rollover");
            body.endTag(null, XML_RECORDTAG_CATEGORY);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}

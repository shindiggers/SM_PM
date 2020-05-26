package com.example.smmoney.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
import com.example.smmoney.records.IDClass;
import com.example.smmoney.records.PayeeClass;
import com.example.smmoney.records.PocketMoneyRecordClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;

import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Database {
    public static final String ACCOUNTS_TABLE_NAME = "accounts";
    public static final String CATEGORIES_TABLE_NAME = "categories";
    public static final String CATEGORYPAYEES_TABLE_NAME = "categorypayee";
    public static final String CLASSES_TABLE_NAME = "classes";
    public static final String DATABASESYNCLIST_TABLE_NAME = "databaseSyncList";
    private static final String DATABASE_NAME = "SMMoneyDB.sql";
    private static final int DATABASE_VERSION_1 = 1;
    private static final int DATABASE_VERSION_10 = 10;
    private static final int DATABASE_VERSION_11 = 11;
    private static final int DATABASE_VERSION_12 = 12;
    private static final int DATABASE_VERSION_13 = 13;
    private static final int DATABASE_VERSION_14 = 14;
    private static final int DATABASE_VERSION_15 = 15;
    private static final int DATABASE_VERSION_16 = 16;
    private static final int DATABASE_VERSION_17 = 17;
    private static final int DATABASE_VERSION_18 = 18;
    private static final int DATABASE_VERSION_19 = 19;
    private static final int DATABASE_VERSION_2 = 2;
    private static final int DATABASE_VERSION_20 = 20;
    private static final int DATABASE_VERSION_21 = 21;
    private static final int DATABASE_VERSION_22 = 22;
    private static final int DATABASE_VERSION_23 = 23;
    private static final int DATABASE_VERSION_24 = 24;
    private static final int DATABASE_VERSION_25 = 25;
    private static final int DATABASE_VERSION_26 = 26;
    private static final int DATABASE_VERSION_27 = 27;
    private static final int DATABASE_VERSION_28 = 28;
    private static final int DATABASE_VERSION_29 = 29;
    private static final int DATABASE_VERSION_3 = 3;
    private static final int DATABASE_VERSION_30 = 30;
    private static final int DATABASE_VERSION_31 = 31;
    private static final int DATABASE_VERSION_32 = 32;
    private static final int DATABASE_VERSION_33 = 33;
    public static final int DATABASE_VERSION_34 = 34;
    private static final int DATABASE_VERSION_4 = 4;
    private static final int DATABASE_VERSION_5 = 5;
    private static final int DATABASE_VERSION_6 = 6;
    private static final int DATABASE_VERSION_7 = 7;
    private static final int DATABASE_VERSION_8 = 8;
    private static final int DATABASE_VERSION_9 = 9;
    private static final int DATABASE_VERSION_CURRENT = 34;
    public static final String EXCHANGERATES_TABLE_NAME = "exchangeRates";
    public static final String FILTERS_TABLE_NAME = "filters";
    public static final String IDS_TABLE_NAME = "ids";
    public static final String PAYEES_TABLE_NAME = "payees";
    private static final int PMSYNC_VERSION_1 = 1;
    public static final int PMSYNC_VERSION_2 = 2;
    public static final int PMSYNC_VERSION_CURRENT = 2;
    public static final String PREFS_TABLE_NAME = "preferences";
    public static final String REPEATINGTRANSACTIONS_TABLE_NAME = "repeatingTransactions";
    public static final String SPLITS_TABLE_NAME = "splits";
    public static final String TRANSACTIONS_TABLE_NAME = "transactions";
    public static int databaseID;
    private static int databaseVersion;
    private static SQLiteDatabase db = null;
    private static final Object dbLock = new Object();
    private static DatabaseHelper dbh = null;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION_CURRENT);
            SQLiteDatabase writableDatabase = getWritableDatabase();
            writableDatabase = getReadableDatabase();
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE preferences (databaseVersion\t\tINTEGER,databaseID\t\t\tINTEGER,multipleCurrencies   BOOLEAN,nextServerID         INTEGER);");
                db.execSQL("INSERT INTO preferences (databaseVersion, databaseID) VALUES (32, random());");
                db.execSQL("CREATE TABLE accounts (deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,accountID\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,displayOrder\t\t\tINTEGER,account\t\t\t\tTEXT,balanceOverall\t\tREAL,balanceCleared\t\tREAL,type\t\t\t\t\tINTEGER,accountNumber\t\tTEXT,institution\t\t\tTEXT,phone\t\t\t\tTEXT,expirationDate\t\tTEXT,checkNumber\t\t\tTEXT,notes\t\t\t\tTEXT,iconFileName\t\t\tTEXT,url\t\t\t\t\tTEXT,ofxid\t\t\t\tTEXT,ofxurl\t\t\t\tTEXT,password\t\t\t\tTEXT,fee\t\t\t\t\tREAL,fixedPercent\t\t\tINTEGER,limitAmount\t\t\tREAL,noLimit\t\t\t\tINTEGER,totalWorth\t\t\tINTEGER,exchangeRate\t\t\tREAL,currencyCode\t\t\tTEXT,lastSyncTime\t\t\tINTEGER DEFAULT 0,keepTheChangeAccountID\tINTEGER,keepChangeRoundTo\t\tREAL,serverID\t\t\t\tTEXT,routingNumber\t\tTEXT,overdraftAccountID\tINTEGER DEFAULT 0);");
                db.execSQL("CREATE INDEX accountNames ON accounts (account);");
                db.execSQL("CREATE INDEX typeaccount ON accounts (type, account);");
                db.execSQL("CREATE INDEX accountDisplayOrder ON accounts (displayOrder);");
                db.execSQL("CREATE INDEX accountServerIDs ON accounts (serverID);");
                db.execSQL("CREATE TABLE transactions (transactionID\t\tINTEGER PRIMARY KEY AUTOINCREMENT,deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,type\t\t\t\t\tINTEGER,date\t\t\t\t\tINTEGER,cleared\t\t\t\tBOOLEAN,accountID\t\t\tINTEGER,payee\t\t\t\tTEXT,checkNumber\t\t\tTEXT,subTotal\t\t\t\tREAL,ofxID\t\t\t\tTEXT,serverID\t\t\t\tTEXT,overdraftID\t\t\tTEXT,image\t\t\t\tBLOB);");
                db.execSQL("CREATE INDEX transactionIDs ON transactions (transactionID);");
                db.execSQL("CREATE INDEX transactionsByDate ON transactions (date);");
                db.execSQL("CREATE INDEX transactionsByAccount ON transactions (accountID, date);");
                db.execSQL("CREATE INDEX transactionPayees ON transactions (payee);");
                db.execSQL("CREATE INDEX transactionTypes ON transactions (type);");
                db.execSQL("CREATE INDEX transactionOFXIDs ON transactions (ofxID);");
                db.execSQL("CREATE INDEX transactionServerIDs ON transactions (serverID);");
                db.execSQL("CREATE TABLE splits (splitID\t\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,transactionID\t\tINTEGER,amount\t\t\t\tREAL,xrate\t\t\t\tREAL,categoryID\t\t\tTEXT,classID\t\t\t\tTEXT,memo\t\t\t\t\tTEXT,transferToAccountID\tINTEGER,currencyCode\t\t\tTEXT,ofxid\t\t\t\tTEXT);");
                db.execSQL("CREATE INDEX transactionSplitIDs ON splits (transactionID);");
                db.execSQL("CREATE INDEX splitCategories ON splits (categoryID);");
                db.execSQL("CREATE TABLE filters (filterID\t\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,filterName\t\t\tTEXT,type\t\t\t\t\tINTEGER,dateFrom\t\t\t\tINTEGER,dateTo\t\t\t\tINTEGER,accountID\t\t\tINTEGER,categoryID\t\t\tTEXT,payee\t\t\t\tTEXT,classID\t\t\t\tTEXT,checkNumber\t\t\tTEXT,cleared\t\t\t\tINTEGER,serverID\t\t\t\tTEXT,selectedFilterName   TEXT,spotlight\t\t\tTEXT);");
                db.execSQL("CREATE INDEX filterIDs ON filters (filterID);");
                db.execSQL("CREATE INDEX filterServerIDs ON filters (serverID);");
                db.execSQL("CREATE TABLE categories (deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,categoryID\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,category\t\t\t\tTEXT UNIQUE,type\t\t\t\t\tINTEGER,budgetPeriod\t\t\tINTEGER,budgetLimit\t\t\tREAL,serverID\t\t\t\tTEXT,includeSubcategories\tBOOLEAN DEFAULT 0,rollover             BOOLEAN DEFAULT 0);");
                db.execSQL("CREATE INDEX categoryIDs ON categories (categoryID);");
                db.execSQL("CREATE INDEX categoryName ON categories (category);");
                db.execSQL("CREATE INDEX categoryServerIDs ON categories (serverID);");
                db.execSQL("CREATE TABLE payees (deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,serverID\t\t\t\tTEXT,payeeID\t\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,payee\t\t\t\tTEXT UNIQUE,latitude\t\t\t\tREAL,longitude\t\t\tREAL);");
                db.execSQL("CREATE INDEX payeeIDs ON payees (payeeID);");
                db.execSQL("CREATE INDEX payeeName ON payees (payee);");
                db.execSQL("CREATE INDEX payeeServerIDs ON payees (serverID);");
                db.execSQL("CREATE TABLE categorypayee (categoryID\t\t\tINTEGER,payeeID              INTEGER,deleted              BOOLEAN,serverID\t\t\t\tTEXT,PRIMARY KEY (categoryID, payeeID));");
                db.execSQL("CREATE INDEX cpCategoryIDs ON categorypayee (categoryID);");
                db.execSQL("CREATE INDEX cpPayeeIDs ON categorypayee (payeeID);");
                db.execSQL("CREATE TABLE classes (deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,serverID\t\t\t\tTEXT,classID\t\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,class\t\t\t\tTEXT UNIQUE);");
                db.execSQL("CREATE INDEX classIDs ON classes (classID);");
                db.execSQL("CREATE INDEX className ON classes (class);");
                db.execSQL("CREATE INDEX classServerIDs ON classes (serverID);");
                db.execSQL("CREATE TABLE ids (deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,serverID\t\t\t\tTEXT,idID\t\t\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,id\t\t\t\t\tTEXT UNIQUE);");
                db.execSQL("CREATE INDEX idIDs ON ids (idID);");
                db.execSQL("CREATE INDEX idName ON ids (id);");
                db.execSQL("CREATE INDEX idServerIDs ON ids (serverID);");
                db.execSQL("CREATE TABLE exchangeRates (deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,currencyCode\t\t\tTEXT PRIMARY KEY,exchangeRate\t\t\tREAL);");
                db.execSQL("CREATE INDEX currencyCodes ON exchangeRates (currencyCode);");
                db.execSQL("CREATE TABLE repeatingTransactions (repeatingID\t\t\tINTEGER PRIMARY KEY AUTOINCREMENT,deleted\t\t\t\tBOOLEAN DEFAULT 0,timestamp\t\t\tINTEGER,serverID\t\t\t\tTEXT,lastProcessedDate\tINTEGER,transactionID\t\tINTEGER,type\t\t\t\t\tINTEGER,endDate\t\t\t\tINTEGER,frequency\t\t\tINTEGER,repeatOn\t\t\t\tINTEGER,startOfWeek\t\t\tINTEGER,sendLocalNotifications INTEGER,notifyDaysInAdvance  INTEGER);");
                db.execSQL("CREATE INDEX repeatingIDs ON repeatingTransactions (repeatingID);");
                db.execSQL("CREATE INDEX repeatingTransactionServerIDs ON repeatingTransactions (serverID);");
                db.execSQL("CREATE TABLE categoryBudgets ('categoryBudgetID' \tINTEGER PRIMARY KEY AUTOINCREMENT,'deleted' \t\t\t\tBOOLEAN DEFAULT 0, 'serverID' \t\t\tTEXT, 'timestamp' \t\t\tINTEGER, 'categoryName' \t\tTEXT, 'date' \t\t\t\tINTEGER, 'budgetLimit' \t\t\tREAL, 'resetRollover' \t\tBOOLEAN DEFAULT 0)");
                db.execSQL("CREATE INDEX categorgyBudgetsServerIDs ON categoryBudgets (serverID);");
                db.execSQL("CREATE TABLE databaseSyncList (databaseID\t\t\tTEXT PRIMARY KEY,lastSyncTime\t\t\tINTEGER);");
            } catch (SQLiteException e) {
                Log.e(SMMoney.TAG, "...");
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

    private static void execSqlWithCatch(String statement) {
        try {
            SQLiteDatabase db = currentDB();
            synchronized (dbLock) {
                db.execSQL(statement);
            }
        } catch (SQLiteException e) {
            Log.i(SMMoney.TAG, e.getLocalizedMessage());
        }
    }

    private static void updateDatabase() {
        if (databaseVersion == DATABASE_VERSION_1) {
            execSqlWithCatch("UPDATE splits SET categoryID = (SELECT categories.category FROM categories WHERE categories.categoryID=splits.categoryID)");
            execSqlWithCatch("UPDATE splits SET classID = (SELECT classes.class FROM classes WHERE classes.classID=splits.classID)");
            databaseVersion = DATABASE_VERSION_2;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_2) {
            execSqlWithCatch("CREATE TABLE repeatingTransactions ('repeatingID' INTEGER PRIMARY KEY AUTOINCREMENT,\t'deleted' BOOLEAN DEFAULT 0, 'timestamp' INTEGER,'lastProcessedDate' INTEGER,'transactionID' INTEGER, 'type' INTEGER, 'endDate' INTEGER,'frequency' INTEGER,'repeatOn' INTEGER,'startOfWeek' INTEGER)");
            databaseVersion = DATABASE_VERSION_3;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_3) {
            execSqlWithCatch("CREATE INDEX splitCategories ON splits (categoryID);");
            databaseVersion = DATABASE_VERSION_4;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_4) {
            execSqlWithCatch("DELETE FROM repeatingtransactions WHERE NOT EXISTS  (SELECT transactionid FROM transactions WHERE repeatingtransactions.transactionid = transactions.transactionid);");
            databaseVersion = DATABASE_VERSION_5;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_5) {
            execSqlWithCatch("ALTER TABLE categories ADD includeSubcategories BOOLEAN DEFAULT 0");
            databaseVersion = DATABASE_VERSION_6;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_6) {
            execSqlWithCatch("ALTER TABLE accounts ADD lastSyncTime INTEGER DEFAULT 0");
            databaseVersion = DATABASE_VERSION_7;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_7) {
            execSqlWithCatch("UPDATE accounts SET displayOrder = 0");
            databaseVersion = DATABASE_VERSION_8;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_8) {
            execSqlWithCatch("UPDATE transactions SET payee = '' WHERE payee ISNULL");
            databaseVersion = DATABASE_VERSION_9;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_9) {
            execSqlWithCatch("DELETE FROM filters");
            databaseVersion = DATABASE_VERSION_10;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_10) {
            execSqlWithCatch("CREATE INDEX transactionPayees ON transactions (payee)");
            execSqlWithCatch("CREATE INDEX transactionTypes ON transactions (type)");
            databaseVersion = DATABASE_VERSION_11;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_11) {
            execSqlWithCatch("DROP INDEX transactionSplitIDs");
            execSqlWithCatch("CREATE INDEX transactionSplitIDs ON splits (transactionID)");
            databaseVersion = DATABASE_VERSION_12;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_12) {
            execSqlWithCatch("DELETE FROM categories WHERE category ISNULL");
            databaseVersion = DATABASE_VERSION_13;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_13) {
            execSqlWithCatch("ALTER TABLE accounts ADD routingNumber TEXT");
            databaseVersion = DATABASE_VERSION_14;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_14) {
            execSqlWithCatch("ALTER TABLE transactions ADD ofxID TEXT");
            execSqlWithCatch("CREATE INDEX transactionOFXIDs ON transactions (ofxID)");
            databaseVersion = DATABASE_VERSION_15;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_15) {
            execSqlWithCatch("UPDATE transactions SET date=date+63114145200 WHERE date < -20000000000");
            databaseVersion = DATABASE_VERSION_16;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_16) {
            execSqlWithCatch("ALTER TABLE accounts ADD overdraftAccountID INTEGER");
            databaseVersion = DATABASE_VERSION_17;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_17) {
            execSqlWithCatch("ALTER TABLE filters ADD selectedFilterName TEXT");
            execSqlWithCatch("UPDATE filters SET selectedFilterName = (SELECT f.filtername FROM filters AS f WHERE f.filterid = filters.filterid)");
            execSqlWithCatch("DELETE FROM filters WHERE filterID IN (SELECT bad_rows.filterID FROM filters AS bad_rows INNER JOIN ( SELECT filtername, MAX(rowid) as max_id FROM filters GROUP BY filtername HAVING COUNT(*) > 1) AS good_rows ON good_rows.filtername = bad_rows.filtername AND good_rows.max_id <> bad_rows.rowid)");
            databaseVersion = DATABASE_VERSION_18;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_18) {
            execSqlWithCatch("DROP TABLE databaseSyncList");
            execSqlWithCatch("CREATE TABLE databaseSyncList ('databaseID' TEXT, 'lastSyncTime' INTEGER)");
            if (((long) databaseID) == 2238011096L) {
                databaseID = new Random().nextInt();
            }
            databaseVersion = DATABASE_VERSION_19;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_19) {
            execSqlWithCatch("ALTER TABLE accounts ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE transactions ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE filters ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE categories ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE payees ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE categorypayee ADD 'deleted' BOOLEAN DEFAULT 0");
            execSqlWithCatch("ALTER TABLE categorypayee ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE categorypayee ADD timestamp TEXT");
            execSqlWithCatch("ALTER TABLE classes ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE ids ADD 'serverID' TEXT");
            execSqlWithCatch("ALTER TABLE repeatingTransactions ADD 'serverID' TEXT");
            execSqlWithCatch("CREATE INDEX accountServerIDs ON accounts (serverID)");
            execSqlWithCatch("CREATE INDEX transactionServerIDs ON transactions (serverID)");
            execSqlWithCatch("CREATE INDEX filterServerIDs ON filters (serverID)");
            execSqlWithCatch("CREATE INDEX categoryServerIDs ON categories (serverID)");
            execSqlWithCatch("CREATE INDEX payeeServerIDs ON payees (serverID)");
            execSqlWithCatch("CREATE INDEX classServerIDs ON classes (serverID)");
            execSqlWithCatch("CREATE INDEX idServerIDs ON ids (serverID)");
            execSqlWithCatch("CREATE INDEX repeatingTransactionServerIDs ON repeatingTransactions (serverID)");
            execSqlWithCatch("UPDATE accounts SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE transactions SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE filters SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE categories SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE payees SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE categorypayee SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE classes SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE ids SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("UPDATE repeatingTransactions SET timestamp = timestamp+2147483647 WHERE timestamp < 0");
            execSqlWithCatch("DELETE FROM categorypayee WHERE categoryID ISNULL");
            execSqlWithCatch("DELETE FROM categorypayee WHERE payeeID ISNULL");
            execSqlWithCatch("DROP TABLE databaseSyncList");
            execSqlWithCatch("CREATE TABLE databaseSyncList ('databaseID' TEXT PRIMARY KEY, 'lastSyncTime' INTEGER)");
            if (((long) databaseID) == 2238011096L) {
                databaseID = new Random().nextInt();
            }
            databaseVersion = DATABASE_VERSION_20;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_20) {
            execSqlWithCatch("UPDATE transactions SET type = 0 WHERE transactionid IN (SELECT transactionid FROM splits WHERE amount <= 0 AND  transactionid in (SELECT transactionid FROM transactions WHERE transactionid in (SELECT transactionid FROM splits WHERE transfertoaccountid <> 0 AND NOT transfertoaccountid IN (SELECT accountId FROM accounts))) GROUP BY transactionid HAVING (COUNT(transactionid) = 1))");
            execSqlWithCatch("UPDATE transactions SET type = 1 WHERE transactionid IN (SELECT transactionid FROM splits WHERE amount > 0 AND  transactionid in (SELECT transactionid FROM transactions WHERE transactionid in (SELECT transactionid FROM splits WHERE transfertoaccountid <> 0 AND NOT transfertoaccountid IN (SELECT accountId FROM accounts)))  GROUP BY transactionid HAVING (COUNT(transactionid) = 1))");
            execSqlWithCatch("UPDATE splits SET transfertoaccountid=0 WHERE transfertoaccountid <> 0 AND NOT transfertoaccountid IN (SELECT accountId FROM accounts)");
            databaseVersion = DATABASE_VERSION_21;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_21) {
            execSqlWithCatch("ALTER TABLE accounts ADD keepTheChangeAccountID INTEGER");
            execSqlWithCatch("DELETE FROM categorypayee WHERE categoryID = 0 OR payeeID = 0");
            execSqlWithCatch("DELETE FROM splits WHERE transactionid IN (SELECT transactionid FROM transactions WHERE accountid NOT IN (SELECT accountid FROM accounts))");
            execSqlWithCatch("DELETE FROM transactions WHERE accountid NOT IN (SELECT accountid FROM accounts)");
            databaseVersion = DATABASE_VERSION_22;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_22) {
            execSqlWithCatch("ALTER TABLE transactions ADD overdraftID TEXT");
            databaseVersion = DATABASE_VERSION_23;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_23) {
            execSqlWithCatch("CREATE TABLE categories_backup AS SELECT * FROM categories;");
            execSqlWithCatch("ALTER TABLE categories_backup ADD serverIDold TEXT");
            execSqlWithCatch("UPDATE categories_backup SET serverIDold = serverid");
            execSqlWithCatch("UPDATE categories_backup SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE categorypayee SET categoryID = (SELECT categories_backup.serverID from categories_backup WHERE categories_backup.serverIDold = categorypayee.categoryID)");
            execSqlWithCatch("UPDATE categories SET serverID = (SELECT categories_backup.serverID from categories_backup WHERE categories_backup.categoryID = categories.categoryID)");
            execSqlWithCatch("DROP TABLE categories_backup");
            execSqlWithCatch("CREATE TABLE payees_backup AS SELECT * FROM payees");
            execSqlWithCatch("ALTER TABLE payees_backup ADD serverIDold TEXT");
            execSqlWithCatch("UPDATE payees_backup SET serverIDold = serverid");
            execSqlWithCatch("UPDATE payees_backup SET serverid = HEX(RANDOMBLOB(16));");
            execSqlWithCatch("UPDATE categorypayee SET payeeID = (SELECT payees_backup.serverID from payees_backup WHERE payees_backup.serverIDold = categorypayee.payeeID)");
            execSqlWithCatch("UPDATE payees SET serverID = (SELECT payees_backup.serverID from payees_backup WHERE payees_backup.payeeID = payees.payeeID)");
            execSqlWithCatch("DROP TABLE payees_backup");
            execSqlWithCatch("DELETE FROM categorypayee WHERE categoryID ISNULL OR payeeID ISNULL OR categoryID = 0 OR payeeID = 0");
            execSqlWithCatch("UPDATE accounts SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE transactions SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE filters SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE categorypayee SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE classes SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE ids SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE repeatingTransactions SET serverid = HEX(RANDOMBLOB(16))");
            execSqlWithCatch("UPDATE accounts SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE transactions SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE filters SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE categories SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE payees SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE categorypayee SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE classes SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE ids SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("UPDATE repeatingTransactions SET timestamp = 1 WHERE timestamp = 0 OR timestamp ISNULL");
            execSqlWithCatch("vacuum");
            databaseVersion = DATABASE_VERSION_24;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_24) {
            execSqlWithCatch("UPDATE accounts SET serverid = HEX(RANDOMBLOB(16)) WHERE serverid ISNULL");
            execSqlWithCatch("DELETE FROM categorypayee");
            execSqlWithCatch("INSERT INTO categorypayee (payeeID, categoryID) SELECT DISTINCT p.serverID, c.serverID FROM transactions t JOIN splits s ON t.transactionID=s.transactionID JOIN payees p ON t.payee=p.payee JOIN categories c ON s.categoryID=c.category");
            execSqlWithCatch("UPDATE categorypayee SET serverID=HEX(RANDOMBLOB(16)), timestamp=1");
            databaseVersion = DATABASE_VERSION_25;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_25) {
            execSqlWithCatch("ALTER TABLE accounts ADD keepChangeRoundTo REAL");
            databaseVersion = DATABASE_VERSION_26;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_26) {
            execSqlWithCatch("UPDATE filters SET deleted='1', timestamp=" + (System.currentTimeMillis() / 1000) + " WHERE filterID IN (SELECT bad_rows.filterID FROM filters AS bad_rows INNER JOIN ( SELECT filtername, MAX(rowid) as max_id FROM filters GROUP BY filtername HAVING COUNT(*) > 1) AS good_rows ON good_rows.filtername = bad_rows.filtername AND good_rows.max_id <> bad_rows.rowid)");
            try {
                db.execSQL("SELECT serverID FROM accounts");
            } catch (SQLiteException e) {
                execSqlWithCatch("ALTER TABLE accounts ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE transactions ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE filters ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE categories ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE payees ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE classes ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE ids ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE repeatingTransactions ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE categorypayee ADD 'deleted' BOOLEAN DEFAULT 0");
                execSqlWithCatch("ALTER TABLE categorypayee ADD 'serverID' TEXT");
                execSqlWithCatch("ALTER TABLE categorypayee ADD timestamp TEXT");
                execSqlWithCatch("UPDATE accounts SET serverid = HEX(RANDOMBLOB(16))");
                execSqlWithCatch("UPDATE transactions SET serverid = HEX(RANDOMBLOB(16))");
                execSqlWithCatch("UPDATE filters SET serverid = HEX(RANDOMBLOB(16))");
                execSqlWithCatch("UPDATE categories SET serverid = HEX(RANDOMBLOB(16))");
                execSqlWithCatch("UPDATE categorypayee SET serverid = HEX(RANDOMBLOB(16))");
                execSqlWithCatch("UPDATE classes SET serverid = HEX(RANDOMBLOB(16))");
                execSqlWithCatch("UPDATE ids SET serverid = HEX(RANDOMBLOB(16))");
                execSqlWithCatch("UPDATE repeatingTransactions SET serverid = HEX(RANDOMBLOB(16))");
            }
            execSqlWithCatch("ALTER TABLE accounts ADD keepChangeRoundTo REAL");
            execSqlWithCatch("ALTER TABLE accounts ADD keepTheChangeAccountID INTEGER");
            execSqlWithCatch("ALTER TABLE transactions ADD overdraftID TEXT");
            databaseVersion = DATABASE_VERSION_27;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_27) {
            execSqlWithCatch("ALTER TABLE categorypayee ADD deleted BOOLEAN");
            execSqlWithCatch("ALTER TABLE filters ADD selectedFilterName TEXT");
            Prefs.fixAccountsForUpdateU4();
            Prefs.fixRepeatingTransactionDisconnectionForUpdate1_09();
            Prefs.replaceIconNamesForUpdate1_11();
            databaseVersion = DATABASE_VERSION_28;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_28) {
            execSqlWithCatch("UPDATE repeatingtransactions SET deleted=1 WHERE NOT EXISTS (SELECT transactionid FROM transactions WHERE repeatingtransactions.transactionid = transactions.transactionid AND deleted = 0)");
            databaseVersion = DATABASE_VERSION_29;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_29) {
            execSqlWithCatch("ALTER TABLE repeatingTransactions ADD sendLocalNotifications INTEGER");
            execSqlWithCatch("ALTER TABLE repeatingTransactions ADD notifyDaysInAdvance INTEGER");
            execSqlWithCatch("CREATE TABLE categoryBudgets ('categoryBudgetID' INTEGER PRIMARY KEY AUTOINCREMENT,'deleted' BOOLEAN DEFAULT 0, 'serverID' TEXT, 'timestamp' INTEGER, 'categoryName' TEXT, 'date' INTEGER, 'budgetLimit' REAL, 'resetRollover' BOOLEAN DEFAULT 0)");
            execSqlWithCatch("ALTER TABLE categories ADD rollover BOOLEAN DEFAULT 0");
            databaseVersion = DATABASE_VERSION_30;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_30) {
            execSqlWithCatch("UPDATE repeatingtransactions SET deleted=1,timeStamp = (SELECT MAX(lastSyncTime) + 1 FROM databaseSyncList) WHERE NOT EXISTS (SELECT transactionid FROM transactions WHERE repeatingtransactions.transactionid = transactions.transactionid AND deleted = 0)");
            databaseVersion = DATABASE_VERSION_31;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_31) {
            execSqlWithCatch("CREATE TEMPORARY TABLE TEMP_TABLE_FILTERS ('filterID' INTEGER PRIMARY KEY AUTOINCREMENT, 'deleted' BOOLEAN DEFAULT 0, 'timestamp' INTEGER, 'filterName' TEXT, 'type' INTEGER, 'dateFrom' INTEGER, 'dateTo' INTEGER, 'accountID' INTEGER, 'categoryID' TEXT, 'payee' TEXT, 'classID' TEXT, 'checkNumber' TEXT, 'cleared' INTEGER, 'spotlight' TEXT, 'selectedFilterName' TEXT, 'serverID' TEXT ); INSERT INTO TEMP_TABLE_FILTERS SELECT filterID, deleted, timestamp, filterName, type, dateFrom, dateTo, accountID, categoryID, payee, classID, checkNumber, cleared, spotlight, selectedFilterName, serverID FROM filters; DROP TABLE filters; CREATE TABLE filters ( 'filterID' INTEGER PRIMARY KEY AUTOINCREMENT, 'deleted' BOOLEAN DEFAULT 0, 'timestamp' INTEGER, 'filterName' TEXT, 'type' INTEGER, 'dateFrom' INTEGER, 'dateTo' INTEGER, 'accountID' INTEGER, 'categoryID' TEXT, 'payee' TEXT, 'classID' TEXT, 'checkNumber' TEXT, 'cleared' INTEGER, 'spotlight' TEXT, 'selectedFilterName' TEXT, 'serverID' TEXT ); INSERT INTO filters SELECT filterID, deleted, timestamp, filterName, type, dateFrom, dateTo, accountID, categoryID, payee, classID, checkNumber, cleared, spotlight, selectedFilterName, serverID FROM TEMP_TABLE_FILTERS; DROP TABLE TEMP_TABLE_FILTERS;");
            databaseVersion = DATABASE_VERSION_32;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_32) {
            execSqlWithCatch("CREATE INDEX accountServerIDs ON accounts (serverID)");
            execSqlWithCatch("CREATE INDEX transactionServerIDs ON transactions (serverID)");
            execSqlWithCatch("CREATE INDEX filterServerIDs ON filters (serverID)");
            execSqlWithCatch("CREATE INDEX categoryServerIDs ON categories (serverID)");
            execSqlWithCatch("CREATE INDEX payeeServerIDs ON payees (serverID)");
            execSqlWithCatch("CREATE INDEX classServerIDs ON classes (serverID)");
            execSqlWithCatch("CREATE INDEX idServerIDs ON ids (serverID)");
            execSqlWithCatch("CREATE INDEX repeatingTransactionServerIDs ON repeatingTransactions (serverID)");
            execSqlWithCatch("ALTER TABLE categorypayee ADD timestamp INTEGER");
            execSqlWithCatch("ALTER TABLE categorypayee ADD deleted INTEGER");
            execSqlWithCatch("CREATE TEMPORARY TABLE TEMP_TABLE_FILTERS ('filterID' INTEGER PRIMARY KEY AUTOINCREMENT, 'deleted' BOOLEAN DEFAULT 0, 'timestamp' INTEGER, 'filterName' TEXT, 'type' INTEGER, 'dateFrom' INTEGER, 'dateTo' INTEGER, 'accountID' INTEGER, 'categoryID' TEXT, 'payee' TEXT, 'classID' TEXT, 'checkNumber' TEXT, 'cleared' INTEGER, 'spotlight' TEXT, 'selectedFilterName' TEXT, 'serverID' TEXT ); INSERT INTO TEMP_TABLE_FILTERS SELECT filterID, deleted, timestamp, filterName, type, dateFrom, dateTo, accountID, categoryID, payee, classID, checkNumber, cleared, spotlight, selectedFilterName, serverID FROM filters; DROP TABLE filters; CREATE TABLE filters ( 'filterID' INTEGER PRIMARY KEY AUTOINCREMENT, 'deleted' BOOLEAN DEFAULT 0, 'timestamp' INTEGER, 'filterName' TEXT, 'type' INTEGER, 'dateFrom' INTEGER, 'dateTo' INTEGER, 'accountID' INTEGER, 'categoryID' TEXT, 'payee' TEXT, 'classID' TEXT, 'checkNumber' TEXT, 'cleared' INTEGER, 'spotlight' TEXT, 'selectedFilterName' TEXT, 'serverID' TEXT ); INSERT INTO filters SELECT filterID, deleted, timestamp, filterName, type, dateFrom, dateTo, accountID, categoryID, payee, classID, checkNumber, cleared, spotlight, selectedFilterName, serverID FROM TEMP_TABLE_FILTERS; DROP TABLE TEMP_TABLE_FILTERS;");
            databaseVersion = DATABASE_VERSION_33;
            updateVersion(databaseVersion);
        }
        if (databaseVersion == DATABASE_VERSION_33) {
            execSqlWithCatch("ALTER TABLE preferences ADD multipleCurrencies INT DEFAULT 0");
            setMultipleCurrencies(Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES));
            execSqlWithCatch("ALTER TABLE preferences ADD homeCurrency TEXT");
            String currencyCode = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
            if (currencyCode != null && currencyCode.length() > 0) {
                setHomeCurrency(Prefs.getStringPref(Prefs.HOMECURRENCYCODE));
            }
            databaseVersion = DATABASE_VERSION_CURRENT;
            updateVersion(databaseVersion);
        }
    }

    public static Cursor rawQuery(String query, String[] args) {
        Cursor c;
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            c = db.rawQuery(query, args);
        }
        return c;
    }

    public static void execSQL(String s) {
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            db.execSQL(s);
        }
    }

    public static void execSQL(String s, String[] args) {
        if (args == null || args.length == 0) {
            execSQL(s);
            return;
        }
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            db.execSQL(s, args);
        }
    }

    public static int delete(String table, String whereClause, String[] whereArgs) {
        int retVal;
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            retVal = db.delete(table, whereClause, whereArgs);
        }
        return retVal;
    }

    public static Cursor query(SQLiteQueryBuilder qb, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder) {
        Cursor c;
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            c = qb.query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder);
        }
        return c;
    }

    public static long insert(String table, String nullColumnHack, ContentValues values) {
        long retVal;
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            retVal = db.insert(table, nullColumnHack, values);
        }
        return retVal;
    }

    public static int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        int retVal;
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            retVal = db.update(table, values, whereClause, whereArgs);
        }
        return retVal;
    }

    public static long replace(String table, String nullColumnHack, ContentValues initialValues) {
        long retVal;
        SQLiteDatabase db = currentDB();
        synchronized (dbLock) {
            retVal = db.insert(table, nullColumnHack, initialValues);
        }
        return retVal;
    }

    public static void deleteUnlinkedRepeatingTransactions() {
        execSqlWithCatch("UPDATE repeatingtransactions SET deleted=1,timeStamp = (SELECT MAX(lastSyncTime) + 1 FROM databaseSyncList) WHERE NOT EXISTS (SELECT transactionid FROM transactions WHERE repeatingtransactions.transactionid = transactions.transactionid AND deleted = 0)");
    }

    public static void loadDatabasePreferences() {
        Cursor c = rawQuery("SELECT databaseVersion, databaseID FROM preferences WHERE rowid=1", null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            databaseVersion = c.getInt(0);
            databaseID = c.getInt(1);
        } else {
            Random r = new Random();
            databaseVersion = DATABASE_VERSION_CURRENT;
            updateVersion(databaseVersion);
            databaseID = r.nextInt();
        }
        c.close();
        updateDatabase();
    }

    public static void pullPrefsOutOfDatabase() {
        Prefs.setPref(Prefs.MULTIPLECURRENCIES, getMultipleCurrencies());
        Prefs.setPref(Prefs.HOMECURRENCYCODE, getHomeCurrency());
    }

    private static void updateVersion(int version) {
        execSQL("UPDATE preferences SET databaseVersion=" + version + " WHERE rowid=1", null);
    }

    public static SQLiteDatabase currentDB() {
        if (dbh == null) {
            dbh = new DatabaseHelper(SMMoney.getAppContext());
        }
        if (db == null) {
            db = dbh.getWritableDatabase();
            db.setLockingEnabled(true);
        }
        return db;
    }

    public static void unlockDB() {
    }

    public static void closeDB() {
        if (dbh != null) {
            dbh.close();
            db = null;
        }
    }

    public static void closeDBAndNullify() {
        dbh.close();
        dbh = null;
        db = null;
    }

    public static void autoAddLookupItemsFromTransaction(TransactionClass transaction) {
        if (transaction.getPayee() != null && transaction.getPayee().length() > 0 && PayeeClass.idForPayee(transaction.getPayee()) == 0) {
            PayeeClass.insertIntoDatabase(transaction.getPayee());
        }
        for (SplitsClass split : transaction.getSplits()) {
            if (!split.isTransfer()) {
                if (ClassNameClass.idForClass(split.getClassName()) == 0 && split.getClassName() != null && split.getClassName().length() > 0) {
                    ClassNameClass.insertIntoDatabase(split.getClassName());
                }
                int categoryID = CategoryClass.idForCategory(split.getCategory());
                if (categoryID == 0 && split.getCategory() != null && split.getCategory().length() > 0) {
                    categoryID = CategoryClass.insertIntoDatabase(split.getCategory());
                }
                CategoryClass categoryRecord = new CategoryClass(categoryID);
                if (split.getCategory() != null && split.getCategory().length() > 0 && split.getCategory().contains(":")) {
                    String cat = split.getCategory();
                    int index = cat.lastIndexOf(":");
                    while (index != -1) {
                        cat = cat.substring(0, index);
                        if (CategoryClass.idForCategory(cat) == 0 && cat != null && cat.length() > 0) {
                            categoryID = CategoryClass.insertIntoDatabase(cat);
                        }
                        index = cat.lastIndexOf(":");
                    }
                }
            }
        }
    }

    public static void populateDatabaseDefaults(Context context) {
        int i = 0;
        String[] defaultCategories = new String[20];
        defaultCategories[0] = Locales.kLOC_DEFAULTCATEGORIES1;
        defaultCategories[1] = Locales.kLOC_DEFAULTCATEGORIES2;
        defaultCategories[2] = Locales.kLOC_DEFAULTCATEGORIES3;
        defaultCategories[3] = Locales.kLOC_DEFAULTCATEGORIES4;
        defaultCategories[4] = Locales.kLOC_DEFAULTCATEGORIES5;
        defaultCategories[5] = Locales.kLOC_DEFAULTCATEGORIES6;
        defaultCategories[6] = Locales.kLOC_DEFAULTCATEGORIES7;
        defaultCategories[7] = Locales.kLOC_DEFAULTCATEGORIES8;
        defaultCategories[8] = Locales.kLOC_DEFAULTCATEGORIES9;
        defaultCategories[9] = Locales.kLOC_DEFAULTCATEGORIES10;
        defaultCategories[10] = Locales.kLOC_DEFAULTCATEGORIES11;
        defaultCategories[11] = Locales.kLOC_DEFAULTCATEGORIES12;
        defaultCategories[12] = Locales.kLOC_DEFAULTCATEGORIES13;
        defaultCategories[13] = Locales.kLOC_DEFAULTCATEGORIES14;
        defaultCategories[14] = Locales.kLOC_DEFAULTCATEGORIES15;
        defaultCategories[15] = Locales.kLOC_DEFAULTCATEGORIES16;
        defaultCategories[16] = Locales.kLOC_DEFAULTCATEGORIES17;
        defaultCategories[17] = Locales.kLOC_DEFAULTCATEGORIES18;
        defaultCategories[18] = Locales.kLOC_DEFAULTCATEGORIES19;
        defaultCategories[19] = Locales.kLOC_DEFAULTCATEGORIES20;
        if (CategoryClass.idForCategory(SMMoney.getAppContext().getString(R.string.kLOC_DEFAULTCATEGORIES1)) <= 0) {
            int i2;
            int length = defaultCategories.length;
            for (i2 = 0; i2 < length; i2 += 1) {
                CategoryClass.insertIntoDatabase(defaultCategories[i2]);
            }
            String[] defaultIDs = new String[5];
            defaultIDs[0] = Locales.kLOC_DEFAULTIDS1;
            defaultIDs[1] = Locales.kLOC_DEFAULTIDS2;
            defaultIDs[2] = Locales.kLOC_DEFAULTIDS3;
            defaultIDs[3] = Locales.kLOC_DEFAULTIDS4;
            defaultIDs[4] = Locales.kLOC_DEFAULTIDS5;
            length = defaultIDs.length;
            for (i2 = 0; i2 < length; i2 += 1) {
                IDClass.insertIntoDatabase(defaultIDs[i2]);
            }
            String[] defaultClasses = new String[2];
            defaultClasses[0] = Locales.kLOC_DEFAULTCLASSES1;
            defaultClasses[1] = Locales.kLOC_DEFAULTCLASSES2;
            i2 = defaultClasses.length;
            while (i < i2) {
                ClassNameClass.insertIntoDatabase(defaultClasses[i]);
                i += 1;
            }
        }
    }

    public static String SQLFormat(String aString) {
        if (aString == null) {
            return "''";
        }
        return DatabaseUtils.sqlEscapeString(aString);
    }

    public static void sqlite3_begin() {
        try {
            currentDB().beginTransaction();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "sql error begining a new transaction");
        }
    }

    public static void sqlite3_commit() {
        try {
            currentDB().setTransactionSuccessful();
            currentDB().endTransaction();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "sql error commiting a transaction");
        }
    }

    public static void sqlite3_rollback() {
        try {
            currentDB().endTransaction();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "sql error rollingback a transaction");
        }
    }

    public static void wipeDatabase() {
        execSQL("DELETE FROM transactions");
        execSQL("DELETE FROM splits");
        execSQL("DELETE FROM accounts");
        execSQL("DELETE FROM filters");
        execSQL("DELETE FROM categories");
        execSQL("DELETE FROM payees");
        execSQL("DELETE FROM categorypayee");
        execSQL("DELETE FROM classes");
        execSQL("DELETE FROM ids");
        execSQL("DELETE FROM repeatingTransactions");
        execSQL("DELETE FROM categoryBudgets");
        execSQL("DELETE FROM databaseSyncList");
        execSQL("DELETE FROM sqlite_sequence");
    }

    public static String newServerID() {
        return UUID.randomUUID().toString();
    }

    public static PocketMoneyRecordClass[] queryServerSyncTableWithPKandClassAndTime(String table, String primaryKey, Class<? extends PocketMoneyRecordClass> aClass, long lastSyncTime) {
        Cursor c;
        ArrayList<PocketMoneyRecordClass> foundRecords = new ArrayList<>();
        if (lastSyncTime > 0) {
            c = rawQuery("SELECT " + primaryKey + " FROM " + table + " WHERE timestamp >= " + lastSyncTime, null);
        } else {
            c = rawQuery("SELECT " + primaryKey + " FROM " + table, null);
        }
        Constructor<? extends PocketMoneyRecordClass> constructor = null;
        PocketMoneyRecordClass pm = null;
        try {
            Class[] clsArr = new Class[PMSYNC_VERSION_1];
            clsArr[0] = Integer.TYPE;
            constructor = aClass.getConstructor(clsArr);
        } catch (SecurityException | NoSuchMethodException e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        while (c.moveToNext()) {
            try {
                Object[] objArr = new Object[PMSYNC_VERSION_1];
                objArr[0] = c.getInt(0);
                pm = constructor.newInstance(objArr);
            } catch (Exception e3) {
                Log.e(SMMoney.TAG, e3.getLocalizedMessage());
                e3.printStackTrace();
            }
            foundRecords.add(pm);
        }
        if (foundRecords.size() > 0) {
            PocketMoneyRecordClass[] returnRecords = new PocketMoneyRecordClass[foundRecords.size()];
            int i = 0;
            for (PocketMoneyRecordClass foundRecord : foundRecords) {
                int i2 = i + 1;
                returnRecords[i] = foundRecord;
                i = i2;
            }
            c.close();
            return returnRecords;
        }
        c.close();
        return null;
    }

    public static boolean queryAndWriteServerSyncTableWithPKandClassAndTime(BufferedWriter out, String table, String primaryKey, Class<? extends PocketMoneyRecordClass> aClass, long lastSyncTime) {
        Cursor c;
        if (lastSyncTime > 0) {
            c = rawQuery("SELECT " + primaryKey + " FROM " + table + " WHERE timestamp >= " + lastSyncTime, null);
        } else {
            c = rawQuery("SELECT " + primaryKey + " FROM " + table, null);
        }
        Constructor<? extends PocketMoneyRecordClass> constructor = null;
        try {
            Class[] clsArr = new Class[PMSYNC_VERSION_1];
            clsArr[0] = Integer.TYPE;
            constructor = aClass.getConstructor(clsArr);
        } catch (SecurityException | NoSuchMethodException e) {
            Log.e(SMMoney.TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        while (c.moveToNext()) {
            try {
                Object[] objArr = new Object[PMSYNC_VERSION_1];
                objArr[0] = c.getInt(0);
                out.write(new StringBuilder(String.valueOf(constructor.newInstance(objArr).XMLString())).append("\n").toString());
            } catch (Exception e3) {
                Log.e(SMMoney.TAG, e3.getLocalizedMessage());
                e3.printStackTrace();
            }
        }
        return true;
    }

    public static long lastSyncTimeForUDID(String udid) {
        Cursor c;
        long lastSyncTime = 0;
        try {
            c = rawQuery("SELECT lastSyncTime FROM databaseSyncList WHERE databaseID LIKE '" + udid + "'", null);
        } catch (Exception e) {
            c = rawQuery("SELECT lastSyncTime FROM databaseSyncList WHERE databaseID LIKE " + SQLFormat(udid), null);
        }
        if (c.getCount() > 0) {
            c.moveToFirst();
            lastSyncTime = c.getLong(0);
        }
        c.close();
        return lastSyncTime;
    }

    public static void setLastSyncTime(long lastSyncTime, String udid) {
        try {
            execSQL("INSERT OR REPLACE INTO databaseSyncList (databaseID,lastSyncTime) VALUES ('" + udid + "'," + lastSyncTime + ")");
        } catch (Exception e) {
            execSQL("INSERT OR REPLACE INTO databaseSyncList (databaseID,lastSyncTime) VALUES (" + SQLFormat(udid) + "," + lastSyncTime + ")");
        }
    }

    private static boolean getMultipleCurrencies() {
        boolean mc = false;
        Cursor c = rawQuery("SELECT multipleCurrencies FROM preferences WHERE rowid=1", null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            mc = c.getInt(0) == PMSYNC_VERSION_1;
        }
        c.close();
        return mc;
    }

    public static void setMultipleCurrencies(boolean mc) {
        execSqlWithCatch("UPDATE preferences SET multipleCurrencies=" + (mc ? PMSYNC_VERSION_1 : 0) + " WHERE rowid=1");
    }

    private static String getHomeCurrency() {
        String currency = "";
        Cursor c = rawQuery("SELECT homeCurrency FROM preferences WHERE rowid=1", null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            currency = c.getString(0);
        }
        c.close();
        return currency;
    }

    public static void setHomeCurrency(String currency) {
        execSqlWithCatch("UPDATE preferences SET homeCurrency=" + SQLFormat(currency) + " WHERE rowid=1");
    }
}

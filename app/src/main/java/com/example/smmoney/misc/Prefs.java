package com.example.smmoney.misc;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.accounts.AccountTypeIconGridActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

public class Prefs {
    public static final String ALLTRANSACTIONS = "prefsdisplayaccountalltransactions"; // ACCOUNTS PREF Show all trans. Type: boolean
    private static final String ALTBALANCETYPE = "secondbalancelinepref";
    private static final String APP_FIRST_RUN = "appFirstRun"; // Is this first run v0. Type must be boolean
    private static final String APP_FIRST_RUN_U1 = "appFirstRunUpdate1"; // Is this first run v1. Type must be boolean
    private static final String APP_FIRST_RUN_U1_05 = "appFirstRunUpdate1_05"; // Is this first run v1.05. Type must be boolean
    private static final String APP_FIRST_RUN_U1_09 = "appFirstRunUpdate1_09"; // Is this first run v1.09 Type must be boolean
    private static final String APP_FIRST_RUN_U1_11 = "appFirstRunUpdate1_11"; // Is this first run v1.11 Type must be boolean
    private static final String APP_FIRST_RUN_U2 = "appFirstRunUpdate2"; // Is this first run v2. Type must be boolean
    private static final String APP_FIRST_RUN_U2_0 = "appFirstRunUpdate2_0"; // Is this first run v2.0 Type must be boolean
    private static final String APP_FIRST_RUN_U2_0_5 = "appFirstRunUpdate2_0_5"; // Is this first run v2.0.5 Type must be boolean
    private static final String APP_FIRST_RUN_U3 = "appFirstRunUpdate3"; // Is this first run v3. Type must be boolean
    public static final String AUTOADD_LOOKUPS = "prefsmanagedlistsautoadd";
    public static final String AUTO_FILL = "prefsmanagedlistsautofill";
    public static final String AUTO_SUGGEST = "prefsautosuggest";
    public static final String BALANCEBARREGISTER = "balancebarregister";
    public static final String BALANCEBARUNIFIED = "balancebarunified";
    public static final String BALANCEONDATE = "prefsaccountsviewoptionsasofdate";
    public static final String BALANCETYPE = "balanceType";
    public static final String BUDGETDISPLAY = "budgetDisplay";
    public static final String BUDGETHIDEZEROSACTUALS = "budgetHideZeros"; // BUDGET PREFS hide if amount = 0 Type: boolean
    public static final String BUDGETINCLUDETRANSFERS = "budgetIncludeTransfers"; // BUDGET PREFS incl both sides of xfer Type: boolean
    public static final String BUDGETINCLUDEUNBUDGETED = "budgetIncludeUnbudgeted"; // BUDGET PREFS incl unbudgeted cat in total Type: boolean
    public static final String BUDGETSAVEDBEAT = "savedBeatBudgetDisplay";
    public static final String BUDGETSHOWALLACCOUNTS = "budgetShowAllAccounts"; // BUDGET PREFS show all a/cs in budget? Tpye: booleaan
    public static final String BUDGETSHOWCENTS = "budgetShowCents"; // BUDGET PREFS show decimal in budget. Type: boolean
    public static final String BUDGETSHOWUNBUDGETED = "budgetShowUnbudgeted";
    public static final String BUDGETSTARTDATE = "budgetStartDate";
    public static final String BUDGETS_SORTON = "budgetsSortOn";
    public static final String COLLAPSE_ASSETS = "collapseAssets"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_BANKS = "collapseBanks"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_CASH = "collapseCash"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_CREDITCARDS = "collapseCreditCards"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_CUSTOM = "collapseCustom"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_EXPENSES = "collapseExpense"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_INCOME = "collapseIncome"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_LIABILITIES = "collapseLiabilities"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_ONLINE = "collapseOnline"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String COLLAPSE_UNBUDGETED = "collapseUnbudgeted"; // ACCOUNTS VIEW state re accounts view. Type - maybe boolean?
    public static final String CREATED_ON = "createdon";
    public static final String CURRENT_DATABASE_NAME = "currentDBName";
    private static final boolean DEBUGGING = true;
    private static final String DEFAULTROUNDING = "prefscurrencydefaultrounding";
    public static final String DISPLAY_BUDGETPERIOD = "budgetPeriod";
    public static final String EDITTRANSACTION_CATBYPAYEE_FIELD = "prefsdispplayeditcatposition";
    public static final String EDITTRANSACTION_SHOW_ALL_ACCOUNTS = "prefsdisplayeditallaccounts";
    public static final String EDITTRANSACTION_SHOW_CATEGORY_FIELD = "prefsdisplayeditcategory";
    public static final String EDITTRANSACTION_SHOW_CLASS_FIELD = "prefsdisplayeditclass";
    public static final String EDITTRANSACTION_SHOW_CLEARED_FIELD = "prefsdisplayeditcleared";
    public static final String EDITTRANSACTION_SHOW_ID_FIELD = "prefsdisplayeditid";
    public static final String EDITTRANSACTION_SHOW_MEMO_FIELD = "prefsdisplayeditmemo";
    public static final String EDITTRANSACTION_STARTING_FIELD = "prefsdisplayeditstartediting";
    public static final String EMAILPARTNER_EMAIL = "emailpartneremail";
    public static final String EMAILPARTNER_ENABLED = "emailpartner";
    public static final String ENCODING = "prefsdatatransfersfileencoding";
    public static final String EXPORT_STOREDEVICE = "prefsdatatransferssdcard";
    public static final String FILTERS = "prefsdisplayaccountfilters"; // ACCOUNTS PREFS Show filers on accounts screen Tpye:boolean
    public static final String GROUPBYACCOUNTTYPE = "prefsdisplayaccountgroupbyaccounttype"; //ACCOUNTS PREFS Group eg assets or no. Tpye: boolean
    public static final String HINT_ACCOUNT_INFO = "h2"; // whether this hint shows? Type: Likely boolean
    public static final String HINT_ACCOUNT_TYPE_OPTIONS = "h8"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_ACCOUNT_VIEW_OPTIONS = "h6"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_BUDGETS = "hB"; // whether this hint shows? Type: Likely boolean
    public static final String HINT_EDITTRANSACTION = "h5"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_FILETRANSFER = "hA"; // whether this hint shows? Type: Likely boolean
    public static final String HINT_FIRSTNEWACCOUNT = "h3"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_KEYBOARD = "h9"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_REDBADGE = "hD"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_REGISTER = "h4"; // whether this hint shows? Type: Likely boolean
    public static final String HINT_REPEATING = "hE"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_REPORTS = "h7"; // whether this hint shows? Type: Likely boolean
    private static final String HINT_VIDEOS = "hC"; // whether this hint shows? Type: Likely boolean
    public static final String HINT_WELCOME = "h1"; // whether this hint shows? Type: Likely boolean
    public static final String HOMECURRENCYCODE = "prefscurrencyhomecurrency"; // Home curr code (3 digit). Type: String
    public static final String LASTFULLEXPORT = "lastFllExport";
    public static final String LAST_UPGRADE_DIALOG = "lastupgradedialog";
    public static final String MULTIPLECURRENCIES = "prefscurrencymultiplecurrencies"; // multi curr. Tpye: boolean
    public static final String NAMEOFPREFS = "myCustomSharedPrefs";
    public static final String NEWESTTRANSACTIONFIRST = "prefstransactionviewoptionssortorder";
    public static final String PASSWORD = "prefssecuritypassword"; // To hold password? Type presumably String?
    public static final String PASSWORD_DELAY = "prefssecuritydelay"; // To hold delay before require pw. Type? Maybe long? double?
    public static final String PASSWORD_DELAY_LAST = "prefspassworddelaylast"; // ?? Hold prev pw delay value?
    public static final String PMSYNC_CLIENTSERVER = "clientServer";
    public static final String PMSYNC_IP = "pmsyncip";
    public static final String PMSYNC_PORT = "pmsyncport";
    public static final String PREFS_QIF_EXPORT_SEPERATELY = "datatransferqiffileforeachaccount";
    public static final String PREFS_REPEATING_UPCOMING_PERIOD = "repeatingupcomingperiod";
    public static final String PREFS_REPORTDISPLAY = "reportDisplay";
    public static final String PREFS_REPORTS_CHARTTYPE = "reportChartType";
    public static final String PREFS_REPORTS_SORTDIRECTION = "reportSortDirection";
    public static final String PREFS_REPORTS_SORTON = "reportSortOn";
    public static final String QIF_DATEFORMAT = "datatransferqifdateformat";
    public static final String QIF_DATESEPARATOR = "datatransferqifdateseparator";
    public static final String QIF_EXPORT_SEPERATELY = "datatransferqiffileforeachaccount";
    public static final String QIF_IMPORT_BUDGETS = "datatransferqifwipeonlyonrestore";
    public static final String QIF_IMPORT_DUPS = "datatransferqifimportdups";
    public static final String QIF_MARKALLCLEARED = "datatransferqifmarkallclear";
    public static final String QIF_NUMBERFORMAT = "datatransfernumberformat";
    public static final String QIF_WIPETRANSACTIONS = "datatransferqifbudgets";
    public static final String RECURPOSTINGDISABLEDWARNING = "recurpostingwarning";
    public static final String RECURPOSTINGENABLED = "recurPostingEnabled";
    public static final String RECURRDAYSINADVANCE = "recurDaysInAdvance";
    public static final String REPEATINGTRANSACTIONS = "prefsdisplayaccountrepeatingtransactions"; // ACCOUNTS PREF Show all trans option on a/c's screen Type: boolean
    public static final String REPORTS_GROUPSUBCATEGORIES = "prefsdisplayreportsgrouponsubcategories";
    public static final String REPORTS_PERIOD = "reportsperiod";
    public static final String REPORTS_SORTON = "reportSortOn";
    public static final String SHOWSUMMARYCHARTS = "showSummaryCharts";
    public static final String SHOWTIME = "prefsdisplayedittime";
    public static final String SHUTTINGDOWN = "shuttingdown";
    public static final String SUMMARYCHARTS_CHARTTYPE = "summaryChartType";
    public static final String THEME_COLOR = "themecolor";
    public static final String TRANSACTIONS_SHOW_CATEGORY_FIELD = "prefsdisplayregistercategory";
    public static final String TRANSACTIONS_SHOW_CLASS_FIELD = "prefsdisplayregisterclass";
    public static final String TRANSACTIONS_SHOW_DATE_FIELD = "prefsdisplayregisterdate";
    public static final String TRANSACTIONS_SHOW_FOREIGNAMOUNT = "prefsdisplayregisterforeignamount";
    public static final String TRANSACTIONS_SHOW_ID_FIELD = "prefsdisplayregisterid";
    public static final String TRANSACTIONS_SHOW_NOTES_FIELD = "prefsdisplayregistermemo";
    public static final String TRANSACTIONS_SHOW_RUNNING_FIELD = "prefsdisplayregistercurrentbalance";
    public static final String TRANSACTIONS_SHOW_TRANSTOANDTO_FIELD = "prefsdisplayedittransferandpayee";
    public static final String TRANSACTIONS_SORTON = "prefstransactionviewoptionssorton";
    public static final String TRANSACTIONS_TRUNCATE_ID = "prefsdisplayregistertruncateid";
    public static final String TRANSACTIONS_UNLINK_ID_FIELD = "prefsdisplayeditunlink";
    public static final String TRANSFERTYPE = "prefsdatatransferstransfermode";
    public static final String UPDATEEXCHANGERATES = "prefscurrencyupdatexrates"; // update f/x rates or no. Type: boolean
    public static final String USINGUUID = "usinguuid";
    public static final String VIEWACCOUNTS = "prefsaccountsviewoptionsshowaccounts";
    private static SharedPreferences sharedPrefs = null;

    static class AnonymousClass1 implements OnClickListener {
        private final /* synthetic */ Context val$cont;

        AnonymousClass1(Context context) {
            this.val$cont = context;
        }

        public void onClick(DialogInterface dialog, int id) {
            Builder alt_bld = new Builder(this.val$cont);
            Builder cancelable = alt_bld.setMessage("This will deleted the current DB. Are you sure you want to do this?").setCancelable(false);
            CharSequence charSequence = Locales.kLOC_GENERAL_YES;
            cancelable.setPositiveButton(charSequence, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Prefs.importDB(AnonymousClass1.this.val$cont);
                }
            }).setNegativeButton(Locales.kLOC_GENERAL_NO, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alt_bld.create().show();
        }
    }

    static class AnonymousClass2 implements OnClickListener {
        private final /* synthetic */ Context val$cont;

        AnonymousClass2(Context context) {
            this.val$cont = context;
        }

        public void onClick(DialogInterface dialog, int id) {
            Prefs.exportDB(this.val$cont);
            dialog.cancel();
        }
    }

    public static void processDataBase(Context context) {
        Builder alt_bld = new Builder(context);
        alt_bld.setMessage("/sdcard/PocketMoneyDB/SMMoneyDB.sql\nYou need to restart after importing").setCancelable(false).setPositiveButton("Import", new AnonymousClass1(context)).setNegativeButton("Export", new AnonymousClass2(context));
        alt_bld.create().show();
    }

    public static void importDB(Context c) {
        File dbBackupFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PocketMoneyBackup/SMMoneyDB.sql");
        if (!dbBackupFile.exists()) {
            Log.w(SMMoney.TAG, "Database backup file does not exist, cannot import.");
        } else if (!dbBackupFile.canRead()) {
            Log.w(SMMoney.TAG, "Database backup file exists, but is not readable, cannot import.");
        }
        File dbFile = new File(Environment.getDataDirectory() + "/data/" + c.getPackageName() + "/databases/SMMoneyDB.sql");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        try {
            dbFile.createNewFile();
            copyFile(dbBackupFile, dbFile);
        } catch (IOException e) {
            Log.e("com.catamount.com", e.getMessage(), e);
        }
    }

    public static void exportDB(Context c) {
        File dbFile = new File(Environment.getDataDirectory() + "/data/" + c.getPackageName() + "/databases/SMMoneyDB.sql");
        if (SMMoney.IsExternalStorageWritable()) {
            //File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "PocketMoneyBackup");
            File exportDir = new File(Environment.getExternalStorageDirectory(), "PocketMoneyBackup");
            exportDir.mkdirs();
            File file = new File(exportDir, dbFile.getName());
            try {
                file.createNewFile();
                copyFile(dbFile, file);
                Toast.makeText(c, "Backup Successful", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e(SMMoney.TAG, e.getMessage(), e);
                Toast.makeText(c, "Failed to copy backup file", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel(); FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    private static SharedPreferences getSharedPrefs() {
        if (sharedPrefs == null) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(SMMoney.getAppContext());
        }
        return sharedPrefs;
    }

    private static SharedPreferences getSharedPrefs(Context context) {
        if (sharedPrefs == null) {
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sharedPrefs;
    }

    public static String getStringPref(String thePref) {
        return getSharedPrefs().getString(thePref, "Blue");
    }

    public static Long getLongPref(String thePref) {
        return getSharedPrefs().getLong(thePref, 0L);
    }

    public static int getIntPref(String thePref) {
        return getSharedPrefs().getInt(thePref, 0);
    }

    public static boolean getBooleanPref(String thePref) {
        return getSharedPrefs().getBoolean(thePref, false);
    }

    public static String getStringPref(Context context, String thePref) {
        return getSharedPrefs().getString(thePref, "");
    }

    public static Long getLongPref(Context context, String thePref) {
        return getSharedPrefs().getLong(thePref, 0L);
    }

    public static int getIntPref(Context context, String thePref) {
        return getSharedPrefs().getInt(thePref, 0);
    }

    public static boolean getBooleanPref(Context context, String thePref) {
        return getSharedPrefs(context).getBoolean(thePref, false);
    }

    public static boolean getBooleanPref(String thePref, boolean defaultValue) {
        return getSharedPrefs().getBoolean(thePref, defaultValue);
    }

    public static void setPref(String thePref, String newPref) {
        Editor editor = getSharedPrefs().edit();
        editor.putString(thePref, newPref);
        editor.apply();
    }

    public static void setPref(String thePref, int newPref) {
        Editor editor = getSharedPrefs().edit();
        editor.putInt(thePref, newPref);
        editor.apply();
    }

    public static void setPref(String thePref, long newPref) {
        Editor editor = getSharedPrefs().edit();
        editor.putLong(thePref, newPref);
        editor.apply();
    }

    public static void setPref(String thePref, boolean newPref) {
        Editor editor = getSharedPrefs().edit();
        editor.putBoolean(thePref, newPref);
        editor.apply();
    }

    public static void resetHints() {
        setPref(HINT_WELCOME, false);
        setPref(HINT_ACCOUNT_INFO, false);
        setPref(HINT_FIRSTNEWACCOUNT, false);
        setPref(HINT_REGISTER, false);
        setPref(HINT_EDITTRANSACTION, false);
        setPref(HINT_ACCOUNT_VIEW_OPTIONS, false);
        setPref(HINT_REPORTS, false);
        setPref(HINT_ACCOUNT_TYPE_OPTIONS, false);
        setPref(HINT_KEYBOARD, false);
        setPref(HINT_FILETRANSFER, false);
        setPref(HINT_BUDGETS, false);
        setPref(HINT_VIDEOS, false);
        setPref(HINT_REDBADGE, false);
        setPref(HINT_REPEATING, false);
    }

    public static boolean hasPassword() {
        String pass = getStringPref(PASSWORD);
        return (pass != null && pass.length() > 0) && DEBUGGING;
    }

    public static void initialize() {
        if (!getBooleanPref(APP_FIRST_RUN)) {
            Database.populateDatabaseDefaults(SMMoney.getAppContext());
            setPref(QIF_DATEFORMAT, Locales.kLOC_GENERAL_DEFAULT);
            setPref(QIF_DATESEPARATOR, Locales.kLOC_GENERAL_DEFAULT);
            String code = "USD";
            try {
                code = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (code != null) {
                setPref(HOMECURRENCYCODE, code);
                Database.setHomeCurrency(code);
            }
            setPref(DEFAULTROUNDING, DEBUGGING);
            setPref(MULTIPLECURRENCIES, false);
            setPref(UPDATEEXCHANGERATES, DEBUGGING);
            setPref(GROUPBYACCOUNTTYPE, false);
            setPref(ALLTRANSACTIONS, DEBUGGING);
            setPref(FILTERS, false);
            setPref(REPEATINGTRANSACTIONS, DEBUGGING);
            setPref(EDITTRANSACTION_STARTING_FIELD, Locales.kLOC_GENERAL_NONE);
            setPref(EDITTRANSACTION_SHOW_ID_FIELD, DEBUGGING);
            setPref(EDITTRANSACTION_SHOW_CLEARED_FIELD, DEBUGGING);
            setPref(EDITTRANSACTION_SHOW_CATEGORY_FIELD, DEBUGGING);
            setPref(EDITTRANSACTION_SHOW_MEMO_FIELD, DEBUGGING);
            setPref(EDITTRANSACTION_SHOW_CLASS_FIELD, DEBUGGING);
            setPref(SHOWTIME, false);
            setPref(TRANSACTIONS_SHOW_TRANSTOANDTO_FIELD, false);
            setPref(TRANSACTIONS_UNLINK_ID_FIELD, false);
            setPref(ALTBALANCETYPE, false);
            setPref(REPORTS_GROUPSUBCATEGORIES, DEBUGGING);
            setPref(TRANSACTIONS_SHOW_ID_FIELD, DEBUGGING);
            setPref(TRANSACTIONS_SHOW_CATEGORY_FIELD, DEBUGGING);
            setPref(TRANSACTIONS_SHOW_CLASS_FIELD, false);
            setPref(TRANSACTIONS_SHOW_NOTES_FIELD, false);
            setPref(TRANSACTIONS_SHOW_RUNNING_FIELD, DEBUGGING);
            setPref(TRANSACTIONS_SHOW_FOREIGNAMOUNT, false);
            setPref(AUTOADD_LOOKUPS, DEBUGGING);
            setPref(AUTO_FILL, DEBUGGING);
            setPref(PASSWORD, "");
            setPref(PASSWORD_DELAY_LAST, new GregorianCalendar().getTimeInMillis());
            setPref(PASSWORD_DELAY, Locales.kLOC_GENERAL_NONE);
            setPref(BALANCEONDATE, 0L);
            setPref(VIEWACCOUNTS, Enums.kViewAccountsAll/*0*/);
            setPref(NEWESTTRANSACTIONFIRST, Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING);
            setPref(QIF_NUMBERFORMAT, Locales.kLOC_GENERAL_DEFAULT);
            setPref(QIF_MARKALLCLEARED, false);
            setPref(QIF_IMPORT_DUPS, DEBUGGING);
            setPref(QIF_EXPORT_SEPERATELY, false);
            setPref(BALANCETYPE, Enums.kBalanceTypeCurrent/*2*/);
            setPref(BALANCEBARREGISTER, Enums.kBalanceTypeCurrent /*2*/);
            setPref(APP_FIRST_RUN, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U1)) {
            setPref(BALANCEONDATE, 0L);
            setPref(NEWESTTRANSACTIONFIRST, Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING);
            setPref(VIEWACCOUNTS, Enums.kViewAccountsAll/*0*/);
            setPref(HOMECURRENCYCODE, "USD");
            String code = "USD";
            try {
                code = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (code != null) {
                setPref(HOMECURRENCYCODE, code);
                Database.setHomeCurrency(code);
            }
            setPref(APP_FIRST_RUN_U1, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U2)) {
            setPref(ENCODING, "ISO-8859-1");
            setPref(APP_FIRST_RUN_U2, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U3)) {
            setPref(TRANSACTIONS_TRUNCATE_ID, DEBUGGING);
            setPref(APP_FIRST_RUN_U3, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U1_05)) {
            setPref(TRANSACTIONS_SHOW_DATE_FIELD, DEBUGGING);
            setPref(APP_FIRST_RUN_U1_05, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U1_09)) {
            setPref(APP_FIRST_RUN_U1_09, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U1_11)) {
            setPref(PMSYNC_PORT, 9191);
            setPref(APP_FIRST_RUN_U1_11, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U2_0)) {
            setPref(BUDGETSTARTDATE, Locales.kLOC_GENERAL_DEFAULT);
            setPref(COLLAPSE_ASSETS, DEBUGGING);
            setPref(COLLAPSE_BANKS, DEBUGGING);
            setPref(COLLAPSE_CASH, DEBUGGING);
            setPref(COLLAPSE_CREDITCARDS, DEBUGGING);
            setPref(COLLAPSE_CUSTOM, DEBUGGING);
            setPref(COLLAPSE_EXPENSES, DEBUGGING);
            setPref(COLLAPSE_INCOME, DEBUGGING);
            setPref(COLLAPSE_LIABILITIES, DEBUGGING);
            setPref(COLLAPSE_ONLINE, DEBUGGING);
            setPref(COLLAPSE_UNBUDGETED, DEBUGGING);
            setPref(APP_FIRST_RUN_U2_0, DEBUGGING);
        }
        if (!getBooleanPref(APP_FIRST_RUN_U2_0_5)) {
            File f = Environment.getExternalStorageDirectory();
            if (f != null) {
                setPref(EXPORT_STOREDEVICE, f.toString());
            }
            setPref(APP_FIRST_RUN_U2_0_5, DEBUGGING);
        }
    }

    public static String getUUID() {
        String uuid = getSharedPrefs().getString("UUID", "");
        if (uuid.length() != 0) {
            return uuid;
        }
        uuid = UUID.randomUUID().toString();
        setPref("UUID", uuid);
        return uuid;
    }

    public static void replaceIconNamesForUpdate1_11() {
        Cursor c = Database.rawQuery("SELECT accountID FROM accounts", null);
        while (c.moveToNext()) {
            AccountClass account = new AccountClass(c.getInt(0));
            account.hydrate();
            account.setIconFileName(AccountTypeIconGridActivity.replaceIconNameWithUppercase(account.getIconFileName()));
            account.saveToDataBaseAndUpdateTimeStamp(false);
        }
    }

    public static void fixRepeatingTransactionDisconnectionForUpdate1_09() {
        Database.execSQL("DELETE FROM repeatingTransactions WHERE transactionID NOT IN( SELECT transactionID FROM transactions WHERE transactions.deleted = 0)");
    }

    private static void disconnectTransferForTransactionU4(TransactionClass transaction, int transactionsAccountID) {
        Cursor c = Database.rawQuery("SELECT splitID,transferToAccountID FROM splits WHERE transactionID=" + transaction.getTransactionID(), null);
        TransactionClass otherTransaction = null;
        int transFromAccountID = transaction.getTransactionID();
        while (c.moveToNext()) {
            SplitsClass s = new SplitsClass(c.getInt(0));
            int transToAccountID = c.getInt(1);
            Cursor sc = Database.rawQuery("SELECT transactionID FROM transactions WHERE deleted=0 AND accountID=" + transToAccountID + " AND date=" + (transaction.getDate().getTimeInMillis() / 1000) + " AND type<>" + 5, null);
            int transToID = 0;
            while (sc.moveToNext()) {
                transToID = sc.getInt(0);
                otherTransaction = new TransactionClass(transToID);
            }
            Cursor tc = Database.rawQuery("SELECT splitID FROM splits WHERE transactionID=" + transToID, null);
            int splitIndex = 0;
            while (tc.moveToNext()) {
                SplitsClass split = new SplitsClass(tc.getInt(0));
                double transAmount = (-1.0d * split.getAmount()) / split.getXrate();
                double diff = transAmount - split.getAmount();
                if (CurrencyExt.amountAsCurrency(transAmount).equals(CurrencyExt.amountAsCurrency(split.getAmount()))) {
                    diff = 0.0d;
                }
                if (diff >= -9.0E-6d && diff <= 1.0E-6d && transFromAccountID == transToAccountID) {
                    if (otherTransaction.getPayee() == null || otherTransaction.getPayee().length() == 0) {
                        otherTransaction.setPayee("*accountID " + transToAccountID + " - missing account name*");
                    } else if (otherTransaction.getCategoryAtIndex(splitIndex) == null || otherTransaction.getCategoryAtIndex(splitIndex).length() == 0) {
                        otherTransaction.setCategoryAtIndex(otherTransaction.getTransferToAccountAtIndex(splitIndex), splitIndex);
                    }
                    otherTransaction.setTransferToAccountAtIndex(null, splitIndex);
                    otherTransaction.initType();
                    otherTransaction.saveToDatabase();
                }
                splitIndex++;
            }
        }
    }

    public static void fixAccountsForUpdateU4() {
        Cursor c = Database.rawQuery("SELECT transactionID,accountID FROM transactions WHERE accountID IN (SELECT DISTINCT accountID FROM transactions EXCEPT SELECT DISTINCT accountID FROM accounts)", null);
        while (c.moveToNext()) {
            TransactionClass t = new TransactionClass(c.getInt(0));
            disconnectTransferForTransactionU4(t, c.getInt(1));
            t.deleteFromDatabase();
        }
        Database.execSQL("UPDATE repeatingTransactions SET deleted='1', timestamp=" + System.currentTimeMillis() + " WHERE transactionID IN (SELECT transactionID " + "FROM transactions WHERE deleted = 0 AND accountID IN (" + "SELECT DISTINCT accountID FROM transactions " + "EXCEPT " + "SELECT DISTINCT accountID FROM accounts))");
        Database.execSQL("UPDATE transactions SET deleted='1', timestamp=" + System.currentTimeMillis() + " WHERE type=" + 5 + " AND accountID IN (" + "SELECT DISTINCT accountID FROM transactions " + "EXCEPT " + "SELECT DISTINCT accountID FROM accounts)");
    }
}

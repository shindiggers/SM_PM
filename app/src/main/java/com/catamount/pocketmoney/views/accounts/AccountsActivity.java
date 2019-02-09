package com.catamount.pocketmoney.views.accounts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
//import com.android.vending.licensing.LicenseChecker;
//import com.android.vending.licensing.LicenseCheckerCallback;
//import com.android.vending.licensing.LicenseCheckerCallback.ApplicationErrorCode;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.database.AccountDB;
import com.catamount.pocketmoney.database.Database;
import com.catamount.pocketmoney.database.TransactionDB;
import com.catamount.pocketmoney.importexport.ImportExportCSV;
import com.catamount.pocketmoney.importexport.ImportExportQIF;
import com.catamount.pocketmoney.importexport.ImportExportTDF;
import com.catamount.pocketmoney.importexport.ofx.ImportExportOFX;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.Enums;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PMGlobal;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.prefs.MainPrefsActivity;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.BalanceBar;
import com.catamount.pocketmoney.views.HandlerActivity;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.budgets.BudgetsActivity;
import com.catamount.pocketmoney.views.charts.ChartViewDelegate;
import com.catamount.pocketmoney.views.charts.items.ChartItem;
import com.catamount.pocketmoney.views.charts.views.ChartBarView;
import com.catamount.pocketmoney.views.charts.views.ChartView;
import com.catamount.pocketmoney.views.desktopsync.PocketMoneySyncActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.repeating.RepeatingActivity;
import com.catamount.pocketmoney.views.reports.AccountsReportDataSource;
import com.catamount.pocketmoney.views.reports.CategoryReportDataSource;
import com.catamount.pocketmoney.views.reports.ClassReportDataSource;
import com.catamount.pocketmoney.views.reports.PayeeReportDataSource;
import com.catamount.pocketmoney.views.reports.ReportsActivity;
import com.catamount.pocketmoney.views.transactions.TransactionsActivity;
//import com.google.android.gms.ads.AdRequest.Builder;
//import com.google.android.gms.ads.AdView;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

import static android.support.constraint.Constraints.TAG;
import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class AccountsActivity extends PocketMoneyActivity implements HandlerActivity, ChartViewDelegate {
    public static final int ACCOUNT_REQUEST_BUDGET = 2;
    public static final int ACCOUNT_REQUEST_EMAIL = 3;
    public static final int ACCOUNT_REQUEST_FILTER = 1;
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlZQhocxMouDNAC9NuSWdBSxRZi20xvuZMyG1YdvEXIA6gUgbF/JKLKqlbtapkMTk+ssYo3vOOXPbYEtVmBHMjQsohxQ8WORw1EVw/bhsAbvd4rcywqdPAZAKA0Iuv3JSYVzh82w/Wauv4WbhK2P7ALWWXY6enGsZp1CtkGeHhjM2bZpRuiD6JYj9+JHro0559mUkATtGGZlSbSNlnZOkkxfDqBrEyAteRxCx43xixAbScU3SyVAX5xh7QN/0wlVFA37fu9O/iQkffHR+UcOc3VDvTamKYr98wYe/pPLZMbxSEuxKSU5dsdTkTgI2EO67spggzAkKiu33gm86x/dBSwIDAQAB";
    public static boolean DEBUG = false;
    public static boolean IS_GOOGLE_MARKET = false;
    private static final byte[] SALT = new byte[]{(byte) -25, (byte) 23, (byte) -92, (byte) -16, (byte) -78, (byte) -65, (byte) 20, (byte) 65, (byte) 36, (byte) -9, (byte) 18, (byte) -44, (byte) 13, (byte) -81, (byte) -44, (byte) -13, (byte) -4, (byte) 19, (byte) -111, (byte) 73};
    private static AsyncTask initTask = null;
    private final int CMENU_DELETE = ACCOUNT_REQUEST_EMAIL;
    private final int CMENU_EDIT = ACCOUNT_REQUEST_FILTER;
    private final int DIALOG_REPORTS = 10;
    private final int EMAIL_CSV = ACCOUNT_REQUEST_BUDGET;
    private final int EMAIL_OFX = ACCOUNT_REQUEST_EMAIL;
    private final int EMAIL_QIF = 0;
    private final int EMAIL_TDF = ACCOUNT_REQUEST_FILTER;
    private final int IMPORT_PROGRESS_DIALOG = 9;
    private final int LISCENSING = 8;
    private final int MENU_EMAILTRANSFERS = ACCOUNT_REQUEST_EMAIL;
    private final int MENU_FILETRANSFERS = 3;
    private final int MENU_NEW = 1;
    private final int MENU_PREFS = 2;
    private final int MENU_QUIT = 6;
    private final int MENU_REPEATING = 5;
    private final int MENU_SDCARDTRANSFER = 5;
    private final int MENU_SD_EXPORT = 7;
    private final int MENU_SD_IMPORT = 6;
    private final int MENU_SD_IMPORT_CSV = 11;
    private final int MENU_SD_IMPORT_OFX = 14;
    private final int MENU_SD_IMPORT_QIF = 13;
    private final int MENU_SD_IMPORT_TDF = 12;
    private final int MENU_TRANSFER = ACCOUNT_REQUEST_EMAIL;
    private final int MENU_VIEW = 4;
    private final int MENU_WIFITRANSFERS = ACCOUNT_REQUEST_BUDGET;
    private final int MENU_WIFI_EXPORT = 4;
    public final int REQUEST_EDIT = ACCOUNT_REQUEST_BUDGET;
    public final int REQUEST_NEW = ACCOUNT_REQUEST_FILTER;
    RadioButton accountRadioButton;
    private AccountRowAdapter adapter;
    private double availableCreditBalanceCache = 0.0d;
    private double availableFundsBalanceCache = 0.0d;
    private BalanceBar balanceBar;
    private AsyncTask balanceBarTask;
    private ChartView cashFlowChartView;
    private double clearedBalanceCache = 0.0d;
    Context context;
    private double currentBalanceCache = 0.0d;
    private String emailFileLocation;
    private ArrayList<Uri> fileNames;
    private double futureBalanceCache = 0.0d;
    private boolean graphButtonEnabled = true;
    private ImageView graphLeftArrow;
    private TextView graphNetworthTextView;
    private ImageView graphRightArrow;
    private ProgressBar graphSpinner;
    private AsyncTask graphTask;
    private TextView graphTitleTextView;
    boolean launching = false;
    //private LicenseChecker mChecker;
    private Handler mHandler = null;
    //private LicenseCheckerCallback mLicenseCheckerCallback;
    private Button moreChartsButton;
    private int msgEmail = -1;
    private ChartView netWorthChartView;
    boolean progUpdate = false;
    private ProgressDialog progressDialog = null;
    private boolean shouldEmail = false;
    double startTime = 0.0d;
    private ChartView theChartView;
    private FrameLayout theGraphLayout;
    private AlertDialog tipDialog;
    private TextView titleTextMenu;
    private TextView titleTextView;
    private WakeLock wakeLock;

    static class AnonymousClass49 implements OnClickListener {
        private final /* synthetic */ Activity val$c;

        AnonymousClass49(Activity activity) {
            this.val$c = activity;
        }

        public void onClick(DialogInterface dialog, int id) {
            String str;
            String str2 = "android.intent.action.VIEW";
            if (AccountsActivity.IS_GOOGLE_MARKET) {
                str = "http://market.android.com/details?id=com.catamount.pocketmoney";
            } else {
                str = "http://www.amazon.com/gp/product/B004JVI48G";
            }
            this.val$c.startActivity(new Intent(str2, Uri.parse(str)));
        }
    }

    private class BalanceTask extends AsyncTask {
        private int pref;
        private double totalWorth;

        private BalanceTask() {
            this.totalWorth = 0.0d;
            this.pref = 0;
        }

        protected Object doInBackground(Object... params) {
            Log.d("ACCOUNTSACTIVITY","BalanceTask() doInBackground() has just run");
            this.pref = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
            if (this.pref == 5) {
                Prefs.setPref(Prefs.BALANCETYPE, AccountsActivity.ACCOUNT_REQUEST_BUDGET);
                this.pref = AccountsActivity.ACCOUNT_REQUEST_BUDGET;
            }
            this.totalWorth = AccountsActivity.this.totalWorth(this.pref);
            return null;
        }

        protected void onPostExecute(Object result) {
            Log.d("ACCOUNTSACTIVITY","onPostExecute(Object result) has just been triggered");
            AccountsActivity.this.balanceBar.balanceAmountTextView.setVisibility(View.VISIBLE);
            AccountsActivity.this.balanceBar.balanceAmountTextView.setTextColor(this.totalWorth < 0.0d ? -65536 : -1);
            AccountsActivity.this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(this.totalWorth));
            AccountsActivity.this.balanceBar.balanceTypeTextView.setVisibility(View.VISIBLE);
            AccountsActivity.this.balanceBar.balanceTypeTextView.setText(AccountDB.totalWorthLabel(this.pref));
            AccountsActivity.this.balanceBar.balanceTypeTextView.setTextColor(-1);
            AccountsActivity.this.balanceBar.progressBar.setVisibility(View.GONE);
            synchronized (AccountsActivity.this.adapter) {
                AccountsActivity.this.reloadData();
            }
        }
    }

    private class InitTask extends AsyncTask {
        private boolean addedTransactions;

        private InitTask() {
            this.addedTransactions = false;
        }

        protected Object doInBackground(Object... params) {
            Log.d("ACCOUNTSACTIVITY","InitTask() doInBackground - ie update f/x rate - has just run");
            if (Prefs.getBooleanPref(Prefs.UPDATEEXCHANGERATES)) {
                AccountDB.updateExchangeRates();
            }
            Database.deleteUnlinkedRepeatingTransactions();
            this.addedTransactions = TransactionDB.addRepeatingTransactions();
            return null;
        }

        protected void onPostExecute(Object result) {
            synchronized (AccountsActivity.this.adapter) {
                AccountsActivity.this.reloadData();
                AccountsActivity.this.reloadBalanceBar();
                AccountsActivity.this.reloadCharts();
                if (AccountsActivity.this.adapter.getCount() == 0 && (AccountsActivity.this.tipDialog == null || !AccountsActivity.this.tipDialog.isShowing())) {
                    AccountsActivity.this.showMenuDialog();
                }
            }
            AccountsActivity.initTask = null;
        }
    }

    //private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
      //  private MyLicenseCheckerCallback() {
        //}

        public void allow() {
            if (!AccountsActivity.this.isFinishing()) {
                String wtf = "";
            }
        }

        public void dontAllow() {
            if (!AccountsActivity.this.isFinishing()) {
                AccountsActivity.this.showDialog(8);
            }
        }

//        public void applicationError(ApplicationErrorCode errorCode) {
//            if (!AccountsActivity.this.isFinishing()) {
//                String breakokay = new StringBuilder(String.valueOf("")).toString();
//            }
//        }

    private void testTest() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ACCOUNTSACTIVITY", "i =" + ContextCompat.checkSelfPermission(this, "android.permission.READ_PHONE_STATE"));
        Log.d("ACCOUNTSACTIVITY","onCreate() has just run");
        this.wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(26, "AccountsActivity:DoNotDimScreen");
        this.context = this;
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.accounts, null);
        setContentView(layout);
        setupView(layout);
        setResult(ACCOUNT_REQUEST_FILTER);
        getActionBar().setTitle("PocketMoney");
        getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
    }

    protected void onResume() {
        super.onResume();
        Log.d("ACCOUNTSACTIVITY","onResume just called");
        if (isLite(this)) {
            checkLastUpgradeDialog();
        } else if (IS_GOOGLE_MARKET && !DEBUG) {
            checkLicense();
        }
        if (!Prefs.getBooleanPref(Prefs.HINT_WELCOME) && (this.tipDialog == null || !this.tipDialog.isShowing())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(Locales.kLOC_TIP_WELCOME_TITLE);
            alert.setCancelable(false);
            alert.setMessage(Locales.kLOC_TIP_WELCOME);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Prefs.setPref(Prefs.HINT_WELCOME, true);
                    AccountsActivity.this.showMenuDialog();
                    dialog.dismiss();
                }
            });
            this.tipDialog = alert.show();
        }
        if (Prefs.getBooleanPref(Prefs.HINT_WELCOME) && Prefs.getBooleanPref(Prefs.HINT_ACCOUNT_INFO) && !Prefs.getBooleanPref(Prefs.HINT_FIRSTNEWACCOUNT)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(Locales.kLOC_TIP_WELCOMEBACK_TITLE);
            int i = R.string.kLOC_TIP_FIRST_TRANSACTION;
            Object[] objArr = new Object[ACCOUNT_REQUEST_FILTER];
            objArr[0] = this.adapter.getCount() > 0 ? this.adapter.getElements().get(0).getAccount() : "";
            alert.setMessage(getString(i, objArr));
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Prefs.setPref(Prefs.HINT_FIRSTNEWACCOUNT, true);
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        FrameLayout frameLayout = this.theGraphLayout;
        int i2 = (!Prefs.getBooleanPref(Prefs.SHOWSUMMARYCHARTS) || PocketMoney.isLiteVersion()) ? View.GONE : View.VISIBLE;
        frameLayout.setVisibility(i2);
        clearBalanceCache();
        if (initTask == null) {
            initTask = new InitTask();
            initTask.execute();
        }
        testTest();
    }

    private void showMenuDialog() {
        if (this.tipDialog != null) {
            Log.d("ACCOUNTSACTIVITY","showMenuDialog() called and this.tipDialog != null");
            this.tipDialog.isShowing();
        }
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    private void checkLastUpgradeDialog() {
        long createdOn = Prefs.getLongPref(Prefs.CREATED_ON);
        long lastDisplayed = Prefs.getLongPref(Prefs.LAST_UPGRADE_DIALOG);
        if (createdOn == 0) {
            Prefs.setPref(Prefs.CREATED_ON, System.currentTimeMillis());
        } else if (lastDisplayed == 0) {
            if (System.currentTimeMillis() - createdOn > -1702967296) {
                displayLiteDialog(this);
                Prefs.setPref(Prefs.LAST_UPGRADE_DIALOG, System.currentTimeMillis());
            }
        } else if (System.currentTimeMillis() - lastDisplayed > 864000000) {
            displayLiteDialog(this);
            Prefs.setPref(Prefs.LAST_UPGRADE_DIALOG, System.currentTimeMillis());
        }
    }

    private void checkLicense() {
    }

    public void chartViewSelectedItem(ChartView chartView, ChartItem chartItem) {
        int row = this.theChartView.dataSource.rowOfChartItem(chartItem);
        if (row != -1) {
            this.theChartView.dataSource.selectAllDataPointsForRow(row);
            reloadChartHeader(row);
        }
        this.theChartView.invalidate();
    }

    private void reloadChartHeader(int row) {
        if (row == -1) {
            this.graphNetworthTextView.setText("");
            this.graphTitleTextView.setText("");
            return;
        }
        GregorianCalendar selectedDate = this.theChartView.dataSource.dateForRow(row);
        this.graphTitleTextView.setText(new StringBuilder(String.valueOf(this.theChartView.dataSource.title())).append(": ").append(CalExt.descriptionWithYear(selectedDate)).append(" ").append(CalExt.descriptionWithMonth(selectedDate)).toString());
        this.graphNetworthTextView.setText(CurrencyExt.amountAsCurrency(this.theChartView.dataSource.networthForRow(row)));
    }

    public void reloadCharts() {
        this.netWorthChartView.setVisibility(View.GONE);
        this.cashFlowChartView.setVisibility(View.GONE);
        this.moreChartsButton.setVisibility(View.GONE);
        ((View) this.graphSpinner.getParent()).setVisibility(View.VISIBLE);
        this.graphSpinner.setVisibility(View.VISIBLE);
        if (Prefs.getBooleanPref(Prefs.SHOWSUMMARYCHARTS)) {
            switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
                case PocketMoneyThemes.kThemeBlack /*0*/:
                    this.theChartView = this.netWorthChartView;
                    break;
                case ACCOUNT_REQUEST_FILTER /*1*/:
                    this.theChartView = this.cashFlowChartView;
                    break;
                case ACCOUNT_REQUEST_BUDGET /*2*/:
                    this.theChartView = null;
                    break;
            }
            if (this.graphTask != null) {
                this.graphTask.cancel(true);
                this.graphTask = null;
            }
            this.graphTask = new AsyncTask() {
                protected Object doInBackground(Object... arg0) {
                    if (AccountsActivity.this.theChartView != null) {
                        synchronized (AccountsActivity.this.adapter) {
                            //AccountsActivity.this.theChartView.reloadData(true);
                        }
                    }
                    return this;
                }

                protected void onPostExecute(Object result) {
                    synchronized (AccountsActivity.this.adapter) {
                        AccountsActivity.this.graphReloadCallback();
                        AccountsActivity.this.theChartView.reloadData(true);
                    }
                }
            };
            this.graphTask.execute();
        }
    }

    private void graphReloadCallback() {
        if (this.theChartView != null) {
            reloadChartHeader(this.theChartView.dataSource.numberOfDataPointsInSeries(this.theChartView, 0) - 1);
        } else {
            reloadChartHeader(-1);
        }
        if (this.theChartView != null) {
            this.theChartView.invalidate();
        }
        showCorrectChart();
        this.graphButtonEnabled = true;
        this.graphTask = null;
    }

    private void showCorrectChart() {
        ((View) this.graphSpinner.getParent()).setVisibility(View.GONE);
        this.graphSpinner.setVisibility(View.GONE);
        switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                this.netWorthChartView.setVisibility(View.VISIBLE);
                return;
            case ACCOUNT_REQUEST_FILTER /*1*/:
                this.cashFlowChartView.setVisibility(View.VISIBLE);
                return;
            case ACCOUNT_REQUEST_BUDGET /*2*/:
                ((View) this.moreChartsButton.getParent()).setVisibility(View.VISIBLE);
                this.moreChartsButton.setVisibility(View.VISIBLE);
                return;
            default:
                return;
        }
    }

    public void reloadData() {
        this.adapter.setElements(AccountDB.queryOnViewType(Prefs.getIntPref(Prefs.VIEWACCOUNTS)));
        this.adapter.notifyDataSetChanged();
    }

    private View.OnClickListener getBalanceBarClickListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                String str;
                int i = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
                if (v.equals(AccountsActivity.this.balanceBar.nextButton)) {
                    i = AccountsActivity.this.balanceBar.nextBalanceTypeAfter(i);
                    Log.d("ACCOUNTSACTIVITY","'Next' balance bar button clicked");
                } else {
                    i = AccountsActivity.this.balanceBar.nextBalanceTypeBefore(i);
                    Log.d("ACCOUNTSACTIVITY","'Previous' balance bar button clicked");
                }
                if (Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED)) {
                    str = Prefs.BALANCETYPE;
                    Log.d("ACCOUNTSACTIVITY","Balance bar type = 'unified'");
                } else {
                    str = Prefs.BALANCEBARREGISTER;
                    Log.d("ACCOUNTSACTIVITY","Balance bar type != 'unified'");
                }
                Prefs.setPref(str, i);
                AccountsActivity.this.reloadBalanceBar();
                Log.d("ACCOUNTSACTIVITY","reloadBalanceBar() just called");
            }
        };
    }

    public void clearBalanceCache() {
        this.currentBalanceCache = 0.0d;
        this.futureBalanceCache = 0.0d;
        this.availableFundsBalanceCache = 0.0d;
        this.availableCreditBalanceCache = 0.0d;
        this.clearedBalanceCache = 0.0d;
    }

    public void animateBalanceBarBack() {
        reloadBalanceBar();
        TranslateAnimation anim = new TranslateAnimation(0.0f, -200.0f, 0.0f, 0.0f);
        anim.setDuration(1);
        anim.setRepeatCount(0);
        this.balanceBar.balanceView.setAnimation(anim);
        ScaleAnimation scaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
        scaleAnim.setDuration(1);
        scaleAnim.setRepeatCount(0);
        this.balanceBar.balanceView.setAnimation(scaleAnim);
    }

    private double totalWorth(int pref) {
        double totalWorth;
        switch (pref) {
            case Enums.kChartTypeNegativePie /*-1*/:
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                return 0.0d;
            case PocketMoneyThemes.kThemeBlack /*0*/:
                if (this.futureBalanceCache != 0.0d) {
                    return this.futureBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.futureBalanceCache = totalWorth;
                return totalWorth;
            case ACCOUNT_REQUEST_FILTER /*1*/:
                if (this.clearedBalanceCache != 0.0d) {
                    return this.clearedBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.clearedBalanceCache = totalWorth;
                return totalWorth;
            case ACCOUNT_REQUEST_BUDGET /*2*/:
                if (this.currentBalanceCache != 0.0d) {
                    return this.currentBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.currentBalanceCache = totalWorth;
                return totalWorth;
            case ACCOUNT_REQUEST_EMAIL /*3*/:
                if (this.availableFundsBalanceCache != 0.0d) {
                    return this.availableFundsBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.availableFundsBalanceCache = totalWorth;
                return totalWorth;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                if (this.availableCreditBalanceCache != 0.0d) {
                    return this.availableCreditBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.availableCreditBalanceCache = totalWorth;
                return totalWorth;
            default:
                return 0.0d;
        }
    }

    public void reloadBalanceBar() {
        this.balanceBar.balanceAmountTextView.setText("");
        this.balanceBar.balanceTypeTextView.setText("");
        this.balanceBar.balanceAmountTextView.setVisibility(View.GONE);
        this.balanceBar.balanceTypeTextView.setVisibility(View.GONE);
        this.balanceBar.progressBar.setVisibility(View.VISIBLE);
        new BalanceTask().execute();
    }

    private void reloadBalanceBarCallBack(double totalWorth, int pref) {
    }

    private void deleteAccount(final AccountClass account) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(Locales.kLOC_ACCOUNT_DELETE);
        alert.setMessage(Locales.kLOC_ACCOUNT_DELETE_BODY);
        alert.setPositiveButton(Locales.kLOC_GENERAL_DELETE, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AccountsActivity.this.context);
                alert.setTitle(Locales.kLOC_ACCOUNT_DELETE);
                alert.setMessage(Locales.kLOC_ACCOUNT_DELETE_CONFIRM);
                CharSequence charSequence = Locales.kLOC_GENERAL_DELETE;
                final AccountClass accountClass = account;
                alert.setPositiveButton(charSequence, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        TransactionDB.deleteRecordsFromAccount(accountClass.getAccount());
                        TransactionDB.deleteRepeatingRecordsFromAccount(accountClass.getAccount());
                        TransactionDB.deleteRepetaingRecordsFromTransactionForAccount(accountClass.getAccount());
                        accountClass.setDeleted(true);
                        accountClass.saveToDatabase();
                        AccountsActivity.this.reloadData();
                        AccountsActivity.this.reloadBalanceBar();
                    }
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
            }
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    public void setupView(LinearLayout layout) {
        Log.d("ACCOUNTSACTIVITY","setupView() called and started");
        createHandler();
        this.balanceBar = layout.findViewById(R.id.balancebar);
        this.balanceBar.nextButton.setOnClickListener(getBalanceBarClickListener());
        this.balanceBar.previousButton.setOnClickListener(getBalanceBarClickListener());
        ListView listView = layout.findViewById(R.id.the_list);
        listView.setItemsCanFocus(true);
        listView.setVerticalScrollBarEnabled(false);
        this.adapter = new AccountRowAdapter(this);
        listView.setAdapter(this.adapter);
        listView.setFocusable(false);
        this.accountRadioButton = layout.findViewById(R.id.accountsbutton);
        RadioGroup rg = layout.findViewById(R.id.radiogroup);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!AccountsActivity.this.progUpdate) {
                    AccountsActivity.this.startActivityForResult(new Intent(AccountsActivity.this, BudgetsActivity.class), AccountsActivity.ACCOUNT_REQUEST_BUDGET);
                    AccountsActivity.this.overridePendingTransition(0, 0);
                    AccountsActivity.this.progUpdate = true;
                    AccountsActivity.this.accountRadioButton.setChecked(true);
                    AccountsActivity.this.progUpdate = false;
                }
            }
        });
        ((View) rg.getParent()).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        layout.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        listView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.titleTextView = layout.findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        //this.titleTextView.setTextSize(COMPLEX_UNIT_SP, 50);
        FrameLayout theView = layout.findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.titleTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AccountsActivity.this.openOptionsMenu();
            }
        });
        theView.setVisibility(View.GONE);
        this.theGraphLayout = layout.findViewById(R.id.chartframelayout);
        this.graphLeftArrow = layout.findViewById(R.id.graphleftarrow);
        this.graphLeftArrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AccountsActivity.this.graphButtonEnabled) {
                    switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
                        case PocketMoneyThemes.kThemeBlack /*0*/:
                            Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, AccountsActivity.ACCOUNT_REQUEST_FILTER);
                            break;
                        case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                            Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, AccountsActivity.ACCOUNT_REQUEST_BUDGET);
                            break;
                        case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                            Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, 0);
                            break;
                    }
                    AccountsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AccountsActivity.this.reloadCharts();
                        }
                    });
                }
            }
        });
        this.graphRightArrow = layout.findViewById(R.id.graphrightarrow);
        this.graphRightArrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (AccountsActivity.this.graphButtonEnabled) {
                    switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
                        case PocketMoneyThemes.kThemeBlack /*0*/:
                            Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, AccountsActivity.ACCOUNT_REQUEST_BUDGET);
                            break;
                        case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                            Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, 0);
                            break;
                        case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                            Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, AccountsActivity.ACCOUNT_REQUEST_FILTER);
                            break;
                    }
                    AccountsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AccountsActivity.this.reloadCharts();
                        }
                    });
                }
            }
        });
        this.netWorthChartView = (ChartBarView) layout.findViewById(R.id.networthbarchart);
        this.netWorthChartView.delegate = this;
        this.netWorthChartView.dataSource = new NetWorthDataSource(this.adapter);
        this.cashFlowChartView = (ChartBarView) layout.findViewById(R.id.cashflowbarchart);
        this.cashFlowChartView.delegate = this;
        this.cashFlowChartView.dataSource = new CashFlowDataSource(this.adapter);
        this.moreChartsButton = layout.findViewById(R.id.morechartsbutton);
        this.moreChartsButton.setTextColor(-7829368);
        this.moreChartsButton.setText(Locales.kLOC_CHARTS_MORECHARTS);
        this.moreChartsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AccountsActivity.this.showDialog(10);
            }
        });
        this.graphSpinner = layout.findViewById(R.id.graphspinner);
        this.graphTitleTextView = layout.findViewById(R.id.graphtitletextview);
        this.graphNetworthTextView = layout.findViewById(R.id.networthtextview);
    }

    private void importCSVFromSD() {
        final ImportExportCSV importcsv = new ImportExportCSV(new StringBuilder(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())).append("/PocketMoneyBackup/").append("PocketMoney.csv").toString(), this);
        if (importcsv.hasFile()) {
            new Thread() {
                public void run() {
                    importcsv.importIntoDatabase(AccountsActivity.this);
                }
            }.start();
        } else {
            Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
        }
    }

    private void importTDFFromSD() {
        final ImportExportCSV importtdf = new ImportExportCSV(new StringBuilder(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())).append("/PocketMoneyBackup/").append("PocketMoney.txt").toString(), this);
        if (importtdf.hasFile()) {
            new Thread() {
                public void run() {
                    importtdf.importIntoDatabase(AccountsActivity.this);
                }
            }.start();
        } else {
            Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
        }
    }

    private void importQIFFromSD() {
        File[] qifList = new File(new StringBuilder(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())).append("/PocketMoneyBackup").toString()).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".qif") || name.endsWith(".QIF")) && !name.startsWith(".");
            }
        });
        if (qifList != null) {
            for (int i = 0; i < qifList.length; i += ACCOUNT_REQUEST_FILTER) {
                Log.i("Q File Path = ", qifList[i].getAbsolutePath());
                final ImportExportQIF importqif = new ImportExportQIF(qifList[i].getAbsolutePath(), this);
                if (importqif.hasFile()) {
                    new Thread() {
                        public void run() {
                            importqif.importIntoDatabase(AccountsActivity.this);
                            new File(importqif.QIFPath).delete();
                        }
                    }.start();
                } else {
                    Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
                }
            }
            return;
        }
        Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
    }

    private void importOFXFromSD() {
        File[] qifList = new File(new StringBuilder(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())).append("/PocketMoneyBackup").toString()).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".ofx") || name.endsWith(".qfx") || name.endsWith(".OFX") || name.endsWith(".QFX")) && !name.startsWith(".");
            }
        });
        if (qifList != null) {
            for (int i = 0; i < qifList.length; i += ACCOUNT_REQUEST_FILTER) {
                if (qifList[i].exists()) {
                    final ImportExportOFX importofx = new ImportExportOFX(this, qifList[i].getAbsolutePath());
                    new Thread() {
                        public void run() {
                            importofx.importIntoDatabase();
                            File importedFile = new File(importofx.path);
                            AccountsActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    AccountsActivity.this.reloadData();
                                }
                            });
                        }
                    }.start();
                } else {
                    Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
                }
            }
            return;
        }
        Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
    }

    private void restoreFromSD() {
        AlertDialog.Builder alert2 = new AlertDialog.Builder(this.context);
        alert2.setTitle(Locales.kLOC_TOOLS_RESTORE_SD);
        alert2.setMessage(Locales.kLOC_TRANSFERS_RESTORE);
        alert2.setPositiveButton(Locales.kLOC_GENERAL_YES, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AccountsActivity.this.context);
                alert.setTitle(Locales.kLOC_TOOLS_RESTORE_SD);
                alert.setMessage(Locales.kLOC_TRANSFERS_RESTORE_CONFIRM);
                alert.setPositiveButton(Locales.kLOC_GENERAL_YES, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Database.closeDBAndNullify();
                        Prefs.importDB(AccountsActivity.this);
                        Database.loadDatabasePreferences();
                        Prefs.initialize();
                        AccountsActivity.this.reloadData();
                        AccountsActivity.this.reloadBalanceBar();
                    }
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_NO, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
        alert2.setNegativeButton(Locales.kLOC_GENERAL_NO, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert2.show();
    }

    private void exportCSVToSD() {
        new Thread() {
            public void run() {
                ImportExportCSV exportcsv = new ImportExportCSV(AccountsActivity.this);
                exportcsv.setFilter(new FilterClass());
                exportcsv.exportRecords();
            }
        }.start();
    }

    private void exportTDFToSD() {
        new Thread() {
            public void run() {
                ImportExportTDF exporttdf = new ImportExportTDF(AccountsActivity.this);
                exporttdf.setFilter(new FilterClass());
                exporttdf.exportRecords();
            }
        }.start();
    }

    private void exportQIFToSD() {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.show();
        new Thread() {
            public void run() {
                boolean exportSeperately = Prefs.getBooleanPref(Prefs.QIF_EXPORT_SEPERATELY);
                String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                FilterClass filter = new FilterClass();
                if (exportSeperately) {
                    ArrayList<String> accNames = new ArrayList();
                    Iterator<AccountClass> iterator = AccountsActivity.this.adapter.getElements().iterator();
                    while (iterator.hasNext()) {
                        try {
                            accNames.add(iterator.next().getAccount());
                        } catch (Exception exAcc) {
                            Log.i("***** Doing this ERROR:", exAcc.toString());
                        }
                    }
                    String[] array = accNames.toArray(new String[accNames.size()]);
                    int length = array.length;
                    for (int i = 0; i < length; i += AccountsActivity.ACCOUNT_REQUEST_FILTER) {
                        String item = array[i];
                        filter.setAccount(item);
                        ArrayList query = TransactionDB.queryWithFilter(filter);
                        String fileName = new StringBuilder(String.valueOf(fileDir)).append("/PocketMoneyBackup/").append(item).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).append(".qif").toString();
                        ImportExportQIF exportqif = new ImportExportQIF(AccountsActivity.this);
                        exportqif.QIFPath = fileName;
                        exportqif.accountNameBeingImported = item;
                        exportqif.setFilter(filter);
                        exportqif.exportRecords(query);
                    }
                } else {
                    new ImportExportQIF(AccountsActivity.this).exportRecords();
                }
                try {
                    pd.dismiss();
                } catch (Exception e) {
                }
            }
        }.start();
    }

    private void exportOFXToSD() {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.show();
        new Thread() {
            public void run() {
                String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                FilterClass filter = new FilterClass();
                Iterator it = AccountsActivity.this.adapter.getElements().iterator();
                while (it.hasNext()) {
                    AccountClass account = (AccountClass) it.next();
                    filter.setAccount(account.getAccount());
                    ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(filter);
                    ImportExportOFX exportofx = new ImportExportOFX(AccountsActivity.this.context, new StringBuilder(String.valueOf(fileDir)).append("/PocketMoneyBackup/").append(account.getAccount()).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).append(".qfx").toString());
                    exportofx.accountNameBeingImported = account.getAccount();
                    exportofx.filter = filter;
                    exportofx.exportRecords(query);
                }
                pd.dismiss();
            }
        }.start();
    }

    private void backupToSD() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.context);
        alert.setTitle(Locales.kLOC_TOOLS_BACKUP_SD);
        alert.setMessage(Locales.kLOC_TRANSFERS_BACKUPFILE_INFO);
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Prefs.exportDB(AccountsActivity.this);
            }
        });
        alert.show();
    }

    private void generateCSVForEmail() {
        String pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        this.msgEmail = ACCOUNT_REQUEST_BUDGET;
        this.shouldEmail = true;
        this.emailFileLocation = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append("PocketMoney.csv").toString();
        final String fl3 = this.emailFileLocation;
        new Thread() {
            public void run() {
                ImportExportCSV exportcsv = new ImportExportCSV(AccountsActivity.this);
                exportcsv.CSVPath = fl3;
                exportcsv.setFilter(new FilterClass());
                exportcsv.exportRecords();
            }
        }.start();
    }

    private void generateTDFForEmail() {
        String pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        this.msgEmail = ACCOUNT_REQUEST_FILTER;
        this.shouldEmail = true;
        this.emailFileLocation = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append("PocketMoney.txt").toString();
        final String fl2 = this.emailFileLocation;
        new Thread() {
            public void run() {
                ImportExportTDF exporttdf = new ImportExportTDF(AccountsActivity.this);
                exporttdf.CSVPath = fl2;
                exporttdf.setFilter(new FilterClass());
                exporttdf.exportRecords();
            }
        }.start();
    }

    private void generateEmailForQIF(ArrayList<Uri> fileNames) {
        Intent emailIntent = new Intent("android.intent.action.SEND_MULTIPLE");
        emailIntent.setType("text/qif");
        Prefs.exportDB(this);
        emailIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", fileNames);
        int i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
        Object[] objArr = new Object[ACCOUNT_REQUEST_FILTER];
        objArr[0] = "QIF";
        emailIntent.putExtra("android.intent.extra.SUBJECT", getString(i, objArr));
        i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
        objArr = new Object[ACCOUNT_REQUEST_BUDGET];
        objArr[0] = "QIF";
        objArr[ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
        emailIntent.putExtra("android.intent.extra.TEXT", getString(i, objArr));
        this.fileNames = fileNames;
        startActivityForResult(emailIntent, ACCOUNT_REQUEST_EMAIL);
    }

    private void generateQIFForEmail() {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.show();
        new Thread() {
            public void run() {
                boolean exportSeperately = Prefs.getBooleanPref(Prefs.QIF_EXPORT_SEPERATELY);
                String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                AccountsActivity.this.msgEmail = 0;
                AccountsActivity.this.shouldEmail = true;
                FilterClass filter = new FilterClass();
                ImportExportQIF exportqif;
                if (exportSeperately) {
                    final ArrayList<Uri> fileNames = new ArrayList();
                    ArrayList<String> accNames = new ArrayList();
                    Iterator<AccountClass> iterator = AccountsActivity.this.adapter.getElements().iterator();
                    while (iterator.hasNext()) {
                        try {
                            accNames.add(iterator.next().getAccount());
                        } catch (Exception exAcc) {
                            Log.i("***** Doing this ERROR:", exAcc.toString());
                        }
                    }
                    String[] array = accNames.toArray(new String[accNames.size()]);
                    int length = array.length;
                    for (int i = 0; i < length; i += AccountsActivity.ACCOUNT_REQUEST_FILTER) {
                        String item = array[i];
                        filter.setAccount(item);
                        ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(filter);
                        String fileName = new StringBuilder(String.valueOf(fileDir)).append("/PocketMoneyBackup/").append(item).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).append(".qif").toString();
                        fileNames.add(Uri.parse("file://" + fileName));
                        exportqif = new ImportExportQIF(AccountsActivity.this);
                        exportqif.QIFPath = fileName;
                        exportqif.accountNameBeingImported = item;
                        exportqif.setFilter(filter);
                        exportqif.exportRecords(query);
                    }
                    AccountsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AccountsActivity.this.generateEmailForQIF(fileNames);
                        }
                    });
                } else {
                    AccountsActivity.this.emailFileLocation = new StringBuilder(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())).append("/PocketMoneyBackup/PocketMoney.qif").toString();
                    String fl = AccountsActivity.this.emailFileLocation;
                    exportqif = new ImportExportQIF(AccountsActivity.this);
                    exportqif.QIFPath = fl;
                    exportqif.exportRecords();
                }
                pd.dismiss();
            }
        }.start();
    }

    private void generateEmailForOFX(ArrayList<Uri> fileNames) {
        Intent emailIntent = new Intent("android.intent.action.SEND_MULTIPLE");
        emailIntent.setType("text/ofx");
        Prefs.exportDB(this);
        emailIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", fileNames);
        emailIntent.putExtra("android.intent.extra.SUBJECT", "PocketMoney OFX/QFX File");
        int i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
        Object[] objArr = new Object[ACCOUNT_REQUEST_BUDGET];
        objArr[0] = "OFX/QFX";
        objArr[ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
        emailIntent.putExtra("android.intent.extra.TEXT", getString(i, objArr[0],objArr[1]));
        this.fileNames = fileNames;
        startActivityForResult(emailIntent, ACCOUNT_REQUEST_EMAIL);
    }

    private void generateOFXForEmail() {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.show();
        new Thread() {
            public void run() {
                String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                FilterClass filter = new FilterClass();
                final ArrayList<Uri> fileNames = new ArrayList();
                Iterator it = AccountsActivity.this.adapter.getElements().iterator();
                while (it.hasNext()) {
                    AccountClass account = (AccountClass) it.next();
                    filter.setAccount(account.getAccount());
                    ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(filter);
                    String fileName = new StringBuilder(String.valueOf(fileDir)).append("/PocketMoneyBackup/").append(account.getAccount()).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).append(".qfx").toString();
                    fileNames.add(Uri.parse("file://" + fileName));
                    ImportExportOFX exportofx = new ImportExportOFX(AccountsActivity.this.context, fileName);
                    exportofx.accountNameBeingImported = account.getAccount();
                    exportofx.filter = filter;
                    exportofx.exportRecords(query);
                }
                pd.dismiss();
                AccountsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AccountsActivity.this.generateEmailForOFX(fileNames);
                    }
                });
            }
        }.start();
    }

    private void generateBackupForEmail() {
        Intent emailIntent = new Intent("android.intent.action.SEND");
        emailIntent.setType("text/plain");
        Prefs.exportDB(this);
        emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/PocketMoneyBackup/PocketMoneyDB.sql"));
        emailIntent.putExtra("android.intent.extra.SUBJECT", "PocketMoney Backup File");
        int i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
        Object[] objArr = new Object[ACCOUNT_REQUEST_BUDGET];
        objArr[0] = Locales.kLOC_TOOLS_BACKUPFILES;
        objArr[ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
        emailIntent.putExtra("android.intent.extra.TEXT", getString(i, objArr[0],objArr[1]));
        startActivity(emailIntent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("ACCOUNTSACTIVITY","Menu has just been opened");
        menu.add(0, ACCOUNT_REQUEST_FILTER, 0, Locales.kLOC_ACCOUNT_NEW).setIcon(R.drawable.abouticon);
        menu.add(0, ACCOUNT_REQUEST_BUDGET, 0, Locales.kLOC_GENERAL_PREFERENCES).setIcon(R.drawable.abouticon);
        menu.add(0, ACCOUNT_REQUEST_EMAIL, 0, Locales.kLOC_TOOLS_FILETRANSFERS).setIcon(R.drawable.abouticon);
        menu.add(0, 4, 0, "View Options").setIcon(R.drawable.abouticon);
        menu.add(0, 5, 0, Locales.kLOC_REPEATING_TRANSACTIONS).setIcon(R.drawable.abouticon);
        menu.add(0, 6, 0, Locales.kLOC_GENERAL_QUIT).setIcon(R.drawable.abouticon);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_NEW /*1*/:
                if (!isLite(this) || this.adapter.getElements().size() < ACCOUNT_REQUEST_BUDGET) {
                    Intent i = new Intent(this, AccountsEditActivity.class);
                    i.putExtra("Account", new AccountClass());
                    startActivity(i);
                    return true;
                }
                displayLiteDialog(this);
                return true;
            case MENU_PREFS /*2*/:
                startActivity(new Intent(this, MainPrefsActivity.class));
                return true;
            case MENU_FILETRANSFERS /*3*/:
                showDialog(ACCOUNT_REQUEST_FILTER);
                return true;
            case MENU_VIEW /*4*/:
                startActivity(new Intent(this, AccountsViewOptionsActivity.class));
                return true;
            case MENU_REPEATING /*5*/:
                startActivity(new Intent(this, RepeatingActivity.class));
                return true;
            case MENU_QUIT /*6*/:
                Prefs.setPref(Prefs.SHUTTINGDOWN, true);
                setResult(ACCOUNT_REQUEST_FILTER);
                finish();
                break;
        }
        return false;
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        switch (id) {
            case ACCOUNT_REQUEST_FILTER /*1*/:
                CharSequence[] items = new CharSequence[ACCOUNT_REQUEST_EMAIL];
                items[0] = Locales.kLOC_TOOLS_FILETRANSFERS_EMAIL;
                items[ACCOUNT_REQUEST_FILTER] = Locales.kLOC_TOOLS_FILETRANSFERS_SDCARD;
                items[ACCOUNT_REQUEST_BUDGET] = Locales.kLOC_DESKTOPSYNC_TITLE;
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setItems(items, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                AccountsActivity.this.showDialog(AccountsActivity.ACCOUNT_REQUEST_EMAIL);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                AccountsActivity.this.showDialog(5);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                AccountsActivity.this.context.startActivity(new Intent(AccountsActivity.this.context, PocketMoneySyncActivity.class));
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case ACCOUNT_REQUEST_BUDGET /*2*/:
                CharSequence[] items2 = new CharSequence[]{Locales.kLOC_TOOLS_BACKUP, Locales.kLOC_TOOLS_RESTORE, Locales.kLOC_TOOLS_IMPORT, Locales.kLOC_TOOLS_EXPORT};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS_WIFI);
                builder.setItems(items2, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                                AccountsActivity.this.showDialog(4);
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case ACCOUNT_REQUEST_EMAIL /*3*/:
                CharSequence[] items3 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX", Locales.kLOC_TOOLS_BACKUPFILES};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS_EMAIL);
                builder.setItems(items3, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                AccountsActivity.this.generateQIFForEmail();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                AccountsActivity.this.generateTDFForEmail();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                AccountsActivity.this.generateCSVForEmail();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                                AccountsActivity.this.generateOFXForEmail();
                                return;
                            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                                AccountsActivity.this.generateBackupForEmail();
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                CharSequence[] items4 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setItems(items4, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                new ImportExportQIF(AccountsActivity.this).exportRecords();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                ImportExportTDF exporttdf = new ImportExportTDF(AccountsActivity.this);
                                exporttdf.setFilter(new FilterClass());
                                exporttdf.exportRecords();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                ImportExportCSV exportcsv = new ImportExportCSV(AccountsActivity.this);
                                exportcsv.setFilter(new FilterClass());
                                exportcsv.exportRecords();
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                CharSequence[] items5 = new CharSequence[]{Locales.kLOC_TOOLS_BACKUP_SD, Locales.kLOC_TOOLS_RESTORE_SD, Locales.kLOC_TOOLS_IMPORT_SD, Locales.kLOC_TOOLS_EXPORT_SD};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS_SDCARD);
                builder.setItems(items5, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                AccountsActivity.this.backupToSD();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                AccountsActivity.this.restoreFromSD();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                AccountsActivity.this.showDialog(6);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                                AccountsActivity.this.showDialog(7);
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                CharSequence[] items7 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setItems(items7, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                AccountsActivity.this.showDialog(13);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                AccountsActivity.this.showDialog(12);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                AccountsActivity.this.showDialog(11);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                                AccountsActivity.this.showDialog(14);
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case LookupsListActivity.ID_LOOKUP /*7*/:
                CharSequence[] items6 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setItems(items6, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                AccountsActivity.this.exportQIFToSD();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                AccountsActivity.this.exportTDFToSD();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                AccountsActivity.this.exportCSVToSD();
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                                AccountsActivity.this.exportOFXToSD();
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                return new AlertDialog.Builder(this).setTitle("Application not licensed").setMessage("This application is not licensed. Please purchase it from Android Market.").setPositiveButton("Buy app", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AccountsActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://market.android.com/details?id=" + AccountsActivity.this.getPackageName())));
                        AccountsActivity.this.finish();
                    }
                }).setNegativeButton("Quit", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent();
                        i.setAction("android.intent.action.MAIN");
                        i.addCategory("android.intent.category.HOME");
                        AccountsActivity.this.context.startActivity(i);
                    }
                }).setCancelable(false).create();
            case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                this.progressDialog = new ProgressDialog(this);
                this.progressDialog.setProgressStyle(ACCOUNT_REQUEST_FILTER);
                this.progressDialog.setMessage("Transferring...\n\nWarning: This may take several minutes");
                this.progressDialog.setCancelable(true);
                return this.progressDialog;
            case LookupsListActivity.FILTER_DATES /*10*/:
                return new AlertDialog.Builder(this).setTitle("").setItems(new CharSequence[]{Locales.kLOC_TOOLS_ACCOUNTREPORT, Locales.kLOC_TOOLS_CATEGORYREPORT, Locales.kLOC_TOOLS_CLASSREPORT, Locales.kLOC_TOOLS_PAYEEREPORT}, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FilterClass f = new FilterClass();
                        ArrayList<TransactionClass> transactions = TransactionDB.queryWithFilter(f);
                        Intent i;
                        switch (which) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                AccountsReportDataSource ds = new AccountsReportDataSource(transactions, f);
                                i = new Intent(AccountsActivity.this, ReportsActivity.class);
                                PMGlobal.datasource = ds;
                                AccountsActivity.this.startActivity(i);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                CategoryReportDataSource ds2 = new CategoryReportDataSource(transactions, f);
                                i = new Intent(AccountsActivity.this, ReportsActivity.class);
                                PMGlobal.datasource = ds2;
                                AccountsActivity.this.startActivity(i);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                ClassReportDataSource ds3 = new ClassReportDataSource(transactions, f);
                                i = new Intent(AccountsActivity.this, ReportsActivity.class);
                                PMGlobal.datasource = ds3;
                                AccountsActivity.this.startActivity(i);
                                return;
                            case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                                PayeeReportDataSource ds4 = new PayeeReportDataSource(transactions, f);
                                i = new Intent(AccountsActivity.this, ReportsActivity.class);
                                PMGlobal.datasource = ds4;
                                AccountsActivity.this.startActivity(i);
                                return;
                            default:
                                return;
                        }
                    }
                }).create();
            case LookupsListActivity.FILTER_PAYEES /*11*/:
                builder = new AlertDialog.Builder(this.context);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setMessage("The file 'PocketMoney.csv' should be placed in the folder '/Download/PocketMoneyBackup'");
                builder.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        AccountsActivity.this.importCSVFromSD();
                    }
                });
                builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                return builder.create();
            case LookupsListActivity.FILTER_IDS /*12*/:
                builder = new AlertDialog.Builder(this.context);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setMessage("The file to 'PocketMoney.txt' should be placed in the folder '/Download/PocketMoneyBackup'");
                builder.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        AccountsActivity.this.importTDFFromSD();
                    }
                });
                builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                return builder.create();
            case LookupsListActivity.FILTER_CLEARED /*13*/:
                builder = new AlertDialog.Builder(this.context);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setMessage("Place the *.qif file(s) in the folder '/Download/PocketMoneyBackup'\n\nMake sure to select the correct file format in the preferences");
                builder.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        AccountsActivity.this.importQIFFromSD();
                    }
                });
                builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                return builder.create();
            case LookupsListActivity.FILTER_CATEGORIES /*14*/:
                builder = new AlertDialog.Builder(this.context);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setMessage("Place the *.ofx file in the folder '/Download/PocketMoneyBackup'.\n\nWarning: Make sure to select the correct file format in the preferences");
                builder.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        AccountsActivity.this.importOFXFromSD();
                    }
                });
                builder.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                return builder.create();
            default:
                return null;
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AccountRowHolder aHolder = (AccountRowHolder) v.getTag();
        Intent i = new Intent();
        i.putExtra("Account", aHolder.account);
        MenuItem item = menu.add(0, ACCOUNT_REQUEST_FILTER, 0, Locales.kLOC_GENERAL_EDIT);
        item.setIcon(R.drawable.abouticon);
        item.setIntent(i);
        item = menu.add(0, ACCOUNT_REQUEST_EMAIL, 0, Locales.kLOC_GENERAL_DELETE);
        item.setIcon(R.drawable.abouticon);
        item.setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case ACCOUNT_REQUEST_FILTER /*1*/:
                Intent it = new Intent(this, AccountsEditActivity.class);
                it.putExtra("Account", (AccountClass) b.get("Account"));
                startActivity(it);
                return true;
            case ACCOUNT_REQUEST_EMAIL /*3*/:
                deleteAccount((AccountClass) b.get("Account"));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            switch (requestCode) {
                case ACCOUNT_REQUEST_FILTER /*1*/:
                    if (resultCode == ACCOUNT_REQUEST_FILTER) {
                        Intent i = new Intent(this, TransactionsActivity.class);
                        i.putExtra("Filter", (FilterClass) data.getExtras().get("Filter"));
                        startActivity(i);
                        return;
                    }
                    return;
                case ACCOUNT_REQUEST_BUDGET /*2*/:
                    if (Prefs.getBooleanPref(Prefs.SHUTTINGDOWN)) {
                        setResult(ACCOUNT_REQUEST_FILTER);
                        finish();
                        return;
                    }
                    return;
                case ACCOUNT_REQUEST_EMAIL /*3*/:
                    Iterator it = this.fileNames.iterator();
                    while (it.hasNext()) {
                        if (!new File(((Uri) it.next()).getPath()).delete()) {
                            int i2 = ACCOUNT_REQUEST_FILTER + ACCOUNT_REQUEST_FILTER;
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void displayError(String msg) {
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Error");
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setButton(-1, "OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public Handler getHandler() {
        if (this.mHandler == null) {
            createHandler();
        }
        return this.mHandler;
    }

    private void createHandler() {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                        AccountsActivity.this.animateBalanceBarBack();
                        break;
                    case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                        break;
                    case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                        if (!AccountsActivity.this.shouldEmail && msg.obj.getClass().equals(String.class)) {
                            Toast.makeText(AccountsActivity.this.context, (String) msg.obj, Toast.LENGTH_LONG).show();
                        }
                        try {
                            AccountsActivity.this.wakeLock.release();
                        } catch (Exception e) {
                        }
                        if (AccountsActivity.this.progressDialog != null) {
                            AccountsActivity.this.progressDialog.dismiss();
                            AccountsActivity.this.progressDialog.setProgress(0);
                        }
                        if (AccountsActivity.this.shouldEmail) {
                            Intent emailIntent = new Intent("android.intent.action.SEND");
                            AccountsActivity accountsActivity;
                            int i;
                            Object[] objArr;
                            switch (AccountsActivity.this.msgEmail) {
                                case PocketMoneyThemes.kThemeBlack /*0*/:
                                    emailIntent.setType("text/qif");
                                    emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + AccountsActivity.this.emailFileLocation));
                                    Log.i("QQQQQQQQ  ", AccountsActivity.this.emailFileLocation);
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_FILTER];
                                    objArr[0] = "QIF";
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArr));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_BUDGET];
                                    objArr[0] = "QIF";
                                    objArr[AccountsActivity.ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArr));
                                    AccountsActivity.this.startActivity(Intent.createChooser(emailIntent, "CHOOSE EMAIL CLIENT"));
                                    break;
                                case AccountsActivity.ACCOUNT_REQUEST_FILTER /*1*/:
                                    emailIntent.setType("text/txt");
                                    emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + AccountsActivity.this.emailFileLocation));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_FILTER];
                                    objArr[0] = "TDF";
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArr));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_BUDGET];
                                    objArr[0] = "TDF";
                                    objArr[AccountsActivity.ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArr));
                                    AccountsActivity.this.startActivity(emailIntent);
                                    break;
                                case AccountsActivity.ACCOUNT_REQUEST_BUDGET /*2*/:
                                    emailIntent.setType("text/csv");
                                    emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + AccountsActivity.this.emailFileLocation));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_FILTER];
                                    objArr[0] = "CSV";
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArr));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_BUDGET];
                                    objArr[0] = "CSV";
                                    objArr[AccountsActivity.ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArr));
                                    AccountsActivity.this.startActivity(emailIntent);
                                    break;
                                case AccountsActivity.ACCOUNT_REQUEST_EMAIL /*3*/:
                                    emailIntent.setType("text/ofx");
                                    emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + AccountsActivity.this.emailFileLocation));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_FILTER];
                                    objArr[0] = "OFX/QFX";
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArr));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    objArr = new Object[AccountsActivity.ACCOUNT_REQUEST_BUDGET];
                                    objArr[0] = "OFX/QFX";
                                    objArr[AccountsActivity.ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArr));
                                    AccountsActivity.this.startActivity(emailIntent);
                                    break;
                            }
                            AccountsActivity.this.shouldEmail = false;
                            return;
                        }
                        AccountsActivity.this.reloadData();
                        return;
                    case LookupsListActivity.CLASS_LOOKUP /*6*/:
                        if (AccountsActivity.this.progressDialog != null && AccountsActivity.this.progressDialog.isShowing()) {
                            AccountsActivity.this.progressDialog.dismiss();
                            AccountsActivity.this.progressDialog.setProgress(0);
                        }
                        AlertDialog alert = new AlertDialog.Builder(AccountsActivity.this.context).create();
                        alert.setTitle("Error");
                        alert.setMessage((String) msg.obj);
                        alert.setCancelable(false);
                        alert.setButton(-1, "OK", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message id " + msg.what);
                }
                if (AccountsActivity.this.progressDialog == null || !AccountsActivity.this.progressDialog.isShowing()) {
                    AccountsActivity.this.showDialog(9);
                    try {
                        AccountsActivity.this.wakeLock.acquire();
                    } catch (Exception e2) {
                    }
                }
                if (AccountsActivity.this.progressDialog != null && AccountsActivity.this.progressDialog.isShowing()) {
                    AccountsActivity.this.progressDialog.setProgress(msg.arg1);
                }
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        Prefs.setPref(Prefs.SHUTTINGDOWN, true);
        setResult(ACCOUNT_REQUEST_FILTER);
        finish();
        return true;
    }

    public static void displayLiteDialog(Activity c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(Locales.kLOC_LITE_UPGRADE).setMessage(Locales.kLOC_LITE_UPGRADE_BODY_ANDROID).setCancelable(false).setPositiveButton(Locales.kLOC_LITE_BUYIT, new AnonymousClass49(c)).setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public static boolean isLite(Context c) {
        return c.getPackageName().toLowerCase().contains("lite");
    }
}

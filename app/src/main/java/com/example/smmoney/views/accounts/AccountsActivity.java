package com.example.smmoney.views.accounts;

import static androidx.core.content.FileProvider.getUriForFile;
import static com.example.smmoney.SMMoney.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.importexport.ImportExportCSV;
import com.example.smmoney.importexport.ImportExportQIF;
import com.example.smmoney.importexport.ImportExportTDF;
import com.example.smmoney.importexport.ofx.ImportExportOFX;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.prefs.MainPrefsActivity;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.BalanceBar;
import com.example.smmoney.views.HandlerActivity;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.PocketMoneyProgressDialog;
import com.example.smmoney.views.budgets.BudgetsActivity;
import com.example.smmoney.views.charts.ChartViewDelegate;
import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.charts.views.ChartView;
import com.example.smmoney.views.desktopsync.PocketMoneySyncActivity;
import com.example.smmoney.views.repeating.RepeatingActivity;
import com.example.smmoney.views.reports.AccountsReportDataSource;
import com.example.smmoney.views.reports.CategoryReportDataSource;
import com.example.smmoney.views.reports.ClassReportDataSource;
import com.example.smmoney.views.reports.PayeeReportDataSource;
import com.example.smmoney.views.reports.ReportsActivity;
import com.example.smmoney.views.transactions.TransactionsActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.android.vending.licensing.LicenseChecker;
//import com.android.vending.licensing.LicenseCheckerCallback;
//import com.android.vending.licensing.LicenseCheckerCallback.ApplicationErrorCode;
//import com.google.android.gms.ads.AdRequest.Builder;
//import com.google.android.gms.ads.AdView;

public class AccountsActivity extends PocketMoneyActivity implements
        HandlerActivity, ChartViewDelegate,
        DialogFragmentEmailTransfers.DialogEmailTransferListener,
        DialogFragmentFileTransfer.DialogFileTransferListener,
        DialogFragmentSdImport.DialogSdImportListener,
        DialogFragmentSdExport.DialogSdExportListener,
        DialogFragmentSdImportQIF.DialogSdImportQIFListener,
        DialogFragmentSdImportTDF.DialogSdImportTDFListener,
        DialogFragmentSdImportCSV.DialogSdImportCSVListener,
        DialogFragmentSdImportOFX.DialogSdImportOFXListener,
        DialogFragmentLocalStorageTransfers.DialogLocalStorageTransferListener {
    private static final int ACCOUNT_REQUEST_BUDGET = 2;
    private static final int ACCOUNT_REQUEST_EMAIL = 3;
    private static final int ACCOUNT_REQUEST_FILTER = 1;
    private static final int PERMISSION_BACKUP_QIF = 107;
    private static final int PERMISSION_BACKUP_TDF = 108;
    private static final int PERMISSION_BACKUP_CSV = 109;
    private static final int PERMISSION_BACKUP_OFX = 110;
    private static final int PERMISSION_RESTORE_CSV = 111;
    private static final int PERMISSION_RESTORE_TDF = 112;
    private static final int PERMISSION_RESTORE_QIF = 113;
    private static final int PERMISSION_RESTORE_OFX = 114;
    //private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlZQhocxMouDNAC9NuSWdBSxRZi20xvuZMyG1YdvEXIA6gUgbF/JKLKqlbtapkMTk+ssYo3vOOXPbYEtVmBHMjQsohxQ8WORw1EVw/bhsAbvd4rcywqdPAZAKA0Iuv3JSYVzh82w/Wauv4WbhK2P7ALWWXY6enGsZp1CtkGeHhjM2bZpRuiD6JYj9+JHro0559mUkATtGGZlSbSNlnZOkkxfDqBrEyAteRxCx43xixAbScU3SyVAX5xh7QN/0wlVFA37fu9O/iQkffHR+UcOc3VDvTamKYr98wYe/pPLZMbxSEuxKSU5dsdTkTgI2EO67spggzAkKiu33gm86x/dBSwIDAQAB";
    public static final boolean DEBUG = false;
    public static final boolean IS_GOOGLE_MARKET = false;
    private static boolean initTaskRunning = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int DIALOG_REPORTS = 10;
    private final int EMAIL_CSV = 2;
    private final int EMAIL_OFX = 3;
    private final int EMAIL_QIF = 0;
    private final int EMAIL_TDF = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EMAIL_BACKUP = 4;
    private final int IMPORT_PROGRESS_DIALOG = 9;
    @SuppressWarnings("FieldCanBeLocal")
    private final int LISCENSING = 8;
    //private final int MENU_EMAILTRANSFERS = 3;
    //private final int DIALOG_FILETRANSFERS = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_FILETRANSFERS = 3;
    private final int MENU_NEW = 1;
    private final int MENU_PREFS = 2;
    private final int MENU_QUIT = 6;
    private final int MENU_REPEATING = 5;
    //private final int MENU_SDCARDTRANSFER = 5;
    //private final int MENU_SD_EXPORT = 7;
    //private final int MENU_SD_IMPORT = 6;
    //private final int MENU_SD_IMPORT_CSV = 11;
    //private final int MENU_SD_IMPORT_OFX = 14;
    //private final int MENU_SD_IMPORT_QIF = 13;
    //private final int MENU_SD_IMPORT_TDF = 12;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_TRANSFER = 3;
    private final int MENU_VIEW = 4;
    //private final int MENU_WIFITRANSFERS = 2;
    //private final int MENU_WIFI_EXPORT = 4;
    private final int PERMISSION_EMAIL_QIF = 100;
    private final int PERMISSION_EMAIL_TDF = 101;
    private final int PERMISSION_EMAIL_CSV = 102;
    private final int PERMISSION_EMAIL_OFX = 103;
    private final int PERMISSION_EMAIL_DB = 104;
    private final int PERMISSION_BACKUP_DB = 105;
    private final int PERMISSION_RESTORE_DB = 106;
    //public final int REQUEST_EDIT = 2;
    //public final int REQUEST_NEW = 1;
    private RadioButton accountRadioButton;
    private AccountRowAdapter adapter;
    private double availableCreditBalanceCache = 0.0d;
    private double availableFundsBalanceCache = 0.0d;
    private BalanceBar balanceBar;
    //private AsyncTask balanceBarTask;
    private ChartView cashFlowChartView;
    private double clearedBalanceCache = 0.0d;
    private Context context;
    private double currentBalanceCache = 0.0d;
    private String emailFileLocation;
    private ArrayList<Uri> fileNames;
    private double futureBalanceCache = 0.0d;
    private boolean graphButtonEnabled = true;
    @SuppressWarnings("FieldCanBeLocal")
    private ImageView graphLeftArrow;
    private TextView graphNetworthTextView;
    @SuppressWarnings("FieldCanBeLocal")
    private ImageView graphRightArrow;
    private ProgressBar graphSpinner;
    private java.util.concurrent.Future<?> graphFuture;
    private TextView graphTitleTextView;
    //boolean launching = false;
    //private LicenseChecker mChecker;
    private Handler mHandler = null;
    //private LicenseCheckerCallback mLicenseCheckerCallback;
    private Button moreChartsButton;
    private int msgEmail = -1;
    private ChartView netWorthChartView;
    private boolean progUpdate = false;
    private PocketMoneyProgressDialog progressDialog = null;
    private boolean shouldEmail = false;
    //double startTime = 0.0d;
    private ChartView theChartView;
    private FrameLayout theGraphLayout;
    private AlertDialog tipDialog;
    private WakeLock wakeLock;

    public final ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == ACCOUNT_REQUEST_FILTER && result.getData() != null) {
                    Intent i = new Intent(this, TransactionsActivity.class);
                    i.putExtra("Filter", (FilterClass) Objects.requireNonNull(result.getData().getExtras()).get("Filter"));
                    startActivity(i);
                }
            }
    );

    public final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                reloadData();
            }
    );

    private final ActivityResultLauncher<Intent> budgetLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Prefs.getBooleanPref(Prefs.SHUTTINGDOWN)) {
                    setResult(ACCOUNT_REQUEST_FILTER);
                    finish();
                }
            }
    );

    private final ActivityResultLauncher<Intent> emailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (this.fileNames != null) {
                    for (Uri fileName : this.fileNames) {
                        File file = new File(Objects.requireNonNull(fileName.getPath()));
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Log.d("ACCOUNTSACTIVITY", "Notification permission granted");
                } else {
                    Log.d("ACCOUNTSACTIVITY", "Notification permission denied");
                }
            }
    );

    @SuppressWarnings("EmptyMethod")
    private void testTest() {
    }

    @SuppressWarnings("EmptyMethod")
    private void checkLicense() {
    }

    //private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
    //  private MyLicenseCheckerCallback() {
    //}

//    TODO: Investigate what this method was intended to do? Based on dontAllow() method, seems to be related to Licensing. What if wtf?
//    public void allow() {
//        if (!AccountsActivity.this.isFinishing()) {
//            String wtf = "";
//        }
//    }

//    TODO: May need to reactivte this method as part of licensing app
//    public void dontAllow() {
//        if (!AccountsActivity.this.isFinishing()) {
//            AccountsActivity.this.showDialog(LISCENSING /*8*/);
//        }
//    }

//        public void applicationError(ApplicationErrorCode errorCode) {
//            if (!AccountsActivity.this.isFinishing()) {
//                String breakokay = new StringBuilder(String.valueOf("")).toString();
//            }
//        }

    public void reloadCharts() {
        this.netWorthChartView.setVisibility(View.GONE);
        this.cashFlowChartView.setVisibility(View.GONE);
        this.moreChartsButton.setVisibility(View.GONE);
        ((View) this.graphSpinner.getParent()).setVisibility(View.VISIBLE);
        this.graphSpinner.setVisibility(View.VISIBLE);
        if (Prefs.getBooleanPref(Prefs.SHOWSUMMARYCHARTS)) {
            switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
                case Enums.kSumamryChartTypeNetWorth /*0*/:
                    this.theChartView = this.netWorthChartView;
                    break;
                case Enums.kSumamryChartTypeCashFlow /*1*/:
                    this.theChartView = this.cashFlowChartView;
                    break;
                case Enums.kSumamryChartMoreCharts /*2*/:
                    this.theChartView = null;
                    break;
            }
            if (this.graphFuture != null) {
                this.graphFuture.cancel(true);
                this.graphFuture = null;
            }
            this.graphFuture = executor.submit(() -> {
                if (AccountsActivity.this.theChartView != null) {
                    synchronized (AccountsActivity.this.adapter) {
                        //AccountsActivity.this.theChartView.reloadData(true); TODO This line causes null pointer exception. Same as trying to load graph in ReportsActivity. To fix
                    }
                }
                runOnUiThread(() -> {
                    synchronized (AccountsActivity.this.adapter) {
                        AccountsActivity.this.graphReloadCallback();
                        if (AccountsActivity.this.theChartView != null) {
                            AccountsActivity.this.theChartView.reloadData(true);
                        }
                    }
                    this.graphFuture = null;
                });
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ACCOUNTSACTIVITY", "onCreate() has just run");
        this.wakeLock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(26, "AccountsActivity:DoNotDimScreen");
        this.context = this;
        @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.accounts, null);
        setContentView(layout);
        setupView(layout);
        setResult(ACCOUNT_REQUEST_FILTER);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SM Money");
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        }

        // Request Notification permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    public static void displayLiteDialog(final Activity c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(Locales.kLOC_LITE_UPGRADE)
                .setMessage(Locales.kLOC_LITE_UPGRADE_BODY_ANDROID)
                .setCancelable(false)
                .setPositiveButton(Locales.kLOC_LITE_BUYIT, (dialog, which) -> {
                    String str;
                    if (AccountsActivity.IS_GOOGLE_MARKET) {
                        str = "http://market.android.com/details?id=com.catamount.pocketmoney";
                    } else {
                        str = "http://www.amazon.com/gp/product/B004JVI48G";
                    }
                    c.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
                })
                .setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    private void showMenuDialog() {
        if (this.tipDialog != null) {
            Log.d("ACCOUNTSACTIVITY", "showMenuDialog() called and this.tipDialog != null");
            this.tipDialog.isShowing();
        }
    }

//    TODO: Think this method can be deleted as now replaced titleTextView with SupportActionBar
//    private void setTitle(String title) {
//        this.titleTextView.setText(title);
//    }

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

    protected void onResume() {
        super.onResume();
        Log.d("ACCOUNTSACTIVITY", "onResume just called");
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
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
                Prefs.setPref(Prefs.HINT_WELCOME, true);
                AccountsActivity.this.showMenuDialog();
                dialog.dismiss();
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
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
                Prefs.setPref(Prefs.HINT_FIRSTNEWACCOUNT, true);
                dialog.dismiss();
            });
            alert.show();
        }
        FrameLayout frameLayout = this.theGraphLayout;
        int i2 = (!Prefs.getBooleanPref(Prefs.SHOWSUMMARYCHARTS) || SMMoney.isLiteVersion()) ? View.GONE : View.VISIBLE;
        frameLayout.setVisibility(i2);
        clearBalanceCache();
        if (!initTaskRunning) {
            initTaskRunning = true;
            executor.execute(() -> {
                Log.d("ACCOUNTSACTIVITY", "InitTask() doInBackground - ie update f/x rate - has just run");
                if (Prefs.getBooleanPref(Prefs.UPDATEEXCHANGERATES)) {
                    AccountDB.updateExchangeRates();
                }
                Database.deleteUnlinkedRepeatingTransactions();
                TransactionDB.addRepeatingTransactions();

                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    synchronized (adapter) {
                        reloadData();
                        reloadBalanceBar();
                        reloadCharts();
                        if (adapter.getCount() == 0 && (tipDialog == null || !tipDialog.isShowing())) {
                            showMenuDialog();
                        }
                    }
                    initTaskRunning = false;
                });
            });
        }
        testTest();
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
        this.graphTitleTextView.setText(this.theChartView.dataSource.title() + ": " + CalExt.descriptionWithYear(selectedDate) + " " + CalExt.descriptionWithMonth(selectedDate));
        this.graphNetworthTextView.setText(CurrencyExt.amountAsCurrency(this.theChartView.dataSource.networthForRow(row)));
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
    }

    private void showCorrectChart() {
        ((View) this.graphSpinner.getParent()).setVisibility(View.GONE);
        this.graphSpinner.setVisibility(View.GONE);
        switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
            case Enums.kSumamryChartTypeNetWorth /*0*/:
                this.netWorthChartView.setVisibility(View.VISIBLE);
                return;
            case Enums.kSumamryChartTypeCashFlow /*1*/:
                this.cashFlowChartView.setVisibility(View.VISIBLE);
                return;
            case Enums.kSumamryChartMoreCharts /*2*/:
                ((View) this.moreChartsButton.getParent()).setVisibility(View.VISIBLE);
                this.moreChartsButton.setVisibility(View.VISIBLE);
                return;
            default:
        }
    }

    private void reloadData() {
        this.adapter.setElements(AccountDB.queryOnViewType(Prefs.getIntPref(Prefs.VIEWACCOUNTS)));
        this.adapter.notifyDataSetChanged();
    }

    @SuppressLint("HandlerLeak")
    private void createHandler() {
        this.mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HandlerActivity.MSG_ANIMATEBALANCEBAR /*3*/:
                        AccountsActivity.this.animateBalanceBarBack();
                        break;
                    case HandlerActivity.MSG_PROGRESS_UPDATE /*4*/:
                        break;
                    case HandlerActivity.MSG_PROGRESS_FINISH /*5*/:
                        if (!AccountsActivity.this.shouldEmail && msg.obj.getClass().equals(String.class)) {
                            Snackbar.make(findViewById(R.id.accounts_root_view), (CharSequence) msg.obj, Snackbar.LENGTH_LONG).show();
                        }
                        try {
                            AccountsActivity.this.wakeLock.release();
                        } catch (Exception e) {
                            Log.d(TAG, "handleMessage: ");
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
                                case EMAIL_QIF /*0*/:
                                    emailIntent.setType("text/qif");
                                    String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
                                    File sharedQifFile = new File(pmExternalPath, "SMMoney.qif");
                                    Uri contentUriQif = getUriForFile(AccountsActivity.this, "com.example.fileprovider", sharedQifFile);
                                    emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    emailIntent.putExtra("android.intent.extra.STREAM", contentUriQif);
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    objArr = new Object[1];
                                    objArr[0] = "QIF";
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArr));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    Object[] objArrEmailText = new Object[2];
                                    objArrEmailText[0] = "QIF";
                                    objArrEmailText[1] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArrEmailText));
                                    AccountsActivity.this.startActivity(Intent.createChooser(emailIntent, "CHOOSE EMAIL CLIENT"));
                                    break;
                                case EMAIL_TDF /*1*/:
                                    emailIntent.setType("text/txt");
                                    String pmExternalPathTdf = SMMoney.getExternalPocketMoneyDirectory();
                                    File sharedTxtFile = new File(pmExternalPathTdf, "SMMoney.txt");
                                    Uri contentUriTxt = getUriForFile(AccountsActivity.this, "com.example.fileprovider", sharedTxtFile);
                                    emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    emailIntent.putExtra("android.intent.extra.STREAM", contentUriTxt);
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    Object[] objArrEmailSubjectTDF = new Object[1];
                                    objArrEmailSubjectTDF[0] = "TDF";
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArrEmailSubjectTDF));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    Object[] objArrEmailTextTDF = new Object[2];
                                    objArrEmailTextTDF[0] = "TDF";
                                    objArrEmailTextTDF[1] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArrEmailTextTDF));
                                    AccountsActivity.this.startActivity(emailIntent);
                                    break;
                                case EMAIL_CSV /*2*/:
                                    emailIntent.setType("text/csv");
                                    String pmExternalPathCsv = SMMoney.getExternalPocketMoneyDirectory();
                                    File sharedCsvFile = new File(pmExternalPathCsv, "SMMoney.csv");
                                    Uri contentUriCsv = getUriForFile(AccountsActivity.this, "com.example.fileprovider", sharedCsvFile);
                                    emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    emailIntent.putExtra("android.intent.extra.STREAM", contentUriCsv);
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    objArr = new Object[1];
                                    objArr[0] = "CSV";
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArr));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    Object[] objArrEmailTextCSV = new Object[2];
                                    objArrEmailTextCSV[0] = "CSV";
                                    objArrEmailTextCSV[1] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArrEmailTextCSV));
                                    AccountsActivity.this.startActivity(emailIntent);
                                    break;
                                case EMAIL_OFX /*3*/:
                                    Log.i(TAG, "EMAIL_OFX IN ACCOUNTSACTIVITY HANDLER - START");
                                    Intent emailOfxIntent = new Intent("android.intent.action.SEND_MULTIPLE");
                                    emailOfxIntent.setType("text/ofx");
                                    ArrayList<Uri> fileNames = new ArrayList<>();
                                    String ofxDirPath = SMMoney.getExternalPocketMoneyDirectory();
                                    File ofxDir = new File(ofxDirPath);
                                    File[] ofxFiles;
                                    ofxFiles = ofxDir.listFiles();
                                    if (ofxFiles != null) {
                                        for (File file : ofxFiles) {
                                            Uri contentUriOfx = getUriForFile(AccountsActivity.this, "com.example.fileprovider", file);
                                            fileNames.add(contentUriOfx);
                                        }
                                    }
                                    emailOfxIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    emailOfxIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileNames);
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
                                    objArr = new Object[1];
                                    objArr[0] = "OFX/QFX";
                                    emailOfxIntent.putExtra("android.intent.extra.SUBJECT", accountsActivity.getString(i, objArr));
                                    accountsActivity = AccountsActivity.this;
                                    i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
                                    Object[] objArrEmailTextOFX = new Object[2];
                                    objArrEmailTextOFX[0] = "OFX/QFX";
                                    objArrEmailTextOFX[1] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                                    emailOfxIntent.putExtra("android.intent.extra.TEXT", accountsActivity.getString(i, objArrEmailTextOFX));
                                    Log.i(TAG, "ACCOUNTS_ACTIVITY.JAVA: HANDLER - BEFORE START ACTIVTY CALLED");
                                    AccountsActivity.this.startActivity(emailOfxIntent);
                                    break;
                            }
                            AccountsActivity.this.shouldEmail = false;
                            return;
                        }
                        AccountsActivity.this.reloadData();
                        return;
                    case HandlerActivity.MSG_ERROR /*6*/:
                        if (AccountsActivity.this.progressDialog != null && AccountsActivity.this.progressDialog.isShowing()) {
                            AccountsActivity.this.progressDialog.dismiss();
                            AccountsActivity.this.progressDialog.setProgress(0);
                        }
                        AlertDialog alert = new AlertDialog.Builder(AccountsActivity.this.context).create();
                        alert.setTitle("Error");
                        alert.setMessage((String) msg.obj);
                        alert.setCancelable(false);
                        alert.setButton(-1, "OK", (dialog, id) -> dialog.dismiss());
                        alert.show();
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message id " + msg.what);
                }
                if (AccountsActivity.this.progressDialog == null || !AccountsActivity.this.progressDialog.isShowing()) {
                    AccountsActivity.this.progressDialog = new PocketMoneyProgressDialog(AccountsActivity.this);
                    AccountsActivity.this.progressDialog.setMessage("Transferring...\n\nWarning: This may take several minutes");
                    AccountsActivity.this.progressDialog.setCancelable(true);
                    AccountsActivity.this.progressDialog.show();
                    try {
                        AccountsActivity.this.wakeLock.acquire(10000);
                    } catch (Exception e2) {
                        Log.d(TAG, "handleMessage: ");
                    }
                }
                if (AccountsActivity.this.progressDialog != null && AccountsActivity.this.progressDialog.isShowing()) {
                    AccountsActivity.this.progressDialog.setProgress(msg.arg1);
                }
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

    private void animateBalanceBarBack() {
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
            case Enums.kBalanceTypeNone /*-1*/:
            case Enums.kBalanceTypeFiltered /*5*/:
                return 0.0d;
            case Enums.kBalanceTypeFuture /*0*/:
                if (this.futureBalanceCache != 0.0d) {
                    return this.futureBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.futureBalanceCache = totalWorth;
                return totalWorth;
            case Enums.kBalanceTypeCleared /*1*/:
                if (this.clearedBalanceCache != 0.0d) {
                    return this.clearedBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.clearedBalanceCache = totalWorth;
                return totalWorth;
            case Enums.kBalanceTypeCurrent /*2*/:
                if (this.currentBalanceCache != 0.0d) {
                    return this.currentBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.currentBalanceCache = totalWorth;
                return totalWorth;
            case Enums.kBalanceTypeAvailableFunds /*3*/:
                if (this.availableFundsBalanceCache != 0.0d) {
                    return this.availableFundsBalanceCache;
                }
                totalWorth = AccountDB.totalWorth(pref);
                this.availableFundsBalanceCache = totalWorth;
                return totalWorth;
            case Enums.kBalanceTypeAvailableCredit /*4*/:
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

        executor.execute(() -> {
            int pref = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
            if (pref == Enums.kBalanceTypeFiltered /*5*/) {
                Prefs.setPref(Prefs.BALANCETYPE, Enums.kBalanceTypeCurrent /*2*/);
                pref = Enums.kBalanceTypeCurrent /*2*/;
            }
            double totalWorth = totalWorth(pref);
            final int finalPref = pref;
            runOnUiThread(() -> {
                if (isFinishing()) return;
                balanceBar.balanceAmountTextView.setVisibility(View.VISIBLE);
                balanceBar.balanceAmountTextView.setTextColor(totalWorth < 0.0d ? ContextCompat.getColor(this, R.color.theme_red_label_color_on_black) : ContextCompat.getColor(this, R.color.black_theme_text));
                balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(totalWorth));
                balanceBar.balanceTypeTextView.setVisibility(View.VISIBLE);
                balanceBar.balanceTypeTextView.setText(AccountDB.totalWorthLabel(finalPref));
                balanceBar.balanceTypeTextView.setTextColor(ContextCompat.getColor(this, R.color.black_theme_text));
                balanceBar.progressBar.setVisibility(View.GONE);
                synchronized (adapter) {
                    reloadData();
                }
            });
        });
    }

//    TODO: Figure out what this callback method is supposed to do and correct
//    private void reloadBalanceBarCallBack(double totalWorth, int pref) {
//    }

    private View.OnClickListener getBalanceBarClickListener() {
        return v -> {
            String str;
            int i = Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED) ? Prefs.getIntPref(Prefs.BALANCETYPE) : Prefs.getIntPref(Prefs.BALANCEBARREGISTER);
            if (v.equals(AccountsActivity.this.balanceBar.nextButton)) {
                i = AccountsActivity.this.balanceBar.nextBalanceTypeAfter(i);
                Log.d("ACCOUNTSACTIVITY", "'Next' balance bar button clicked");
            } else {
                i = AccountsActivity.this.balanceBar.nextBalanceTypeBefore(i);
                Log.d("ACCOUNTSACTIVITY", "'Previous' balance bar button clicked");
            }
            if (Prefs.getBooleanPref(Prefs.BALANCEBARUNIFIED)) {
                str = Prefs.BALANCETYPE;
                Log.d("ACCOUNTSACTIVITY", "Balance bar type = 'unified'");
            } else {
                str = Prefs.BALANCEBARREGISTER;
                Log.d("ACCOUNTSACTIVITY", "Balance bar type != 'unified'");
            }
            Prefs.setPref(str, i);
            AccountsActivity.this.reloadBalanceBar();
            Log.d("ACCOUNTSACTIVITY", "reloadBalanceBar() just called");
        };
    }

    private void deleteAccount(final AccountClass account) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(Locales.kLOC_ACCOUNT_DELETE);
        alert.setMessage(Locales.kLOC_ACCOUNT_DELETE_BODY);
        alert.setPositiveButton(Locales.kLOC_GENERAL_DELETE, (dialog, whichButton) -> {
            AlertDialog.Builder alert1 = new AlertDialog.Builder(AccountsActivity.this.context);
            alert1.setTitle(Locales.kLOC_ACCOUNT_DELETE);
            alert1.setMessage(Locales.kLOC_ACCOUNT_DELETE_CONFIRM);
            CharSequence charSequence = Locales.kLOC_GENERAL_DELETE;
            alert1.setPositiveButton(charSequence, (dialog2, whichButton2) -> {
                TransactionDB.deleteRecordsFromAccount(account.getAccount());
                TransactionDB.deleteRepeatingRecordsFromAccount(account.getAccount());
                TransactionDB.deleteRepetaingRecordsFromTransactionForAccount(account.getAccount());
                account.setDeleted(true);
                account.saveToDatabase();
                AccountsActivity.this.reloadData();
                AccountsActivity.this.reloadBalanceBar();
            });
            alert1.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog1, whichButton1) -> dialog1.cancel());
            alert1.show();
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, whichButton) -> dialog.cancel());
        alert.show();
    }

    private void importCSVFromSD() {
        final ImportExportCSV importcsv = new ImportExportCSV(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PocketMoneyBackup/" + "SMMoney.csv", this);
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
        final ImportExportTDF importtdf = new ImportExportTDF(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PocketMoneyBackup/" + "SMMoney.txt", this);
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

    private void setupView(LinearLayout layout) {
        Log.d("ACCOUNTSACTIVITY", "setupView() called and started");
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
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (!AccountsActivity.this.progUpdate) {
                Intent intent = new Intent(AccountsActivity.this, BudgetsActivity.class);
                budgetLauncher.launch(intent);
                AccountsActivity.this.overridePendingTransition(0, 0);
                AccountsActivity.this.progUpdate = true;
                AccountsActivity.this.accountRadioButton.setChecked(true);
                AccountsActivity.this.progUpdate = false;
            }
        });
        ((View) rg.getParent()).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        layout.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.theGraphLayout = layout.findViewById(R.id.chartframelayout);
        this.graphLeftArrow = layout.findViewById(R.id.graphleftarrow);
        this.graphLeftArrow.setOnClickListener(v -> {
            if (AccountsActivity.this.graphButtonEnabled) {
                switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
                    case Enums.kSumamryChartTypeNetWorth /*0*/:
                        Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, Enums.kSumamryChartTypeCashFlow/*1*/);
                        break;
                    case Enums.kSumamryChartTypeCashFlow /*1*/:
                        Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, Enums.kSumamryChartMoreCharts/*2*/);
                        break;
                    case Enums.kSumamryChartMoreCharts /*2*/:
                        Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, Enums.kSumamryChartTypeNetWorth/*0*/);
                        break;
                }
                AccountsActivity.this.runOnUiThread(() -> AccountsActivity.this.reloadCharts());
            }
        });
        this.graphRightArrow = layout.findViewById(R.id.graphrightarrow);
        this.graphRightArrow.setOnClickListener(v -> {
            if (AccountsActivity.this.graphButtonEnabled) {
                switch (Prefs.getIntPref(Prefs.SUMMARYCHARTS_CHARTTYPE)) {
                    case Enums.kSumamryChartTypeNetWorth /*0*/:
                        Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, Enums.kSumamryChartMoreCharts /*2*/);
                        break;
                    case Enums.kSumamryChartTypeCashFlow /*1*/:
                        Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, Enums.kSumamryChartTypeNetWorth /*0*/);
                        break;
                    case Enums.kSumamryChartMoreCharts /*2*/:
                        Prefs.setPref(Prefs.SUMMARYCHARTS_CHARTTYPE, Enums.kSumamryChartTypeCashFlow /*1*/);
                        break;
                }
                AccountsActivity.this.runOnUiThread(() -> AccountsActivity.this.reloadCharts());
            }
        });
        this.netWorthChartView = layout.findViewById(R.id.networthbarchart);
        this.netWorthChartView.delegate = this;
        this.netWorthChartView.dataSource = new NetWorthDataSource(this.adapter);
        this.cashFlowChartView = layout.findViewById(R.id.cashflowbarchart);
        this.cashFlowChartView.delegate = this;
        this.cashFlowChartView.dataSource = new CashFlowDataSource(this.adapter);
        this.moreChartsButton = layout.findViewById(R.id.morechartsbutton);
        this.moreChartsButton.setTextColor(-7829368);
        this.moreChartsButton.setText(Locales.kLOC_CHARTS_MORECHARTS);
        this.moreChartsButton.setOnClickListener(v -> AccountsActivity.this.showReportsDialog());
        this.graphSpinner = layout.findViewById(R.id.graphspinner);
        this.graphTitleTextView = layout.findViewById(R.id.graphtitletextview);
        this.graphNetworthTextView = layout.findViewById(R.id.networthtextview);
    }

    private void importQIFFromSD() {
        File[] qifList = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PocketMoneyBackup").listFiles((dir, name) -> (name.endsWith(".qif") || name.endsWith(".QIF")) && !name.startsWith("."));
        if (qifList != null) {
            for (File file : qifList) {
                Log.i("Q File Path = ", file.getAbsolutePath());
                final ImportExportQIF importqif = new ImportExportQIF(file.getAbsolutePath(), this);
                if (importqif.hasFile()) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "InterruptedException in importQIFFromSD", e);
                        }
                        importqif.importIntoDatabase(AccountsActivity.this);
                        //new File(importqif.QIFPath).delete();
                    }).start();
                } else {
                    Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
                }
            }
            return;
        }
        Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
    }

    private void importOFXFromSD() {
        File[] qifList = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PocketMoneyBackup").listFiles((dir, name) -> (name.endsWith(".ofx") || name.endsWith(".qfx") || name.endsWith(".OFX") || name.endsWith(".QFX")) && !name.startsWith("."));
        if (qifList != null) {
            for (File file : qifList) {
                if (file.exists()) {
                    final ImportExportOFX importofx = new ImportExportOFX(this, file.getAbsolutePath());
                    new Thread(() -> {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "InterruptedException in importQIFFromSD", e);
                        }
                        importofx.importIntoDatabase();
                        File importedFile = new File(importofx.path);
                        AccountsActivity.this.runOnUiThread(() -> AccountsActivity.this.reloadData());
                    }).start();
                } else {
                    Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
                }
            }
            return;
        }
        Toast.makeText(this.context, "File not found", Toast.LENGTH_LONG).show();
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
        final PocketMoneyProgressDialog pd = new PocketMoneyProgressDialog(this.context);
        pd.setMessage("Exporting...");
        pd.show();
        new Thread() {
            public void run() {
                boolean exportSeperately = Prefs.getBooleanPref(Prefs.QIF_EXPORT_SEPERATELY);
                String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                FilterClass filter = new FilterClass();
                if (exportSeperately) {
                    ArrayList<String> accNames = new ArrayList<>();
                    for (AccountClass accountClass : AccountsActivity.this.adapter.getElements()) {
                        try {
                            accNames.add(accountClass.getAccount());
                        } catch (Exception exAcc) {
                            Log.i("***** Doing this ERROR:", exAcc.toString());
                        }
                    }
                    String[] array = accNames.toArray(new String[0]);
                    //int length = array.length;
                    for (String item : array) {
                        filter.setAccount(item);
                        ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(filter);
                        String fileName = fileDir + "/PocketMoneyBackup/" + item + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar()) + ".qif";
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
                    Log.e(TAG, "Exception in exportQIFToSD (dismissing progress dialog)", e);
                }
            }
        }.start();
    }

    private void exportOFXToSD() {
        final PocketMoneyProgressDialog pd = new PocketMoneyProgressDialog(this.context);
        pd.setMessage("Exporting...");
        pd.show();
        new Thread() {
            public void run() {
                String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
                FilterClass filter = new FilterClass();
                final ArrayList<Uri> fileNames = new ArrayList<>();
                ArrayList<String> accNames = new ArrayList<>();
                ArrayList<AccountClass> accounts = AccountsActivity.this.adapter.getElements();
                for (AccountClass account : accounts) {
                    try {
                        accNames.add(account.getAccount());
                    } catch (Exception exAcc) {
                        Log.i("***** Doing this ERROR:", exAcc.toString());
                    }
                }
                String[] array = accNames.toArray(new String[0]);
                for (String item : array) {
                    filter.setAccount(item);
                    ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(filter);
                    String filePath = pmExternalPath + item + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar()) + ".qfx";
                    fileNames.add(Uri.parse("file://" + filePath));
                    ImportExportOFX exportofx = new ImportExportOFX(AccountsActivity.this.context, filePath);
                    exportofx.accountNameBeingImported = item;
                    exportofx.filter = filter;
                    exportofx.exportRecords(query);
                }
                pd.dismiss();
            }
        }.start();
    }

    private void restoreFromSD() {
        AlertDialog.Builder alert2 = new AlertDialog.Builder(this.context);
        alert2.setTitle(Locales.kLOC_TOOLS_RESTORE_SD);
        alert2.setMessage(Locales.kLOC_TRANSFERS_RESTORE);
        alert2.setPositiveButton(Locales.kLOC_GENERAL_YES, (dialog, whichButton) -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(AccountsActivity.this.context);
            alert.setTitle(Locales.kLOC_TOOLS_RESTORE_SD);
            alert.setMessage(Locales.kLOC_TRANSFERS_RESTORE_CONFIRM);
            alert.setPositiveButton(Locales.kLOC_GENERAL_YES, (dialog2, whichButton2) -> {
                Database.closeDBAndNullify();
                Prefs.importDB(AccountsActivity.this);
                Database.loadDatabasePreferences();
                Prefs.initialize();
                AccountsActivity.this.reloadData();
                AccountsActivity.this.reloadBalanceBar();
            });
            alert.setNegativeButton(Locales.kLOC_GENERAL_NO, (dialog1, whichButton1) -> dialog1.dismiss());
            alert.show();
        });
        alert2.setNegativeButton(Locales.kLOC_GENERAL_NO, (dialog, whichButton) -> dialog.dismiss());
        alert2.show();
    }

    public void generateCSVForEmail() {
        String pmExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.msgEmail = EMAIL_CSV;
        this.shouldEmail = true;
        this.emailFileLocation = pmExternalPath + "/PocketMoneyBackup/" + "SMMoney.csv";
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
        String pmExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.msgEmail = EMAIL_TDF;
        this.shouldEmail = true;
        this.emailFileLocation = pmExternalPath + "/PocketMoneyBackup/" + "SMMoney.txt";
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

        final ArrayList<Uri> qifUris = new ArrayList<>();
        for (Uri file : fileNames) {
            File fileToAdd = new File(file.getPath());
            Uri contentUriOfx = getUriForFile(AccountsActivity.this, "com.example.fileprovider", fileToAdd);
            qifUris.add(contentUriOfx);

        }
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", qifUris);
        int i = R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT;
        Object[] objArr = new Object[1];
        objArr[0] = "QIF";
        emailIntent.putExtra("android.intent.extra.SUBJECT", getString(i, objArr));
        i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
        Object[] objArr2 = new Object[2];
        objArr2[0] = "QIF";
        objArr2[1] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
        emailIntent.putExtra("android.intent.extra.TEXT", getString(i, objArr2));
        this.fileNames = fileNames;
        emailLauncher.launch(emailIntent);
    }

    private void backupToSD() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.context);
        alert.setTitle(Locales.kLOC_TOOLS_BACKUP_SD);
        alert.setMessage("This will backup your database to Downloads/PocketMoneyBackup/SMMoneyDB.sql");
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
            String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
            File src = new File(Environment.getDataDirectory() + "/data/" + getPackageName() + "/databases/SMMoneyDB.sql");
            File dst = new File(pmExternalPath, "SMMoneyDB.sql");
            try {
                Prefs.copyFile(src, dst);
                Toast.makeText(AccountsActivity.this, "Backup complete", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "IOException in backupToSD", e);
                Toast.makeText(AccountsActivity.this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null);
        alert.show();
    }

    private void generateEmailForOFX(ArrayList<Uri> fileNames) {
        Intent emailIntent = new Intent("android.intent.action.SEND_MULTIPLE");
        emailIntent.setType("text/ofx");
        Prefs.exportDB(this);
        final ArrayList<Uri> ofxUris = new ArrayList<>();
        for (Uri file : fileNames) {
            File fileToAdd = new File(file.getPath());
            Uri contentUriOfx = getUriForFile(AccountsActivity.this, "com.example.fileprovider", fileToAdd);
            ofxUris.add(contentUriOfx);
        }
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", ofxUris);
        emailIntent.putExtra("android.intent.extra.SUBJECT", "SMMoney OFX/QFX File");
        int i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
        Object[] objArr = new Object[2];
        objArr[0] = "OFX/QFX";
        objArr[1] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
        emailIntent.putExtra("android.intent.extra.TEXT", getString(i, objArr));
        this.fileNames = fileNames;
        emailLauncher.launch(emailIntent);
    }

    protected void generateQIFForEmail() {
        final PocketMoneyProgressDialog pd = new PocketMoneyProgressDialog(this.context);
        pd.setMessage("Exporting...");
        pd.show();
        new Thread() {
            public void run() {
                boolean exportSeperately = Prefs.getBooleanPref(Prefs.QIF_EXPORT_SEPERATELY);
                String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
                AccountsActivity.this.msgEmail = EMAIL_QIF;
                AccountsActivity.this.shouldEmail = true;
                FilterClass filter = new FilterClass();
                ImportExportQIF exportqif;
                if (exportSeperately) {
                    final ArrayList<Uri> fileNames = new ArrayList<>();
                    ArrayList<String> accNames = new ArrayList<>();
                    for (AccountClass accountClass : AccountsActivity.this.adapter.getElements()) {
                        try {
                            accNames.add(accountClass.getAccount());
                        } catch (Exception exAcc) {
                            Log.i("***** Doing this ERROR:", exAcc.toString());
                        }
                    }
                    String[] array = accNames.toArray(new String[0]);
                    for (String item : array) {
                        filter.setAccount(item);
                        ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(filter);
                        String fileName = item + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar()) + ".qif";
                        String filePath = pmExternalPath + fileName;
                        fileNames.add(Uri.parse("file://" + filePath));
                        exportqif = new ImportExportQIF(AccountsActivity.this);
                        exportqif.QIFPath = filePath;
                        exportqif.accountNameBeingImported = item;
                        exportqif.setFilter(filter);
                        exportqif.exportRecords(query);
                    }
                    AccountsActivity.this.progressDialog.dismiss();
                    AccountsActivity.this.progressDialog.setProgress(0);
                    AccountsActivity.this.runOnUiThread(() -> AccountsActivity.this.generateEmailForQIF(fileNames));
                } else {
                    AccountsActivity.this.emailFileLocation = pmExternalPath + "SMMoney.qif";
                    String fl = AccountsActivity.this.emailFileLocation;
                    exportqif = new ImportExportQIF(AccountsActivity.this);
                    exportqif.QIFPath = fl;
                    exportqif.exportRecords();
                }
                pd.dismiss();
            }
        }.start();
    }

    private void generateBackupForEmail() {
        Intent emailIntent = new Intent("android.intent.action.SEND");
        emailIntent.setType("text/plain");
        Prefs.exportDB(this);
        File existingDB = new File(Environment.getExternalStorageDirectory(), "PocketMoneyBackup");
        File sharedDB = new File(existingDB, "SMMoneyDB.sql");
        Uri contentUri = getUriForFile(this, "com.example.fileprovider", sharedDB);
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.putExtra("android.intent.extra.STREAM", contentUri);
        emailIntent.putExtra("android.intent.extra.SUBJECT", "SMMoney Backup File");
        int i = R.string.kLOC_FILETRANSFERS_EMAIL_BODY;
        Object[] objArr = new Object[ACCOUNT_REQUEST_BUDGET];
        objArr[0] = Locales.kLOC_TOOLS_BACKUPFILES;
        objArr[ACCOUNT_REQUEST_FILTER] = CalExt.descriptionWithMediumDate(new GregorianCalendar());
        emailIntent.putExtra("android.intent.extra.TEXT", getString(i, objArr[0], objArr[1]));
        startActivity(emailIntent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("ACCOUNTSACTIVITY", "Menu has just been opened");
        menu.add(0, MENU_NEW, 0, Locales.kLOC_ACCOUNT_NEW).setIcon(R.drawable.abouticon);
        menu.add(0, MENU_PREFS, 0, Locales.kLOC_GENERAL_PREFERENCES).setIcon(R.drawable.abouticon);
        menu.add(0, MENU_TRANSFER, 0, Locales.kLOC_TOOLS_FILETRANSFERS).setIcon(R.drawable.abouticon);
        menu.add(0, MENU_VIEW, 0, Locales.kLOC_VIEW_OPTIONS).setIcon(R.drawable.abouticon);
        menu.add(0, MENU_REPEATING, 0, Locales.kLOC_REPEATING_TRANSACTIONS).setIcon(R.drawable.abouticon);
        menu.add(0, MENU_QUIT, 0, Locales.kLOC_GENERAL_QUIT).setIcon(R.drawable.abouticon);
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
                FragmentManager fragmentManager = getSupportFragmentManager();
                DialogFragmentFileTransfer dialogFragmentFileTransfer = new DialogFragmentFileTransfer();
                dialogFragmentFileTransfer.show(fragmentManager, "fragment_dialog");
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

    private void generateOFXForEmail() {
        final PocketMoneyProgressDialog pd = new PocketMoneyProgressDialog(this.context);
        pd.setMessage("Exporting...");
        pd.show();
        new Thread() {
            public void run() {
                String pmExternalPath = SMMoney.getExternalPocketMoneyDirectory();
                AccountsActivity.this.msgEmail = EMAIL_OFX;
                AccountsActivity.this.shouldEmail = true;
                FilterClass filter = new FilterClass();
                final ArrayList<Uri> fileNames = new ArrayList<>();
                ArrayList<String> accNames = new ArrayList<>();
                ArrayList<AccountClass> accounts = AccountsActivity.this.adapter.getElements();
                for (AccountClass account : accounts) {
                    try {
                        accNames.add(account.getAccount());
                    } catch (Exception exAcc) {
                        Log.i("***** Doing this ERROR:", exAcc.toString());
                    }
                }
                String[] array = accNames.toArray(new String[0]);
                for (String item : array) {
                    filter.setAccount(item);
                    ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(filter);
                    String filePath = pmExternalPath + item + "-" + CalExt.descriptionWithTimestamp(new GregorianCalendar()) + ".qfx";
                    fileNames.add(Uri.parse("file://" + filePath));
                    ImportExportOFX exportofx = new ImportExportOFX(AccountsActivity.this.context, filePath);
                    exportofx.accountNameBeingImported = item;
                    exportofx.filter = filter;
                    exportofx.exportRecords(query);
                }

                pd.dismiss();
                AccountsActivity.this.runOnUiThread(() -> AccountsActivity.this.generateEmailForOFX(fileNames));
            }
        }.start();
    }

    private void showReportsDialog() {
        new AlertDialog.Builder(this).setTitle("").setItems(new CharSequence[]{Locales.kLOC_TOOLS_ACCOUNTREPORT, Locales.kLOC_TOOLS_CATEGORYREPORT, Locales.kLOC_TOOLS_CLASSREPORT, Locales.kLOC_TOOLS_PAYEEREPORT}, (dialog, which) -> {
            FilterClass f = new FilterClass();
            ArrayList<TransactionClass> transactions = TransactionDB.queryWithFilter(f);
            Intent i;
            switch (which) {
                case 0 /*0 Locales.kLOC_TOOLS_ACCOUNTREPORT*/:
                    AccountsReportDataSource ds = new AccountsReportDataSource(transactions, f);
                    i = new Intent(AccountsActivity.this, ReportsActivity.class);
                    PMGlobal.datasource = ds;
                    AccountsActivity.this.startActivity(i);
                    return;
                case 1 /*1 Locales.kLOC_TOOLS_CATEGORYREPORT*/:
                    CategoryReportDataSource ds2 = new CategoryReportDataSource(transactions, f);
                    i = new Intent(AccountsActivity.this, ReportsActivity.class);
                    PMGlobal.datasource = ds2;
                    AccountsActivity.this.startActivity(i);
                    return;
                case 2 /*2 Locales.kLOC_TOOLS_CLASSREPORT*/:
                    ClassReportDataSource ds3 = new ClassReportDataSource(transactions, f);
                    i = new Intent(AccountsActivity.this, ReportsActivity.class);
                    PMGlobal.datasource = ds3;
                    AccountsActivity.this.startActivity(i);
                    return;
                case 3 /*3 Locales.kLOC_TOOLS_PAYEEREPORT*/:
                    PayeeReportDataSource ds4 = new PayeeReportDataSource(transactions, f);
                    i = new Intent(AccountsActivity.this, ReportsActivity.class);
                    PMGlobal.datasource = ds4;
                    AccountsActivity.this.startActivity(i);
                    return;
                default:
            }
        }).show();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AccountRowHolder aHolder = (AccountRowHolder) v.getTag();
        Intent i = new Intent();
        i.putExtra("Account", aHolder.account);
        MenuItem item = menu.add(0, CMENU_EDIT, 0, Locales.kLOC_GENERAL_EDIT);
        item.setIcon(R.drawable.abouticon);
        item.setIntent(i);
        item = menu.add(0, CMENU_DELETE, 0, Locales.kLOC_GENERAL_DELETE);
        item.setIcon(R.drawable.abouticon);
        item.setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case CMENU_EDIT /*1*/:
                Intent it = new Intent(this, AccountsEditActivity.class);
                if (b != null) {
                    it.putExtra("Account", (AccountClass) b.get("Account"));
                }
                startActivity(it);
                return true;
            case CMENU_DELETE /*3*/:
                if (b != null) {
                    deleteAccount((AccountClass) b.get("Account"));
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

//    TODO: check if this method is needed. Delete if not
//    public void displayError(String msg) {
//        AlertDialog alert = new AlertDialog.Builder(this).create();
//        alert.setTitle("Error");
//        alert.setMessage(msg);
//        alert.setCancelable(false);
//        alert.setButton(-1, "OK", new OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.dismiss();
//            }
//        });
//        alert.show();
//    }

    public Handler getHandler() {
        if (this.mHandler == null) {
            createHandler();
        }
        return this.mHandler;
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

    private void showLicensingDialog() {
        new AlertDialog.Builder(this).setTitle("Application not licensed").setMessage("This application is not licensed. Please purchase it from Android Market.").setPositiveButton("Buy app", (dialog, which) -> {
            AccountsActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://market.android.com/details?id=" + AccountsActivity.this.getPackageName())));
            AccountsActivity.this.finish();
        }).setNegativeButton("Quit", (dialog, which) -> {
            Intent i = new Intent();
            i.setAction("android.intent.action.MAIN");
            i.addCategory("android.intent.category.HOME");
            AccountsActivity.this.context.startActivity(i);
        }).setCancelable(false).show();
    }

    public static boolean isLite(Context c) {
        return c.getPackageName().toLowerCase().contains("lite");
    }

    protected void showWriteExternalStoraageStatePermission(int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showPermissionExplanationAlertDialog(getString(R.string.permission_dialog_title), getString(R.string.permission_explanation_message), requestCode);
                } else {
                    requestPermission(requestCode);
                }
                return;
            }
        }
        // Proceed if permission is granted or if we are on Android 10+ (where we use legacy storage or scoped paths)
        switch (requestCode) {
            case PERMISSION_EMAIL_QIF:
                generateQIFForEmail();
                break;
            case PERMISSION_EMAIL_TDF:
                generateTDFForEmail();
                break;
            case PERMISSION_EMAIL_CSV:
                generateCSVForEmail();
                break;
            case PERMISSION_EMAIL_OFX:
                generateOFXForEmail();
                break;
            case PERMISSION_EMAIL_DB:
                generateBackupForEmail();
                break;
            case PERMISSION_BACKUP_DB:
                backupToSD();
                break;
            case PERMISSION_RESTORE_DB:
                restoreFromSD();
                break;
            case PERMISSION_RESTORE_QIF:
                importQIFFromSD();
                break;
            case PERMISSION_RESTORE_TDF:
                importTDFFromSD();
                break;
            case PERMISSION_RESTORE_CSV:
                importCSVFromSD();
                break;
            case PERMISSION_RESTORE_OFX:
                importOFXFromSD();
                break;
            case PERMISSION_BACKUP_QIF:
                exportQIFToSD();
                break;
            case PERMISSION_BACKUP_TDF:
                exportTDFToSD();
                break;
            case PERMISSION_BACKUP_CSV:
                exportCSVToSD();
                break;
            case PERMISSION_BACKUP_OFX:
                exportOFXToSD();
                break;
        }
    }

    private void showPermissionExplanationAlertDialog(String title, String message, final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> requestPermission(permissionRequestCode));
        builder.create().show();
    }

    private void showPermissionDeclinedAlertDialog(String title, Spanned message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
        builder.setTitle(title)
                .setMessage(message)

                .setNegativeButton("Open app settings", (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setPositiveButton("I'm sure", (dialog, which) -> {
                });
        builder.create().show();
    }

    private void requestPermission(int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted
            switch (requestCode) {
                case PERMISSION_EMAIL_QIF: {
                    generateQIFForEmail();
                    break;
                }
                case PERMISSION_EMAIL_TDF: {
                    generateTDFForEmail();
                    break;
                }
                case PERMISSION_EMAIL_CSV: {
                    generateCSVForEmail();
                    break;
                }
                case PERMISSION_EMAIL_OFX: {
                    generateOFXForEmail();
                    break;
                }
                case PERMISSION_EMAIL_DB: {
                    generateBackupForEmail();
                    break;
                }
                case PERMISSION_BACKUP_DB: {
                    backupToSD();
                    break;
                }
                case PERMISSION_RESTORE_DB: {
                    restoreFromSD();
                    break;
                }
                case PERMISSION_RESTORE_QIF: {
                    importQIFFromSD();
                    break;
                }
                case PERMISSION_RESTORE_TDF: {
                    importTDFFromSD();
                    break;
                }
                case PERMISSION_RESTORE_CSV: {
                    importCSVFromSD();
                    break;
                }
                case PERMISSION_RESTORE_OFX: {
                    importOFXFromSD();
                    break;
                }
                case PERMISSION_BACKUP_QIF: {
                    exportQIFToSD();
                    break;
                }
                case PERMISSION_BACKUP_TDF: {
                    exportTDFToSD();
                    break;
                }
                case PERMISSION_BACKUP_CSV: {
                    exportCSVToSD();
                    break;
                }
                case PERMISSION_BACKUP_OFX: {
                    exportOFXToSD();
                    break;
                }
            }
        } else {
            // permission denied. Disable the functionality that depends on this permission.
            showPermissionDeclinedAlertDialog(getString(R.string.permissions_declined_permission_dialog_title), Html.fromHtml(getString(R.string.permissions_declined_permission_message)));
        }
    }

    @Override
    public void onFinishEmailDialog(int EmailType) {
        switch (EmailType) {
            case EMAIL_QIF:
                showWriteExternalStoraageStatePermission(PERMISSION_EMAIL_QIF);
                break;
            case EMAIL_TDF:
                showWriteExternalStoraageStatePermission(PERMISSION_EMAIL_TDF);
                break;
            case EMAIL_CSV:
                showWriteExternalStoraageStatePermission(PERMISSION_EMAIL_CSV);
                break;
            case EMAIL_OFX:
                showWriteExternalStoraageStatePermission(PERMISSION_EMAIL_OFX);
                break;
            case EMAIL_BACKUP:
                showWriteExternalStoraageStatePermission(PERMISSION_EMAIL_DB);
                break;
        }
    }

    @Override
    public void onFinishFileTransferDialog(int transferType) {
        switch (transferType) {

            case 0 /*Email Transferss...*/:
                FragmentManager fragmentManagerEmail = getSupportFragmentManager();
                DialogFragmentEmailTransfers dialogFragmentEmailTransfers = new DialogFragmentEmailTransfers();
                dialogFragmentEmailTransfers.show(fragmentManagerEmail, "fragment_dialog_email");
                break;
            case 1 /*Local Storage Trasnfers...*/:
                FragmentManager fragmentManagerLocalStorage = getSupportFragmentManager();
                DialogFragmentLocalStorageTransfers dialogFragmentLocalStorageTransfers = new DialogFragmentLocalStorageTransfers();
                dialogFragmentLocalStorageTransfers.show(fragmentManagerLocalStorage, "fragment_dialog_local");
                break;
            case 2 /*SMMoney Sync...*/:
                AccountsActivity.this.context.startActivity(new Intent(AccountsActivity.this.context, PocketMoneySyncActivity.class));
                break;
        }
    }

    @Override
    public void onFinishLocalStorageTransferDialog(int transferType) {
        switch (transferType) {
            case 0 /*Backup...*/:
                showWriteExternalStoraageStatePermission(PERMISSION_BACKUP_DB);
                break;
            case 1 /*Restore...*/:
                showWriteExternalStoraageStatePermission(PERMISSION_RESTORE_DB);
                break;
            case 2 /*Import...*/:
                FragmentManager fragmentManagerSdImport = getSupportFragmentManager();
                DialogFragmentSdImport dialogFragmentSdImport = new DialogFragmentSdImport();
                dialogFragmentSdImport.show(fragmentManagerSdImport, "fragment_dialog");
                break;
            case 3 /*Export*/:
                FragmentManager fragmentManagerSdExport = getSupportFragmentManager();
                DialogFragmentSdExport dialogFragmentSdExport = new DialogFragmentSdExport();
                dialogFragmentSdExport.show(fragmentManagerSdExport, "fragment_dialog");
                break;
        }
    }

    @Override
    public void onFinishSdImportDialog(int importType) {
        switch (importType) {
            case 0 /*QIF*/:
                FragmentManager fragmentManagerSdImportQIF = getSupportFragmentManager();
                DialogFragmentSdImportQIF dialogFragmentSdImportQIF = new DialogFragmentSdImportQIF();
                dialogFragmentSdImportQIF.show(fragmentManagerSdImportQIF, "fragment_dialog");
                break;
            case 1 /*TDF*/:
                FragmentManager fragmentManagerSdImportTDF = getSupportFragmentManager();
                DialogFragmentSdImportTDF dialogFragmentSdImportTDF = new DialogFragmentSdImportTDF();
                dialogFragmentSdImportTDF.show(fragmentManagerSdImportTDF, "fragment_dialog");
                break;
            case 2 /*CSV*/:
                FragmentManager fragmentManagerSdImportCSV = getSupportFragmentManager();
                DialogFragmentSdImportCSV dialogFragmentSdImportCSV = new DialogFragmentSdImportCSV();
                dialogFragmentSdImportCSV.show(fragmentManagerSdImportCSV, "fragment_dialog");
                break;
            case 3 /*OFX/QFX*/:
                FragmentManager fragmentManagerSdImportOFX = getSupportFragmentManager();
                DialogFragmentSdImportOFX dialogFragmentSdImportOFX = new DialogFragmentSdImportOFX();
                dialogFragmentSdImportOFX.show(fragmentManagerSdImportOFX, "fragment_dialog");
                break;
        }
    }

    @Override
    public void onFinishSdExportDialog(int exportType) {
        switch (exportType) {
            case 0 /*QIF*/:
                showWriteExternalStoraageStatePermission(PERMISSION_BACKUP_QIF);
                break;
            case 1 /*TDF*/:
                showWriteExternalStoraageStatePermission(PERMISSION_BACKUP_TDF);
                break;
            case 2 /*CSV*/:
                showWriteExternalStoraageStatePermission(PERMISSION_BACKUP_CSV);
                break;
            case 3 /*OFX/QFX*/:
                showWriteExternalStoraageStatePermission(PERMISSION_BACKUP_OFX);
                break;
        }
    }

    @Override
    public void onFinishSdImportQIFDialog(String okCancel) {
        if (okCancel.equals(Locales.kLOC_GENERAL_OK)) {
            //Snackbar snackbar = Snackbar.make(this.balanceBar, "On FinishSdImportQIFDialog just ran", Snackbar.LENGTH_LONG);
            //snackbar.show();
            showWriteExternalStoraageStatePermission(PERMISSION_RESTORE_QIF);
            //AccountsActivity.this.importQIFFromSD();
        }
    }

    @Override
    public void onFinishSdImportTDFDialog(String okCancel) {
        if (okCancel.equals(Locales.kLOC_GENERAL_OK)) {
            Snackbar snackbar = Snackbar.make(this.balanceBar, "On FinishSdImportTDFDialog just ran", Snackbar.LENGTH_LONG);
            snackbar.show();
            showWriteExternalStoraageStatePermission(PERMISSION_RESTORE_TDF);
            //AccountsActivity.this.importTDFFromSD();
        }
    }

    @Override
    public void onFinishSdImportCVSDialog(String okCancel) {
        if (okCancel.equals(Locales.kLOC_GENERAL_OK)) {
            Snackbar snackbar = Snackbar.make(this.balanceBar, "On FinishSdImportCSVDialog just ran", Snackbar.LENGTH_LONG);
            snackbar.show();
            showWriteExternalStoraageStatePermission(PERMISSION_RESTORE_CSV);
            //AccountsActivity.this.importCSVFromSD();
        }
    }

    @Override
    public void onFinishSdImportOFXDialog(String okCancel) {
        if (okCancel.equals(Locales.kLOC_GENERAL_OK)) {
            Snackbar snackbar = Snackbar.make(this.balanceBar, "On FinishSdImportOFXDialog just ran", Snackbar.LENGTH_LONG);
            snackbar.show();
            showWriteExternalStoraageStatePermission(PERMISSION_RESTORE_OFX);
            //AccountsActivity.this.importOFXFromSD();
        }
    }
}

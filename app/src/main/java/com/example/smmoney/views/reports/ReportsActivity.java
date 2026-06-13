package com.example.smmoney.views.reports;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PMGlobal;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.PocketMoneyProgressDialog;
import com.example.smmoney.views.charts.ChartViewDelegate;
import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.charts.items.ReportChartItem;
import com.example.smmoney.views.charts.views.ChartBarView;
import com.example.smmoney.views.charts.views.ChartPieView;
import com.example.smmoney.views.charts.views.ChartView;


import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportsActivity extends PocketMoneyActivity implements ChartViewDelegate, ReportDialog.ReportDialogListner {
    public static boolean processData = false;
    private final int MENU_VIEW = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MSG_PROGRESS_FINISH = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MSG_PROGRESS_UPDATE = 1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ReportsRowAdapter adapter;
    private TextView balanceAmountView;
    private TextView balanceLabelView;
    private ChartBarView barChartView;
    private ChartView chartView;
    private ReportDataSource datasource;
    private View nextPeriodView;
    private Button periodButton;
    private ChartPieView pieChartView;
    private View previousPeriodView;
    private PocketMoneyProgressDialog progressDialog = null;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS_FINISH /*0*/:
                    if (ReportsActivity.this.progressDialog != null) {
                        if (ReportsActivity.this.progressDialog.isShowing()) {
                            ReportsActivity.this.progressDialog.dismiss();
                        }
                        return;
                    }
                    return;
                case MSG_PROGRESS_UPDATE /*1*/:
                    if (ReportsActivity.this.progressDialog == null || !ReportsActivity.this.progressDialog.isShowing()) {
                        ReportsActivity.this.progressDialog = new PocketMoneyProgressDialog(ReportsActivity.this);
                        ReportsActivity.this.progressDialog.setMessage("Generating Report.\nPlease wait...");
                        ReportsActivity.this.progressDialog.setCancelable(true);
                        ReportsActivity.this.progressDialog.show();
                    }
                    if (ReportsActivity.this.progressDialog != null && ReportsActivity.this.progressDialog.isShowing()) {
                        ReportsActivity.this.progressDialog.setProgress(msg.arg1);
                        return;
                    }
                    return;
                default:
            }
        }
    };
    @SuppressWarnings("unused")
    private PocketMoneyProgressDialog progressSpinnerDialog;
    private ListView theList;

    private WakeLock wakeLock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) Objects.requireNonNull(getSystemService(POWER_SERVICE))).newWakeLock(26, "ReportsActivity:DoNotDimScreen");
        this.datasource = PMGlobal.datasource;
        this.datasource.currentPeriod = 0;
        setContentView(R.layout.reports);
        setupView();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(this.datasource.title());
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    public void onPause() {
        super.onPause();
        this.wakeLock.release();
    }

    public void onResume() {
        super.onResume();
        this.wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        this.datasource.data = null;
        reloadData();
    }

    private void setupView() {
        this.theList = findViewById(R.id.thelist);
        this.adapter = new ReportsRowAdapter(this);
        this.theList.setAdapter(this.adapter);
        this.theList.setFocusable(false);
        this.theList.setItemsCanFocus(true);
        this.theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.periodButton = findViewById(R.id.periodbutton);
        this.periodButton.setOnClickListener(v -> openDialog());
        this.previousPeriodView = findViewById(R.id.lefttarrow);
        this.nextPeriodView = findViewById(R.id.rightarrow);
        this.previousPeriodView.setOnClickListener(getClickListener());
        this.nextPeriodView.setOnClickListener(getClickListener());
        this.balanceLabelView = findViewById(R.id.balance_label);
        this.balanceAmountView = findViewById(R.id.balance_amount);
        ((View) this.balanceLabelView.getParent().getParent()).setBackgroundResource(R.drawable.theme_gradient_black);
        this.barChartView = findViewById(R.id.barchartview);
        this.barChartView.dataSource = this.datasource;
        this.barChartView.delegate = this;
        this.pieChartView = findViewById(R.id.piechartview);
        this.pieChartView.dataSource = this.datasource;
        this.pieChartView.delegate = this;
        ((View) this.nextPeriodView.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.theList.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private OnClickListener getClickListener() {
        return v -> {
            if (v == ReportsActivity.this.nextPeriodView) {
                ReportsActivity.this.datasource.nextPeriod();
            } else if (v == ReportsActivity.this.previousPeriodView) {
                ReportsActivity.this.datasource.previousPeriod();
            }
            ReportsActivity.this.datasource.data = null;
            ReportsActivity.this.reloadData();
        };
    }

    public FilterClass getFilterForReport(ReportItem report) {
        return this.datasource.newFilterBasedOnSelectedRow(report.expense);
    }

    private void selectChartView() {
        switch (Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE)) {
            case Enums.kReportsChartTypeNone /*0*/:
                this.chartView = null;
                this.pieChartView.setVisibility(View.GONE);
                this.barChartView.setVisibility(View.GONE);
                break;
            case Enums.kReportsChartTypePie /*1*/:
                this.chartView = this.pieChartView;
                this.pieChartView.setVisibility(View.VISIBLE);
                this.barChartView.setVisibility(View.GONE);
                break;
            case Enums.kReportsChartTypeBar /*2*/:
                this.chartView = this.barChartView;
                this.pieChartView.setVisibility(View.GONE);
                this.barChartView.setVisibility(View.VISIBLE);
                break;
        }
        if (SMMoney.isLiteVersion() && this.chartView != null) {
            this.chartView.setVisibility(View.GONE);
        }
    }

    public void reloadData() {
        selectChartView();
        this.periodButton.setText(this.datasource.rangeOfPeriodAsString());
        if (this.chartView != null) {
            this.chartView.deselectChunk();
        }
        processData = true;
        updateProgressBar(0);
        executor.execute(() -> {
            ReportsActivity.this.datasource.reloadData(ReportsActivity.this);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (ReportsActivity.this.datasource.data == null) {
                    ReportsActivity.this.finishProgressBar();
                } else {
                    if (ReportsActivity.processData) {
                        ReportsActivity.this.reloadDataCallback();
                    }
                    ReportsActivity.this.finishProgressBar();
                }
            });
        });
    }

    private void reloadDataCallback() {
        this.adapter.setElements(this.datasource.data);
        loadBalanceBar();
        if (this.chartView != null) {
            this.chartView.invalidate();
        }
    }

    private void loadBalanceBar() {
        double amount = this.datasource.expenseTotal();
        this.balanceAmountView.setText(this.datasource.expenseTotalAsString());
        this.balanceLabelView.setText(Locales.kLOC_REPORT_EXPENSETOTAL);
        if (amount < 0.0d) {
            this.balanceLabelView.setTextColor(PocketMoneyThemes.redOnBlackLabelColor());
            this.balanceAmountView.setTextColor(PocketMoneyThemes.redOnBlackLabelColor());
        } else {
            this.balanceLabelView.setTextColor(ContextCompat.getColor(this, R.color.black_theme_text));
            this.balanceAmountView.setTextColor(PocketMoneyThemes.greenDepositColor());
        }
    }

    public void chartViewSelectedItem(ChartView chartView, ChartItem chartItem) {
        this.theList.setSelection(this.adapter.getElements().indexOf(((ReportChartItem) chartItem).reportItem));
    }

    public void openDialog() {
        ReportDialog reportDialog = new ReportDialog();
        reportDialog.show(getSupportFragmentManager(), "reportDialog");
    }

    @Override
    public void applyPeriodType(int periodType) {
        Prefs.setPref(Prefs.REPORTS_PERIOD, periodType);
        ReportsActivity.this.datasource.currentPeriod = periodType;
        ReportsActivity.this.datasource.data = null;
        ReportsActivity.this.reloadData();
    }

    public void updateProgressBar(int progress) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 1, progress, 0));
    }

    public void finishProgressBar() {
        processData = false;
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 0, "Process to date Completed"), 500);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_VIEW/*1*/, 0, "View Options").setIcon(R.drawable.ic_arrow_drop_down_circle);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_VIEW) {
            startActivity(new Intent(this, ReportsViewOptionsActivity.class));
            return true;
        }
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (processData) {
                processData = false;
                return true;
            }
        } else if (keyCode == 3) {
            processData = false;
        }
        return super.onKeyDown(keyCode, event);
    }
}

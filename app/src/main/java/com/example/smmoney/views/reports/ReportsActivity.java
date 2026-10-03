package com.example.smmoney.views.reports;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.example.smmoney.views.charts.compose.ModernChartsKt;
import com.example.smmoney.views.charts.items.ChartItem;
import com.example.smmoney.views.charts.items.ReportChartItem;
import androidx.compose.ui.platform.ComposeView;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;

public class ReportsActivity extends PocketMoneyActivity implements ReportDialog.ReportDialogListner {
    public static boolean processData = false;
    private static final int MENU_VIEW = 1;
    private static final int MSG_PROGRESS_FINISH = 0;
    private static final int MSG_PROGRESS_UPDATE = 1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ReportsRowAdapter adapter;
    private TextView balanceAmountView;
    private TextView balanceLabelView;
    private ComposeView chartComposeContainer;

    private ReportDataSource datasource;
    private View nextPeriodView;
    private Button periodButton;
    private View previousPeriodView;
    private PocketMoneyProgressDialog progressDialog = null;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS_FINISH:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    return;
                case MSG_PROGRESS_UPDATE:
                    if (progressDialog == null || !progressDialog.isShowing()) {
                        progressDialog = new PocketMoneyProgressDialog(ReportsActivity.this);
                        progressDialog.setMessage("Generating Report.\nPlease wait...");
                        progressDialog.setCancelable(true);
                        progressDialog.show();
                    }
                    progressDialog.setProgress(msg.arg1);
                    return;
                default:
            }
        }
    };
    private ListView theList;

    private WakeLock wakeLock;

    private View emptyChartStateContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
        this.emptyChartStateContainer = findViewById(R.id.empty_chart_state_container);
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
        this.chartComposeContainer = findViewById(R.id.chart_compose_container);
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
        int chartType = Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE);
        if (this.chartComposeContainer != null) {
            if (chartType == Enums.kReportsChartTypeNone || SMMoney.isLiteVersion()) {
                this.chartComposeContainer.setVisibility(View.GONE);
            } else {
                this.chartComposeContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    public void reloadData() {
        selectChartView();
        this.periodButton.setText(this.datasource.rangeOfPeriodAsString());
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
        updateComposeViews();
        
        // Show empty state if there is no data
        if (this.datasource.data == null || this.datasource.data.isEmpty()) {
            this.emptyChartStateContainer.setVisibility(View.VISIBLE);
            
            View chartIconContainer = this.emptyChartStateContainer.findViewById(R.id.empty_chart_icon_container);
            if (chartIconContainer != null) {
                int chartType = Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE);
                if (chartType == Enums.kReportsChartTypeNone || SMMoney.isLiteVersion()) {
                    chartIconContainer.setVisibility(View.GONE);
                    this.chartComposeContainer.setVisibility(View.GONE);
                } else {
                    chartIconContainer.setVisibility(View.VISIBLE);
                    this.chartComposeContainer.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            this.emptyChartStateContainer.setVisibility(View.GONE);
            selectChartView(); // Restore chart visibility based on preference
        }
    }
    
    private void updateComposeViews() {
        if (this.datasource.data != null) {
            List<ChartItem> chartItems = new ArrayList<>();
            for (ReportItem ri : this.datasource.data) {
                if (ri.checked) {
                    ReportChartItem rci = new ReportChartItem(ri.amount, ri.expense, ri.color);
                    rci.reportItem = ri;
                    rci.percent = ri.percent;
                    chartItems.add(rci);
                }
            }
            
            int chartType = Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE);
            
            if (chartType == Enums.kReportsChartTypePie) {
                ModernChartsKt.setPieChartContent(
                    this.chartComposeContainer,
                    chartItems,
                    chartItem -> {
                        chartViewSelectedItem(chartItem);
                        return Unit.INSTANCE;
                    }
                );
            } else if (chartType == Enums.kReportsChartTypeBar) {
                ModernChartsKt.setBarChartContent(
                    this.chartComposeContainer,
                    chartItems,
                    chartItem -> {
                        chartViewSelectedItem(chartItem);
                        return Unit.INSTANCE;
                    }
                );
            }
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
            this.balanceLabelView.setTextColor(PocketMoneyThemes.balanceBarTextViewColor());
            this.balanceAmountView.setTextColor(PocketMoneyThemes.greenDepositColor());
        }
    }

    public void chartViewSelectedItem(ChartItem chartItem) {
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
        mHandler.sendMessage(Message.obtain(mHandler, MSG_PROGRESS_UPDATE, progress, 0));
    }

    public void finishProgressBar() {
        processData = false;
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_PROGRESS_FINISH, "Process to date Completed"), 500);
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

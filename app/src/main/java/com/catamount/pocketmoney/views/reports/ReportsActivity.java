package com.catamount.pocketmoney.views.reports;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PMGlobal;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.charts.ChartViewDelegate;
import com.catamount.pocketmoney.views.charts.items.ChartItem;
import com.catamount.pocketmoney.views.charts.items.ReportChartItem;
import com.catamount.pocketmoney.views.charts.views.ChartBarView;
import com.catamount.pocketmoney.views.charts.views.ChartPieView;
import com.catamount.pocketmoney.views.charts.views.ChartView;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;

public class ReportsActivity extends PocketMoneyActivity implements ChartViewDelegate {
    public static boolean processData = false;
    private final int MENU_PERIOD = 1;
    private final int MENU_VIEW = 1;
    private final int MSG_PROGRESS_FINISH = 0;
    private final int MSG_PROGRESS_UPDATE = 1;
    private ReportsRowAdapter adapter;
    private TextView balanceAmountView;
    private TextView balanceLabelView;
    private ChartBarView barChartView;
    private ChartView chartView;
    private ReportDataSource datasource;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PocketMoneyThemes.kThemeBlack /*0*/:
                    if (ReportsActivity.this.progressDialog != null) {
                        if (ReportsActivity.this.progressDialog.isShowing()) {
                            ReportsActivity.this.progressDialog.dismiss();
                        }
                        ReportsActivity.this.progressDialog.setProgress(0);
                        return;
                    }
                    return;
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    if (ReportsActivity.this.progressDialog == null || !ReportsActivity.this.progressDialog.isShowing()) {
                        ReportsActivity.this.progressDialog = new ProgressDialog(ReportsActivity.this);
                        ReportsActivity.this.progressDialog.setProgressStyle(1);
                        ReportsActivity.this.progressDialog.setMessage("Generating Report.\nPlease wait...");
                        ReportsActivity.this.progressDialog.setCancelable(true);
                        ReportsActivity.this.progressDialog.setOnKeyListener(new OnKeyListener() {
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == 3) {
                                    ReportsActivity.processData = false;
                                } else if (keyCode == 4) {
                                    ReportsActivity.processData = false;
                                }
                                return true;
                            }
                        });
                        ReportsActivity.this.progressDialog.show();
                    }
                    if (ReportsActivity.this.progressDialog != null && ReportsActivity.this.progressDialog.isShowing()) {
                        ReportsActivity.this.progressDialog.setProgress(msg.arg1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private View nextPeriodView;
    private Button periodButton;
    private ChartPieView pieChartView;
    private View previousPeriodView;
    private ProgressDialog progressDialog = null;
    private ProgressDialog progressSpinnerDialog;
    private ListView theList;
    private TextView titleTextView;
    private WakeLock wakeLock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(26, "ReportsActivity:DoNotDimScreen");
        this.datasource = PMGlobal.datasource;
        this.datasource.currentPeriod = 0;
        setContentView(R.layout.reports);
        setupView();
        setTitle(this.datasource.title());
    }

    public void onPause() {
        super.onPause();
        this.wakeLock.release();
    }

    public void onResume() {
        super.onResume();
        this.wakeLock.acquire();
        this.datasource.data = null;
        reloadData();
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    public void setupView() {
        this.theList = findViewById(R.id.thelist);
        this.adapter = new ReportsRowAdapter(this);
        this.theList.setAdapter(this.adapter);
        this.theList.setFocusable(false);
        this.theList.setItemsCanFocus(true);
        this.theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.periodButton = findViewById(R.id.periodbutton);
        this.periodButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ReportsActivity.this.showDialog(1);
            }
        });
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
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        this.titleTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ReportsActivity.this.openOptionsMenu();
            }
        });
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
    }

    private OnClickListener getClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                if (v == ReportsActivity.this.nextPeriodView) {
                    ReportsActivity.this.datasource.nextPeriod();
                } else if (v == ReportsActivity.this.previousPeriodView) {
                    ReportsActivity.this.datasource.previousPeriod();
                }
                ReportsActivity.this.datasource.data = null;
                ReportsActivity.this.reloadData();
            }
        };
    }

    public FilterClass getFilterForReport(ReportItem report) {
        return this.datasource.newFilterBasedOnSelectedRow(report.expense);
    }

    private void selectChartView() {
        switch (Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE)) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                this.chartView = null;
                this.pieChartView.setVisibility(View.GONE);
                this.barChartView.setVisibility(View.GONE);
                break;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                this.chartView = this.pieChartView;
                this.pieChartView.setVisibility(View.VISIBLE);
                this.barChartView.setVisibility(View.GONE);
                break;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                this.chartView = this.barChartView;
                this.pieChartView.setVisibility(View.GONE);
                this.barChartView.setVisibility(View.VISIBLE);
                break;
        }
        if (PocketMoney.isLiteVersion() && this.chartView != null) {
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
        new AsyncTask() {
            protected Object doInBackground(Object... arg0) {
                ReportsActivity.this.datasource.reloadData(ReportsActivity.this);
                if (ReportsActivity.this.datasource.data == null) {
                    ReportsActivity.this.finishProgressBar();
                } else if (!(ReportsActivity.this.chartView == null || PocketMoney.isLiteVersion())) {

                }
                return null;
            }

            protected void onPostExecute(Object result) {
                if (ReportsActivity.processData) {
                    ReportsActivity.this.reloadDataCallback();
                    ReportsActivity.this.chartView.reloadData(false);
                }
                ReportsActivity.this.finishProgressBar();
            }
        }.execute();
    }

    public void reloadDataCallback() {
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
        } else {
            this.balanceLabelView.setTextColor(-1);
        }
    }

    public void chartViewSelectedItem(ChartView chartView, ChartItem chartItem) {
        this.theList.setSelection(this.adapter.getElements().indexOf(((ReportChartItem) chartItem).reportItem));
    }

    protected Dialog onCreateDialog(int id) {
        CharSequence[] items = new CharSequence[]{Locales.kLOC_REPORTS_ONEMONTH, Locales.kLOC_REPORTS_TWOMONTHS, Locales.kLOC_REPORTS_THREEMONTHS, Locales.kLOC_REPORTS_SIXMONTHS, Locales.kLOC_REPORTS_ONEYEAR, Locales.kLOC_PREFERENCES_SHOW_ALL};
        Builder builder = new Builder(this);
        builder.setTitle(Locales.kLOC_BUDGETS_PERIOD);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                int periodType;
                switch (item) {
                    case PocketMoneyThemes.kThemeBlack /*0*/:
                        periodType = 0;
                        break;
                    case SplitsActivity.RESULT_CHANGED /*1*/:
                        periodType = 1;
                        break;
                    case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                        periodType = 2;
                        break;
                    case SplitsActivity.REQUEST_EDIT /*3*/:
                        periodType = 3;
                        break;
                    case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                        periodType = 4;
                        break;
                    default:
                        periodType = 5;
                        break;
                }
                Prefs.setPref(Prefs.REPORTS_PERIOD, periodType);
                ReportsActivity.this.datasource.currentPeriod = periodType;
                ReportsActivity.this.datasource.data = null;
                dialog.dismiss();
                ReportsActivity.this.reloadData();
            }
        });
        return builder.create();
    }

    public void updateProgressBar(int progress) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 1, progress, 0));
    }

    public void finishProgressBar() {
        processData = false;
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 0, "Process to date Completed"), 500);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "View Options").setIcon(R.drawable.ic_dialog_menu_generic);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                startActivity(new Intent(this, ReportsViewOptionsActivity.class));
                return true;
            default:
                return false;
        }
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

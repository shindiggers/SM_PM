package com.catamount.pocketmoney.views.transactions;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.database.AccountDB;
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
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.records.SplitsClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.BalanceBar;
import com.catamount.pocketmoney.views.HandlerActivity;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.accounts.AccountsActivity;
import com.catamount.pocketmoney.views.filters.FiltersMainActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.reports.AccountsReportDataSource;
import com.catamount.pocketmoney.views.reports.CategoryReportDataSource;
import com.catamount.pocketmoney.views.reports.ClassReportDataSource;
import com.catamount.pocketmoney.views.reports.PayeeReportDataSource;
import com.catamount.pocketmoney.views.reports.ReportsActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class TransactionsActivity extends PocketMoneyActivity implements HandlerActivity {
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    private final int DATE_DIALOG_ID = 8;
    private final int EMAIL_CSV = 2;
    private final int EMAIL_QIF = 0;
    private final int EMAIL_TDF = 1;
    private final int IMPORT_PROGRESS_DIALOG = 9;
    private final int MENU_EMAILTRANSFERS = 3;
    private final int MENU_FILETRANSFERS = 1;
    private final int MENU_FILTER = 4;
    private final int MENU_NEW = 1;
    private final int MENU_REPORTS = 5;
    private final int MENU_REPORTS_ACCOUNT = 6;
    private final int MENU_REPORTS_CATEGORY = 7;
    private final int MENU_REPORTS_CLASS = 8;
    private final int MENU_REPORTS_PAYEE = 9;
    private final int MENU_SDCARDTRANSFER = 5;
    private final int MENU_SD_EXPORT = 7;
    private final int MENU_SD_IMPORT = 6;
    private final int MENU_SEARCH = 24;
    private final int MENU_TOOLS = 3;
    private final int MENU_TOOLS_ADJUSTBALANCE = 13;
    private final int MENU_TOOLS_FILETRANSFERS = 10;
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL = 17;
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL_CSV = 23;
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL_QIF = 21;
    private final int MENU_TOOLS_FILETRANSFERS_EMAIL_TDF = 22;
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT = 16;
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT_CSV = 20;
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT_QIF = 18;
    private final int MENU_TOOLS_FILETRANSFERS_EXPORT_TDF = 19;
    private final int MENU_TOOLS_GOTODATE = 11;
    private final int MENU_TOOLS_MARKASCLEAR = 14;
    private final int MENU_TOOLS_ROLLUP = 15;
    private final int MENU_VIEW = 2;
    private final int MENU_WIFITRANSFERS = 2;
    private final int MENU_WIFI_EXPORT = 4;
    public final int REQUEST_EDIT = 2;
    public final int REQUEST_NEW = 1;
    public final int TRANSACTION_REQUEST_EMAIL = 2;
    public final int TRANSACTION_REQUEST_FILTER = 1;
    private FilterClass _filter;
    private TransactionRowAdapter adapter;
    private RadioButton allButton;
    private BalanceBar balanceBar;
    private RadioButton clearedButton;
    private Context context;
    private String emailFileLocation;
    private ArrayList<String> fileNames;
    private boolean firstOpenOfView;
    private ListView listView;
    private OnDateSetListener mDateSetListener = new OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            GregorianCalendar newCal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            boolean descending = Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING);
            int i = 0;
            Iterator it = TransactionsActivity.this.adapter.getElements().iterator();
            while (it.hasNext()) {
                TransactionClass transaction = (TransactionClass) it.next();
                if ((descending && transaction.getDate().before(newCal)) || (!descending && transaction.getDate().after(newCal))) {
                    break;
                }
                i++;
            }
            ((ListView) TransactionsActivity.this.findViewById(R.id.the_list)).setSelection(i);
        }
    };
    private Handler mHandler = null;
    private int msgEmail = -1;
    private RadioButton pendingButton;
    private ProgressDialog progressDialog = null;
    private EditText searchEditText;
    private LinearLayout searchView;
    private boolean shouldEmail = false;
    private TextView titleTextView;
    private WakeLock wakeLock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(26, "TransactionsActivity:DoNotDimScreen");
        this.context = this;
        this._filter = (FilterClass) getIntent().getExtras().get("Filter");
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.transactions, null);
        setupView(layout);
        setContentView(layout);
        //findViewById(R.id.adView).setVisibility(View.GONE);

        this.firstOpenOfView = true;
        getActionBar().setTitle(this._filter.customFilter() ? Locales.kLOC_TOOLS_FILTER + " - " + this._filter.getFilterName() : this._filter.getAccount());
        getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
    }

    protected void onResume() {
        super.onResume();
        reloadData();
        reloadBalanceBar();
        if (this.firstOpenOfView) {
            ListView listView = findViewById(R.id.the_list);
            if (Prefs.getStringPref(Prefs.NEWESTTRANSACTIONFIRST).equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING)) {
                listView.setSelection(0);
            } else {
                listView.setSelection(this.adapter.getCount());
            }
            this.firstOpenOfView = false;
        }
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    public void setupView(LinearLayout layout) {
        createHandler();
        this.balanceBar = layout.findViewById(R.id.balancebar);
        this.balanceBar.nextButton.setOnClickListener(getBalanceBarClickListener());
        this.balanceBar.previousButton.setOnClickListener(getBalanceBarClickListener());
        this.listView = layout.findViewById(R.id.the_list);
        this.listView.setItemsCanFocus(true);
        this.adapter = new TransactionRowAdapter(this);
        this.listView.setAdapter(this.adapter);
        this.listView.setFocusable(false);
        this.searchView = layout.findViewById(R.id.searchlayout);
        this.searchEditText = layout.findViewById(R.id.searcheditext);
        this.searchEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                TransactionsActivity.this._filter.setSpotlight(TransactionsActivity.this.searchEditText.getText().toString());
                TransactionsActivity.this.reloadData();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        View aView = layout.findViewById(R.id.radiogroup);
        this.pendingButton = aView.findViewById(R.id.pendingbutton);
        this.clearedButton = aView.findViewById(R.id.clearedbutton);
        this.allButton = aView.findViewById(R.id.allbutton);
        ((RadioGroup) aView).setOnCheckedChangeListener(getRadioChangedListener());
        layout.findViewById(R.id.add_button).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TransactionsActivity.this.newTransaction();
            }
        });
        FrameLayout theView = layout.findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        theView.setVisibility(View.GONE);
    }

    public void toggleSearch() {
        int i = 8;
        LinearLayout linearLayout = this.searchView;
        if (this.searchView.getVisibility() == View.GONE) {
            i = 0;
        }
        linearLayout.setVisibility(i);
        this.searchView.invalidate();
    }

    public void reloadData() {
        this.adapter.setElements(TransactionDB.queryWithFilter(this._filter));
        this.adapter.notifyDataSetChanged();
    }

    public void markAsClear() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Are you sure you want to mark all the transactions clear?");
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Iterator it = TransactionsActivity.this.adapter.getElements().iterator();
                while (it.hasNext()) {
                    TransactionClass trans = (TransactionClass) it.next();
                    trans.hydrate();
                    trans.setCleared(true);
                    trans.saveToDatabase();
                }
                TransactionsActivity.this.reloadData();
            }
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    private void newTransaction() {
        boolean z = true;
        Intent i = new Intent(this, TransactionEditActivity.class);
        TransactionClass trans = new TransactionClass();
        trans.setAccount(this._filter.getAccount());
        if (this._filter.getCleared() != 1) {
            z = false;
        }
        trans.setCleared(z);
        if (this._filter.getCategory() != null) {
            trans.setCategory(this._filter.getCategory());
        }
        if (this._filter.getPayee() != null) {
            trans.setPayee(this._filter.getPayee());
        }
        AccountClass a1 = AccountDB.recordFor(this._filter.getAccount());
        if (a1 != null) {
            trans.setCurrencyCode(a1.getCurrencyCode());
        }
        trans.dirty = false;
        trans.getSplits().get(0).dirty = false;
        i.putExtra("Transaction", trans);
        startActivity(i);
    }

    private void adjustBalance() {
        if (this._filter.getAccount() == null || this._filter.getAccount().length() == 0 || AccountDB.recordFor(this._filter.getAccount()) == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("You need to have an account selected that exists to adjust the balance");
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            alert.show();
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(12290);
        alert.setMessage(Locales.kLOC_TOOLS_RECONCILE_BODY);
        alert.setView(input);
        alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String adjustString = input.getText().toString().trim();
                if (adjustString != null && adjustString.length() > 0) {
                    TransactionsActivity.this.adjustBalanceAlert(Double.valueOf(Double.parseDouble(adjustString)));
                }
            }
        });
        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    private void adjustBalanceAlert(double newBalance) {
        AccountClass act = AccountDB.recordFor(this._filter.getAccount());
        final double clearedAdjust = act.balanceOfType(1);
        final double futureAdjust = act.balanceOfType(0);
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        final double d = newBalance;
        final double d2 = newBalance;
        alt_bld.setCancelable(false).setMessage(Locales.kLOC_TOOLS_RECONCILE_ALLCLEARED).setPositiveButton(Locales.kLOC_GENERAL_CLEARED, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                TransactionsActivity.this.adjustBalanceConfirm(true, d - clearedAdjust);
            }
        }).setNegativeButton(Locales.kLOC_TOOLS_RECONCILE_ALL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                TransactionsActivity.this.adjustBalanceConfirm(false, d2 - futureAdjust);
            }
        });
        alt_bld.create().show();
    }

    private void adjustBalanceConfirm(final boolean onlyCleared, final double newBalance) {
        AccountClass act = AccountDB.recordFor(this._filter.getAccount());
        String titleString = getString(R.string.kLOC_TOOLS_RECONCILE_CONFIRM, act.formatAmountAsCurrency(newBalance));
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setCancelable(false).setMessage(titleString).setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                TransactionsActivity.this.adjustBalanceToAmount(onlyCleared, newBalance);
            }
        }).setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alt_bld.create().show();
    }

    private void adjustBalanceToAmount(boolean onlyCleared, double newBalance) {
        AccountClass act = AccountDB.recordFor(this._filter.getAccount());
        TransactionClass trans = new TransactionClass();
        trans.setAccount(this._filter.getAccount());
        trans.setSubTotal(newBalance);
        trans.setAmount(newBalance);
        trans.initType();
        trans.setCleared(onlyCleared);
        trans.setCategory(Locales.kLOC_TOOLS_RECONCILE_CATEGORY);
        trans.setPayee(Locales.kLOC_TOOLS_RECONCILE_PAYEE);
        trans.setCurrencyCode(act.getCurrencyCode());
        trans.saveToDatabase();
        reloadData();
    }

    private OnClickListener getBalanceBarClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                int i = Prefs.getIntPref(Prefs.BALANCETYPE);
                if (v.equals(TransactionsActivity.this.balanceBar.nextButton)) {
                    i = TransactionsActivity.this.balanceBar.nextBalanceTypeAfter(i);
                } else {
                    i = TransactionsActivity.this.balanceBar.nextBalanceTypeBefore(i);
                }
                Prefs.setPref(Prefs.BALANCETYPE, i);
                TransactionsActivity.this.reloadBalanceBar();
                TransactionsActivity.this.reloadData();
            }
        };
    }

    public void reloadBalanceBar() {
        if (this._filter != null && this._filter.customFilter()) {
            Prefs.setPref(Prefs.BALANCETYPE, 5);
        } else if (Prefs.getIntPref(Prefs.BALANCETYPE) == 5) {
            Prefs.setPref(Prefs.BALANCETYPE, 2);
        }
        int balanceType = Prefs.getIntPref(Prefs.BALANCETYPE);
        double balance = 0.0d;
        AccountClass act = this._filter.getAccount() != null ? AccountDB.recordFor(this._filter.getAccount()) : null;
        this.balanceBar.setFilter(this._filter);
        if (this._filter.allAccounts()) {
            balanceType = 5;
            balance = TransactionDB.balanceWithFilter(this._filter);
        } else if (balanceType == 5) {
            balance = TransactionDB.balanceWithFilter(this._filter);
        } else if (act != null) {
            balance = act.balanceOfType(balanceType);
        }
        if (5 == this._filter.getType() || act == null || !act.balanceExceedsLimit()) {
            this.balanceBar.balanceAmountTextView.setTextColor(-1);
        } else {
            this.balanceBar.balanceAmountTextView.setTextColor(-65536);
        }
        if (this._filter.getAccount() == null || this._filter.allAccounts()) {
            this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(balance));
        } else {
            this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(balance));
        }
        this.balanceBar.balanceTypeTextView.setText(AccountDB.totalWorthLabel(balanceType));
        this.balanceBar.balanceTypeTextView.setTextColor(-1);
    }

    public void rollupAction() {
        if (this.adapter.getElements().size() != 0) {
            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
            alt_bld.setMessage(getString(R.string.kLOC_TOOLS_ROLLUP_ALERT, new StringBuilder(String.valueOf(this.adapter.getElements().size())).toString())).setCancelable(false).setTitle(Locales.kLOC_TOOLS_ROLLUP).setPositiveButton(Locales.kLOC_GENERAL_YES, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(TransactionsActivity.this.context);
                    alt_bld.setMessage(Locales.kLOC_TOOLS_ROLLUP_CONFIRM).setCancelable(false).setTitle(Locales.kLOC_TOOLS_ROLLUP).setPositiveButton(Locales.kLOC_GENERAL_YES, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TransactionDB.rollupTransactionsInFilter(TransactionsActivity.this.adapter.getElements(), TransactionsActivity.this._filter);
                            TransactionsActivity.this.reloadData();
                        }
                    }).setNegativeButton(Locales.kLOC_GENERAL_NO, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    alt_bld.create().show();
                }
            }).setNegativeButton(Locales.kLOC_GENERAL_NO, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alt_bld.create().show();
        }
    }

    private void exportOFXToSD() {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.show();
        new Thread() {
            public void run() {
                String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(TransactionsActivity.this._filter);
                ImportExportOFX exportofx = new ImportExportOFX(TransactionsActivity.this.context, new StringBuilder(String.valueOf(fileDir)).append("/PocketMoneyBackup/").append(TransactionsActivity.this._filter.allAccounts() ? "PocketMoney" : TransactionsActivity.this._filter.getAccount()).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).append(".qfx").toString());
                exportofx.filter = TransactionsActivity.this._filter;
                if (!TransactionsActivity.this._filter.allAccounts()) {
                    exportofx.accountNameBeingImported = TransactionsActivity.this._filter.getAccount();
                }
                exportofx.exportRecords(query);
                pd.dismiss();
            }
        }.start();
    }

    private void generateEmailForOFX(ArrayList<String> fileNames) {
        Intent emailIntent = new Intent("android.intent.action.SEND");
        emailIntent.setType("text/ofx");
        Prefs.exportDB(this);
        Iterator it = fileNames.iterator();
        while (it.hasNext()) {
            emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + it.next()));
        }
        emailIntent.putExtra("android.intent.extra.SUBJECT", "PocketMoney OFX/QFX File");
        emailIntent.putExtra("android.intent.extra.TEXT", getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "OFX/QFX", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
        this.fileNames = fileNames;
        startActivityForResult(emailIntent, 2);
    }

    private void generateOFXForEmail() {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.show();
        new Thread() {
            public void run() {
                String pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                FilterClass filter = new FilterClass();
                final ArrayList<String> fileNames = new ArrayList();
                ArrayList<TransactionClass> query = TransactionDB.queryWithFilter(TransactionsActivity.this._filter);
                String fileName = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/").append(TransactionsActivity.this._filter.allAccounts() ? "PocketMoney" : TransactionsActivity.this._filter.getAccount()).append("-").append(CalExt.descriptionWithTimestamp(new GregorianCalendar())).append(".qfx").toString();
                fileNames.add(fileName);
                ImportExportOFX exportofx = new ImportExportOFX(TransactionsActivity.this.context, fileName);
                exportofx.filter = TransactionsActivity.this._filter;
                if (!TransactionsActivity.this._filter.allAccounts()) {
                    exportofx.accountNameBeingImported = TransactionsActivity.this._filter.getAccount();
                }
                exportofx.exportRecords(query);
                pd.dismiss();
                TransactionsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        TransactionsActivity.this.generateEmailForOFX(fileNames);
                    }
                });
            }
        }.start();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, 1, 0, Locales.kLOC_TRANSACTION_NEW);
        item.setIcon(R.drawable.circleplus);
        item.setShowAsAction(2);
        menu.add(0, 2, 0, Locales.kLOC_VIEW_OPTIONS).setIcon(R.drawable.ic_dialog_menu_generic);
        SubMenu toolsMenu = menu.addSubMenu(Locales.kLOC_GENERAL_TOOLS);
        toolsMenu.setIcon(R.drawable.icon);
        menu.add(0, 24, 0, Locales.kLOC_TOOLS_SEARCH).setIcon(R.drawable.places_ic_search);
        toolsMenu.add(0, 10, 0, Locales.kLOC_TOOLS_FILETRANSFERS);
        toolsMenu.add(0, 11, 0, Locales.kLOC_TRANSACTIONS_OPTIONS_GOTO);
        toolsMenu.add(0, 13, 0, Locales.kLOC_TOOLS_RECONCILE);
        toolsMenu.add(0, 14, 0, Locales.kLOC_TOOLS_MARKCLEARED);
        item = toolsMenu.add(0, 15, 0, Locales.kLOC_TOOLS_ROLLUP);
        menu.add(0, 4, 0, Locales.kLOC_TOOLS_FILTERS).setIcon(R.drawable.ic_dialog_menu_generic);
        SubMenu reportsMenu = menu.addSubMenu(Locales.kLOC_GENERAL_REPORTS);
        reportsMenu.setIcon(R.drawable.icon);
        reportsMenu.add(0, 6, 0, Locales.kLOC_TOOLS_ACCOUNTREPORT);
        reportsMenu.add(0, 7, 0, Locales.kLOC_TOOLS_CATEGORYREPORT);
        reportsMenu.add(0, 8, 0, Locales.kLOC_TOOLS_CLASSREPORT);
        reportsMenu.add(0, 9, 0, Locales.kLOC_TOOLS_PAYEEREPORT);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                newTransaction();
                return true;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                startActivity(new Intent(this, TransactionViewOptionsActivity.class));
                return true;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                if (AccountsActivity.isLite(this)) {
                    AccountsActivity.displayLiteDialog(this);
                    return true;
                }
                i = new Intent(this, FiltersMainActivity.class);
                i.putExtra("Filter", this._filter);
                startActivityForResult(i, 1);
                return true;
            case LookupsListActivity.CLASS_LOOKUP /*6*/:
                AccountsReportDataSource ds = new AccountsReportDataSource(this.adapter.getElements(), this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds;
                startActivity(i);
                break;
            case LookupsListActivity.ID_LOOKUP /*7*/:
                CategoryReportDataSource ds2 = new CategoryReportDataSource(this.adapter.getElements(), this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds2;
                startActivity(i);
                break;
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                ClassReportDataSource ds3 = new ClassReportDataSource(this.adapter.getElements(), this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds3;
                startActivity(i);
                break;
            case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                PayeeReportDataSource ds4 = new PayeeReportDataSource(this.adapter.getElements(), this._filter);
                i = new Intent(this, ReportsActivity.class);
                PMGlobal.datasource = ds4;
                startActivity(i);
                break;
            case LookupsListActivity.FILTER_DATES /*10*/:
                showDialog(1);
                break;
            case LookupsListActivity.FILTER_PAYEES /*11*/:
                showDialog(8);
                return true;
            case LookupsListActivity.FILTER_CLEARED /*13*/:
                adjustBalance();
                break;
            case LookupsListActivity.FILTER_CATEGORIES /*14*/:
                markAsClear();
                break;
            case LookupsListActivity.FILTER_CLASSES /*15*/:
                rollupAction();
                return true;
            case Enums.kDesktopSyncStateUDIDProcessed /*24*/:
                toggleSearch();
                return true;
        }
        return false;
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        switch (id) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                CharSequence[] items = new CharSequence[]{Locales.kLOC_TOOLS_EMAILREGISTER, Locales.kLOC_TOOLS_FILETRANSFERS_SDCARD};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                TransactionsActivity.this.showDialog(3);
                                return;
                            case SplitsActivity.RESULT_CHANGED /*1*/:
                                TransactionsActivity.this.showDialog(7);
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case SplitsActivity.REQUEST_EDIT /*3*/:
                CharSequence[] items3 = new CharSequence[]{"QIF", "TDF", "CSV", "OFX/QFX"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS_EMAIL);
                builder.setItems(items3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Intent emailIntent = new Intent("android.intent.action.SEND");
                        String pmExternalPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                TransactionsActivity.this.msgEmail = 0;
                                TransactionsActivity.this.shouldEmail = true;
                                TransactionsActivity.this.emailFileLocation = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/PocketMoney.qif").toString();
                                Log.i("****** EMAIL", "EXPORTING QIF");
                                final String fl = TransactionsActivity.this.emailFileLocation;
                                new Thread() {
                                    ProgressDialog pd;

                                    public void run() {
                                        ImportExportQIF exportqif = new ImportExportQIF(TransactionsActivity.this);
                                        exportqif.setFilter(TransactionsActivity.this._filter);
                                        exportqif.QIFPath = fl;
                                        exportqif.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                        this.pd.dismiss();
                                    }
                                }.start();
                                return;
                            case SplitsActivity.RESULT_CHANGED /*1*/:
                                TransactionsActivity.this.msgEmail = 1;
                                TransactionsActivity.this.shouldEmail = true;
                                TransactionsActivity.this.emailFileLocation = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/PocketMoney.txt").toString();
                                final String fl2 = TransactionsActivity.this.emailFileLocation;
                                new Thread() {
                                    ProgressDialog pd;

                                    public void run() {
                                        ImportExportTDF exporttdf = new ImportExportTDF(TransactionsActivity.this);
                                        exporttdf.CSVPath = fl2;
                                        exporttdf.setFilter(TransactionsActivity.this._filter);
                                        exporttdf.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                        this.pd.dismiss();
                                    }
                                }.start();
                                return;
                            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                                TransactionsActivity.this.msgEmail = 2;
                                TransactionsActivity.this.shouldEmail = true;
                                TransactionsActivity.this.emailFileLocation = new StringBuilder(String.valueOf(pmExternalPath)).append("/PocketMoneyBackup/PocketMoney.csv").toString();
                                final String fl3 = TransactionsActivity.this.emailFileLocation;
                                new Thread() {
                                    ProgressDialog pd;

                                    public void run() {
                                        ImportExportCSV exportcsv = new ImportExportCSV(TransactionsActivity.this);
                                        exportcsv.CSVPath = fl3;
                                        exportcsv.setFilter(TransactionsActivity.this._filter);
                                        exportcsv.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                        this.pd.dismiss();
                                    }
                                }.start();
                                return;
                            case SplitsActivity.REQUEST_EDIT /*3*/:
                                TransactionsActivity.this.generateOFXForEmail();
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                CharSequence[] items4 = new CharSequence[]{"QIF", "TDF", "CSV"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle(Locales.kLOC_TOOLS_FILETRANSFERS);
                builder.setItems(items4, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                ImportExportQIF exportqif = new ImportExportQIF(TransactionsActivity.this);
                                exportqif.setFilter(TransactionsActivity.this._filter);
                                exportqif.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                return;
                            case SplitsActivity.RESULT_CHANGED /*1*/:
                                ImportExportTDF exporttdf = new ImportExportTDF(TransactionsActivity.this);
                                exporttdf.setFilter(TransactionsActivity.this._filter);
                                exporttdf.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                return;
                            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                                ImportExportCSV exportcsv = new ImportExportCSV(TransactionsActivity.this);
                                exportcsv.setFilter(TransactionsActivity.this._filter);
                                exportcsv.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
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
                builder.setItems(items6, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case PocketMoneyThemes.kThemeBlack /*0*/:
                                new Thread() {
                                    ProgressDialog pd;

                                    {
                                        this.pd = ProgressDialog.show(TransactionsActivity.this, "Exporting", "Exporting records, please wait");
                                    }

                                    public void run() {
                                        ImportExportQIF exportqif = new ImportExportQIF(TransactionsActivity.this);
                                        exportqif.setFilter(TransactionsActivity.this._filter);
                                        exportqif.exportRecords(TransactionDB.queryWithFilterOrderByAccount(TransactionsActivity.this._filter));
                                        this.pd.dismiss();
                                    }
                                }.start();
                                return;
                            case SplitsActivity.RESULT_CHANGED /*1*/:
                                new Thread() {
                                    ProgressDialog pd;

                                    {
                                        this.pd = ProgressDialog.show(TransactionsActivity.this, "Exporting", "Exporting records, please wait");
                                    }

                                    public void run() {
                                        ImportExportTDF exporttdf = new ImportExportTDF(TransactionsActivity.this);
                                        exporttdf.setFilter(TransactionsActivity.this._filter);
                                        exporttdf.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                        this.pd.dismiss();
                                    }
                                }.start();
                                return;
                            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                                new Thread() {
                                    ProgressDialog pd;

                                    {
                                        this.pd = ProgressDialog.show(TransactionsActivity.this, "Exporting", "Exporting records, please wait");
                                    }

                                    public void run() {
                                        ImportExportCSV exportcsv = new ImportExportCSV(TransactionsActivity.this);
                                        exportcsv.setFilter(TransactionsActivity.this._filter);
                                        exportcsv.exportRecords(TransactionDB.queryWithFilter(TransactionsActivity.this._filter));
                                        this.pd.dismiss();
                                    }
                                }.start();
                                return;
                            case SplitsActivity.REQUEST_EDIT /*3*/:
                                TransactionsActivity.this.exportOFXToSD();
                                return;
                            default:
                                return;
                        }
                    }
                });
                return builder.create();
            case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                GregorianCalendar theDate;
                int firstPosition = ((ListView) findViewById(R.id.the_list)).getFirstVisiblePosition();
                if (this.adapter.getCount() == 0) {
                    theDate = new GregorianCalendar();
                } else {
                    theDate = ((TransactionClass) this.adapter.getItem(firstPosition)).getDate();
                }
                return new DatePickerDialog(this, this.mDateSetListener, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH));
            case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                if (this.progressDialog == null) {
                    this.progressDialog = new ProgressDialog(this);
                    Log.i("*** Progress Dialog **", "Creating");
                }
                this.progressDialog.setProgressStyle(1);
                this.progressDialog.setMessage("Exporting...\n\nWarning: This may take several minutes");
                this.progressDialog.setCancelable(true);
                return this.progressDialog;
            default:
                return null;
        }
    }

    public void displayError(String msg) {
        AlertDialog alert = new AlertDialog.Builder(this.context).create();
        alert.setTitle("Error");
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        TransactionRowHolder aHolder = (TransactionRowHolder) v.getTag();
        Intent i = new Intent();
        i.putExtra("Transaction", aHolder.transaction);
        menu.add(0, 1, 0, Locales.kLOC_GENERAL_EDIT).setIntent(i);
        menu.add(0, 3, 0, Locales.kLOC_GENERAL_DELETE).setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                Intent anIntent = new Intent(this, TransactionEditActivity.class);
                anIntent.putExtra("Transaction", (TransactionClass) b.get("Transaction"));
                startActivity(anIntent);
                return true;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                ((TransactionClass) b.get("Transaction")).transactionDelete();
                reloadData();
                reloadBalanceBar();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            switch (requestCode) {
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    if (resultCode == 1) {
                        String currentAccount = this._filter.getAccount();
                        this._filter = (FilterClass) data.getExtras().get("Filter");
                        if (this._filter.getAccount() != null && this._filter.getAccount().equals(Locales.kLOC_FILTERS_CURRENT_ACCOUNT)) {
                            this._filter.setAccount(currentAccount);
                        }
                        getActionBar().setTitle(this._filter.customFilter() ? Locales.kLOC_TOOLS_FILTER + " - " + this._filter.getFilterName() : this._filter.getAccount());
                        return;
                    }
                    return;
                case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                    Iterator it = this.fileNames.iterator();
                    while (it.hasNext()) {
                        new File((String) it.next()).delete();
                    }
                    return;
                default:
                    return;
            }
        }
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
                    case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                        if (TransactionsActivity.this.progressDialog == null) {
                            try {
                                TransactionsActivity.this.wakeLock.acquire();
                            } catch (Exception e) {
                            }
                        }
                        if (TransactionsActivity.this.progressDialog != null && TransactionsActivity.this.progressDialog.isShowing()) {
                            TransactionsActivity.this.progressDialog.setProgress(msg.arg1);
                            return;
                        }
                        return;
                    case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                        Log.i("*** MSG_PROGRESS_FINISH", "Finished");
                        if (!TransactionsActivity.this.shouldEmail && msg.obj.getClass().equals(String.class)) {
                            Toast.makeText(TransactionsActivity.this.context, (String) msg.obj, Toast.LENGTH_LONG).show();
                        }
                        try {
                            TransactionsActivity.this.wakeLock.release();
                        } catch (Exception e2) {
                        }
                        try {
                            TransactionsActivity.this.progressDialog.dismiss();
                            TransactionsActivity.this.progressDialog = null;
                        } catch (Exception e3) {
                        }
                        if (TransactionsActivity.this.progressDialog != null) {
                            Log.i("*** MSG_PROGRESS_FINISH", "Dismissing");
                            TransactionsActivity.this.progressDialog.dismiss();
                            TransactionsActivity.this.progressDialog = null;
                        }
                        if (TransactionsActivity.this.shouldEmail) {
                            Intent emailIntent = new Intent("android.intent.action.SEND");
                            switch (TransactionsActivity.this.msgEmail) {
                                case PocketMoneyThemes.kThemeBlack /*0*/:
                                    Log.i("****EMAIL", "Should Email");
                                    emailIntent.setType("text/qif");
                                    emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + TransactionsActivity.this.emailFileLocation));
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT, "QIF"));
                                    emailIntent.putExtra("android.intent.extra.TEXT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "QIF", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
                                    TransactionsActivity.this.startActivity(emailIntent);
                                    break;
                                case SplitsActivity.RESULT_CHANGED /*1*/:
                                    emailIntent.setType("text/txt");
                                    emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + TransactionsActivity.this.emailFileLocation));
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT, "TDF"));
                                    emailIntent.putExtra("android.intent.extra.TEXT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "TDF", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
                                    TransactionsActivity.this.startActivity(emailIntent);
                                    break;
                                case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                                    emailIntent.setType("text/csv");
                                    emailIntent.putExtra("android.intent.extra.STREAM", Uri.parse("file://" + TransactionsActivity.this.emailFileLocation));
                                    emailIntent.putExtra("android.intent.extra.SUBJECT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_SUBJECT, "CSV"));
                                    emailIntent.putExtra("android.intent.extra.TEXT", TransactionsActivity.this.getString(R.string.kLOC_FILETRANSFERS_EMAIL_BODY, "CSV", CalExt.descriptionWithMediumDate(new GregorianCalendar())));
                                    TransactionsActivity.this.startActivity(emailIntent);
                                    break;
                            }
                            TransactionsActivity.this.shouldEmail = false;
                            return;
                        }
                        TransactionsActivity.this.reloadData();
                        return;
                    case LookupsListActivity.CLASS_LOOKUP /*6*/:
                        if (TransactionsActivity.this.progressDialog != null) {
                            TransactionsActivity.this.progressDialog.dismiss();
                        }
                        AlertDialog alert = new AlertDialog.Builder(TransactionsActivity.this.context).create();
                        alert.setTitle("Error");
                        alert.setMessage((String) msg.obj);
                        alert.setCancelable(false);
                        alert.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message id " + msg.what);
                }
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 84) {
            return super.onKeyDown(keyCode, event);
        }
        toggleSearch();
        return true;
    }

    private OnCheckedChangeListener getRadioChangedListener() {
        return new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.pendingbutton) {
                    TransactionsActivity.this._filter.setCleared(0);
                } else if (checkedId == R.id.clearedbutton) {
                    TransactionsActivity.this._filter.setCleared(1);
                } else if (checkedId == R.id.allbutton) {
                    TransactionsActivity.this._filter.setCleared(2);
                }
                TransactionsActivity.this.reloadData();
            }
        };
    }
}

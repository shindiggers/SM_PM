package com.catamount.pocketmoney.views.repeating;

import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.database.TransactionDB;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.AccountClass;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.records.RepeatingTransactionClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.BalanceBar;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.accounts.AccountsActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;
import com.catamount.pocketmoney.views.transactions.TransactionEditActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class RepeatingActivity extends PocketMoneyActivity {
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    public final int DATE_DIALOG_ID = 1;
    public final int IMPORT_PROGRESS_DIALOG = 2;
    private final int MENU_NEW = 1;
    private final int MENU_PROCESS = 2;
    private final int MSG_PROGRESS_FINISH = 0;
    private final int MSG_PROGRESS_UPDATE = 1;
    private AccountClass account;
    private RepeatingRowAdapter adapter;
    private BalanceBar balanceBar;
    private FilterClass filter;
    public boolean isProcessingToDate = false;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PocketMoneyThemes.kThemeBlack /*0*/:
                    if (msg.obj.getClass().equals(String.class)) {
                        Toast.makeText(RepeatingActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    try {
                        RepeatingActivity.this.wakeLock.release();
                    } catch (Exception e) {
                    }
                    if (RepeatingActivity.this.progressDialog != null) {
                        if (RepeatingActivity.this.progressDialog.isShowing()) {
                            RepeatingActivity.this.progressDialog.dismiss();
                        }
                        RepeatingActivity.this.progressDialog.setProgress(0);
                    }
                    RepeatingActivity.this.reloadData();
                    return;
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    if (RepeatingActivity.this.progressDialog == null || !RepeatingActivity.this.progressDialog.isShowing()) {
                        RepeatingActivity.this.showDialog(2);
                        try {
                            RepeatingActivity.this.wakeLock.acquire();
                        } catch (Exception e2) {
                        }
                    }
                    if (RepeatingActivity.this.progressDialog != null && RepeatingActivity.this.progressDialog.isShowing()) {
                        RepeatingActivity.this.progressDialog.setProgress(msg.arg1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private ProgressDialog progressDialog = null;
    private ListView theList;
    private TextView titleTextView;
    private WakeLock wakeLock;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(26, "RepeatingActivity:DoNotDimScreen");
        this.filter = new FilterClass();
        this.filter.setType(5);
        setContentView(R.layout.repeating);
        setupView();
        setTitle(Locales.kLOC_REPEATING_TRANSACTIONS);
        getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
    }

    public void onResume() {
        super.onResume();
        reloadData();
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
        getActionBar().setTitle(title);
    }

    private void setupView() {
        this.theList = findViewById(R.id.thelist);
        this.theList.setItemsCanFocus(true);
        ListView listView = this.theList;
        ListAdapter repeatingRowAdapter = new RepeatingRowAdapter(this);
        this.adapter = (RepeatingRowAdapter) repeatingRowAdapter;
        listView.setAdapter(repeatingRowAdapter);
        this.theList.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.theList.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        this.titleTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RepeatingActivity.this.openOptionsMenu();
            }
        });
        FrameLayout theView = findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        theView.setVisibility(View.GONE);
        this.balanceBar = findViewById(R.id.balancebar);
        this.balanceBar.setSecondBalanceEnabled(true);
        this.balanceBar.setBackgroundResource(R.drawable.balancebarforscheduledtransactions);
        this.balanceBar.nextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final EditText textView = new EditText(RepeatingActivity.this);
                Builder b = new Builder(RepeatingActivity.this);
                b.setView(textView);
                b.setTitle(Locales.kLOC_REPEATING_UPCOMING_MESSAGE);
                b.setPositiveButton(Locales.kLOC_GENERAL_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Prefs.setPref(Prefs.PREFS_REPEATING_UPCOMING_PERIOD, Integer.parseInt(textView.getText().toString()));
                            RepeatingActivity.this.reloadData();
                        } catch (NumberFormatException e) {
                        }
                    }
                });
                b.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, null);
                b.create().show();
            }
        });
    }

    private void reloadBalanceBar() {
        ArrayList<RepeatingTransactionClass> repeatingTransactions = TransactionDB.queryAllRepeatingTransactions();
        int days = Prefs.getIntPref(Prefs.PREFS_REPEATING_UPCOMING_PERIOD);
        String text = Locales.kLOC_REPEATING_UPCOMING_LABEL + " " + days + Locales.kLOC_REPEATING_FREQUENCY_DAYS;
        GregorianCalendar upcomingEndDate = CalExt.addDays(new GregorianCalendar(), days);
        double overdueAmount = 0.0d;
        double upcomingAmount = 0.0d;
        Iterator it = repeatingTransactions.iterator();
        while (it.hasNext()) {
            RepeatingTransactionClass repeatingTransaction = (RepeatingTransactionClass) it.next();
            if (repeatingTransaction.isOverdue()) {
                overdueAmount += repeatingTransaction.overdueAmount();
            }
            upcomingAmount += repeatingTransaction.amountBetweenDate(repeatingTransaction.getTransaction().getDate(), upcomingEndDate);
        }
        if (overdueAmount < 0.0d) {
            this.balanceBar.balanceAmountTextView.setTextColor(PocketMoneyThemes.redOnBlackLabelColor());
        } else {
            this.balanceBar.balanceAmountTextView.setTextColor(-1);
        }
        if (upcomingAmount < 0.0d) {
            this.balanceBar.secondBalanceAmountTextView.setTextColor(PocketMoneyThemes.redOnBlackLabelColor());
        } else if (upcomingAmount > 0.0d) {
            this.balanceBar.secondBalanceAmountTextView.setTextColor(PocketMoneyThemes.greenDepositColor());
        } else {
            this.balanceBar.secondBalanceAmountTextView.setTextColor(-1);
        }
        this.balanceBar.secondBalanceAmountTextView.setText(CurrencyExt.amountAsCurrency(upcomingAmount));
        this.balanceBar.balanceAmountTextView.setText(CurrencyExt.amountAsCurrency(overdueAmount));
        this.balanceBar.secondBalanceTypeTextView.setText(text);
        this.balanceBar.secondBalanceTypeTextView.setTextColor(-1);
        this.balanceBar.balanceTypeTextView.setText(Locales.kLOC_REPEATING_OVERDUE_LABEL);
        this.balanceBar.balanceTypeTextView.setTextColor(-1);
    }

    private void reloadData() {
        this.adapter.setElements(TransactionDB.queryWithFilter(this.filter));
        this.adapter.notifyDataSetChanged();
        reloadBalanceBar();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, Locales.kLOC_TRANSACTION_NEW).setIcon(R.drawable.ic_dialog_menu_generic);
        menu.add(0, 2, 0, Locales.kLOC_REPEATING_PROCESSTODATE).setIcon(R.drawable.ic_dialog_menu_generic);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                if (!AccountsActivity.isLite(this) || this.adapter.getElements().size() < 2) {
                    Intent i = new Intent(this, TransactionEditActivity.class);
                    TransactionClass trans = new TransactionClass();
                    trans.isRepeatingTransaction = true;
                    i.putExtra("Transaction", trans);
                    startActivity(i);
                    return true;
                }
                AccountsActivity.displayLiteDialog(this);
                return true;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                showDialog(1);
                return true;
            default:
                return false;
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        RepeatingRowHolder aHolder = (RepeatingRowHolder) v.getTag();
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
                TransactionClass transaction = (TransactionClass) b.get("Transaction");
                new RepeatingTransactionClass(transaction.transactionID, false).deleteFromDatabase();
                transaction.deleteFromDatabase();
                reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                GregorianCalendar theDate = new GregorianCalendar();
                return new DatePickerDialog(this, new OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (!RepeatingActivity.this.isProcessingToDate) {
                            RepeatingActivity.this.isProcessingToDate = true;
                            final GregorianCalendar newCal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                            new Thread() {
                                public void run() {
                                    TransactionDB.addRepeatingEventsThroughDate(newCal, RepeatingActivity.this);
                                    RepeatingActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            RepeatingActivity.this.reloadData();
                                        }
                                    });
                                }
                            }.start();
                        }
                    }
                }, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH));
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                this.progressDialog = new ProgressDialog(this);
                this.progressDialog.setProgressStyle(1);
                this.progressDialog.setMessage("Processing...\n\nWarning: This may take several minutes");
                this.progressDialog.setCancelable(false);
                return this.progressDialog;
            default:
                return null;
        }
    }

    public void updateProgressBar(int progress) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 1, progress, 0));
    }

    public void finishProgressBar() {
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 0, "Process to date Completed"), 500);
        this.isProcessingToDate = false;
    }
}

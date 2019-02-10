package com.example.smmoney.views.splits;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import java.util.Iterator;

public class SplitsActivity extends PocketMoneyActivity {
    public static final int REQUEST_EDIT = 3;
    public static final int RESULT_CHANGED = 1;
    public static final int RESULT_NO_CHANGE = 0;
    private final int CMENU_DELETE = REQUEST_EDIT;
    private final int CMENU_EDIT = RESULT_CHANGED;
    private final int MENU_ADJUST = REQUEST_EDIT;
    private final int MENU_CLEAR = 4;
    private final int MENU_NEW = RESULT_CHANGED;
    private final int MENU_REMAINDER = 2;
    private final int REQUEST_NEW = RESULT_CHANGED;
    private final int REQUEST_REMAINDER = 2;
    private SplitsRowAdapter adapter;
    private Context context;
    private double originalSubtotal = 0.0d;
    private TextView remainderTextView;
    private TextView remainderTitleTextView;
    private TextView splitsTotalTextView;
    private TextView splitsTotalTitleTextView;
    private TextView titleTextView;
    private TextView totalTextView;
    private TextView totalTitleTextView;
    private TransactionClass transaction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        this.transaction = (TransactionClass) getIntent().getExtras().get("Transaction");
        this.originalSubtotal = this.transaction.getSubTotal();
        FrameLayout layout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.splits, null);
        this.splitsTotalTextView = layout.findViewById(R.id.splitssplitstotal);
        this.remainderTextView = layout.findViewById(R.id.splitsremainder);
        this.totalTextView = layout.findViewById(R.id.splitstotal);
        this.splitsTotalTitleTextView = layout.findViewById(R.id.splitssplitstotaltitle);
        this.splitsTotalTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.remainderTitleTextView = layout.findViewById(R.id.splitsremaindertitle);
        this.remainderTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.totalTitleTextView = layout.findViewById(R.id.splitstotaltitle);
        this.totalTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        ListView listView = layout.findViewById(R.id.the_list);
        this.adapter = new SplitsRowAdapter(this, this.transaction);
        listView.setAdapter(this.adapter);
        listView.setFocusable(false);
        listView.setItemsCanFocus(true);
        listView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) listView.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.titleTextView = layout.findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        this.titleTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SplitsActivity.this.openOptionsMenu();
            }
        });
        FrameLayout theView = layout.findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        theView.setVisibility(View.GONE);
        setResult(0);
        setContentView(layout);
        setTitle(Locales.kLOC_EDIT_SPLITS_TITLE);
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
        getActionBar().setTitle(title);
    }

    protected void onResume() {
        super.onResume();
        reloadData();
    }

    public void reloadData() {
        double remainderTotal;
        double totalTotal;
        int i;
        this.adapter.setElements(this.transaction.getSplits());
        this.adapter.notifyDataSetChanged();
        boolean singleXrate = this.transaction.isSingleXrate();
        String currencyCode;
        if (singleXrate) {
            currencyCode = this.transaction.getCurrencyCode();
        } else {
            currencyCode = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        double splitsTotal = splitsSum();
        if (this.originalSubtotal == 0.0d) {
            remainderTotal = 0.0d;
            totalTotal = splitsTotal;
        } else {
            remainderTotal = this.transaction.getSubTotal() - splitsTotal;
            totalTotal = this.transaction.getSubTotal();
        }
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            this.splitsTotalTextView.setText(CurrencyExt.amountAsCurrency(splitsTotal / (singleXrate ? this.transaction.getXrate() : 1.0d), this.transaction.getCurrencyCode()));
            this.remainderTextView.setText(CurrencyExt.amountAsCurrency(remainderTotal / (singleXrate ? this.transaction.getXrate() : 1.0d), this.transaction.getCurrencyCode()));
            this.totalTextView.setText(CurrencyExt.amountAsCurrency(totalTotal / (singleXrate ? this.transaction.getXrate() : 1.0d), this.transaction.getCurrencyCode()));
        } else {
            this.splitsTotalTextView.setText(CurrencyExt.amountAsCurrency(splitsTotal, Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
            this.remainderTextView.setText(CurrencyExt.amountAsCurrency(remainderTotal, Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
            this.totalTextView.setText(CurrencyExt.amountAsCurrency(totalTotal, Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
        }
        int negColor = PocketMoneyThemes.redLabelColor();
        int redColor = PocketMoneyThemes.redLabelColor();
        int greenColor = PocketMoneyThemes.greenDepositColor();
        int altColor = PocketMoneyThemes.alternateCellTextColor();
        TextView textView = this.splitsTotalTextView;
        if (splitsTotal < 0.0d) {
            i = negColor;
        } else {
            i = greenColor;
        }
        textView.setTextColor(i);
        TextView textView2 = this.totalTextView;
        if (totalTotal >= 0.0d) {
            negColor = greenColor;
        }
        textView2.setTextColor(negColor);
        textView2 = this.remainderTextView;
        if (remainderTotal < -0.01d || remainderTotal > 0.009d) {
            altColor = redColor;
        }
        textView2.setTextColor(altColor);
        this.remainderTitleTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.splitsTotalTitleTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.totalTitleTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
    }

    private void setTransactionAsResult() {
        Intent i = new Intent();
        double splitsSum = splitsSum();
        if ((this.originalSubtotal == 0.0d || this.transaction.getNumberOfSplits() == RESULT_CHANGED) && splitsSum != 0.0d) {
            this.transaction.setSubTotal(splitsSum);
        }
        this.transaction.initType();
        i.putExtra("Transaction", this.transaction);
        setResult(RESULT_CHANGED, i);
    }

    private double splitsSum() {
        double splitsTotal = 0.0d;
        Iterator it = this.transaction.getSplits().iterator();
        while (it.hasNext()) {
            splitsTotal += ((SplitsClass) it.next()).getAmount();
        }
        return splitsTotal;
    }

    private void newSplitAction() {
        SplitsClass split = new SplitsClass();
        AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
        split.setCurrencyCode(act == null ? Prefs.getStringPref(Prefs.HOMECURRENCYCODE) : act.getCurrencyCode());
        split.dirty = false;
        Intent i = new Intent(this, SplitsEditActivity.class);
        i.putExtra("Transaction", this.transaction);
        i.putExtra("Split", split);
        startActivityForResult(i, RESULT_CHANGED);
    }

    private void remainderAction() {
        SplitsClass split = new SplitsClass();
        split.setCurrencyCode(AccountDB.recordFor(this.transaction.getAccount()).getCurrencyCode());
        split.setAmount(this.transaction.getSubTotal() - splitsSum());
        split.dirty = false;
        Intent i = new Intent(this, SplitsEditActivity.class);
        i.putExtra("Transaction", this.transaction);
        i.putExtra("Split", split);
        startActivityForResult(i, 2);
    }

    private void adjustSplitsAction() {
        this.transaction.setSubTotal(splitsSum());
        this.transaction.initType();
        reloadData();
    }

    private void clearAction() {
        for (int index = this.transaction.getNumberOfSplits() - 1; index >= 0; index--) {
            this.transaction.deleteSplitAtIndex(index);
        }
        this.transaction.setSubTotal(splitsSum());
        this.transaction.initType();
        reloadData();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, RESULT_CHANGED, 0, Locales.kLOC_SPLITS_NEW).setIcon(R.drawable.ic_arrow_drop_down_circle);
        MenuItem item = menu.add(0, 2, 0, "+" + Locales.kLOC_EDIT_SPLITS_REMAINDER);
        item = menu.add(0, REQUEST_EDIT, 0, Locales.kLOC_EDIT_SPLITS_ADJUST);
        menu.add(0, 4, 0, Locales.kLOC_EDIT_SPLITS_CLEAR).setIcon(R.drawable.ic_arrow_drop_down_circle);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case RESULT_CHANGED /*1*/:
                newSplitAction();
                return true;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                remainderAction();
                return true;
            case REQUEST_EDIT /*3*/:
                adjustSplitsAction();
                return true;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                clearAction();
                return true;
            default:
                return false;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            SplitsClass split = (SplitsClass) data.getExtras().get("Split");
            switch (requestCode) {
                case RESULT_CHANGED /*1*/:
                    if (resultCode == RESULT_CHANGED) {
                        this.transaction.addSplit(split);
                        return;
                    }
                    return;
                case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                    if (resultCode == RESULT_CHANGED) {
                        this.transaction.addSplit(split);
                        return;
                    }
                    return;
                case REQUEST_EDIT /*3*/:
                    if (resultCode == RESULT_CHANGED) {
                        this.transaction.getSplits().remove(data.getExtras().getInt("SplitIndex"));
                        this.transaction.getSplits().add(data.getExtras().getInt("SplitIndex"), split);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        SplitsRowHolder aHolder = (SplitsRowHolder) v.getTag();
        Intent i = new Intent();
        i.putExtra("Split", aHolder.split);
        i.putExtra("Transaction", this.transaction);
        menu.add(0, RESULT_CHANGED, 0, Locales.kLOC_GENERAL_EDIT).setIntent(i);
        menu.add(0, REQUEST_EDIT, 0, Locales.kLOC_GENERAL_DELETE).setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case RESULT_CHANGED /*1*/:
                Intent anIntent = new Intent(this, SplitsEditActivity.class);
                anIntent.putExtra("Transaction", (TransactionClass) b.get("Transaction"));
                anIntent.putExtra("Split", (SplitsClass) b.get("Split"));
                startActivityForResult(anIntent, REQUEST_EDIT);
                return true;
            case REQUEST_EDIT /*3*/:
                this.transaction.deleteSplitAtIndex(this.transaction.getSplits().indexOf(b.get("Split")));
                setTransactionAsResult();
                reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            setTransactionAsResult();
        }
        return super.onKeyDown(keyCode, event);
    }
}

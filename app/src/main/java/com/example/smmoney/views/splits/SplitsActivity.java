package com.example.smmoney.views.splits;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

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

import java.util.Objects;

public class SplitsActivity extends PocketMoneyActivity {
    public static final int REQUEST_EDIT = 3;
    public static final int RESULT_CHANGED = 1;

    public static final int RESULT_NO_CHANGE = 0;
    final ActivityResultLauncher<Intent> editSplitLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_CHANGED && result.getData() != null) {
                    Intent data = result.getData();
                    SplitsClass split = (SplitsClass) data.getExtras().get("Split");
                    int index = data.getIntExtra("SplitIndex", -1);
                    if (index != -1) {
                        this.transaction.getSplits().remove(index);
                        this.transaction.getSplits().add(index, split);
                    } else {
                        this.transaction.addSplit(split);
                    }
                    reloadData();
                }
            }
    );
    private final int CMENU_DELETE = 3;
    private final int CMENU_EDIT = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_ADJUST = 3;
    private final int MENU_CLEAR = 4;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_NEW = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MENU_REMAINDER = 2;
    private final int REQUEST_NEW = 1;
    private final int REQUEST_REMAINDER = 2;
    private SplitsRowAdapter adapter;
    @SuppressWarnings("FieldCanBeLocal")
    private Context context;
    private double originalSubtotal = 0.0d;
    private TextView remainderTextView;
    private TextView remainderTitleTextView;
    private TextView splitsTotalTextView;
    private TextView splitsTotalTitleTextView;
    private TextView totalTextView;
    private TextView totalTitleTextView;
    private TransactionClass transaction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            this.transaction = (TransactionClass) intent.getExtras().get("Transaction");
            if (this.transaction != null) {
                this.transaction.hydrated = true;
                this.transaction.dirty = true;
            }
        }
        if (this.transaction == null) {
            finish();
            return;
        }
        this.originalSubtotal = this.transaction.getSubTotal();
        setContentView(R.layout.splits);
        this.splitsTotalTextView = findViewById(R.id.splitssplitstotal);
        this.remainderTextView = findViewById(R.id.splitsremainder);
        this.totalTextView = findViewById(R.id.splitstotal);
        this.splitsTotalTitleTextView = findViewById(R.id.splitssplitstotaltitle);
        this.splitsTotalTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.remainderTitleTextView = findViewById(R.id.splitsremaindertitle);
        this.remainderTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.totalTitleTextView = findViewById(R.id.splitstotaltitle);
        this.totalTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        ListView listView = findViewById(R.id.the_list);
        this.adapter = new SplitsRowAdapter(this, this.transaction);
        listView.setAdapter(this.adapter);
        listView.setFocusable(false);
        listView.setItemsCanFocus(true);
        listView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) listView.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        setResult(RESULT_NO_CHANGE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_EDIT_SPLITS_TITLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        setTransactionAsResult();
        finish();
        return true;
    }

    protected void onResume() {
        super.onResume();
        reloadData();
    }

    private void reloadData() {
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
        if ((this.originalSubtotal == 0.0d || this.transaction.getNumberOfSplits() == 1) && splitsSum != 0.0d) {
            this.transaction.setSubTotal(splitsSum);
        }
        this.transaction.initType();
        i.putExtra("Transaction", this.transaction);
        setResult(RESULT_CHANGED, i);
    }

    private double splitsSum() {
        double splitsTotal = 0.0d;
        for (SplitsClass splitsClass : this.transaction.getSplits()) {
            splitsTotal += (splitsClass).getAmount();
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
        i.putExtra("SplitIndex", -1);
        editSplitLauncher.launch(i);
    }

    private void remainderAction() {
        SplitsClass split = new SplitsClass();
        split.setCurrencyCode(Objects.requireNonNull(AccountDB.recordFor(this.transaction.getAccount())).getCurrencyCode());
        split.setAmount(this.transaction.getSubTotal() - splitsSum());
        split.dirty = false;
        Intent i = new Intent(this, SplitsEditActivity.class);
        i.putExtra("Transaction", this.transaction);
        i.putExtra("Split", split);
        i.putExtra("SplitIndex", -1);
        editSplitLauncher.launch(i);
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
        menu.add(0, MENU_NEW, 0, Locales.kLOC_SPLITS_NEW).setIcon(R.drawable.ic_arrow_drop_down_circle);
        menu.add(0, MENU_REMAINDER, 0, "+" + Locales.kLOC_EDIT_SPLITS_REMAINDER);
        menu.add(0, MENU_ADJUST, 0, Locales.kLOC_EDIT_SPLITS_ADJUST);
        menu.add(0, MENU_CLEAR, 0, Locales.kLOC_EDIT_SPLITS_CLEAR).setIcon(R.drawable.ic_arrow_drop_down_circle);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return switch (item.getItemId()) {
            case REQUEST_NEW /*1*/ -> {
                newSplitAction();
                yield true;
            }
            case REQUEST_REMAINDER /*2*/ -> {
                remainderAction();
                yield true;
            }
            case REQUEST_EDIT /*3*/ -> {
                adjustSplitsAction();
                yield true;
            }
            case MENU_CLEAR /*4*/ -> {
                clearAction();
                yield true;
            }
            default -> false;
        };
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        SplitsRowHolder aHolder = (SplitsRowHolder) v.getTag();
        Intent i = new Intent();
        i.putExtra("Split", aHolder.split);
        i.putExtra("Transaction", this.transaction);
        menu.add(0, CMENU_EDIT, 0, Locales.kLOC_GENERAL_EDIT).setIntent(i);
        menu.add(0, CMENU_DELETE, 0, Locales.kLOC_GENERAL_DELETE).setIntent(i);
    }

    public boolean onContextItemSelected(MenuItem item) {
        Bundle b = item.getIntent().getExtras();
        switch (item.getItemId()) {
            case CMENU_EDIT /*1*/:
                Intent anIntent = new Intent(this, SplitsEditActivity.class);
                anIntent.putExtra("Transaction", (TransactionClass) Objects.requireNonNull(b).get("Transaction"));
                anIntent.putExtra("Split", (SplitsClass) b.get("Split"));
                // Add the index so we know which one to replace in the launcher callback
                int index = -1;
                SplitsClass target = (SplitsClass) b.get("Split");
                for (int i = 0; i < this.transaction.getSplits().size(); i++) {
                    if (this.transaction.getSplits().get(i).getAmount() == target.getAmount() &&
                            Objects.equals(this.transaction.getSplits().get(i).getCategory(), target.getCategory())) {
                        index = i;
                        break;
                    }
                }
                anIntent.putExtra("SplitIndex", index);
                editSplitLauncher.launch(anIntent);
                return true;
            case CMENU_DELETE /*3*/:
                this.transaction.deleteSplitAtIndex(this.transaction.getSplits().indexOf(Objects.requireNonNull(b).get("Split")));
                setTransactionAsResult();
                reloadData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setTransactionAsResult();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

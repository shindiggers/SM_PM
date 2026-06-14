package com.example.smmoney.views.splits;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.NoteEditor;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.CategoryClass;
import com.example.smmoney.records.ClassNameClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.CurrencyKeyboard;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.exchangerates.ExchangeRateActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;

import java.util.ArrayList;

public class SplitsEditActivity extends PocketMoneyActivity {
    private static final int MENU_SAVE = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITSPLIT_AMOUNT = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITSPLIT_CATEGORY = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EDITSPLIT_CLASS = 3;
    @SuppressWarnings("FieldCanBeLocal")
    private final int NOTE_EDIT_BUTTON = 30;
    @SuppressWarnings("FieldCanBeLocal")
    private final int REQUEST_CURRENCY = 31;

    private final ActivityResultLauncher<Intent> noteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.split.setMemo(selection);
            setNotesText(selection);
            getCells();
        }
    });

    private final ActivityResultLauncher<Intent> currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            Bundle b = result.getData().getExtras();
            try {
                this.split.setCurrencyCode(b.getString("currency"));
                this.split.setXrate(b.getDouble("xrate"));
                this.split.setAmount(b.getDouble("amount"));
                loadAmountXrateValues();
                getCells();
            } catch (NullPointerException e) {
                Log.e(com.example.smmoney.SMMoney.TAG, "NullPointerException in currencyLauncher", e);
            }
        }
    });

    private final ActivityResultLauncher<Intent> lookupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            int type = result.getResultCode();
            if (type == 5) {
                this.split.setCategory(selection);
                this.categoryEditText.setText(selection);
            } else if (type == 6) {
                this.split.setClassName(selection);
                this.classEditText.setText(selection);
            } else if (type == 3) {
                this.split.setTransferToAccount(selection);
                this.transToTextView.setText(selection);
                updateXrates();
            }
            loadCells();
        } else if (result.getResultCode() == 0 && result.getData() != null) {
            // LookupsListActivity might not return an intent on cancel, but we handle it just in case
            int type = result.getData().getIntExtra("type", -1);
            if (type == 3) {
                this.withdrawalButton.setChecked(true);
                this.split.setTransferToAccount("");
                loadCells();
            }
        }
    });

    private EditText amountEditText;
    private TextView amountXrateTextView;
    private AutoCompleteTextView categoryEditText;
    private AutoCompleteTextView classEditText;
    private CurrencyKeyboard currencyKeyboard;
    private Activity currentActivity;
    private RadioButton depositButton;
    @SuppressWarnings("FieldCanBeLocal")
    private FrameLayout keyboardToolbar;
    private EditText memoEditText;
    private boolean programaticUpdate;
    private SplitsClass split;
    private int splitIndex = -1;
    private int splitTransactionType;
    private LinearLayout transToLayout;
    private TextView transToTextView;
    private TextView transToTitleTextView;
    private TransactionClass transaction;
    private RadioButton transferButton;
    private RadioButton withdrawalButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.transaction = (TransactionClass) getIntent().getExtras().get("Transaction");
        this.split = (SplitsClass) getIntent().getExtras().get("Split");
        this.split.hydrated = true;
        this.splitTransactionType = this.split.getTransactionType();
        this.splitIndex = getIntent().getIntExtra("SplitIndex", -1);
        setResult(0);
        this.currentActivity = this;
        setContentView(R.layout.split_edit);
        setupButtons();
        loadCells();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_EDIT_SPLIT_TITLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, MENU_SAVE, 0, Locales.kLOC_GENERAL_SAVE);
        item.setIcon(R.drawable.ic_save_white_24dp);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_SAVE) {
            handleSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSave() {
        save();
        editTextDidFinishChanging(2);
        Intent i = new Intent();
        if (this.splitIndex != -1) {
            i.putExtra("SplitIndex", this.splitIndex);
        }
        i.putExtra("Split", this.split);
        i.putExtra("transaction", this.transaction);
        setResult(1, i);
        finish();
    }

    public void onResume() {
        super.onResume();
        loadCells();
    }

    private void setTitle(String title) {
    }

    private void setupButtons() {
        this.withdrawalButton = findViewById(R.id.withdrawalbutton);
        this.depositButton = findViewById(R.id.depositbutton);
        this.transferButton = findViewById(R.id.transferbutton);
        this.transToTextView = findViewById(R.id.transtotextview);
        this.categoryEditText = findViewById(R.id.categoryedittext);
        this.amountEditText = findViewById(R.id.amountedittext);
        this.amountXrateTextView = findViewById(R.id.amount_xrate_text_view);
        this.memoEditText = findViewById(R.id.memoedittext);
        this.classEditText = findViewById(R.id.classedittext);
        this.transToLayout = findViewById(R.id.transtobutton);
        this.transToTitleTextView = findViewById(R.id.transtolabel);
        this.keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        this.amountEditText.setShowSoftInputOnFocus(false);
        //this.categoryEditText.setAdapter(new ArrayAdapter<>(this, R.layout.lookups_category, CategoryClass.allCategoryNamesInDatabase()));
        // TODO Customise the simple_list_item_1 so that it looks how it should. Just used here to make code work as original code above does not point to a TextView and therefore crashes!!
        this.categoryEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CategoryClass.allCategoryNamesInDatabase()));
        //this.classEditText.setAdapter(new ArrayAdapter<>(this, R.layout.lookups_category, ClassNameClass.allClassNamesInDatabase()));
        // TODO Customise as for above category class adapter
        this.classEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ClassNameClass.allClassNamesInDatabase()));
        ((RadioGroup) this.withdrawalButton.getParent()).setOnCheckedChangeListener(getRadioChangedListener());
        ((LinearLayout) this.memoEditText.getParent()).setOnClickListener(view -> {
            Intent i = new Intent(SplitsEditActivity.this.currentActivity, NoteEditor.class);
            i.putExtra("note", SplitsEditActivity.this.split.getMemo());
            noteLauncher.launch(i);
        });
        LinearLayout v = (LinearLayout) this.categoryEditText.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(5);
        v = (LinearLayout) this.classEditText.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(6);
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            ((LinearLayout) this.amountEditText.getParent()).setOnClickListener(v3 -> {
                Intent i = new Intent(SplitsEditActivity.this.currentActivity, ExchangeRateActivity.class);
                i.putExtra("transaction", SplitsEditActivity.this.transaction);
                i.putExtra("split", SplitsEditActivity.this.split);
                currencyLauncher.launch(i);
            });
        } else {
            findViewById(R.id.amount_currency_button).setVisibility(View.GONE);
            this.amountXrateTextView.setVisibility(View.GONE);
        }
        this.keyboardToolbar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.categoryEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(EDITSPLIT_CATEGORY/*1*/));
        this.classEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(EDITSPLIT_CLASS/*3*/));
        this.currencyKeyboard = findViewById(R.id.keyboardView);
        this.currencyKeyboard.setEditText(this.amountEditText, null);
        this.categoryEditText.setKeyListener(new MyKeyListener(this.categoryEditText.getKeyListener(), EDITSPLIT_CATEGORY/*1*/));
        this.classEditText.setKeyListener(new MyKeyListener(this.classEditText.getKeyListener(), EDITSPLIT_CLASS/*3*/));
        findViewById(R.id.scroll_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ArrayList<View> theViews = new ArrayList<>();
        TextView tView = findViewById(R.id.transtolabel);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.transToTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.category_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.categoryEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.amount_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.amountEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.amountXrateTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.class_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.classEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.memo_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.memoEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        int i = 0;
        for (View theView : theViews) {
            (theView).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i++;
        }
    }

    private void loadCells() {
        this.programaticUpdate = true;
        setType();
        this.programaticUpdate = false;
        configureTransferControl();
        this.transToTextView.setText(this.split.getTransferToAccount());
        this.categoryEditText.setText(this.split.getCategory());
        loadAmountXrateValues();
        updateAmountFieldTextColor();
        this.memoEditText.setText(this.split.getMemo());
        this.classEditText.setText(this.split.getClassName());
    }

    private void save() {
        getCells();
    }

    private void getCells() {
        this.split.setTransferToAccount(this.transToTextView.getText().toString());
        this.split.setCategory(this.categoryEditText.getText().toString());
        this.split.setMemo(this.memoEditText.getText().toString());
        this.split.setClassName(this.classEditText.getText().toString());
        saveAmountXrateValues();
    }

    private void updateXrates() {
        double x2 = 1.0d;
        getCells();
        if (this.split.isTransfer() && Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            AccountClass a1 = AccountDB.recordFor(this.transaction.getAccount());
            AccountClass a2 = AccountDB.recordFor(this.split.getTransferToAccount());
            double x1 = a1 == null ? 1.0d : a1.getExchangeRate();
            if (a2 != null) {
                x2 = a2.getExchangeRate();
            }
            this.split.setXrate(x1 / x2);
            this.split.setCurrencyCode(a2.getCurrencyCode());
            this.amountXrateTextView.setText("x" + this.split.getXrate());
            if (this.transaction.getSubTotal() == 0.0d) {
                this.amountEditText.setText("");
            } else {
                this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.split.getAmount() / this.split.getXrate()), this.split.getCurrencyCode()));
            }
            this.amountEditText.invalidate();
            this.amountXrateTextView.setVisibility(View.VISIBLE);
            this.amountXrateTextView.invalidate();
        }
    }

    private void updateAmountFieldTextColor() {
        if (this.splitTransactionType == Enums.kTransactionTypeWithdrawal /*0*/ || this.splitTransactionType == Enums.kTransactionTypeTransferTo /*2*/) {
            this.amountEditText.setTextColor(-65536);
        } else {
            this.amountEditText.setTextColor(-16711936);
        }
    }

    private void loadAmountXrateValues() {
        AccountClass act = AccountDB.recordFor(this.transaction.getAccount());
        String currencyCode;
        if (act != null) {
            currencyCode = act.getCurrencyCode();
        } else {
            currencyCode = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        if (currencyCode.equals(this.split.getCurrencyCode())) {
            this.amountXrateTextView.setVisibility(View.GONE);
        }
        if (this.split.getAmount() == 0.0d) {
            this.amountEditText.setText("");
        } else if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.split.getAmount() / this.split.getXrate()), this.split.getCurrencyCode()));
            this.amountXrateTextView.setVisibility(View.VISIBLE);
            this.amountXrateTextView.setText("x" + CurrencyExt.exchangeRateAsString(this.split.getXrate()));
        } else {
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.split.getAmount())));
        }
    }

    private void saveAmountXrateValues() {
        double amount = CurrencyExt.amountFromStringWithCurrency(this.amountEditText.getText().toString(), this.split.getCurrencyCode());
        double multiplier = 1.0d;
        if (this.splitTransactionType == Enums.kTransactionTypeTransferTo /*2*/ || this.splitTransactionType == Enums.kTransactionTypeWithdrawal /*0*/) {
            multiplier = -1.0d;
        }
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            multiplier *= this.split.getXrate();
        }
        this.split.setAmount(Math.abs(amount) * multiplier);
    }

    private void configureTransferControl() {
        switch (this.splitTransactionType) {
            case Enums.kTransactionTypeTransferTo /*2*/ -> this.transToTitleTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_FROM);
            case Enums.kTransactionTypeTransferFrom /*3*/ -> this.transToTitleTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_TO);
            default -> {
            }
        }
    }

    private void setNotesText(String note) {
        int i = 25;
        if (note == null || note.length() <= 0) {
            this.memoEditText.setText("");
            return;
        }
        EditText editText = this.memoEditText;
        if (25 > note.length()) {
            i = note.length();
        }
        editText.setText(note.substring(0, i));
    }

    private void setType() {
        if (this.splitTransactionType == Enums.kTransactionTypeWithdrawal /*0*/) {
            this.withdrawalButton.setChecked(true);
            this.transToLayout.setVisibility(View.GONE);
        } else if (this.splitTransactionType == Enums.kTransactionTypeDeposit /*1*/) {
            this.depositButton.setChecked(true);
            this.transToLayout.setVisibility(View.GONE);
        } else if (this.splitTransactionType == Enums.kTransactionTypeTransferTo /*2*/ || this.splitTransactionType == Enums.kTransactionTypeTransferFrom /*3*/) {
            this.transferButton.setChecked(true);
            this.transToLayout.setVisibility(View.VISIBLE);
        }
    }

    private void editTextDidFinishChanging(int editTextCode) {
        if (editTextCode == EDITSPLIT_AMOUNT /*2*/) {
            saveAmountXrateValues();
            loadAmountXrateValues();
        }
    }

    @SuppressWarnings("EmptyMethod")
    private void editTextDidChange(int editTextCode) {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.currencyKeyboard.hide()) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private OnFocusChangeListener getFocusChangedListenerWithID(int id) {
        final int theID = id;
        return (v, hasFocus) -> {
            if (!hasFocus) {
                SplitsEditActivity.this.editTextDidFinishChanging(theID);
            }
        };
    }

    private OnClickListener getLookupListClickListener() {
        return view -> {
            // Save current UI state into the 'split' object before leaving
            SplitsEditActivity.this.getCells();

            Intent i = new Intent(SplitsEditActivity.this.currentActivity, LookupsListActivity.class);
            i.putExtra("type", ((Integer) view.getTag()).intValue());
            lookupLauncher.launch(i);
        };
    }

    private OnCheckedChangeListener getRadioChangedListener() {
        return (group, checkedId) -> {
            if (!SplitsEditActivity.this.programaticUpdate) {
                if (checkedId == R.id.withdrawalbutton) {
                    SplitsEditActivity.this.getCells();
                    SplitsEditActivity.this.splitTransactionType = 0;
                    SplitsEditActivity.this.split.setAmount(Math.abs(SplitsEditActivity.this.split.getAmount()) * -1.0d);
                    SplitsEditActivity.this.split.setTransferToAccount("");
                } else if (checkedId == R.id.depositbutton) {
                    SplitsEditActivity.this.getCells();
                    SplitsEditActivity.this.splitTransactionType = 1;
                    SplitsEditActivity.this.split.setAmount(Math.abs(SplitsEditActivity.this.split.getAmount()));
                    SplitsEditActivity.this.split.setTransferToAccount("");
                } else if (checkedId == R.id.transferbutton) {
                    if (SplitsEditActivity.this.split.getAmount() <= 0.0d) {
                        SplitsEditActivity.this.splitTransactionType = 2;
                    } else {
                        SplitsEditActivity.this.splitTransactionType = 3;
                    }
                    if (SplitsEditActivity.this.transToTextView.getText().toString().isEmpty()) {
                        SplitsEditActivity.this.getCells();
                        Intent i = new Intent(SplitsEditActivity.this.currentActivity, LookupsListActivity.class);
                        i.putExtra("type", 3);
                        lookupLauncher.launch(i);
                    }
                }
                SplitsEditActivity.this.setType();
                SplitsEditActivity.this.loadCells();
            }
        };
    }

    private class MyKeyListener implements KeyListener {
        final KeyListener original;
        private final int editTextCode;

        private MyKeyListener(KeyListener orig, int code) {
            this.original = orig;
            this.editTextCode = code;
        }

        public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
            SplitsEditActivity.this.editTextDidChange(this.editTextCode);
            return this.original.onKeyDown(view, text, keyCode, event);
        }

        public void clearMetaKeyState(View arg0, Editable arg1, int arg2) {
            this.original.clearMetaKeyState(arg0, arg1, arg2);
        }

        public int getInputType() {
            return this.original.getInputType();
        }

        public boolean onKeyOther(View arg0, Editable arg1, KeyEvent arg2) {
            return this.original.onKeyOther(arg0, arg1, arg2);
        }

        public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
            return this.original.onKeyUp(view, text, keyCode, event);
        }
    }
}

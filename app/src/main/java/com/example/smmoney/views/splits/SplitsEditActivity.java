package com.example.smmoney.views.splits;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
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
    private TextView titleTextView;
    private LinearLayout transToLayout;
    private TextView transToTextView;
    private TextView transToTitleTextView;
    private TransactionClass transaction;
    private RadioButton transferButton;
    private RadioButton withdrawalButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.transaction = (TransactionClass) getIntent().getExtras().get("Transaction");
        this.split = (SplitsClass) getIntent().getExtras().get("Split");
        this.split.hydrated = true;
        this.splitTransactionType = this.split.getTransactionType();
        this.splitIndex = getIntent().getExtras().getInt("SplitIndex");
        setResult(0);
        this.currentActivity = this;
        setContentView(R.layout.split_edit);
        setupButtons();
        loadCells();
        setTitle(Locales.kLOC_EDIT_SPLIT_TITLE);
        getSupportActionBar().hide();
    }

    public void onResume() {
        super.onResume();
        loadCells();
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
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
        //this.categoryEditText.setAdapter(new ArrayAdapter<>(this, R.layout.lookups_category, CategoryClass.allCategoryNamesInDatabase()));
        // TODO Customise the simple_list_item_1 so that it looks how it should. Just used here to make code work as original code above does not point to a TextView and therefore crashes!!
        this.categoryEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CategoryClass.allCategoryNamesInDatabase()));
        //this.classEditText.setAdapter(new ArrayAdapter<>(this, R.layout.lookups_category, ClassNameClass.allClassNamesInDatabase()));
        // TODO Customise as for above category class adapter
        this.classEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ClassNameClass.allClassNamesInDatabase()));
        ((RadioGroup) this.withdrawalButton.getParent()).setOnCheckedChangeListener(getRadioChangedListener());
        ((LinearLayout) this.memoEditText.getParent()).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(SplitsEditActivity.this.currentActivity, NoteEditor.class);
                i.putExtra("note", SplitsEditActivity.this.split.getMemo());
                SplitsEditActivity.this.currentActivity.startActivityForResult(i, 30);
            }
        });
        LinearLayout v = (LinearLayout) this.categoryEditText.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(5);
        v = (LinearLayout) this.classEditText.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(6);
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            ((LinearLayout) this.amountEditText.getParent()).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(SplitsEditActivity.this.currentActivity, ExchangeRateActivity.class);
                    i.putExtra("transaction", SplitsEditActivity.this.transaction);
                    i.putExtra("split", SplitsEditActivity.this.split);
                    SplitsEditActivity.this.currentActivity.startActivityForResult(i, 31);
                }
            });
        } else {
            findViewById(R.id.amount_currency_button).setVisibility(View.GONE);
            this.amountXrateTextView.setVisibility(View.GONE);
        }
        TextView button = findViewById(R.id.save_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SplitsEditActivity.this.save();
                SplitsEditActivity.this.editTextDidFinishChanging(2);
                Intent i = new Intent();
                i.putExtra("SplitIndex", SplitsEditActivity.this.splitIndex);
                i.putExtra("Split", SplitsEditActivity.this.split);
                i.putExtra("transaction", SplitsEditActivity.this.transaction);
                SplitsEditActivity.this.setResult(1, i);
                SplitsEditActivity.this.finish();
            }
        });
        button = findViewById(R.id.cancel_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SplitsEditActivity.this.finish();
            }
        });
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
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
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
        this.transaction.hydrate();
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
            case Enums.kTransactionTypeTransferTo /*2*/:
                this.transToTitleTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_FROM);
                return;
            case Enums.kTransactionTypeTransferFrom /*3*/:
                this.transToTitleTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_TO);
                return;
            default:
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            String selection = null;
            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    selection = extras.getString("selection");
                }
            }
            switch (requestCode) {
                case SplitsActivity.REQUEST_EDIT /*3*/:
                    this.transToTextView.setText(selection);
                    updateXrates();
                    break;
                case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                    this.categoryEditText.setText(selection);
                    break;
                case LookupsListActivity.CLASS_LOOKUP /*6*/:
                    this.classEditText.setText(selection);
                    break;
                case NOTE_EDIT_BUTTON /*30*/:
                    break;
                case REQUEST_CURRENCY /*31*/:
                    Bundle b = data.getExtras();
                    try {
                        this.split.setCurrencyCode(b.getString("currency"));
                        this.split.setXrate(b.getDouble("xrate"));
                        this.split.setAmount(b.getDouble("amount"));
                        loadAmountXrateValues();
                        break;
                    } catch (NullPointerException e) {
                        break;
                    }
            }
            if (resultCode == -1) {
                this.split.setMemo(selection);
                setNotesText(selection);
            }
            getCells();
        } else if (requestCode == 3) {
            this.withdrawalButton.setChecked(true);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.currencyKeyboard.hide()) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private OnFocusChangeListener getFocusChangedListenerWithID(int id) {
        final int theID = id;
        return new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    SplitsEditActivity.this.editTextDidFinishChanging(theID);
                }
            }
        };
    }

    private OnClickListener getLookupListClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(SplitsEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", ((Integer) view.getTag()).intValue());
                SplitsEditActivity.this.currentActivity.startActivityForResult(i, (Integer) view.getTag());
            }
        };
    }

    private OnCheckedChangeListener getRadioChangedListener() {
        return new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
                        if (SplitsEditActivity.this.transToTextView.getText().toString().length() == 0) {
                            SplitsEditActivity.this.getCells();
                            Intent i = new Intent(SplitsEditActivity.this.currentActivity, LookupsListActivity.class);
                            i.putExtra("type", 3);
                            SplitsEditActivity.this.currentActivity.startActivityForResult(i, 3);
                        }
                    }
                    SplitsEditActivity.this.setType();
                    SplitsEditActivity.this.loadCells();
                }
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

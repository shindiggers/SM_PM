package com.example.smmoney.views.splits;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

    private final ActivityResultLauncher<Intent> noteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            if (this.split != null) {
                this.split.setMemo(selection);
                setNotesText(selection);
                getCells();
            }
        }
    });

    private final ActivityResultLauncher<Intent> currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            Bundle b = result.getData().getExtras();
            try {
                if (this.split != null && b != null) {
                    this.split.setCurrencyCode(b.getString("currency"));
                    this.split.setXrate(b.getDouble("xrate"));
                    this.split.setAmount(b.getDouble("amount"));
                    loadAmountXrateValues();
                    getCells();
                }
            } catch (NullPointerException e) {
                Log.e(com.example.smmoney.SMMoney.TAG, "NullPointerException in currencyLauncher", e);
            }
        }
    });

    private final ActivityResultLauncher<Intent> categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            if (this.split != null) {
                this.split.setCategory(selection);
                this.categoryEditText.setText(selection);
                loadCells();
            }
        }
    });

    private final ActivityResultLauncher<Intent> classLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            if (this.split != null) {
                this.split.setClassName(selection);
                this.classEditText.setText(selection);
                loadCells();
            }
        }
    });

    private final ActivityResultLauncher<Intent> transToLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            if (this.split != null) {
                this.split.setTransferToAccount(selection);
                this.transToTextView.setText(selection);
                updateXrates();
                loadCells();
            }
        }
    });

    private EditText amountEditText;
    private TextView amountXrateTextView;
    private AutoCompleteTextView categoryEditText;
    private AutoCompleteTextView classEditText;
    private CurrencyKeyboard currencyKeyboard;
    private MaterialButton depositButton;
    private EditText memoEditText;
    private boolean programaticUpdate;
    private SplitsClass split;
    private int splitIndex = -1;
    private int splitTransactionType;
    private LinearLayout transToLayout;
    private TextView transToTextView;
    private TextView transToTitleTextView;
    private TransactionClass transaction;
    private MaterialButton transferButton;
    private MaterialButton withdrawalButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.transaction = androidx.core.os.BundleCompat.getSerializable(extras, "Transaction", TransactionClass.class);
            this.split = androidx.core.os.BundleCompat.getSerializable(extras, "Split", SplitsClass.class);
        }
        if (this.split == null) {
            finish();
            return;
        }
        this.split.hydrated = true;
        this.splitTransactionType = this.split.getTransactionType();
        this.splitIndex = getIntent().getIntExtra("SplitIndex", -1);
        setResult(0);
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
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getCells();
    }

    public void onResume() {
        super.onResume();
        loadCells();
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
        FrameLayout keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        this.amountEditText.setShowSoftInputOnFocus(false);
        this.categoryEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CategoryClass.allCategoryNamesInDatabase()));
        this.classEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ClassNameClass.allClassNamesInDatabase()));
        
        MaterialButtonToggleGroup group = (MaterialButtonToggleGroup) this.withdrawalButton.getParent();
        group.addOnButtonCheckedListener(getRadioChangedListener());
        
        android.content.res.ColorStateList bgTint = PocketMoneyThemes.segmentedButtonBackgroundTint();
        android.content.res.ColorStateList textTint = PocketMoneyThemes.segmentedButtonTextTint();
        android.content.res.ColorStateList strokeTint = android.content.res.ColorStateList.valueOf(PocketMoneyThemes.currentTintColor());
        
        this.withdrawalButton.setBackgroundTintList(bgTint);
        this.withdrawalButton.setTextColor(textTint);
        this.withdrawalButton.setStrokeColor(strokeTint);

        this.depositButton.setBackgroundTintList(bgTint);
        this.depositButton.setTextColor(textTint);
        this.depositButton.setStrokeColor(strokeTint);

        this.transferButton.setBackgroundTintList(bgTint);
        this.transferButton.setTextColor(textTint);
        this.transferButton.setStrokeColor(strokeTint);

        ((LinearLayout) this.memoEditText.getParent()).setOnClickListener(view -> {
            Intent i = new Intent(this, NoteEditor.class);
            i.putExtra("note", this.split.getMemo());
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
                Intent i = new Intent(this, ExchangeRateActivity.class);
                i.putExtra("transaction", this.transaction);
                i.putExtra("split", this.split);
                currencyLauncher.launch(i);
            });
        } else {
            findViewById(R.id.amount_currency_button).setVisibility(View.GONE);
            this.amountXrateTextView.setVisibility(View.GONE);
        }
        keyboardToolbar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        this.categoryEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(1));
        this.classEditText.setOnFocusChangeListener(getFocusChangedListenerWithID(3));
        this.currencyKeyboard = findViewById(R.id.keyboardView);
        this.currencyKeyboard.setEditText(this.amountEditText, null);
        this.categoryEditText.setKeyListener(new MyKeyListener(this.categoryEditText.getKeyListener()));
        this.classEditText.setKeyListener(new MyKeyListener(this.classEditText.getKeyListener()));
        findViewById(R.id.scroll_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ArrayList<View> theViews = new ArrayList<>();
        this.transToTitleTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.transToTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) this.transToTitleTextView.getParent());
        TextView tView = findViewById(R.id.category_label);
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

        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        android.widget.ImageView iconView;
        if ((iconView = findViewById(R.id.transtoto_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        if ((iconView = findViewById(R.id.category_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        if ((iconView = findViewById(R.id.class_drop_down)) != null) iconView.setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);
        if ((iconView = findViewById(R.id.amount_currency_button)) != null) iconView.setColorFilter(fieldLabelColor, android.graphics.PorterDuff.Mode.SRC_IN);

        theViews.add((View) tView.getParent());
        int i = 0;
        for (View theView : theViews) {
            (theView).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i++;
        }
    }

    private void loadCells() {
        if (this.split == null) return;
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
        if (this.split == null) return;
        this.split.setTransferToAccount(this.transToTextView.getText().toString());
        this.split.setCategory(this.categoryEditText.getText().toString());
        this.split.setMemo(this.memoEditText.getText().toString());
        this.split.setClassName(this.classEditText.getText().toString());
        saveAmountXrateValues();
    }

    private void updateXrates() {
        if (this.split == null || this.transaction == null) return;
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
            if (a2 != null) {
                this.split.setCurrencyCode(a2.getCurrencyCode());
            }
            this.amountXrateTextView.setText(String.format("x%s", this.split.getXrate()));
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
        if (this.splitTransactionType == Enums.kTransactionTypeWithdrawal || this.splitTransactionType == Enums.kTransactionTypeTransferTo) {
            this.amountEditText.setTextColor(-65536);
        } else {
            this.amountEditText.setTextColor(-16711936);
        }
    }

    private void loadAmountXrateValues() {
        if (this.split == null || this.transaction == null) return;
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
            this.amountXrateTextView.setText(String.format("x%s", CurrencyExt.exchangeRateAsString(this.split.getXrate())));
        } else {
            this.amountEditText.setText(CurrencyExt.amountAsCurrency(Math.abs(this.split.getAmount())));
        }
    }

    private void saveAmountXrateValues() {
        if (this.split == null) return;
        double amount = CurrencyExt.amountFromStringWithCurrency(this.amountEditText.getText().toString(), this.split.getCurrencyCode());
        double multiplier = 1.0d;
        if (this.splitTransactionType == Enums.kTransactionTypeTransferTo || this.splitTransactionType == Enums.kTransactionTypeWithdrawal) {
            multiplier = -1.0d;
        }
        if (Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            multiplier *= this.split.getXrate();
        }
        this.split.setAmount(Math.abs(amount) * multiplier);
    }

    private void configureTransferControl() {
        switch (this.splitTransactionType) {
            case Enums.kTransactionTypeTransferTo -> this.transToTitleTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_FROM);
            case Enums.kTransactionTypeTransferFrom -> this.transToTitleTextView.setText(Locales.kLOC_EDIT_TRANSACTION_TRANS_TO);
            default -> {
            }
        }
    }

    private void setNotesText(String note) {
        int i = 25;
        if (note == null || note.isEmpty()) {
            this.memoEditText.setText("");
            return;
        }
        if (25 > note.length()) {
            i = note.length();
        }
        this.memoEditText.setText(note.substring(0, i));
    }

    private void setType() {
        if (this.splitTransactionType == Enums.kTransactionTypeWithdrawal) {
            this.withdrawalButton.setChecked(true);
            this.transToLayout.setVisibility(View.GONE);
        } else if (this.splitTransactionType == Enums.kTransactionTypeDeposit) {
            this.depositButton.setChecked(true);
            this.transToLayout.setVisibility(View.GONE);
        } else if (this.splitTransactionType == Enums.kTransactionTypeTransferTo || this.splitTransactionType == Enums.kTransactionTypeTransferFrom) {
            this.transferButton.setChecked(true);
            this.transToLayout.setVisibility(View.VISIBLE);
        }
    }

    private void editTextDidFinishChanging(int editTextCode) {
        if (editTextCode == 2 /* EDITSPLIT_AMOUNT */) {
            saveAmountXrateValues();
            loadAmountXrateValues();
        }
    }

    @SuppressWarnings("EmptyMethod")
    private void editTextDidChange() {
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == 4 && this.currencyKeyboard.hide()) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private OnFocusChangeListener getFocusChangedListenerWithID(int id) {
        return (v, hasFocus) -> {
            if (!hasFocus) {
                SplitsEditActivity.this.editTextDidFinishChanging(id);
            }
        };
    }

    private OnClickListener getLookupListClickListener() {
        return view -> {
            SplitsEditActivity.this.getCells();
            int type = (Integer) view.getTag();
            Intent i = new Intent(this, LookupsListActivity.class);
            i.putExtra("type", type);
            if (type == 5) {
                categoryLauncher.launch(i);
            } else if (type == 6) {
                classLauncher.launch(i);
            } else if (type == 3) {
                transToLauncher.launch(i);
            }
        };
    }

    private MaterialButtonToggleGroup.OnButtonCheckedListener getRadioChangedListener() {
        return (group, checkedId, isChecked) -> {
            if (isChecked && !SplitsEditActivity.this.programaticUpdate) {
                if (checkedId == R.id.withdrawalbutton) {
                    SplitsEditActivity.this.getCells();
                    SplitsEditActivity.this.splitTransactionType = 0;
                    if (SplitsEditActivity.this.split != null) {
                        SplitsEditActivity.this.split.setAmount(Math.abs(SplitsEditActivity.this.split.getAmount()) * -1.0d);
                        SplitsEditActivity.this.split.setTransferToAccount("");
                    }
                } else if (checkedId == R.id.depositbutton) {
                    SplitsEditActivity.this.getCells();
                    SplitsEditActivity.this.splitTransactionType = 1;
                    if (SplitsEditActivity.this.split != null) {
                        SplitsEditActivity.this.split.setAmount(Math.abs(SplitsEditActivity.this.split.getAmount()));
                        SplitsEditActivity.this.split.setTransferToAccount("");
                    }
                } else if (checkedId == R.id.transferbutton) {
                    if (SplitsEditActivity.this.split != null) {
                        if (SplitsEditActivity.this.split.getAmount() <= 0.0d) {
                            SplitsEditActivity.this.splitTransactionType = 2;
                        } else {
                            SplitsEditActivity.this.splitTransactionType = 3;
                        }
                        if (SplitsEditActivity.this.transToTextView.getText().toString().isEmpty()) {
                            SplitsEditActivity.this.getCells();
                            Intent i = new Intent(this, LookupsListActivity.class);
                            i.putExtra("type", 3);
                            transToLauncher.launch(i);
                        }
                    }
                }
                SplitsEditActivity.this.setType();
                SplitsEditActivity.this.loadCells();
            }
        };
    }

    private class MyKeyListener implements KeyListener {
        final KeyListener original;

        private MyKeyListener(KeyListener orig) {
            this.original = orig;
        }

        public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
            SplitsEditActivity.this.editTextDidChange();
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

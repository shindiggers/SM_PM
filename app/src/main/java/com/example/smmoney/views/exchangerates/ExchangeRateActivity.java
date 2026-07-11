package com.example.smmoney.views.exchangerates;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.transition.TransitionManager;

import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.ExchangeRateCallbackInterface;
import com.example.smmoney.misc.ExchangeRateClass;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.SplitsClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.CurrencyKeyboard;
import com.example.smmoney.views.PocketMoneyActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExchangeRateActivity extends PocketMoneyActivity implements ExchangeRateCallbackInterface {
    private static final String TAG = "ExchangeRateActivity";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private enum CalculatedField { FOREIGN, RATE, ACCOUNT }
    private CalculatedField currentCalculatedField = CalculatedField.ACCOUNT;

    // Formula: foreignAmount * exchangeRate = accountAmount
    private double accountAmount;
    private double exchangeRate = 1.0;
    private double foreignAmount;
    
    private String accountCurrency;
    private String foreignCurrency;
    private TransactionClass transaction;

    private EditText row1EditText;
    private EditText row2EditText;
    private EditText row3EditText;
    
    private ImageView lockRow1Icon;
    private ImageView lockRow2Icon;
    private ImageView lockRow3Icon;
    private ImageView fetchRateButton;
    
    private TextView row1CurrencyTextView;
    private TextView row3CurrencyTextView;
    private TextView statusTextView;
    
    private CurrencyKeyboard currencyKeyboard;
    private boolean programaticUpdate = false;
    private boolean isSwapped = false;

    private final TextWatcher row1Watcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (programaticUpdate) return;
            double val = CurrencyExt.amountFromString(s.toString());
            if (isSwapped) accountAmount = val;
            else foreignAmount = val;
            performCalculation();
        }
    };

    private final TextWatcher row2Watcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (programaticUpdate) return;
            double val = CurrencyExt.amountFromString(s.toString());
            if (isSwapped) exchangeRate = val != 0 ? 1.0 / val : 1.0;
            else exchangeRate = val != 0 ? val : 1.0;
            performCalculation();
        }
    };

    private final TextWatcher row3Watcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (programaticUpdate) return;
            double val = CurrencyExt.amountFromString(s.toString());
            if (isSwapped) foreignAmount = val;
            else accountAmount = val;
            performCalculation();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED);
        extractData();
        setContentView(R.layout.exchangerate);
        setupView();
        updateLockStates();
        loadCells();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_GENERAL_EXCHANGERATE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void extractData() {
        Bundle b = getIntent().getExtras();
        if (b == null) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            this.transaction = b.getSerializable("transaction", TransactionClass.class);
        } else {
            //noinspection deprecation
            this.transaction = (TransactionClass) b.get("transaction");
        }
        if (this.transaction != null) {
            AccountClass acct = AccountDB.recordFor(this.transaction.getAccount());
            this.accountCurrency = acct != null ? acct.getCurrencyCode() : Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        } else {
            this.accountCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        SplitsClass s;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            s = b.getSerializable("split", SplitsClass.class);
        } else {
            //noinspection deprecation
            s = (SplitsClass) b.get("split");
        }
        if (s != null) {
            this.exchangeRate = s.getXrate() != 0 ? s.getXrate() : 1.0;
            this.accountAmount = Math.abs(s.getAmount());
            this.foreignAmount = round(this.accountAmount / this.exchangeRate);
            this.foreignCurrency = s.getCurrencyCode();
        } else {
            this.exchangeRate = 1.0;
            this.accountAmount = 0.0;
            this.foreignAmount = 0.0;
            this.foreignCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
    }

    private void setupView() {
        this.row1EditText = findViewById(R.id.foreign_amount_edit_text);
        this.row2EditText = findViewById(R.id.exchange_rate_edit_text);
        this.row3EditText = findViewById(R.id.account_amount_edit_text);
        this.lockRow1Icon = findViewById(R.id.lock_foreign);
        this.lockRow2Icon = findViewById(R.id.lock_rate);
        this.lockRow3Icon = findViewById(R.id.lock_account);
        this.row1CurrencyTextView = findViewById(R.id.foreign_text_view);
        this.row3CurrencyTextView = findViewById(R.id.account_currency_text_view);
        this.statusTextView = findViewById(R.id.status_text);
        this.fetchRateButton = findViewById(R.id.fetch_rate_button);
        this.lockRow1Icon.setOnClickListener(v -> setCalculatedField(isSwapped ? CalculatedField.ACCOUNT : CalculatedField.FOREIGN));
        this.lockRow2Icon.setOnClickListener(v -> setCalculatedField(CalculatedField.RATE));
        this.lockRow3Icon.setOnClickListener(v -> setCalculatedField(isSwapped ? CalculatedField.FOREIGN : CalculatedField.ACCOUNT));
        this.fetchRateButton.setOnClickListener(v -> fetchHistoricalRate());
        findViewById(R.id.swap_button).setOnClickListener(v -> performVisualSwap());
        findViewById(R.id.foreign_row).setOnClickListener(v -> { if (!isSwapped) openCurrencyPicker(); });
        findViewById(R.id.account_row).setOnClickListener(v -> { if (isSwapped) openCurrencyPicker(); });
        this.row1EditText.addTextChangedListener(row1Watcher);
        this.row2EditText.addTextChangedListener(row2Watcher);
        this.row3EditText.addTextChangedListener(row3Watcher);

        // Suppress system keyboard
        this.row1EditText.setShowSoftInputOnFocus(false);
        this.row2EditText.setShowSoftInputOnFocus(false);
        this.row3EditText.setShowSoftInputOnFocus(false);

        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        int amountColor = getAmountTextColor();
        int groupBg = PocketMoneyThemes.groupTableViewBackgroundColor();
        
        findViewById(R.id.parent_view).setBackgroundColor(groupBg);
        findViewById(R.id.scrollView).setBackgroundColor(groupBg);

        // Theme the rows
        findViewById(R.id.foreign_row).setBackgroundResource(PocketMoneyThemes.editRowSelector(0));
        findViewById(R.id.rate_row).setBackgroundResource(PocketMoneyThemes.editRowSelector(1));
        findViewById(R.id.account_row).setBackgroundResource(PocketMoneyThemes.editRowSelector(2));

        this.row1EditText.setTextColor(amountColor);
        this.row2EditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.row3EditText.setTextColor(amountColor);
        this.row1CurrencyTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.row3CurrencyTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());

        ((TextView)findViewById(R.id.foreign_amount_label)).setTextColor(fieldLabelColor);
        ((TextView)findViewById(R.id.exchange_rate_label)).setTextColor(fieldLabelColor);
        ((TextView)findViewById(R.id.account_amount_label)).setTextColor(fieldLabelColor);
        this.statusTextView.setTextColor(fieldLabelColor);
        
        // Tint links and swap lines
        findViewById(R.id.link1).setBackgroundColor(fieldLabelColor);
        findViewById(R.id.link2).setBackgroundColor(fieldLabelColor);
        findViewById(R.id.swap_vertical_line).setBackgroundColor(fieldLabelColor);
        findViewById(R.id.swap_top_tick).setBackgroundColor(fieldLabelColor);
        findViewById(R.id.swap_bottom_tick).setBackgroundColor(fieldLabelColor);
        ((TextView)findViewById(R.id.multiply_label)).setTextColor(fieldLabelColor);
        ((TextView)findViewById(R.id.equals_label)).setTextColor(fieldLabelColor);

        this.fetchRateButton.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        ((ImageView)findViewById(R.id.currency_picker_arrow)).setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        ((ImageView)findViewById(R.id.account_currency_picker_arrow)).setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        ((ImageView)findViewById(R.id.swap_button)).setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        this.currencyKeyboard = findViewById(R.id.keyboardView);
        this.currencyKeyboard.setEditText(this.row1EditText, null);
        this.currencyKeyboard.setEditText(this.row2EditText, null);
        this.currencyKeyboard.setEditText(this.row3EditText, null);
    }

    private int getAmountTextColor() {
        if (this.transaction == null) return PocketMoneyThemes.primaryEditTextColor();
        return (this.transaction.getType() == Enums.kTransactionTypeWithdrawal || this.transaction.getType() == Enums.kTransactionTypeTransferTo) 
            ? PocketMoneyThemes.primaryEditTextColor() : PocketMoneyThemes.greenDepositColor();
    }

    private void setCalculatedField(CalculatedField field) {
        this.currentCalculatedField = field;
        updateLockStates();
        performCalculation();
    }

    private void updateLockStates() {
        int activeTint = PocketMoneyThemes.currentTintColor();
        int inactiveTint = Color.GRAY;
        boolean row1Calculated = isSwapped ? currentCalculatedField == CalculatedField.ACCOUNT : currentCalculatedField == CalculatedField.FOREIGN;
        boolean row2Calculated = currentCalculatedField == CalculatedField.RATE;
        boolean row3Calculated = isSwapped ? currentCalculatedField == CalculatedField.FOREIGN : currentCalculatedField == CalculatedField.ACCOUNT;
        this.lockRow1Icon.setImageResource(row1Calculated ? R.drawable.ic_lock : R.drawable.ic_lock_open);
        this.lockRow2Icon.setImageResource(row2Calculated ? R.drawable.ic_lock : R.drawable.ic_lock_open);
        this.lockRow3Icon.setImageResource(row3Calculated ? R.drawable.ic_lock : R.drawable.ic_lock_open);
        this.lockRow1Icon.setColorFilter(row1Calculated ? activeTint : inactiveTint);
        this.lockRow2Icon.setColorFilter(row2Calculated ? activeTint : inactiveTint);
        this.lockRow3Icon.setColorFilter(row3Calculated ? activeTint : inactiveTint);
        this.row1EditText.setEnabled(!row1Calculated);
        this.row2EditText.setEnabled(!row2Calculated);
        this.row3EditText.setEnabled(!row3Calculated);
        this.fetchRateButton.setVisibility(currentCalculatedField == CalculatedField.RATE ? View.GONE : View.VISIBLE);
        updateInstructionalText();
    }

    private void updateInstructionalText() {
        String foreignLabel = Locales.kLOC_EXCHANGERATE_WORKSHEET_FOREIGNAMOUNT;
        String rateLabel = Locales.kLOC_GENERAL_EXCHANGERATE;
        String accountLabel = Locales.kLOC_EXCHANGERATE_WORKSHEET_ACCOUNTAMOUNT;
        String instruction = switch (currentCalculatedField) {
            case FOREIGN -> "Enter " + rateLabel + " and " + accountLabel + " to calculate " + foreignLabel + ".";
            case RATE -> "Enter " + foreignLabel + " and " + accountLabel + " to calculate " + rateLabel + ".";
            case ACCOUNT -> "Enter " + foreignLabel + " and " + rateLabel + " to calculate " + accountLabel + ".";
        };
        this.statusTextView.setText(instruction);
    }

    private double round(double val) { return Math.round(val * 100.0) / 100.0; }

    private void performCalculation() {
        switch (currentCalculatedField) {
            case FOREIGN -> { if (exchangeRate != 0) foreignAmount = round(accountAmount / exchangeRate); }
            case RATE -> { if (foreignAmount != 0) exchangeRate = accountAmount / foreignAmount; }
            case ACCOUNT -> { accountAmount = round(foreignAmount * exchangeRate); }
        }
        programaticUpdate = true;
        updateVisualFields(false);
        programaticUpdate = false;
    }

    private void updateVisualFields(boolean forceAll) {
        View focused = getCurrentFocus();
        if (isSwapped) {
            if (forceAll || focused != row1EditText) this.row1EditText.setText(CurrencyExt.amountAsString(accountAmount));
            if (forceAll || focused != row2EditText) this.row2EditText.setText(CurrencyExt.exchangeRateAsString(exchangeRate != 0 ? 1.0 / exchangeRate : 1.0));
            if (forceAll || focused != row3EditText) this.row3EditText.setText(CurrencyExt.amountAsString(foreignAmount));
        } else {
            if (forceAll || focused != row1EditText) this.row1EditText.setText(CurrencyExt.amountAsString(foreignAmount));
            if (forceAll || focused != row2EditText) this.row2EditText.setText(CurrencyExt.exchangeRateAsString(exchangeRate));
            if (forceAll || focused != row3EditText) this.row3EditText.setText(CurrencyExt.amountAsString(accountAmount));
        }
    }

    private void loadCells() {
        programaticUpdate = true;
        updateVisualFields(true);
        this.row1CurrencyTextView.setText(isSwapped ? accountCurrency : foreignCurrency);
        this.row3CurrencyTextView.setText(isSwapped ? foreignCurrency : accountCurrency);
        TextView l1 = findViewById(R.id.foreign_amount_label);
        TextView l3 = findViewById(R.id.account_amount_label);
        l1.setText(isSwapped ? Locales.kLOC_EXCHANGERATE_WORKSHEET_ACCOUNTAMOUNT : Locales.kLOC_EXCHANGERATE_WORKSHEET_FOREIGNAMOUNT);
        l3.setText(isSwapped ? Locales.kLOC_EXCHANGERATE_WORKSHEET_FOREIGNAMOUNT : Locales.kLOC_EXCHANGERATE_WORKSHEET_ACCOUNTAMOUNT);
        findViewById(R.id.currency_picker_arrow).setVisibility(isSwapped ? View.GONE : View.VISIBLE);
        findViewById(R.id.account_currency_picker_arrow).setVisibility(isSwapped ? View.VISIBLE : View.GONE);
        programaticUpdate = false;
    }

    private void performVisualSwap() {
        TransitionManager.beginDelayedTransition(findViewById(R.id.inner_layout));
        this.isSwapped = !this.isSwapped;
        updateLockStates();
        loadCells();
    }

    private void openCurrencyPicker() {
        final String[] currencyCodes = CurrencyExt.getCurrenciesWithSymbols();
        new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme())
                .setItems(currencyCodes, (dialog, item) -> {
                    this.foreignCurrency = currencyCodes[item].substring(0, 3);
                    loadCells(); // Update labels immediately
                    if (currentCalculatedField != CalculatedField.RATE) fetchHistoricalRate();
                }).show();
    }

    private void fetchHistoricalRate() {
        if (this.transaction == null || currentCalculatedField == CalculatedField.RATE) return;
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(this.transaction.getDate().getTime());
        String from = this.foreignCurrency;
        String to = this.accountCurrency;
        this.statusTextView.setText(String.format("Fetching rate for %s...", dateStr));
        executor.execute(() -> {
            try {
                if (from.equals(to)) {
                    runOnUiThread(() -> {
                        this.exchangeRate = 1.0;
                        performCalculation();
                        loadCells();
                        updateInstructionalText();
                    });
                    return;
                }
                URL url = new URL("https://api.frankfurter.app/" + dateStr + "?from=" + from + "&to=" + to);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                r.close();
                JSONObject json = new JSONObject(sb.toString());
                double rate = json.getJSONObject("rates").getDouble(to);
                runOnUiThread(() -> {
                    this.exchangeRate = rate;
                    performCalculation();
                    loadCells();
                    updateInstructionalText();
                    Toast.makeText(this, "Rate updated", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    this.statusTextView.setText("Failed to fetch rate.");
                    Toast.makeText(this, "Fetch error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent i = new Intent();
        i.putExtra("xrate", this.exchangeRate);
        i.putExtra("amount", this.accountAmount);
        i.putExtra("currency", this.foreignCurrency);
        setResult(Activity.RESULT_OK, i);
        finish();
        return true;
    }

    @Override public void lookupExchangeRateCallback(ExchangeRateClass ex, double r, AccountClass a) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.currencyKeyboard.hide()) return false;
            return onSupportNavigateUp();
        }
        return super.onKeyDown(keyCode, event);
    }
}

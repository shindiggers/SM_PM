package com.example.smmoney.views.exchangerates;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.misc.CurrencyExt;
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

import java.util.ArrayList;
import java.util.Objects;

public class ExchangeRateActivity extends PocketMoneyActivity implements ExchangeRateCallbackInterface {
    private double accountAmount;
    private EditText accountAmountEditText;
    private RadioButton accountAmountRadioButton;
    private String accountCurrency;
    private TextView accountCurrencyTextView;
    private CurrencyKeyboard currencyKeyboard;
    private double exchangeRate;
    private EditText exchangeRateEditText;
    private RadioButton exchangeRateRadioButton;
    private double foreignAmount;
    private EditText foreignAmountEditText;
    private RadioButton foreignAmountRadioButton;
    private String foreignCurrency;
    private TextView foreignCurrencyTextView;
    @SuppressWarnings("unused")
    private OnFocusChangeListener mFocusChangedListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                ExchangeRateActivity.this.foreignAmountRadioButton.setEnabled(true);
                ExchangeRateActivity.this.exchangeRateRadioButton.setEnabled(true);
                ExchangeRateActivity.this.accountAmountRadioButton.setEnabled(true);
                ExchangeRateActivity.this.foreignAmountRadioButton.getBackground().setAlpha(255);
                ExchangeRateActivity.this.exchangeRateRadioButton.getBackground().setAlpha(255);
                ExchangeRateActivity.this.accountAmountRadioButton.getBackground().setAlpha(255);
                if (v.equals(ExchangeRateActivity.this.foreignAmountEditText)) {
                    if (ExchangeRateActivity.this.foreignAmountRadioButton.isChecked()) {
                        ExchangeRateActivity.this.exchangeRateRadioButton.setChecked(true);
                    }
                    ExchangeRateActivity.this.foreignAmountRadioButton.setEnabled(false);
                    ExchangeRateActivity.this.foreignAmountRadioButton.getBackground().setAlpha(50);
                } else if (v.equals(ExchangeRateActivity.this.exchangeRateEditText)) {
                    if (ExchangeRateActivity.this.exchangeRateRadioButton.isChecked()) {
                        ExchangeRateActivity.this.foreignAmountRadioButton.setChecked(true);
                    }
                    ExchangeRateActivity.this.exchangeRateRadioButton.setEnabled(false);
                    ExchangeRateActivity.this.exchangeRateRadioButton.getBackground().setAlpha(50);
                } else if (v.equals(ExchangeRateActivity.this.accountAmountEditText)) {
                    if (ExchangeRateActivity.this.accountAmountRadioButton.isChecked()) {
                        ExchangeRateActivity.this.foreignAmountRadioButton.setChecked(true);
                    }
                    ExchangeRateActivity.this.accountAmountRadioButton.setEnabled(false);
                    ExchangeRateActivity.this.accountAmountRadioButton.getBackground().setAlpha(50);
                }
            }
        }
    };
    private Runnable onFocusChangedRunnableAccountAmount = new Runnable() {
        public void run() {
            foreignAmountRadioButton.setVisibility(View.VISIBLE);
            exchangeRateRadioButton.setVisibility(View.VISIBLE);
            accountAmountRadioButton.setVisibility(View.VISIBLE);
            ExchangeRateActivity.this.foreignAmountRadioButton.setEnabled(true);
            ExchangeRateActivity.this.exchangeRateRadioButton.setEnabled(true);
            ExchangeRateActivity.this.accountAmountRadioButton.setEnabled(true);
            ExchangeRateActivity.this.foreignAmountRadioButton.getBackground().setAlpha(255);
            ExchangeRateActivity.this.exchangeRateRadioButton.getBackground().setAlpha(255);
            ExchangeRateActivity.this.accountAmountRadioButton.getBackground().setAlpha(255);
            if (!foreignAmountRadioButton.isChecked() && !exchangeRateRadioButton.isChecked() && !accountAmountRadioButton.isChecked()) {
                exchangeRateRadioButton.setChecked(true);
            }
            if (ExchangeRateActivity.this.accountAmountRadioButton.isChecked()) {
                ExchangeRateActivity.this.exchangeRateRadioButton.setChecked(true);
            }
            ExchangeRateActivity.this.foreignAmountRadioButton.setText("Change\nForeign Amount");
            ExchangeRateActivity.this.exchangeRateRadioButton.setText("Change\nExchange Rate");
            ExchangeRateActivity.this.accountAmountRadioButton.setText("Account\nAmount");
            ExchangeRateActivity.this.accountAmountRadioButton.setEnabled(false);
            ExchangeRateActivity.this.accountAmountRadioButton.getBackground().setAlpha(50);
        }
    };
    private Runnable onFocusChangedRunnableExchangeAmount = new Runnable() {
        public void run() {
            foreignAmountRadioButton.setVisibility(View.VISIBLE);
            exchangeRateRadioButton.setVisibility(View.VISIBLE);
            accountAmountRadioButton.setVisibility(View.VISIBLE);
            ExchangeRateActivity.this.foreignAmountRadioButton.setEnabled(true);
            ExchangeRateActivity.this.exchangeRateRadioButton.setEnabled(true);
            ExchangeRateActivity.this.accountAmountRadioButton.setEnabled(true);
            ExchangeRateActivity.this.foreignAmountRadioButton.getBackground().setAlpha(255);
            ExchangeRateActivity.this.exchangeRateRadioButton.getBackground().setAlpha(255);
            ExchangeRateActivity.this.accountAmountRadioButton.getBackground().setAlpha(255);
            if (!foreignAmountRadioButton.isChecked() && !exchangeRateRadioButton.isChecked() && !accountAmountRadioButton.isChecked()) {
                foreignAmountRadioButton.setChecked(true);
            }
            if (ExchangeRateActivity.this.exchangeRateRadioButton.isChecked()) {
                ExchangeRateActivity.this.foreignAmountRadioButton.setChecked(true);
            }
            ExchangeRateActivity.this.foreignAmountRadioButton.setText("Change\nForeign Amount");
            ExchangeRateActivity.this.exchangeRateRadioButton.setText("Exchange\nRate");
            ExchangeRateActivity.this.accountAmountRadioButton.setText("Change\nAccount Amount");
            ExchangeRateActivity.this.exchangeRateRadioButton.setEnabled(false);
            ExchangeRateActivity.this.exchangeRateRadioButton.getBackground().setAlpha(50);
        }
    };
    private Runnable onFocusChangedRunnableForeignAmount = new Runnable() {
        public void run() {
            foreignAmountRadioButton.setVisibility(View.VISIBLE);
            exchangeRateRadioButton.setVisibility(View.VISIBLE);
            accountAmountRadioButton.setVisibility(View.VISIBLE);
            ExchangeRateActivity.this.foreignAmountRadioButton.setEnabled(true);
            ExchangeRateActivity.this.exchangeRateRadioButton.setEnabled(true);
            ExchangeRateActivity.this.accountAmountRadioButton.setEnabled(true);
            ExchangeRateActivity.this.foreignAmountRadioButton.getBackground().setAlpha(255);
            ExchangeRateActivity.this.exchangeRateRadioButton.getBackground().setAlpha(255);
            ExchangeRateActivity.this.accountAmountRadioButton.getBackground().setAlpha(255);
            if (!foreignAmountRadioButton.isChecked() && !exchangeRateRadioButton.isChecked() && !accountAmountRadioButton.isChecked()) {
                exchangeRateRadioButton.setChecked(true);
            }
            if (ExchangeRateActivity.this.foreignAmountRadioButton.isChecked() || exchangeRateRadioButton.isChecked()) {
                ExchangeRateActivity.this.exchangeRateRadioButton.setChecked(true);
            }
            ExchangeRateActivity.this.foreignAmountRadioButton.setText("Foreign\nAmount");
            ExchangeRateActivity.this.exchangeRateRadioButton.setText("Change\nExchange Rate");
            ExchangeRateActivity.this.accountAmountRadioButton.setText("Change\nAccount Amount");
            ExchangeRateActivity.this.foreignAmountRadioButton.setEnabled(false);
            ExchangeRateActivity.this.foreignAmountRadioButton.getBackground().setAlpha(50);
        }
    };
    private boolean programaticUpdate;
    private TextView titleTextView;

    private class MyTextWatcher implements TextWatcher {
        private MyTextWatcher() {
        }

        public void afterTextChanged(Editable s) {
            if (!ExchangeRateActivity.this.programaticUpdate) {
                ExchangeRateActivity.this.textFieldDidChange();
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        double d = 0.0d;
        super.onCreate(savedInstanceState);
        setResult(0);
        Bundle b = getIntent().getExtras();
        try {
            if (b != null) {
                this.accountCurrency = Objects.requireNonNull(AccountDB.recordFor(((TransactionClass) Objects.requireNonNull(b.get("transaction"))).getAccount())).getCurrencyCode();
            }
        } catch (NullPointerException e) {
            this.accountCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
        }
        try {
            SplitsClass s = null;
            if (b != null) {
                s = (SplitsClass) b.get("split");
            }
            double xrate;
            if (s != null) {
                xrate = s.getXrate();

                setExchangeRate(xrate);
                setAccountAmount(s.getAmount());
                if (xrate != 0.0d) {
                    d = s.getAmount() / xrate;
                }

                setForeignAmount(d);
                this.foreignCurrency = s.getCurrencyCode();
            }
        } catch (NullPointerException e2) {
            this.exchangeRate = 1.0d;
            this.accountAmount = 1.0d;
            this.foreignAmount = 1.0d;
        }
        setContentView(R.layout.exchangerate);
        setupButtons();
        loadCells();
        setTitle();
    }

    private void setTitle() {
        this.titleTextView.setText(Locales.kLOC_GENERAL_EXCHANGERATE);
    }

    private void setExchangeRate(double n) {
        n = Math.abs(n);
        if (n == 0.0d) {
            this.exchangeRate = 1.0d;
        } else {
            this.exchangeRate = n;
        }
    }

    private void setAccountAmount(double n) {
        this.accountAmount = Math.abs(n);
    }

    private void setForeignAmount(double n) {
        this.foreignAmount = Math.abs(n);
    }

    private void setupButtons() {
        this.foreignAmountEditText = findViewById(R.id.foreign_amount_edit_text);
        this.foreignCurrencyTextView = findViewById(R.id.foreign_text_view);
        this.exchangeRateEditText = findViewById(R.id.exchange_rate_edit_text);
        this.accountAmountEditText = findViewById(R.id.account_amount_edit_text);
        this.accountCurrencyTextView = findViewById(R.id.account_currency_text_view);
        Button invertButton = findViewById(R.id.invert_button);
        this.foreignAmountRadioButton = findViewById(R.id.foreign_amount_segmented);
        this.exchangeRateRadioButton = findViewById(R.id.exchange_rate_segmented);
        this.accountAmountRadioButton = findViewById(R.id.account_amount_segmented);
        invertButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!foreignAmountRadioButton.isChecked() && !exchangeRateRadioButton.isChecked() && !accountAmountRadioButton.isChecked()) {
                    accountAmountRadioButton.setChecked(true);
                }
                ExchangeRateActivity.this.saveXrate();
                ExchangeRateActivity.this.exchangeRateEditText.setText(CurrencyExt.exchangeRateAsString(1.0d / ExchangeRateActivity.this.exchangeRate));
            }
        });
        View v = (View) this.foreignAmountEditText.getParent();
        v.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        v.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final String[] currencyCodes = CurrencyExt.getCurrenciesWithSymbols();
                new Builder(ExchangeRateActivity.this).setItems(currencyCodes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (ExchangeRateActivity.this.exchangeRateRadioButton.isChecked()) {
                            if (ExchangeRateActivity.this.foreignAmountRadioButton.isEnabled()) {
                                ExchangeRateActivity.this.foreignAmountRadioButton.setChecked(true);
                            } else {
                                ExchangeRateActivity.this.accountAmountRadioButton.setChecked(true);
                            }
                        }
                        ExchangeRateActivity.this.selectedCurrency(currencyCodes[item].substring(0, 3));
                    }
                }).show();
            }
        });
        this.foreignAmountEditText.addTextChangedListener(new MyTextWatcher());
        this.exchangeRateEditText.addTextChangedListener(new MyTextWatcher());
        this.accountAmountEditText.addTextChangedListener(new MyTextWatcher());
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        findViewById(R.id.linearLayout1).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        findViewById(R.id.linearLayout2).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ArrayList<View> theViews = new ArrayList<>();
        TextView tView = findViewById(R.id.foreign_amount_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.foreignAmountEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.foreignCurrencyTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.exchange_rate_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.exchangeRateEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.account_amount_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.accountAmountEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.accountCurrencyTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        this.currencyKeyboard = findViewById(R.id.keyboardView);
        this.currencyKeyboard.setEditText(this.foreignAmountEditText, this.onFocusChangedRunnableForeignAmount);
        this.currencyKeyboard.setEditText(this.exchangeRateEditText, this.onFocusChangedRunnableExchangeAmount);
        this.currencyKeyboard.setEditText(this.accountAmountEditText, this.onFocusChangedRunnableAccountAmount);
        int i = 0;
        for (View theView : theViews) {
            (theView).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i++;
        }
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        if (!foreignAmountRadioButton.isChecked() && !exchangeRateRadioButton.isChecked() && !accountAmountRadioButton.isChecked()) {
            foreignAmountRadioButton.setVisibility(View.INVISIBLE);
            exchangeRateRadioButton.setVisibility(View.INVISIBLE);
            accountAmountRadioButton.setVisibility(View.INVISIBLE);
        }
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
    }

    public void lookupExchangeRateCallback(ExchangeRateClass exchangeRateInstance, final double rate, AccountClass account) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (rate == 0.0d) {
                    ExchangeRateActivity.this.exchangeRateEditText.setText("1");
                    return;
                }
                ExchangeRateActivity.this.exchangeRateEditText.setText(CurrencyExt.exchangeRateAsString(1.0d / rate));
                ExchangeRateActivity.this.exchangeRateEditText.invalidate();
            }
        });
    }

    private void selectedCurrency(final String currencyCode) {
        this.foreignCurrency = currencyCode;
        this.foreignCurrencyTextView.setText(this.foreignCurrency);
        new Runnable() {
            public void run() {
                new ExchangeRateClass(false, ExchangeRateActivity.this).lookupExchangeRate(currencyCode, ExchangeRateActivity.this.accountCurrencyTextView.getText().toString(), null);
            }
        }.run();
    }

    private void loadCells() {
        this.programaticUpdate = true;
        this.foreignAmountEditText.setText(this.foreignAmount != 0.0d ? CurrencyExt.amountAsString(this.foreignAmount) : "");
        this.foreignCurrencyTextView.setText(this.foreignCurrency);
        this.exchangeRateEditText.setText(this.exchangeRate != 0.0d ? CurrencyExt.amountAsString(this.exchangeRate) : "");
        this.accountAmountEditText.setText(this.accountAmount != 0.0d ? CurrencyExt.amountAsString(this.accountAmount) : "");
        this.accountCurrencyTextView.setText(this.accountCurrency);
        this.programaticUpdate = false;
    }

    private void screenDataToValues() {
        setForeignAmount(CurrencyExt.amountFromString(this.foreignAmountEditText.getText().toString()));
        this.foreignCurrency = this.foreignCurrencyTextView.getText().toString();
        saveXrate();
        setAccountAmount(CurrencyExt.amountFromString(this.accountAmountEditText.getText().toString()));
        this.accountCurrency = this.accountCurrencyTextView.getText().toString();
    }

    private void saveXrate() {
        setExchangeRate(CurrencyExt.amountFromStringWithCurrency(this.exchangeRateEditText.getText().toString(), null));
    }

    private void textFieldDidChange() {
        double d = 0.0d;
        this.programaticUpdate = true;
        screenDataToValues();
        if (this.exchangeRateRadioButton.isChecked()) {
            if (this.foreignAmount != 0.0d) {
                d = this.accountAmount / this.foreignAmount;
            }
            setExchangeRate(d);
            this.exchangeRateEditText.setText(CurrencyExt.exchangeRateAsString(this.exchangeRate));
        } else if (this.foreignAmountRadioButton.isChecked()) {
            if (this.exchangeRate != 0.0d) {
                d = this.accountAmount / this.exchangeRate;
            }
            setForeignAmount(d);
            this.foreignAmountEditText.setText(CurrencyExt.amountAsString(this.foreignAmount));
        } else if (this.accountAmountRadioButton.isChecked()) {
            setAccountAmount(this.foreignAmount * this.exchangeRate);
            this.accountAmountEditText.setText(CurrencyExt.amountAsString(this.accountAmount));
        }
        this.programaticUpdate = false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (this.currencyKeyboard.hide()) {
                return false;
            }
            Intent i = new Intent();
            i.putExtra("xrate", this.exchangeRate);
            i.putExtra("amount", this.accountAmount);
            i.putExtra("currency", this.foreignCurrency);
            setResult(1, i);
        }
        return super.onKeyDown(keyCode, event);
    }
}

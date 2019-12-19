package com.example.smmoney.views.accounts;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.ExchangeRateCallbackInterface;
import com.example.smmoney.misc.ExchangeRateClass;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.NoteEditor;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class AccountsEditActivity extends PocketMoneyActivity implements ExchangeRateCallbackInterface {
    public final int NOTE_EDIT_BUTTON = 3;
    private AccountClass account;
    private EditText accountName;
    private EditText accountNumber;
    private EditText bankID;
    private EditText checkNumber;
    private TextView currency;
    private Activity currentActivity;
    private EditText exchangeRate;
    private EditText expires;
    private EditText fee;
    private ImageView icon;
    private int iconResourceID;
    private EditText institution;
    private TextView keepTheChangeAccountTextView;
    private EditText keepTheChangeRoundToEditText;
    private FrameLayout keyboardToolbar;
    private EditText limit;
    private TextView notes;
    private EditText phone;
    private TextView titleTextView;
    private CheckBox totalworth;
    private TextView type;
    private EditText website;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.currentActivity = this;
        setContentView(R.layout.accounts_edit);
        this.account = (AccountClass) Objects.requireNonNull(getIntent().getExtras()).get("Account");
        loadInfo();
        setupButtons();
        setTitle();
        Objects.requireNonNull(getActionBar()).hide();
    }

    public void onResume() {
        super.onResume();
        if (!Prefs.getBooleanPref(Prefs.HINT_ACCOUNT_INFO)) {
            Builder alert = new Builder(this);
            alert.setMessage(Locales.kLOC_TIP_ACCOUNT_INFO);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Prefs.setPref(Prefs.HINT_ACCOUNT_INFO, true);
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    private void setupButtons() {
        LinearLayout v = (LinearLayout) this.type.getParent();
        v.setOnClickListener(getBtnClickListener());
        v.setTag(1);
        v = (LinearLayout) this.icon.getParent();
        v.setTag(2);
        v.setOnClickListener(getBtnClickListener());
        v = (LinearLayout) this.notes.getParent();
        v.setTag(3);
        v.setOnClickListener(getBtnClickListener());
        ((View) this.currency.getParent()).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String[] currencyCodes = CurrencyExt.getCurrenciesWithSymbols();
                new Builder(AccountsEditActivity.this).setItems(currencyCodes, new OnClickListener() {
                    public void onClick(DialogInterface dialog, final int item) {
                        AccountsEditActivity.this.currency.setText(currencyCodes[item].substring(0, 3));
                        dialog.dismiss();
                        new Runnable() {
                            public void run() {
                                new ExchangeRateClass(false, AccountsEditActivity.this).lookupExchangeRate(currencyCodes[item].substring(0, 3), Prefs.getStringPref(Prefs.HOMECURRENCYCODE), null);
                            }
                        }.run();
                    }
                }).show();
            }
        });
        TextView button = findViewById(R.id.save_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AccountsEditActivity.this.save();
                AccountsEditActivity.this.finish();
            }
        });
        button = findViewById(R.id.cancel_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AccountsEditActivity.this.copyDB();
                AccountsEditActivity.this.finish();
            }
        });
        this.keyboardToolbar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ScrollView sv = findViewById(R.id.scroll_view);
        sv.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) sv.getParent()).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());

        ArrayList<View> theViews = new ArrayList<>();

        TextView tView = findViewById(R.id.account_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.accountName.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.total_worth_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_type_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.type.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_icon_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_expires_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.expires.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_number_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.accountNumber.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.routing_number_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.bankID.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_institution_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.institution.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_phone_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.phone.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_website_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.website.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_fee_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.fee.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_limit_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.limit.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_check_number_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.checkNumber.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_currency_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.currency.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_exchangerate_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.exchangeRate.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_keep_the_change_account_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());

        TextView exchangeRateSuffix = findViewById(R.id.amount_xrate_text_view);
        exchangeRateSuffix.setTextColor(PocketMoneyThemes.primaryCellTextColor());

        this.keepTheChangeAccountTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        View aView = (View) tView.getParent();
        aView.setTag(18);
        aView.setOnClickListener(getBtnClickListener());
        theViews.add(aView);

        tView = findViewById(R.id.account_keep_the_change_round_to_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.keepTheChangeRoundToEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());

        tView = findViewById(R.id.account_notes_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.notes.setTextColor(PocketMoneyThemes.primaryCellTextColor());
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

    private void setTitle() {
        this.titleTextView.setText(Locales.kLOC_ACCOUNT_INFO_TITLE/*"Account Info"*/);
    }

    private void loadInfo() {
        this.accountName = findViewById(R.id.accountname);
        this.totalworth = findViewById(R.id.totalworthcb);
        this.type = findViewById(R.id.type);
        this.icon = findViewById(R.id.icon);
        this.expires = findViewById(R.id.expires);
        this.accountNumber = findViewById(R.id.accountnumber);
        this.bankID = findViewById(R.id.bankid);
        this.institution = findViewById(R.id.institution);
        this.phone = findViewById(R.id.phone);
        this.website = findViewById(R.id.website);
        this.fee = findViewById(R.id.fee);
        this.limit = findViewById(R.id.limit);
        this.checkNumber = findViewById(R.id.checknumber);
        this.notes = findViewById(R.id.notestextview);
        this.currency = findViewById(R.id.currency_edit_text);
        this.exchangeRate = findViewById(R.id.xratetextview);
        TextView exchangeRateSuffix = findViewById(R.id.amount_xrate_text_view);
        this.keepTheChangeAccountTextView = findViewById(R.id.keep_the_change_account_text);
        this.keepTheChangeRoundToEditText = findViewById(R.id.account_keep_the_change_round_to_text);
        this.keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        if (!Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            ((View) this.currency.getParent()).setVisibility(View.GONE);
            ((View) this.exchangeRate.getParent()).setVisibility(View.GONE);
            ((View) exchangeRateSuffix.getParent()).setVisibility(View.GONE);
        }
        this.accountName.setText(this.account.getAccount());
        this.totalworth.setChecked(this.account.getTotalWorth());
        this.type.setText(this.account.typeAsString());
        this.expires.setText(this.account.getExpirationDate());
        this.accountNumber.setText(this.account.getAccountNumber());
        this.bankID.setText(this.account.getRoutingNumber());
        this.institution.setText(this.account.getInstitution());
        this.phone.setText(this.account.getPhone());
        this.website.setText(this.account.getUrl());
        this.fee.setText(this.account.feeAsString());
        this.limit.setText(this.account.limitAsString());
        this.checkNumber.setText(this.account.getCheckNumber());
        setNotesText(this.account.getNotes());
        this.currency.setText(this.account.getCurrencyCode());
        this.exchangeRate.setText(this.account.exchangeRateAsString());
        exchangeRateSuffix.setText(String.format(getString(R.string.equalscurrencysymbol), Prefs.getStringPref(Prefs.HOMECURRENCYCODE)));
        this.keepTheChangeAccountTextView.setText(this.account.getKeepTheChangeAccount() == null ? "None" : this.account.getKeepTheChangeAccount());
        this.keepTheChangeRoundToEditText.setText(this.account.keepChangeRoundToAsString());
        this.iconResourceID = this.account.getIconFileNameResourceIDUsingContext(this.currentActivity);
        this.icon.setImageResource(this.iconResourceID);
    }

    private void save() {
        if (this.accountName.getText().toString().length() > 0) {
            this.account.setAccount(this.accountName.getText().toString());
            this.account.setTotalWorth(this.totalworth.isChecked());
            this.account.setTypeFromString(this.type.getText().toString());
            try {
                this.account.setIconFileNameFromResourceWithContext(this.iconResourceID, this.currentActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.account.setExpirationDate(this.expires.getText().toString());
            this.account.setAccountNumber(this.accountNumber.getText().toString());
            this.account.setRoutingNumber(this.bankID.getText().toString());
            this.account.setInstitution(this.institution.getText().toString());
            this.account.setPhone(this.phone.getText().toString());
            this.account.setUrl(this.website.getText().toString());
            this.account.setFeeFromString(this.fee.getText().toString());
            this.account.setLimitFromString(this.limit.getText().toString());
            this.account.setCheckNumber(this.checkNumber.getText().toString());
            this.account.setNotes(this.notes.getText().toString());
            this.account.setCurrencyCode(this.currency.getText().toString());
            try {
                this.account.setExchangeRate(Double.parseDouble(this.exchangeRate.getText().toString()));
            } catch (Exception e2) {
                this.account.setExchangeRate(1.0d);
            }
            this.account.setKeepChangeRoundToFromString(this.keepTheChangeRoundToEditText.getText().toString());
            this.account.setKeepTheChangeAccount(this.keepTheChangeAccountTextView.getText().toString());
            this.account.saveToDatabase();
        }
    }

    private void setNotesText(String note) {
        int i = 25;
        if (note.length() > 0) {
            TextView textView = this.notes;
            if (25 > note.length()) {
                i = note.length();
            }
            textView.setText(note.substring(0, i));
            return;
        }
        this.notes.setText("");
    }

    public void lookupExchangeRateCallback(ExchangeRateClass exchangeRateInstance, final double rate, AccountClass account) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (rate == 0.0d) {
                    AccountsEditActivity.this.exchangeRate.setText("1");
                    return;
                }
                Locale current = getResources().getConfiguration().locale;

                AccountsEditActivity.this.exchangeRate.setText(String.format(current, "%.3f", (Double) rate));
                AccountsEditActivity.this.exchangeRate.invalidate();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            String selection;
            try {
                if (data.getExtras() != null) {
                    selection = data.getExtras().getString("selection");
                    switch (requestCode) {
                        case SplitsActivity.RESULT_CHANGED /*1*/:
                            this.type.setText(selection);
                            return;
                        case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                            if (selection != null) {
                                this.iconResourceID = Integer.parseInt(selection);
                            }
                            this.icon.setImageResource(this.iconResourceID);
                            return;
                        case SplitsActivity.REQUEST_EDIT /*3*/:
                            if (resultCode == -1) {
                                this.account.setNotes(selection);
                                if (selection != null) {
                                    setNotesText(selection);
                                }
                                return;
                            }
                            return;
                        case LookupsListActivity.ACCOUNT_LOOKUP_WITH_NONE /*18*/:
                            this.keepTheChangeAccountTextView.setText(selection);
                            return;
                        default:
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener getBtnClickListener() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                Intent i;
                switch ((Integer) view.getTag()) {
                    case SplitsActivity.RESULT_CHANGED /*1*/:
                    case LookupsListActivity.ACCOUNT_LOOKUP_WITH_NONE /*18*/:
                        i = new Intent(AccountsEditActivity.this.currentActivity, LookupsListActivity.class);
                        i.putExtra("type", ((Integer) view.getTag()).intValue());
                        AccountsEditActivity.this.currentActivity.startActivityForResult(i, (Integer) view.getTag());
                        return;
                    case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                        AccountsEditActivity.this.currentActivity.startActivityForResult(new Intent(AccountsEditActivity.this.currentActivity, AccountTypeIconGridActivity.class), 2);
                        return;
                    case NOTE_EDIT_BUTTON /*3*/:
                        i = new Intent(AccountsEditActivity.this.currentActivity, NoteEditor.class);
                        i.putExtra("note", AccountsEditActivity.this.account.getNotes());
                        AccountsEditActivity.this.currentActivity.startActivityForResult(i, (Integer) view.getTag());
                        return;
                    default:
                }
            }
        };
    }

    private void copyFile(File src, File dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel(); FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    private void copyDB() {
        try {
            copyFile(new File(Environment.getDataDirectory() + "/data/com.catamount.pocketmoney/databases/SMMoneyDB.sql"), new File(SMMoney.getExternalPocketMoneyDirectory(), "databasedata"));
        } catch (IOException e) {
            Log.e("com.catamount.pocketmon", "what the what the what the fuck - " + e.getMessage());
        }
    }
}

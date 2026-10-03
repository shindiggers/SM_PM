package com.example.smmoney.views.accounts;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.IntentCompat;
import androidx.core.os.ConfigurationCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.ExchangeRateCallbackInterface;
import com.example.smmoney.misc.ExchangeRateClass;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.NoteEditor;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;

import java.util.Locale;

public class AccountsEditActivity extends PocketMoneyActivity implements ExchangeRateCallbackInterface {
    private static final String TAG = "AccountsEditActivity";
    private static final int MENU_SAVE = 1;
    public final int NOTE_EDIT_BUTTON = 3;

    private final ActivityResultLauncher<Intent> typeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.type.setText(selection);
        }
    });

    private final ActivityResultLauncher<Intent> iconLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            if (selection != null) {
                this.iconResourceID = Integer.parseInt(selection);
            }
            this.icon.setImageResource(this.iconResourceID);
        }
    });

    private final ActivityResultLauncher<Intent> noteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == -1 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.account.setNotes(selection);
            if (selection != null) {
                setNotesText(selection);
            }
        }
    });

    private final ActivityResultLauncher<Intent> ktcLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            this.keepTheChangeAccountTextView.setText(selection);
        }
    });

    private AccountClass account;
    private EditText accountName;
    private EditText accountNumber;
    private EditText bankID;
    private EditText checkNumber;
    private TextView currency;
    private EditText exchangeRate;
    private EditText expires;
    private EditText fee;
    private ImageView icon;
    private int iconResourceID;
    private EditText institution;
    private TextView keepTheChangeAccountTextView;
    private EditText keepTheChangeRoundToEditText;
    private TextView ccCycleTemplateLabel;
    private Spinner ccCycleTemplateSpinner;
    private TextView ccStatementDayLabel;
    private Spinner ccStatementDaySpinner;
    private TextView ccDueOffsetLabel;
    private Spinner ccDueOffsetSpinner;
    private FrameLayout keyboardToolbar;
    private EditText limit;
    private TextView notes;
    private EditText phone;
    private CheckBox totalworth;
    private TextView type;
    private EditText website;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts_edit);
        this.account = IntentCompat.getSerializableExtra(getIntent(), "Account", AccountClass.class);
        loadInfo();
        setupButtons();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_ACCOUNT_INFO_TITLE);
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
        menu.add(0, MENU_SAVE, 0, Locales.kLOC_GENERAL_SAVE)
                .setIcon(R.drawable.ic_save_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_SAVE) {
            save();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        if (!Prefs.getBooleanPref(Prefs.HINT_ACCOUNT_INFO)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this, PocketMoneyThemes.dialogTheme());
            alert.setMessage(Locales.kLOC_TIP_ACCOUNT_INFO);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
                Prefs.setPref(Prefs.HINT_ACCOUNT_INFO, true);
                dialog.dismiss();
            });
            alert.show();
        }
    }

    private void setupButtons() {
        View v = findViewById(R.id.type_row);
        v.setOnClickListener(getBtnClickListener());
        v.setTag(1);
        v = findViewById(R.id.icon_row);
        v.setTag(2);
        v.setOnClickListener(getBtnClickListener());
        v = findViewById(R.id.notes_row);
        v.setTag(3);
        v.setOnClickListener(getBtnClickListener());
        findViewById(R.id.currency_row).setOnClickListener(v1 -> {
            final String[] currencyCodes = CurrencyExt.getCurrenciesWithSymbols();
            new AlertDialog.Builder(AccountsEditActivity.this, PocketMoneyThemes.dialogTheme()).setItems(currencyCodes, (dialog, item) -> {
                AccountsEditActivity.this.currency.setText(currencyCodes[item].substring(0, 3));
                dialog.dismiss();
                new Thread(() -> new ExchangeRateClass(false, AccountsEditActivity.this).lookupExchangeRate(currencyCodes[item].substring(0, 3), Prefs.getStringPref(Prefs.HOMECURRENCYCODE), null)).start();
            }).show();
        });

        this.keyboardToolbar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ScrollView sv = findViewById(R.id.scroll_view);
        sv.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        TextView tView = findViewById(R.id.account_keep_the_change_account_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());

        TextView exchangeRateSuffix = findViewById(R.id.amount_xrate_text_view);
        exchangeRateSuffix.setTextColor(PocketMoneyThemes.primaryCellTextColor());

        this.keepTheChangeAccountTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        View aView = findViewById(R.id.ktc_account_row);
        aView.setTag(18);
        aView.setOnClickListener(getBtnClickListener());
        

        
        

        TextView label;
        label = findViewById(R.id.account_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.total_worth_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_type_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_icon_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_expires_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_number_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.routing_number_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_institution_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_phone_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_website_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_fee_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_limit_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_check_number_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_currency_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_exchangerate_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_keep_the_change_round_to_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());
        label = findViewById(R.id.account_notes_label); label.setTextColor(PocketMoneyThemes.fieldLabelColor());

        this.accountName.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.type.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.expires.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.accountNumber.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.bankID.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.institution.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.phone.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.website.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.fee.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.limit.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.checkNumber.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.currency.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.exchangeRate.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.keepTheChangeRoundToEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.notes.setTextColor(PocketMoneyThemes.primaryCellTextColor());

        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        ImageView iconView;
        if ((iconView = findViewById(R.id.type_arrow)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = findViewById(R.id.icon_arrow)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = findViewById(R.id.currency_arrow)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = findViewById(R.id.ktc_arrow)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
        if ((iconView = findViewById(R.id.notes_arrow)) != null) iconView.setColorFilter(fieldLabelColor, PorterDuff.Mode.SRC_IN);
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
        
        this.ccCycleTemplateLabel = findViewById(R.id.account_cc_cycle_template_label);
        this.ccCycleTemplateSpinner = findViewById(R.id.account_cc_cycle_template_spinner);
        this.ccStatementDayLabel = findViewById(R.id.account_cc_statement_day_label);
        this.ccStatementDaySpinner = findViewById(R.id.account_cc_statement_day_spinner);
        this.ccDueOffsetLabel = findViewById(R.id.account_cc_due_offset_label);
        this.ccDueOffsetSpinner = findViewById(R.id.account_cc_due_offset_spinner);
        this.keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        if (!Prefs.getBooleanPref(Prefs.MULTIPLECURRENCIES)) {
            findViewById(R.id.account_currency_label).setVisibility(View.GONE);
            this.currency.setVisibility(View.GONE);
            findViewById(R.id.currency_arrow).setVisibility(View.GONE);
            findViewById(R.id.account_exchangerate_label).setVisibility(View.GONE);
            this.exchangeRate.setVisibility(View.GONE);
            exchangeRateSuffix.setVisibility(View.GONE);
            findViewById(R.id.divider4).setVisibility(View.GONE);
        }
        this.accountName.setText(this.account.getAccount());
        this.totalworth.setChecked(this.account.getTotalWorth());
        CheckBoxTint.colorCheckBox(this.totalworth);
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
        
        setupCreditCardSpinners();
        
        this.iconResourceID = this.account.getIconFileNameResourceIDUsingContext(this);
        this.icon.setImageResource(this.iconResourceID);
    }

    private void setupCreditCardSpinners() {
        boolean isCreditCard = this.account.getType() == Enums.kAccountTypeCreditCard;
        this.ccCycleTemplateLabel.setVisibility(isCreditCard ? View.VISIBLE : View.GONE);
        this.ccCycleTemplateSpinner.setVisibility(isCreditCard ? View.VISIBLE : View.GONE);
        this.ccStatementDayLabel.setVisibility(isCreditCard ? View.VISIBLE : View.GONE);
        this.ccStatementDaySpinner.setVisibility(isCreditCard ? View.VISIBLE : View.GONE);
        this.ccDueOffsetLabel.setVisibility(isCreditCard ? View.VISIBLE : View.GONE);
        this.ccDueOffsetSpinner.setVisibility(isCreditCard ? View.VISIBLE : View.GONE);

        if (!isCreditCard) return;

        // 1. Template Spinner
        String[] templates = new String[]{"Standard UK (Working Day Statement -> Offset Payment)", "Standard US (Fixed Due Date -> Offset Statement)", "Fixed Calendar Day (Fixed Statement -> Offset Payment)", "Custom"};
        ArrayAdapter<String> templateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, templates);
        templateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.ccCycleTemplateSpinner.setAdapter(templateAdapter);

        // 2. Statement Day Spinner
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.ccStatementDaySpinner.setAdapter(dayAdapter);

        // 3. Due Offset Spinner
        String[] offsets = new String[60];
        for (int i = 0; i < 60; i++) offsets[i] = String.valueOf(i);
        ArrayAdapter<String> offsetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, offsets);
        offsetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.ccDueOffsetSpinner.setAdapter(offsetAdapter);

        // Parse existing rules
        String stmtRule = this.account.getCcStatementCycleRule();
        String dueRule = this.account.getCcDueDateRule();
        
        // Very basic mapping for prototype
        if (stmtRule.startsWith("WorkingDayOfMonth")) {
            this.ccCycleTemplateSpinner.setSelection(0);
            try { this.ccStatementDaySpinner.setSelection(Integer.parseInt(stmtRule.split(":")[1]) - 1); } catch (Exception ignored) {}
        } else if (stmtRule.startsWith("WorkingDaysBeforeDue")) {
            this.ccCycleTemplateSpinner.setSelection(1);
            try { this.ccDueOffsetSpinner.setSelection(Integer.parseInt(stmtRule.split(":")[1])); } catch (Exception ignored) {}
        } else if (stmtRule.startsWith("FixedCalendarDay")) {
            this.ccCycleTemplateSpinner.setSelection(2);
            try { this.ccStatementDaySpinner.setSelection(Integer.parseInt(stmtRule.split(":")[1]) - 1); } catch (Exception ignored) {}
        } else {
            this.ccCycleTemplateSpinner.setSelection(3);
        }
        
        if (dueRule.startsWith("CalendarDaysAfterStatement")) {
            try { this.ccDueOffsetSpinner.setSelection(Integer.parseInt(dueRule.split(":")[1])); } catch (Exception ignored) {}
        } else if (dueRule.startsWith("FixedDayOfMonth")) {
            try { this.ccStatementDaySpinner.setSelection(Integer.parseInt(dueRule.split(":")[1]) - 1); } catch (Exception ignored) {}
        }
    }

    private void save() {
        if (!this.accountName.getText().toString().isEmpty()) {
            this.account.setAccount(this.accountName.getText().toString());
            this.account.setTotalWorth(this.totalworth.isChecked());
            this.account.setTypeFromString(this.type.getText().toString());
            try {
                this.account.setIconFileNameFromResourceWithContext(this.iconResourceID, this);
            } catch (Exception e) {
                Log.e(TAG, "Exception in save (setting icon)", e);
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

            if (this.account.getType() == Enums.kAccountTypeCreditCard) {
                int tmpl = this.ccCycleTemplateSpinner.getSelectedItemPosition();
                int day = this.ccStatementDaySpinner.getSelectedItemPosition() + 1; // 1-based index
                int offset = this.ccDueOffsetSpinner.getSelectedItemPosition();
                
                if (tmpl == 0) {
                    // Standard UK
                    this.account.setCcStatementCycleRule("WorkingDayOfMonth:" + day);
                    this.account.setCcDueDateRule("CalendarDaysAfterStatement:" + offset);
                    this.account.setCcWeekendPolicy("FRIDAY_PULLS_TO_THURSDAY");
                } else if (tmpl == 1) {
                    // Standard US
                    this.account.setCcStatementCycleRule("WorkingDaysBeforeDue:" + offset);
                    this.account.setCcDueDateRule("FixedDayOfMonth:" + day); // 'day' here is due date
                    this.account.setCcWeekendPolicy("NEXT_WORKING_DAY");
                } else if (tmpl == 2) {
                    // Fixed Calendar Day
                    this.account.setCcStatementCycleRule("FixedCalendarDay:" + day);
                    this.account.setCcDueDateRule("CalendarDaysAfterStatement:" + offset);
                    this.account.setCcWeekendPolicy("NEXT_WORKING_DAY");
                }
            }

            this.account.saveToDatabase();
        }
    }

    private void setNotesText(String note) {
        int i = 25;
        if (!note.isEmpty()) {
            TextView textView = this.notes;
            if (25 > note.length()) {
                i = note.length();
            }
            textView.setText(note.substring(0, i));
            return;
        }
        this.notes.setText("");
    }

    @Override
    public void lookupExchangeRateCallback(ExchangeRateClass exchangeRateInstance, final double rate, AccountClass account) {
        runOnUiThread(() -> {
            if (rate == 0.0d) {
                AccountsEditActivity.this.exchangeRate.setText(Locales.kLOC_EXCHANGERATE_DEFAULT_RATE);
                return;
            }

            Locale current = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
            if (current == null) {
                current = Locale.getDefault();
            }
            String formattedRate = String.format(current, "%.3f", rate);

            if (Prefs.getBooleanPref(Prefs.UPDATEEXCHANGERATES)) {
                // Auto-update is ON: Just apply the rate
                AccountsEditActivity.this.exchangeRate.setText(formattedRate);
                AccountsEditActivity.this.exchangeRate.invalidate();
            } else {
                // Auto-update is OFF: Show the decision dialog
                String currentRateStr = AccountsEditActivity.this.exchangeRate.getText().toString();
                new AlertDialog.Builder(AccountsEditActivity.this, PocketMoneyThemes.dialogTheme())
                        .setTitle(Locales.kLOC_GENERAL_EXCHANGERATE)
                        .setMessage(String.format(Locales.kLOC_EXCHANGERATE_DIALOG_MESSAGE,
                                AccountsEditActivity.this.currency.getText().toString(),
                                formattedRate,
                                currentRateStr))
                        .setPositiveButton(Locales.kLOC_EXCHANGERATE_DIALOG_USE_SPOT, (dialog, which) -> {
                            AccountsEditActivity.this.exchangeRate.setText(formattedRate);
                            AccountsEditActivity.this.exchangeRate.invalidate();
                        })
                        .setNegativeButton(Locales.kLOC_EXCHANGERATE_DIALOG_KEEP_CURRENT, null)
                        .show();
            }
        });
    }

    private View.OnClickListener getBtnClickListener() {
        return view -> {
            Intent i;
            switch ((Integer) view.getTag()) {
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    i = new Intent(AccountsEditActivity.this, LookupsListActivity.class);
                    i.putExtra("type", 1);
                    typeLauncher.launch(i);
                    return;
                case LookupsListActivity.ACCOUNT_LOOKUP_WITH_NONE /*18*/:
                    i = new Intent(AccountsEditActivity.this, LookupsListActivity.class);
                    i.putExtra("type", 18);
                    ktcLauncher.launch(i);
                    return;
                case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                    iconLauncher.launch(new Intent(AccountsEditActivity.this, AccountTypeIconGridActivity.class));
                    return;
                case NOTE_EDIT_BUTTON /*3*/:
                    i = new Intent(AccountsEditActivity.this, NoteEditor.class);
                    i.putExtra("note", AccountsEditActivity.this.account.getNotes());
                    noteLauncher.launch(i);
                    return;
                default:
            }
        };
    }
}












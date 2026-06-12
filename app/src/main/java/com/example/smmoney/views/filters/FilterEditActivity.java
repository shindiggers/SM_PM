package com.example.smmoney.views.filters;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.FilterClass;
import com.example.smmoney.views.FromToDateActivity;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Objects;

public class FilterEditActivity extends PocketMoneyActivity {
    private static final int MENU_SAVE = 1;
    private final ActivityResultLauncher<Intent> customDateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            Intent data = result.getData();
            GregorianCalendar gregorianCalendar = null;
            String date = data.getStringExtra("FromDate");
            if (date != null) {
                this.filter.setDateFrom(date.equals("*") ? null : CalExt.dateFromDescriptionWithMediumDate(date));
            }
            date = data.getStringExtra("ToDate");
            if (date != null && !date.equals("*")) {
                gregorianCalendar = CalExt.dateFromDescriptionWithMediumDate(date);
            }
            this.filter.setDateTo(gregorianCalendar);
            this.datesTextView.setText(this.filter.customDateString());
        }
    });

    private final ActivityResultLauncher<Intent> lookupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            String selection = result.getData().getStringExtra("selection");
            int type = result.getResultCode();
            switch (type) {
                case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                    this.transactionTypeTextView.setText(selection);
                    break;
                case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                    this.accountsTextView.setText(selection);
                    break;
                case LookupsListActivity.FILTER_DATES /*10*/:
                    if (selection != null && selection.contains(Locales.kLOC_FILTER_DATES_CUSTOM)) {
                        Intent i = new Intent(this, FromToDateActivity.class);
                        i.putExtra("FromDate", this.filter.getDateFrom() != null ? CalExt.descriptionWithMediumDate(this.filter.getDateFrom()) : "*");
                        i.putExtra("ToDate", this.filter.getDateTo() != null ? CalExt.descriptionWithMediumDate(this.filter.getDateTo()) : "*");
                        customDateLauncher.launch(i);
                    }
                    this.datesTextView.setText(selection);
                    this.filter.setDate(selection);
                    break;
                case LookupsListActivity.FILTER_PAYEES /*11*/:
                    this.payeeEditText.setText(selection);
                    break;
                case LookupsListActivity.FILTER_IDS /*12*/:
                    this.idEditText.setText(selection);
                    break;
                case LookupsListActivity.FILTER_CLEARED /*13*/:
                    this.clearedTextView.setText(selection);
                    break;
                case LookupsListActivity.FILTER_CATEGORIES /*14*/:
                    this.categoriesTextView.setText(selection);
                    break;
                case LookupsListActivity.FILTER_CLASSES /*15*/:
                    this.classesTextView.setText(selection);
                    break;
            }
        }
    });

    private TextView accountsTextView;
    private EditText categoriesTextView;
    private EditText classesTextView;
    private TextView clearedTextView;
    private FilterEditActivity currentActivity;
    private TextView datesTextView;
    private FilterClass filter;
    private EditText filterNameEditText;
    private EditText idEditText;
    private FrameLayout keyboardToolbar;
    private EditText payeeEditText;
    private TextView transactionTypeTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.filter = (FilterClass) Objects.requireNonNull(getIntent().getExtras()).get("Filter");
        this.currentActivity = this;
        setContentView(R.layout.filter_edit);
        loadInfo();
        setupButtons();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_TOOLS_FILTER_EDIT);
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
            save();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupButtons() {
        FrameLayout v = (FrameLayout) this.payeeEditText.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(11);
        v = (FrameLayout) this.idEditText.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(12);
        v = (FrameLayout) this.transactionTypeTextView.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(8);
        v = (FrameLayout) this.accountsTextView.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(9);
        v = (FrameLayout) this.datesTextView.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(10);
        v = (FrameLayout) this.clearedTextView.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(13);
        v = (FrameLayout) this.categoriesTextView.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(14);
        v = (FrameLayout) this.classesTextView.getParent();
        v.setOnClickListener(getLookupListClickListener());
        v.setTag(15);

        this.keyboardToolbar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ScrollView sv = findViewById(R.id.scroll_view);
        sv.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) sv.getParent()).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ArrayList<View> theViews = new ArrayList<>();
        TextView tView = findViewById(R.id.filter_name_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.filterNameEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_transaction_type_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.transactionTypeTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_accounts_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.accountsTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_dates_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.datesTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_payees_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.payeeEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_ids_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.idEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_cleared_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.clearedTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_category_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.categoriesTextView.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        tView = findViewById(R.id.filter_classes_label);
        tView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.classesTextView.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add((View) tView.getParent());
        int i = 0;
        for (View theView : theViews) {
            (theView).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i++;
        }
    }

    private void loadInfo() {
        this.filterNameEditText = findViewById(R.id.filtereditname);
        this.payeeEditText = findViewById(R.id.filtereditpayees);
        this.idEditText = findViewById(R.id.filtereditids);
        this.transactionTypeTextView = findViewById(R.id.filteredittranstype);
        this.accountsTextView = findViewById(R.id.filtereditaccounts);
        this.datesTextView = findViewById(R.id.filtereditdates);
        this.clearedTextView = findViewById(R.id.filtereditcleared);
        this.categoriesTextView = findViewById(R.id.filtereditcategories);
        this.classesTextView = findViewById(R.id.filtereditclasses);
        this.keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        this.filterNameEditText.setText(this.filter.getFilterName());
        this.payeeEditText.setText(this.filter.getPayee());
        this.idEditText.setText(this.filter.getCheckNumber());
        this.transactionTypeTextView.setText(this.filter.typeAsString());
        TextView textView = this.accountsTextView;
        CharSequence account = (this.filter.getAccount() == null || this.filter.getAccount().length() <= 0) ? Locales.kLOC_FILTERS_ALL_ACCOUNTS : this.filter.getAccount();
        textView.setText(account);
        this.datesTextView.setText(this.filter.isCustomDate() ? this.filter.customDateString() : this.filter.getDate());
        this.clearedTextView.setText(this.filter.clearedAsString());
        EditText editText = this.classesTextView;
        account = (this.filter.getClassName() == null || this.filter.getClassName().length() <= 0) ? Locales.kLOC_FILTERS_ALL_CLASSES : this.filter.getClassName();
        editText.setText(account);
        editText = this.categoriesTextView;
        account = (this.filter.getCategory() == null || this.filter.getCategory().length() <= 0) ? Locales.kLOC_FILTERS_ALL_CATEGORIES : this.filter.getCategory();
        editText.setText(account);
    }

    private void saveInfo() {
        this.filter.setFilterName(this.filterNameEditText.getText().toString());
        this.filter.setPayee(this.payeeEditText.getText().toString());
        this.filter.setCheckNumber(this.idEditText.getText().toString());
        this.filter.setTypeFromString(this.transactionTypeTextView.getText().toString());
        this.filter.setAccount(this.accountsTextView.getText().toString());
        this.filter.setClearedFromString(this.clearedTextView.getText().toString());
        this.filter.setCategory(this.categoriesTextView.getText().toString());
        this.filter.setClassName(this.classesTextView.getText().toString());
        if (this.filter.getCategory().equals(Locales.kLOC_FILTERS_ALL_CATEGORIES)) {
            this.filter.setCategory("");
        }
        if (this.filter.getClassName().equals(Locales.kLOC_FILTERS_ALL_CLASSES)) {
            this.filter.setClassName("");
        }
    }

    private void save() {
        saveInfo();
        Intent i = new Intent();
        i.putExtra("Filter", this.filter);
        setResult(1, i);
        this.filter.saveToDatabase();
    }

    private OnClickListener getLookupListClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(FilterEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", ((Integer) view.getTag()).intValue());
                i.putExtra("FromDate", FilterEditActivity.this.filter.getDateFrom() != null ? CalExt.descriptionWithMediumDate(FilterEditActivity.this.filter.getDateFrom()) : "*");
                i.putExtra("ToDate", FilterEditActivity.this.filter.getDateTo() != null ? CalExt.descriptionWithMediumDate(FilterEditActivity.this.filter.getDateTo()) : "*");
                lookupLauncher.launch(i);
            }
        };
    }
}

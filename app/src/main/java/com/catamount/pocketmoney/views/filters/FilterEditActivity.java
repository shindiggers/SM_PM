package com.catamount.pocketmoney.views.filters;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.Enums;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.records.FilterClass;
import com.catamount.pocketmoney.views.FromToDateActivity;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class FilterEditActivity extends PocketMoneyActivity {
    private final int REQUEST_CUSTOM_DATE = 30;
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
    private TextView titleTextView;
    private TextView transactionTypeTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.filter = (FilterClass) getIntent().getExtras().get("Filter");
        this.currentActivity = this;
        setContentView(R.layout.filter_edit);
        loadInfo();
        setupButtons();
        setTitle(Locales.kLOC_TOOLS_FILTER_EDIT);
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
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
        TextView button = findViewById(R.id.save_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FilterEditActivity.this.save();
                FilterEditActivity.this.finish();
            }
        });
        button = findViewById(R.id.cancel_button);
        button.setBackgroundResource(PocketMoneyThemes.currentTintToolbarButtonDrawable());
        button.setTextColor(-1);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FilterEditActivity.this.finish();
            }
        });
        this.keyboardToolbar.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ScrollView sv = findViewById(R.id.scroll_view);
        sv.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) sv.getParent()).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        ArrayList<View> theViews = new ArrayList();
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
        Iterator it = theViews.iterator();
        while (it.hasNext()) {
            ((View) it.next()).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i++;
        }
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        GregorianCalendar gregorianCalendar = null;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            String selection = data.getExtras().getString("selection");
            switch (requestCode) {
                case LookupsListActivity.FILTER_TRANSACTION_TYPE /*8*/:
                    this.transactionTypeTextView.setText(selection);
                    return;
                case LookupsListActivity.FILTER_ACCOUNTS /*9*/:
                    this.accountsTextView.setText(selection);
                    return;
                case LookupsListActivity.FILTER_DATES /*10*/:
                    if (selection.contains(Locales.kLOC_FILTER_DATES_CUSTOM)) {
                        Intent i = new Intent(this, FromToDateActivity.class);
                        i.putExtra("FromDate", this.filter.getDateFrom() != null ? CalExt.descriptionWithMediumDate(this.filter.getDateFrom()) : "*");
                        i.putExtra("ToDate", this.filter.getDateTo() != null ? CalExt.descriptionWithMediumDate(this.filter.getDateTo()) : "*");
                        startActivityForResult(i, 30);
                    }
                    this.datesTextView.setText(selection);
                    this.filter.setDate(selection);
                    return;
                case LookupsListActivity.FILTER_PAYEES /*11*/:
                    this.payeeEditText.setText(selection);
                    return;
                case LookupsListActivity.FILTER_IDS /*12*/:
                    this.idEditText.setText(selection);
                    return;
                case LookupsListActivity.FILTER_CLEARED /*13*/:
                    this.clearedTextView.setText(selection);
                    return;
                case LookupsListActivity.FILTER_CATEGORIES /*14*/:
                    this.categoriesTextView.setText(selection);
                    return;
                case LookupsListActivity.FILTER_CLASSES /*15*/:
                    this.classesTextView.setText(selection);
                    return;
                case Enums.kDesktopSyncStateSendPhotos /*30*/:
                    String date = data.getExtras().getString("FromDate");
                    this.filter.setDateFrom(date.equals("*") ? null : CalExt.dateFromDescriptionWithMediumDate(date));
                    date = data.getExtras().getString("ToDate");
                    FilterClass filterClass = this.filter;
                    if (!date.equals("*")) {
                        gregorianCalendar = CalExt.dateFromDescriptionWithMediumDate(date);
                    }
                    filterClass.setDateTo(gregorianCalendar);
                    this.datesTextView.setText(this.filter.customDateString());
                    return;
                default:
                    return;
            }
        }
    }

    private OnClickListener getLookupListClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(FilterEditActivity.this.currentActivity, LookupsListActivity.class);
                i.putExtra("type", ((Integer) view.getTag()).intValue());
                i.putExtra("FromDate", FilterEditActivity.this.filter.getDateFrom() != null ? CalExt.descriptionWithMediumDate(FilterEditActivity.this.filter.getDateFrom()) : "*");
                i.putExtra("ToDate", FilterEditActivity.this.filter.getDateTo() != null ? CalExt.descriptionWithMediumDate(FilterEditActivity.this.filter.getDateTo()) : "*");
                FilterEditActivity.this.currentActivity.startActivityForResult(i, (Integer) view.getTag());
            }
        };
    }
}

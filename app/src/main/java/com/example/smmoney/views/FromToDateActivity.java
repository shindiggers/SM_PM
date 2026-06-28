package com.example.smmoney.views;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class FromToDateActivity extends PocketMoneyActivity {
    private static final int FROMTODATE_RESULT_SELECTED = 1;
    private Button fromDateButton;
    private Button toDateButton;
    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fromtodate);
        setupView();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_CUSTOM_DATE_RANGE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        processDates();
        return true;
    }

    private void setupView() {
        this.fromDateButton = findViewById(R.id.fromtodatefromdate);
        this.toDateButton = findViewById(R.id.fromtodatetodate);
        this.errorMessage = findViewById(R.id.error_message);

        Button fromToday = findViewById(R.id.fromtodatelefttoday);
        Button fromNone = findViewById(R.id.fromtodateleftnone);
        Button toToday = findViewById(R.id.fromtodaterighttoday);
        Button toNone = findViewById(R.id.fromtodaterightnone);

        // Styling
        int themeLabelColor = PocketMoneyThemes.fieldLabelColor();
        int themeActionColor = PocketMoneyThemes.currentTintColor();
        int themeTextColor = PocketMoneyThemes.primaryCellTextColor();
        int rowBackground = PocketMoneyThemes.alternatingRowSelector();

        ((TextView) findViewById(R.id.from_date_label)).setTextColor(themeLabelColor);
        ((TextView) findViewById(R.id.to_date_label)).setTextColor(themeLabelColor);

        // Main Date Buttons
        this.fromDateButton.setTextColor(themeTextColor);
        this.fromDateButton.setBackgroundResource(rowBackground);
        this.toDateButton.setTextColor(themeTextColor);
        this.toDateButton.setBackgroundResource(rowBackground);

        // Tint the calendar icons to match theme
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(this.fromDateButton, android.content.res.ColorStateList.valueOf(themeActionColor));
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(this.toDateButton, android.content.res.ColorStateList.valueOf(themeActionColor));

        // Helper Buttons
        fromToday.setTextColor(themeTextColor);
        fromToday.setBackgroundResource(rowBackground);
        fromNone.setTextColor(themeTextColor);
        fromNone.setBackgroundResource(rowBackground);
        toToday.setTextColor(themeTextColor);
        toToday.setBackgroundResource(rowBackground);
        toNone.setTextColor(themeTextColor);
        toNone.setBackgroundResource(rowBackground);

        this.errorMessage.setTextColor(PocketMoneyThemes.redLabelColor());
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());

        // Date Pickers
        this.fromDateButton.setOnClickListener(v -> showDatePickerDialog(true));
        this.toDateButton.setOnClickListener(v -> showDatePickerDialog(false));

        // Helper Listeners
        fromToday.setOnClickListener(v -> {
            fromDateButton.setText(CalExt.descriptionWithMediumDate(new GregorianCalendar()));
            validateRange();
        });
        fromNone.setOnClickListener(v -> {
            fromDateButton.setText(Locales.kLOC_ANY_DATE);
            validateRange();
        });
        toToday.setOnClickListener(v -> {
            toDateButton.setText(CalExt.descriptionWithMediumDate(new GregorianCalendar()));
            validateRange();
        });
        toNone.setOnClickListener(v -> {
            toDateButton.setText(Locales.kLOC_ANY_DATE);
            validateRange();
        });

        // Initial Data
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String from = bundle.getString("FromDate", "*");
            String to = bundle.getString("ToDate", "*");
            this.fromDateButton.setText(from.equals("*") ? Locales.kLOC_ANY_DATE : from);
            this.toDateButton.setText(to.equals("*") ? Locales.kLOC_ANY_DATE : to);
        }
        validateRange();
    }

    private boolean validateRange() {
        String fromStr = this.fromDateButton.getText().toString();
        String toStr = this.toDateButton.getText().toString();

        if (fromStr.equals(Locales.kLOC_ANY_DATE) || toStr.equals(Locales.kLOC_ANY_DATE)) {
            errorMessage.setVisibility(View.GONE);
            return true;
        }

        GregorianCalendar fromDate = CalExt.dateFromDescriptionWithMediumDate(fromStr);
        GregorianCalendar toDate = CalExt.dateFromDescriptionWithMediumDate(toStr);

        if (toDate.before(fromDate)) {
            errorMessage.setVisibility(View.VISIBLE);
            return false;
        } else {
            errorMessage.setVisibility(View.GONE);
            return true;
        }
    }

    private void processDates() {
        if (validateRange()) {
            Intent i = new Intent();
            String from = this.fromDateButton.getText().toString();
            String to = this.toDateButton.getText().toString();
            i.putExtra("FromDate", from.equals(Locales.kLOC_ANY_DATE) ? "*" : from);
            i.putExtra("ToDate", to.equals(Locales.kLOC_ANY_DATE) ? "*" : to);
            setResult(FROMTODATE_RESULT_SELECTED, i);
            finish();
        } else {
            Toast.makeText(this, "Invalid date range", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog(boolean isFromDate) {
        Button targetButton = isFromDate ? fromDateButton : toDateButton;
        GregorianCalendar theDate;
        String buttonText = targetButton.getText().toString();
        if (buttonText.equals(Locales.kLOC_ANY_DATE)) {
            theDate = new GregorianCalendar();
        } else {
            theDate = CalExt.dateFromDescriptionWithMediumDate(buttonText);
        }

        new DatePickerDialog(this, PocketMoneyThemes.datePickerTheme(), getDateListener(isFromDate), theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private OnDateSetListener getDateListener(boolean isAFromDate) {
        final boolean isFromDate = isAFromDate;
        return (view, year, monthOfYear, dayOfMonth) -> {
            String dateStr = CalExt.descriptionWithMediumDate(new GregorianCalendar(year, monthOfYear, dayOfMonth));
            (isFromDate ? FromToDateActivity.this.fromDateButton : FromToDateActivity.this.toDateButton).setText(dateStr);
            validateRange();
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            processDates();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

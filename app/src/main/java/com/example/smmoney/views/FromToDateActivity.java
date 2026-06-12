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
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class FromToDateActivity extends PocketMoneyActivity {
    private static final int FROMTODATE_RESULT_SELECTED = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int FROMDATE_DIALOG_ID = 1;
    @SuppressWarnings("FieldCanBeLocal")
    private final int TODATE_DIALOG_ID = 2;
    private Button fromDate;
    private Button leftNoneButton;
    private Button leftTodayButton;
    private Button rightNoneButton;
    private Button rightTodayButton;
    private Button toDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fromtodate);
        setupView();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Locales.kLOC_FILTER_DATES_CUSTOM);
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
        this.leftNoneButton = findViewById(R.id.fromtodateleftnone);
        this.leftTodayButton = findViewById(R.id.fromtodatelefttoday);
        this.rightNoneButton = findViewById(R.id.fromtodaterightnone);
        this.rightTodayButton = findViewById(R.id.fromtodaterighttoday);
        this.fromDate = findViewById(R.id.fromtodatefromdate);
        this.toDate = findViewById(R.id.fromtodatetodate);
        this.fromDate.setOnClickListener(getOnDateClickListener());
        this.toDate.setOnClickListener(getOnDateClickListener());
        this.leftNoneButton.setOnClickListener(getOnNoneClickListener());
        this.rightNoneButton.setOnClickListener(getOnNoneClickListener());
        this.leftTodayButton.setOnClickListener(getOnTodayClickListener());
        this.rightTodayButton.setOnClickListener(getOnTodayClickListener());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            this.toDate.setText(bundle.getString("ToDate"));
            this.fromDate.setText(bundle.getString("FromDate"));
        }
        ((TextView) findViewById(R.id.arrow_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private void processDates() {
        Intent i = new Intent();
        i.putExtra("FromDate", this.fromDate.getText().toString());
        i.putExtra("ToDate", this.toDate.getText().toString());
        setResult(FROMTODATE_RESULT_SELECTED, i);
        finish();
    }

    private OnClickListener getOnTodayClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                (v == FromToDateActivity.this.leftTodayButton ? FromToDateActivity.this.fromDate : FromToDateActivity.this.toDate).setText(CalExt.descriptionWithMediumDate(new GregorianCalendar()));
            }
        };
    }

    private OnClickListener getOnNoneClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                (v == FromToDateActivity.this.leftNoneButton ? FromToDateActivity.this.fromDate : FromToDateActivity.this.toDate).setText("*");
            }
        };
    }

    private OnClickListener getOnDateClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                boolean isFromDate = v == FromToDateActivity.this.fromDate;

                // Toggle visibility of helpers based on which date is being picked
                int visibilityLeft = isFromDate ? View.GONE : View.VISIBLE;
                int visibilityRight = isFromDate ? View.VISIBLE : View.GONE;

                FromToDateActivity.this.leftNoneButton.setVisibility(visibilityLeft);
                FromToDateActivity.this.leftNoneButton.invalidate();
                FromToDateActivity.this.leftTodayButton.setVisibility(visibilityLeft);
                FromToDateActivity.this.leftTodayButton.invalidate();

                FromToDateActivity.this.rightNoneButton.setVisibility(visibilityRight);
                FromToDateActivity.this.rightNoneButton.invalidate();
                FromToDateActivity.this.rightTodayButton.setVisibility(visibilityRight);
                FromToDateActivity.this.rightTodayButton.invalidate();

                FromToDateActivity.this.showDatePickerDialog(isFromDate);
            }
        };
    }

    private void showDatePickerDialog(boolean isFromDate) {
        GregorianCalendar theDate;
        if ((isFromDate ? this.fromDate : this.toDate).getText().toString().equals("*")) {
            theDate = new GregorianCalendar();
        } else {
            theDate = CalExt.dateFromDescriptionWithMediumDate((isFromDate ? this.fromDate : this.toDate).getText().toString());
        }
        new DatePickerDialog(this, getDateListener(isFromDate), theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private OnDateSetListener getDateListener(boolean isAFromDate) {
        final boolean isFromDate = isAFromDate;
        return new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                (isFromDate ? FromToDateActivity.this.fromDate : FromToDateActivity.this.toDate).setText(CalExt.descriptionWithMediumDate(new GregorianCalendar(year, monthOfYear, dayOfMonth)));
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        processDates();
        return true;
    }
}

package com.catamount.pocketmoney.views;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class FromToDateActivity extends PocketMoneyActivity {
    public static final int FROMTODATE_RESULT_SELECTED = 1;
    private final int FROMDATE_DIALOG_ID = FROMTODATE_RESULT_SELECTED;
    private final int TODATE_DIALOG_ID = 2;
    private Button fromDate;
    private Button leftNoneButton;
    private Button leftTodayButton;
    private Button rightNoneButton;
    private Button rightTodayButton;
    private TextView titleTextView;
    private Button toDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LayoutInflater.from(this).inflate(R.layout.fromtodate, null));
        setupView();
        setTitle("PocketMoney");
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    public void setupView() {
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
        this.toDate.setText(bundle.getString("ToDate"));
        this.fromDate.setText(bundle.getString("FromDate"));
        ((TextView) findViewById(R.id.arrow_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        findViewById(R.id.parent_view).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        findViewById(R.id.the_tool_bar).setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
    }

    public void processDates() {
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
                boolean isFromDate;
                int i = 0;
                int i2 = FromToDateActivity.FROMTODATE_RESULT_SELECTED;
                isFromDate = v == FromToDateActivity.this.fromDate;
                FromToDateActivity.this.leftNoneButton.setVisibility(isFromDate ? View.GONE : View.VISIBLE);
                FromToDateActivity.this.leftNoneButton.invalidate();
                FromToDateActivity.this.leftTodayButton.setVisibility(isFromDate ? View.GONE : View.VISIBLE);
                FromToDateActivity.this.leftTodayButton.invalidate();
                FromToDateActivity.this.rightNoneButton.setVisibility(isFromDate ? View.VISIBLE : View.GONE);
                FromToDateActivity.this.rightNoneButton.invalidate();
                Button access$5 = FromToDateActivity.this.rightTodayButton;
                if (!isFromDate) {
                    i = FromToDateActivity.FROMTODATE_RESULT_SELECTED;
                }
                access$5.setVisibility(i);
                FromToDateActivity.this.rightTodayButton.invalidate();
                FromToDateActivity fromToDateActivity = FromToDateActivity.this;
                if (!isFromDate) {
                    i2 = 2;
                }
                fromToDateActivity.showDialog(i2);
            }
        };
    }

    protected Dialog onCreateDialog(int id) {
        if (id != FROMTODATE_RESULT_SELECTED && id != 2) {
            return null;
        }
        GregorianCalendar theDate;
        boolean isFromDate = id == FROMTODATE_RESULT_SELECTED;
        if ((isFromDate ? this.fromDate : this.toDate).getText().toString().equals("*")) {
            theDate = new GregorianCalendar();
        } else {
            theDate = CalExt.dateFromDescriptionWithMediumDate((isFromDate ? this.fromDate : this.toDate).getText().toString());
        }
        return new DatePickerDialog(this, getDateListener(isFromDate), theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_WEEK));
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

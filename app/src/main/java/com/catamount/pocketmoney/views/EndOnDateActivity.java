package com.catamount.pocketmoney.views;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.TextView;
//import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.splits.SplitsActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class EndOnDateActivity extends PocketMoneyActivity {
    public static int ENDONDATE_RESULT_DATESELECTED = 2;
    public static int ENDONDATE_RESULT_NODATESELECTED = 1;
    private final int DATE_DIALOG_ID = 1;
    private GregorianCalendar prevDate = null;
    private CheckBox theCheckbox;
    private TextView theDate;
    private FrameLayout theDateRow;
    private TextView titleTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LayoutInflater.from(this).inflate(R.layout.endondate, null));
        setupView();
        String previousDate = getIntent().getExtras().getString("Date");
        Log.d("ENDONDATEACTIVITY","String previous date = " + previousDate);
        if (!previousDate.equals(Locales.kLOC_EDIT_REPEATING_ENDONNONE)) { //kLOC_EDIT_REPEATING_ENDONNONE = "No end Date"
            Log.d("ENDONDATEACTIVITY","String previousDate != "+ Locales.kLOC_EDIT_REPEATING_ENDONNONE);
            this.prevDate = CalExt.dateFromDescriptionWithMediumDate(previousDate);

            this.theDate.setText(previousDate);
            this.theCheckbox.setChecked(true);
            this.theDateRow.setVisibility(View.VISIBLE);
        }
        getActionBar().setTitle("End on Date");
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    public void setupView() {
        this.theCheckbox = findViewById(R.id.endondatecheckbox);
        this.theCheckbox.setButtonDrawable(Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android"));
        this.theDate = findViewById(R.id.endondatedate);
        this.theDateRow = (FrameLayout) this.theDate.getParent();
        this.theCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EndOnDateActivity.this.theDateRow.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (isChecked) {
                    EndOnDateActivity.this.showDialog(1);
                }
            }
        });
        this.theDateRow.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EndOnDateActivity.this.showDialog(1);
            }
        });
        ((TextView) findViewById(R.id.endon_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());

        if (getIntent().getExtras().getBoolean(Prefs.BUDGETSTARTDATE)) {
            ((TextView) findViewById(R.id.endon_label)).setText(Locales.kLOC_BUDGET_STARTDATE);
        }
        this.theDate.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        ((View) this.theCheckbox.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.theDate.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.theDate.getParent().getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    public void processDate() {
        if (this.theCheckbox.isChecked()) {
            Intent i = new Intent();
            i.putExtra("Date", this.theDate.getText().toString());
            Log.d("ENDONDATEACT","PutExtra out of this method = "+ this.theDate.getText().toString());
            setResult(ENDONDATE_RESULT_DATESELECTED, i);
        } else {
            setResult(ENDONDATE_RESULT_NODATESELECTED);
        }
        finish();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID /*1*/:
                if (this.prevDate == null) {
                    this.prevDate = new GregorianCalendar();
                    this.theDate.setText(CalExt.descriptionWithMediumDate(this.prevDate));
                }
                return new DatePickerDialog(this, getDateListener(), this.prevDate.get(Calendar.YEAR), this.prevDate.get(Calendar.MONTH), this.prevDate.get(Calendar.DAY_OF_WEEK));
            default:
                return null;
        }
    }

    private OnDateSetListener getDateListener() {
        return new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                EndOnDateActivity.this.theDate.setText(CalExt.descriptionWithMediumDate(new GregorianCalendar(year, monthOfYear, dayOfMonth)));
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        processDate();
        return true;
    }
}

package com.example.smmoney.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.budgets.BudgetsDatePickerDialog;

import java.util.GregorianCalendar;
import java.util.Objects;


public class EndOnDateActivity extends PocketMoneyActivity implements DatePickerDialog.OnDateSetListener {
    public static int ENDONDATE_RESULT_DATESELECTED = 2;
    public static int ENDONDATE_RESULT_NODATESELECTED = 1;
    private GregorianCalendar prevDate = null;
    private CheckBox theCheckbox;
    private TextView theDate;
    private FrameLayout theDateRow;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LayoutInflater.from(this).inflate(R.layout.endondate, null));
        setupView();
        String previousDate = Objects.requireNonNull(getIntent().getExtras()).getString("Date");
        Log.d("ENDONDATEACTIVITY", "String previous date = " + previousDate);
        assert previousDate != null;
        if (!previousDate.equals(Locales.kLOC_EDIT_REPEATING_ENDONNONE)) { //kLOC_EDIT_REPEATING_ENDONNONE = "No end Date"
            Log.d("ENDONDATEACTIVITY", "String previousDate != " + Locales.kLOC_EDIT_REPEATING_ENDONNONE);
            this.prevDate = CalExt.dateFromDescriptionWithMediumDate(previousDate);

            this.theDate.setText(previousDate);
            this.theCheckbox.setChecked(true);
            this.theDateRow.setVisibility(View.VISIBLE);
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.kLOC_EDIT_REPEATING_ENDON);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
    }


    private void setupView() {
        this.theCheckbox = findViewById(R.id.endondatecheckbox);
        CheckBoxTint.colorCheckBox(this.theCheckbox);
        this.theDate = findViewById(R.id.endondatedate);
        ImageView theDateIcon = findViewById(R.id.endondate_ic_calendar);
        this.theDateRow = (FrameLayout) this.theDate.getParent();
        this.theCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EndOnDateActivity.this.theDateRow.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (isChecked) {
                    long datelong;

                    if (prevDate == null) {
                        prevDate = new GregorianCalendar();
                        datelong = prevDate.getTimeInMillis();


                    } else {
                        datelong = prevDate.getTimeInMillis();
                    }

                    Bundle args = new Bundle();
                    args.putLong("dateInt", datelong);
                    //EndOnDateActivity.this.showDialog(DATE_DIALOG_ID/*1*/);
                    DialogFragment datePicker = new BudgetsDatePickerDialog();
                    datePicker.setArguments(args);
                    datePicker.show(getSupportFragmentManager(), "date picker");
                }
            }
        });
        this.theDateRow.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                long datelong;

                if (prevDate == null) {
                    prevDate = new GregorianCalendar();
                    datelong = prevDate.getTimeInMillis();
                } else {
                    datelong = prevDate.getTimeInMillis();
                }

                Bundle args = new Bundle();
                args.putLong("dateInt", datelong);

                DialogFragment datePicker = new BudgetsDatePickerDialog();
                datePicker.setArguments(args);
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });
        ((TextView) findViewById(R.id.endon_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());

        if (Objects.requireNonNull(getIntent().getExtras()).getBoolean(Prefs.BUDGETSTARTDATE)) {
            ((TextView) findViewById(R.id.endon_label)).setText(Locales.kLOC_BUDGET_STARTDATE);
        }
        this.theDate.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theDateIcon.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
        ((View) this.theCheckbox.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.theDate.getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ((View) this.theDate.getParent().getParent()).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    private void processDate() {
        if (this.theCheckbox.isChecked()) {
            Intent i = new Intent();
            i.putExtra("Date", this.theDate.getText().toString());
            Log.d("ENDONDATEACT", "PutExtra out of this method = " + this.theDate.getText().toString());
            setResult(ENDONDATE_RESULT_DATESELECTED, i);
        } else {
            setResult(ENDONDATE_RESULT_NODATESELECTED);
        }
        finish();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        processDate();
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        EndOnDateActivity.this.theDate.setText(CalExt.descriptionWithMediumDate(new GregorianCalendar(year, monthOfYear, dayOfMonth)));
        prevDate.set(year, monthOfYear, dayOfMonth);
    }

}

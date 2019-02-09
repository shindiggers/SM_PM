package com.catamount.pocketmoney.views.repeating;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.records.RepeatingTransactionClass;
import com.catamount.pocketmoney.records.TransactionClass;
import com.catamount.pocketmoney.views.EndOnDateActivity;
import com.catamount.pocketmoney.views.PocketMoneyActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class RepeatingEditActivity extends PocketMoneyActivity {
    private final int REQUEST_ENDON = 1;
    private Context context;
    private String[] daysOfWeek;
    private TextView endOnTextView;
    private EditText everyTextView;
    private TextView frequencyTextView;
    private ImageView fridayCheck;
    private TextView fridayTextView;
    private String monday;
    private ImageView mondayCheck;
    private TextView mondayTextView;
    private CheckBox notifyCheckBox;
    private EditText notifyDaysInAdvanceTextView;
    private TextView repeatOnTextView;
    private RepeatingTransactionClass repeatingTransaction;
    private ImageView saturdayCheck;
    private TextView saturdayTextView;
    private String suffix = "";
    private TextView suffixTextView;
    private String sunday;
    private ImageView sundayCheck;
    private TextView sundayTextView;
    private ImageView thursdayCheck;
    private TextView thursdayTextView;
    private TextView titleTextView;
    private TransactionClass transaction;
    private ImageView tuesdayCheck;
    private TextView tuesdayTextView;
    private ImageView wednesdayCheck;
    private TextView wednesdayTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LayoutInflater.from(this).inflate(R.layout.repeating_edit, null));
        this.repeatingTransaction = (RepeatingTransactionClass) getIntent().getExtras().get("RepeatingTransaction");
        this.repeatingTransaction.hydrate();
        this.transaction = (TransactionClass) getIntent().getExtras().get("Transaction");
        this.repeatingTransaction.hydrated = true;
        this.repeatingTransaction.getTransaction().hydrated = true;
        this.context = this;
        setResult(0);
        loadViews();
        setupButtons();
        reloadData();
        setTitle(Locales.kLOC_EDIT_REPEATING_TITLE);
        getActionBar().setTitle(Locales.kLOC_EDIT_REPEATING_TITLE);
        getActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
    }

    public void onResume() {
        super.onResume();
        if (!Prefs.getBooleanPref(Prefs.HINT_REPEATING)) {
            Builder alert = new Builder(this);
            alert.setTitle(Locales.kLOC_TIP_REEPEATING_TITLE);
            alert.setMessage(Locales.kLOC_TIP_REEPEATING);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Prefs.setPref(Prefs.HINT_REPEATING, true);
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    private void setTitle(String title) {
        this.titleTextView.setText(title);
    }

    private void loadViews() {
        this.daysOfWeek = new DateFormatSymbols().getWeekdays();
        this.frequencyTextView = findViewById(R.id.frequencytextview);
        this.everyTextView = findViewById(R.id.everytextview);
        this.endOnTextView = findViewById(R.id.endontextview);
        this.sundayTextView = findViewById(R.id.sundaytextview);
        this.mondayTextView = findViewById(R.id.mondaytextview);
        this.tuesdayTextView = findViewById(R.id.tuesdaytextview);
        this.wednesdayTextView = findViewById(R.id.wednesdaytextview);
        this.thursdayTextView = findViewById(R.id.thursdaytextview);
        this.fridayTextView = findViewById(R.id.fridaytextview);
        this.saturdayTextView = findViewById(R.id.saturdaytextview);
        this.repeatOnTextView = findViewById(R.id.repeatontextview);
        this.suffixTextView = findViewById(R.id.suffixtextview);
        this.notifyDaysInAdvanceTextView = findViewById(R.id.daysinadvancetextview);
        this.notifyCheckBox = findViewById(R.id.notifycheckbox);
        this.notifyCheckBox.setButtonDrawable(Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android"));
        this.sunday = this.daysOfWeek[1];
        this.monday = this.daysOfWeek[2];
        this.tuesdayTextView.setText(this.daysOfWeek[3]);
        this.wednesdayTextView.setText(this.daysOfWeek[4]);
        this.thursdayTextView.setText(this.daysOfWeek[5]);
        this.fridayTextView.setText(this.daysOfWeek[6]);
        this.saturdayTextView.setText(this.daysOfWeek[7]);
        this.sundayCheck = findViewById(R.id.sundaycheck);
        this.mondayCheck = findViewById(R.id.mondaycheck);
        this.tuesdayCheck = findViewById(R.id.tuesdaycheck);
        this.wednesdayCheck = findViewById(R.id.wednesdaycheck);
        this.thursdayCheck = findViewById(R.id.thursdaycheck);
        this.fridayCheck = findViewById(R.id.fridaycheck);
        this.saturdayCheck = findViewById(R.id.saturdaycheck);
    }

    private void setupButtons() {
        ((View) this.frequencyTextView.getParent()).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(RepeatingEditActivity.this.context, LookupsListActivity.class);
                i.putExtra("type", 16);
                ((Activity) RepeatingEditActivity.this.context).startActivityForResult(i, 16);
            }
        });
        ((View) this.endOnTextView.getParent()).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(RepeatingEditActivity.this.context, EndOnDateActivity.class);
                i.putExtra("Date", RepeatingEditActivity.this.endOnTextView.getText().toString());
                ((Activity) RepeatingEditActivity.this.context).startActivityForResult(i, 1);
            }
        });
        this.everyTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    RepeatingEditActivity.this.everyTextView.setText(RepeatingEditActivity.this.everyTextView.getText().toString().replace(RepeatingEditActivity.this.suffix, ""));
                } else {
                    RepeatingEditActivity.this.everyTextView.setText(new StringBuilder(String.valueOf(RepeatingEditActivity.this.everyTextView.getText().toString())).append(RepeatingEditActivity.this.suffix).toString());
                }
            }
        });
        this.notifyCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((View) RepeatingEditActivity.this.notifyDaysInAdvanceTextView.getParent()).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        ((View) this.sundayTextView.getParent()).setOnClickListener(getDayClickListener(this.sundayCheck));
        ((View) this.mondayTextView.getParent()).setOnClickListener(getDayClickListener(this.mondayCheck));
        ((View) this.tuesdayTextView.getParent()).setOnClickListener(getDayClickListener(this.tuesdayCheck));
        ((View) this.wednesdayTextView.getParent()).setOnClickListener(getDayClickListener(this.wednesdayCheck));
        ((View) this.thursdayTextView.getParent()).setOnClickListener(getDayClickListener(this.thursdayCheck));
        ((View) this.fridayTextView.getParent()).setOnClickListener(getDayClickListener(this.fridayCheck));
        ((View) this.saturdayTextView.getParent()).setOnClickListener(getDayClickListener(this.saturdayCheck));
        ArrayList<View> theViews = new ArrayList();
        ScrollView sv = findViewById(R.id.scroll_view);
        sv.setVerticalScrollBarEnabled(false);
        sv.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        View aView = (View) this.frequencyTextView.getParent();
        ((TextView) findViewById(R.id.frequency_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.frequencyTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add(aView);
        aView = (View) this.everyTextView.getParent();
        ((TextView) findViewById(R.id.every_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.everyTextView.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        theViews.add(aView);
        aView = (View) this.endOnTextView.getParent();
        ((TextView) findViewById(R.id.end_on_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.endOnTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add(aView);
        aView = (View) this.notifyCheckBox.getParent();
        ((TextView) findViewById(R.id.notifylabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        this.notifyDaysInAdvanceTextView.setTextColor(-16777216);
        aView = (View) this.notifyDaysInAdvanceTextView.getParent();
        ((TextView) findViewById(R.id.daysinadvancelabel)).setTextColor(-16777216);
        ((TextView) findViewById(R.id.daysinadvancesuffixtextview)).setTextColor(-16777216);
        this.endOnTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        theViews.add(aView);
        aView = (View) this.repeatOnTextView.getParent();
        this.repeatOnTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        aView = (View) this.sundayTextView.getParent();
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.sundayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        aView = (View) this.mondayTextView.getParent();
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.mondayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        aView = (View) this.tuesdayTextView.getParent();
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.tuesdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        aView = (View) this.wednesdayTextView.getParent();
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.wednesdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        aView = (View) this.thursdayTextView.getParent();
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.thursdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        aView = (View) this.fridayTextView.getParent();
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.fridayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        aView = (View) this.saturdayTextView.getParent();
        aView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.saturdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        theViews.add(aView);
        this.suffixTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        int i = 0;
        Iterator it = theViews.iterator();
        while (it.hasNext()) {
            ((View) it.next()).setBackgroundResource(i % 2 == 0 ? PocketMoneyThemes.primaryRowSelector() : PocketMoneyThemes.alternatingRowSelector());
            i++;
        }
        this.titleTextView = findViewById(R.id.title_text_view);
        this.titleTextView.setTextColor(PocketMoneyThemes.toolbarTextColor());
        FrameLayout theView = findViewById(R.id.the_tool_bar);
        theView.setBackgroundResource(PocketMoneyThemes.currentTintDrawable());
        theView.setVisibility(View.GONE);
    }

    private void reloadData() {
        loadInfo();
        setupView();
    }

    private void loadInfo() {
        this.frequencyTextView.setText(this.repeatingTransaction.typeAsString());
        this.everyTextView.setText(new StringBuilder(String.valueOf(this.repeatingTransaction.getFrequency())).toString());
        this.notifyCheckBox.setChecked(this.repeatingTransaction.getSendLocalNotifications());
        this.notifyDaysInAdvanceTextView.setText(this.repeatingTransaction.getNotifyDaysInAdance());
        switch (this.repeatingTransaction.getType()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                this.suffix = Locales.kLOC_REPEATING_FREQUENCY_DAYS;
                break;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                this.suffix = Locales.kLOC_REPEATING_FREQUENCY_WEEKS;
                ImageView[] checkMarks = new ImageView[]{this.sundayCheck, this.mondayCheck, this.tuesdayCheck, this.wednesdayCheck, this.thursdayCheck, this.fridayCheck, this.saturdayCheck};
                int dow = 0;
                boolean atLeastOneDaySet = false;
                this.thursdayTextView.setText(this.daysOfWeek[5]);
                this.wednesdayTextView.setText(this.daysOfWeek[4]);
                this.tuesdayTextView.setText(this.daysOfWeek[3]);
                this.mondayTextView.setText(this.daysOfWeek[2]);
                this.sundayTextView.setText(this.daysOfWeek[1]);
                for (ImageView check : checkMarks) {
                    if (this.repeatingTransaction.repeatesOnDayOfWeek(dow)) {
                        check.setVisibility(View.VISIBLE);
                        atLeastOneDaySet = true;
                    } else {
                        check.setVisibility(View.GONE);
                    }
                    dow++;
                }
                if (!atLeastOneDaySet) {
                    checkMarks[new GregorianCalendar().get(Calendar.DAY_OF_WEEK) - 1].setVisibility(View.VISIBLE);
                    break;
                }
                break;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                this.suffix = Locales.kLOC_REPEATING_FREQUENCY_MONTHS;
                this.sundayTextView.setText(this.repeatingTransaction.repeatsOnDayOfMonthAsString());
                this.mondayTextView.setText(this.repeatingTransaction.repeatsOnDateOfMonthAsString());
                this.tuesdayTextView.setText(Locales.kLOC_REPEATING_LASTDAY_MONTH);
                this.wednesdayTextView.setText(this.repeatingTransaction.repeatsOnLastOrdinalWeekdayAsString());
                this.thursdayTextView.setText(Locales.kLOC_REPEATING_LASTWEEKDAY_MONTH);
                this.sundayCheck.setVisibility(View.GONE);
                this.mondayCheck.setVisibility(View.GONE);
                this.tuesdayCheck.setVisibility(View.GONE);
                this.wednesdayCheck.setVisibility(View.GONE);
                this.thursdayCheck.setVisibility(View.GONE);
                if (this.repeatingTransaction.getRepeatOn() != 0 || !this.repeatingTransaction.showOrdinalDayOfMonth()) {
                    if (this.repeatingTransaction.getRepeatOn() != 1 || !this.repeatingTransaction.showDateOfMonth()) {
                        if (this.repeatingTransaction.getRepeatOn() != 2 || !this.repeatingTransaction.isLastDay()) {
                            if (this.repeatingTransaction.getRepeatOn() != 4 || !this.repeatingTransaction.isLastDay()) {
                                if (this.repeatingTransaction.getRepeatOn() == 3 && this.repeatingTransaction.isLastWeekday()) {
                                    this.thursdayCheck.setVisibility(View.VISIBLE);
                                    break;
                                }
                            }
                            this.wednesdayCheck.setVisibility(View.VISIBLE);
                            break;
                        }
                        this.tuesdayCheck.setVisibility(View.VISIBLE);
                        break;
                    }
                    this.mondayCheck.setVisibility(View.VISIBLE);
                    break;
                }
                this.sundayCheck.setVisibility(View.VISIBLE);
                break;
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                this.suffix = Locales.kLOC_REPEATING_FREQUENCY_YEARS;
                break;
        }
        this.suffixTextView.setText(this.suffix);
        this.endOnTextView.setText(this.repeatingTransaction.getEndDate() != null ? CalExt.descriptionWithMediumDate(this.repeatingTransaction.getEndDate()) : Locales.kLOC_EDIT_REPEATING_ENDONNONE);
    }

    private void setupView() {
        int every = 8;
        int endon = 8;
        int sunday = 8;
        int monday = 8;
        int tuesday = 8;
        int wednesday = 8;
        int thursday = 8;
        int friday = 8;
        int saturday = 8;
        int repeaton = 8;
        int notify = 8;
        switch (this.repeatingTransaction.getType()) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                break;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                saturday = 0;
                friday = 0;
                thursday = 0;
                wednesday = 0;
                tuesday = 0;
                monday = 0;
                sunday = 0;
                repeaton = 0;
                break;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                View check = null;
                boolean alreadyChecked = false;
                every = 0;
                endon = 0;
                notify = 0;
                if (this.repeatingTransaction.isLastOrdinalWeekday()) {
                    wednesday = 0;
                    check = this.wednesdayCheck;
                    alreadyChecked = check.getVisibility() == View.VISIBLE;
                }
                if (this.repeatingTransaction.showOrdinalDayOfMonth()) {
                    sunday = 0;
                    if (!alreadyChecked) {
                        check = this.sundayCheck;
                        alreadyChecked = check.getVisibility() == View.VISIBLE;
                    }
                }
                if (this.repeatingTransaction.showDateOfMonth()) {
                    monday = 0;
                    if (!alreadyChecked) {
                        check = this.mondayCheck;
                        alreadyChecked = check.getVisibility() == View.VISIBLE;
                    }
                }
                if (this.repeatingTransaction.isLastDay()) {
                    tuesday = 0;
                    if (!alreadyChecked) {
                        check = this.tuesdayCheck;
                        alreadyChecked = check.getVisibility() == View.VISIBLE;
                    }
                }
                if (this.repeatingTransaction.isLastWeekday()) {
                    thursday = 0;
                    if (!alreadyChecked) {
                        check = this.thursdayCheck;
                        alreadyChecked = check.getVisibility() == View.VISIBLE;
                    }
                }
                if (!(alreadyChecked || check == null)) {
                    check.setVisibility(View.VISIBLE);
                    break;
                }
        }
        every = 0;
        endon = 0;
        notify = 0;
        ((View) this.notifyCheckBox.getParent()).setVisibility(notify);
        ((View) this.everyTextView.getParent()).setVisibility(every);
        ((View) this.endOnTextView.getParent()).setVisibility(endon);
        ((View) this.sundayTextView.getParent()).setVisibility(sunday);
        ((View) this.mondayTextView.getParent()).setVisibility(monday);
        ((View) this.tuesdayTextView.getParent()).setVisibility(tuesday);
        ((View) this.wednesdayTextView.getParent()).setVisibility(wednesday);
        ((View) this.thursdayTextView.getParent()).setVisibility(thursday);
        ((View) this.fridayTextView.getParent()).setVisibility(friday);
        ((View) this.saturdayTextView.getParent()).setVisibility(saturday);
        ((View) this.repeatOnTextView.getParent()).setVisibility(repeaton);
    }

    private void save() {
        this.repeatingTransaction.setTypeFromString(this.frequencyTextView.getText().toString());
        try {
            RepeatingTransactionClass repeatingTransactionClass = this.repeatingTransaction;
            int parseInt = (!this.everyTextView.getText().toString().contains(this.suffix) || this.suffix.equals("")) ? Integer.parseInt(this.everyTextView.getText().toString()) : Integer.parseInt(this.everyTextView.getText().toString().replace(this.suffix, ""));
            repeatingTransactionClass.setFrequency(parseInt);
        } catch (Exception e) {
            this.repeatingTransaction.setFrequency(1);
        }
        String endOnText = this.endOnTextView.getText().toString();
        this.repeatingTransaction.setEndDate(endOnText.equalsIgnoreCase(Locales.kLOC_EDIT_REPEATING_ENDONNONE) ? null : CalExt.dateFromDescriptionWithMediumDate(endOnText));
        switch (this.repeatingTransaction.getType()) {
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                int dow = 0;
                for (ImageView check : new ImageView[]{this.sundayCheck, this.mondayCheck, this.tuesdayCheck, this.wednesdayCheck, this.thursdayCheck, this.fridayCheck, this.saturdayCheck}) {
                    boolean z;
                    RepeatingTransactionClass repeatingTransactionClass2 = this.repeatingTransaction;
                    z = check.getVisibility() == View.VISIBLE;
                    repeatingTransactionClass2.setRepeatOnDay(dow, z);
                    dow++;
                }
                break;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                if (this.sundayCheck.getVisibility() != View.VISIBLE) {
                    if (this.mondayCheck.getVisibility() != View.VISIBLE) {
                        if (this.tuesdayCheck.getVisibility() != View.VISIBLE) {
                            if (this.wednesdayCheck.getVisibility() != View.VISIBLE) {
                                if (this.thursdayCheck.getVisibility() == View.VISIBLE) {
                                    this.repeatingTransaction.setRepeatOnMonth(3);
                                    break;
                                }
                            }
                            this.repeatingTransaction.setRepeatOnMonth(4);
                            break;
                        }
                        this.repeatingTransaction.setRepeatOnMonth(2);
                        break;
                    }
                    this.repeatingTransaction.setRepeatOnMonth(1);
                    break;
                }
                this.repeatingTransaction.setRepeatOnMonth(0);
                break;
        }
        this.repeatingTransaction.setSendLocalNotifications(this.notifyCheckBox.isChecked());
        try {
            this.repeatingTransaction.setNotifyDaysInAdvance(Integer.parseInt(this.notifyDaysInAdvanceTextView.getText().toString()));
        } catch (Exception e2) {
            this.repeatingTransaction.setNotifyDaysInAdvance(0);
        }
        Intent i = new Intent();
        i.putExtra("Transaction", this.transaction);
        i.putExtra("RepeatingTransaction", this.repeatingTransaction);
        setResult(1, i);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            switch (requestCode) {
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    if (resultCode != EndOnDateActivity.ENDONDATE_RESULT_DATESELECTED) {
                        this.endOnTextView.setText(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
                        break;
                    } else {
                        this.endOnTextView.setText(data.getExtras().getString("Date"));
                        break;
                    }
                case LookupsListActivity.REPEAT_TYPE /*16*/:
                    this.frequencyTextView.setText(data.getExtras().getString("selection"));
                    break;
            }
            this.repeatingTransaction.hydrated = true;
            save();
            reloadData();
        }
    }

    private View.OnClickListener getDayClickListener(final ImageView check) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                int i = 8;
                int i2 = 0;
                ImageView imageView;
                if (RepeatingEditActivity.this.repeatingTransaction.getType() == 2) {
                    imageView = check;
                    if (check.getVisibility() != View.VISIBLE) {
                        i = 0;
                    }
                    imageView.setVisibility(i);
                    return;
                }
                int i3;
                ImageView access$6 = RepeatingEditActivity.this.sundayCheck;
                if (check == RepeatingEditActivity.this.sundayCheck) {
                    i3 = 0;
                } else {
                    i3 = 8;
                }
                access$6.setVisibility(i3);
                access$6 = RepeatingEditActivity.this.mondayCheck;
                if (check == RepeatingEditActivity.this.mondayCheck) {
                    i3 = 0;
                } else {
                    i3 = 8;
                }
                access$6.setVisibility(i3);
                access$6 = RepeatingEditActivity.this.tuesdayCheck;
                if (check == RepeatingEditActivity.this.tuesdayCheck) {
                    i3 = 0;
                } else {
                    i3 = 8;
                }
                access$6.setVisibility(i3);
                access$6 = RepeatingEditActivity.this.wednesdayCheck;
                if (check == RepeatingEditActivity.this.wednesdayCheck) {
                    i3 = 0;
                } else {
                    i3 = 8;
                }
                access$6.setVisibility(i3);
                imageView = RepeatingEditActivity.this.thursdayCheck;
                if (check != RepeatingEditActivity.this.thursdayCheck) {
                    i2 = 8;
                }
                imageView.setVisibility(i2);
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 && keyCode != 3) {
            return super.onKeyDown(keyCode, event);
        }
        save();
        finish();
        return true;
    }
}

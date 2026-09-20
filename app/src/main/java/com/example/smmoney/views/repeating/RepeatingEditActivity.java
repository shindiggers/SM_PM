package com.example.smmoney.views.repeating;

import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.CheckBoxTint;
import com.example.smmoney.views.EndOnDateActivity;
import com.example.smmoney.views.PocketMoneyActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class RepeatingEditActivity extends PocketMoneyActivity {

    private final ActivityResultLauncher<Intent> frequencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            this.frequencyTextView.setText(result.getData().getStringExtra("selection"));
            this.repeatingTransaction.hydrated = true;
            save();
            reloadData();
        }
    });

    private final ActivityResultLauncher<Intent> endOnLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != 0 && result.getData() != null) {
            if (result.getResultCode() != EndOnDateActivity.ENDONDATE_RESULT_DATESELECTED) {
                this.endOnTextView.setText(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
            } else {
                this.endOnTextView.setText(result.getData().getStringExtra("Date"));
            }
            this.repeatingTransaction.hydrated = true;
            save();
            reloadData();
        }
    });

    private String[] daysOfWeek;
    private TextView endOnTextView;
    private EditText everyTextView;
    private TextView frequencyTextView;
    private ImageView fridayCheck;
    private TextView fridayTextView;
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
    private ImageView sundayCheck;
    private TextView sundayTextView;
    private ImageView thursdayCheck;
    private TextView thursdayTextView;
    private TransactionClass transaction;
    private ImageView tuesdayCheck;
    private TextView tuesdayTextView;
    private ImageView wednesdayCheck;
    private TextView wednesdayTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeating_edit);
        this.repeatingTransaction = androidx.core.content.IntentCompat.getSerializableExtra(getIntent(), "RepeatingTransaction", RepeatingTransactionClass.class);
        this.transaction = androidx.core.content.IntentCompat.getSerializableExtra(getIntent(), "Transaction", TransactionClass.class);
        this.repeatingTransaction.hydrate();
        this.repeatingTransaction.hydrated = true;
        this.repeatingTransaction.getTransaction().hydrated = true;
        setResult(RESULT_CANCELED);
        loadViews();
        setupButtons();
        reloadData();
        setTitle(Locales.kLOC_EDIT_REPEATING_TITLE);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Locales.kLOC_EDIT_REPEATING_TITLE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onResume() {
        super.onResume();
        if (!Prefs.getBooleanPref(Prefs.HINT_REPEATING)) {
            Builder alert = new Builder(this, PocketMoneyThemes.dialogTheme());
            alert.setTitle(Locales.kLOC_TIP_REEPEATING_TITLE);
            alert.setMessage(Locales.kLOC_TIP_REEPEATING);
            alert.setPositiveButton(Locales.kLOC_GENERAL_OK, (dialog, whichButton) -> {
                Prefs.setPref(Prefs.HINT_REPEATING, true);
                dialog.dismiss();
            });
            alert.show();
        }
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
        CheckBoxTint.colorCheckBox(this.notifyCheckBox);
        this.tuesdayTextView.setText(this.daysOfWeek[Calendar.TUESDAY /*3*/]);
        this.wednesdayTextView.setText(this.daysOfWeek[Calendar.WEDNESDAY /*4*/]);
        this.thursdayTextView.setText(this.daysOfWeek[Calendar.THURSDAY /*5*/]);
        this.fridayTextView.setText(this.daysOfWeek[Calendar.FRIDAY /*6*/]);
        this.saturdayTextView.setText(this.daysOfWeek[Calendar.SATURDAY /*7*/]);
        this.sundayCheck = findViewById(R.id.sundaycheck);
        this.mondayCheck = findViewById(R.id.mondaycheck);
        this.tuesdayCheck = findViewById(R.id.tuesdaycheck);
        this.wednesdayCheck = findViewById(R.id.wednesdaycheck);
        this.thursdayCheck = findViewById(R.id.thursdaycheck);
        this.fridayCheck = findViewById(R.id.fridaycheck);
        this.saturdayCheck = findViewById(R.id.saturdaycheck);
    }

    @Override
    public boolean onSupportNavigateUp() {
        save();
        finish();
        return true;
    }


    private void setupButtons() {
        findViewById(R.id.row_frequency).setOnClickListener(v -> {
            Intent i = new Intent(this, LookupsListActivity.class);
            i.putExtra("type", 16);
            frequencyLauncher.launch(i);
        });
        findViewById(R.id.row_endon).setOnClickListener(v -> {
            Intent i = new Intent(this, EndOnDateActivity.class);
            i.putExtra("Date", this.endOnTextView.getText().toString());
            endOnLauncher.launch(i);
        });
        this.everyTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                this.everyTextView.setText(this.everyTextView.getText().toString().replace(this.suffix, ""));
            } else {
                this.everyTextView.setText(String.format("%s%s", this.everyTextView.getText().toString(), this.suffix));
            }
        });
        this.notifyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> findViewById(R.id.row_daysinadvance).setVisibility(isChecked ? View.VISIBLE : View.GONE));
        findViewById(R.id.row_sunday).setOnClickListener(getDayClickListener(this.sundayCheck));
        findViewById(R.id.row_monday).setOnClickListener(getDayClickListener(this.mondayCheck));
        findViewById(R.id.row_tuesday).setOnClickListener(getDayClickListener(this.tuesdayCheck));
        findViewById(R.id.row_wednesday).setOnClickListener(getDayClickListener(this.wednesdayCheck));
        findViewById(R.id.row_thursday).setOnClickListener(getDayClickListener(this.thursdayCheck));
        findViewById(R.id.row_friday).setOnClickListener(getDayClickListener(this.fridayCheck));
        findViewById(R.id.row_saturday).setOnClickListener(getDayClickListener(this.saturdayCheck));
        // ArrayList<View> theViews = new ArrayList<>();
        ScrollView sv = findViewById(R.id.scroll_view);
        sv.setVerticalScrollBarEnabled(false);
        sv.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        ImageView frequencyDateIcon = findViewById(R.id.frequency_ic_calendar);
        frequencyDateIcon.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
        ((TextView) findViewById(R.id.frequency_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.frequencyTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        
        ((TextView) findViewById(R.id.every_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.everyTextView.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        
        ImageView theDateIcon = findViewById(R.id.repeting_edit_ic_calendar);
        theDateIcon.setColorFilter(PocketMoneyThemes.fieldLabelColor(), PorterDuff.Mode.SRC_IN);
        ((TextView) findViewById(R.id.end_on_label)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        this.endOnTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        
        ((TextView) findViewById(R.id.notifylabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        this.notifyDaysInAdvanceTextView.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        ((TextView) findViewById(R.id.daysinadvancelabel)).setTextColor(PocketMoneyThemes.fieldLabelColor());
        ((TextView) findViewById(R.id.daysinadvancesuffixtextview)).setTextColor(PocketMoneyThemes.primaryCellTextColor());
        this.endOnTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        
        this.repeatOnTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        findViewById(R.id.row_sunday).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.sundayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        findViewById(R.id.row_monday).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.mondayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        findViewById(R.id.row_tuesday).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.tuesdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        findViewById(R.id.row_wednesday).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.wednesdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        findViewById(R.id.row_thursday).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.thursdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        findViewById(R.id.row_friday).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.fridayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        findViewById(R.id.row_saturday).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.saturdayTextView.setTextColor(PocketMoneyThemes.fieldLabelColor());
        
        this.suffixTextView.setTextColor(PocketMoneyThemes.primaryCellTextColor());

        // Theme the dividers for visibility in all themes
        int[] dividerIds = {R.id.divider_freq, R.id.divider_notify, R.id.divider_repeaton, R.id.divider_sunday, R.id.divider_monday, R.id.divider_tuesday, R.id.divider_wednesday, R.id.divider_thursday, R.id.divider_friday, R.id.divider_saturday};
        for (int id : dividerIds) {
            View divider = findViewById(id);
            if (divider != null) {
                divider.setBackgroundColor(PocketMoneyThemes.fieldLabelColor());
                divider.setAlpha(0.3f); // Subtle but visible
            }
        }

    }

    private void reloadData() {
        loadInfo();
        setupView();
    }

    private void loadInfo() {
        this.frequencyTextView.setText(this.repeatingTransaction.typeAsString());
        this.everyTextView.setText(String.valueOf(this.repeatingTransaction.getFrequency()));
        this.notifyCheckBox.setChecked(this.repeatingTransaction.getSendLocalNotifications());
        this.notifyDaysInAdvanceTextView.setText(String.valueOf(this.repeatingTransaction.getNotifyDaysInAdance()));
        switch (this.repeatingTransaction.getType()) {
            case Enums.repeatDaily /*1*/:
                this.suffix = Locales.kLOC_REPEATING_FREQUENCY_DAYS;
                break;
            case Enums.repeatWeekly /*2*/:
                this.suffix = Locales.kLOC_REPEATING_FREQUENCY_WEEKS;
                ImageView[] checkMarks = new ImageView[]{this.sundayCheck, this.mondayCheck, this.tuesdayCheck, this.wednesdayCheck, this.thursdayCheck, this.fridayCheck, this.saturdayCheck};
                int dow = 0;
                boolean atLeastOneDaySet = false;
                this.thursdayTextView.setText(this.daysOfWeek[Calendar.THURSDAY /*5*/]);
                this.wednesdayTextView.setText(this.daysOfWeek[Calendar.WEDNESDAY /*4*/]);
                this.tuesdayTextView.setText(this.daysOfWeek[Calendar.TUESDAY /*3*/]);
                this.mondayTextView.setText(this.daysOfWeek[Calendar.MONDAY /*2*/]);
                this.sundayTextView.setText(this.daysOfWeek[Calendar.SUNDAY /*1*/]);
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
            case Enums.repeatMonthly /*3*/:
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
                if (this.repeatingTransaction.getRepeatOn() != Enums.monthlyDayOfMonth /*0*/ || !this.repeatingTransaction.showOrdinalDayOfMonth()) {
                    if (this.repeatingTransaction.getRepeatOn() != Enums.monthlyDateInMonth /*1*/ || !this.repeatingTransaction.showDateOfMonth()) {
                        if (this.repeatingTransaction.getRepeatOn() != Enums.monthlyLastDayOfMonth /*2*/ || !this.repeatingTransaction.isLastDay()) {
                            if (this.repeatingTransaction.getRepeatOn() != Enums.monthlyLastOrdinalWeekdayOfMonth /*4*/ || !this.repeatingTransaction.isLastDay()) {
                                if (this.repeatingTransaction.getRepeatOn() == Enums.monthlyLastWeekDayOfMonth /*3*/ && this.repeatingTransaction.isLastWeekday()) {
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
            case Enums.repeatYearly /*4*/:
                this.suffix = Locales.kLOC_REPEATING_FREQUENCY_YEARS;
                break;
        }
        this.suffixTextView.setText(this.suffix);
        this.endOnTextView.setText(this.repeatingTransaction.getEndDate() != null ? CalExt.descriptionWithMediumDate(this.repeatingTransaction.getEndDate()) : Locales.kLOC_EDIT_REPEATING_ENDONNONE);
    }

    private void setupView() {
        int every; // not necessary to initialize this view here as it gets initialized later
        int endon; // not necessary to initialize this view here as it gets initialized later
        int sunday = View.GONE /*8*/;
        int monday = View.GONE /*8*/;
        int tuesday = View.GONE /*8*/;
        int wednesday = View.GONE /*8*/;
        int thursday = View.GONE /*8*/;
        int friday = View.GONE /*8*/;
        int saturday = View.GONE /*8*/;
        int repeaton = View.GONE /*8*/;
        int notify; // not necessary to initialize this view here as it gets initialized later
        switch (this.repeatingTransaction.getType()) {
            case Enums.repeatDaily /*1*/:
            case Enums.repeatYearly /*4*/:
                break;
            case Enums.repeatWeekly /*2*/:
                saturday = View.VISIBLE /*0*/;
                friday = View.VISIBLE /*0*/;
                thursday = View.VISIBLE /*0*/;
                wednesday = View.VISIBLE /*0*/;
                tuesday = View.VISIBLE /*0*/;
                monday = View.VISIBLE /*0*/;
                sunday = View.VISIBLE /*0*/;
                repeaton = View.VISIBLE /*0*/;
                break;
            case Enums.repeatMonthly /*3*/:
                View check = null;
                boolean alreadyChecked = false;
                //every = View.VISIBLE /*0*/; TEMP CODED OUT AS JETBRAINS SAYS NOT NEEDED
                //endon = View.VISIBLE /*0*/; TEMP CODED OUT AS JETBRAINS SAYS NOT NEEDED
                //notify = View.VISIBLE /*0*/; TEMP CODED OUT AS JETBRAINS SAYS NOT NEEDED
                if (this.repeatingTransaction.isLastOrdinalWeekday()) {
                    wednesday = View.VISIBLE /*0*/;
                    check = this.wednesdayCheck;
                    alreadyChecked = check.getVisibility() == View.VISIBLE;
                }
                if (this.repeatingTransaction.showOrdinalDayOfMonth()) {
                    sunday = View.VISIBLE /*0*/;
                    if (!alreadyChecked) {
                        check = this.sundayCheck;
                        alreadyChecked = check.getVisibility() == View.VISIBLE;
                    }
                }
                if (this.repeatingTransaction.showDateOfMonth()) {
                    monday = View.VISIBLE /*0*/;
                    if (!alreadyChecked) {
                        check = this.mondayCheck;
                        alreadyChecked = check.getVisibility() == View.VISIBLE;
                    }
                }
                if (this.repeatingTransaction.isLastDay()) {
                    tuesday = View.VISIBLE /*0*/;
                    if (!alreadyChecked) {
                        check = this.tuesdayCheck;
                        alreadyChecked = check.getVisibility() == View.VISIBLE;
                    }
                }
                if (this.repeatingTransaction.isLastWeekday()) {
                    thursday = View.VISIBLE /*0*/;
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
        every = View.VISIBLE /*0*/;
        endon = View.VISIBLE /*0*/;
        notify = View.VISIBLE /*0*/;
        findViewById(R.id.row_notify).setVisibility(notify);
        findViewById(R.id.row_every).setVisibility(every);
        findViewById(R.id.row_endon).setVisibility(endon);
        findViewById(R.id.row_sunday).setVisibility(sunday);
        findViewById(R.id.row_monday).setVisibility(monday);
        findViewById(R.id.row_tuesday).setVisibility(tuesday);
        findViewById(R.id.row_wednesday).setVisibility(wednesday);
        findViewById(R.id.row_thursday).setVisibility(thursday);
        findViewById(R.id.row_friday).setVisibility(friday);
        findViewById(R.id.row_saturday).setVisibility(saturday);
        findViewById(R.id.row_repeaton).setVisibility(repeaton);
    }

    private void save() {
        this.repeatingTransaction.setTypeFromString(this.frequencyTextView.getText().toString());
        try {
            RepeatingTransactionClass repeatingTransactionClass = this.repeatingTransaction;
            int parseInt = (!this.everyTextView.getText().toString().contains(this.suffix) || this.suffix.isEmpty()) ? Integer.parseInt(this.everyTextView.getText().toString()) : Integer.parseInt(this.everyTextView.getText().toString().replace(this.suffix, ""));
            repeatingTransactionClass.setFrequency(parseInt);
        } catch (Exception e) {
            this.repeatingTransaction.setFrequency(1);
        }
        String endOnText = this.endOnTextView.getText().toString();
        this.repeatingTransaction.setEndDate(endOnText.equalsIgnoreCase(Locales.kLOC_EDIT_REPEATING_ENDONNONE) ? null : CalExt.dateFromDescriptionWithMediumDate(endOnText));
        switch (this.repeatingTransaction.getType()) {
            case Enums.repeatWeekly /*2*/:
                int dow = 0;
                for (ImageView check : new ImageView[]{this.sundayCheck, this.mondayCheck, this.tuesdayCheck, this.wednesdayCheck, this.thursdayCheck, this.fridayCheck, this.saturdayCheck}) {
                    boolean z;
                    RepeatingTransactionClass repeatingTransactionClass2 = this.repeatingTransaction;
                    z = check.getVisibility() == View.VISIBLE;
                    repeatingTransactionClass2.setRepeatOnDay(dow, z);
                    dow++;
                }
                break;
            case Enums.repeatMonthly /*3*/:
                if (this.sundayCheck.getVisibility() != View.VISIBLE) {
                    if (this.mondayCheck.getVisibility() != View.VISIBLE) {
                        if (this.tuesdayCheck.getVisibility() != View.VISIBLE) {
                            if (this.wednesdayCheck.getVisibility() != View.VISIBLE) {
                                if (this.thursdayCheck.getVisibility() == View.VISIBLE) { // This is opposite
                                    this.repeatingTransaction.setRepeatOnMonth(Enums.monthlyLastWeekDayOfMonth/*3*/);
                                    break;
                                }
                            }
                            this.repeatingTransaction.setRepeatOnMonth(Enums.monthlyLastOrdinalWeekdayOfMonth/*4*/);
                            break;
                        }
                        this.repeatingTransaction.setRepeatOnMonth(Enums.monthlyLastDayOfMonth/*2*/);
                        break;
                    }
                    this.repeatingTransaction.setRepeatOnMonth(Enums.monthlyDateInMonth/*1*/);
                    break;
                }
                this.repeatingTransaction.setRepeatOnMonth(Enums.monthlyDayOfMonth/*0*/);
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
        setResult(RESULT_OK, i);
    }

    private View.OnClickListener getDayClickListener(final ImageView check) {
        return v -> {
            int i = View.GONE /*8*/;
            int i2 = View.VISIBLE /*0*/;
            ImageView imageView;
            if (RepeatingEditActivity.this.repeatingTransaction.getType() == Enums.repeatWeekly /*2*/) {
                imageView = check;
                if (check.getVisibility() != View.VISIBLE) {
                    i = View.VISIBLE /*0*/;
                }
                imageView.setVisibility(i);
                return;
            }
            int i3;
            ImageView dayCheck = RepeatingEditActivity.this.sundayCheck;
            if (check == RepeatingEditActivity.this.sundayCheck) {
                i3 = View.VISIBLE /*0*/;
            } else {
                i3 = View.GONE /*8*/;
            }
            dayCheck.setVisibility(i3);
            dayCheck = RepeatingEditActivity.this.mondayCheck;
            if (check == RepeatingEditActivity.this.mondayCheck) {
                i3 = View.VISIBLE /*0*/;
            } else {
                i3 = View.GONE /*8*/;
            }
            dayCheck.setVisibility(i3);
            dayCheck = RepeatingEditActivity.this.tuesdayCheck;
            if (check == RepeatingEditActivity.this.tuesdayCheck) {
                i3 = View.VISIBLE /*0*/;
            } else {
                i3 = View.GONE /*8*/;
            }
            dayCheck.setVisibility(i3);
            dayCheck = RepeatingEditActivity.this.wednesdayCheck;
            if (check == RepeatingEditActivity.this.wednesdayCheck) {
                i3 = View.VISIBLE /*0*/;
            } else {
                i3 = View.GONE /*8*/;
            }
            dayCheck.setVisibility(i3);
            imageView = RepeatingEditActivity.this.thursdayCheck;
            if (check != RepeatingEditActivity.this.thursdayCheck) {
                i2 = View.GONE /*8*/;
            }
            imageView.setVisibility(i2);
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

    /**
     * This method is called when on pressing the back button arrow in the action bar, which is
     * implemented using the 'getSupportActionBar()' method. When the back button arrow is
     * clicked, it calls the 'save()' method and finishes the current activity.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item); // Call the default implementation if the selected menu item is not the back button arrow
    }

}









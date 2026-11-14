package com.example.smmoney.views.budgets;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class BudgetScheduleDialog extends AppCompatDialogFragment {

    public interface ScheduleDialogListener {
        void onScheduleUpdated(int periodType);
    }

    private ScheduleDialogListener listener;
    private int selectedPeriod;
    private String selectedAnchorDate;
    private boolean isCustomAnchor;

    private TextView selectedDateText;
    private View customDateContainer;
    private TextView explanationText;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ScheduleDialogListener) {
            listener = (ScheduleDialogListener) context;
        } else {
            throw new ClassCastException(context + " must implement ScheduleDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectedPeriod = Prefs.getIntPref(Prefs.DISPLAY_BUDGETPERIOD);
        selectedAnchorDate = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        if (selectedAnchorDate.isEmpty()) {
            selectedAnchorDate = SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT);
        }
        isCustomAnchor = !selectedAnchorDate.equalsIgnoreCase(SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT));

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_budget_schedule, null);

        // Styling
        int fieldLabelColor = PocketMoneyThemes.fieldLabelColor();
        int textColor = PocketMoneyThemes.primaryCellTextColor();
        ColorStateList tint = ColorStateList.valueOf(PocketMoneyThemes.currentTintColor());

        TextView freqLabel = view.findViewById(R.id.dialog_frequency_label);
        TextView anchorLabel = view.findViewById(R.id.dialog_anchor_label);
        freqLabel.setTextColor(fieldLabelColor);
        anchorLabel.setTextColor(fieldLabelColor);

        RadioGroup rgFrequency = view.findViewById(R.id.rg_frequency);
        RadioGroup rgAnchor = view.findViewById(R.id.rg_anchor);
        customDateContainer = view.findViewById(R.id.custom_date_container);
        selectedDateText = view.findViewById(R.id.tv_selected_anchor_date);
        MaterialButton btnChangeDate = view.findViewById(R.id.btn_change_anchor_date);
        explanationText = view.findViewById(R.id.tv_anchor_explanation);

        selectedDateText.setTextColor(textColor);
        btnChangeDate.setStrokeColor(ColorStateList.valueOf(fieldLabelColor));
        btnChangeDate.setTextColor(fieldLabelColor);

        // Theme all radio buttons
        for (int i = 0; i < rgFrequency.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgFrequency.getChildAt(i);
            rb.setTextColor(textColor);
            rb.setButtonTintList(tint);
        }
        for (int i = 0; i < rgAnchor.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgAnchor.getChildAt(i);
            rb.setTextColor(textColor);
            rb.setButtonTintList(tint);
        }

        // Set initial frequency radio
        int freqRbId = switch (selectedPeriod) {
            case Enums.kBudgetPeriodWeek -> R.id.rb_weekly;
            case Enums.kBudgetPeriodBiweekly -> R.id.rb_biweekly;
            case Enums.kBudgetPeriod4Weeks -> R.id.rb_4weeks;
            case Enums.kBudgetPeriodBimonthly -> R.id.rb_bimonthly;
            case Enums.kBudgetPeriodQuarter -> R.id.rb_quarterly;
            case Enums.kBudgetPeriodHalfYear -> R.id.rb_halfyear;
            case Enums.kBudgetPeriodYear -> R.id.rb_yearly;
            case Enums.kBudgetPeriodDay -> R.id.rb_daily;
            default -> R.id.rb_monthly;
        };
        rgFrequency.check(freqRbId);

        // Set initial anchor radio
        if (isCustomAnchor) {
            rgAnchor.check(R.id.rb_anchor_custom);
            customDateContainer.setVisibility(View.VISIBLE);
        } else {
            rgAnchor.check(R.id.rb_anchor_default);
            customDateContainer.setVisibility(View.GONE);
        }
        updateAnchorViews();

        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_monthly) selectedPeriod = Enums.kBudgetPeriodMonth;
            else if (checkedId == R.id.rb_biweekly) selectedPeriod = Enums.kBudgetPeriodBiweekly;
            else if (checkedId == R.id.rb_4weeks) selectedPeriod = Enums.kBudgetPeriod4Weeks;
            else if (checkedId == R.id.rb_weekly) selectedPeriod = Enums.kBudgetPeriodWeek;
            else if (checkedId == R.id.rb_quarterly) selectedPeriod = Enums.kBudgetPeriodQuarter;
            else if (checkedId == R.id.rb_halfyear) selectedPeriod = Enums.kBudgetPeriodHalfYear;
            else if (checkedId == R.id.rb_yearly) selectedPeriod = Enums.kBudgetPeriodYear;
            else if (checkedId == R.id.rb_bimonthly) selectedPeriod = Enums.kBudgetPeriodBimonthly;
            else if (checkedId == R.id.rb_daily) selectedPeriod = Enums.kBudgetPeriodDay;
            updateAnchorViews();
        });

        rgAnchor.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_anchor_default) {
                isCustomAnchor = false;
                customDateContainer.setVisibility(View.GONE);
            } else {
                isCustomAnchor = true;
                if (selectedAnchorDate.equalsIgnoreCase(SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT))) {
                    selectedAnchorDate = CalExt.descriptionWithMediumDate(new GregorianCalendar());
                }
                customDateContainer.setVisibility(View.VISIBLE);
            }
            updateAnchorViews();
        });

        btnChangeDate.setOnClickListener(v -> showDatePicker());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), PocketMoneyThemes.dialogTheme());
        builder.setTitle(R.string.BUDGET_SCHEDULE_TITLE)
                .setView(view)
                .setPositiveButton(R.string.GENERAL_OK, (dialog, which) -> {
                    String finalAnchor = isCustomAnchor ? selectedAnchorDate : SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT);
                    Prefs.setPref(Prefs.DISPLAY_BUDGETPERIOD, selectedPeriod);
                    Prefs.setPref(Prefs.BUDGETSTARTDATE, finalAnchor);
                    if (listener != null) {
                        listener.onScheduleUpdated(selectedPeriod);
                    }
                })
                .setNegativeButton(R.string.GENERAL_CANCEL, null);

        return builder.create();
    }

    private void showDatePicker() {
        GregorianCalendar initial = new GregorianCalendar();
        if (isCustomAnchor && !selectedAnchorDate.equalsIgnoreCase(SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT))) {
            initial = CalExt.dateFromDescriptionWithMediumDate(selectedAnchorDate);
        }

        DatePickerDialog dp = new DatePickerDialog(
                requireActivity(),
                PocketMoneyThemes.datePickerTheme(),
                (view, year, month, dayOfMonth) -> {
                    GregorianCalendar picked = new GregorianCalendar(year, month, dayOfMonth);
                    selectedAnchorDate = CalExt.descriptionWithMediumDate(picked);
                    updateAnchorViews();
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    private void updateAnchorViews() {
        GregorianCalendar now = new GregorianCalendar();

        if (isCustomAnchor) {
            selectedDateText.setText(selectedAnchorDate);
            GregorianCalendar date = CalExt.dateFromDescriptionWithMediumDate(selectedAnchorDate);

            // Compute sample start & end using BudgetsRowAdapter period math
            String prevAnchor = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
            Prefs.setPref(Prefs.BUDGETSTARTDATE, selectedAnchorDate);
            GregorianCalendar sampleStart = BudgetsRowAdapter.startOfPeriod(now, selectedPeriod);
            GregorianCalendar sampleEnd = computeEndOfPeriod(sampleStart, selectedPeriod, selectedAnchorDate);
            Prefs.setPref(Prefs.BUDGETSTARTDATE, prevAnchor);

            String sampleRange = CalExt.descriptionWithMediumDate(sampleStart) + " – " + CalExt.descriptionWithMediumDate(sampleEnd);

            switch (selectedPeriod) {
                case Enums.kBudgetPeriodMonth -> {
                    int startDay = date.get(Calendar.DAY_OF_MONTH);
                    String startSuffix = getDayOfMonthSuffix(startDay);
                    int endDay = (startDay == 1) ? CalExt.endOfMonth(now).get(Calendar.DAY_OF_MONTH) : startDay - 1;
                    String endSuffix = getDayOfMonthSuffix(endDay);
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_MONTH, startDay, startSuffix, endDay, endSuffix, sampleRange));
                }
                case Enums.kBudgetPeriodWeek -> {
                    String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date.getTime());
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_WEEK, dayOfWeek, sampleRange));
                }
                case Enums.kBudgetPeriodBiweekly -> {
                    String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date.getTime());
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_BIWEEKLY, dayOfWeek, sampleRange));
                }
                case Enums.kBudgetPeriod4Weeks -> {
                    String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date.getTime());
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_4WEEKS, dayOfWeek, sampleRange));
                }
                case Enums.kBudgetPeriodQuarter -> {
                    String dateDesc = new SimpleDateFormat("d MMMM", Locale.getDefault()).format(date.getTime());
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_QUARTER, dateDesc, sampleRange));
                }
                case Enums.kBudgetPeriodHalfYear -> {
                    String dateDesc = new SimpleDateFormat("d MMMM", Locale.getDefault()).format(date.getTime());
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_HALFYEAR, dateDesc, sampleRange));
                }
                case Enums.kBudgetPeriodYear -> {
                    String dateDesc = new SimpleDateFormat("d MMMM", Locale.getDefault()).format(date.getTime());
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_YEAR, dateDesc, sampleRange));
                }
                case Enums.kBudgetPeriodBimonthly -> {
                    String dateDesc = new SimpleDateFormat("d MMMM", Locale.getDefault()).format(date.getTime());
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_BIMONTHLY, dateDesc, sampleRange));
                }
                default -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CUSTOM_DAILY, CalExt.descriptionWithMediumDate(now)));
            }
        } else {
            // Calendar Default
            String prevAnchor = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
            Prefs.setPref(Prefs.BUDGETSTARTDATE, SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT));
            GregorianCalendar sampleStart = BudgetsRowAdapter.startOfPeriod(now, selectedPeriod);
            GregorianCalendar sampleEnd = computeEndOfPeriod(sampleStart, selectedPeriod, SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT));
            Prefs.setPref(Prefs.BUDGETSTARTDATE, prevAnchor);

            String sampleRange = CalExt.descriptionWithMediumDate(sampleStart) + " – " + CalExt.descriptionWithMediumDate(sampleEnd);

            switch (selectedPeriod) {
                case Enums.kBudgetPeriodMonth -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_MONTH, sampleRange));
                case Enums.kBudgetPeriodQuarter -> {
                    int quarter = (sampleStart.get(Calendar.MONTH) / 3) + 1;
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_QUARTER, "Q" + quarter + ": " + sampleRange));
                }
                case Enums.kBudgetPeriodHalfYear -> {
                    int half = (sampleStart.get(Calendar.MONTH) < 6) ? 1 : 2;
                    explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_HALFYEAR, "H" + half + ": " + sampleRange));
                }
                case Enums.kBudgetPeriodYear -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_YEAR, sampleRange));
                case Enums.kBudgetPeriodWeek -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_WEEK, sampleRange));
                case Enums.kBudgetPeriodBiweekly -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_BIWEEKLY, sampleRange));
                case Enums.kBudgetPeriod4Weeks -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_4WEEKS, sampleRange));
                case Enums.kBudgetPeriodBimonthly -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_BIMONTHLY, sampleRange));
                default -> explanationText.setText(getString(R.string.BUDGET_EXPLANATION_CALENDAR_DAILY, CalExt.descriptionWithMediumDate(now)));
            }
        }
    }

    private GregorianCalendar computeEndOfPeriod(GregorianCalendar start, int budgetPeriod, String anchorDateString) {
        GregorianCalendar budgetStartDate = CalExt.dateFromDescriptionWithMediumDate(anchorDateString);
        boolean firstOfMonth = anchorDateString.equalsIgnoreCase(SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT)) || budgetStartDate.get(Calendar.DAY_OF_MONTH) == 1;
        boolean endOfMonth = !anchorDateString.equalsIgnoreCase(SMMoney.getAppContext().getString(R.string.GENERAL_DEFAULT)) && budgetStartDate.get(Calendar.DAY_OF_MONTH) == CalExt.endOfMonth(budgetStartDate).get(Calendar.DAY_OF_MONTH);

        return switch (budgetPeriod) {
            case Enums.kBudgetPeriodDay -> CalExt.endOfDay(start);
            case Enums.kBudgetPeriodWeek -> CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(start), 1));
            case Enums.kBudgetPeriodBiweekly -> CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(start), 2));
            case Enums.kBudgetPeriod4Weeks -> CalExt.endOfDay(CalExt.addWeeks(CalExt.subtractDay(start), 4));
            case Enums.kBudgetPeriodMonth -> {
                if (firstOfMonth) yield CalExt.endOfMonth(start);
                GregorianCalendar end = CalExt.addMonth(CalExt.subtractDay(start));
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodBimonthly -> {
                if (firstOfMonth) yield CalExt.endOfMonth(CalExt.addMonth(start));
                GregorianCalendar end = CalExt.addMonths(CalExt.subtractDay(start), 2);
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodQuarter -> {
                if (firstOfMonth) yield CalExt.endOfMonth(CalExt.addMonths(start, 2));
                GregorianCalendar end = CalExt.addMonths(CalExt.subtractDay(start), 3);
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodHalfYear -> {
                if (firstOfMonth) yield CalExt.endOfMonth(CalExt.addMonths(start, 5));
                GregorianCalendar end = CalExt.addMonths(CalExt.subtractDay(start), 6);
                yield endOfMonth ? CalExt.endOfMonth(end) : end;
            }
            case Enums.kBudgetPeriodYear -> CalExt.endOfDay(CalExt.addYear(CalExt.subtractDay(start)));
            default -> start;
        };
    }

    private String getDayOfMonthSuffix(int n) {
        if (n >= 11 && n <= 13) return "th";
        return switch (n % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}

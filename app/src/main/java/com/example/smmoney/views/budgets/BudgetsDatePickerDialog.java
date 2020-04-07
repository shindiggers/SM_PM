package com.example.smmoney.views.budgets;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.example.smmoney.misc.PocketMoneyThemes;

import java.util.Calendar;

// Custom DatePicker DialogFragment. Can be called from any activity
public class BudgetsDatePickerDialog extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle dateInt = this.getArguments();
        long currDate = 0;
        //check that a dateInt was passed in by the calling activity
        if (dateInt != null) {
            // set currDate to the time in milliseconds as passed in be the calling activity
            currDate = dateInt.getLong("dateInt");
        }

        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        if (currDate != 0) {
            // if a date was passed in by the caller, set the date to the passed in date
            c.setTimeInMillis(currDate);
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(requireActivity(), PocketMoneyThemes.datePickerTheme(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
    }
}

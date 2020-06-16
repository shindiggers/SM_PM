package com.example.smmoney.views.budgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.smmoney.R;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;


public class BudgetsPeriodDialog extends AppCompatDialogFragment {

    private BudgetsDialogListner budgetDialogListner;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = new CharSequence[]{Locales.kLOC_REPEATING_FREQUENCY_DAILY, Locales.kLOC_REPEATING_FREQUENCY_WEEKLY, Locales.kLOC_BUDGETS_BIWEEKLY, Locales.kLOC_BUDGETS_4WEEKS, Locales.kLOC_REPEATING_FREQUENCY_MONTHLY, Locales.kLOC_BUDGETS_BIMONTHLY, Locales.kLOC_REPEATING_FREQUENCY_QUARTERLY, Locales.kLOC_BUDGETS_HALFYEAR, Locales.kLOC_REPEATING_FREQUENCY_YEARLY};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        builder.setTitle(Locales.kLOC_BUDGETS_PERIOD)
                .setSingleChoiceItems(items, Prefs.getIntPref(Prefs.DISPLAY_BUDGETPERIOD), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int periodType = -1;
                        switch (which) {
                            case 0 /*0*/:
                                periodType = Enums.kBudgetPeriodDay;
                                break;
                            case 1 /*1*/:
                                periodType = Enums.kBudgetPeriodWeek;
                                break;
                            case 2 /*2*/:
                                periodType = Enums.kBudgetPeriodBiweekly;
                                break;
                            case 3 /*3*/:
                                periodType = Enums.kBudgetPeriod4Weeks;
                                break;
                            case 4 /*4*/:
                                periodType = Enums.kBudgetPeriodMonth;
                                break;
                            case 5 /*5*/:
                                periodType = Enums.kBudgetPeriodBimonthly;
                                break;
                            case 6 /*6*/:
                                periodType = Enums.kBudgetPeriodQuarter;
                                break;
                            case 7 /*7*/:
                                periodType = Enums.kBudgetPeriodHalfYear;
                                break;
                            case 8 /*8*/:
                                periodType = Enums.kBudgetPeriodYear;
                                break;
                        }
                        Prefs.setPref(Prefs.DISPLAY_BUDGETPERIOD, periodType);
                        budgetDialogListner.applyPeriodType2(periodType);

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            budgetDialogListner = (BudgetsDialogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ReportDialogListner");
        }
    }

    public interface BudgetsDialogListner {
        void applyPeriodType2(int periodType);
    }

}
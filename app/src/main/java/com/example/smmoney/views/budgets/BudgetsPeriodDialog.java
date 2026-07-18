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
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;


public class BudgetsPeriodDialog extends AppCompatDialogFragment {

    private BudgetsDialogListner budgetDialogListner;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = new CharSequence[]{Locales.kLOC_REPEATING_FREQUENCY_DAILY, Locales.kLOC_REPEATING_FREQUENCY_WEEKLY, Locales.kLOC_BUDGETS_BIWEEKLY, Locales.kLOC_BUDGETS_4WEEKS, Locales.kLOC_REPEATING_FREQUENCY_MONTHLY, Locales.kLOC_BUDGETS_BIMONTHLY, Locales.kLOC_REPEATING_FREQUENCY_QUARTERLY, Locales.kLOC_BUDGETS_HALFYEAR, Locales.kLOC_REPEATING_FREQUENCY_YEARLY};

        int currentPref = Prefs.getIntPref(Prefs.DISPLAY_BUDGETPERIOD);
        int initialSelection = switch (currentPref) {
            case Enums.kBudgetPeriodDay -> 0;
            case Enums.kBudgetPeriodWeek -> 1;
            case Enums.kBudgetPeriodBiweekly -> 2;
            case Enums.kBudgetPeriod4Weeks -> 3;
            case Enums.kBudgetPeriodMonth -> 4;
            case Enums.kBudgetPeriodBimonthly -> 5;
            case Enums.kBudgetPeriodQuarter -> 6;
            case Enums.kBudgetPeriodHalfYear -> 7;
            case Enums.kBudgetPeriodYear -> 8;
            default -> 0;
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), PocketMoneyThemes.dialogTheme());
        builder.setTitle(Locales.kLOC_BUDGETS_PERIOD)
                .setSingleChoiceItems(items, initialSelection, (dialog, which) -> {
                    int periodType = switch (which) {
                        case 0 -> Enums.kBudgetPeriodDay;
                        case 1 -> Enums.kBudgetPeriodWeek;
                        case 2 -> Enums.kBudgetPeriodBiweekly;
                        case 3 -> Enums.kBudgetPeriod4Weeks;
                        case 4 -> Enums.kBudgetPeriodMonth;
                        case 5 -> Enums.kBudgetPeriodBimonthly;
                        case 6 -> Enums.kBudgetPeriodQuarter;
                        case 7 -> Enums.kBudgetPeriodHalfYear;
                        case 8 -> Enums.kBudgetPeriodYear;
                        default -> Enums.kBudgetPeriodDay;
                    };
                    Prefs.setPref(Prefs.DISPLAY_BUDGETPERIOD, periodType);
                    budgetDialogListner.applyPeriodType2(periodType);

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            budgetDialogListner = (BudgetsDialogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement ReportDialogListner");
        }
    }

    public interface BudgetsDialogListner {
        void applyPeriodType2(int periodType);
    }

}
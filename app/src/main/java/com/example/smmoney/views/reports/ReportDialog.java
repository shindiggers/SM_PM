package com.example.smmoney.views.reports;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;

public class ReportDialog extends AppCompatDialogFragment {

    private ReportDialogListner reportDialogListner;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = new CharSequence[]{Locales.kLOC_REPORTS_ONEMONTH, Locales.kLOC_REPORTS_TWOMONTHS, Locales.kLOC_REPORTS_THREEMONTHS, Locales.kLOC_REPORTS_SIXMONTHS, Locales.kLOC_REPORTS_ONEYEAR, Locales.kLOC_PREFERENCES_SHOW_ALL};

        int theme = PocketMoneyThemes.dialogTheme();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), theme);
        builder.setTitle(Locales.kLOC_BUDGETS_PERIOD)
                .setSingleChoiceItems(items, Prefs.getIntPref(Prefs.REPORTS_PERIOD), (dialog, which) -> {
                    int periodType = switch (which) {
                        case Enums.kReportPeriodOneMonth /*0*/ -> Enums.kReportPeriodOneMonth;
                        case Enums.kReportPeriodTwoMonths /*1*/ -> Enums.kReportPeriodTwoMonths;
                        case Enums.kReportPeriodThreeMonths /*2*/ -> Enums.kReportPeriodThreeMonths;
                        case Enums.kReportPeriodSixMonths /*3*/ -> Enums.kReportPeriodSixMonths;
                        case Enums.kReportPeriodOneYear /*4*/ -> Enums.kReportPeriodOneYear;
                        default -> Enums.kReportPeriodAll; /*5*/
                    };

                    Prefs.setPref(Prefs.REPORTS_PERIOD, periodType);
                    reportDialogListner.applyPeriodType(periodType);

                    dialog.dismiss();


                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            reportDialogListner = (ReportDialogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement ReportDialogListner");
        }
    }

    public interface ReportDialogListner {
        void applyPeriodType(int periodType);
    }
}

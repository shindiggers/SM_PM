package com.example.smmoney.views.reports;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class ReportsViewOptionsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference chartTypePref;
    private ListPreference sortDirectionPref;
    private ListPreference sortOnPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.reports_view_options);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
        updateSummaries();
    }

    private void updateSummaries() {
        this.chartTypePref.setSummary(chartTypeString());
        this.sortDirectionPref.setSummary(sortDirectionString());
        this.sortOnPref.setSummary(sortOnString());
    }

    private String chartTypeString() {
        return switch (Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE)) {
            case Enums.kReportsChartTypeNone /*0*/ -> SMMoney.getAppContext().getString(R.string.GENERAL_NONE);
            case Enums.kReportsChartTypeBar /*2*/ -> "Bar Chart";
            default -> "Pie Chart";
        };
    }

    private String sortOnString() {
        return switch (Prefs.getIntPref(Prefs.REPORTS_SORTON)) {
            case Enums.kReportsSortOnItem /*0*/ -> SMMoney.getAppContext().getString(R.string.REPORTDISPLAY_ITEM);
            case Enums.kReportsSortOnAmount /*1*/ -> SMMoney.getAppContext().getString(R.string.GENERAL_AMOUNT);
            default -> SMMoney.getAppContext().getString(R.string.REPORTDISPLAY_COUNT);
        };
    }

    private String sortDirectionString() {
        if (Prefs.getIntPref(Prefs.PREFS_REPORTS_SORTDIRECTION) == Enums.ReportsSortDirectionAscending /*0*/) {
            return SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_ASCENDING);
        }
        return SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_DESCENDING);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        this.chartTypePref = fragment.findPreference("chartreportsoptions");
        this.sortDirectionPref = fragment.findPreference("directionreportsoptions");
        this.sortOnPref = fragment.findPreference("sortonreportsoptions");
        if (SMMoney.isLiteVersion()) {
            Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypeNone /*0*/);
            this.chartTypePref.setEnabled(false);
        }
        String[] theStrings = new String[]{SMMoney.getAppContext().getString(R.string.GENERAL_NONE), "Pie Chart", "Bar Chart"};
        this.chartTypePref.setEntries(theStrings);
        this.chartTypePref.setEntryValues(theStrings);
        this.chartTypePref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue.equals(SMMoney.getAppContext().getString(R.string.GENERAL_NONE))) {
                Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypeNone /*0*/);
            } else if (newValue.equals("Pie Chart")) {
                Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypePie /*1*/);
            } else if (newValue.equals("Bar Chart")) {
                Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypeBar /*2*/);
            }
            preference.setSummary((String) newValue);
            return true;
        });
        String[] theStrings2 = new String[]{SMMoney.getAppContext().getString(R.string.REPORTDISPLAY_ITEM), SMMoney.getAppContext().getString(R.string.GENERAL_AMOUNT), SMMoney.getAppContext().getString(R.string.REPORTDISPLAY_COUNT)};
        this.sortOnPref.setEntries(theStrings2);
        this.sortOnPref.setEntryValues(theStrings2);
        this.sortOnPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue.equals(SMMoney.getAppContext().getString(R.string.REPORTDISPLAY_ITEM))) {
                Prefs.setPref(Prefs.REPORTS_SORTON, Enums.kReportsSortOnItem /*0*/);
            } else if (newValue.equals(SMMoney.getAppContext().getString(R.string.GENERAL_AMOUNT))) {
                Prefs.setPref(Prefs.REPORTS_SORTON, Enums.kReportsSortOnAmount /*1*/);
            } else if (newValue.equals(SMMoney.getAppContext().getString(R.string.REPORTDISPLAY_COUNT))) {
                Prefs.setPref(Prefs.REPORTS_SORTON, Enums.kReportsSortOnCount /*2*/);
            }
            preference.setSummary((String) newValue);
            return true;
        });
        String[] theStrings3 = new String[]{SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_ASCENDING), SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_DESCENDING)};
        this.sortDirectionPref.setEntries(theStrings3);
        this.sortDirectionPref.setEntryValues(theStrings3);
        this.sortDirectionPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue.equals(SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_ASCENDING))) {
                Prefs.setPref(Prefs.PREFS_REPORTS_SORTDIRECTION, Enums.ReportsSortDirectionAscending /*0*/);
            } else if (newValue.equals(SMMoney.getAppContext().getString(R.string.TRANSACTIONS_OPTIONS_DESCENDING))) {
                Prefs.setPref(Prefs.PREFS_REPORTS_SORTDIRECTION, Enums.ReportsSortDirectionDescending /*1*/);
            }
            preference.setSummary((String) newValue);
            return true;
        });
    }
}

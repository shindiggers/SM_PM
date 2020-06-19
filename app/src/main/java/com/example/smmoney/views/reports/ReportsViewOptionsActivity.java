package com.example.smmoney.views.reports;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class ReportsViewOptionsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference chartTypePref;
    @SuppressWarnings("FieldCanBeLocal")
    private Context context;
    private ListPreference sortDirectionPref;
    private ListPreference sortOnPref;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.reports_view_options);
        this.context = this;
        setupPrefs();
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    protected void onStart() {
        super.onStart();
        this.chartTypePref.setSummary(chartTypeString());
        this.sortDirectionPref.setSummary(sortDirectionString());
        this.sortOnPref.setSummary(sortOnString());
    }

    private String chartTypeString() {
        switch (Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE)) {
            case Enums.kReportsChartTypeNone /*0*/:
                return Locales.kLOC_GENERAL_NONE;
            case Enums.kReportsChartTypeBar /*2*/:
                return "Bar Chart";
            default:
                return "Pie Chart";
        }
    }

    private String sortOnString() {
        switch (Prefs.getIntPref(Prefs.REPORTS_SORTON)) {
            case Enums.kReportsSortOnItem /*0*/:
                return Locales.kLOC_REPORTDISPLAY_ITEM;
            case Enums.kReportsSortOnAmount /*1*/:
                return Locales.kLOC_GENERAL_AMOUNT;
            default:
                return Locales.kLOC_REPORTDISPLAY_COUNT;
        }
    }

    private String sortDirectionString() {
        if (Prefs.getIntPref(Prefs.PREFS_REPORTS_SORTDIRECTION) == Enums.ReportsSortDirectionAscending /*0*/) {
            return Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING;
        }
        return Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING;
    }

    private void setupPrefs() {
        this.chartTypePref = (ListPreference) findPreference("chartreportsoptions");
        this.sortDirectionPref = (ListPreference) findPreference("directionreportsoptions");
        this.sortOnPref = (ListPreference) findPreference("sortonreportsoptions");
        if (SMMoney.isLiteVersion()) {
            Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypeNone /*0*/);
            this.chartTypePref.setEnabled(false);
        }
        String[] theStrings = new String[]{Locales.kLOC_GENERAL_NONE, "Pie Chart", "Bar Chart"};
        this.chartTypePref.setEntries(theStrings);
        this.chartTypePref.setEntryValues(theStrings);
        this.chartTypePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(Locales.kLOC_GENERAL_NONE)) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypeNone /*0*/);
                } else if (newValue.equals("Pie Chart")) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypePie /*1*/);
                } else if (newValue.equals("Bar Chart")) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, Enums.kReportsChartTypeBar /*2*/);
                }
                preference.setSummary((String) newValue);
                return true;
            }
        });
        String[] theStrings2 = new String[]{Locales.kLOC_REPORTDISPLAY_ITEM, Locales.kLOC_GENERAL_AMOUNT, Locales.kLOC_REPORTDISPLAY_COUNT};
        this.sortOnPref.setEntries(theStrings2);
        this.sortOnPref.setEntryValues(theStrings2);
        this.sortOnPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(Locales.kLOC_REPORTDISPLAY_ITEM)) {
                    Prefs.setPref(Prefs.REPORTS_SORTON, Enums.kReportsSortOnItem /*0*/);
                } else if (newValue.equals(Locales.kLOC_GENERAL_AMOUNT)) {
                    Prefs.setPref(Prefs.REPORTS_SORTON, Enums.kReportsSortOnAmount /*1*/);
                } else if (newValue.equals(Locales.kLOC_REPORTDISPLAY_COUNT)) {
                    Prefs.setPref(Prefs.REPORTS_SORTON, Enums.kReportsSortOnCount /*2*/);
                }
                preference.setSummary((String) newValue);
                return true;
            }
        });
        String[] theStrings3 = new String[]{Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING, Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING};
        this.sortDirectionPref.setEntries(theStrings3);
        this.sortDirectionPref.setEntryValues(theStrings3);
        this.sortDirectionPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING)) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_SORTDIRECTION, Enums.ReportsSortDirectionAscending /*0*/);
                } else if (newValue.equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING)) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_SORTDIRECTION, Enums.ReportsSortDirectionDescending /*1*/);
                }
                preference.setSummary((String) newValue);
                return true;
            }
        });
    }
}

package com.catamount.pocketmoney.views.reports;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import com.catamount.pocketmoney.PocketMoney;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.PocketMoneyPreferenceActivity;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;

public class ReportsViewOptionsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference chartTypePref;
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

    public String chartTypeString() {
        switch (Prefs.getIntPref(Prefs.PREFS_REPORTS_CHARTTYPE)) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return Locales.kLOC_GENERAL_NONE;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                return "Bar Chart";
            default:
                return "Pie Chart";
        }
    }

    public String sortOnString() {
        switch (Prefs.getIntPref(Prefs.REPORTS_SORTON)) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return Locales.kLOC_REPORTDISPLAY_ITEM;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return Locales.kLOC_GENERAL_AMOUNT;
            default:
                return Locales.kLOC_REPORTDISPLAY_COUNT;
        }
    }

    public String sortDirectionString() {
        switch (Prefs.getIntPref(Prefs.PREFS_REPORTS_SORTDIRECTION)) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING;
            default:
                return Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING;
        }
    }

    public void setupPrefs() {
        this.chartTypePref = (ListPreference) findPreference("chartreportsoptions");
        this.sortDirectionPref = (ListPreference) findPreference("directionreportsoptions");
        this.sortOnPref = (ListPreference) findPreference("sortonreportsoptions");
        if (PocketMoney.isLiteVersion()) {
            Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, 0);
            this.chartTypePref.setEnabled(false);
        }
        String[] theStrings = new String[]{Locales.kLOC_GENERAL_NONE, "Pie Chart", "Bar Chart"};
        this.chartTypePref.setEntries(theStrings);
        this.chartTypePref.setEntryValues(theStrings);
        this.chartTypePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(Locales.kLOC_GENERAL_NONE)) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, 0);
                } else if (newValue.equals("Pie Chart")) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, 1);
                } else if (newValue.equals("Bar Chart")) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_CHARTTYPE, 2);
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
                    Prefs.setPref(Prefs.REPORTS_SORTON, 0);
                } else if (newValue.equals(Locales.kLOC_GENERAL_AMOUNT)) {
                    Prefs.setPref(Prefs.REPORTS_SORTON, 1);
                } else if (newValue.equals(Locales.kLOC_REPORTDISPLAY_COUNT)) {
                    Prefs.setPref(Prefs.REPORTS_SORTON, 2);
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
                    Prefs.setPref(Prefs.PREFS_REPORTS_SORTDIRECTION, 0);
                } else if (newValue.equals(Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING)) {
                    Prefs.setPref(Prefs.PREFS_REPORTS_SORTDIRECTION, 1);
                }
                preference.setSummary((String) newValue);
                return true;
            }
        });
    }
}

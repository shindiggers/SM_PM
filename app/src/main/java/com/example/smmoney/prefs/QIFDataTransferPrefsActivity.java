package com.example.smmoney.prefs;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import com.example.smmoney.R;
import com.example.smmoney.importexport.ImportExportQIF;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class QIFDataTransferPrefsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference dateFormatListPref;
    private ListPreference dateSeparatorListPref;
    private ListPreference numberFormatListPref;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_datatransfers_qifoptions);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        setupPrefs();
    }

    protected void onStart() {
        super.onStart();
        this.dateFormatListPref.setSummary(this.dateFormatListPref.getValue());
        this.dateSeparatorListPref.setSummary(this.dateSeparatorListPref.getValue());
        this.numberFormatListPref.setSummary(this.numberFormatListPref.getValue());
    }

    private void setupPrefs() {
        this.dateFormatListPref = (ListPreference) findPreference(Prefs.QIF_DATEFORMAT);
        this.dateSeparatorListPref = (ListPreference) findPreference(Prefs.QIF_DATESEPARATOR);
        this.numberFormatListPref = (ListPreference) findPreference(Prefs.QIF_NUMBERFORMAT);
        this.dateFormatListPref.setEntries(ImportExportQIF.dateFormats());
        this.dateFormatListPref.setEntryValues(ImportExportQIF.dateFormats());
        this.dateSeparatorListPref.setEntries(ImportExportQIF.dateSeparators());
        this.dateSeparatorListPref.setEntryValues(ImportExportQIF.dateSeparators());
        this.numberFormatListPref.setEntries(ImportExportQIF.numberFormats());
        this.numberFormatListPref.setEntryValues(ImportExportQIF.numberFormats());
        this.dateFormatListPref.setOnPreferenceChangeListener(getChangeListener());
        this.dateSeparatorListPref.setOnPreferenceChangeListener(getChangeListener());
        this.numberFormatListPref.setOnPreferenceChangeListener(getChangeListener());
    }

    public OnPreferenceChangeListener getChangeListener() {
        return new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        };
    }
}

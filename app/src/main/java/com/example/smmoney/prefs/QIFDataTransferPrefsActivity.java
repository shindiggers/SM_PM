package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.importexport.ImportExportQIF;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class QIFDataTransferPrefsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference dateFormatListPref;
    private ListPreference dateSeparatorListPref;
    private ListPreference numberFormatListPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_datatransfers_qifoptions);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        this.dateFormatListPref = fragment.findPreference(Prefs.QIF_DATEFORMAT);
        this.dateSeparatorListPref = fragment.findPreference(Prefs.QIF_DATESEPARATOR);
        this.numberFormatListPref = fragment.findPreference(Prefs.QIF_NUMBERFORMAT);
        this.dateFormatListPref.setEntries(ImportExportQIF.dateFormats());
        this.dateFormatListPref.setEntryValues(ImportExportQIF.dateFormats());
        this.dateSeparatorListPref.setEntries(ImportExportQIF.dateSeparators());
        this.dateSeparatorListPref.setEntryValues(ImportExportQIF.dateSeparators());
        this.numberFormatListPref.setEntries(ImportExportQIF.numberFormats());
        this.numberFormatListPref.setEntryValues(ImportExportQIF.numberFormats());
        this.dateFormatListPref.setOnPreferenceChangeListener(getChangeListener());
        this.dateSeparatorListPref.setOnPreferenceChangeListener(getChangeListener());
        this.numberFormatListPref.setOnPreferenceChangeListener(getChangeListener());

        this.dateFormatListPref.setSummary(this.dateFormatListPref.getValue());
        this.dateSeparatorListPref.setSummary(this.dateSeparatorListPref.getValue());
        this.numberFormatListPref.setSummary(this.numberFormatListPref.getValue());
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return (preference, newValue) -> {
            preference.setSummary((String) newValue);
            return true;
        };
    }
}

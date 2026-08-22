package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.importexport.ImportExportQIF;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class QIFDataTransferPrefsActivity extends PocketMoneyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(Locales.kLOC_QIFOPTIONS_TITLE);
        loadParentFragment(R.xml.prefs_datatransfers_qifoptions);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        ListPreference dateFormatListPref = fragment.findPreference(Prefs.QIF_DATEFORMAT);
        ListPreference dateSeparatorListPref = fragment.findPreference(Prefs.QIF_DATESEPARATOR);
        ListPreference numberFormatListPref = fragment.findPreference(Prefs.QIF_NUMBERFORMAT);

        if (dateFormatListPref != null) {
            dateFormatListPref.setEntries(ImportExportQIF.dateFormats());
            dateFormatListPref.setEntryValues(ImportExportQIF.dateFormats());
            dateFormatListPref.setOnPreferenceChangeListener(getChangeListener());
            dateFormatListPref.setSummary(dateFormatListPref.getValue());
        }

        if (dateSeparatorListPref != null) {
            dateSeparatorListPref.setEntries(ImportExportQIF.dateSeparators());
            dateSeparatorListPref.setEntryValues(ImportExportQIF.dateSeparators());
            dateSeparatorListPref.setOnPreferenceChangeListener(getChangeListener());
            dateSeparatorListPref.setSummary(dateSeparatorListPref.getValue());
        }

        if (numberFormatListPref != null) {
            numberFormatListPref.setEntries(ImportExportQIF.numberFormats());
            numberFormatListPref.setEntryValues(ImportExportQIF.numberFormats());
            numberFormatListPref.setOnPreferenceChangeListener(getChangeListener());
            numberFormatListPref.setSummary(numberFormatListPref.getValue());
        }
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return (preference, newValue) -> {
            preference.setSummary((String) newValue);
            return true;
        };
    }
}

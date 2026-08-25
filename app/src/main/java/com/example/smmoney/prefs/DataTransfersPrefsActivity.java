package com.example.smmoney.prefs;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class DataTransfersPrefsActivity extends PocketMoneyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(Locales.kLOC_PREFS_DATATRANFER);
        loadParentFragment(R.xml.prefs_datatransfers);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        ListPreference transferModePref = fragment.findPreference(Prefs.TRANSFERTYPE);
        ListPreference fileEncodingPref = fragment.findPreference(Prefs.ENCODING);
        ListPreference storageDevicePref = fragment.findPreference(Prefs.EXPORT_STOREDEVICE);
        Preference qifOptionsPref = fragment.findPreference("prefsdatatransfersqifoptions");

        if (transferModePref != null) {
            String[] theValues = new String[]{"0"};
            transferModePref.setEntries(new String[]{"Download/PocketMoneyBackup"});
            transferModePref.setEntryValues(theValues);
            transferModePref.setOnPreferenceChangeListener(getChangeListener());
            transferModePref.setSummary(transferModePref.getEntry());
        }

        if (fileEncodingPref != null) {
            String[] encodingValues = new String[]{"UTF-8", "UTF-16", "ISO-8859-1"};
            fileEncodingPref.setEntries(new String[]{"Unicode (UTF-8)", "Unicode (UTF-16)", "Western (ISO Latin 1)"});
            fileEncodingPref.setEntryValues(encodingValues);
            fileEncodingPref.setOnPreferenceChangeListener(getChangeListener());
            fileEncodingPref.setSummary(fileEncodingPref.getEntry());
        }

        if (storageDevicePref != null) {
            String[] deviceStrings = new String[]{"Internal Storage"};
            storageDevicePref.setEntries(deviceStrings);
            storageDevicePref.setEntryValues(deviceStrings);
            storageDevicePref.setOnPreferenceChangeListener(getChangeListener());
            storageDevicePref.setSummary(storageDevicePref.getEntry());
        }

        if (qifOptionsPref != null) {
            qifOptionsPref.setOnPreferenceClickListener(preference -> {
                DataTransfersPrefsActivity.this.startActivity(new Intent(DataTransfersPrefsActivity.this, QIFDataTransferPrefsActivity.class));
                return true;
            });
        }
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return (preference, newValue) -> {
            if (preference instanceof ListPreference listPreference) {
                int index = listPreference.findIndexOfValue((String) newValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }
            return true;
        };
    }
}

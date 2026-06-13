package com.example.smmoney.prefs;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class DataTransfersPrefsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference fileEncodingPref;
    private ListPreference storageDevicePref;
    private ListPreference transferModePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_datatransfers);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        this.transferModePref = fragment.findPreference(Prefs.TRANSFERTYPE);
        this.fileEncodingPref = fragment.findPreference(Prefs.ENCODING);
        Preference qifOptionsPref = fragment.findPreference("prefsdatatransfersqifoptions");
        Preference emailPartnerOptionsPref = fragment.findPreference("datatransferemailprefs");
        this.storageDevicePref = fragment.findPreference(Prefs.EXPORT_STOREDEVICE);
        String[] theValues = new String[]{"0"};
        this.transferModePref.setEntries(new String[]{"Download/PocketMoneyBackup"});
        this.transferModePref.setEntryValues(theValues);
        String[] encodingValues = new String[]{"UTF-8", "UTF-16", "ISO-8859-1"};
        this.fileEncodingPref.setEntries(new String[]{"Unicode (UTF-8)", "Unicode (UTF-16)", "Western (ISO Latin 1)"});
        this.fileEncodingPref.setEntryValues(encodingValues);
        String[] deviceStrings = new String[]{"Internal Storage"};
        this.storageDevicePref.setEntries(deviceStrings);
        this.storageDevicePref.setEntryValues(deviceStrings);
        this.transferModePref.setOnPreferenceChangeListener(getChangeListener());
        this.fileEncodingPref.setOnPreferenceChangeListener(getChangeListener());
        this.storageDevicePref.setOnPreferenceChangeListener(getChangeListener());

        this.transferModePref.setSummary(this.transferModePref.getEntry());
        this.fileEncodingPref.setSummary(this.fileEncodingPref.getEntry());
        this.storageDevicePref.setSummary(this.storageDevicePref.getEntry());

        qifOptionsPref.setOnPreferenceClickListener(preference -> {
            DataTransfersPrefsActivity.this.startActivity(new Intent(DataTransfersPrefsActivity.this, QIFDataTransferPrefsActivity.class));
            return true;
        });
        emailPartnerOptionsPref.setOnPreferenceClickListener(preference -> {
            DataTransfersPrefsActivity.this.startActivity(new Intent(DataTransfersPrefsActivity.this, DataTransfersEmailPrefActivity.class));
            return true;
        });
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

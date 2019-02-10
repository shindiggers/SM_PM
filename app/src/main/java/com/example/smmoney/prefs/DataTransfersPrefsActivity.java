package com.example.smmoney.prefs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class DataTransfersPrefsActivity extends PocketMoneyPreferenceActivity {
    private Context context;
    private Preference emailPartnerOptionsPref;
    private ListPreference fileEncodingPref;
    private Preference qifOptionsPref;
    private ListPreference storageDevicePref;
    private ListPreference transferModePref;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_datatransfers);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.context = this;
        setupPrefs();
    }

    protected void onStart() {
        super.onStart();
        this.transferModePref.setSummary(this.transferModePref.getEntry());
        this.fileEncodingPref.setSummary(this.fileEncodingPref.getEntry());
        this.storageDevicePref.setSummary(this.storageDevicePref.getEntry());
    }

    public void setupPrefs() {
        this.transferModePref = (ListPreference) findPreference(Prefs.TRANSFERTYPE);
        this.fileEncodingPref = (ListPreference) findPreference(Prefs.ENCODING);
        this.qifOptionsPref = findPreference("prefsdatatransfersqifoptions");
        this.emailPartnerOptionsPref = findPreference("datatransferemailprefs");
        this.storageDevicePref = (ListPreference) findPreference(Prefs.EXPORT_STOREDEVICE);
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
        this.qifOptionsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DataTransfersPrefsActivity.this.context.startActivity(new Intent(DataTransfersPrefsActivity.this.context, QIFDataTransferPrefsActivity.class));
                return true;
            }
        });
        this.emailPartnerOptionsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DataTransfersPrefsActivity.this.context.startActivity(new Intent(DataTransfersPrefsActivity.this.context, DataTransfersEmailPrefActivity.class));
                return true;
            }
        });
    }

    public OnPreferenceChangeListener getChangeListener() {
        return new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(((ListPreference) preference).getEntries()[((ListPreference) preference).findIndexOfValue((String) newValue)]);
                return true;
            }
        };
    }
}

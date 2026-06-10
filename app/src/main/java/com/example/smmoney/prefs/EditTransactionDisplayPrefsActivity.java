package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class EditTransactionDisplayPrefsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference startEditingListPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_display_edit_transaction);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        this.startEditingListPref = fragment.findPreference(Prefs.EDITTRANSACTION_STARTING_FIELD);
        String[] startPositions = new String[]{Locales.kLOC_GENERAL_NONE, Locales.kLOC_GENERAL_PAYEE, Locales.kLOC_GENERAL_CATEGORY, Locales.kLOC_GENERAL_AMOUNT};
        this.startEditingListPref.setEntries(startPositions);
        this.startEditingListPref.setEntryValues(startPositions);
        this.startEditingListPref.setOnPreferenceChangeListener(getChangeListener());
        this.startEditingListPref.setSummary(this.startEditingListPref.getValue());
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        };
    }
}

package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class EditTransactionDisplayPrefsActivity extends PocketMoneyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(SMMoney.getAppContext().getString(R.string.EDIT_TRANSACTION_TITLE));
        loadParentFragment(R.xml.prefs_display_edit_transaction);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        ListPreference startEditingListPref = fragment.findPreference(Prefs.EDITTRANSACTION_STARTING_FIELD);
        if (startEditingListPref != null) {
            String[] startPositions = new String[]{
                    SMMoney.getAppContext().getString(R.string.GENERAL_NONE),
                    SMMoney.getAppContext().getString(R.string.GENERAL_PAYEE),
                    SMMoney.getAppContext().getString(R.string.GENERAL_CATEGORY),
                    SMMoney.getAppContext().getString(R.string.GENERAL_AMOUNT)};
            startEditingListPref.setEntries(startPositions);
            startEditingListPref.setEntryValues(startPositions);
            startEditingListPref.setOnPreferenceChangeListener(getChangeListener());
            startEditingListPref.setSummary(startEditingListPref.getValue());
        }
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return (preference, newValue) -> {
            preference.setSummary((String) newValue);
            return true;
        };
    }
}

package com.example.smmoney.views.transactions;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivityV2;

public class TransactionViewOptionsActivity extends PocketMoneyPreferenceActivityV2 {
    private ListPreference sortOnListPref;
    private ListPreference sortOrderPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.transaction_view_options);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
        updateSummaries();
    }

    private void updateSummaries() {
        this.sortOrderPref.setSummary(this.sortOrderPref.getValue());
        this.sortOnListPref.setSummary(this.sortOnListPref.getValue());
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        this.sortOnListPref = fragment.findPreference(Prefs.TRANSACTIONS_SORTON);
        this.sortOrderPref = fragment.findPreference(Prefs.NEWESTTRANSACTIONFIRST);
        String[] theStrings = TransactionDB.transactionSortTypes();
        this.sortOnListPref.setEntries(theStrings);
        this.sortOnListPref.setEntryValues(theStrings);
        this.sortOnListPref.setOnPreferenceChangeListener(getChangeListener());
        String[] someStrings = new String[]{Locales.kLOC_TRANSACTIONS_OPTIONS_DESCENDING, Locales.kLOC_TRANSACTIONS_OPTIONS_ASCENDING};
        this.sortOrderPref.setEntries(someStrings);
        this.sortOrderPref.setEntryValues(someStrings);
        this.sortOrderPref.setOnPreferenceChangeListener(getChangeListener());
        if (this.sortOnListPref.getValue() == null || this.sortOnListPref.getValue().length() == 0) {
            this.sortOnListPref.setDefaultValue(theStrings[0]);
        }
        if (this.sortOrderPref.getValue() == null || this.sortOrderPref.getValue().length() == 0) {
            this.sortOrderPref.setDefaultValue(someStrings[0]);
        }
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

package com.catamount.pocketmoney.views.transactions;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.database.TransactionDB;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.PocketMoneyPreferenceActivity;

public class TransactionViewOptionsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference sortOnListPref;
    private ListPreference sortOrderPref;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.transaction_view_options);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        setupPrefs();
    }

    protected void onStart() {
        super.onStart();
        this.sortOrderPref.setSummary(this.sortOrderPref.getValue());
        this.sortOnListPref.setSummary(this.sortOnListPref.getValue());
    }

    public void setupPrefs() {
        this.sortOnListPref = (ListPreference) findPreference(Prefs.TRANSACTIONS_SORTON);
        this.sortOrderPref = (ListPreference) findPreference(Prefs.NEWESTTRANSACTIONFIRST);
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

    public OnPreferenceChangeListener getChangeListener() {
        return new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        };
    }
}

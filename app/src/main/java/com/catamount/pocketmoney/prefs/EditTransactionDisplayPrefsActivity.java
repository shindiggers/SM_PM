package com.catamount.pocketmoney.prefs;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.PocketMoneyPreferenceActivity;

public class EditTransactionDisplayPrefsActivity extends PocketMoneyPreferenceActivity {
    ListPreference categoryPositionListPref;
    ListPreference startEditingListPref;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_display_edit_transaction);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        setupPrefs();
    }

    protected void onResume() {
        super.onResume();
        this.startEditingListPref.setSummary(this.startEditingListPref.getValue());
    }

    public void setupPrefs() {
        this.startEditingListPref = (ListPreference) findPreference(Prefs.EDITTRANSACTION_STARTING_FIELD);
        String[] startPositions = new String[]{Locales.kLOC_GENERAL_NONE, Locales.kLOC_GENERAL_PAYEE, Locales.kLOC_GENERAL_CATEGORY, Locales.kLOC_GENERAL_AMOUNT};
        this.startEditingListPref.setEntries(startPositions);
        this.startEditingListPref.setEntryValues(startPositions);
        this.startEditingListPref.setOnPreferenceChangeListener(getChangeListener());
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

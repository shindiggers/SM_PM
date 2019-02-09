package com.catamount.pocketmoney.prefs;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.database.Database;
import com.catamount.pocketmoney.misc.CurrencyExt;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.PocketMoneyPreferenceActivity;
import java.util.ArrayList;
import java.util.Currency;

public class CurrencyPrefsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference listPref;
    private CheckBoxPreference multipleCurrencyPref;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefs_currency);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        setupPrefs();
    }

    protected void onStart() {
        super.onStart();
        this.listPref.setSummary(this.listPref.getValue());
    }

    public void setupPrefs() {
        String[] codeList = CurrencyExt.getCurrencies();
        ArrayList<String> nameList = new ArrayList();
        ArrayList<String> codeNameList = new ArrayList();
        this.listPref = (ListPreference) findPreference(Prefs.HOMECURRENCYCODE);
        this.multipleCurrencyPref = (CheckBoxPreference) findPreference(Prefs.MULTIPLECURRENCIES);
        this.multipleCurrencyPref.setOnPreferenceChangeListener(getChangeListener());
        for (String loc : codeList) {
            try {
                nameList.add(new StringBuilder(String.valueOf(Currency.getInstance(loc).getCurrencyCode())).append(" \ufffd ").append(Currency.getInstance(loc).getSymbol()).toString());
                codeNameList.add(Currency.getInstance(loc).getCurrencyCode());
            } catch (IllegalArgumentException e) {
            }
        }
        this.listPref.setEntries(nameList.toArray(new String[1]));
        this.listPref.setEntryValues(codeNameList.toArray(new String[1]));
        this.listPref.setOnPreferenceChangeListener(getChangeListener());
    }

    public OnPreferenceChangeListener getChangeListener() {
        return new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference.equals(CurrencyPrefsActivity.this.listPref)) {
                    Database.setHomeCurrency((String) newValue);
                    preference.setSummary((String) newValue);
                } else if (preference.equals(CurrencyPrefsActivity.this.multipleCurrencyPref)) {
                    Database.setMultipleCurrencies((Boolean) newValue);
                }
                return true;
            }
        };
    }
}

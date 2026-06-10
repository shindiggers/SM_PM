package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

import java.util.ArrayList;
import java.util.Currency;

public class CurrencyPrefsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference listPref;
    private CheckBoxPreference multipleCurrencyPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_currency);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        String[] codeList = CurrencyExt.getCurrencies();
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> codeNameList = new ArrayList<>();
        this.listPref = fragment.findPreference(Prefs.HOMECURRENCYCODE);
        this.multipleCurrencyPref = fragment.findPreference(Prefs.MULTIPLECURRENCIES);
        this.multipleCurrencyPref.setOnPreferenceChangeListener(getChangeListener());
        for (String loc : codeList) {
            try {
                nameList.add(Currency.getInstance(loc).getCurrencyCode() + " - " + Currency.getInstance(loc).getSymbol());
                codeNameList.add(Currency.getInstance(loc).getCurrencyCode());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        this.listPref.setEntries(nameList.toArray(new String[1]));
        this.listPref.setEntryValues(codeNameList.toArray(new String[1]));
        this.listPref.setOnPreferenceChangeListener(getChangeListener());
        this.listPref.setSummary(this.listPref.getValue());
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return new Preference.OnPreferenceChangeListener() {
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

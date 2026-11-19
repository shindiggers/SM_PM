package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.misc.CurrencyPickerDialog;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class CurrencyPrefsActivity extends PocketMoneyPreferenceActivity {
    private CheckBoxPreference multipleCurrencyPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(SMMoney.getAppContext().getString(R.string.ACCOUNT_CURRENCY_LABEL));
        loadParentFragment(R.xml.prefs_currency);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        Preference homeCurrencyPref = fragment.findPreference(Prefs.HOMECURRENCYCODE);
        this.multipleCurrencyPref = fragment.findPreference(Prefs.MULTIPLECURRENCIES);
        if (this.multipleCurrencyPref != null) {
            this.multipleCurrencyPref.setOnPreferenceChangeListener(getChangeListener());
        }
        if (homeCurrencyPref != null) {
            String currentHomeCurrency = Prefs.getStringPref(Prefs.HOMECURRENCYCODE);
            homeCurrencyPref.setSummary(currentHomeCurrency.trim().isEmpty() ? "USD" : currentHomeCurrency);
            homeCurrencyPref.setOnPreferenceClickListener(preference -> {
                CurrencyPickerDialog.show(getSupportFragmentManager(), (currencyCode, displayText) -> {
                    Prefs.setPref(Prefs.HOMECURRENCYCODE, currencyCode);
                    Database.setHomeCurrency(currencyCode);
                    preference.setSummary(currencyCode);
                });
                return true;
            });
        }
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return (preference, newValue) -> {
            if (preference.equals(CurrencyPrefsActivity.this.multipleCurrencyPref)) {
                Database.setMultipleCurrencies((Boolean) newValue);
            }
            return true;
        };
    }
}

package com.example.smmoney.prefs;

import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.LaunchActivity;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class DisplayOptionsPrefsActivity extends PocketMoneyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(Locales.kLOC_PREFS_VIEWOPTIONS);
        loadParentFragment(R.xml.prefs_display_main);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        Preference accountDisplayPrefs = fragment.findPreference("AccountDisplayPrefs");
        if (accountDisplayPrefs != null) {
            accountDisplayPrefs.setOnPreferenceClickListener(preference -> {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, AccountDisplayPrefsActivity.class));
                return true;
            });
        }

        Preference transactionRegisterDisplayPrefs = fragment.findPreference("TransactionRegisterDisplayPrefs");
        if (transactionRegisterDisplayPrefs != null) {
            transactionRegisterDisplayPrefs.setOnPreferenceClickListener(preference -> {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, TransactionRegisterDisplayPrefsActivity.class));
                return true;
            });
        }

        Preference budgetsPrefs = fragment.findPreference("BudgetsPrefs");
        if (budgetsPrefs != null) {
            budgetsPrefs.setOnPreferenceClickListener(preference -> {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, BudgetsDisplayPrefsActivity.class));
                return true;
            });
        }

        Preference editTransactionPrefs = fragment.findPreference("EditTransactionPrefs");
        if (editTransactionPrefs != null) {
            editTransactionPrefs.setOnPreferenceClickListener(preference -> {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, EditTransactionDisplayPrefsActivity.class));
                return true;
            });
        }

        Preference reportsPrefs = fragment.findPreference("ReportsPrefs");
        if (reportsPrefs != null) {
            reportsPrefs.setOnPreferenceClickListener(preference -> {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, ReportsDisplayPrefsActivity.class));
                return true;
            });
        }

        ListPreference themes = fragment.findPreference(Prefs.THEME_COLOR);
        if (themes != null) {
            String[] colors = new String[]{"Black", "Blue", Locales.kLOC_THEME_COLOR_GREEN, Locales.kLOC_THEME_COLOR_PURPLE, Locales.kLOC_THEME_COLOR_GRAY, Locales.kLOC_THEME_COLOR_COFFEE};
            themes.setEntries(colors);
            themes.setEntryValues(colors);
            themes.setOnPreferenceChangeListener(getChangeListener());
            themes.setSummary(Prefs.getStringPref(Prefs.THEME_COLOR));
        }
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return (preference, newValue) -> {
            String newTheme = (String) newValue;
            preference.setSummary(newTheme);
            
            // Force immediate save and update the theme engine
            Prefs.setPref(Prefs.THEME_COLOR, newTheme);
            PocketMoneyThemes.setTheme(newTheme);

            DisplayOptionsPrefsActivity.this.runOnUiThread(() -> {
                Builder alert = new Builder(DisplayOptionsPrefsActivity.this, PocketMoneyThemes.dialogTheme());
                alert.setTitle(Locales.kLOC_GENERAL_RELAUNCH_APP);
                alert.setMessage(Locales.kLOC_GENERAL_RELAUNCH_APP_THEME);
                alert.setPositiveButton(Locales.kLOC_GENERAL_QUIT, (dialog, whichButton) -> {
                    Intent i = new Intent(DisplayOptionsPrefsActivity.this, LaunchActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    DisplayOptionsPrefsActivity.this.startActivity(i);
                    dialog.dismiss();
                });
                alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, (dialog, whichButton) -> dialog.dismiss());
                alert.show();
            });
            return true;
        };
    }
}

package com.example.smmoney.prefs;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import com.example.smmoney.views.PocketMoneyPreferenceActivityV2;

public class DisplayOptionsPrefsActivity extends PocketMoneyPreferenceActivityV2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_display_main);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        fragment.findPreference("AccountDisplayPrefs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, AccountDisplayPrefsActivity.class));
                return true;
            }
        });
        fragment.findPreference("TransactionRegisterDisplayPrefs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, TransactionRegisterDisplayPrefsActivity.class));
                return true;
            }
        });
        fragment.findPreference("BudgetsPrefs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, BudgetsDisplayPrefsActivity.class));
                return true;
            }
        });
        fragment.findPreference("EditTransactionPrefs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, EditTransactionDisplayPrefsActivity.class));
                return true;
            }
        });
        fragment.findPreference("ReportsPrefs").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.startActivity(new Intent(DisplayOptionsPrefsActivity.this, ReportsDisplayPrefsActivity.class));
                return true;
            }
        });
        ListPreference themes = fragment.findPreference(Prefs.THEME_COLOR);
        String[] colors = new String[]{"Black", "Blue", Locales.kLOC_THEME_COLOR_GREEN, Locales.kLOC_THEME_COLOR_PURPLE, Locales.kLOC_THEME_COLOR_GRAY, Locales.kLOC_THEME_COLOR_COFFEE};
        themes.setEntries(colors);
        themes.setEntryValues(colors);
        themes.setOnPreferenceChangeListener(getChangeListener());
        themes.setSummary(Prefs.getStringPref(Prefs.THEME_COLOR));
    }

    private Preference.OnPreferenceChangeListener getChangeListener() {
        return new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                PocketMoneyThemes.refreshTheme();
                DisplayOptionsPrefsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Builder alert = new Builder(DisplayOptionsPrefsActivity.this);
                        alert.setTitle(Locales.kLOC_GENERAL_RELAUNCH_APP);
                        alert.setMessage(Locales.kLOC_GENERAL_RELAUNCH_APP_THEME);
                        alert.setPositiveButton(Locales.kLOC_GENERAL_QUIT, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = new Intent(DisplayOptionsPrefsActivity.this, LaunchActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                DisplayOptionsPrefsActivity.this.startActivity(i);
                                dialog.dismiss();
                            }
                        });
                        alert.setNegativeButton(Locales.kLOC_GENERAL_CANCEL, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                    }
                });
                return true;
            }
        };
    }
}

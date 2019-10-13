package com.example.smmoney.prefs;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.LaunchActivity;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class DisplayOptionsPrefsActivity extends PocketMoneyPreferenceActivity {
    private Context context;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_display_main);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.context = this;
        setupPrefs();
    }

    private void setupPrefs() {
        findPreference("AccountDisplayPrefs").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.context.startActivity(new Intent(DisplayOptionsPrefsActivity.this.getBaseContext(), AccountDisplayPrefsActivity.class));
                return true;
            }
        });
        findPreference("TransactionRegisterDisplayPrefs").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.context.startActivity(new Intent(DisplayOptionsPrefsActivity.this.getBaseContext(), TransactionRegisterDisplayPrefsActivity.class));
                return true;
            }
        });
        findPreference("BudgetsPrefs").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.context.startActivity(new Intent(DisplayOptionsPrefsActivity.this.getBaseContext(), BudgetsDisplayPrefsActivity.class));
                return true;
            }
        });
        findPreference("EditTransactionPrefs").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.context.startActivity(new Intent(DisplayOptionsPrefsActivity.this.getBaseContext(), EditTransactionDisplayPrefsActivity.class));
                return true;
            }
        });
        findPreference("ReportsPrefs").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                DisplayOptionsPrefsActivity.this.context.startActivity(new Intent(DisplayOptionsPrefsActivity.this.getBaseContext(), ReportsDisplayPrefsActivity.class));
                return true;
            }
        });
        ListPreference themes = (ListPreference) findPreference(Prefs.THEME_COLOR);
        String[] colors = new String[]{"Black", "Blue", Locales.kLOC_THEME_COLOR_GREEN, Locales.kLOC_THEME_COLOR_PURPLE, Locales.kLOC_THEME_COLOR_GRAY, Locales.kLOC_THEME_COLOR_COFFEE};
        themes.setEntries(colors);
        themes.setEntryValues(colors);
        themes.setOnPreferenceChangeListener(getChangeListener());
        themes.setSummary(Prefs.getStringPref(Prefs.THEME_COLOR));
    }

    private OnPreferenceChangeListener getChangeListener() {
        return new OnPreferenceChangeListener() {
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

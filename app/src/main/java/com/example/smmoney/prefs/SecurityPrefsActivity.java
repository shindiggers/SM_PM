package com.example.smmoney.prefs;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivityV2;

public class SecurityPrefsActivity extends PocketMoneyPreferenceActivityV2 {
    private EditTextPreference confirmPref;
    private ListPreference delayListPref;
    private EditTextPreference passwordPref;
    private String storedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_security);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void checkPassword() {
        if (this.passwordPref.getText() == null || this.confirmPref.getText() == null || this.passwordPref.getText().equals(this.confirmPref.getText())) {
            clearToPrefs();
            return;
        }
        this.passwordPref.setText(this.storedPassword);
        this.confirmPref.setText(this.storedPassword);
        Builder alt_bld = new Builder(this);
        alt_bld.setMessage(Locales.kLOC_PREFERENCES_PASSWORD_NOTMATCH).setCancelable(false).setNegativeButton(Locales.kLOC_GENERAL_OK, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SecurityPrefsActivity.this.clearToPrefs();
                dialog.cancel();
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.setTitle(Locales.kLOC_PREFERENCES_PASSWORD_TITLE);
        alert.setIcon(R.drawable.icon);
        alert.show();
    }

    private void clearToPrefs() {
        Intent i = new Intent(this, MainPrefsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        String[] delays = new String[]{Locales.kLOC_GENERAL_NONE, Locales.kLOC_PASSWORDDELAY1MIN, Locales.kLOC_PASSWORDDELAY5MINS, Locales.kLOC_PASSWORDDELAY10MINS, Locales.kLOC_PASSWORDDELAY15MINS, Locales.kLOC_PASSWORDDELAY30MINS, Locales.kLOC_PASSWORDDELAY1HOUR, Locales.kLOC_PASSWORDDELAY2HOURS, Locales.kLOC_PASSWORDDELAY4HOURS, Locales.kLOC_PASSWORDDELAY8HOURS, Locales.kLOC_PASSWORDDELAY24HOURS};
        this.delayListPref = fragment.findPreference(Prefs.PASSWORD_DELAY);
        this.delayListPref.setEntries(delays);
        this.delayListPref.setEntryValues(delays);
        if (this.delayListPref.getValue() == null) {
            this.delayListPref.setDefaultValue(delays[0]);
        }
        this.delayListPref.setOnPreferenceChangeListener(getOnChangeListener());
        this.delayListPref.setSummary(this.delayListPref.getEntry());

        this.passwordPref = fragment.findPreference(Prefs.PASSWORD);
        this.confirmPref = fragment.findPreference("prefssecurityconfirm");
        this.storedPassword = this.passwordPref.getText();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_HOME) {
            return super.onKeyDown(keyCode, event);
        }
        checkPassword();
        return true;
    }

    private Preference.OnPreferenceChangeListener getOnChangeListener() {
        return new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        };
    }
}

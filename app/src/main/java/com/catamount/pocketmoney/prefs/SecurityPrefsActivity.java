package com.catamount.pocketmoney.prefs;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.KeyEvent;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.PocketMoneyPreferenceActivity;

public class SecurityPrefsActivity extends PocketMoneyPreferenceActivity {
    static final int SECURITY_RESULT_DONTPROMPT = 1;
    static final int SECURITY_RESULT_PROMPT = 0;
    boolean confirmChanged = false;
    EditTextPreference confirmPref;
    Context context;
    ListPreference delayListPref;
    boolean passwordChanged = false;
    EditTextPreference passwordPref;
    String storedPassword;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_security);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.context = this;
        setupPrefs();
    }

    protected void onStart() {
        super.onStart();
        this.delayListPref.setSummary(this.delayListPref.getEntry());
    }

    protected void checkPassword() {
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

    public void setupPrefs() {
        String[] delays = new String[]{Locales.kLOC_GENERAL_NONE, Locales.kLOC_PASSWORDDELAY1MIN, Locales.kLOC_PASSWORDDELAY5MINS, Locales.kLOC_PASSWORDDELAY10MINS, Locales.kLOC_PASSWORDDELAY15MINS, Locales.kLOC_PASSWORDDELAY30MINS, Locales.kLOC_PASSWORDDELAY1HOUR, Locales.kLOC_PASSWORDDELAY2HOURS, Locales.kLOC_PASSWORDDELAY4HOURS, Locales.kLOC_PASSWORDDELAY8HOURS, Locales.kLOC_PASSWORDDELAY24HOURS};
        this.delayListPref = (ListPreference) findPreference(Prefs.PASSWORD_DELAY);
        this.delayListPref.setEntries(delays);
        this.delayListPref.setEntryValues(delays);
        if (this.delayListPref.getValue() == null) {
            this.delayListPref.setDefaultValue(delays[0]);
        }
        this.delayListPref.setOnPreferenceChangeListener(getOnChangeListener());
        this.passwordPref = (EditTextPreference) findPreference(Prefs.PASSWORD);
        this.confirmPref = (EditTextPreference) findPreference("prefssecurityconfirm");
        this.storedPassword = this.passwordPref.getText();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 && keyCode != 3) {
            return super.onKeyDown(keyCode, event);
        }
        checkPassword();
        return true;
    }

    public OnPreferenceChangeListener getOnChangeListener() {
        return new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        };
    }
}

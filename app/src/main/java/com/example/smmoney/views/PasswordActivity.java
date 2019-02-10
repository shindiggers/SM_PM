package com.example.smmoney.views;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;

public class PasswordActivity extends Activity {
    public static final int PASSWORD_CORRECT = 132;
    public static final int PASSWORD_INCORRECT = 133;
    private boolean preferenceScreen;
    private String thePass;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(PASSWORD_INCORRECT);
        setTitle(Locales.kLOC_PREFERENCES_PASSWORD_TITLE);
        setContentView(LayoutInflater.from(this).inflate(R.layout.password, null));
        ((EditText) findViewById(R.id.passwordedittext)).addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PasswordActivity.this.checkPassword(s.toString());
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.thePass = Prefs.getStringPref(Prefs.PASSWORD);
        if (this.thePass == null || this.thePass.length() == 0) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            setResult(PASSWORD_CORRECT);
            finish();
        }
        if (Prefs.getLongPref(Prefs.PASSWORD_DELAY_LAST) + getDelayLongFromDelayPref(Prefs.getStringPref(Prefs.PASSWORD_DELAY)) > System.currentTimeMillis()) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            setResult(PASSWORD_CORRECT);
            finish();
        }
    }

    public void checkPassword(String text) {
        if (text != null) {
            if (this.thePass == null || this.thePass.equals(text)) {
                Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
                setResult(PASSWORD_CORRECT);
                finish();
            }
        }
    }

    public static long getDelayLongFromDelayPref(String theString) {
        if (Locales.kLOC_GENERAL_NONE.equals(theString)) {
            return 0;
        }
        if (Locales.kLOC_PASSWORDDELAY1MIN.equals(theString)) {
            return 60000;
        }
        if (Locales.kLOC_PASSWORDDELAY5MINS.equals(theString)) {
            return 300000;
        }
        if (Locales.kLOC_PASSWORDDELAY10MINS.equals(theString)) {
            return 600000;
        }
        if (Locales.kLOC_PASSWORDDELAY15MINS.equals(theString)) {
            return 900000;
        }
        if (Locales.kLOC_PASSWORDDELAY30MINS.equals(theString)) {
            return 1800000;
        }
        if (Locales.kLOC_PASSWORDDELAY1HOUR.equals(theString)) {
            return 3600000;
        }
        if (Locales.kLOC_PASSWORDDELAY2HOURS.equals(theString)) {
            return 7200000;
        }
        if (Locales.kLOC_PASSWORDDELAY4HOURS.equals(theString)) {
            return 14400000;
        }
        if (Locales.kLOC_PASSWORDDELAY8HOURS.equals(theString)) {
            return 28800000;
        }
        if (Locales.kLOC_PASSWORDDELAY24HOURS.equals(theString)) {
            return 86400000;
        }
        return 0;
    }
}

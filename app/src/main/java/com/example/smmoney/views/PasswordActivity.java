package com.example.smmoney.views;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.Prefs;

public class PasswordActivity extends AppCompatActivity {
    public static final int PASSWORD_CORRECT = 132;
    public static final int PASSWORD_INCORRECT = 133;
    private String thePass;

    private static long getDelayLongFromDelayPref(String theString) {
        if (Locales.kLOC_GENERAL_NONE.equals(theString)) return 0;
        if (Locales.kLOC_PASSWORDDELAY1MIN.equals(theString)) return 60000;
        if (Locales.kLOC_PASSWORDDELAY5MINS.equals(theString)) return 300000;
        if (Locales.kLOC_PASSWORDDELAY10MINS.equals(theString)) return 600000;
        if (Locales.kLOC_PASSWORDDELAY15MINS.equals(theString)) return 900000;
        if (Locales.kLOC_PASSWORDDELAY30MINS.equals(theString)) return 1800000;
        if (Locales.kLOC_PASSWORDDELAY1HOUR.equals(theString)) return 3600000;
        if (Locales.kLOC_PASSWORDDELAY2HOURS.equals(theString)) return 7200000;
        if (Locales.kLOC_PASSWORDDELAY4HOURS.equals(theString)) return 14400000;
        if (Locales.kLOC_PASSWORDDELAY8HOURS.equals(theString)) return 28800000;
        if (Locales.kLOC_PASSWORDDELAY24HOURS.equals(theString)) return 86400000;
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(PASSWORD_INCORRECT);
        setTitle(Locales.kLOC_PREFERENCES_PASSWORD_TITLE);
        setContentView(R.layout.password);

        EditText passwordEditText = findViewById(R.id.passwordedittext);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PasswordActivity.this.checkPassword(s.toString());
            }
        });
    }

    private void checkPassword(String text) {
        if (text != null) {
            if (this.thePass == null || this.thePass.equals(text)) {
                Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
                setResult(PASSWORD_CORRECT);
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.thePass = Prefs.getStringPref(Prefs.PASSWORD);

        // If no password is set, just skip
        if (this.thePass == null || this.thePass.isEmpty()) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            setResult(PASSWORD_CORRECT);
            finish();
            return;
        }

        // Check if we are within the delay period
        long lastDelayTime = Prefs.getLongPref(Prefs.PASSWORD_DELAY_LAST);
        String delayPref = Prefs.getStringPref(Prefs.PASSWORD_DELAY);
        if (lastDelayTime + getDelayLongFromDelayPref(delayPref) > System.currentTimeMillis()) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            setResult(PASSWORD_CORRECT);
            finish();
        }
    }
}

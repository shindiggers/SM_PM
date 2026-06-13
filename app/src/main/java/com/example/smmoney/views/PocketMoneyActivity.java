package com.example.smmoney.views;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_HOME;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smmoney.misc.Prefs;

public abstract class PocketMoneyActivity extends AppCompatActivity {
    private boolean isStartingActivity = false;
    private boolean showPasswordScreen = false;

    private final ActivityResultLauncher<Intent> passwordLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == PasswordActivity.PASSWORD_INCORRECT) {
                    setResult(PasswordActivity.PASSWORD_INCORRECT);
                    finish();
                }
                this.isStartingActivity = false;
            }
    );

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(0);
        String dontShowPass = null;
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                dontShowPass = extras.getString("dontShowPass");
            }
        } catch (Exception e) {
            Log.e(com.example.smmoney.SMMoney.TAG, "Exception in PocketMoneyActivity onCreate getting extras", e);
        }
        if (dontShowPass == null) {
            this.showPasswordScreen = true;
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.showPasswordScreen) {
            // Don't reset showPasswordScreen until we actually launch successfully
            if (Prefs.hasPassword()) {
                long lastDelayTime = Prefs.getLongPref(Prefs.PASSWORD_DELAY_LAST);
                String delayPref = Prefs.getStringPref(Prefs.PASSWORD_DELAY);
                // Standard logic to check if we actually need a password right now
                if (lastDelayTime + getDelayLongFromDelayPref(delayPref) <= System.currentTimeMillis()) {
                    this.showPasswordScreen = false;
                    this.isStartingActivity = true;
                    Intent intent = new Intent(this, PasswordActivity.class);
                    intent.putExtra("dontShowPass", "");
                    passwordLauncher.launch(intent);
                    return;
                }
            }
            this.showPasswordScreen = false;
        }
    }

    private long getDelayLongFromDelayPref(String theString) {
        if (com.example.smmoney.misc.Locales.kLOC_GENERAL_NONE.equals(theString)) return 0;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY1MIN.equals(theString)) return 60000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY5MINS.equals(theString))
            return 300000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY10MINS.equals(theString))
            return 600000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY15MINS.equals(theString))
            return 900000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY30MINS.equals(theString))
            return 1800000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY1HOUR.equals(theString))
            return 3600000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY2HOURS.equals(theString))
            return 7200000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY4HOURS.equals(theString))
            return 14400000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY8HOURS.equals(theString))
            return 28800000;
        if (com.example.smmoney.misc.Locales.kLOC_PASSWORDDELAY24HOURS.equals(theString))
            return 86400000;
        return 0;
    }

    protected void onPause() {
        super.onPause();
        android.util.Log.d("PMA", "onPause: " + getClass().getSimpleName() + " isStarting=" + isStartingActivity);
        if (!this.isStartingActivity) {
            this.showPasswordScreen = true;
        }
    }

    @SuppressWarnings("EmptyMethod")
    public void finish() {
        super.finish();
    }

    public void startActivity(Intent i) {
        this.showPasswordScreen = false;
        this.isStartingActivity = true;
        i.putExtra("dontShowPass", "");
        super.startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.isStartingActivity = false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_HOME) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            startActivity(new Intent(this, PasswordActivity.class));
        } else if (keyCode == KEYCODE_BACK) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            String className = getClass().getSimpleName();
            if (className.equals("AccountsActivity") || className.equals("BudgetsActivity")) {
                startActivity(new Intent(this, PasswordActivity.class));
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

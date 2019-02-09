package com.catamount.pocketmoney.views;

import android.content.Intent;
import android.preference.PreferenceActivity;
import com.catamount.pocketmoney.misc.Prefs;

public class PocketMoneyPreferenceActivity extends PreferenceActivity {
    boolean skipPasswordScreen = false;

    protected void onResume() {
        super.onResume();
        this.skipPasswordScreen = !Prefs.hasPassword();
    }

    protected void onPause() {
        super.onPause();
        if (!this.skipPasswordScreen) {
            super.startActivity(new Intent(this, PasswordActivity.class));
        }
    }

    public void finish() {
        this.skipPasswordScreen = true;
        super.finish();
    }

    public void startActivityForResult(Intent i, int req) {
        this.skipPasswordScreen = true;
        super.startActivityForResult(i, req);
    }

    public void startActivity(Intent i) {
        this.skipPasswordScreen = true;
        super.startActivity(i);
    }
}

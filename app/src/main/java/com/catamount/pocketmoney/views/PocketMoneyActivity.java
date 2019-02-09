package com.catamount.pocketmoney.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.accounts.AccountsActivity;
import com.catamount.pocketmoney.views.budgets.BudgetsActivity;

public class PocketMoneyActivity extends Activity {
    protected boolean isStartingActivity = false;
    protected boolean showPasswordScreen = false;
    protected boolean skipPasswordScreen = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(0);
        String dontShowPass = null;
        try {
            dontShowPass = getIntent().getExtras().getString("dontShowPass");
        } catch (NullPointerException e) {
        }
        if (dontShowPass == null) {
            this.showPasswordScreen = true;
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.showPasswordScreen) {
            startActivityForResult(new Intent(this, PasswordActivity.class), 9999);
        }
    }

    protected void onPause() {
        super.onPause();
        if (!this.isStartingActivity) {
            this.showPasswordScreen = true;
        }
    }

    public void finish() {
        super.finish();
    }

    public void startActivityForResult(Intent i, int req) {
        this.showPasswordScreen = false;
        this.isStartingActivity = true;
        i.putExtra("dontShowPass", new String());
        super.startActivityForResult(i, req);
    }

    public void startActivity(Intent i) {
        this.showPasswordScreen = false;
        this.isStartingActivity = true;
        i.putExtra("dontShowPass", new String());
        super.startActivityForResult(i, 9999);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PasswordActivity.PASSWORD_INCORRECT) {
            setResult(PasswordActivity.PASSWORD_INCORRECT);
            finish();
        }
        this.isStartingActivity = false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 3) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            startActivity(new Intent(this, PasswordActivity.class));
        } else if (keyCode == 4) {
            Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
            if (getClass().equals(AccountsActivity.class) || getClass().equals(BudgetsActivity.class)) {
                startActivity(new Intent(this, PasswordActivity.class));
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

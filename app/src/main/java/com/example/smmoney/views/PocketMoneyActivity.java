package com.example.smmoney.views;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_HOME;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smmoney.misc.Prefs;

public abstract class PocketMoneyActivity extends AppCompatActivity {
    private boolean isStartingActivity = false;
    private boolean showPasswordScreen = false;
    protected boolean skipPasswordScreen = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(0);
        String dontShowPass = null;
        try {
            dontShowPass = getIntent().getExtras().getString("dontShowPass");
        } catch (NullPointerException e) {
            e.printStackTrace();
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

    @SuppressWarnings("EmptyMethod")
    public void finish() {
        super.finish();
    }

    public void startActivityForResult(Intent i, int req) {
        this.showPasswordScreen = false;
        this.isStartingActivity = true;
        i.putExtra("dontShowPass", "");
        super.startActivityForResult(i, req);
    }

    public void startActivity(Intent i) {
        this.showPasswordScreen = false;
        this.isStartingActivity = true;
        i.putExtra("dontShowPass", "");
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

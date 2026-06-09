package com.example.smmoney.views;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_HOME;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smmoney.misc.Prefs;

public abstract class PocketMoneyActivity extends AppCompatActivity {
    private boolean isStartingActivity = false;
    private boolean showPasswordScreen = false;
    protected boolean skipPasswordScreen = false;

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
            e.printStackTrace();
        }
        if (dontShowPass == null) {
            this.showPasswordScreen = true;
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.showPasswordScreen) {
            this.showPasswordScreen = false; // Reset to avoid re-triggering on return
            Intent intent = new Intent(this, PasswordActivity.class);
            intent.putExtra("dontShowPass", "");
            passwordLauncher.launch(intent);
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

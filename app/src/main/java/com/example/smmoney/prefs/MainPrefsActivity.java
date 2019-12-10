package com.example.smmoney.prefs;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;

import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PasswordActivity;
import com.example.smmoney.views.accounts.AccountsActivity;
import com.example.smmoney.views.budgets.BudgetsActivity;

import java.util.Objects;

public class MainPrefsActivity extends ListActivity {
    private boolean isStartingActivity = false;
    private boolean showPasswordScreen = false;
    protected boolean skipPasswordScreen = false;

    public void onCreate(Bundle savedInstanceState) throws NullPointerException {
        super.onCreate(savedInstanceState);
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        setListAdapter(new MainPrefsRowAdapter(this));
        setResult(0);
        try {
            Objects.requireNonNull(getActionBar()).setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.currentTintColor()));
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        String dontShowPass;
        try {
            dontShowPass = Objects.requireNonNull(getIntent().getExtras()).getString("dontShowPass");
        } catch (NullPointerException e) {
            e.printStackTrace();
            dontShowPass = null;
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
        Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
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

package com.example.smmoney.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.Prefs;

public abstract class PocketMoneyPreferenceActivityV2 extends AppCompatActivity {
    private boolean skipPasswordScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void startActivityForResult(Intent i, int req) {
        this.skipPasswordScreen = true;
        super.startActivityForResult(i, req);
    }

    @Override
    public void startActivity(Intent i) {
        this.skipPasswordScreen = true;
        super.startActivity(i);
    }

    protected void loadParentFragment(int xmlResId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, PocketMoneyPreferenceFragment.newInstance(xmlResId))
                .commit();
    }

    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        // Hook for subclasses to setup preferences
    }
}

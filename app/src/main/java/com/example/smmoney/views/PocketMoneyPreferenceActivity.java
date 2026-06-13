package com.example.smmoney.views;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;

public abstract class PocketMoneyPreferenceActivity extends PocketMoneyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    protected void loadParentFragment(int xmlResId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, PocketMoneyPreferenceFragment.newInstance(xmlResId))
                .commit();
    }

    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        // Hook for subclasses to set up preferences
    }
}

package com.example.smmoney.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;

public abstract class PocketMoneyPreferenceActivity extends PocketMoneyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById(R.id.settings_container).setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            
            // Ensure title color is visible (White for themed bars, Black for white theme)
            int titleColor = (PocketMoneyThemes.preferenceScreenTheme() == R.style.MyTheme_White) ? Color.BLACK : Color.WHITE;
            SpannableString s = new SpannableString(getSupportActionBar().getTitle() != null ? getSupportActionBar().getTitle() : "");
            s.setSpan(new ForegroundColorSpan(titleColor), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setTitle(s);
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

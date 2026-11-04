package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class ReportsDisplayPrefsActivity extends PocketMoneyPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(SMMoney.getAppContext().getString(R.string.GENERAL_REPORTS));
        loadParentFragment(R.xml.prefs_display_reports);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        androidx.preference.Preference showSummaryChartsPref = fragment.findPreference(Prefs.SHOWSUMMARYCHARTS);
        if (showSummaryChartsPref != null) {
            showSummaryChartsPref.setEnabled(true);
        }
    }
}

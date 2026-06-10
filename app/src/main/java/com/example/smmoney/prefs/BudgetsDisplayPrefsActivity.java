package com.example.smmoney.prefs;

import android.os.Bundle;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class BudgetsDisplayPrefsActivity extends PocketMoneyPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_display_budgets);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }
}

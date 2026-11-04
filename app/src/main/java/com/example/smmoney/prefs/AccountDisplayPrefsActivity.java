package com.example.smmoney.prefs;

import android.os.Bundle;
import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class AccountDisplayPrefsActivity extends PocketMoneyPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(SMMoney.getAppContext().getString(R.string.PREFERENCES_SHOW_ACCOUNTS_TITLE));
        loadParentFragment(R.xml.prefs_display_accounts);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }
}

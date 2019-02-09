package com.catamount.pocketmoney.prefs;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.misc.Prefs;
import com.catamount.pocketmoney.views.PocketMoneyPreferenceActivity;

public class ReportsDisplayPrefsActivity extends PocketMoneyPreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_display_reports);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        findPreference(Prefs.SHOWSUMMARYCHARTS).setEnabled(true);
    }
}

package com.example.smmoney.prefs;

import android.os.Bundle;
import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyPreferenceActivityV2;

public class DataTransfersEmailPrefActivity extends PocketMoneyPreferenceActivityV2 {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.prefs_datatransfers_email);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }
}

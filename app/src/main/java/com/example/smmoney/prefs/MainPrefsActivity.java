package com.example.smmoney.prefs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.ListView;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyActivity;

public class MainPrefsActivity extends PocketMoneyActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(Locales.kLOC_PREFERENCES_TITLE);
        setContentView(R.layout.activity_main_prefs);

        ListView listView = findViewById(android.R.id.list);
        listView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        listView.setAdapter(new MainPrefsRowAdapter(this));
        
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            
            // Ensure title color is visible
            int titleColor = (PocketMoneyThemes.preferenceScreenTheme() == R.style.MyTheme_White) ? Color.BLACK : Color.WHITE;
            SpannableString s = new SpannableString(getSupportActionBar().getTitle() != null ? getSupportActionBar().getTitle() : "");
            s.setSpan(new ForegroundColorSpan(titleColor), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setTitle(s);
        }
    }

    @Override
    public void finish() {
        Prefs.setPref(Prefs.PASSWORD_DELAY_LAST, System.currentTimeMillis());
        super.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

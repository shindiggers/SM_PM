package com.example.smmoney.prefs;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.PocketMoneyActivity;

public class MainPrefsActivity extends PocketMoneyActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_prefs);

        ListView listView = findViewById(android.R.id.list);
        listView.setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        listView.setAdapter(new MainPrefsRowAdapter(this));
        
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(PocketMoneyThemes.actionBarColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

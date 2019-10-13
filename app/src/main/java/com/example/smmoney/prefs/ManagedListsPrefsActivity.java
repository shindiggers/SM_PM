package com.example.smmoney.prefs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;

public class ManagedListsPrefsActivity extends PocketMoneyPreferenceActivity {
    private Context context;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(Locales.kLOC_PREFS_MANAGEDLISTS);
        addPreferencesFromResource(R.xml.prefs_display_managed_lists);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
        this.context = this;
        setupPrefs();
    }

    private void setupPrefs() {
        findPreference("PayeeManagedListsPref").setOnPreferenceClickListener(getListenerForID(4));
        findPreference("CategoryManagedListsPref").setOnPreferenceClickListener(getListenerForID(5));
        findPreference("ClassManagedListsPref").setOnPreferenceClickListener(getListenerForID(6));
        findPreference("IDManagedListsPref").setOnPreferenceClickListener(getListenerForID(7));
    }

    private OnPreferenceClickListener getListenerForID(int id) {
        final int theID = id;
        return new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(ManagedListsPrefsActivity.this.getBaseContext(), LookupsListActivity.class);
                i.putExtra("type", theID);
                i.putExtra("dontShowPass", "");
                ManagedListsPrefsActivity.this.context.startActivity(i);
                return true;
            }
        };
    }
}

package com.example.smmoney.prefs;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;

public class ManagedListsPrefsActivity extends PocketMoneyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(Locales.kLOC_PREFS_MANAGEDLISTS);
        loadParentFragment(R.xml.prefs_display_managed_lists);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        fragment.findPreference("PayeeManagedListsPref").setOnPreferenceClickListener(getListenerForID(4));
        fragment.findPreference("CategoryManagedListsPref").setOnPreferenceClickListener(getListenerForID(5));
        fragment.findPreference("ClassManagedListsPref").setOnPreferenceClickListener(getListenerForID(6));
        fragment.findPreference("IDManagedListsPref").setOnPreferenceClickListener(getListenerForID(7));
    }

    private Preference.OnPreferenceClickListener getListenerForID(int id) {
        final int theID = id;
        return preference -> {
            Intent i = new Intent(ManagedListsPrefsActivity.this, LookupsListActivity.class);
            i.putExtra("type", theID);
            i.putExtra("dontShowPass", "");
            ManagedListsPrefsActivity.this.startActivity(i);
            return true;
        };
    }
}

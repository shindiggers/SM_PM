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
        Preference payeePref = fragment.findPreference("PayeeManagedListsPref");
        if (payeePref != null) {
            payeePref.setOnPreferenceClickListener(getListenerForID(4));
        }

        Preference categoryPref = fragment.findPreference("CategoryManagedListsPref");
        if (categoryPref != null) {
            categoryPref.setOnPreferenceClickListener(getListenerForID(5));
        }

        Preference classPref = fragment.findPreference("ClassManagedListsPref");
        if (classPref != null) {
            classPref.setOnPreferenceClickListener(getListenerForID(6));
        }

        Preference idPref = fragment.findPreference("IDManagedListsPref");
        if (idPref != null) {
            idPref.setOnPreferenceClickListener(getListenerForID(7));
        }
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

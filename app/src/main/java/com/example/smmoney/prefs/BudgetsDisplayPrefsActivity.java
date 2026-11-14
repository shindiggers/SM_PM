package com.example.smmoney.prefs;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;
import com.example.smmoney.views.budgets.BudgetAccountsSelectionActivity;
import com.example.smmoney.views.budgets.BudgetCategoriesSelectionActivity;

public class BudgetsDisplayPrefsActivity extends PocketMoneyPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        setTitle(SMMoney.getAppContext().getString(R.string.PREFS_VIEWOPTIONS_BUDGETS));
        loadParentFragment(R.xml.prefs_display_budgets);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        Preference chooseAccounts = fragment.findPreference("budgetChooseAccounts");
        if (chooseAccounts != null) {
            chooseAccounts.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(this, BudgetAccountsSelectionActivity.class));
                return true;
            });
        }

        Preference chooseCategories = fragment.findPreference("budgetChooseCategories");
        if (chooseCategories != null) {
            chooseCategories.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(this, BudgetCategoriesSelectionActivity.class));
                return true;
            });
        }
    }
}

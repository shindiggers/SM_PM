package com.example.smmoney.views.budgets;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.EndOnDateActivity;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class BudgetsViewOptionsActivity extends PocketMoneyPreferenceActivity {
    private ListPreference sortOnPref;
    private Preference startOnDate;

    private final ActivityResultLauncher<Intent> datePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0) {
                    if (result.getResultCode() == EndOnDateActivity.ENDONDATE_RESULT_DATESELECTED && result.getData() != null) {
                        Intent data = result.getData();
                        this.startOnDate.setSummary(data.getStringExtra("Date"));
                        Prefs.setPref(Prefs.BUDGETSTARTDATE, data.getStringExtra("Date"));
                    } else if (result.getResultCode() == EndOnDateActivity.ENDONDATE_RESULT_NODATESELECTED) {
                        this.startOnDate.setSummary(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
                        Prefs.setPref(Prefs.BUDGETSTARTDATE, Locales.kLOC_GENERAL_DEFAULT);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.budgets_view_options);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
        updateSummaries();
    }

    private void updateSummaries() {
        String date = Prefs.getStringPref(Prefs.BUDGETSTARTDATE);
        if (date != null) {
            this.startOnDate.setSummary(date);
        } else {
            this.startOnDate.setSummary(Locales.kLOC_GENERAL_DEFAULT);
        }
        this.sortOnPref.setSummary(nameOfSortOnListPref(Prefs.getIntPref(Prefs.BUDGETS_SORTON)));
    }

    private String nameOfSortOnListPref(int sortOn) {
        switch (sortOn) {
            case Enums.kBudgetsSortTypeActual /*1*/:
                return Locales.kLOC_BUDGETS_ACTUAL;
            case Enums.kBudgetsSortTypeBudgeted /*2*/:
                return Locales.kLOC_BUDGETS_BUDGETED;
            case Enums.kBudgetsSortTypePercentage /*3*/:
                return Locales.kLOC_BUDGETS_PERCENTAGE;
            default:
                return Locales.kLOC_GENERAL_CATEGORY;
        }
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        this.sortOnPref = fragment.findPreference("budgetsortonpref");
        this.startOnDate = fragment.findPreference("budgetStartDatePref");
        this.sortOnPref.setEntries(new String[]{Locales.kLOC_GENERAL_CATEGORY, Locales.kLOC_BUDGETS_ACTUAL, Locales.kLOC_BUDGETS_BUDGETED, Locales.kLOC_BUDGETS_PERCENTAGE});
        this.sortOnPref.setEntryValues(new String[]{"0", "1", "2", "3"});
        this.sortOnPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int sortOn = Integer.parseInt((String) newValue);
                Prefs.setPref(Prefs.BUDGETS_SORTON, sortOn);
                preference.setSummary(BudgetsViewOptionsActivity.this.nameOfSortOnListPref(sortOn));
                return true;
            }
        });
        this.startOnDate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent anIntent = new Intent(BudgetsViewOptionsActivity.this, EndOnDateActivity.class);
                anIntent.putExtra("Date", BudgetsViewOptionsActivity.this.startOnDate.getSummary().toString());
                anIntent.putExtra(Prefs.BUDGETSTARTDATE, true);
                datePickerLauncher.launch(anIntent);
                return true;
            }
        });
    }
}

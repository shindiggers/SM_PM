package com.example.smmoney.views.budgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.example.smmoney.R;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.EndOnDateActivity;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class BudgetsViewOptionsActivity extends PocketMoneyPreferenceActivity {
    private Context context;
    private ListPreference sortOnPref;
    private Preference startOnDate;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.budgets_view_options);
        this.context = this;
        setupPrefs();
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    protected void onStart() {
        super.onStart();
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

    private void setupPrefs() {
        this.sortOnPref = (ListPreference) findPreference("budgetsortonpref");
        this.startOnDate = findPreference("budgetStartDatePref");
        this.sortOnPref.setEntries(new String[]{Locales.kLOC_GENERAL_CATEGORY, Locales.kLOC_BUDGETS_ACTUAL, Locales.kLOC_BUDGETS_BUDGETED, Locales.kLOC_BUDGETS_PERCENTAGE});
        this.sortOnPref.setEntryValues(new String[]{"0", "1", "2", "3"});
        this.sortOnPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int sortOn = Integer.parseInt((String) newValue);
                Prefs.setPref(Prefs.BUDGETS_SORTON, sortOn);
                preference.setSummary(BudgetsViewOptionsActivity.this.nameOfSortOnListPref(sortOn));
                return true;
            }
        });
        this.startOnDate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent anIntent = new Intent(BudgetsViewOptionsActivity.this.context, EndOnDateActivity.class);
                anIntent.putExtra("Date", BudgetsViewOptionsActivity.this.startOnDate.getSummary().toString());
                anIntent.putExtra(Prefs.BUDGETSTARTDATE, true);
                ((Activity) BudgetsViewOptionsActivity.this.context).startActivityForResult(anIntent, 1);
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            if (resultCode == EndOnDateActivity.ENDONDATE_RESULT_DATESELECTED) {
                this.startOnDate.setSummary(data.getStringExtra("Date"));
                Prefs.setPref(Prefs.BUDGETSTARTDATE, data.getStringExtra("Date"));
            } else if (resultCode == EndOnDateActivity.ENDONDATE_RESULT_NODATESELECTED) {
                this.startOnDate.setSummary(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
                Prefs.setPref(Prefs.BUDGETSTARTDATE, Locales.kLOC_GENERAL_DEFAULT);
            }
        }
    }
}

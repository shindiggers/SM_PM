package com.example.smmoney.views.accounts;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.EndOnDateActivity;
import com.example.smmoney.views.PocketMoneyPreferenceActivityV2;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;

import java.util.GregorianCalendar;

public class AccountsViewOptionsActivity extends PocketMoneyPreferenceActivityV2 {
    private Preference asOfDatePref;
    private ListPreference showAccountsListPref;

    private final ActivityResultLauncher<Intent> datePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0) {
                    if (result.getResultCode() == EndOnDateActivity.ENDONDATE_RESULT_DATESELECTED && result.getData() != null) {
                        Intent data = result.getData();
                        this.asOfDatePref.setSummary(data.getStringExtra("Date"));
                        Prefs.setPref(Prefs.BALANCEONDATE, CalExt.dateFromDescriptionWithMediumDate(data.getStringExtra("Date")).getTimeInMillis());
                    } else if (result.getResultCode() == EndOnDateActivity.ENDONDATE_RESULT_NODATESELECTED) {
                        this.asOfDatePref.setSummary(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
                        Prefs.setPref(Prefs.BALANCEONDATE, 0L);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        loadParentFragment(R.xml.accounts_view_options);
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        setupPrefs(fragment);
        updateSummaries();
    }

    private void updateSummaries() {
        GregorianCalendar cal = new GregorianCalendar();
        long millis = Prefs.getLongPref(Prefs.BALANCEONDATE);
        if (millis != 0L) {
            cal.setTimeInMillis(millis);
            this.asOfDatePref.setSummary(CalExt.descriptionWithMediumDate(cal));
        } else {
            this.asOfDatePref.setSummary(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
        }
        this.showAccountsListPref.setSummary(nameOfAccountListPref());
    }

    private String nameOfAccountListPref() {
        switch (Prefs.getIntPref(Prefs.VIEWACCOUNTS)) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return Locales.kLOC_PREFERENCES_NON_ZERO;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                return Locales.kLOC_GENERAL_TOTALWORTH;
            default:
                return Locales.kLOC_PREFERENCES_SHOW_ALL;
        }
    }

    private void setupPrefs(PreferenceFragmentCompat fragment) {
        this.showAccountsListPref = fragment.findPreference("viewaccountslistpreference");
        this.asOfDatePref = fragment.findPreference(Prefs.BALANCEONDATE);
        String[] theStrings = new String[]{Locales.kLOC_PREFERENCES_SHOW_ALL, Locales.kLOC_PREFERENCES_NON_ZERO, Locales.kLOC_GENERAL_TOTALWORTH};
        this.showAccountsListPref.setEntries(theStrings);
        this.showAccountsListPref.setEntryValues(theStrings);
        this.showAccountsListPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(Locales.kLOC_PREFERENCES_SHOW_ALL)) {
                    Prefs.setPref(Prefs.VIEWACCOUNTS, Enums.kViewAccountsAll /*0*/);
                } else if (newValue.equals(Locales.kLOC_PREFERENCES_NON_ZERO)) {
                    Prefs.setPref(Prefs.VIEWACCOUNTS, Enums.kViewAccountsNonZero /*1*/);
                } else if (newValue.equals(Locales.kLOC_GENERAL_TOTALWORTH)) {
                    Prefs.setPref(Prefs.VIEWACCOUNTS, Enums.kViewAccountsTotalWorth/*2*/);
                }
                preference.setSummary((String) newValue);
                return true;
            }
        });
        this.asOfDatePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent anIntent = new Intent(AccountsViewOptionsActivity.this, EndOnDateActivity.class);
                anIntent.putExtra("Date", AccountsViewOptionsActivity.this.asOfDatePref.getSummary());
                datePickerLauncher.launch(anIntent);
                return true;
            }
        });
    }

}

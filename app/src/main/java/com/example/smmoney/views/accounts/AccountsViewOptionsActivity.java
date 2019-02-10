package com.example.smmoney.views.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.misc.Prefs;
import com.example.smmoney.views.EndOnDateActivity;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.splits.SplitsActivity;
import java.util.GregorianCalendar;

public class AccountsViewOptionsActivity extends PocketMoneyPreferenceActivity {
    private Preference asOfDatePref;
    private Context context;
    private ListPreference showAccountsListPref;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("VIEWOPTACT","onCreate() has just run");
        setTheme(PocketMoneyThemes.preferenceScreenTheme());
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.accounts_view_options);
        this.context = this;
        setupPrefs();
        getWindow().setBackgroundDrawableResource(PocketMoneyThemes.primaryRowSelector());
        getListView().setBackgroundColor(PocketMoneyThemes.groupTableViewBackgroundColor());
        getListView().setCacheColorHint(PocketMoneyThemes.groupTableViewBackgroundColor());
    }

    protected void onStart() {
        super.onStart();
        Log.d("VIEWOPTACT","onStart() has just run");
        GregorianCalendar cal = new GregorianCalendar();
        Log.d("ACCNTVIEWOPTIONS","Balance on date = " + Prefs.BALANCEONDATE);
        Long millis = Prefs.getLongPref(Prefs.BALANCEONDATE);

        Log.d("ACCTVIEWOPTACT","millis = "+millis);

        if (millis != 0L) {
            cal.setTimeInMillis(millis);
            this.asOfDatePref.setSummary(CalExt.descriptionWithMediumDate(cal));
        } else {
            this.asOfDatePref.setSummary(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
        }
        this.showAccountsListPref.setSummary(nameOfAccountListPref());
    }

    public String nameOfAccountListPref() {
        switch (Prefs.getIntPref(Prefs.VIEWACCOUNTS)) {
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return Locales.kLOC_PREFERENCES_NON_ZERO;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                return Locales.kLOC_GENERAL_TOTALWORTH;
            default:
                return Locales.kLOC_PREFERENCES_SHOW_ALL;
        }
    }

    public void setupPrefs() {
        Log.d("ACCTSVIEWOPTSACT","setupPrefs() has just run");
        this.showAccountsListPref = (ListPreference) findPreference("viewaccountslistpreference");
        this.asOfDatePref = findPreference(Prefs.BALANCEONDATE);
        Log.d("ACCTSVIEWOPTACT","Preference asOfDatePref set to = "+ asOfDatePref);
        String[] theStrings = new String[]{Locales.kLOC_PREFERENCES_SHOW_ALL, Locales.kLOC_PREFERENCES_NON_ZERO, Locales.kLOC_GENERAL_TOTALWORTH};
        this.showAccountsListPref.setEntries(theStrings);
        this.showAccountsListPref.setEntryValues(theStrings);
        this.showAccountsListPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(Locales.kLOC_PREFERENCES_SHOW_ALL)) {
                    Prefs.setPref(Prefs.VIEWACCOUNTS, 0);
                } else if (newValue.equals(Locales.kLOC_PREFERENCES_NON_ZERO)) {
                    Prefs.setPref(Prefs.VIEWACCOUNTS, 1);
                } else if (newValue.equals(Locales.kLOC_GENERAL_TOTALWORTH)) {
                    Prefs.setPref(Prefs.VIEWACCOUNTS, 2);
                }
                preference.setSummary((String) newValue);
                return true;
            }
        });
        this.asOfDatePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent anIntent = new Intent(AccountsViewOptionsActivity.this.context, EndOnDateActivity.class);
                anIntent.putExtra("Date", AccountsViewOptionsActivity.this.asOfDatePref.getSummary());
                Log.d("ACCTSVIEWOPTACT","Second element of putExtra = " + AccountsViewOptionsActivity.this.asOfDatePref.getSummary());
                ((Activity) AccountsViewOptionsActivity.this.context).startActivityForResult(anIntent, 1);
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            if (resultCode == EndOnDateActivity.ENDONDATE_RESULT_DATESELECTED) {
                this.asOfDatePref.setSummary(data.getStringExtra("Date"));
                Prefs.setPref(Prefs.BALANCEONDATE, CalExt.dateFromDescriptionWithMediumDate(data.getStringExtra("Date")).getTimeInMillis());
                Log.d("ACCTSVIEWOPTACT","Result from endon date Intent Preference BALANCE ON DATE ="+ CalExt.dateFromDescriptionWithMediumDate(data.getStringExtra("Date")).getTimeInMillis());
            } else if (resultCode == EndOnDateActivity.ENDONDATE_RESULT_NODATESELECTED) {
                this.asOfDatePref.setSummary(Locales.kLOC_EDIT_REPEATING_ENDONNONE);
                Prefs.setPref(Prefs.BALANCEONDATE, 0);
            }
        }
    }
}

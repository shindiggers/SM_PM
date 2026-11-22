package com.example.smmoney.prefs;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.smmoney.R;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.notifications.NotificationWorkManagerHelper;
import com.example.smmoney.views.PocketMoneyPreferenceActivity;

public class NotificationPrefsActivity extends PocketMoneyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.PREFERENCES_NOTIFICATIONS_TITLE);
        loadParentFragment(R.xml.prefs_notifications);
    }

    @Override
    public void onPreferencesCreated(PreferenceFragmentCompat fragment) {
        super.onPreferencesCreated(fragment);
        updateTimePreferenceEntries(fragment, "notif_preferred_alert_time");
        updateTimePreferenceEntries(fragment, "notif_daily_prompt_time");
    }

    private void updateTimePreferenceEntries(PreferenceFragmentCompat fragment, String key) {
        androidx.preference.ListPreference pref = fragment.findPreference(key);
        if (pref == null) return;

        CharSequence[] values = pref.getEntryValues();
        if (values == null || values.length == 0) return;

        CharSequence[] dynamicEntries = new CharSequence[values.length];
        java.text.DateFormat df = android.text.format.DateFormat.getTimeFormat(this);

        for (int i = 0; i < values.length; i++) {
            String val = values[i].toString();
            try {
                String[] parts = val.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
                cal.set(java.util.Calendar.MINUTE, minute);

                dynamicEntries[i] = df.format(cal.getTime());
            } catch (Exception e) {
                dynamicEntries[i] = val;
            }
        }
        pref.setEntries(dynamicEntries);
    }

    @Override
    public void finish() {
        super.finish();
        // Reschedule repeating transaction notifications if alert time or settings changed
        new Thread(TransactionDB::setupNotifications).start();
        // Sync WorkManager periodic tasks with updated preferences
        NotificationWorkManagerHelper.scheduleAllWork(getApplicationContext());
    }
}

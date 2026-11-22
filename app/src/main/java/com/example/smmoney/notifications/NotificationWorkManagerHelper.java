package com.example.smmoney.notifications;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationWorkManagerHelper {
    public static final String WORK_NAME_BALANCE_LIMIT = "balance_limit_check";
    public static final String WORK_NAME_OVERDUE_DIGEST = "overdue_digest_check";
    public static final String WORK_NAME_DAILY_REMINDER = "daily_entry_reminder";

    public static void scheduleAllWork(Context context) {
        WorkManager wm = WorkManager.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String morningAlertTime = prefs.getString("notif_preferred_alert_time", "09:00");
        long morningDelayMs = calculateInitialDelayMillis(morningAlertTime, 9);

        // 1. Balance Limit Check: run once every 24 hours aligned to morning alert time
        boolean limitEnabled = prefs.getBoolean("notif_limit_warnings_enabled", true);
        if (limitEnabled) {
            PeriodicWorkRequest limitRequest = new PeriodicWorkRequest.Builder(
                    BalanceLimitWorker.class,
                    24, TimeUnit.HOURS
            )
                    .setInitialDelay(morningDelayMs, TimeUnit.MILLISECONDS)
                    .build();
            wm.enqueueUniquePeriodicWork(WORK_NAME_BALANCE_LIMIT, ExistingPeriodicWorkPolicy.UPDATE, limitRequest);
        } else {
            wm.cancelUniqueWork(WORK_NAME_BALANCE_LIMIT);
        }

        // 2. Overdue Digest Check: frequency based on preference (1, 2, or 7 days) aligned to morning alert time
        boolean overdueEnabled = prefs.getBoolean("notif_overdue_digest_enabled", true);
        if (overdueEnabled) {
            int freqDays = 1;
            try {
                freqDays = Integer.parseInt(prefs.getString("notif_overdue_digest_freq_days", "1"));
            } catch (NumberFormatException ignored) {
            }
            PeriodicWorkRequest overdueRequest = new PeriodicWorkRequest.Builder(
                    OverdueDigestWorker.class,
                    freqDays, TimeUnit.DAYS
            )
                    .setInitialDelay(morningDelayMs, TimeUnit.MILLISECONDS)
                    .build();
            wm.enqueueUniquePeriodicWork(WORK_NAME_OVERDUE_DIGEST, ExistingPeriodicWorkPolicy.UPDATE, overdueRequest);
        } else {
            wm.cancelUniqueWork(WORK_NAME_OVERDUE_DIGEST);
        }

        // 3. Daily Entry Reminder: run once every 24 hours aligned to evening prompt time (default 20:00 / 8 PM)
        boolean dailyEnabled = prefs.getBoolean("notif_daily_prompt_enabled", false);
        if (dailyEnabled) {
            String dailyPromptTime = prefs.getString("notif_daily_prompt_time", "20:00");
            long eveningDelayMs = calculateInitialDelayMillis(dailyPromptTime, 20);

            PeriodicWorkRequest dailyRequest = new PeriodicWorkRequest.Builder(
                    DailyReminderWorker.class,
                    24, TimeUnit.HOURS
            )
                    .setInitialDelay(eveningDelayMs, TimeUnit.MILLISECONDS)
                    .build();
            wm.enqueueUniquePeriodicWork(WORK_NAME_DAILY_REMINDER, ExistingPeriodicWorkPolicy.UPDATE, dailyRequest);
        } else {
            wm.cancelUniqueWork(WORK_NAME_DAILY_REMINDER);
        }
    }

    private static long calculateInitialDelayMillis(String targetTimeStr, int defaultHour) {
        int targetHour = defaultHour;
        int targetMinute = 0;
        try {
            if (targetTimeStr != null && targetTimeStr.contains(":")) {
                String[] parts = targetTimeStr.split(":");
                targetHour = Integer.parseInt(parts[0]);
                targetMinute = Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {
        }

        Calendar now = Calendar.getInstance();
        Calendar target = (Calendar) now.clone();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}

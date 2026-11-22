package com.example.smmoney.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smmoney.R;
import com.example.smmoney.views.accounts.AccountsActivity;

public class DailyReminderWorker extends Worker {
    private static final String TAG = "DailyReminderWorker";
    public static final String CHANNEL_ID_DAILY = "daily_reminders";
    private static final int NOTIFICATION_ID_DAILY = 7777;

    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("notif_daily_prompt_enabled", false);
        if (!enabled) {
            Log.d(TAG, "Daily reminder prompt disabled in preferences");
            return Result.success();
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_DAILY,
                    context.getString(R.string.PREF_NOTIF_DAILY_CATEGORY),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(context.getString(R.string.PREF_NOTIF_DAILY_ENABLED_SUMMARY));
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, AccountsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, NOTIFICATION_ID_DAILY, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle(context.getString(R.string.NOTIFICATION_DAILY_PROMPT_TITLE))
                .setContentText(context.getString(R.string.NOTIFICATION_DAILY_PROMPT_BODY))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.NOTIFICATION_DAILY_PROMPT_BODY)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        if (nm != null) {
            nm.notify(NOTIFICATION_ID_DAILY, builder.build());
        }

        return Result.success();
    }
}

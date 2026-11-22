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
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.views.repeating.RepeatingActivity;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class OverdueDigestWorker extends Worker {
    private static final String TAG = "OverdueDigestWorker";
    public static final String CHANNEL_ID_OVERDUE = "overdue_transactions";
    private static final int NOTIFICATION_ID_OVERDUE = 8888;

    public OverdueDigestWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("notif_overdue_digest_enabled", true);
        if (!enabled) {
            Log.d(TAG, "Overdue digest disabled in preferences");
            return Result.success();
        }

        ArrayList<RepeatingTransactionClass> repeatingList = TransactionDB.queryAllRepeatingTransactions();
        GregorianCalendar today = new GregorianCalendar();
        int overdueCount = 0;

        for (RepeatingTransactionClass rt : repeatingList) {
            rt.hydrate();
            if (rt.getTransaction() != null && rt.isOverdueOnDate(today)) {
                overdueCount++;
            }
        }

        if (overdueCount > 0) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID_OVERDUE,
                        context.getString(R.string.PREF_NOTIF_OVERDUE_CATEGORY),
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription(context.getString(R.string.PREF_NOTIF_OVERDUE_ENABLED_SUMMARY));
                nm.createNotificationChannel(channel);
            }

            Intent intent = new Intent(context, RepeatingActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, NOTIFICATION_ID_OVERDUE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            String body = context.getResources().getQuantityString(R.plurals.notification_overdue_digest_body, overdueCount, overdueCount);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_OVERDUE)
                    .setSmallIcon(R.drawable.ic_launcher_monochrome)
                    .setContentTitle(context.getString(R.string.NOTIFICATION_OVERDUE_DIGEST_TITLE))
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            if (nm != null) {
                nm.notify(NOTIFICATION_ID_OVERDUE, builder.build());
            }
        }

        return Result.success();
    }
}

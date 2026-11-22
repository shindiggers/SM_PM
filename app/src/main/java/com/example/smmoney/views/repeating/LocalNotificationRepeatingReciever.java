package com.example.smmoney.views.repeating;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.smmoney.R;
import com.example.smmoney.notifications.NotificationActionReceiver;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.transactions.TransactionEditActivity;

import java.util.GregorianCalendar;

public class LocalNotificationRepeatingReciever extends BroadcastReceiver {
    private static final String TAG = "NOTIFY";
    public static final String CHANNEL_ID_REPEATING = "repeating_transactions";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive triggered");

        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(TAG, "No extras in intent");
            return;
        }
        int repeatingID = extras.getInt("repeatingID", 0);
        if (repeatingID == 0) {
            Log.e(TAG, "repeatingID is 0 or missing");
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean repeatingEnabled = prefs.getBoolean("notif_repeating_enabled", true);
        if (!repeatingEnabled) {
            Log.d(TAG, "Repeating notifications globally disabled in preferences");
            return;
        }

        RepeatingTransactionClass repeatingTransaction = new RepeatingTransactionClass(repeatingID);
        repeatingTransaction.hydrate();

        TransactionClass trans = repeatingTransaction.getTransaction();
        if (trans == null) {
            Log.e(TAG, "Repeating transaction has null transaction for ID: " + repeatingID);
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_REPEATING,
                    context.getString(R.string.REPEATING_TRANSACTIONS),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.PREF_NOTIF_REPEATING_ENABLED_SUMMARY));
            notificationManager.createNotificationChannel(channel);
        }

        boolean isOverdue = repeatingTransaction.isOverdueOnDate(new GregorianCalendar());
        String notificationTitle = isOverdue
                ? context.getString(R.string.REPEATING_OVERDUE_LABEL) + ": " + context.getString(R.string.REPEATING_NOTIFICATION_TITLE)
                : context.getString(R.string.REPEATING_NOTIFICATION_TITLE);

        String amountStr = trans.subTotalAsCurrency();
        String body;
        if (trans.isTransfer()) {
            body = context.getString(R.string.NOTIFICATION_TRANSFER_BODY,
                    amountStr,
                    trans.getAccount(),
                    trans.getTransferToAccount());
        } else {
            String payeeOrDesc = trans.getPayee();
            if (payeeOrDesc == null || payeeOrDesc.trim().isEmpty()) {
                payeeOrDesc = context.getString(R.string.REPEATING_TRANSACTIONS);
            }
            body = context.getString(R.string.NOTIFICATION_STANDARD_BODY,
                    trans.getAccount(),
                    payeeOrDesc,
                    amountStr);
        }

        // Tap content intent -> Opens TransactionEditActivity
        Intent editIntent = new Intent(context, TransactionEditActivity.class);
        editIntent.putExtras(extras);
        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, repeatingTransaction.repeatingID * -1, editIntent, pendingIntentFlags);

        // Action 1: Post Now
        Intent postIntent = new Intent(context, NotificationActionReceiver.class);
        postIntent.setAction(NotificationActionReceiver.ACTION_POST_TRANSACTION);
        postIntent.putExtra(NotificationActionReceiver.EXTRA_REPEATING_ID, repeatingTransaction.repeatingID);
        PendingIntent postPendingIntent = PendingIntent.getBroadcast(context, repeatingTransaction.repeatingID * 10 + 1, postIntent, pendingIntentFlags);

        // Action 2: Snooze
        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction(NotificationActionReceiver.ACTION_SNOOZE_TRANSACTION);
        snoozeIntent.putExtra(NotificationActionReceiver.EXTRA_REPEATING_ID, repeatingTransaction.repeatingID);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, repeatingTransaction.repeatingID * 10 + 2, snoozeIntent, pendingIntentFlags);

        // Action 3: Skip
        Intent skipIntent = new Intent(context, NotificationActionReceiver.class);
        skipIntent.setAction(NotificationActionReceiver.ACTION_SKIP_TRANSACTION);
        skipIntent.putExtra(NotificationActionReceiver.EXTRA_REPEATING_ID, repeatingTransaction.repeatingID);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(context, repeatingTransaction.repeatingID * 10 + 3, skipIntent, pendingIntentFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REPEATING)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle(notificationTitle)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_check_circle_24dp, context.getString(R.string.NOTIFICATION_ACTION_POST), postPendingIntent)
                .addAction(R.drawable.ic_today, context.getString(R.string.NOTIFICATION_ACTION_SNOOZE), snoozePendingIntent)
                .addAction(R.drawable.ic_event_repeat, context.getString(R.string.NOTIFICATION_ACTION_SKIP), skipPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(repeatingTransaction.repeatingID, builder.build());
        }
    }
}

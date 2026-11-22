package com.example.smmoney.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.example.smmoney.R;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.repeating.LocalNotificationRepeatingReciever;

import java.util.GregorianCalendar;

public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationAction";

    public static final String ACTION_POST_TRANSACTION = "com.example.smmoney.ACTION_POST_TRANSACTION";
    public static final String ACTION_SNOOZE_TRANSACTION = "com.example.smmoney.ACTION_SNOOZE_TRANSACTION";
    public static final String ACTION_SKIP_TRANSACTION = "com.example.smmoney.ACTION_SKIP_TRANSACTION";

    public static final String EXTRA_REPEATING_ID = "repeatingID";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        final String action = intent.getAction();
        final int repeatingID = intent.getIntExtra(EXTRA_REPEATING_ID, 0);
        if (repeatingID <= 0) {
            Log.w(TAG, "Invalid repeatingID: " + repeatingID);
            return;
        }

        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Immediately dismiss the active notification
        if (nm != null) {
            nm.cancel(repeatingID);
        }

        new Thread(() -> {
            RepeatingTransactionClass rt = new RepeatingTransactionClass(repeatingID);
            rt.hydrate();
            TransactionClass trans = rt.getTransaction();
            if (trans == null) {
                Log.w(TAG, "Transaction not found for ID: " + repeatingID);
                return;
            }

            switch (action) {
                case ACTION_POST_TRANSACTION -> handlePost(context, rt, trans);
                case ACTION_SNOOZE_TRANSACTION -> handleSnooze(context, rt, trans);
                case ACTION_SKIP_TRANSACTION -> handleSkip(context, rt, trans);
                default -> Log.w(TAG, "Unhandled action: " + action);
            }
        }).start();
    }

    private void handlePost(Context context, RepeatingTransactionClass rt, TransactionClass trans) {
        String description = getTransactionDescription(trans);
        String amount = trans.subTotalAsCurrency();

        GregorianCalendar postDate = null;
        if (rt.getLastProcessedDate() != null) {
            postDate = rt.getNextTransactionDateAfter(rt.getLastProcessedDate());
        }
        if (postDate == null) {
            postDate = trans.getDate();
        }
        if (postDate == null) {
            postDate = new GregorianCalendar();
        }

        rt.postAndAdvanceTransaction(trans.getSubTotal(), postDate);
        rt.saveToDatabase();

        showToast(context, context.getString(R.string.NOTIFICATION_POSTED_TOAST, description, amount));
    }

    private void handleSnooze(Context context, RepeatingTransactionClass rt, TransactionClass trans) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String snoozeHoursStr = prefs.getString("notif_default_snooze_hours", "24");
        int snoozeHours = 24;
        try {
            snoozeHours = Integer.parseInt(snoozeHoursStr);
        } catch (NumberFormatException ignored) {
        }

        GregorianCalendar alarmTime = CalExt.addHours(new GregorianCalendar(), snoozeHours);

        Intent alarmIntent = new Intent(context, LocalNotificationRepeatingReciever.class);
        String amountStr = trans.subTotalAsABSString();
        String body = CalExt.descriptionWithMediumDate(trans.getDate()) + " "
                + trans.getAccount() + "->"
                + (trans.isTransfer() ? trans.getTransferToAccount() : trans.getPayee())
                + " (" + amountStr + ")";

        alarmIntent.putExtra("repeatingID", rt.repeatingID);
        alarmIntent.putExtra("localNotification", true);
        alarmIntent.putExtra("Posting", true);
        alarmIntent.putExtra("body", body);
        alarmIntent.putExtra("amount", amountStr);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pIntent = PendingIntent.getBroadcast(context, rt.repeatingID, alarmIntent, flags);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pIntent);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pIntent);
            }
        }

        String snoozeDurationDesc = formatSnoozeDuration(snoozeHours);
        showToast(context, context.getString(R.string.NOTIFICATION_SNOOZED_TOAST, snoozeDurationDesc));
    }

    private void handleSkip(Context context, RepeatingTransactionClass rt, TransactionClass trans) {
        String description = getTransactionDescription(trans);

        GregorianCalendar targetDate = null;
        if (rt.getLastProcessedDate() != null) {
            targetDate = rt.getNextTransactionDateAfter(rt.getLastProcessedDate());
        }
        if (targetDate == null) {
            targetDate = trans.getDate();
        }
        if (targetDate == null) {
            targetDate = new GregorianCalendar();
        }

        TransactionDB.skipTransactionToDate(rt, targetDate);

        showToast(context, context.getString(R.string.NOTIFICATION_SKIPPED_TOAST, description));
    }

    private String getTransactionDescription(TransactionClass trans) {
        if (trans.isTransfer()) {
            return trans.getAccount() + " ➔ " + trans.getTransferToAccount();
        }
        String payee = trans.getPayee();
        if (payee != null && !payee.trim().isEmpty()) {
            return payee;
        }
        return trans.getAccount();
    }

    private String formatSnoozeDuration(int hours) {
        if (hours == 1) return "1 hour";
        if (hours < 24) return hours + " hours";
        int days = hours / 24;
        return days == 1 ? "1 day" : days + " days";
    }

    private void showToast(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}

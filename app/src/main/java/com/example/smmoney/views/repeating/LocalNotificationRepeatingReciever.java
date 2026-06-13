package com.example.smmoney.views.repeating;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.views.transactions.TransactionEditActivity;

import java.util.GregorianCalendar;

public class LocalNotificationRepeatingReciever extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("NOTIFY", "onReceive triggered");
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e("NOTIFY", "No extras in intent");
            return;
        }
        int repeatingID = extras.getInt("repeatingID", 0);
        if (repeatingID == 0) {
            Log.e("NOTIFY", "repeatingID is 0 or missing");
            return;
        }
        RepeatingTransactionClass repeatingTransaction = new RepeatingTransactionClass(repeatingID);
        repeatingTransaction.hydrate();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TransactionEditActivity.class);
        i.putExtras(extras);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        if (extras.getBoolean("cancelled")) {
            am.set(AlarmManager.RTC_WAKEUP, CalExt.addWeeks(new GregorianCalendar(), 520).getTimeInMillis(), PendingIntent.getBroadcast(context, repeatingTransaction.repeatingID, new Intent(context, LocalNotificationRepeatingReciever.class), pendingIntentFlags));
            return;
        }
        String newBody = extras.getString("body");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "repeating_transactions";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Repeating Transactions", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle((repeatingTransaction.isOverdueOnDate(new GregorianCalendar()) ? "OVERDUE " : "") + "Repeating Transaction Due")
                .setContentText(newBody)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(PendingIntent.getActivity(context, repeatingTransaction.repeatingID * -1, i, pendingIntentFlags))
                .setAutoCancel(true);

        notificationManager.cancel(repeatingTransaction.repeatingID);
        Intent deleteIntent = new Intent(context, LocalNotificationRepeatingReciever.class);
        deleteIntent.putExtra("cancelled", true);
        deleteIntent.putExtra("repeatingTransaction", repeatingTransaction);
        builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, pendingIntentFlags));

        notificationManager.notify(repeatingTransaction.repeatingID, builder.build());
        PendingIntent p = PendingIntent.getBroadcast(context, repeatingTransaction.repeatingID, intent, PendingIntent.FLAG_NO_CREATE | (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
        if (p != null) {
            p.cancel();
        }
    }
}

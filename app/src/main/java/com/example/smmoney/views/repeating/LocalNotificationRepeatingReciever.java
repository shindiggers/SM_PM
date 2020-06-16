package com.example.smmoney.views.repeating;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.views.transactions.TransactionEditActivity;

import java.util.GregorianCalendar;

public class LocalNotificationRepeatingReciever extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        RepeatingTransactionClass repeatingTransaction = (RepeatingTransactionClass) extras.get("repeatingTransaction");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TransactionEditActivity.class);
        i.putExtras(extras);
        if (extras.getBoolean("cancelled")) {
            am.set(AlarmManager.RTC_WAKEUP, CalExt.addWeeks(new GregorianCalendar(), 520).getTimeInMillis(), PendingIntent.getBroadcast(context, repeatingTransaction.repeatingID, new Intent(context, LocalNotificationRepeatingReciever.class), PendingIntent.FLAG_CANCEL_CURRENT));
            return;
        }
        String newBody = extras.getString("body");
        Notification notification = new Notification(R.drawable.icon, "SMMoney", System.currentTimeMillis());
        //notification.setLatestEventInfo(context, new StringBuilder(String.valueOf(repeatingTransaction.isOverdueOnDate(new GregorianCalendar()) ? "OVERDUE " : "")).append("Repeating Transaction Due").toString(), newBody, PendingIntent.getActivity(context, repeatingTransaction.repeatingID * -1, i, PendingIntent.FLAG_CANCEL_CURRENT));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(repeatingTransaction.repeatingID);
        Intent deleteIntent = new Intent(context, LocalNotificationRepeatingReciever.class);
        deleteIntent.putExtra("cancelled", true);
        deleteIntent.putExtra("repeatingTransaction", repeatingTransaction);
        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager.notify(repeatingTransaction.repeatingID, notification);
        PendingIntent p = PendingIntent.getBroadcast(context, repeatingTransaction.repeatingID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (p != null) {
            p.cancel();
        }
    }
}

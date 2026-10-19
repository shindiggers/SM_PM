package com.example.smmoney.views.repeating;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            Log.d("NOTIFY", "Rescheduling notifications after boot/update");
            new Thread(() -> LocalNotificationAlertActivitiy.updateLocalNotifications(context.getApplicationContext())).start();
        }
    }
}

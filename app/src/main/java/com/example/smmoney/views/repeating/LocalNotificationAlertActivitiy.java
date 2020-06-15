package com.example.smmoney.views.repeating;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

import com.example.smmoney.R;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.RepeatingTransactionClass;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class LocalNotificationAlertActivitiy extends Activity {
    private Context context;
    private GregorianCalendar date;
    private OnClickListener mainOkayListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };
    private OnClickListener mainPostListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            new Builder(LocalNotificationAlertActivitiy.this.context).setItems(new CharSequence[]{Locales.kLOC_DUPLICATE_TRANSACTION_EXISTING_TIME, Locales.kLOC_DUPLICATE_TRANSACTION_PRESENT_TIME}, LocalNotificationAlertActivitiy.this.postMenuListener);
        }
    };
    private OnClickListener mainSnoozeListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            new Builder(LocalNotificationAlertActivitiy.this.context).setItems(new CharSequence[]{Locales.kLOC_PASSWORDDELAY1HOUR, Locales.kLOC_PASSWORDDELAY2HOURS, Locales.kLOC_PASSWORDDELAY4HOURS, Locales.kLOC_PASSWORDDELAY8HOURS, Locales.kLOC_REPEATING_ONE_DAY, Locales.kLOC_REPEATING_TWO_DAYS}, LocalNotificationAlertActivitiy.this.snoozeMenuListener);
        }
    };
    private OnClickListener postMenuListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            GregorianCalendar gregorianCalendar;
            if (which == PocketMoneyThemes.kThemeBlack) { /*0*/
                gregorianCalendar = (GregorianCalendar) LocalNotificationAlertActivitiy.this.date.clone();
                return;
            }
            gregorianCalendar = new GregorianCalendar();
        }
    };
    private RepeatingTransactionClass repeatingTransaction;
    private OnClickListener snoozeMenuListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            String transferToAccount;
            int hours = 0;
            switch (which) {
                case 0 /*0*/:
                    hours = 1;
                    break;
                case 1 /*1*/:
                    hours = 2;
                    break;
                case 2 /*2*/:
                    hours = 4;
                    break;
                case 3 /*3*/:
                    hours = 8;
                    break;
                case 4 /*4*/:
                    hours = 24;
                    break;
                case 5 /*5*/:
                    hours = 48;
                    break;
            }
            GregorianCalendar newDate = CalExt.addHours(LocalNotificationAlertActivitiy.this.date, hours);
            Intent intent = new Intent(LocalNotificationAlertActivitiy.this.context, LocalNotificationRepeatingReciever.class);
            String body = Locales.kLOC_REPEATING_NOTIFY_DUE;
            //<string name="kLOC_REPEATING_NOTIFY_DUE">%1$sRepeating Transaction due\n%2$s %3$s➜%4$s</string>
            //noinspection RegExpUnexpectedAnchor,ResultOfMethodCallIgnored
            body.replaceFirst("%1$s", LocalNotificationAlertActivitiy.this.repeatingTransaction.isOverdueOnDate(newDate) ? Locales.kLOC_REPEATING_OVERDUE : "");
            //noinspection RegExpUnexpectedAnchor,ResultOfMethodCallIgnored,ResultOfMethodCallIgnored
            body.replaceFirst("%2$s", CalExt.descriptionWithMediumDate(LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getDate()));
            //noinspection RegExpUnexpectedAnchor,ResultOfMethodCallIgnored,ResultOfMethodCallIgnored
            body.replaceFirst("%3$s", LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getAccount());
            //noinspection RegExpUnexpectedAnchor
            String str = "%4$s";
            if (LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().isTransfer()) {
                transferToAccount = LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getTransferToAccount();
            } else {
                transferToAccount = LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getPayee();
            }
            //noinspection ResultOfMethodCallIgnored
            body.replaceFirst(str, transferToAccount);
            intent.putExtra("body", body);
            intent.putExtra("date", LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getDate());
            intent.putExtra("amount", LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().subTotalAsABSString());
            ((AlarmManager) LocalNotificationAlertActivitiy.this.context.getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, newDate.getTimeInMillis(), PendingIntent.getBroadcast(LocalNotificationAlertActivitiy.this.context, LocalNotificationAlertActivitiy.this.repeatingTransaction.repeatingID, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setTheme(R.style.MyTheme); //MyTheme used as placeholder. Work on it for final
        Bundle extras = getIntent().getExtras();
        this.repeatingTransaction = new RepeatingTransactionClass(extras.getInt("repeatingID"));
        this.date = (GregorianCalendar) extras.get("date");
        String amount = extras.getString("amount");
        String body = extras.getString("body");
        Builder builder = new Builder(this.context);
        builder.setMessage(body);
        builder.setPositiveButton("Post", this.mainPostListener);
        builder.setNegativeButton("Snooze", this.mainSnoozeListener);
        builder.setNeutralButton("Okay", this.mainOkayListener);
        builder.create().show();
    }

    public static void updateLocalNotifications() {
        ArrayList<RepeatingTransactionClass> items = TransactionDB.queryAllRepeatingTransactionsWithLocalNotifications();
    }
}

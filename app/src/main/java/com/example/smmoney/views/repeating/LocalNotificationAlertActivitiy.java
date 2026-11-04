package com.example.smmoney.views.repeating;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.os.BundleCompat;

import com.example.smmoney.R;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.records.RepeatingTransactionClass;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class LocalNotificationAlertActivitiy extends Activity {
    private Context context;
    private GregorianCalendar date;
    private final OnClickListener mainOkayListener = (dialog, which) -> dialog.dismiss();
    private final OnClickListener postMenuListener = (dialog, which) -> {
        GregorianCalendar gregorianCalendar;
        if (which == PocketMoneyThemes.kThemeBlack) { /*0*/
            gregorianCalendar = (GregorianCalendar) LocalNotificationAlertActivitiy.this.date.clone();
        } else {
            gregorianCalendar = new GregorianCalendar();
        }
        
        LocalNotificationAlertActivitiy.this.repeatingTransaction.postAndAdvanceTransaction(LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getSubTotal(), gregorianCalendar);
    };
    private final OnClickListener mainPostListener = (dialog, which) -> new Builder(LocalNotificationAlertActivitiy.this.context, PocketMoneyThemes.dialogTheme()).setItems(new CharSequence[]{SMMoney.getAppContext().getString(R.string.DUPLICATE_TRANSACTION_EXISTING_TIME), SMMoney.getAppContext().getString(R.string.DUPLICATE_TRANSACTION_PRESENT_TIME)}, LocalNotificationAlertActivitiy.this.postMenuListener);
    private final OnClickListener snoozeMenuListener = (dialog, which) -> {
        String transferToAccount;
        int hours = switch (which) {
            case 0 /*0*/ -> 1;
            case 1 /*1*/ -> 2;
            case 2 /*2*/ -> 4;
            case 3 /*3*/ -> 8;
            case 4 /*4*/ -> 24;
            case 5 /*5*/ -> 48;
            default -> 0;
        };
        GregorianCalendar newDate = CalExt.addHours(LocalNotificationAlertActivitiy.this.date, hours);
        Intent intent = new Intent(LocalNotificationAlertActivitiy.this.context, LocalNotificationRepeatingReciever.class);
        String body = SMMoney.getAppContext().getString(R.string.REPEATING_NOTIFY_DUE);
        // TODO: 28/10/2026 Sort out the comments below - do as part of modernising the notifications functionality
        //<string name="kLOC_REPEATING_NOTIFY_DUE">%1$sRepeating Transaction due\n%2$s %3$s➜%4$s</string>
        //noinspection RegExpUnexpectedAnchor,ResultOfMethodCallIgnored
        body.replaceFirst("%1$s", LocalNotificationAlertActivitiy.this.repeatingTransaction.isOverdueOnDate(newDate) ? SMMoney.getAppContext().getString(R.string.REPEATING_OVERDUE) : "");
        // noinspection RegExpUnexpectedAnchor,ResultOfMethodCallIgnored
        body.replaceFirst("%2$s", CalExt.descriptionWithMediumDate(LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getDate()));
        // noinspection RegExpUnexpectedAnchor,ResultOfMethodCallIgnored
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
    };
    private RepeatingTransactionClass repeatingTransaction;
    private final OnClickListener mainSnoozeListener = (dialog, which) -> new Builder(LocalNotificationAlertActivitiy.this.context, PocketMoneyThemes.dialogTheme()).setItems(new CharSequence[]{context.getString(R.string.PASSWORDDELAY1HOUR), context.getString(R.string.PASSWORDDELAY2HOURS), context.getString(R.string.PASSWORDDELAY4HOURS), context.getString(R.string.PASSWORDDELAY8HOURS), context.getString(R.string.REPEATING_ONE_DAY), context.getString(R.string.REPEATING_TWO_DAYS)}, LocalNotificationAlertActivitiy.this.snoozeMenuListener);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setTheme(R.style.MyTheme); //MyTheme used as placeholder. Work on it for final
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        this.repeatingTransaction = new RepeatingTransactionClass(extras.getInt("repeatingID", 0));
        this.date = BundleCompat.getSerializable(extras, "date", GregorianCalendar.class);
        String amount = extras.getString("amount");
        String body = extras.getString("body");
        if (body != null && amount != null && !amount.isEmpty() && !body.contains(amount)) {
            body = body + "\n" + SMMoney.getAppContext().getString(R.string.GENERAL_AMOUNT) + ": " + amount;
        }
        Builder builder = new Builder(this.context, PocketMoneyThemes.dialogTheme());
        builder.setMessage(body);
        builder.setPositiveButton(R.string.REPEATING_NOTIFY_POST, this.mainPostListener);
        builder.setNegativeButton(R.string.REPEATING_NOTIFY_SNOOZE, this.mainSnoozeListener);
        builder.setNeutralButton(R.string.GENERAL_OK, this.mainOkayListener);
        builder.create().show();
    }

    public static void updateLocalNotifications(Context context) {
        if (context == null) return;
        ArrayList<RepeatingTransactionClass> items = TransactionDB.queryAllRepeatingTransactionsWithLocalNotifications();
        for (RepeatingTransactionClass item : items) {
            item.setupNotification(context);
        }
    }
}

package com.catamount.pocketmoney.views.repeating;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import com.catamount.pocketmoney.R;
import com.catamount.pocketmoney.database.TransactionDB;
import com.catamount.pocketmoney.misc.CalExt;
import com.catamount.pocketmoney.misc.Locales;
import com.catamount.pocketmoney.misc.PocketMoneyThemes;
import com.catamount.pocketmoney.records.RepeatingTransactionClass;
import com.catamount.pocketmoney.views.lookups.LookupsListActivity;
import com.catamount.pocketmoney.views.splits.SplitsActivity;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class LocalNotificationAlertActivitiy extends Activity {
    Context context;
    GregorianCalendar date;
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
            switch (which) {
                case PocketMoneyThemes.kThemeBlack /*0*/:
                    gregorianCalendar = (GregorianCalendar) LocalNotificationAlertActivitiy.this.date.clone();
                    return;
                default:
                    gregorianCalendar = new GregorianCalendar();
                    return;
            }
        }
    };
    RepeatingTransactionClass repeatingTransaction;
    private OnClickListener snoozeMenuListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            String transferToAccount;
            int hours = 0;
            switch (which) {
                case PocketMoneyThemes.kThemeBlack /*0*/:
                    hours = 1;
                    break;
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    hours = 2;
                    break;
                case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                    hours = 4;
                    break;
                case SplitsActivity.REQUEST_EDIT /*3*/:
                    hours = 8;
                    break;
                case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                    hours = 24;
                    break;
                case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                    hours = 48;
                    break;
            }
            GregorianCalendar newDate = CalExt.addHours(LocalNotificationAlertActivitiy.this.date, hours);
            Intent intent = new Intent(LocalNotificationAlertActivitiy.this.context, LocalNotificationRepeatingReciever.class);
            String body = Locales.kLOC_REPEATING_NOTIFY_DUE;
            body.replaceFirst("%1$s", LocalNotificationAlertActivitiy.this.repeatingTransaction.isOverdueOnDate(newDate) ? Locales.kLOC_REPEATING_OVERDUE : "");
            body.replaceFirst("%2$s", CalExt.descriptionWithMediumDate(LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getDate()));
            body.replaceFirst("%3$s", LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getAccount());
            String str = "%4$s";
            if (LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().isTransfer()) {
                transferToAccount = LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getTransferToAccount();
            } else {
                transferToAccount = LocalNotificationAlertActivitiy.this.repeatingTransaction.getTransaction().getPayee();
            }
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

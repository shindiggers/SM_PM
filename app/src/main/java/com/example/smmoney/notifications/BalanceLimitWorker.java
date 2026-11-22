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
import com.example.smmoney.database.AccountDB;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.records.AccountClass;
import com.example.smmoney.records.RepeatingTransactionClass;
import com.example.smmoney.records.TransactionClass;
import com.example.smmoney.views.accounts.AccountsActivity;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class BalanceLimitWorker extends Worker {
    private static final String TAG = "BalanceLimitWorker";
    public static final String CHANNEL_ID_LIMIT_ALERTS = "account_limit_alerts";

    public BalanceLimitWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("notif_limit_warnings_enabled", true);
        if (!enabled) {
            Log.d(TAG, "Limit warnings disabled in preferences");
            return Result.success();
        }

        int defaultLookaheadDays = 7;
        try {
            defaultLookaheadDays = Integer.parseInt(prefs.getString("notif_limit_lookahead_days", "7"));
        } catch (NumberFormatException ignored) {
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_LIMIT_ALERTS,
                    context.getString(R.string.PREF_NOTIF_LIMIT_CATEGORY),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.PREF_NOTIF_LIMIT_ENABLED_SUMMARY));
            nm.createNotificationChannel(channel);
        }

        ArrayList<AccountClass> accounts = AccountDB.queryOnViewType(0 /* All accounts */);
        ArrayList<RepeatingTransactionClass> repeatingList = TransactionDB.queryAllRepeatingTransactions();
        GregorianCalendar today = CalExt.beginningOfToday();

        for (AccountClass account : accounts) {
            if (!account.hasLimit()) {
                continue;
            }

            int lookaheadDays = account.getLimitLookaheadDays() > 0
                    ? account.getLimitLookaheadDays()
                    : defaultLookaheadDays;
            GregorianCalendar windowEnd = CalExt.endOfDay(CalExt.addDays((GregorianCalendar) today.clone(), lookaheadDays));

            // Calculate running balance projection using balanceOfType(Enums.kBalanceTypeCurrent)
            double currentRunningBal = account.balanceOfType(com.example.smmoney.misc.Enums.kBalanceTypeCurrent);
            boolean isCredit = account.isCreditType();
            double limit = account.getLimit();

            // Collect upcoming scheduled transactions for this account in the window
            for (RepeatingTransactionClass rt : repeatingList) {
                rt.hydrate();
                TransactionClass trans = rt.getTransaction();
                if (trans == null) continue;

                boolean affectsAccount = account.getAccount().equalsIgnoreCase(trans.getAccount())
                        || (trans.isTransfer() && account.getAccount().equalsIgnoreCase(trans.getTransferToAccount()));

                if (!affectsAccount) continue;

                GregorianCalendar nextDate = null;
                if (rt.getLastProcessedDate() != null) {
                    nextDate = rt.getNextTransactionDateAfter(rt.getLastProcessedDate());
                }
                if (nextDate == null) {
                    nextDate = trans.getDate();
                }

                while (nextDate != null && !nextDate.after(windowEnd)) {
                    if (!nextDate.before(today)) {
                        double transAmount = trans.getSubTotal();
                        if (trans.isTransfer() && account.getAccount().equalsIgnoreCase(trans.getTransferToAccount())) {
                            transAmount = -transAmount; // Transfer deposit into account
                        }

                        currentRunningBal += transAmount;

                        boolean breached;
                        if (isCredit) {
                            // Credit card: negative balance beyond -limit
                            breached = (-1.0d * limit > currentRunningBal);
                        } else {
                            // Bank/Checking: balance below minimum limit
                            breached = (limit >= currentRunningBal);
                        }

                        if (breached) {
                            double breachAmount = isCredit
                                    ? Math.abs(currentRunningBal) - limit
                                    : limit - currentRunningBal;

                            String formattedBreach = CurrencyExt.amountAsCurrency(breachAmount, account.getCurrencyCode());
                            String dateStr = CalExt.descriptionWithMediumDate(nextDate);
                            String body;
                            if (isCredit) {
                                body = context.getString(R.string.NOTIFICATION_LIMIT_BREACH_BODY, dateStr, formattedBreach, trans.getPayee());
                            } else {
                                body = context.getString(R.string.NOTIFICATION_MIN_BALANCE_BODY, dateStr, formattedBreach, trans.getPayee());
                            }

                            postLimitNotification(context, nm, account.accountID,
                                    context.getString(R.string.NOTIFICATION_LIMIT_WARNING_TITLE, account.getAccount()),
                                    body);
                            break; // Alert once per account
                        }
                    }
                    nextDate = rt.getNextTransactionDateAfter(nextDate);
                }
            }
        }

        return Result.success();
    }

    private void postLimitNotification(Context context, NotificationManager nm, int notifId, String title, String body) {
        if (nm == null) return;
        Intent intent = new Intent(context, AccountsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, notifId * -100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_LIMIT_ALERTS)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        nm.notify(notifId * 1000, builder.build());
    }
}

package com.example.smmoney.records;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Xml;
import com.example.smmoney.SMMoney;
import com.example.smmoney.database.Database;
import com.example.smmoney.database.TransactionDB;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.lookups.LookupsListActivity;
import com.example.smmoney.views.repeating.LocalNotificationRepeatingReciever;
import com.example.smmoney.views.splits.SplitsActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

public class RepeatingTransactionClass extends PocketMoneyRecordClass implements Serializable {
    public static final String XML_LISTTAG_REPEATINGTRANSACTIONS = "REPEATINGTRANSACTIONS";
    public static final String XML_RECORDTAG_REPEATINGTRANSACTION = "RPTTRANSCLASS";
    private String currentElementValue;
    public final String dayNameToken;
    public final String dayOrdinalToken;
    private GregorianCalendar endDate;
    public final String frequenceToken;
    private int frequency;
    public boolean hydratedTransaction;
    private GregorianCalendar lastProcessedDate;
    public final String monthNameToken;
    private int notifyDaysInAdvance;
    private int repeatOn;
    public int repeatingID;
    private boolean sendLocalNotifications;
    private int startOfWeek;
    private TransactionClass transaction;
    private int transactionID;
    public String transactionServerID;
    private int type;
    public final String weekOrdinalToken;

    public RepeatingTransactionClass() {
        this.frequenceToken = "^f";
        this.dayOrdinalToken = "^x";
        this.weekOrdinalToken = "^w";
        this.monthNameToken = "^m";
        this.dayNameToken = "^d";
        this.hydrated = true;
        this.transactionID = 0;
        this.repeatingID = 0;
        this.transaction = null;
        this.type = 0;
        this.endDate = null;
        this.frequency = 0;
        this.repeatOn = 0;
        this.startOfWeek = 0;
        this.dirty = false;
    }

    public RepeatingTransactionClass(int pk) {
        this.frequenceToken = "^f";
        this.dayOrdinalToken = "^x";
        this.weekOrdinalToken = "^w";
        this.monthNameToken = "^m";
        this.dayNameToken = "^d";
        this.repeatingID = pk;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.REPEATINGTRANSACTIONS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"lastProcessedDate"}, "repeatingID=" + pk, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(((long) curs.getDouble(0)) * 1000);
            setLastProcessedDate(cal);
        } else {
            setLastProcessedDate(null);
        }
        this.dirty = false;
        curs.close();
    }

    public RepeatingTransactionClass(int transactionID, boolean usesTransID) {
        this.frequenceToken = "^f";
        this.dayOrdinalToken = "^x";
        this.weekOrdinalToken = "^w";
        this.monthNameToken = "^m";
        this.dayNameToken = "^d";
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Database.REPEATINGTRANSACTIONS_TABLE_NAME);
        Cursor curs = Database.query(qb, new String[]{"repeatingID"}, "transactionID=" + transactionID, null, null, null, null);
        if (curs.getCount() != 0) {
            curs.moveToFirst();
            this.repeatingID = curs.getInt(0);
        } else {
            this.repeatingID = 0;
            setLastProcessedDate(null);
        }
        this.dirty = false;
        curs.close();
    }

    public RepeatingTransactionClass(TransactionClass aTransaction, boolean skipCheck) {
        this.frequenceToken = "^f";
        this.dayOrdinalToken = "^x";
        this.weekOrdinalToken = "^w";
        this.monthNameToken = "^m";
        this.dayNameToken = "^d";
        this.transactionID = TransactionDB.getRepeatingTransactionFor(aTransaction);
        if (this.transactionID != 0) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.REPEATINGTRANSACTIONS_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"repeatingID"}, "transactionID=" + this.transactionID, null, null, null, null);
            if (curs.getCount() != 0) {
                curs.moveToFirst();
                this.repeatingID = curs.getInt(0);
            } else {
                this.repeatingID = 0;
                setLastProcessedDate(null);
            }
            this.dirty = false;
            curs.close();
        }
    }

    public RepeatingTransactionClass(TransactionClass aTransaction) {
        this.frequenceToken = "^f";
        this.dayOrdinalToken = "^x";
        this.weekOrdinalToken = "^w";
        this.monthNameToken = "^m";
        this.dayNameToken = "^d";
        this.transactionID = TransactionDB.getRepeatingTransactionFor(aTransaction);
        if (this.transactionID == 0) {
            TransactionDB.fixRepeatingTransactionsThatDontRepeatOnDate();
            aTransaction.hydrated = false;
            this.transactionID = TransactionDB.getRepeatingTransactionFor(aTransaction);
        }
        if (this.transactionID != 0) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.REPEATINGTRANSACTIONS_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"repeatingID"}, "transactionID=" + this.transactionID, null, null, null, null);
            if (curs.getCount() != 0) {
                curs.moveToFirst();
                this.repeatingID = curs.getInt(0);
            } else {
                this.repeatingID = 0;
                setLastProcessedDate(null);
            }
            this.dirty = false;
            curs.close();
        }
    }

    public static String[] types() {
        return new String[]{Locales.kLOC_GENERAL_NONE, Locales.kLOC_REPEATING_FREQUENCY_DAILY, Locales.kLOC_REPEATING_FREQUENCY_WEEKLY, Locales.kLOC_REPEATING_FREQUENCY_MONTHLY, Locales.kLOC_REPEATING_FREQUENCY_YEARLY};
    }

    public String typeAsString() {
        return types()[this.type];
    }

    public void setTypeFromString(String aString) {
        int i = 0;
        for (String type : types()) {
            if (type.equals(aString)) {
                setType(i);
            }
            i++;
        }
    }

    public void setType(int aType) {
        if (this.type != aType) {
            this.dirty = true;
            this.type = aType;
        }
    }

    public int getType() {
        hydrate();
        return this.type;
    }

    public String typeEveryAsString() {
        if (2 == this.type && 2 == this.frequency) {
            return Locales.kLOC_BUDGETS_BIWEEKLY; // type 2 = weekly therefore if frequency = 2 this returns "Bi-weekly"
        }
        if (2 == this.type && 2 < this.frequency) {
            return this.frequency + "-" + Locales.kLOC_REPEATING_FREQUENCY_WEEKLY; // type 2 = weekly therefore if frequency < 2 this returns "Weekly"
        }
        if (3 == this.type && 2 == this.frequency) {
            return Locales.kLOC_BUDGETS_BIMONTHLY; // type 3 = monthly therefore if frequency = 2 this returns "Bi-monthly"
        }
        if (3 != this.type || 2 >= this.frequency) { //TODO repeating view always shows none SO: type must ALWAYS by 0. Code elsewhere must not be writing '0' as the type. Figure where this is happening and fix
            return types()[this.type]; // type 3 = weekly thereofore if type is not monthly OR frequency > 2 this returns whatever 'type' is. That could be type = 0 = "None"; type = 1 = "Daily"; type = 4 = "Yearly"
        }
        return this.frequency + "-" + Locales.kLOC_REPEATING_FREQUENCY_MONTHLY; // default if none of other conditions are met = "Monthly"
    }

    public void setSendLocalNotifications(boolean send) {
        if (this.sendLocalNotifications != send) {
            this.dirty = true;
            this.sendLocalNotifications = send;
        }
    }

    public boolean getSendLocalNotifications() {
        hydrate();
        return this.sendLocalNotifications;
    }

    public void setNotifyDaysInAdvance(int days) {
        if (this.notifyDaysInAdvance != days) {
            this.dirty = true;
            this.notifyDaysInAdvance = days;
        }
    }

    public int getNotifyDaysInAdance() {
        hydrate();
        return this.notifyDaysInAdvance;
    }

    public void setFrequency(int freq) {
        if (this.frequency != freq) {
            this.dirty = true;
            if (freq > 0) {
                this.frequency = freq;
            } else {
                this.frequency = 1;
            }
        }
    }

    public int getFrequency() {
        hydrate();
        if (this.frequency == 0) {
            return 1;
        }
        return this.frequency;
    }

    public void setLastProcessedDate(GregorianCalendar cal) {
        int i = 0;
        if (this.lastProcessedDate != null || cal != null) {
            int i2 = this.lastProcessedDate != null ? 1 : 0;
            if (cal != null) {
                i = 1;
            }
            if ((i & i2) == 0 || !this.lastProcessedDate.equals(cal)) {
                this.dirty = true;
                this.lastProcessedDate = cal;
            }
        }
    }

    public GregorianCalendar getLastProcessedDate() {
        hydrate();
        return this.lastProcessedDate;
    }

    public boolean isOverdueOnDate(GregorianCalendar aDate) {
        if (getTransaction() == null) {
            return false;
        }
        return CalExt.endOfDay(getTransaction().getDate()).before(aDate);
    }

    public boolean isOverdue() {
        return isOverdueOnDate(new GregorianCalendar());
    }

    public double overdueAmount() {
        return amountBetweenDate(this.lastProcessedDate, new GregorianCalendar());
    }

    public double amountBetweenDate(GregorianCalendar aBeginDate, GregorianCalendar anEndDate) {
        GregorianCalendar beginDateTemp = (GregorianCalendar) aBeginDate.clone();
        int count = 0;
        if (this.type != 5) {
            if (this.endDate != null && this.endDate.after(anEndDate)) {
                anEndDate = (GregorianCalendar) this.endDate.clone();
            }
            while (CalExt.endOfDay(beginDateTemp).before(anEndDate)) {
                if (beginDateTemp.after(this.lastProcessedDate)) {
                    count++;
                }
                beginDateTemp = getNextTransactionDateAfter(beginDateTemp);
                if (beginDateTemp == null) {
                    break;
                }
            }
            return this.transaction.getSubTotal() * ((double) count);
        } else if (CalExt.endOfDay(this.transaction.getDate()).after(CalExt.beginningOfDay(aBeginDate)) && CalExt.beginningOfDay(this.transaction.getDate()).before(CalExt.endOfDay(anEndDate))) {
            return this.transaction.getSubTotal();
        } else {
            return 0.0d;
        }
    }

    private GregorianCalendar getNextTransactionDateAfter(GregorianCalendar lastDate) {
        GregorianCalendar calendar = new GregorianCalendar();
        GregorianCalendar futureDate = lastDate;
        if (repeatsOnDate(futureDate)) {
            switch (getType()) {
                case SplitsActivity.RESULT_CHANGED /*1*/:
                    return CalExt.addDays(futureDate, getFrequency() * 1);
                case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                    return CalExt.addWeeks(futureDate, getFrequency() * 1);
                case SplitsActivity.REQUEST_EDIT /*3*/:
                    GregorianCalendar returnDate;
                    int dow;
                    switch (getRepeatOn()) {
                        case PocketMoneyThemes.kThemeBlack /*0*/:
                            returnDate = (GregorianCalendar) futureDate.clone();
                            returnDate.set(Calendar.MONTH, returnDate.get(Calendar.MONTH) + (getFrequency() * 1));
                            returnDate.set(Calendar.DAY_OF_MONTH, 1);
                            int newWeekDay = returnDate.get(Calendar.DAY_OF_WEEK);
                            dow = futureDate.get(Calendar.DAY_OF_WEEK);
                            int day = futureDate.get(Calendar.DAY_OF_MONTH);
                            int week = day / 7;
                            if (day % 7 > 0) {
                                week++;
                            }
                            int newDay = 0;
                            if (newWeekDay == dow) {
                                newDay = ((week - 1) * 7) + 1;
                            } else if (newWeekDay < dow) {
                                newDay = (((week - 1) * 7) + 1) + (dow - newWeekDay);
                            } else if (newWeekDay > dow) {
                                newDay = ((((week - 1) * 7) + 1) + (7 - newWeekDay)) + dow;
                            }
                            returnDate.set(Calendar.DAY_OF_MONTH, newDay);
                            return returnDate;
                        case SplitsActivity.RESULT_CHANGED /*1*/:
                            return CalExt.addMonths(futureDate, getFrequency() * 1);
                        case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                        case SplitsActivity.REQUEST_EDIT /*3*/:
                        case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                            returnDate = CalExt.addMonths(futureDate, getFrequency() * 1);
                            int month = futureDate.get(Calendar.MONTH);
                            while (month == returnDate.get(Calendar.MONTH)) {
                                returnDate.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            returnDate.add(Calendar.DAY_OF_MONTH, -1);
                            if (getRepeatOn() == 2) {
                                return returnDate;
                            }
                            if (getRepeatOn() == 3) {
                                int daysBackward = 0;
                                if (returnDate.get(Calendar.DAY_OF_WEEK) == 1) {
                                    daysBackward = -2;
                                } else if (returnDate.get(Calendar.DAY_OF_WEEK) == 7) {
                                    daysBackward = -1;
                                }
                                if (daysBackward != 0) {
                                    return CalExt.addDays(returnDate, daysBackward);
                                }
                                return returnDate;
                            } else if (getRepeatOn() != 4) {
                                return returnDate;
                            } else {
                                dow = futureDate.get(Calendar.DAY_OF_WEEK);
                                int dowReturnDate = returnDate.get(Calendar.DAY_OF_WEEK);
                                while (dow != dowReturnDate) {
                                    returnDate = CalExt.addDays(returnDate, -1);
                                    dowReturnDate = returnDate.get(Calendar.DAY_OF_WEEK);
                                }
                                return returnDate;
                            }
                        default:
                            setRepeatOn(0);
                            return null;
                    }
                case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                    return CalExt.addYears(futureDate, getFrequency() * 1);
                default:
                    return null;
            }
        }
        for (int days = 0; days < 3650; days++) {
            futureDate.add(Calendar.DAY_OF_WEEK, 1);
            if (repeatsOnDate(CalExt.beginningOfDay(futureDate))) {
                return (GregorianCalendar) futureDate.clone();
            }
        }
        return null;
    }

    public void setEndDate(GregorianCalendar cal) {
        int i = 0;
        if (this.endDate != null || cal != null) {
            int i2 = this.endDate != null ? 1 : 0;
            if (cal != null) {
                i = 1;
            }
            if ((i & i2) == 0 || !this.endDate.equals(cal)) {
                this.dirty = true;
                this.endDate = cal;
            }
        }
    }

    public GregorianCalendar getEndDate() {
        hydrate();
        return this.endDate;
    }

    public void setStartOfWeek(int aStartOfWeek) {
        if (this.startOfWeek != aStartOfWeek) {
            this.dirty = true;
            this.startOfWeek = aStartOfWeek;
        }
    }

    public int getStartOfWeek() {
        hydrate();
        return this.startOfWeek;
    }

    public void setRepeatOn(int anInt) {
        if (this.repeatOn != anInt) {
            this.dirty = true;
            this.repeatOn = anInt;
        }
    }

    public int getRepeatOn() {
        hydrate();
        return this.repeatOn;
    }

    public boolean isRepeating() {
        return getType() != 0;
    }

    public boolean repeatesOnDayOfWeek(int dow) {
        if (this.type != 2) {
            return false;
        }
        return ((1 << dow) & getRepeatOn()) != 0;
    }

    public void setRepeatOnDay(int dow, boolean on) {
        if (on) {
            setRepeatOn(getRepeatOn() | (1 << dow));
        } else {
            setRepeatOn(getRepeatOn() & ((1 << dow) ^ 255));
        }
    }

    public void setRepeatOnMonth(int monthlyRepType) {
        setRepeatOn(monthlyRepType);
    }

    public void setTransaction(TransactionClass trans) {
        int i = 0;
        if (this.transaction != null || trans != null) {
            int i2 = this.transaction != null ? 1 : 0;
            if (trans != null) {
                i = 1;
            }
            if ((i & i2) == 0 || !this.transaction.equals(trans)) {
                this.dirty = true;
                this.transaction = trans;
            }
        }
    }

    public TransactionClass getTransaction() {
        hydrate();
        return this.transaction;
    }

    public void deleteNotification(Context context) {
        Intent intent = new Intent(context, LocalNotificationRepeatingReciever.class);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(this.repeatingID);
        PendingIntent p = PendingIntent.getBroadcast(context, this.repeatingID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (p != null) {
            p.cancel();
        }
    }

    public void setupNotification(Context context) {
        if (getTransaction() != null) {
            Intent intent = new Intent(context, LocalNotificationRepeatingReciever.class);
            if (getSendLocalNotifications()) {
                GregorianCalendar newDate = (GregorianCalendar) getTransaction().getDate().clone();
                if (getNotifyDaysInAdance() > 0) {
                    newDate = CalExt.subtractDays(newDate, getNotifyDaysInAdance());
                }
                String newBody = new StringBuilder(String.valueOf(CalExt.descriptionWithMediumDate(getTransaction().getDate()))).append(" ").append(getTransaction().getAccount()).append("->").append(getTransaction().isTransfer() ? getTransaction().getTransferToAccount() : getTransaction().getPayee()).toString();
                intent.putExtra("repeatingTransaction", this);
                intent.putExtra("localNotification", true);
                intent.putExtra("Posting", true);
                intent.putExtra("body", newBody);
                if (PendingIntent.getBroadcast(context, this.repeatingID, intent, PendingIntent.FLAG_NO_CREATE) == null) {
                    ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, newDate.getTimeInMillis(), PendingIntent.getBroadcast(context, this.repeatingID, intent, PendingIntent.FLAG_CANCEL_CURRENT));
                }
            }
        }
    }

    public void postAndAdvanceTransaction() {
        TransactionDB.postTransactionOnDate(this, new GregorianCalendar());
        if (getType() == 5) {
            this.transaction.deleteFromDatabase();
            deleteFromDatabase();
            return;
        }
        setLastProcessedDate(this.transaction.getDate());
        advanceTransactionDateToNextPostDateAfterDate(getLastProcessedDate());
        setupNotification(SMMoney.getAppContext());
    }

    public void postAndAdvanceTransaction(double subtotal, GregorianCalendar date) {
        TransactionDB.postTransactionOnDateWithSubtotal(this, date, subtotal);
        if (getType() == 5) {
            this.transaction.deleteFromDatabase();
            deleteFromDatabase();
            return;
        }
        setLastProcessedDate(this.transaction.getDate());
        advanceTransactionDateToNextPostDateAfterDate(getLastProcessedDate());
        setupNotification(SMMoney.getAppContext());
    }

    public String repeatsOnDayOfMonthAsString() {
        if (getTransaction() == null) {
            return "";
        }
        String retVal = Locales.kLOC_REPEATING_MONTHLYDAY;
        String[] weekDays = new DateFormatSymbols().getWeekdays();
        String[] weekOrdinals = Locales.kLOC_WEEK_ORDINALS.split(" ");
        String weekDayName = weekDays[getTransaction().getDate().get(Calendar.DAY_OF_WEEK)];
        String weekOrdinal = "";
        if (weekOrdinals.length > getTransaction().getDate().get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1) {
            weekOrdinal = weekOrdinals[getTransaction().getDate().get(Calendar.DAY_OF_WEEK_IN_MONTH) - 1];
        } else {
            weekOrdinal = weekOrdinals[weekOrdinals.length - 1];
        }
        return retVal.replace("^d", weekDayName).replace("^w", weekOrdinal);
    }

    public String repeatsOnDateOfMonthAsString() {
        if (getTransaction() == null) {
            return "";
        }
        return Locales.kLOC_REPEATING_MONTHLYDATE.replace("^x", Locales.kLOC_DAY_ORDINALS.split(" ")[getTransaction().getDate().get(Calendar.DAY_OF_MONTH) - 1]);
    }

    public String repeatsOnLastOrdinalWeekdayAsString() {
        if (getTransaction() == null) {
            return "";
        }
        String[] daysOfWeek = new DateFormatSymbols().getWeekdays();
        String[] weekOrdinals = Locales.kLOC_WEEK_ORDINALS.split(" ");
        return Locales.kLOC_REPEATING_MONTHLYDAY.replace("^w", weekOrdinals[weekOrdinals.length - 1]).replace("^d", daysOfWeek[getTransaction().getDate().get(Calendar.DAY_OF_WEEK)]);
    }

    public boolean showDateOfMonth() {
        return this.transaction.getDate().get(Calendar.DAY_OF_MONTH) <= 28;
    }

    public boolean showOrdinalDayOfMonth() {
        return getTransaction().getDate().get(Calendar.DAY_OF_WEEK_IN_MONTH) <= 4;
    }

    public boolean isLastWeekday() {
        return isLastWeekday(this.transaction.getDate());
    }

    public boolean isLastOrdinalWeekday() {
        return isLastOrdinalWeekday(this.transaction.getDate());
    }

    public boolean isLastDay() {
        return isLastDay(this.transaction.getDate());
    }

    private boolean isLastDay(GregorianCalendar date) {
        return date.getActualMaximum(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isLastOrdinalWeekday(GregorianCalendar date) {
        return getTransaction() != null && date.get(Calendar.DAY_OF_MONTH) + 6 >= date.getActualMaximum(Calendar.DAY_OF_MONTH) && date.get(Calendar.DAY_OF_WEEK) == getTransaction().getDate().get(Calendar.DAY_OF_WEEK);
    }

    private boolean isLastWeekday(GregorianCalendar date) {
        int dow = date.get(Calendar.DAY_OF_WEEK);
        if (dow == 7 || dow == 1) {
            return false;
        }
        if (date.getActualMaximum(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        return dow == 6 && date.get(Calendar.DAY_OF_MONTH) + 2 >= date.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public boolean repeatsOnDate(GregorianCalendar cal) {
        if (getTransaction() == null) {
            return false;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        GregorianCalendar startDate = CalExt.beginningOfDay(getTransaction().getDate());
        cal = CalExt.beginningOfDay(cal);
        if (getEndDate() != null && cal.after(getEndDate())) {
            return false;
        }
        boolean onDate;
        switch (getType()) {
            case PocketMoneyThemes.kThemeBlack /*0*/:
                return false;
            case SplitsActivity.RESULT_CHANGED /*1*/:
                return ((int) ((cal.getTimeInMillis() - startDate.getTimeInMillis()) / 86400000)) % getFrequency() == 0;
            case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                onDate = ((1 << (cal.get(Calendar.DAY_OF_WEEK) + -1)) & getRepeatOn()) != 0;
                if (!onDate) {
                    return onDate;
                }
                return ((int) ((cal.getTimeInMillis() - startDate.getTimeInMillis()) / 604800000)) % getFrequency() == 0;
            case SplitsActivity.REQUEST_EDIT /*3*/:
                switch (getRepeatOn()) {
                    case PocketMoneyThemes.kThemeBlack /*0*/:
                        onDate = ((int) (((double) (cal.getTimeInMillis() - startDate.getTimeInMillis())) / 2.62974383E9d)) % getFrequency() == 0;
                        if (!onDate) {
                            return onDate;
                        }
                        onDate = cal.get(Calendar.DAY_OF_WEEK) == startDate.get(Calendar.DAY_OF_WEEK) && cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) == startDate.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                        return onDate;
                    case SplitsActivity.RESULT_CHANGED /*1*/:
                        onDate = ((int) (((double) (cal.getTimeInMillis() - startDate.getTimeInMillis())) / 2.62974383E9d)) % getFrequency() == 0;
                        if (!onDate) {
                            return onDate;
                        }
                        onDate = cal.get(Calendar.DAY_OF_MONTH) == startDate.get(Calendar.DAY_OF_MONTH);
                        if (onDate) {
                            return onDate;
                        }
                        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                        onDate = startDate.get(Calendar.DAY_OF_MONTH) > daysInMonth && cal.get(Calendar.DAY_OF_MONTH) == daysInMonth;
                        return onDate;
                    case LookupsListActivity.ACCOUNT_ICON_LOOKUP /*2*/:
                        return isLastDay(cal);
                    case SplitsActivity.REQUEST_EDIT /*3*/:
                        return isLastWeekday(cal);
                    case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                        return isLastOrdinalWeekday(cal);
                    default:
                        setRepeatOn(0);
                        return false;
                }
            case LookupsListActivity.PAYEE_LOOKUP /*4*/:
                onDate = ((int) (((double) (calendar.getTimeInMillis() - startDate.getTimeInMillis())) / 3.1558464E10d)) % getFrequency() == 0;
                if (!onDate) {
                    return onDate;
                }
                onDate = cal.get(Calendar.DAY_OF_MONTH) == startDate.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == startDate.get(Calendar.MONTH);
                if (!onDate && startDate.get(Calendar.MONTH) == 1 && startDate.get(Calendar.DAY_OF_MONTH) == 29 && cal.get(Calendar.MONTH) == 1 && cal.get(Calendar.DAY_OF_MONTH) == cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    return true;
                }
                return onDate;
            case LookupsListActivity.CATEGORY_LOOKUP /*5*/:
                return CalExt.beginningOfDay(getTransaction().getDate()).equals(CalExt.beginningOfDay(cal));
            default:
                return false;
        }
    }

    public void advanceTransactionDateToNextPostDateAfterDateIgnoringCurrentlySetDate(GregorianCalendar cal) {
        GregorianCalendar calendar = new GregorianCalendar();
        GregorianCalendar futureDate = (GregorianCalendar) cal.clone();
        GregorianCalendar processRepeatingEventsThroughDate = (GregorianCalendar) CalExt.beginningOfDay(cal).clone();
        boolean found = false;
        int days = 0;
        while (!found && days < 3650) {
            futureDate.add(Calendar.DAY_OF_WEEK, 1);
            GregorianCalendar futureDateBOD = CalExt.beginningOfDay(futureDate);
            if (!(processRepeatingEventsThroughDate.equals(futureDateBOD) || processRepeatingEventsThroughDate.after(futureDateBOD))) {
                if (repeatsOnDate(futureDateBOD)) {
                    getTransaction().setDate(futureDate);
                    this.dirty = true;
                    found = true;
                } else if (getEndDate() != null && futureDateBOD.after(CalExt.beginningOfDay(getEndDate()))) {
                    found = true;
                }
            }
            days++;
        }
    }

    public void advanceTransactionDateToNextPostDateAfterDate(GregorianCalendar lastDate) {
        getTransaction().setDate(CalExt.dateWithTimeFromDate(getNextTransactionDateAfter(lastDate), getTransaction().getDate()));
        this.dirty = true;
    }

    public static int insertNewTransactionIDIntoDatabase(int newTransactionID) {
        ContentValues content = new ContentValues();
        content.put("transactionID", newTransactionID);
        content.put("deleted", 0);
        content.put("serverID", Database.newServerID());
        long id = Database.insert(Database.REPEATINGTRANSACTIONS_TABLE_NAME, null, content);
        if (id == -1) {
            return 0;
        }
        return (int) id;
    }

    public static RepeatingTransactionClass recordWithServerID(String serverID) {
        RepeatingTransactionClass record = null;
        if (serverID == null || serverID.length() == 0) {
            return null;
        }
        Cursor c = Database.rawQuery("SELECT repeatingID FROM repeatingTransactions WHERE serverID=" + Database.SQLFormat(serverID), null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            record = new RepeatingTransactionClass(c.getInt(0));
        }
        c.close();
        return record;
    }

    public void deleteFromDatabase() {
        if (this.repeatingID != 0) {
            ((NotificationManager) SMMoney.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(this.repeatingID);
            ContentValues values = new ContentValues();
            values.put("deleted", 1);
            values.put("timestamp", new GregorianCalendar().getTimeInMillis() / 1000);
            Database.update(Database.REPEATINGTRANSACTIONS_TABLE_NAME, values, "repeatingID=" + this.repeatingID, null);
        }
    }

    public void hydrateTransaction() {
        if (!this.hydratedTransaction) {
            this.transaction = new TransactionClass(this.transactionID);
            this.transaction.hydrate();
            this.hydratedTransaction = true;
        }
    }

    public void hydrate() {
        if (!this.hydrated) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Database.REPEATINGTRANSACTIONS_TABLE_NAME);
            Cursor curs = Database.query(qb, new String[]{"deleted", "timestamp", "lastProcessedDate", "transactionID", "type", "endDate", "frequency", "repeatOn", "startOfWeek", "notifyDaysInAdvance", "sendLocalNotifications", "serverID"}, "repeatingID=" + this.repeatingID, null, null, null, null);
            if (curs.getCount() <= 0) {
                this.hydrated = true;
                curs.close();
                return;
            }
            boolean wasDirty = this.dirty;
            curs.moveToFirst();
            int col = 0 + 1; // col = 1
            this.deleted = curs.getInt(0) == 1;
            this.timestamp = new GregorianCalendar();
            int col2 = col + 1; // col2 = 2
            this.timestamp.setTimeInMillis(((long) curs.getDouble(col)) * 1000);
            GregorianCalendar cal = new GregorianCalendar();
            col = col2 + 1; // col = 3
            cal.setTimeInMillis(((long) curs.getDouble(col2)) * 1000);
            setLastProcessedDate(cal);
            col2 = col + 1; // col2 = 4
            this.transactionID = curs.getInt(col);
            col = col2 + 1; // col = 5
            setType(curs.getInt(col2));
            col2 = col + 1;  // col 2 = 6
            double tempDate = curs.getDouble(col);
            cal = new GregorianCalendar();
            cal.setTimeInMillis(((long) tempDate) * 1000);
            if (tempDate == 0.0d) {
                cal = null;
            }
            setEndDate(cal);
            col = col2 + 1; // col = 7
            setFrequency(curs.getInt(col2));
            col2 = col + 1; // col2 = 8
            setRepeatOn(curs.getInt(col));
            col = col2 + 1; // col = 9
            setStartOfWeek(curs.getInt(col2));
            col2 = col + 1; // col2 = 10
            setNotifyDaysInAdvance(curs.getInt(col));
            col = col2 + 1; // col = 11
            setSendLocalNotifications(curs.getInt(col2) == 1);
            col2 = col + 1; // col2 = 12
            String str = curs.getString(col);
            if (str == null) {
                str = "";
            }
            setServerID(str);
            hydrateTransaction();
            if (2 == this.type && this.repeatOn == 0) {
                setRepeatOn(this.transaction.getDate().get(Calendar.DAY_OF_WEEK) - 1);
            }
            if (!wasDirty && this.dirty) {
                this.dirty = false;
            }
            curs.close();
            this.hydrated = true;
        }
    }

    public void dehydrateTransaction() {
        if (this.transaction != null) {
            if (this.transaction.getType() != 5) {
                this.transaction.setType(5); // Transaction object of type 5 = repeating transaction
                this.transaction.dirty = true;
            }
            this.transaction.saveToDatabase();
            System.out.println("repeating(dehydrate1).trans.id = " + getTransaction().transactionID);
            this.transactionID = this.transaction.getTransactionID();
            System.out.println("repeating(dehydrate1).trans.id = " + getTransaction().transactionID);
            this.transaction = null;
            this.hydratedTransaction = false;
        }
    }

    public void dehydrateAndUpdateTimeStamp(boolean updateTimeStamp) {
        long j = 0;
        if (this.dirty) {
            dehydrateTransaction();
            ContentValues values = new ContentValues();
            GregorianCalendar cal = new GregorianCalendar();
            String str = "timestamp";
            long currentTimeMillis = (updateTimeStamp || this.timestamp == null) ? System.currentTimeMillis() / 1000 : this.timestamp.getTimeInMillis() / 1000;
            values.put(str, currentTimeMillis);
            str = "lastProcessedDate";
            if (this.lastProcessedDate != null) {
                currentTimeMillis = this.lastProcessedDate.getTimeInMillis() / 1000;
            } else {
                currentTimeMillis = 0;
            }
            values.put(str, (double) currentTimeMillis);
            values.put("transactionID", this.transactionID);
            values.put("type", this.type);
            String str2 = "endDate";
            if (this.endDate != null) {
                j = this.endDate.getTimeInMillis() / 1000;
            }
            values.put(str2, (double) j);
            values.put("frequency", this.frequency);
            values.put("repeatOn", this.repeatOn);
            values.put("startOfWeek", this.startOfWeek);
            values.put("repeatingID", this.repeatingID);
            values.put("sendLocalNotifications", this.sendLocalNotifications);
            values.put("notifyDaysInAdvance", this.notifyDaysInAdvance);
            if (this.serverID == null || this.serverID.length() == 0) {
                this.serverID = Database.newServerID();
            }
            values.put("serverID", this.serverID);
            Database.update(Database.REPEATINGTRANSACTIONS_TABLE_NAME, values, "repeatingID=" + this.repeatingID, null);
            this.dirty = false;
        }
        this.hydrated = false;
    }

    public void saveToDataBaseAndUpdateTimeStamp(boolean updateTimeStamp) {
        if (this.dirty) {
            if (this.repeatingID == 0) {
                this.repeatingID = insertNewTransactionIDIntoDatabase(this.transactionID);
            }
            dehydrateAndUpdateTimeStamp(updateTimeStamp);
        }
    }

    public void updateWithXML(String xmlTransaction) {
        try {
            XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            InputSource is = new InputSource(new StringReader(xmlTransaction));
            xr.setContentHandler(this);
            xr.parse(is);
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error parsing xml");
        }
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        this.currentElementValue = null;
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        boolean z = false;
        if (this.currentElementValue == null) {
            this.currentElementValue = "";
        }
        if (!localName.equals("filterID")) {
            if (localName.equals("timestamp")) {
                this.timestamp = CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue);
            } else if (localName.equals("deleted")) {
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setDeleted(z);
            } else if (localName.equals("lastProcessedDate")) {
                setLastProcessedDate(CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue));
            } else if (localName.equals("type")) {
                setType(Integer.valueOf(this.currentElementValue));
            } else if (localName.equals("endDate")) {
                setEndDate(CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue));
            } else if (localName.equals("frequency")) {
                setFrequency(Integer.valueOf(this.currentElementValue));
            } else if (localName.equals("repeatOn")) {
                setRepeatOn(Integer.valueOf(this.currentElementValue));
            } else if (localName.equals("startOfWeek")) {
                setStartOfWeek(Integer.valueOf(this.currentElementValue));
            } else if (localName.equals("transactionServerID")) {
                this.transactionServerID = new String(this.currentElementValue);
            } else if (localName.equals("transactionID")) {
                this.transactionID = Integer.valueOf(this.currentElementValue);
            } else if (localName.equals("serverID")) {
                setServerID(this.currentElementValue);
            } else if (localName.equals("sendLocalNotifications")) {
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setSendLocalNotifications(z);
            } else if (localName.equals("notifyDaysInAdvance")) {
                setNotifyDaysInAdvance(Integer.valueOf(this.currentElementValue));
            } else if (localName.equals("transactionServerID")) {
                Class<?> c = getClass();
                try {
                    c.getDeclaredField(localName).set(this, this.currentElementValue);
                } catch (Exception e) {
                    Log.i(SMMoney.TAG, "Invalid tag parsing " + c.getName() + " xml[" + localName + "]");
                }
            }
        }
        this.currentElementValue = null;
    }

    public void characters(char[] ch, int start, int length) {
        if (this.currentElementValue == null) {
            this.currentElementValue = new String(ch, start, length);
        } else {
            this.currentElementValue += new String(ch, start, length);
        }
    }

    private void addText(XmlSerializer body, String text) throws IOException {
        if (text == null) {
            text = "";
        }
        body.text(text);
    }

    public String XMLString() {
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            public void write(int b) {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };
        XmlSerializer body = Xml.newSerializer();
        try {
            body.setOutput(output, "UTF-8");
            body.startTag(null, XML_RECORDTAG_REPEATINGTRANSACTION);
            body.startTag(null, "repeatingID");
            addText(body, Integer.toString(this.repeatingID));
            body.endTag(null, "repeatingID");
            body.startTag(null, "serverID");
            addText(body, getServerID());
            body.endTag(null, "serverID");
            body.startTag(null, "deleted");
            addText(body, this.deleted ? "Y" : "N");
            body.endTag(null, "deleted");
            body.startTag(null, "timestamp");
            addText(body, this.timestamp == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(this.timestamp));
            body.endTag(null, "timestamp");
            body.startTag(null, "lastProcessedDate");
            addText(body, getLastProcessedDate() == null ? CalExt.descriptionWithISO861Date(new GregorianCalendar()) : CalExt.descriptionWithISO861Date(getLastProcessedDate()));
            body.endTag(null, "lastProcessedDate");
            body.startTag(null, "type");
            addText(body, Integer.toString(getType()));
            body.endTag(null, "type");
            body.startTag(null, "endDate");
            addText(body, getEndDate() == null ? "" : CalExt.descriptionWithISO861Date(getEndDate()));
            body.endTag(null, "endDate");
            body.startTag(null, "frequency");
            addText(body, Integer.toString(getFrequency()));
            body.endTag(null, "frequency");
            body.startTag(null, "repeatOn");
            addText(body, Integer.toString(getRepeatOn()));
            body.endTag(null, "repeatOn");
            body.startTag(null, "startOfWeek");
            addText(body, Integer.toString(getStartOfWeek()));
            body.endTag(null, "startOfWeek");
            body.startTag(null, "transactionID");
            addText(body, Integer.toString(this.transactionID));
            body.endTag(null, "transactionID");
            body.startTag(null, "transactionServerID");
            if (getTransaction() != null) {
                addText(body, getTransaction().getServerID());
            }
            body.endTag(null, "transactionServerID");
            body.startTag(null, "sendLocalNotifications");
            addText(body, getSendLocalNotifications() ? "Y" : "N");
            body.endTag(null, "sendLocalNotifications");
            body.startTag(null, "notifyDaysInAdvance");
            addText(body, Integer.toString(getNotifyDaysInAdance()));
            body.endTag(null, "notifyDaysInAdvance");
            body.endTag(null, XML_RECORDTAG_REPEATINGTRANSACTION);
            body.flush();
            return output.toString();
        } catch (Exception e) {
            Log.e(SMMoney.TAG, "Error while creating XML");
            return "";
        }
    }
}

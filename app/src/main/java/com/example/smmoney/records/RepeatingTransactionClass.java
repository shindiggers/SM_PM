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
import com.example.smmoney.misc.Enums;
import com.example.smmoney.misc.Locales;
import com.example.smmoney.views.repeating.LocalNotificationRepeatingReciever;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.SAXParserFactory;

public class RepeatingTransactionClass extends PocketMoneyRecordClass implements Serializable {
    public static final String XML_LISTTAG_REPEATINGTRANSACTIONS = "REPEATINGTRANSACTIONS";
    public static final String XML_RECORDTAG_REPEATINGTRANSACTION = "RPTTRANSCLASS";
    private String currentElementValue;
    @SuppressWarnings("unused")
    private final String dayNameToken;
    @SuppressWarnings("unused")
    private final String dayOrdinalToken;
    private GregorianCalendar endDate;
    @SuppressWarnings("unused")
    private final String frequenceToken;
    private int frequency;
    public boolean hydratedTransaction;
    private GregorianCalendar lastProcessedDate;
    @SuppressWarnings("unused")
    private final String monthNameToken;
    private int notifyDaysInAdvance;
    private int repeatOn;
    public int repeatingID;
    private boolean sendLocalNotifications;
    private int startOfWeek;
    private TransactionClass transaction;
    private int transactionID;
    public String transactionServerID;
    private int type;
    @SuppressWarnings("unused")
    private final String weekOrdinalToken;

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
        Cursor curs = Database.query(qb, new String[]{"lastProcessedDate"}, "repeatingID=" + pk, null, null, null, null); // SQL statement: SELECT lastProcessedDate FROM repeatingTransactions WHERE (repeatingID=1)  ...for pk=1
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

    public RepeatingTransactionClass(int transactionID, @SuppressWarnings("unused") boolean usesTransID) {
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

    public RepeatingTransactionClass(TransactionClass aTransaction, @SuppressWarnings("unused") boolean skipCheck) {
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
        this.transactionID = TransactionDB.getRepeatingTransactionFor(aTransaction); //THIS RETURNS TRANS ID 0 !!!SEEMS TO BE AN ERROR AS TRANS ID IS NOT 0 !!! PUT A BREAK BEFORE THIS LINE AND DEBUG FROM HERE!!!
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
        return new String[]{Locales.kLOC_GENERAL_NONE, Locales.kLOC_REPEATING_FREQUENCY_DAILY,
                Locales.kLOC_REPEATING_FREQUENCY_WEEKLY, Locales.kLOC_REPEATING_FREQUENCY_MONTHLY,
                Locales.kLOC_REPEATING_FREQUENCY_YEARLY};
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

    private void setType(int aType) {
        if (this.type != aType) {
            this.dirty = true;
            this.type = aType;
        }
    }

    public int getType() {
        hydrate();
        return this.type; // 0=no repeat, 1=daily, 2=weekly, 3=monthly, 4=yearly, 5=repeat once only
    }

    public String typeEveryAsString() {
        if (Enums.repeatWeekly == this.type && this.frequency == 2) {
            return Locales.kLOC_BUDGETS_BIWEEKLY;
        }
        if (Enums.repeatWeekly == this.type && 2 < this.frequency) {
            return this.frequency + "-" + Locales.kLOC_REPEATING_FREQUENCY_WEEKLY; // type 2 = weekly therefore if frequency < 2 this returns "Weekly"
        }
        if (Enums.repeatMonthly == this.type && 2 == this.frequency) {
            return Locales.kLOC_BUDGETS_BIMONTHLY; // type 3 = monthly therefore if frequency = 2 this returns "Bi-monthly"
        }
        if (Enums.repeatMonthly != this.type || 2 >= this.frequency) {
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
            if ((i & i2) == 0 || !this.lastProcessedDate.equals(cal)) { // (i & i2) is a bitwise AND. Gives 1 if i & i2 are both '1', otherwise gives 0. So 1 is only returned where lastProcessedDate != null AND cal != null. This test is also passed if lastProcessedDate != cal (ie if the lastProcessedDate of the passed in RT (ie cal) is not equal to the lastProcessedDate of the [persisted] RT object against which the DB is comparing
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
        if (this.type != Enums.repeatingOnce /*5*/) {
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
        @SuppressWarnings("unused") GregorianCalendar calendar = new GregorianCalendar();
        @SuppressWarnings("UnnecessaryLocalVariable") GregorianCalendar futureDate = lastDate;
        if (repeatsOnDate(futureDate)) {
            switch (getType()) {
                case Enums.repeatDaily /*1*/:
                    return CalExt.addDays(futureDate, getFrequency());
                case Enums.repeatWeekly /*2*/:
                    return CalExt.addWeeks(futureDate, getFrequency());
                case Enums.repeatMonthly /*3*/:
                    GregorianCalendar returnDate;
                    int dow;
                    switch (getRepeatOn()) {
                        case Enums.monthlyDayOfMonth /*0*/:
                            returnDate = (GregorianCalendar) futureDate.clone();
                            returnDate.set(Calendar.MONTH, returnDate.get(Calendar.MONTH) + (getFrequency()));
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
                        case Enums.monthlyDateInMonth /*1*/:
                            return CalExt.addMonths(futureDate, getFrequency());
                        case Enums.monthlyLastDayOfMonth /*2*/:
                        case Enums.monthlyLastWeekDayOfMonth /*3*/:
                        case Enums.monthlyLastOrdinalWeekdayOfMonth /*4*/:
                            returnDate = CalExt.addMonths(futureDate, getFrequency());
                            int month = futureDate.get(Calendar.MONTH);
                            while (month == returnDate.get(Calendar.MONTH)) {
                                returnDate.add(Calendar.DAY_OF_MONTH, 1);
                            }
                            returnDate.add(Calendar.DAY_OF_MONTH, -1);
                            if (getRepeatOn() == Enums.monthlyLastDayOfMonth /*2*/) {
                                return returnDate;
                            }
                            if (getRepeatOn() == Enums.monthlyLastWeekDayOfMonth /*3*/) {
                                int daysBackward = 0;
                                if (returnDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY /*1*/) {
                                    daysBackward = -2; /* take 2 days off the get to Friday (ie last weekday) */
                                } else if (returnDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY /*7*/) {
                                    daysBackward = -1; /* take 1 days off the get to Friday (ie last weekday) */
                                }
                                if (daysBackward != 0) {
                                    return CalExt.addDays(returnDate, daysBackward);
                                }
                                return returnDate;
                            } else if (getRepeatOn() != Enums.monthlyLastOrdinalWeekdayOfMonth /*4*/) {
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
                            setRepeatOn(Enums.monthlyDayOfMonth /*0*/);
                            return null;
                    }
                case Enums.repeatYearly /*4*/:
                    return CalExt.addYears(futureDate, getFrequency());
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

    private void setStartOfWeek(int aStartOfWeek) {
        if (this.startOfWeek != aStartOfWeek) {
            this.dirty = true;
            this.startOfWeek = aStartOfWeek;
        }
    }

    private int getStartOfWeek() {
        hydrate();
        return this.startOfWeek;
    }

    private void setRepeatOn(int anInt) {
        if (this.repeatOn != anInt) {
            this.dirty = true;
            this.repeatOn = anInt;
        }
    }

    public int getRepeatOn() {
        hydrate();
        return this.repeatOn;
    }

    /**
     * Uses @link{getType()} method to check whether a Tranaction is a RepeatingTransaction
     * <p>
     * type 0 = no repeat
     * type 1 = daily repeat
     * type 2 = weekly repeat
     * type 3 = monthly repeat
     * type 4 = yearly repeat
     * type 5 = repeat once
     *
     * @return boolen returns true if Transaction is Repeating Transaction, false otherwise
     */
    public boolean isRepeating() {
        return getType() != Enums.repeatNone /*0*/;
    }

    public boolean repeatesOnDayOfWeek(int dow) {
        if (this.type != Enums.repeatWeekly /*2*/) {
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
                String newBody = CalExt.descriptionWithMediumDate(getTransaction().getDate()) + " " + getTransaction().getAccount() + "->" + (getTransaction().isTransfer() ? getTransaction().getTransferToAccount() : getTransaction().getPayee());
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

    @SuppressWarnings("unused")
    public void postAndAdvanceTransaction() {
        TransactionDB.postTransactionOnDate(this, new GregorianCalendar());
        if (getType() == Enums.repeatingOnce /*5*/) {
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
        if (getType() == Enums.repeatingOnce /*5*/) {
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
        @SuppressWarnings("UnusedAssignment") String weekOrdinal = "";
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

    /**
     * DOWIM = how many times a particular weekday (M to S) has occured on the transaction date
     * If this is less than 4 then we want to show "The nth week of the month"
     * If it is 4 or more,
     *
     * @return false
     */
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

    /**
     * Calculates if the weekday of @date (ie Sun, Mon, Tue etc) is the last Sun, Mon, Tue etc of the month
     *
     * @param date the date of the repeating transaction
     * @return true or false as appropriate
     */

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

    /**
     * Checks if a Transaction repeats on the date that is passed into the method
     *
     * @param cal Gregorian Calendar object
     * @return boolean
     */
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
            case Enums.repeatNone /*0*/:
                return false;
            case Enums.repeatDaily /*1*/:
                return ((int) ((cal.getTimeInMillis() - startDate.getTimeInMillis()) / 86400000)) % getFrequency() == 0; // 86,400,000 = milliseconds in a day
            // i.e 86,400,000/1000 = 86,400 seconds/60 = 1,440 mins/60 = 24 hours
            // If the difference between the date (in millis) of the transaction passed in (i.e. 'cal') and the date (in millis) of the startDate (i.e. the trans associated with the RT)
            // is divisible by 86,400,000 without remainder then the transaction must be due to repeat as the difference is exactly 1 day and this is a daily repeat test.
            case Enums.repeatWeekly /*2*/:
                onDate = ((1 << (cal.get(Calendar.DAY_OF_WEEK) + -1)) & getRepeatOn()) != 0; //DAY_OF_WEEK - 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
                if (!onDate) {
                    return onDate;
                }
                return ((int) ((cal.getTimeInMillis() - startDate.getTimeInMillis()) / 604800000)) % getFrequency() == 0; // 604,800,000 = milliseconds in a week i.e. 604,800,000/1000 = 604,800 seconds/60 = 10,800 mins/60 = 168 hours/24 = 7 days.
            case Enums.repeatMonthly /*3*/:
                switch (getRepeatOn()) {
                    case Enums.monthlyDayOfMonth /*0*/:
                        onDate = ((int) (((double) (cal.getTimeInMillis() - startDate.getTimeInMillis())) / 2.62974383E9d)) % getFrequency() == 0;
                        if (!onDate) {
                            return onDate;
                        }
                        onDate = cal.get(Calendar.DAY_OF_WEEK) == startDate.get(Calendar.DAY_OF_WEEK) && cal.get(Calendar.DAY_OF_WEEK_IN_MONTH) == startDate.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                        return onDate;
                    case Enums.monthlyDateInMonth /*1*/:
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
                    case Enums.monthlyLastDayOfMonth /*2*/:
                        return isLastDay(cal);
                    case Enums.monthlyLastWeekDayOfMonth /*3*/:
                        return isLastWeekday(cal);
                    case Enums.monthlyLastOrdinalWeekdayOfMonth /*4*/:
                        return isLastOrdinalWeekday(cal);
                    default:
                        setRepeatOn(0);
                        return false;
                }
            case Enums.repeatYearly /*4*/:
                // 3.1558464^10 millis = 365.26 days. Cast to int = 365 days. Divide diff in time (in millis) by this. Result = # of years.
                // Divide result by frequency (eg every 1 year, 2 years etc). If the remained is 0 then there is a whole number of 'x' yearly intervals between the years so...
                // the transaction repeats on date. Set boolean onDate to 'true', otherwise 'false'. WHAT ABOUT 366 DAY LEAP YEARS?
                onDate = ((int) (((double) (calendar.getTimeInMillis() - startDate.getTimeInMillis())) / 3.1558464E10d)) % getFrequency() == 0;
                if (!onDate) {
                    return onDate;
                }
                onDate = cal.get(Calendar.DAY_OF_MONTH) == startDate.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == startDate.get(Calendar.MONTH);// if DAY_OF_MONTH and MONTH of both dates are same, set onDate to 'true', otherwise false
                // If no previous match to 'x' yearly gap between dates -> check if RT transaction date is 29 Feb. If it is, check if date of transaction passed (i.e. 'cal')
                // in is last day of Feb.
                // If these conditions are 'true' then return 'true', the transaction must repeat
                if (!onDate && startDate.get(Calendar.MONTH) == Calendar.FEBRUARY && startDate.get(Calendar.DAY_OF_MONTH) == 29 && cal.get(Calendar.MONTH) == Calendar.FEBRUARY && cal.get(Calendar.DAY_OF_MONTH) == cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    return true;
                }
                return onDate;
            case Enums.repeatingOnce /*5*/:
                return CalExt.beginningOfDay(getTransaction().getDate()).equals(CalExt.beginningOfDay(cal));
            default:
                return false;
        }
    }

    public void advanceTransactionDateToNextPostDateAfterDateIgnoringCurrentlySetDate(GregorianCalendar cal) {
        @SuppressWarnings("unused") GregorianCalendar calendar = new GregorianCalendar();
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

    private static int insertNewTransactionIDIntoDatabase(int newTransactionID) {
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

    private void hydrateTransaction() {
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
            int col = 1;                                                                        // col = 1
            this.deleted = curs.getInt(0) == 1;                                      // set this.deleted to 1 (ie true) if the selected RT is deleted
            this.timestamp = new GregorianCalendar();                                           // set the timestamp to current time/date
            int col2 = col + 1;                                                                 // col2 = 2
            this.timestamp.setTimeInMillis(((long) curs.getDouble(col)) * 1000);                // set timestamp = timestamp of the RT that has just been read from the database
            GregorianCalendar cal = new GregorianCalendar();                                    // create new GC object called 'cal'. Initialise to current time/date (ie degault when new CG intantiated)
            col = col2 + 1;                                                                     // col = 3
            cal.setTimeInMillis(((long) curs.getDouble(col2)) * 1000);                          // set 'cal' to the lastProcessedDate as just read from the database
            setLastProcessedDate(cal);                                                          // set the lastProcessedDate of the RT object to the value of 'cal' (ie the lastProcessedDate just read from the database
            col2 = col + 1;                                                                     // col2 = 4
            this.transactionID = curs.getInt(col);                                              // set the transactionID of the RT object equal to the transactionID just read from the database for the RT
            col = col2 + 1;                                                                     // col = 5
            setType(curs.getInt(col2));                                                         // set the 'type' of the RT to the value just read from the database. See Enums line 223 et seq - eg 3 = monthlyLastWeekdatOfMonth
            col2 = col + 1;                                                                     // col 2 = 6
            double tempDate = curs.getDouble(col);                                              // store the 'endDate' of the RT as just read from the database in tempDate
            cal = new GregorianCalendar();                                                      // re-set 'cal' to a new GC object, automatically instantiated with current time/date
            cal.setTimeInMillis(((long) tempDate) * 1000);                                      // change the 'cal' time to the value of tempDate (ie the endDate of the RT just read from database). NB If there is no endDate, that would be null
            if (tempDate == 0.0d) {                                                             // check if there is no endDate
                cal = null;                                                                     // if so, set 'cal' to null
            }
            setEndDate(cal);                                                                    // set the endDate of the RT object to equal 'cal', as determined above
            col = col2 + 1;                                                                     // col = 7
            setFrequency(curs.getInt(col2));                                                    // set the frequency of the RT object to the frequency just read from the RT database table
            col2 = col + 1;                                                                     // col2 = 8
            setRepeatOn(curs.getInt(col));                                                      // set the 'repeatsOn' RT object param to the repeats.On of the RT just read from the database
            col = col2 + 1;                                                                     // col = 9
            setStartOfWeek(curs.getInt(col2));                                                  // set the startOfWeek to the RT startOfWeek just read from the database
            col2 = col + 1;                                                                     // col2 = 10
            setNotifyDaysInAdvance(curs.getInt(col));                                           // set the notifyDaysInAdvance to the RT notifyDaysInAdvance just read from the database
            col = col2 + 1;                                                                     // col = 11
            setSendLocalNotifications(curs.getInt(col2) == 1);                                  // set the sendLocalNotifications RT param to the notifyDaysInAdvance of the RT just read from the database
            String str = curs.getString(col);                                                   // read the serverID of the RT just retrieved from the DB
            if (str == null) {
                str = "";
            }
            setServerID(str);                                                                   // set the serverID of the RT object to the above serverID
            hydrateTransaction();
            if (Enums.repeatWeekly/*2*/ == this.type && this.repeatOn == Enums.monthlyDayOfMonth/*0*/) {
                setRepeatOn(this.transaction.getDate().get(Calendar.DAY_OF_WEEK) - 1);
            }
            if (!wasDirty && this.dirty) {
                this.dirty = false;
            }
            curs.close();
            this.hydrated = true;
        }
    }

    private void dehydrateTransaction() {
        if (this.transaction != null) {
            if (this.transaction.getType() != Enums.kTransactionTypeRepeating /*5*/) {
                this.transaction.setType(Enums.kTransactionTypeRepeating/*5*/); // Transaction object of type 5 = repeating transaction
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
            @SuppressWarnings("unused") GregorianCalendar cal = new GregorianCalendar();
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
                setType(Integer.parseInt(this.currentElementValue));
            } else if (localName.equals("endDate")) {
                setEndDate(CalExt.dateFromDescriptionWithISO861Date(this.currentElementValue));
            } else if (localName.equals("frequency")) {
                setFrequency(Integer.parseInt(this.currentElementValue));
            } else if (localName.equals("repeatOn")) {
                setRepeatOn(Integer.parseInt(this.currentElementValue));
            } else if (localName.equals("startOfWeek")) {
                setStartOfWeek(Integer.parseInt(this.currentElementValue));
            } else if (localName.equals("transactionServerID")) {
                this.transactionServerID = this.currentElementValue;
            } else if (localName.equals("transactionID")) {
                this.transactionID = Integer.parseInt(this.currentElementValue);
            } else if (localName.equals("serverID")) {
                setServerID(this.currentElementValue);
            } else if (localName.equals("sendLocalNotifications")) {
                if (this.currentElementValue.equals("Y") || this.currentElementValue.equals("1")) {
                    z = true;
                }
                setSendLocalNotifications(z);
            } else if (localName.equals("notifyDaysInAdvance")) {
                setNotifyDaysInAdvance(Integer.parseInt(this.currentElementValue));
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
            private final StringBuilder string = new StringBuilder();

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

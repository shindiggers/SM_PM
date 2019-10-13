package com.example.smmoney.misc;

import android.text.format.DateFormat;
import com.example.smmoney.SMMoney;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalExt {
    private static SimpleDateFormat dfdISO861 = null;
    private static String[] months = null;

    private static String[] getMonths() {
        if (months == null) {
            months = new DateFormatSymbols().getMonths();
        }
        return months;
    }

    /**
     * Constructs a GregorianCalendar with today's date (default for empty constructor)
     *
     * Set HOUR_OF_DAY to 0
     * Set MINUTE to 0
     * Set SECOND to 0
     * Set MILLISECOND to 0
     *
     * @return GregorianCalendar object with current date, time set to just after midnight (ie start of day)
     */
    public static GregorianCalendar beginningOfToday() {
        GregorianCalendar newCal = new GregorianCalendar();
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }
    /**
     * Constructs a GregorianCalendar with today's date (default for empty constructor)
     *
     * Set HOUR_OF_DAY to 23
     * Set MINUTE to 59
     * Set SECOND to 59
     * Set MILLISECOND to 999
     *
     * @return GregorianCalendar object with current date, time set to just before midnight (ie end of day)
     */
    public static GregorianCalendar endOfToday() {
        GregorianCalendar newCal = new GregorianCalendar();
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static GregorianCalendar beginningOfDay(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static GregorianCalendar endOfDay(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static GregorianCalendar endOfWeek(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.DAY_OF_WEEK, newCal.getActualMaximum(Calendar.DAY_OF_WEEK));
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static GregorianCalendar beginningOfWeek(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.DAY_OF_WEEK, 0);
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static GregorianCalendar beginningOfMonth(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.DAY_OF_MONTH, newCal.getActualMinimum(Calendar.DAY_OF_MONTH));
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static GregorianCalendar endOfMonth(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.DAY_OF_MONTH, newCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static GregorianCalendar beginningOfQuarter(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        return beginningOfMonth(addMonths(newCal, (newCal.get(Calendar.MONTH) % 3) * -1));
    }

    public static GregorianCalendar endOfQuarter(GregorianCalendar cal) {
        return endOfMonth(addMonths(beginningOfQuarter((GregorianCalendar) cal.clone()), 2));
    }

    public static GregorianCalendar beginningOfYear(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.DAY_OF_YEAR, newCal.getActualMinimum(Calendar.DAY_OF_YEAR));
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static GregorianCalendar endOfYear(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.set(Calendar.DAY_OF_YEAR, newCal.getActualMaximum(Calendar.DAY_OF_YEAR));
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static GregorianCalendar middleOfYear() {
        GregorianCalendar newCal = new GregorianCalendar();
        newCal.set(Calendar.MONTH, 5);
        newCal.set(Calendar.DAY_OF_MONTH, 0);
        newCal.set(Calendar.HOUR_OF_DAY, 0);
        newCal.set(Calendar.MINUTE, 0);
        newCal.set(Calendar.SECOND, 0);
        newCal.set(Calendar.MILLISECOND, 0);
        return newCal;
    }

    public static int daysBetween(GregorianCalendar day1, GregorianCalendar day2) {
        return (int) ((day2.getTimeInMillis() - day1.getTimeInMillis()) / 86400000);
    }

    public static int weeksBetween(GregorianCalendar day1, GregorianCalendar day2) {
        return (int) ((day2.getTimeInMillis() - day1.getTimeInMillis()) / 604800000);
    }

    public static int monthsBetween(GregorianCalendar day1, GregorianCalendar day2) {
        return (int) (((double) (day2.getTimeInMillis() - day1.getTimeInMillis())) / 2.62974383E9d);
    }

    public static int yearsBetween(GregorianCalendar day1, GregorianCalendar day2) {
        return (int) (((double) (day2.getTimeInMillis() - day1.getTimeInMillis())) / 3.1558464E10d);
    }

    public static int daysInYear(GregorianCalendar cal) {
        GregorianCalendar beginingOfYear = (GregorianCalendar) cal.clone();
        GregorianCalendar endOfYear = (GregorianCalendar) cal.clone();
        beginingOfYear.set(Calendar.MONTH, 0);
        beginingOfYear.set(Calendar.DAY_OF_MONTH, 0);
        beginingOfYear.set(Calendar.HOUR_OF_DAY, 0);
        beginingOfYear.set(Calendar.MINUTE, 0);
        beginingOfYear.set(Calendar.SECOND, 0);
        beginingOfYear.set(Calendar.MILLISECOND, 0);
        endOfYear.set(Calendar.MONTH, endOfYear.getActualMaximum(Calendar.MONTH));
        endOfYear.set(Calendar.DAY_OF_MONTH, endOfYear.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfYear.set(Calendar.HOUR_OF_DAY, 23);
        endOfYear.set(Calendar.MINUTE, 59);
        endOfYear.set(Calendar.SECOND, 59);
        endOfYear.set(Calendar.MILLISECOND, 999);
        return daysBetween(beginingOfYear, endOfYear);
    }

    public static GregorianCalendar addHours(GregorianCalendar cal, int hours) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.HOUR_OF_DAY, hours);
        return newCal;
    }

    public static GregorianCalendar addSecond(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.SECOND, 1);
        return newCal;
    }

    public static GregorianCalendar subtractSecond(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.SECOND, -1);
        return newCal;
    }

    public static GregorianCalendar subtractDay(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.DAY_OF_MONTH, -1);
        return newCal;
    }

    public static GregorianCalendar subtractDays(GregorianCalendar cal, int days) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.DAY_OF_MONTH, days * -1);
        return newCal;
    }

    public static GregorianCalendar addDays(GregorianCalendar cal, int days) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.DAY_OF_MONTH, days);
        return newCal;
    }

    public static GregorianCalendar addWeeks(GregorianCalendar cal, int weeks) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.DAY_OF_MONTH, weeks * 7);
        return newCal;
    }

    public static GregorianCalendar addMonth(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.MONTH, 1);
        return newCal;
    }

    public static GregorianCalendar addMonths(GregorianCalendar cal, int months) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.MONTH, months);
        return newCal;
    }

    public static GregorianCalendar subtractMonth(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.MONTH, -1);
        return newCal;
    }

    public static GregorianCalendar subtractMonths(GregorianCalendar cal, int months) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.MONTH, -months);
        return newCal;
    }

    public static GregorianCalendar addYears(GregorianCalendar cal, int years) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.YEAR, years);
        return newCal;
    }

    public static GregorianCalendar addYear(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.YEAR, 1);
        return newCal;
    }

    public static GregorianCalendar subtractYear(GregorianCalendar cal) {
        GregorianCalendar newCal = (GregorianCalendar) cal.clone();
        newCal.add(Calendar.YEAR, -1);
        return newCal;
    }

    public static GregorianCalendar distantFuture() {
        GregorianCalendar newCal = new GregorianCalendar();
        newCal.set(Calendar.YEAR, 3000);
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static GregorianCalendar distantPast() {
        GregorianCalendar newCal = new GregorianCalendar();
        newCal.set(Calendar.YEAR, 500);
        newCal.set(Calendar.HOUR_OF_DAY, 23);
        newCal.set(Calendar.MINUTE, 59);
        newCal.set(Calendar.SECOND, 59);
        newCal.set(Calendar.MILLISECOND, 999);
        return newCal;
    }

    public static String descriptionWithMonth(GregorianCalendar cal) {
        return getMonths()[cal.get(Calendar.MONTH)];
    }

    public static String descriptionWithYear(GregorianCalendar cal) {
        return String.valueOf(cal.get(Calendar.YEAR));
    }

    public static String descriptionWithShortDate(GregorianCalendar cal) {
        return DateFormat.getDateFormat(SMMoney.getAppContext()).format(new Date(cal.getTimeInMillis()));
    }

    private static String descriptionWithMediumDateAndTime(GregorianCalendar cal) {
        Date newDate = new Date(cal.getTimeInMillis());
        return DateFormat.getMediumDateFormat(SMMoney.getAppContext()).format(newDate) + " " + DateFormat.getTimeFormat(SMMoney.getAppContext()).format(newDate);
    }

    public static String descriptionWithShortTime(GregorianCalendar cal) {
        return DateFormat.getTimeFormat(SMMoney.getAppContext()).format(new Date(cal.getTimeInMillis()));
    }

    public static String descriptionWithMediumDate(GregorianCalendar cal) {
        return DateFormat.getMediumDateFormat(SMMoney.getAppContext()).format(new Date(cal.getTimeInMillis()));
    }

    public static String descriptionWithDateTime(GregorianCalendar cal) {
        return descriptionWithMediumDateAndTime(cal);
    }

    public static String descriptionWithTimestamp(GregorianCalendar cal) {
        Date newDate = new Date(cal.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(newDate);
    }

    public static String descriptionWithISO861Date(GregorianCalendar cal) {
        Date newDate = new Date(cal.getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(newDate);
    }

    public static GregorianCalendar dateFromDescriptionWithISO861Date(String aString) {
        if (aString == null) {
            return null;
        }
        if (dfdISO861 == null) {
            dfdISO861 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        }
        GregorianCalendar cal = new GregorianCalendar();
        dfdISO861.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            cal.setTime(dfdISO861.parse(aString));
            return cal;
        } catch (ParseException e) {
            return null;
        }
    }

    public static GregorianCalendar dateFromDescriptionWithShortDate(String aString) {
        try {
            Date theDate = DateFormat.getDateFormat(SMMoney.getAppContext()).parse(aString);
            GregorianCalendar newCal = new GregorianCalendar();
            newCal.setTimeInMillis(theDate.getTime());
            return newCal;
        } catch (ParseException e) {
            return null;
        }
    }

    public static GregorianCalendar dateFromDescriptionWithMediumDate(String aString) {
        try {
            Date theDate = DateFormat.getMediumDateFormat(SMMoney.getAppContext()).parse(aString);
            GregorianCalendar newCal = new GregorianCalendar();
            newCal.setTimeInMillis(theDate.getTime());
            return newCal;
        } catch (ParseException e) {
            return new GregorianCalendar();
        }
    }

    public static GregorianCalendar dateFromDescriptionWithTime(String str) {
        GregorianCalendar newCal = new GregorianCalendar();
        try {
            Date theTime = DateFormat.getTimeFormat(SMMoney.getAppContext()).parse(str);
            newCal.set(Calendar.HOUR_OF_DAY, theTime.getHours());
            newCal.set(Calendar.MINUTE, theTime.getMinutes());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newCal;
    }

    public static GregorianCalendar dateWithTimeFromDate(GregorianCalendar date, GregorianCalendar timeDate) {
        date = beginningOfDay(date);
        date.set(Calendar.MILLISECOND, timeDate.get(Calendar.MILLISECOND));
        date.set(Calendar.SECOND, timeDate.get(Calendar.SECOND));
        date.set(Calendar.MINUTE, timeDate.get(Calendar.MINUTE));
        date.set(Calendar.HOUR_OF_DAY, timeDate.get(Calendar.HOUR_OF_DAY));
        return date;
    }
}

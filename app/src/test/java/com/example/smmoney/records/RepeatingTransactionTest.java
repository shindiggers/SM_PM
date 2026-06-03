package com.example.smmoney.records;

import static org.junit.Assert.assertEquals;

import com.example.smmoney.misc.Enums;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class RepeatingTransactionTest {

    private TransactionClass baseTransaction;
    private RepeatingTransactionClass repeatingTransaction;

    @Before
    public void setUp() {
        // Create a base transaction
        baseTransaction = new TransactionClass();
        baseTransaction.setSubTotal(100.0);
        // Start date: Jan 1, 2026
        baseTransaction.setDate(new GregorianCalendar(2026, Calendar.JANUARY, 1));

        // Initialize repeating transaction manually to avoid DB calls in constructor
        repeatingTransaction = new RepeatingTransactionClass();
        repeatingTransaction.setTransaction(baseTransaction);
        repeatingTransaction.setFrequency(1);
    }

    @Test
    public void testDailyRepeat() {
        repeatingTransaction.setType(Enums.repeatDaily);
        repeatingTransaction.setFrequency(1); // Every day

        // Jan 1 to Jan 10 (10 days inclusive)
        GregorianCalendar start = new GregorianCalendar(2026, Calendar.JANUARY, 1);
        GregorianCalendar end = new GregorianCalendar(2026, Calendar.JANUARY, 10);

        // Should find 10 occurrences: Jan 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        double amount = repeatingTransaction.amountBetweenDate(start, end);
        assertEquals(1000.0, amount, 0.001);
    }

    @Test
    public void testWeeklyRepeat() {
        repeatingTransaction.setType(Enums.repeatWeekly);
        repeatingTransaction.setFrequency(1); // Every week
        // Jan 1, 2026 is a Thursday. Bitmask for Thursday: 1 << (5-1) = 16
        repeatingTransaction.setRepeatOn(16);

        GregorianCalendar start = new GregorianCalendar(2026, Calendar.JANUARY, 1);
        GregorianCalendar end = new GregorianCalendar(2026, Calendar.JANUARY, 31);

        // Jan 1 (Thu), 8 (Thu), 15 (Thu), 22 (Thu), 29 (Thu) -> 5 occurrences
        double amount = repeatingTransaction.amountBetweenDate(start, end);
        assertEquals(500.0, amount, 0.001);
    }

    @Test
    public void testMonthlyDateRepeat() {
        repeatingTransaction.setType(Enums.repeatMonthly);
        repeatingTransaction.setRepeatOn(Enums.monthlyDateInMonth);
        repeatingTransaction.setFrequency(1); // Every month

        GregorianCalendar start = new GregorianCalendar(2026, Calendar.JANUARY, 1);
        GregorianCalendar end = new GregorianCalendar(2026, Calendar.MARCH, 1);

        // Jan 1, Feb 1, Mar 1 -> 3 occurrences
        double amount = repeatingTransaction.amountBetweenDate(start, end);
        assertEquals(300.0, amount, 0.001);
    }

    @Test
    public void testMonthlyLastDayRepeat() {
        repeatingTransaction.setType(Enums.repeatMonthly);
        repeatingTransaction.setRepeatOn(Enums.monthlyLastDayOfMonth);
        repeatingTransaction.setFrequency(1);
        baseTransaction.setDate(new GregorianCalendar(2026, Calendar.JANUARY, 31));

        GregorianCalendar start = new GregorianCalendar(2026, Calendar.JANUARY, 1);
        GregorianCalendar end = new GregorianCalendar(2026, Calendar.MARCH, 31);

        // Jan 31, Feb 28, Mar 31 -> 3 occurrences
        double amount = repeatingTransaction.amountBetweenDate(start, end);
        assertEquals(300.0, amount, 0.001);
    }

    @Test
    public void testEndDateCapping() {
        repeatingTransaction.setType(Enums.repeatDaily);
        // End date: Jan 5
        repeatingTransaction.setEndDate(new GregorianCalendar(2026, Calendar.JANUARY, 5));

        GregorianCalendar start = new GregorianCalendar(2026, Calendar.JANUARY, 1);
        GregorianCalendar end = new GregorianCalendar(2026, Calendar.JANUARY, 31);

        // Even though we look through Jan 31, it should stop at Jan 5
        // Jan 1, 2, 3, 4, 5 -> 5 occurrences
        double amount = repeatingTransaction.amountBetweenDate(start, end);
        assertEquals(500.0, amount, 0.001);
    }

    @Test
    public void testLastProcessedDateExclusion() {
        repeatingTransaction.setType(Enums.repeatDaily);
        // Last posted: Jan 3
        repeatingTransaction.setLastProcessedDate(new GregorianCalendar(2026, Calendar.JANUARY, 3));

        GregorianCalendar start = new GregorianCalendar(2026, Calendar.JANUARY, 1);
        GregorianCalendar end = new GregorianCalendar(2026, Calendar.JANUARY, 10);

        // Window is Jan 1-10, but Jan 1, 2, 3 are already processed
        // Should find: Jan 4, 5, 6, 7, 8, 9, 10 -> 7 occurrences
        double amount = repeatingTransaction.amountBetweenDate(start, end);
        assertEquals(700.0, amount, 0.001);
    }

    @Test
    public void testTodayInclusivity() {
        // GIVEN: Today is May 1st. User looks forward 30 days (to May 31st)
        GregorianCalendar today = new GregorianCalendar(2026, Calendar.MAY, 1);
        GregorianCalendar endDate = new GregorianCalendar(2026, Calendar.MAY, 31);

        // A transaction scheduled for May 31st
        baseTransaction.setDate(new GregorianCalendar(2026, Calendar.MAY, 31));
        repeatingTransaction.setType(Enums.repeatMonthly);
        repeatingTransaction.setRepeatOn(Enums.monthlyDateInMonth);
        repeatingTransaction.setFrequency(1);

        // WHEN: We check amount between today and May 31st
        double amount = repeatingTransaction.amountBetweenDate(today, endDate);

        // THEN: It should be included (£100) because the window is inclusive of the 30th day
        assertEquals(100.0, amount, 0.001);
    }
}

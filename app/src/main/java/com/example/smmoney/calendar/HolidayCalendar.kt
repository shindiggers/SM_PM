package com.example.smmoney.calendar

import java.time.LocalDate

/**
 * Common interface for calculating working days and public holidays across different jurisdictions.
 */
interface HolidayCalendar {
    /**
     * Returns true if the date is a standard working day and not a public holiday.
     */
    fun isWorkingDay(date: LocalDate): Boolean

    /**
     * Returns true if the date is a statutory public holiday for this jurisdiction.
     */
    fun isBankHoliday(date: LocalDate): Boolean

    /**
     * Resolves the n-th working day of a given month and year.
     */
    fun getNthWorkingDayOfMonth(year: Int, month: Int, n: Int): LocalDate

    /**
     * Rolls a date forward to the next available working day if it falls on a weekend or holiday.
     */
    fun rollForwardToWorkingDay(date: LocalDate): LocalDate

    /**
     * Rolls a date backward to the previous available working day if it falls on a weekend or holiday.
     */
    fun rollBackwardToWorkingDay(date: LocalDate): LocalDate
}

package com.example.smmoney.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles US Federal Reserve and statutory federal holidays and banking day calculations.
 */
class USFederalHolidayCalendar(
    private val includeInaugurationDay: Boolean = false
) : HolidayCalendar {

    // Cache computed holidays per year to avoid repeated date calculations
    private val holidayCache = ConcurrentHashMap<Int, Set<LocalDate>>()

    // Historical or one-off national days of mourning / emergency closures
    private val adHocHolidays = setOf(
        LocalDate.of(2018, 12, 5) // National Day of Mourning for President George H.W. Bush
    )

    /**
     * Returns true if the date is a Monday-Friday working day and not a Federal Reserve banking holiday.
     */
    override fun isWorkingDay(date: LocalDate): Boolean {
        return (date.dayOfWeek != DayOfWeek.SATURDAY) &&
            (date.dayOfWeek != DayOfWeek.SUNDAY) &&
            !isBankHoliday(date)
    }

    /**
     * Returns true if the date is an observed US Federal Holiday.
     */
    override fun isBankHoliday(date: LocalDate): Boolean {
        val yearHolidays = holidayCache.computeIfAbsent(date.year) { calculateHolidaysForYear(it) }
        return yearHolidays.contains(date)
    }

    /**
     * Resolves the n-th working day of a given month and year.
     */
    override fun getNthWorkingDayOfMonth(year: Int, month: Int, n: Int): LocalDate {
        require(n > 0) { "Working day index must be greater than 0" }
        var date = LocalDate.of(year, month, 1)
        var count = 0

        while (date.monthValue == month) {
            if (isWorkingDay(date)) {
                count++
                if (count == n) return date
            }
            date = date.plusDays(1)
        }

        throw IllegalArgumentException("Month $month in year $year has fewer than $n working days.")
    }

    /**
     * Rolls a date forward to the next available working day if it falls on a weekend or holiday.
     */
    override fun rollForwardToWorkingDay(date: LocalDate): LocalDate {
        var current = date
        while (!isWorkingDay(current)) {
            current = current.plusDays(1)
        }
        return current
    }

    /**
     * Rolls a date backward to the previous available working day if it falls on a weekend or holiday.
     */
    override fun rollBackwardToWorkingDay(date: LocalDate): LocalDate {
        var current = date
        while (!isWorkingDay(current)) {
            current = current.minusDays(1)
        }
        return current
    }

    /**
     * Calculates all observed US federal banking holidays for a specific year.
     */
    fun calculateHolidaysForYear(year: Int): Set<LocalDate> {
        val holidays = mutableSetOf<LocalDate>()

        // 1. New Year's Day (Jan 1) - Note: If Jan 1 is a Saturday then 31 Dec of prior year is observed.
        holidays.add(adjustForFederalObservation(LocalDate.of(year, 1, 1)))

        // Also check if the NEXT year's New Year's Day lands on a Saturday (falls back to Dec 31 of THIS year)
        val nextYearNewYear = LocalDate.of(year + 1, 1, 1)
        if (nextYearNewYear.dayOfWeek == DayOfWeek.SATURDAY) {
            holidays.add(LocalDate.of(year, 12, 31))
        }

        // 2. Martin Luther King Jr. Day (Third Monday in January)
        holidays.add(LocalDate.of(year, 1, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY)))

        // 3. Optional: Inauguration Day (Every 4 years on Jan 20; if Sunday, observed on Monday Jan 21)
        if (includeInaugurationDay && (year - 1789) % 4 == 0) {
            val inauguration = LocalDate.of(year, 1, 20)
            val observed = if (inauguration.dayOfWeek == DayOfWeek.SUNDAY) inauguration.plusDays(1) else inauguration
            holidays.add(observed)
        }

        // 4. Washington's Birthday / Presidents' Day (Third Monday in February)
        holidays.add(LocalDate.of(year, 2, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY)))

        // 5. Memorial Day (Last Monday in May)
        holidays.add(LocalDate.of(year, 5, 1).with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY)))

        // 6. Juneteenth National Independence Day (June 19 - Statutory since 2021)
        if (year >= 2021) {
            holidays.add(adjustForFederalObservation(LocalDate.of(year, 6, 19)))
        }

        // 7. Independence Day (July 4)
        holidays.add(adjustForFederalObservation(LocalDate.of(year, 7, 4)))

        // 8. Labor Day (First Monday in September)
        holidays.add(LocalDate.of(year, 9, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)))

        // 9. Columbus Day / Indigenous Peoples' Day (Second Monday in October)
        holidays.add(LocalDate.of(year, 10, 1).with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.MONDAY)))

        // 10. Veterans Day (November 11)
        holidays.add(adjustForFederalObservation(LocalDate.of(year, 11, 11)))

        // 11. Thanksgiving Day (Fourth Thursday in November)
        holidays.add(LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY)))

        // 12. Christmas Day (December 25)
        holidays.add(adjustForFederalObservation(LocalDate.of(year, 12, 25)))

        // Add any known historical ad-hoc holidays
        adHocHolidays.filter { it.year == year }.forEach { holidays.add(it) }

        return holidays
    }

    /**
     * Standard US Federal 5 U.S.C. 6103 observation rule:
     * - Saturday holidays move to preceding Friday.
     * - Sunday holidays move to following Monday.
     */
    private fun adjustForFederalObservation(date: LocalDate): LocalDate {
        return when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.minusDays(1)
            DayOfWeek.SUNDAY -> date.plusDays(1)
            else -> date
        }
    }
}
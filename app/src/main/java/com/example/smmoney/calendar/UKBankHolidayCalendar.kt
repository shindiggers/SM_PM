package com.example.smmoney.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles England and Wales statutory bank holidays and working day calculations.
 */
class UKBankHolidayCalendar {

    // Cache computed bank holidays per year to avoid redundant calculation
    private val holidayCache = ConcurrentHashMap<Int, Set<LocalDate>>()

    // Historical/one-off statutory bank holidays in the UK
    private val adHocHolidays = setOf(
        LocalDate.of(2022, 6, 3),  // Queen's Platinum Jubilee
        LocalDate.of(2022, 9, 19), // State Funeral of Queen Elizabeth II
        LocalDate.of(2023, 5, 8)   // Coronation of King Charles III
    )

    /**
     * Returns true if the date is a Monday-Friday working day and not a Bank Holiday.
     */
    fun isWorkingDay(date: LocalDate): Boolean {
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
            return false
        }
        return !isBankHoliday(date)
    }

    /**
     * Returns true if the date is an England & Wales statutory bank holiday.
     */
    fun isBankHoliday(date: LocalDate): Boolean {
        val yearHolidays = holidayCache.computeIfAbsent(date.year) { calculateHolidaysForYear(it) }
        return yearHolidays.contains(date)
    }

    /**
     * Resolves the n-th working day of a given month and year (e.g. 15th working day for Barclaycard).
     */
    fun getNthWorkingDayOfMonth(year: Int, month: Int, n: Int): LocalDate {
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
     * Rolls a date to the next available working day if it falls on a weekend or holiday.
     */
    fun rollForwardToWorkingDay(date: LocalDate): LocalDate {
        var current = date
        while (!isWorkingDay(current)) {
            current = current.plusDays(1)
        }
        return current
    }

    /**
     * Rolls a date to the previous available working day if it falls on a weekend or holiday.
     */
    fun rollBackwardToWorkingDay(date: LocalDate): LocalDate {
        var current = date
        while (!isWorkingDay(current)) {
            current = current.minusDays(1)
        }
        return current
    }

    /**
     * Calculates all standard bank holidays for England and Wales for a specific year.
     */
    fun calculateHolidaysForYear(year: Int): Set<LocalDate> {
        val holidays = mutableSetOf<LocalDate>()

        // 1. New Year's Day (Substitute day to Monday if Jan 1 falls on Sat/Sun)
        val newYearsDay = LocalDate.of(year, 1, 1)
        holidays.add(getSubstituteDate(newYearsDay))

        // 2. Easter Calculations (Good Friday & Easter Monday)
        val easterSunday = calculateEasterSunday(year)
        val goodFriday = easterSunday.minusDays(2)
        val easterMonday = easterSunday.plusDays(1)
        holidays.add(goodFriday)
        holidays.add(easterMonday)

        // 3. Early May Bank Holiday (First Monday in May)
        // Exception: In 2020 it was moved to Friday, May 8 for VE Day 75th Anniversary
        val earlyMayHoliday = if (year == 2020) {
            LocalDate.of(2020, 5, 8)
        } else {
            LocalDate.of(year, 5, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
        }
        holidays.add(earlyMayHoliday)

        // 4. Spring Bank Holiday (Last Monday in May)
        // Exception: 2022 moved to Thursday 2 June for the Queen's Platinum Jubilee
        val springHoliday = if (year == 2022) {
            LocalDate.of(2022, 6, 2)
        } else {
            LocalDate.of(year, 5, 1).with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY))
        }
        holidays.add(springHoliday)

        // 5. Summer Bank Holiday (Last Monday in August)
        val summerHoliday = LocalDate.of(year, 8, 1).with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY))
        holidays.add(summerHoliday)

        // 6. Christmas Day & Boxing Day (with dual substitute resolution)
        val christmasDay = LocalDate.of(year, 12, 25)
        val boxingDay = LocalDate.of(year, 12, 26)

        when (// Saturday Christmas, Sunday Boxing Day -> Mon 27 & Tue 28 are substitute holidays
            christmasDay.dayOfWeek) {
            DayOfWeek.SATURDAY -> {
                holidays.add(LocalDate.of(year, 12, 27))
                holidays.add(LocalDate.of(year, 12, 28))
            }
            // Sunday Christmas, Monday Boxing Day -> Mon 26 (Boxing) & Tue 27 (Substituted Christmas)
            DayOfWeek.SUNDAY -> {
                holidays.add(LocalDate.of(year, 12, 26))
                holidays.add(LocalDate.of(year, 12, 27))
            }
            // Friday Christmas, Saturday Boxing Day -> Fri 25 (Christmas) & Mon 28 (Substituted Boxing)
            DayOfWeek.FRIDAY -> {
                holidays.add(christmasDay)
                holidays.add(LocalDate.of(year, 12, 28))
            }
            // Normal weekday occurrences
            else -> {
                holidays.add(christmasDay)
                holidays.add(boxingDay)
            }
        }

        // Add any known ad-hoc statutory holidays for this year
        adHocHolidays.filter { it.year == year }.forEach { holidays.add(it) }

        return holidays
    }

    /**
     * Shifts a fixed-date holiday to Monday if it falls on a Saturday or Sunday.
     */
    private fun getSubstituteDate(date: LocalDate): LocalDate {
        return when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.plusDays(2)
            DayOfWeek.SUNDAY -> date.plusDays(1)
            else -> date
        }
    }

    /**
     * Computes Western Easter Sunday using the Meeus/Jones/Butcher algorithm.
     */
    private fun calculateEasterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1

        return LocalDate.of(year, month, day)
    }
}

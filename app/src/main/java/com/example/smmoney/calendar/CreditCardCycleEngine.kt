package com.example.smmoney.calendar

import java.time.DayOfWeek
import java.time.LocalDate

enum class WeekendRollPolicy {
    NEXT_WORKING_DAY,        // Standard BACS roll
    PREVIOUS_WORKING_DAY,    // Conservative early pull
    FRIDAY_PULLS_TO_THURSDAY // Barclaycard-specific observed behavior
}

sealed class StatementCycleRule {
    data class WorkingDayOfMonth(val targetWorkingDay: Int) : StatementCycleRule()
    data class FixedCalendarDay(val dayOfMonth: Int) : StatementCycleRule()
    data class WorkingDaysBeforeDue(val daysPrior: Int) : StatementCycleRule()

    // Used for simpler serialization/templates in UI
    companion object {
        fun fromString(str: String): StatementCycleRule? {
            if (str.isEmpty()) return null
            val parts = str.split(":")
            if (parts.size != 2) return null
            return try {
                when (parts[0]) {
                    "WorkingDayOfMonth" -> WorkingDayOfMonth(parts[1].toInt())
                    "FixedCalendarDay" -> FixedCalendarDay(parts[1].toInt())
                    "WorkingDaysBeforeDue" -> WorkingDaysBeforeDue(parts[1].toInt())
                    else -> null
                }
            } catch (_: NumberFormatException) {
                null
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            is WorkingDayOfMonth -> "WorkingDayOfMonth:$targetWorkingDay"
            is FixedCalendarDay -> "FixedCalendarDay:$dayOfMonth"
            is WorkingDaysBeforeDue -> "WorkingDaysBeforeDue:$daysPrior"
        }
    }
}

sealed class DueDateRule {
    data class CalendarDaysAfterStatement(val days: Int = 25) : DueDateRule()
    data class FixedDayOfMonth(val dayOfMonth: Int) : DueDateRule()

    companion object {
        fun fromString(str: String): DueDateRule? {
            if (str.isEmpty()) return null
            val parts = str.split(":")
            if (parts.size != 2) return null
            return try {
                when (parts[0]) {
                    "CalendarDaysAfterStatement" -> CalendarDaysAfterStatement(parts[1].toInt())
                    "FixedDayOfMonth" -> FixedDayOfMonth(parts[1].toInt())
                    else -> null
                }
            } catch (_: NumberFormatException) {
                null
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            is CalendarDaysAfterStatement -> "CalendarDaysAfterStatement:$days"
            is FixedDayOfMonth -> "FixedDayOfMonth:$dayOfMonth"
        }
    }
}

data class PaymentSchedule(
    val statementDate: LocalDate,
    val contractualDueDate: LocalDate,
    val cashflowDebitDate: LocalDate
)

data class CreditCardScheduleConfig(
    val statementRule: StatementCycleRule,
    val dueDateRule: DueDateRule,
    val weekendPolicy: WeekendRollPolicy,
    val holidayCalendar: HolidayCalendar = HolidayCalendarFactory.getCalendarForJurisdiction()
) {
    fun calculateNextPaymentDate(referenceDate: LocalDate): PaymentSchedule {
        val statementDate = resolveStatementDate(referenceDate)
        val contractualDueDate = resolveDueDate(statementDate)
        val debitDate = resolveDirectDebitDate(contractualDueDate)

        return PaymentSchedule(
            statementDate = statementDate,
            contractualDueDate = contractualDueDate,
            cashflowDebitDate = debitDate
        )
    }

    private fun resolveStatementDate(referenceDate: LocalDate): LocalDate {
        return when (statementRule) {
            is StatementCycleRule.FixedCalendarDay -> {
                val day = statementRule.dayOfMonth
                var possibleDate = LocalDate.of(referenceDate.year, referenceDate.month, day.coerceAtMost(referenceDate.month.length(referenceDate.isLeapYear)))
                if (possibleDate.isBefore(referenceDate)) {
                    val nextMonth = referenceDate.plusMonths(1)
                    possibleDate = LocalDate.of(nextMonth.year, nextMonth.month, day.coerceAtMost(nextMonth.month.length(nextMonth.isLeapYear)))
                }
                possibleDate
            }
            is StatementCycleRule.WorkingDayOfMonth -> {
                var possibleDate = holidayCalendar.getNthWorkingDayOfMonth(referenceDate.year, referenceDate.monthValue, statementRule.targetWorkingDay)
                if (possibleDate.isBefore(referenceDate)) {
                    val nextMonth = referenceDate.plusMonths(1)
                    possibleDate = holidayCalendar.getNthWorkingDayOfMonth(nextMonth.year, nextMonth.monthValue, statementRule.targetWorkingDay)
                }
                possibleDate
            }
            is StatementCycleRule.WorkingDaysBeforeDue -> {
                // Not perfectly supported with arbitrary Due Dates, assuming S+25 is the norm.
                // In US S-offset, you usually anchor to the due date. Let's do a simple offset backwards.
                val baseDue = resolveDueDate(referenceDate) // Approximate base on current month
                var statement = baseDue
                var count = 0
                while (count < statementRule.daysPrior) {
                    statement = statement.minusDays(1)
                    if (holidayCalendar.isWorkingDay(statement)) {
                        count++
                    }
                }
                if (statement.isBefore(referenceDate)) {
                    val nextMonthDue = baseDue.plusMonths(1)
                    statement = nextMonthDue
                    count = 0
                    while (count < statementRule.daysPrior) {
                        statement = statement.minusDays(1)
                        if (holidayCalendar.isWorkingDay(statement)) {
                            count++
                        }
                    }
                }
                statement
            }
        }
    }

    private fun resolveDueDate(statementDate: LocalDate): LocalDate {
        return when (dueDateRule) {
            is DueDateRule.CalendarDaysAfterStatement -> {
                statementDate.plusDays(dueDateRule.days.toLong())
            }
            is DueDateRule.FixedDayOfMonth -> {
                val day = dueDateRule.dayOfMonth
                // Assuming due date is next occurrence of this day after statement
                var possibleDate = LocalDate.of(statementDate.year, statementDate.month, day.coerceAtMost(statementDate.month.length(statementDate.isLeapYear)))
                if (!possibleDate.isAfter(statementDate)) {
                    val nextMonth = statementDate.plusMonths(1)
                    possibleDate = LocalDate.of(nextMonth.year, nextMonth.month, day.coerceAtMost(nextMonth.month.length(nextMonth.isLeapYear)))
                }
                possibleDate
            }
        }
    }

    private fun resolveDirectDebitDate(dueDate: LocalDate): LocalDate {
        var debit = dueDate

        // Custom Barclaycard Friday safety pullback
        if (weekendPolicy == WeekendRollPolicy.FRIDAY_PULLS_TO_THURSDAY && debit.dayOfWeek == DayOfWeek.FRIDAY) {
            return debit.minusDays(1)
        }

        // Standard weekend/bank holiday rolls
        debit = when (weekendPolicy) {
            WeekendRollPolicy.PREVIOUS_WORKING_DAY -> holidayCalendar.rollBackwardToWorkingDay(debit)
            else -> holidayCalendar.rollForwardToWorkingDay(debit)
        }
        return debit
    }
}
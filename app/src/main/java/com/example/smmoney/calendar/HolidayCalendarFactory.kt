package com.example.smmoney.calendar

import java.util.Locale

/**
 * Factory for providing the correct HolidayCalendar based on jurisdiction.
 */
object HolidayCalendarFactory {

    /**
     * Returns a HolidayCalendar implementation for the given country code (e.g., "GB", "US").
     * If no country code is provided, it falls back to the system locale.
     */
    fun getCalendarForJurisdiction(countryCode: String? = null): HolidayCalendar {
        val targetCountry = countryCode ?: Locale.getDefault().country
        
        // Setup for future expansion:
        // return when (targetCountry.uppercase()) {
        //     "US" -> USFederalHolidayCalendar()
        //     "FR" -> FrenchBankHolidayCalendar()
        //     else -> UKBankHolidayCalendar()
        // }
        
        return UKBankHolidayCalendar()
    }
}

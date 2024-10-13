package edu.usc.nlcaceres.infectionprevention.composables.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeTest {
  @Test fun `Format time based hour and minute`() {
    // - WHEN the hour and minute are below 12, THEN the time is AM in 12-hour format
    val midnight = formattedTime(0, 0)
    assertEquals("12:00 AM", midnight)
    // - WHEN the hour and minute past 12, THEN the time is PM in 12-hour format
    val onePM = formattedTime(13, 5)
    assertEquals("1:05 PM", onePM)

    //! The func shouldn't ever get negative values or values past 23 BUT it can handle them as shown below

    // - WHEN the hour is greater than 23, THEN it will be rotated to the correct value between 0 - 23
    val rotatedOneAM = formattedTime(24, 5)
    assertEquals("1:05 AM", rotatedOneAM)
    // - Rotates every 23 hours (due to 0-index hour) so 70 is actually 1:00 AM, NOT 72
    val tripleRotatedTime = formattedTime(72, 49)
    assertEquals("3:49 AM", tripleRotatedTime)
    // - WHEN the minute is greater than 59, THEN it will be rotated to the correct value between 0 - 59
    val rotatedMinuteOneAM = formattedTime(1, 64) // - 64 becomes 5
    assertEquals("1:05 AM", rotatedMinuteOneAM)

    // - WHEN the hour is negative, THEN its value is flipped to its positive value
    val negativeHour = formattedTime(-10, 15) // - -10 becomes 10 AM
    assertEquals("10:15 AM", negativeHour)
    // - WHEN the minute is negative, THEN its value is flipped to the positive value
    val negativeMinute = formattedTime(-13, -31) // - (-13, -31) flips to 1:31 PM
    assertEquals("1:31 PM", negativeMinute)
    // - WHEN the hour and minute are negative AND exceed the expected values
    val rotatedNegative = formattedTime(-26, -99)
    // - THEN the value is flipped to positive and rotated correctly
    assertEquals("3:40 AM", rotatedNegative)
  }
  @Test fun `Format date based on UTC milliseconds from epoch`() {
    // - WHEN 0 is given, THEN the date is Jan 01 1970 (the beginning of the epoch)
    val startDate = formattedDate(0)
    assertEquals("Jan 01, 1970", startDate)

    // - WHEN a positive millisecond is given
    val anotherDate = formattedDate(1603782000000)
    // - THEN the date will be after 1970
    assertEquals("Oct 27, 2020", anotherDate) // - Oct 27, 2020 at 12:00 AM PST
    // - AND any positive millisecond from any particular date results in the same date String
    val laterInTheDate = formattedDate(1603800540000) // - Oct 27, 2020 at 5:00 PM PST
    assertEquals("Oct 27, 2020", laterInTheDate) // - STILL Oct 27 even more than halfway through the day

    // - WHEN a negative millisecond is given
    val preEpochDate = formattedDate(-1)
    // - THEN the date will be from before 1970
    assertEquals("Dec 31, 1969", preEpochDate)
  }
}
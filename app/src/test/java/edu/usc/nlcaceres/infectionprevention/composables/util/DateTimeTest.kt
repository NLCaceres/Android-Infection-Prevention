package edu.usc.nlcaceres.infectionprevention.composables.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DateTimeTest {
  @Test fun `Format time based hour and minute`() {
    // - WHEN the hour and minute are below 12, THEN the time is AM in 12-hour format
    val midnight = formattedTime(0, 0)
    assertEquals("12:00 AM", midnight)
    // - WHEN the hour and minute past 12, THEN the time is PM in 12-hour format
    val onePM = formattedTime(13, 5)
    assertEquals("1:05 PM", onePM)
    // - WHEN the hour is 23, THEN the time is 11pm, NOT 12am
    val elevenPM = formattedTime(23, 5)
    assertEquals("11:05 PM", elevenPM)

    // - WHEN the hour is over 23, THEN throw an IllegalArgumentException with the invalid hour:minute value
    val hourException = assertThrows(IllegalArgumentException::class.java) { formattedTime(24, 5) }
    assertEquals("Invalid hour:minute value of 24:05", hourException.message)
    assertThrows(IllegalArgumentException::class.java) { formattedTime(72, 49) }
    // - WHEN the hour is under 0, THEN throw an IllegalArgumentException with the invalid hour:minute value
    val negativeHour = assertThrows(IllegalArgumentException::class.java) { formattedTime(-9, 12) }
    assertEquals("Invalid hour:minute value of -9:12", negativeHour.message)

    // - WHEN the minute is over 59, THEN throw an IllegalArgumentException with the invalid hour:minute value
    val minuteException = assertThrows(IllegalArgumentException::class.java) { formattedTime(1, 64) }
    assertEquals("Invalid hour:minute value of 1:64", minuteException.message)
    assertThrows(IllegalArgumentException::class.java) { formattedTime(12, 72) }
    // - WHEN the minute is under 0, THEN throw an IllegalArgumentException with the invalid hour:minute value
    val negativeMinute = assertThrows(IllegalArgumentException::class.java) { formattedTime(9, -12) }
    assertEquals("Invalid hour:minute value of 9:-12", negativeMinute.message)

    // - WHEN BOTH the hour and minute exceed their range
    val badTime = assertThrows(IllegalArgumentException::class.java) { formattedTime(30, 61) }
    // - THEN throw an IllegalArgumentException with the invalid hour:minute value
    assertEquals("Invalid hour:minute value of 30:61", badTime.message)
    // - WHEN BOTH the hour and minute are below their range
    val negativeTime = assertThrows(IllegalArgumentException::class.java) { formattedTime(-25, -80) }
    // - THEN throw an IllegalArgumentException with the invalid hour:minute value
    assertEquals("Invalid hour:minute value of -25:-80", negativeTime.message)
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
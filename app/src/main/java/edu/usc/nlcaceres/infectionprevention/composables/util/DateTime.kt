package edu.usc.nlcaceres.infectionprevention.composables.util

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TimePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
val DatesUntilNow: SelectableDates = object: SelectableDates {
  override fun isSelectableDate(utcTimeMillis: Long): Boolean {
    val instant = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()
    val selectedDateInstant = Instant.ofEpochMilli(utcTimeMillis)
    return selectedDateInstant.isBefore(instant)
  }
  override fun isSelectableYear(year: Int): Boolean {
    val instant = Instant.now()
    return instant.atZone(ZoneId.systemDefault()).year >= year
  }
}

@OptIn(ExperimentalMaterial3Api::class)
fun formattedTime(time: TimePickerState): String {
  return formattedTime(time.hour, time.minute)
}
fun formattedTime(hour: Int, minute: Int): String {
  // Rotating the hour and minutes with "%" unfortunately doesn't work well for a few reasons
  // 1. 23 % 23 = 0 so 11pm becomes 12am... 2. The offset grows each rotation so 46 isn't 11pm, it's 10pm, etc
  // So `require()` is the perfect precondition check to get valid input OR throw an IllegalArgException
  require(hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) { "Invalid hour:minute value of $hour:${"%02d".format(minute)}" }
  val localTime = LocalTime.of(hour, minute)
  val formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
  return localTime.format(formatter)
}
@OptIn(ExperimentalMaterial3Api::class)
fun formattedDate(date: DatePickerState): String {
  return formattedDate(date.selectedDateMillis)
}
fun formattedDate(dateUtcMilli: Long?): String {
  if (dateUtcMilli == null) { return "" }
  val instant = Instant.ofEpochMilli(dateUtcMilli)
  val localDate = LocalDate.ofInstant(instant, ZoneOffset.UTC)
  val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
  return localDate.format(formatter)
}

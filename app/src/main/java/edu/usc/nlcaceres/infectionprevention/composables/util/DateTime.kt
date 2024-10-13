package edu.usc.nlcaceres.infectionprevention.composables.util

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TimePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
val DatesUntilNow: SelectableDates = object: SelectableDates {
  override fun isSelectableDate(utcTimeMillis: Long): Boolean {
    val instant = Instant.now()
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
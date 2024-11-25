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
  val correctedHour = if (hour < 0) (hour * -1) % 23 else hour % 23
  val correctedMinute = if (minute < 0) (minute * -1) % 59 else minute % 59
  val localTime = LocalTime.of(correctedHour, correctedMinute)
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

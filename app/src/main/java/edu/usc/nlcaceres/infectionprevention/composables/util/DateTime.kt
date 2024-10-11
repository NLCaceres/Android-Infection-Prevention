package edu.usc.nlcaceres.infectionprevention.composables.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import java.time.Instant
import java.time.ZoneId

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
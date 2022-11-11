package edu.usc.nlcaceres.infectionprevention

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import java.time.LocalDate

// Handles both TimePicker and DatePicker Dialog Listening assuming Time set followed by Date
class ListenerDateTimeSet(private val updateDateTime: (String) -> Unit):
  DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

  private var dateTimeString = ""
  override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
    val amOrPM = if (hour < 12) "AM" else "PM" // Always seems to return 24 hour time so to check if AM or PM
    val hourOfDay = when { // Need to handle military time
      hour > 12 -> hour - 12 // 13, 14, etc down to 1, 2, etc.
      hour == 0 -> 12 // Midnight
      else -> hour
    }
    val timeOfDay = String.format("%d:%02d", hourOfDay, minute) // Format to 12:00, 12:05 (NOT 12:5), 12:10 (NOT 12:1)

    dateTimeString = "$timeOfDay $amOrPM" // Prefer template strings to concatenation whether Java or Kotlin!

    val localDate = LocalDate.now() // MUST provide a date to make an Instant so use current localDate as a default
    // WHICH makes the template string from onDateSet a good default for ALL dateTimeStrings
    localDate.run { updateDateTime("$dateTimeString ${monthValue}/${dayOfMonth}/${year}") } // Ex: 12:34 PM 1/1/2022
    with(localDate) {
      DatePickerDialog(view.context, this@ListenerDateTimeSet, year, monthValue, dayOfMonth).show()
    } // By defaulting to current localDate above, user can still send report since its Instant was already set!
  }
  // Using LocalDate is a huge improvement over Calendar + SimpleDateFormatter
  override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
    // Month received here is 0-indexed despite no Calendar usage but day is a simple 1-indexed value
    dateTimeString = "$dateTimeString ${month+1}/$day/$year" // Just template a new string!

    updateDateTime(dateTimeString) // String should use both time and date in formatter
  }
}
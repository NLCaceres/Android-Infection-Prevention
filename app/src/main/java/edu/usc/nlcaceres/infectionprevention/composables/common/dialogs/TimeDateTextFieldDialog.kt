package edu.usc.nlcaceres.infectionprevention.composables.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import edu.usc.nlcaceres.infectionprevention.composables.common.AppOutlinedTextField
import edu.usc.nlcaceres.infectionprevention.composables.util.DatesUntilNow
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDateTextFieldDialog() {
  var isVisible by remember { mutableStateOf(false) }
  val localDateTime = LocalDateTime.now()
  val timePickerState = rememberTimePickerState(
    localDateTime.hour, localDateTime.minute, is24Hour = false
  )
  val datePickerState = rememberDatePickerState(
    localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    selectableDates = DatesUntilNow
  )
  Column {
    AppOutlinedTextField("", "Select a Time & Date", onClick = { isVisible = true })
    if (isVisible) {
      TimeDatePickerDialog(timePickerState, datePickerState, {}, {}, { isVisible = false })
    }
  }
}

@Preview(widthDp = 400, heightDp = 600, showBackground = true)
@Composable
private fun TimeDateTextFieldDialogPreview() {
  AppTheme {
    TimeDateTextFieldDialog()
  }
}
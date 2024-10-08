package edu.usc.nlcaceres.infectionprevention.composables.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import edu.usc.nlcaceres.infectionprevention.composables.common.AppOutlinedTextField
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDatePickerDialog(
  timeState: TimePickerState, dateState: DatePickerState,
  onConfirmTime: () -> Unit, onConfirmDate: () -> Unit, onDismiss: () -> Unit
) {
  var showDatePicker by remember { mutableStateOf(false) }
  if (showDatePicker) {
    AppDatePickerDialog(dateState, { onConfirmDate() }, onDismiss)
  }
  else {
    TimeInputDialog(timeState, { onConfirmTime(); showDatePicker = true }, onDismiss)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(widthDp = 400, heightDp = 600, showBackground = true)
@Composable
private fun TimeDatePickerDialogPreview() {
  var isVisible by remember { mutableStateOf(false) }
  val currentTime = Calendar.getInstance()
  val timePickerState = rememberTimePickerState(
    currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), is24Hour = false
  )
  val datePickerState = rememberDatePickerState()
  AppTheme {
    Column {
      AppOutlinedTextField("", "Foo", onClick = { isVisible = true })
      if (isVisible){
        TimeDatePickerDialog(timePickerState, datePickerState, {}, {}, { isVisible = false })
      }
    }
  }
}
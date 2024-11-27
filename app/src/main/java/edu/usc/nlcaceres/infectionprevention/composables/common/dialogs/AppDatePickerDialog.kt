package edu.usc.nlcaceres.infectionprevention.composables.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import edu.usc.nlcaceres.infectionprevention.composables.common.AppOutlinedTextField
import edu.usc.nlcaceres.infectionprevention.composables.common.buttons.NegativeButton
import edu.usc.nlcaceres.infectionprevention.composables.common.buttons.PositiveButton
import edu.usc.nlcaceres.infectionprevention.composables.util.DatesUntilNow
import edu.usc.nlcaceres.infectionprevention.composables.util.formattedDate
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
  state: DatePickerState, onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  DatePickerDialog(
    onDismiss,
    { PositiveButton({ onDateSelected(state.selectedDateMillis); onDismiss() }) },
    Modifier.then(modifier),
    { NegativeButton(onDismiss) }
  ) {
    DatePicker(state)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(widthDp = 400, heightDp = 600, showBackground = true)
@Composable
private fun AppDatePickerDialogPreview() {
  var isVisible by remember { mutableStateOf(false) }
  val localDateTime = LocalDateTime.now()
  val datePickerState = rememberDatePickerState(
    localDateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli(),
    selectableDates = DatesUntilNow
  )
  val dateStr = formattedDate(datePickerState)
  AppTheme {
    Column {
      AppOutlinedTextField(dateStr, "Foo", readOnly = true, onClick = { isVisible = true })
      if (isVisible) {
        AppDatePickerDialog(datePickerState, { isVisible = false }, { isVisible = false })
      }
    }
  }
}
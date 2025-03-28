package edu.usc.nlcaceres.infectionprevention.composables.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.composables.common.AppOutlinedTextField
import edu.usc.nlcaceres.infectionprevention.composables.common.buttons.NegativeButton
import edu.usc.nlcaceres.infectionprevention.composables.common.buttons.PositiveButton
import edu.usc.nlcaceres.infectionprevention.composables.util.formattedTime
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputDialog(state: TimePickerState, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  BasicAlertDialog(onDismiss) {
    Surface(
      Modifier.padding(horizontal = 10.dp, vertical = 0.dp), shape = RoundedCornerShape(15.dp)
    ) {
      Column {
        Text(
          "Enter time", modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 5.dp),
          style = MaterialTheme.typography.titleLarge
        )
        HorizontalDivider(Modifier.padding(start = 8.dp, bottom = 15.dp, end = 8.dp))
        TimeInput(state, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), appTimePickerColors())
        Row(Modifier.fillMaxWidth().padding(end = 10.dp, bottom = 5.dp), Arrangement.End) {
          NegativeButton(onDismiss)
          PositiveButton(onConfirm)
        }
      }
    }
  }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appTimePickerColors(): TimePickerColors {
  //TODO: `timeSelector` doesn't currently let its border change, even though `periodSelector` can
  return TimePickerDefaults.colors(
    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(widthDp = 320, heightDp = 500, showBackground = true)
@Composable
private fun TimeInputDialogPreview() {
  var isVisible by remember { mutableStateOf(false) }
  val localDateTime = LocalDateTime.now()
  val timePickerState = rememberTimePickerState(
    localDateTime.hour, localDateTime.minute, is24Hour = false
  )
  val timeStr = formattedTime(timePickerState)
  AppTheme {
    Column {
      AppOutlinedTextField(timeStr, "Foo", readOnly = true)
      if (isVisible) {
        TimeInputDialog(timePickerState, onConfirm = { isVisible = false }, onDismiss = { isVisible = false })
      }
    }
  }
}
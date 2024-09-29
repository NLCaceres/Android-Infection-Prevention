package edu.usc.nlcaceres.infectionprevention.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseTimeDateWidget() {
  val currentTime = Calendar.getInstance()
  val timePickerState = rememberTimePickerState(
    currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), is24Hour = false
  )
  Box(Modifier.fillMaxSize()) {
    Surface(
      Modifier.align(Alignment.Center).padding(horizontal = 10.dp), shape = RoundedCornerShape(15.dp)
    ) {
      Column(Modifier.padding(5.dp)) {
        Text("Enter time", modifier = Modifier.padding(start = 12.dp, top = 5.dp, bottom = 10.dp))
        TimeInput(timePickerState, Modifier.padding(10.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.End) {
          Button(
            onClick = {}, Modifier.padding(end = 10.dp),
            shape = RoundedCornerShape(7.dp),
            contentPadding = PaddingValues(15.dp, 5.dp)
          ) {
            Text("Cancel")
          }
          Button(
            onClick = {}, Modifier.padding(end = 10.dp),
            shape = RoundedCornerShape(7.dp),
            contentPadding = PaddingValues(15.dp, 5.dp)
          ) {
            Text("OK")
          }
        }
      }
    }
  }
}

@Preview(widthDp = 320, heightDp = 500, showBackground = true)
@Composable
private fun TimeDateWidgetPreview() {
  AppTheme {
    Box {
      Column {
        AppOutlinedTextField("", "Foo")

      }
      UseTimeDateWidget()
    }

  }
}
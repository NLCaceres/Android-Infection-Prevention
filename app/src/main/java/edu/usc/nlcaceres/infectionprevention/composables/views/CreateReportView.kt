package edu.usc.nlcaceres.infectionprevention.composables.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.composables.common.MaterialSpinner
import edu.usc.nlcaceres.infectionprevention.composables.common.NavigableTextField
import edu.usc.nlcaceres.infectionprevention.composables.common.buttons.AppButton
import edu.usc.nlcaceres.infectionprevention.composables.common.dialogs.TimeDateTextFieldDialog
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun CreateReportView() {
  Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxWidth()) {
      Text(
        "New Observation", modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp),
        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge
      )
      TimeDateTextFieldDialog(Modifier.padding(start = 20.dp, top = 15.dp))
      MaterialSpinner("Select a Health Practice", listOf("Hand Hygiene", "Contact"), {}, Modifier.padding(start = 20.dp, top = 15.dp))
      NavigableTextField("John Smith", "Select an Employee", Modifier.padding(start = 20.dp, top = 15.dp))
      NavigableTextField("USC Unit #2 Room #123", "Select a Location", Modifier.padding(start = 20.dp, top = 15.dp))
    }
    AppButton({}, "Submit", Modifier.align(BiasAlignment(0.7f, 0.85f)))
  }
}

@Preview(widthDp = 325, heightDp = 500, showBackground = true)
@Composable
fun CreateReportViewPreview() {
  AppTheme {
    CreateReportView()
  }
}
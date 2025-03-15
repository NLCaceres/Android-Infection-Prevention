package edu.usc.nlcaceres.infectionprevention.composables.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.usc.nlcaceres.infectionprevention.composables.common.MaterialSpinner
import edu.usc.nlcaceres.infectionprevention.composables.common.NavigableTextField
import edu.usc.nlcaceres.infectionprevention.composables.common.buttons.AppButton
import edu.usc.nlcaceres.infectionprevention.composables.common.dialogs.TimeDateTextFieldDialog
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.Report
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme
import edu.usc.nlcaceres.infectionprevention.viewModels.ViewModelCreateReport
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateReportView(modifier: Modifier = Modifier, viewModel: ViewModelCreateReport = viewModel()) {
  val report = viewModel.newReport()
  val headerText by viewModel.healthPracticeHeaderText.observeAsState("")
  val adapterData by viewModel.adapterData.observeAsState(
    Triple(emptyList<Employee>(), emptyList<HealthPractice>(), emptyList<Location>())
  )
  CreateReportView(
    headerText, adapterData.second, report, modifier.then(Modifier.testTag("CreateReportView")),
    viewModel::updateDate, viewModel::updateHealthPractice, viewModel::submitReport
  )
}

@Composable
fun CreateReportView(
  headerText: String, healthPractices: List<HealthPractice>, report: Report, modifier: Modifier = Modifier,
  onTimeDateChange: (String) -> Unit, onHealthPracticeSelect: (HealthPractice) -> Unit, onSubmit: () -> Unit
) {
  val employeeName = report.employee?.fullName ?: ""
  val locationName = report.location?.toString() ?: ""
  Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxWidth().then(modifier)) {
      Text(
        headerText, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp),
        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge
      )
      TimeDateTextFieldDialog(onTimeDateChange, onTimeDateChange, Modifier.padding(start = 20.dp, top = 15.dp))
      MaterialSpinner(
        "Select a Health Practice", healthPractices,
        { _, healthPractice -> onHealthPracticeSelect(healthPractice) },
        Modifier.padding(start = 20.dp, top = 15.dp)
      )
      NavigableTextField(employeeName, "Select an Employee", Modifier.padding(start = 20.dp, top = 15.dp))
      NavigableTextField(locationName, "Select a Location", Modifier.padding(start = 20.dp, top = 15.dp))
    }
    AppButton(onSubmit, "Submit", Modifier.align(BiasAlignment(0.7f, 0.85f)))
  }
}

@Preview(widthDp = 325, heightDp = 500, showBackground = true)
@Composable
fun CreateReportViewPreview() {
  var headerStr by remember { mutableStateOf("Some Observation") }
  val healthPractices = listOf(HealthPractice(null, "Foo", null), HealthPractice(null, "Bar", null))
  // Why no `remember` or `mutableStateOf` for `report`? `report.copy` almost always causes recomposition
  // when using them. It ONLY SKIPS if the new Report is structurally equivalent since `copy`
  // is shallow BUT always produces a referentially different `report` instance. SO it CAN be
  // more performant to limit `State` usage if the stateful parent Screen correctly updates w/out it
  // SINCE child composables w/ params related to un-remembered state will recompose or skip anyway
  var report = Report(null, null, null, null, Instant.now())
  AppTheme {
    CreateReportView(
      headerStr, healthPractices, report, Modifier,
      {
        val formatter = DateTimeFormatter.ofPattern("h:mm a MMM dd, yyyy").withZone(ZoneId.systemDefault())
        report = report.copy(date = ZonedDateTime.parse(it, formatter).toInstant())
      },
      { headerStr = "Some ${it.name}"; report = report.copy(healthPractice = it) }
    ) {}
  }
}
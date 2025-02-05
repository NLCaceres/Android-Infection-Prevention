package edu.usc.nlcaceres.infectionprevention.viewModels

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ViewModelCreateReport @Inject constructor(private val employeeRepository: EmployeeRepository,
                                                private val healthPracticeRepository: HealthPracticeRepository,
                                                private val locationRepository: LocationRepository,
                                                private val reportRepository: ReportRepository) : ViewModel() {
  private val _loadingState = MutableLiveData(false)
  val loadingState: LiveData<Boolean> = _loadingState

  private val _selectedPracticeName = MutableLiveData("")
  val healthPracticeHeaderText = _selectedPracticeName.distinctUntilChanged().map { name ->
    if (name.isBlank()) "New Observation" else "New $name Observation"
  } // Using isBlank() above prevents "New null Observation" when spinner data is loading
  fun selectedHealthPracticeIndex(healthPracticeList: List<HealthPractice> = _healthPracticeList.value ?: emptyList(),
                                  name: String = healthPracticeHeaderText.value ?: "") =
    healthPracticeList.indexOfFirst { healthPractice -> healthPractice.name == name }

  private val _dateTimeString = MutableLiveData("")
  val dateTimeString: LiveData<String> = _dateTimeString
  fun updateDate(dateTimeString: String) {
    _dateTimeString.value = dateTimeString
    // Need single 'd' and 'M' in pattern since it is never 0-padded aka 12/01/1996 vs 12/1/1996
    val dateFormatter = DateTimeFormatter.ofPattern("h:mm a MMM dd, yyyy").withZone(ZoneId.systemDefault())
    val dateTimeInstant = ZonedDateTime.parse(dateTimeString, dateFormatter).toInstant()
    updateReport(dateTime = dateTimeInstant)
  }

  // Might NOT need employee or location liveData given flow usage BUT might be useful to cache recent data
  private val _employeeList = MutableLiveData(emptyList<Employee>())
  private val _locationList = MutableLiveData(emptyList<Location>())
  private val _healthPracticeList = MutableLiveData(emptyList<HealthPractice>())
  fun updateHealthPractice(healthPractice: HealthPractice?) {
    _selectedPracticeName.value = healthPractice?.name ?: "" // Update HeaderText
    updateReport(healthPractice = healthPractice)
  }

  // Combining values in a zip isn't quite as easy as using combine(flow1, flow2, flow3...) etc.
  // BUT doing the following works! The flows start running together onCollect in a somewhat recursive way
  // It breaks into (zip1 + fetchLocation) then breaks into (fetchEmployee + fetchHealthPractice) + fetchLocation
  // The 1 major limitation: Slowest emitter delays the release of the next Triple to the collector
  private val adapterFlow = employeeRepository.fetchEmployeeList()
    .zip(healthPracticeRepository.fetchHealthPracticeList()) { employees, healthPractices -> Pair(employees, healthPractices) }
    .zip(locationRepository.fetchLocationList()) { (employeeList, healthPracticeList), locationList ->
      _employeeList.value = employeeList
      _healthPracticeList.value = healthPracticeList
      _locationList.value = locationList
      Triple(employeeList, healthPracticeList, locationList)
    }.onStart { _loadingState.value = true }
      .onCompletion { delay(1000);  _loadingState.value = false; EspressoIdlingResource.decrement() }
      .catch { e -> _snackbarMessage.value = when (e) {
        is IOException -> "Sorry! Having trouble with the internet connection!"
        else -> "Sorry! Seems we're having an issue on our end!"
      }} // If all 3 flows fail, 1st to fail is ONLY 1 caught. All else stop. LiveData WON'T restart the flow
  val adapterData = adapterFlow.asLiveData() // EVEN if the observer is removed and added again

  private val _newReport = MutableLiveData(Report(null, null, null, null, Instant.now()))
  fun newReport() = _newReport.value!! // Should never be null since initial value set above
  fun updateReport(employee: Employee? = newReport().employee, healthPractice: HealthPractice? = newReport().healthPractice,
                   location: Location? = newReport().location, dateTime: Instant = newReport().date) {
    _newReport.value = Report(null, employee, healthPractice, location, dateTime)
  }
  fun submitReport() {
    Log.d("CreateReport", "Submitting report!")
    // TODO: Basic Validation (non-null values most likely)
    reportRepository.createReport() // Take newReport as input to submit to server
  }

  private val _snackbarMessage = MutableLiveData("")
  val snackbarMessage: LiveData<String> = _snackbarMessage
}
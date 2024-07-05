package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.Observer
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildEmployee
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildHealthPractice
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildLocation
import edu.usc.nlcaceres.infectionprevention.helpers.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.mockito.quality.Strictness
import java.io.IOException
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ViewModelCreateReportTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()
  @get:Rule
  var executorRule = InstantTaskExecutorRule()

  private lateinit var employeeRepository: EmployeeRepository
  private lateinit var healthPracticeRepository: HealthPracticeRepository
  private lateinit var locationRepository: LocationRepository
  private lateinit var reportRepository: ReportRepository

  @Mock lateinit var loadingObserver: Observer<Boolean>
  @Mock lateinit var headerTextObserver: Observer<String>
  @Mock lateinit var adapterDataObserver: Observer<Triple<*,*,*>>

  @Test fun `Check Loading State as Repositories Flow`() { // Check loading state and final zipped Triple
    val employeeList = listOf(buildEmployee(), buildEmployee())
    val healthPracticeList = listOf(buildHealthPractice())
    val locationList = listOf(buildLocation(), buildLocation(), buildLocation())
    employeeRepository = mock { on { fetchEmployeeList() } doReturn flow { emit(employeeList) } }
    healthPracticeRepository = mock { on { fetchHealthPracticeList() } doReturn flow { emit(healthPracticeList) } }
    locationRepository = mock { on { fetchLocationList() } doReturn flow { emit(locationList) } }
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)
    assertFalse(viewModel.loadingState.value!!) // Should default to false

    viewModel.loadingState.observeForever(loadingObserver)
    viewModel.adapterData.observeForever(adapterDataObserver) // Starts the loadingState
    viewModel.adapterData.removeObserver(adapterDataObserver)
    viewModel.loadingState.removeObserver(loadingObserver)

    verify(loadingObserver, times(3)).onChanged(any())
    val inOrderChecker = inOrder(loadingObserver)
    inOrderChecker.verify(loadingObserver, times(1)).onChanged(false) // Defaults to false
    inOrderChecker.verify(loadingObserver, times(1)).onChanged(true) // onStart sets it true
    inOrderChecker.verify(loadingObserver, times(1)).onChanged(false) // onComplete sets it back to false

    verify(adapterDataObserver, times(1)).onChanged(any())
    val expectedTriple = Triple(employeeList, healthPracticeList, locationList)
    verify(adapterDataObserver, times(1)).onChanged(expectedTriple)
  }
  @Test fun `Observe Repositories Failed with Basic Exception`() {
    employeeRepository = mock { on { fetchEmployeeList() } doReturn flow { throw Exception("Failed Employees") } }
    healthPracticeRepository = mock { on { fetchHealthPracticeList() } doReturn flow { emit(emptyList()) } }
    locationRepository = mock { on { fetchLocationList() } doReturn flow { emit(emptyList()) } }
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)

    viewModel.adapterData.observeForever(adapterDataObserver)
    viewModel.adapterData.removeObserver(adapterDataObserver)
    val defaultSnackbarErrorMessage = "Sorry! Seems we're having an issue on our end!"
    assertEquals(defaultSnackbarErrorMessage, viewModel.snackbarMessage.value)
  }
  @Test fun `Observe Repositories All Failed`() {
    employeeRepository = mock { on { fetchEmployeeList() } doReturn flow { throw Exception("Failed Employees") } }
    healthPracticeRepository = mock { on { fetchHealthPracticeList() } doReturn flow { throw Exception("Failed Employees") } }
    locationRepository = mock { on { fetchLocationList() } doReturn flow { throw Exception("Failed Employees") } }
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)

    viewModel.adapterData.observeForever(adapterDataObserver)
    viewModel.adapterData.removeObserver(adapterDataObserver)
    val defaultSnackbarErrorMessage = "Sorry! Seems we're having an issue on our end!"
    assertEquals(defaultSnackbarErrorMessage, viewModel.snackbarMessage.value)
  }
  @Test fun `Observe Repositories Failed with IO Exception`() {
    employeeRepository = mock { on { fetchEmployeeList() } doReturn flow { emit(emptyList()) } }
    healthPracticeRepository = mock { on { fetchHealthPracticeList() } doReturn flow { throw IOException("Failed Health Practices") } }
    locationRepository = mock { on { fetchLocationList() } doReturn flow { emit(emptyList()) } }
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)

    viewModel.adapterData.observeForever(adapterDataObserver)
    viewModel.adapterData.removeObserver(adapterDataObserver)
    val snackbarIOErrorMessage = "Sorry! Having trouble with the internet connection!"
    assertEquals(snackbarIOErrorMessage, viewModel.snackbarMessage.value)
  }

  @Test fun `Check and Update Selected Health Practice`() { // HeaderText + Report's HealthPractice
    employeeRepository = mock()
    healthPracticeRepository = mock()
    locationRepository = mock()
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)
    //? WHEN LiveData has an initial value (an empty string here), THEN its first value will never be null
    // SINCE the healthPracticeHeader LiveData checks for empty strings, the first value is the following
    assertEquals("New Observation", viewModel.healthPracticeHeaderText.value)

    viewModel.healthPracticeHeaderText.observeForever(headerTextObserver)
    val defaultHeaderText = viewModel.healthPracticeHeaderText.value
    assertNotNull("Observed header text should be non-null", defaultHeaderText)
    assertNull("Report HealthPractice unexpectedly set", viewModel.newReport().healthPractice)
    assertEquals("New Observation", defaultHeaderText)

    val newHealthPractice = HealthPractice(null, "PPE", null)
    viewModel.updateHealthPractice(newHealthPractice)
    assertEquals("New PPE Observation", viewModel.healthPracticeHeaderText.value)
    assertNotNull("Report HealthPractice unexpectedly not updated", viewModel.newReport().healthPractice)
    assertEquals(newHealthPractice, viewModel.newReport().healthPractice)

    viewModel.updateHealthPractice(null)
    assertEquals("New Observation", viewModel.healthPracticeHeaderText.value)
    assertNull("Report HealthPractice unexpectedly non-null", viewModel.newReport().healthPractice)
    assertEquals(null, viewModel.newReport().healthPractice)

    viewModel.healthPracticeHeaderText.removeObserver(headerTextObserver)
  }

  @Test fun `Find Selected HealthPractice Index`() { // Grab HealthPractice index based on expected name
    val emittedHealthPracticeList = listOf(HealthPractice(null, "Droplet", null),
      buildHealthPractice(), buildHealthPractice(), buildHealthPractice())
    // Since the repos are zipped, they MUST ALL emit a list to make 1 Triple(empty, healthPracticeList, empty)
    employeeRepository = mock { on { fetchEmployeeList() } doReturn flow { emit(emptyList()) } }
    healthPracticeRepository = mock { on { fetchHealthPracticeList() } doReturn flow { emit(emittedHealthPracticeList) } }
    locationRepository = mock { on { fetchLocationList() } doReturn flow { emit(emptyList()) } }
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)

    assertFalse(viewModel.adapterData.hasActiveObservers()) // No observers makes healthPracticeList default to empty
    val noIndexFound = viewModel.selectedHealthPracticeIndex(name = "Droplet")
    assertEquals(-1, noIndexFound) // An emptyList returns -1 upon indexOf search

    viewModel.adapterData.observeForever(adapterDataObserver)
    assert(viewModel.adapterData.hasActiveObservers()) // Now observing so emitted healthPracticeList available!
    val indexFound = viewModel.selectedHealthPracticeIndex(name = "Droplet")
    assertEquals(0, indexFound)

    val healthPracticeList = listOf(buildHealthPractice(), buildHealthPractice(), buildHealthPractice(),
      HealthPractice(null, "PPE", null))
    val lastIndexFound = viewModel.selectedHealthPracticeIndex(healthPracticeList, "PPE")
    assertEquals(3, lastIndexFound)

    val unknownHealthPracticeName = "Unknown health practice"
    val missingIndex = viewModel.selectedHealthPracticeIndex(name = unknownHealthPracticeName)
    assertEquals(-1, missingIndex) // If the healthPractice doesn't exist then -1 is guaranteed!
    val missingIndex2 = viewModel.selectedHealthPracticeIndex(healthPracticeList, unknownHealthPracticeName)
    assertEquals(-1, missingIndex2)

    viewModel.adapterData.removeObserver(adapterDataObserver)
  }

  @Test fun `Check and Update Date with String and Parser`() { // Date Edit Text + Report's Instant
    employeeRepository = mock()
    healthPracticeRepository = mock()
    locationRepository = mock()
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)
    val dateTimeInstant = Instant.now().truncatedTo(ChronoUnit.MINUTES)
    assertEquals("", viewModel.dateTimeString.value!!)
    assertNotNull("ViewModel Report Date not set!", viewModel.newReport().date)
    val viewModelDate = viewModel.newReport().date.truncatedTo(ChronoUnit.MINUTES)
    assertEquals(dateTimeInstant, viewModelDate) // Since only need time precision to minutes, these instants should now be equal

    val dateTimeStringAfternoon = "12:34 PM 1/1/2011"
    viewModel.updateDate(dateTimeStringAfternoon)
    assertEquals(dateTimeStringAfternoon, viewModel.dateTimeString.value!!)
    assertNotNull("ViewModel Report Date not set!", viewModel.newReport().date)
    val viewModelDate2 = viewModel.newReport().date.atZone(ZoneId.systemDefault())
    assertEquals(12, viewModelDate2.hour)
    assertEquals(34, viewModelDate2.minute)
    assertEquals(1, viewModelDate2.monthValue)
    assertEquals(1, viewModelDate2.dayOfMonth)
    assertEquals(2011, viewModelDate2.year)

    val dateTimeStringNight = "8:08 PM 5/15/2015"
    viewModel.updateDate(dateTimeStringNight)
    assertEquals(dateTimeStringNight, viewModel.dateTimeString.value!!)
    assertNotNull("ViewModel Report Date not set!", viewModel.newReport().date)
    val viewModelDate3 = viewModel.newReport().date.atZone(ZoneId.systemDefault())
    assertEquals(20, viewModelDate3.hour)
    assertEquals(8, viewModelDate3.minute)
    assertEquals(5, viewModelDate3.monthValue)
    assertEquals(15, viewModelDate3.dayOfMonth)
    assertEquals(2015, viewModelDate3.year)

    val dateTimeStringMidnight = "12:34 AM 10/11/2020"
    viewModel.updateDate(dateTimeStringMidnight)
    assertEquals(dateTimeStringMidnight, viewModel.dateTimeString.value!!)
    assertNotNull("ViewModel Report Date not set!", viewModel.newReport().date)
    val viewModelDate4 = viewModel.newReport().date.atZone(ZoneId.systemDefault())
    assertEquals(0, viewModelDate4.hour) // In 24-hour mode, midnight == 0
    assertEquals(34, viewModelDate4.minute)
    assertEquals(10, viewModelDate4.monthValue)
    assertEquals(11, viewModelDate4.dayOfMonth)
    assertEquals(2020, viewModelDate4.year)
  }

  @Test fun `Check and Update NewReport Value`() {
    employeeRepository = mock()
    healthPracticeRepository = mock()
    locationRepository = mock()
    reportRepository = mock()
    val viewModel = ViewModelCreateReport(employeeRepository, healthPracticeRepository, locationRepository, reportRepository)
    val viewModelReport = viewModel.newReport()
    // Instant and HealthPractice defaults tested w/ updateDate above
    assertNotNull("ViewModel Report not set!", viewModelReport)
    assertEquals(null, viewModelReport.id)
    assertEquals(null, viewModelReport.employee)
    assertEquals(null, viewModelReport.location)

    val newEmployee = buildEmployee()
    viewModel.updateReport(employee = newEmployee)
    val newReportWithEmployee = viewModel.newReport()
    assertNotEquals(viewModelReport, newReportWithEmployee) // LiveData gets a whole new Report object!
    assertNotEquals(newEmployee, viewModelReport.employee) // So employee field also not equal!
    assertEquals(newEmployee, newReportWithEmployee.employee)
    assertEquals(null, newReportWithEmployee.id) // BUT ID and location values remain equal to previous's null values
    assertEquals(null, newReportWithEmployee.location)

    val newLocation = buildLocation()
    viewModel.updateReport(location = newLocation)
    val newReportWithLocation = viewModel.newReport()
    assertNotEquals(newReportWithEmployee, newReportWithLocation) // Previous location different than new location
    assertNotEquals(newReportWithEmployee.location, newReportWithLocation.location)
    assertEquals(newEmployee, newReportWithLocation.employee) // BUT same employee value as before!
    assertEquals(null, newReportWithLocation.id) // BUT ID remains equal to previous's null values
    assertEquals(newLocation, newReportWithLocation.location)

    val anotherEmployee = buildEmployee()
    viewModel.updateReport(employee = anotherEmployee)
    val newReportWithNewEmployee = viewModel.newReport()
    assertNotEquals(newReportWithLocation, newReportWithNewEmployee)
    assertNotEquals(newEmployee, newReportWithNewEmployee.employee) // New employee value!
    assertEquals(anotherEmployee, newReportWithNewEmployee.employee) // Got a match with new employee value
    assertEquals(newLocation, newReportWithNewEmployee.location)

    val finalEmployee = buildEmployee()
    val finalLocation = buildLocation()
    viewModel.updateReport(employee = finalEmployee, location = finalLocation)
    val finalReport = viewModel.newReport()
    assertNotEquals(newReportWithNewEmployee, finalReport)
    assertNotEquals(anotherEmployee, finalReport.employee) // Previous value not the new value
    assertEquals(finalEmployee, finalReport.employee) // Got a match with new employee value
    assertNotEquals(newLocation, finalReport.location) // Previous location also no longer equal
    assertEquals(finalLocation, finalReport.location) // New location value too!

    // ID not tested since it can't be set or affected by user input
  }
}
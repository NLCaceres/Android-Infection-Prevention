package edu.usc.nlcaceres.infectionprevention.domain

import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildLocation
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildEmployee
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildReport
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildHealthPractice
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.TimeZone

class SortFilterReportsUseCaseTest {
  private lateinit var sortFilterListUseCase: SortFilterReportsUseCase
  @Before
  fun `Setup Use Case`() {
    sortFilterListUseCase = SortFilterReportsUseCase()
  }
  // Sort Reports Function
  @Test fun `Sort Reports by Date`() {
    val dateString = "2019-05-19T06:36:05.018Z"
    val timestamp = Instant.parse(dateString)

    val report1 = buildReport(Employee(null, "Arthur", "Mac", null), date = timestamp)
    val date2 = timestamp.plus(1, ChronoUnit.DAYS) // Day after
    val report2 = buildReport(Employee(null, "Arthur", "Mac", null), date = date2)
    val date3 = timestamp.plus(1, ChronoUnit.DAYS) // Day after AGAIN
    val report3 = buildReport(Employee(null, "Arthur", "Mac", null), date = date3)
    val date4 = timestamp.plus(7, ChronoUnit.DAYS) // Week after
    val report4 = buildReport(Employee(null, "Arthur", "Mac", null), date = date4)

    val reportList = listOf(report2, report3, report4, report1) // 2, 3, 4, 1
    val unexpectedSortedList = listOf(report1, report2, report3, report4) // 1, 2, 3, 4 ---> Oldest first, recent last (ascending)
    // Since Kotlin's sortedBy is a stable sort, report2 will come before 3 because 2 came before 3 in the original!
    val expectedSortedList = listOf(report4, report2, report3, report1) // 4, 2, 3, 1 ---> Most recent to oldest (descending)
    val sortedList = sortFilterListUseCase.sortReportsBy(reportList, "Date Reported")

    assertNotEquals(reportList, sortedList)
    assertNotEquals(unexpectedSortedList, sortedList) // Oldest first, recent last (ascending)
    assertEquals(expectedSortedList, sortedList) // Most recent to oldest (descending)

    // Showcasing sortedBy's stable sort algorithm
    val reportList2 = listOf(report3, report2, report4, report1) // If 3 comes before 2 this time!
    val expectedSortedList2 = listOf(report4, report3, report2, report1) // THEN we do get a simple 4,3,2,1
    val sortedList2 = sortFilterListUseCase.sortReportsBy(reportList2, "Date Reported")

    assertNotEquals(reportList2, sortedList2) // 3,2,4,1
    assertEquals(expectedSortedList2, sortedList2) // NOW 4,3,2,1   NOT 4,2,3,1 this time
  }
  @Test fun `Sort Reports by Employee Name (A-Z)`() {
    // Can't use employee factory because it assigns employee names with numbers
    // so firstName7 < firstName10 should be right BUT is sometimes read as firstName7 < firstName1 leading to inconsistent results!
    val employee1 = buildReport(employee = Employee(null, "Arthur", "Mac", null))
    val employee2 = buildReport(employee = Employee(null, "Bea", "Mac", null))
    val employee3 = buildReport(employee = Employee(null, "Bea", "Macdonald", null))
    val employee4 = buildReport(employee = Employee(null, "Cameron", "Darnold", null))

    val reportList = listOf(employee2, employee3, employee4, employee1) // 2, 3, 4, 1
    val expectedSortedList = listOf(employee1, employee2, employee3, employee4) // 1, 2, 3, 4
    val sortedList = sortFilterListUseCase.sortReportsBy(reportList, "Employee Name (A-Z)")

    assertNotEquals(reportList, sortedList)
    assertEquals(expectedSortedList, sortedList)
  }
  @Test fun `Sort Reports by Employee Name (Z-A)`() {
    val employee1 = buildReport(employee = Employee(null, "Arthur", "Mac", null))
    val employee2 = buildReport(employee = Employee(null, "Bea", "Mac", null))
    val employee3 = buildReport(employee = Employee(null, "Bea", "Macdonald", null))
    val employee4 = buildReport(employee = Employee(null, "Cameron", "Darnold", null))

    val reportList = listOf(employee2, employee3, employee4, employee1) // 2, 3, 4, 1
    val expectedSortedList = listOf(employee4, employee3, employee2, employee1) // 4, 3, 2, 1
    val sortedList = sortFilterListUseCase.sortReportsBy(reportList, "Employee Name (Z-A)")

    assertNotEquals(reportList, sortedList)
    assertEquals(expectedSortedList, sortedList)
  }
  @Test fun `Sort Reports if Invalid Sort String with Fallback`() {
    val employee1 = buildReport()
    val employee2 = buildReport()
    val employee3 = buildReport()
    val employee4 = buildReport()
    val reportList = listOf(employee2, employee3, employee4, employee1) // 2, 3, 4, 1
    val sortedList = sortFilterListUseCase.sortReportsBy(reportList, "Foobar")

    assertEquals(reportList, sortedList) // Simply get back the original report if invalid sort string
  }

  // Filter Individual Report Function
  @Test fun `Filter Reports by Precaution Type`() {
    val filterGroup = "Precaution Type"

    val standardPrecautionReport = buildReport(healthPractice = buildHealthPractice("Standard"))
    val standardTypeFilter = "Standard"
    val keepStandardReports = sortFilterListUseCase.filterReportsBy(standardPrecautionReport, filterGroup, standardTypeFilter)
    assert(keepStandardReports) // Filter returns true

    val isolationPrecautionReport = buildReport(healthPractice = buildHealthPractice("Isolation"))
    val dontKeepIsolationReports = sortFilterListUseCase.filterReportsBy(isolationPrecautionReport, filterGroup, standardTypeFilter)
    assertFalse(dontKeepIsolationReports) // Isolation reports filtered out. Filter returns false

    val isolationTypeFilter = "Isolation"
    val keepIsolationReports = sortFilterListUseCase.filterReportsBy(isolationPrecautionReport, filterGroup, isolationTypeFilter)
    assert(keepIsolationReports) // Isolation reports now kept in!

    val dontKeepStandardReports = sortFilterListUseCase.filterReportsBy(standardPrecautionReport, filterGroup, isolationTypeFilter)
    assertFalse(dontKeepStandardReports) // Standard reports now kept out
  }
  @Test fun `Filter Reports by Health Practice Type`() {
    val filterGroup = "Health Practice Type"
    val report = buildReport(healthPractice = buildHealthPractice())
    val healthPracticeNum = report.healthPractice?.name?.takeLastWhile { it.isDigit() }?.toInt() ?: 0

    val filterName = "healthPractice$healthPracticeNum"
    val keepSameNameHealthPracticeReports = sortFilterListUseCase.filterReportsBy(report, filterGroup, filterName)
    assert(keepSameNameHealthPracticeReports)

    val filterNamePrefix = "healthPrac"
    val keepSamePrefixHealthPracticeReports = sortFilterListUseCase.filterReportsBy(report, filterGroup, filterNamePrefix)
    assert(keepSamePrefixHealthPracticeReports)

    val filterNameIgnoresCase = "HealthPractice$healthPracticeNum"
    val healthPracticeReportIgnoresCase = sortFilterListUseCase.filterReportsBy(report, filterGroup, filterNameIgnoresCase)
    assertFalse(healthPracticeReportIgnoresCase)

    val healthPractice2 = HealthPractice(null, "OtherHealthPractice0", null)
    val report2 = buildReport(healthPractice = healthPractice2)
    val keepDiffNameHealthPracticeReport = sortFilterListUseCase.filterReportsBy(report2, filterGroup, filterName)
    assertFalse(keepDiffNameHealthPracticeReport) // Different name will get filtered out and return false

    assert(sortFilterListUseCase.filterReportsBy(report2, filterGroup, "Other")) // Matching filter name so now returns true!
  }

  // Filter Reports List by Text
  @Test fun `Filter Reports by Employee Name Text`() {
    val reportToCheckEmployee = buildReport(buildEmployee()) // Default fullName == "firstName0 surname0"
    val employeeNum = reportToCheckEmployee.employee?.firstName?.takeLastWhile { it.isDigit() }?.toInt() ?: 0
    val reportToCheckDifferentEmployee = buildReport(Employee(null, "John", "Smith", null))
    val reportList = listOf(reportToCheckEmployee, reportToCheckEmployee, reportToCheckDifferentEmployee)

    val filteredListByPrefix = sortFilterListUseCase.filterReportsByText("firstName", reportList)
    assertEquals(2, filteredListByPrefix.size)

    val filteredListBySuffix = sortFilterListUseCase.filterReportsByText("surname$employeeNum", reportList)
    assertEquals(2, filteredListBySuffix.size)

    // Use a value from the middle of the string and regardless of case will get 2 matches
    val filteredListContainsIgnoresCase = sortFilterListUseCase.filterReportsByText("NAME", reportList)
    assertEquals(2, filteredListContainsIgnoresCase.size)

    val filteredListOther = sortFilterListUseCase.filterReportsByText("John", reportList)
    assertEquals(1, filteredListOther.size) // Only get differentEmployee!

    // Need to use default constructor since factory builds an Employee anyway if null passed in
    val reportWithoutEmployee = Report(null, null, buildHealthPractice(), buildLocation(), Instant.now())
    val reportList2 = listOf(reportWithoutEmployee, reportWithoutEmployee)
    // Since Employee field is null, default Elvis operator returns false to filter
    val filteredListDefaultFalse = sortFilterListUseCase.filterReportsByText("firstName", reportList2)
    assertEquals(0, filteredListDefaultFalse.size) // And all reports filtered out
  }
  @Test fun `Filter Reports by Date Text`() {
    // Basic Java.util.TimeZone more testable than android.icu version since it can set default for mocking purposes
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles")) // Setup mock default
    val dateString = "2019-05-19T06:36:05.018Z" // May 18 2019 11:36PM
    val timestamp = Instant.parse(dateString)

    val reportToCheckDate = buildReport(date = timestamp)
    val reportToCheckWrongDate = buildReport(date = timestamp.plus(1, ChronoUnit.DAYS))
    val reportList = listOf(reportToCheckDate, reportToCheckDate, reportToCheckWrongDate)

    val filteredListByPrefix = sortFilterListUseCase.filterReportsByText("May 18", reportList)
    assertEquals(2, filteredListByPrefix.size)

    val filteredListOther = sortFilterListUseCase.filterReportsByText("May 19", reportList)
    assertEquals(1, filteredListOther.size)

    val filteredListContains = sortFilterListUseCase.filterReportsByText("18", reportList)
    assertEquals(2, filteredListContains.size)

    val filteredListFromSuffix = sortFilterListUseCase.filterReportsByText(":36PM", reportList)
    assertEquals(3, filteredListFromSuffix.size)
  }
  @Test fun `Filter Reports by Location Name Text`() {
    val reportToCheckLocation = buildReport(location = buildLocation()) // location.toString == "facility0 unit0 room0"
    val locationNum = reportToCheckLocation.location?.roomNum?.takeLastWhile { it.isDigit() }?.toInt() ?: 0
    val reportToCheckWrongLocation = buildReport(location = buildLocation()) // "facility1 unit1 room1"
    val reportList = listOf(reportToCheckLocation, reportToCheckLocation, reportToCheckWrongLocation)

    val filteredList = sortFilterListUseCase.filterReportsByText("facility$locationNum", reportList)
    assertEquals(2, filteredList.size)
    val filteredListByUnit = sortFilterListUseCase.filterReportsByText("unit$locationNum", reportList)
    assertEquals(2, filteredListByUnit.size)
    val filteredListByRoom = sortFilterListUseCase.filterReportsByText("room$locationNum", reportList)
    assertEquals(2, filteredListByRoom.size)

    val filteredList2 = sortFilterListUseCase.filterReportsByText("facility${locationNum+1}", reportList)
    assertEquals(1, filteredList2.size)
    val filteredListByUnit2 = sortFilterListUseCase.filterReportsByText("unit${locationNum+1}", reportList)
    assertEquals(1, filteredListByUnit2.size)
    val filteredListByRoom2 = sortFilterListUseCase.filterReportsByText("room${locationNum+1}", reportList)
    assertEquals(1, filteredListByRoom2.size)

    // All should appear in filteredList based on common prefix, and none if something completely different
    val filteredListAllFacility = sortFilterListUseCase.filterReportsByText("facility", reportList)
    assertEquals(3, filteredListAllFacility.size)
    val filteredListAllUnit = sortFilterListUseCase.filterReportsByText("unit", reportList)
    assertEquals(3, filteredListAllUnit.size)
    val filteredListAllRoom = sortFilterListUseCase.filterReportsByText("room", reportList)
    assertEquals(3, filteredListAllRoom.size)
    val filteredListOfNone = sortFilterListUseCase.filterReportsByText("nothing", reportList)
    assertEquals(0, filteredListOfNone.size)
    // Even if prefix is different case, should still get all 3 returned
    val filteredListAllFacilityIgnoreCase = sortFilterListUseCase.filterReportsByText("FACILITY", reportList)
    assertEquals(3, filteredListAllFacilityIgnoreCase.size)
    val filteredListAllUnitIgnoreCase = sortFilterListUseCase.filterReportsByText("UNIT", reportList)
    assertEquals(3, filteredListAllUnitIgnoreCase.size)
    val filteredListAllRoomIgnoreCase = sortFilterListUseCase.filterReportsByText("ROOM", reportList)
    assertEquals(3, filteredListAllRoomIgnoreCase.size)
    // If using suffix, should still get 2
    val filteredListFacilityWithSuffix = sortFilterListUseCase.filterReportsByText("ity$locationNum", reportList)
    assertEquals(2, filteredListFacilityWithSuffix.size)
    val filteredListUnitWithSuffix = sortFilterListUseCase.filterReportsByText("it$locationNum", reportList)
    assertEquals(2, filteredListUnitWithSuffix.size)
    val filteredListRoomWithSuffix = sortFilterListUseCase.filterReportsByText("om$locationNum", reportList)
    assertEquals(2, filteredListRoomWithSuffix.size)
  }
  @Test fun `Filter Reports by Health Practice Name Text`() {
    val reportToCheckHealthPractice = buildReport(healthPractice = buildHealthPractice()) // Default fullName == "healthPractice0"
    val reportToCheckDifferentHealthPractice = buildReport(healthPractice = HealthPractice(null, "Hand Hygiene", null))
    val reportList = listOf(reportToCheckHealthPractice, reportToCheckHealthPractice, reportToCheckDifferentHealthPractice)

    val filteredListWithPrefix = sortFilterListUseCase.filterReportsByText("healthPractice", reportList)
    assertEquals(2, filteredListWithPrefix.size)
    val filteredListWithSuffix = sortFilterListUseCase.filterReportsByText("Practice", reportList)
    assertEquals(2, filteredListWithSuffix.size)
    val filteredListContains = sortFilterListUseCase.filterReportsByText("Prac", reportList)
    assertEquals(2, filteredListContains.size)

    val filteredList2 = sortFilterListUseCase.filterReportsByText("Hand", reportList)
    assertEquals(1, filteredList2.size)
    val filteredListIgnoresCase = sortFilterListUseCase.filterReportsByText("HAND", reportList)
    assertEquals(1, filteredListIgnoresCase.size)

    // Need to use default constructor since factory builds a healthPractice anyway if null passed in
    val reportWithoutHealthPractice = Report(null, buildEmployee(), null, buildLocation(), Instant.now())
    val reportList2 = listOf(reportWithoutHealthPractice, reportWithoutHealthPractice)
    // Since HealthPractice field is null, default Elvis operator returns false to filter
    val filteredListDefaultFalse = sortFilterListUseCase.filterReportsByText("healthPractice", reportList2)
    assertEquals(0, filteredListDefaultFalse.size) // And all reports filtered out
  }

  // Main driver function. Rest all tested so just verify they are called as expected!
  @Test fun `Check SortAndFilter Func Calls Expected Helper Funcs`() {
    sortFilterListUseCase = mock { on { beginSortAndFilter(any(), any()) }.thenCallRealMethod() }
    val reportList = listOf(buildReport(), buildReport(), buildReport())
    val filterList = arrayListOf(FilterItem("SomeSorter", false, "Sort By"))

    sortFilterListUseCase.beginSortAndFilter(reportList, filterList)
    verify(sortFilterListUseCase, times(1)).sortReportsBy(any(), any())
    verify(sortFilterListUseCase, never()).filterReportsBy(any(), any(), any())

    filterList.add(FilterItem("SomeFilter", false, "SomeFilterGroup"))
    sortFilterListUseCase.beginSortAndFilter(reportList, filterList)
    verify(sortFilterListUseCase, times(2)).sortReportsBy(any(), any())
    // Filter called for each item in the list. 3 items = 3 calls
    verify(sortFilterListUseCase, times(3)).filterReportsBy(any(), any(), any())

    filterList.removeAt(0) // Remove the sorter
    sortFilterListUseCase.beginSortAndFilter(reportList, filterList)
    verify(sortFilterListUseCase, times(2)).sortReportsBy(any(), any())
    verify(sortFilterListUseCase, times(6)).filterReportsBy(any(), any(), any())

    filterList.add(FilterItem("SomeFilter", false, "SomeFilterGroup"))
    sortFilterListUseCase.beginSortAndFilter(reportList, filterList)
    verify(sortFilterListUseCase, times(2)).sortReportsBy(any(), any())
    // (Two filters * 3 items in the list) + 6 previous calls -> 6 + 6 = 12 total calls!
    verify(sortFilterListUseCase, times(12)).filterReportsBy(any(), any(), any())
  }
}
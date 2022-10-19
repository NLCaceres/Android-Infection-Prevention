package edu.usc.nlcaceres.infectionprevention.domain

import java.util.TimeZone
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.data.Report
import javax.inject.Inject

/* Handles sorting and filtering of reports for the ReportList View */
class SortFilterReportsUseCase @Inject constructor() {
  // Sort and Filter Reports based on user selected filters AND 1 sort option
  fun beginSortAndFilter(reportsList: List<Report>, chosenFilters: List<FilterItem>): List<Report> {
    val onlySortNeeded = chosenFilters.size == 1 && chosenFilters[0].filterGroupName == "Sort By" // Helps avoid filtering
    var sortByIndex = if (onlySortNeeded) 0 else -1
    val filteredList = if (chosenFilters.isEmpty() || onlySortNeeded) reportsList
    else reportsList.filter { report -> // For each report,
      for ((i, filter) in chosenFilters.withIndex()) { // Run each filter on it to check if we retain it or filter it
        if (filter.filterGroupName == "Sort By") { sortByIndex = i; continue } // Sorter found so skip filtering
        // If filterReports finds a match then this element should be retained by list.filter() so return@filter true
        if (filterReportsBy(report, filter.filterGroupName, filter.name)) { return@filter true }
      }
      return@filter false // If made it this far then element should be filtered out
    }
    // If we found a sort instruction then sort the list and return it
    return if (sortByIndex in chosenFilters.indices) {
      sortReportsBy(filteredList, chosenFilters[sortByIndex].name)
    }
    else { filteredList } // Else return filteredList, no sorting
  }

  // Sort list of reports by a particular property
  fun sortReportsBy(reportList: List<Report>, sorter: String) = when (sorter) {
    // If not explicit for dates, Kotlin tries to use generic getDate(): java.util.Date and not this class's date property getter
    "Date Reported" -> reportList.sortedByDescending(Report::date)
    "Employee Name (A-Z)" -> reportList.sortedBy { it.employee?.fullName }
    "Employee Name (Z-A)" -> reportList.sortedByDescending { it.employee?.fullName }
    else -> reportList
  }

  // Check if a particular report matches precautionType or HealthPractice type by name
  fun filterReportsBy(report: Report, filterCategory: String, filter: String) = when (filterCategory) {
    "Precaution Type" -> report.healthPractice?.precaution?.name?.startsWith(filter)
    "Health Practice Type" -> report.healthPractice?.name?.startsWith(filter)
    else -> false // Unknown filter, no match. Allow empty results (if results empty, use sorryTextView message?)
  } ?: false // If a null chain fails, returning null, then Elvis fallback to non-null false

  // Any text contained in fields of the report that match searchText will return true
  fun filterReportsByText(searchText: String, reportList: List<Report>): List<Report> {
    val ignoresCase = true
    return reportList.filter { report -> // If case sensitive, then `str2 in str1` beats str1.contain(str2)
      // Best to call functions in a chained "||" statement rather than save them in variables
      // since Kotlin will only run as far as its first returned true, preventing further contains() checks
      (report.employee?.fullName?.contains(searchText, ignoresCase) ?: false) ||
          report.formattedDate(TimeZone.getDefault().id).contains(searchText, ignoresCase) ||
          report.location.toString().contains(searchText, ignoresCase) ||
          (report.healthPractice?.name?.contains(searchText, ignoresCase) ?: false)
    }
  }
  // TODO: Allow multiple formats to be compared
  //private fun dateSearchComparison(date : Date, searchText : String) {}
}


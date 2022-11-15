package edu.usc.nlcaceres.infectionprevention.helpers.di

import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakeEmployeeRepository.EmployeeFactory
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakeHealthPracticeRepository.HealthPracticeFactory
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakeLocationRepository.LocationFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.temporal.ChronoUnit

class FakeReportRepository: ReportRepository {
  var someList: List<Report> = emptyList()

  var needDelay: Boolean = false
  var optionalClosure: () -> Unit = { } // COULD add suspend keyword without affecting tests BUT seemingly unneeded

  init { populateList() }

  override fun fetchReportList(): Flow<List<Report>> {
    return flow { // Can't return inside flow builder
      emit(emptyList())
      emit(someList)
      if (needDelay) { delay(3000) }
      // Could call following from helper function BUT this seems to be enough time to let tests set closure ref
      optionalClosure.invoke() // ALSO acts more like actual repository
    }
  }

  override fun createReport() { }

  fun populateList() { someList = makeList() }
  fun clearList() { someList = emptyList() }

  companion object Factory {
    fun makeList(): List<Report> {
      val employeeList = EmployeeFactory.makeList()

      // Hand Hygiene, Contact, Droplet, Hand Hygiene, PPE
      val healthPracticeList = with(HealthPracticeFactory.makeList()) { listOf(get(0), get(4), get(3), get(0), get(1)) }

      // USC Unit 4 Room: 202 --> HSC Unit 3 Room: 321 --> HSC Unit 3 Room: 213 --> HSC Unit 5 Room: 121 --> USC Unit 2 Room: 123
      val locationList = with(LocationFactory.makeList()) { listOf(get(1), get(3), get(2), get(4), get(0)) }

      // May 18 11:36PM PST, May 19..., May 25..., May 13..., April 21... (All same time PM PST)
      val timestamp = Instant.parse("2019-05-19T06:36:05.018Z")
      val timestampList = listOf(timestamp, timestamp.plus(1, ChronoUnit.DAYS),
        timestamp.plus(7, ChronoUnit.DAYS), timestamp.minus(5, ChronoUnit.DAYS),
        timestamp.minus(27, ChronoUnit.DAYS))

      val finalList = arrayListOf<Report>()
      for (i in 0..4) {
        finalList.add(Report(null, employeeList[i], healthPracticeList[i], locationList[i], timestampList[i]))
      }
      return finalList
    }
  }
}
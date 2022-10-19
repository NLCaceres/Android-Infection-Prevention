package edu.usc.nlcaceres.infectionprevention.helpers.di

import edu.usc.nlcaceres.infectionprevention.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.temporal.ChronoUnit

class FakeReportRepository : ReportRepository {
  var someList: List<Report> = emptyList()

  var needDelay: Boolean = false
  var optionalClosure: () -> Unit = { } // COULD add suspend keyword without affecting tests BUT seemingly unneeded

  override fun fetchReportList(): Flow<List<Report>> {
    return flow { // Can't return inside flow builder
      emit(emptyList())
      emit(someList)
      if (needDelay) { delay(3000) }
      // Could call following from helper function BUT this seems to be enough time to let tests set closure ref
      optionalClosure.invoke() // ALSO acts more like actual repository
    }
  }

  fun populateList() { someList = makeList() }
  fun clearList() { someList = emptyList() }

  companion object Factory {
    fun makeList(): List<Report> {
      val timestamp = Instant.parse("2019-05-19T06:36:05.018Z")
      val report1 = Report(null, Employee(null, "John", "Smith", null),
        HealthPractice(null, "Hand Hygiene", null),
        Location(null, "USC", "4", "202"), timestamp
      )
      val report2 = Report(null, Employee(null, "Jill", "Chambers", null),
        HealthPractice(null, "Contact", null),
        Location(null, "HSC", "3", "321"), timestamp.plus(1, ChronoUnit.DAYS)
      )
      val report3 = Report(null, Employee(null, "Victor", "Richards", null),
        HealthPractice(null, "Droplet", null),
        Location(null, "HSC", "3", "213"), timestamp.plus(7, ChronoUnit.DAYS)
      )
      val report4 = Report(null, Employee(null, "Melody", "Rios", null),
        HealthPractice(null, "Hand Hygiene", null),
        Location(null, "HSC", "5", "121"), timestamp.minus(5, ChronoUnit.DAYS)
      )
      val report5 = Report(null, Employee(null, "Brian", "Ishida", null),
        HealthPractice(null, "PPE", null),
        Location(null, "USC", "2", "123"), timestamp.minus(27, ChronoUnit.DAYS)
      )
      return arrayListOf(report1, report2, report3, report4, report5)
    }
  }
}
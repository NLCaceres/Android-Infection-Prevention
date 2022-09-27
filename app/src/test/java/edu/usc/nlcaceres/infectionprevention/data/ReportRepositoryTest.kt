package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory
import edu.usc.nlcaceres.infectionprevention.helpers.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.mockito.quality.Strictness

@ExperimentalCoroutinesApi
class ReportRepositoryTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var remoteDataSource: ReportDataSource
  @Mock lateinit var reportListCollector: FlowCollector<List<Report>>

  @Test fun fetchSuccessfulReportList() = runTest {
    val reportList = arrayListOf(ReportsFactory.buildReport(null, null, null))
    val successfulResult = Result.success(reportList)
    remoteDataSource = mock() { onBlocking { fetchReportList() } doReturn successfulResult }

    val reportRepository = AppReportRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    reportRepository.fetchReportList().collect(reportListCollector)

    verifyBlocking(reportListCollector, times(2)) { emit(any()) }
    verifyBlocking(reportListCollector, times(1)) { emit(emptyList()) } // Initial empty list emitted
    verifyBlocking(reportListCollector, times(1)) { emit(reportList) } // Result success value emitted
    verifyBlocking(remoteDataSource, times(1)) { fetchReportList() }
  }

  @Test fun fetchFailureReportList() = runTest {
    val failureResult: Result<List<Report>> = Result.failure(Exception("Problem"))
    remoteDataSource = mock() { onBlocking { fetchReportList() } doReturn failureResult }

    val reportRepository = AppReportRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    reportRepository.fetchReportList().catch { assertEquals(it.message, "Problem") }
        .collect(reportListCollector)

    verifyBlocking(reportListCollector, times(2)) { emit(any()) }
    verifyBlocking(reportListCollector, times(2)) { emit(emptyList()) }
    verifyBlocking(remoteDataSource, times(1)) { fetchReportList() }
  }
}
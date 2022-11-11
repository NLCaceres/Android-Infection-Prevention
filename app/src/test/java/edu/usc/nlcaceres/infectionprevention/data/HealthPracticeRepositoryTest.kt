package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildHealthPractice
import edu.usc.nlcaceres.infectionprevention.helpers.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.mockito.quality.Strictness

@ExperimentalCoroutinesApi
class HealthPracticeRepositoryTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var remoteDataSource: HealthPracticeDataSource
  @Mock lateinit var healthPracticeListCollector: FlowCollector<List<HealthPractice>>

  @Test fun `Fetch Successful Health Practice List`() = runTest {
    val healthPracticeList = arrayListOf(buildHealthPractice())
    val successfulResult = Result.success(healthPracticeList)
    remoteDataSource = mock() { onBlocking { fetchHealthPracticeList() } doReturn successfulResult }

    val reportRepository = AppHealthPracticeRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    reportRepository.fetchHealthPracticeList().collect(healthPracticeListCollector)

    verifyBlocking(healthPracticeListCollector, times(2)) { emit(any()) }
    verifyBlocking(healthPracticeListCollector, times(1)) { emit(emptyList()) } // Initial empty list emitted
    verifyBlocking(healthPracticeListCollector, times(1)) { emit(healthPracticeList) } // Result success value emitted
    verifyBlocking(remoteDataSource, times(1)) { fetchHealthPracticeList() }
  }

  @Test fun `Fetch Failure Health Practice List`() = runTest {
    val failureResult: Result<List<HealthPractice>> = Result.failure(Exception("Problem"))
    remoteDataSource = mock() { onBlocking { fetchHealthPracticeList() } doReturn failureResult }

    val healthPracticeRepository = AppHealthPracticeRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    healthPracticeRepository.fetchHealthPracticeList().catch { assertEquals(it.message, "Problem") }
        .collect(healthPracticeListCollector)

    verifyBlocking(healthPracticeListCollector, times(2)) { emit(any()) }
    // Even on failure when Result's value is null (or simply not set), we should by default get an emptyList
    verifyBlocking(healthPracticeListCollector, times(2)) { emit(emptyList()) } // Therefore getting it 2 times!
    verifyBlocking(remoteDataSource, times(1)) { fetchHealthPracticeList() }
  }
}
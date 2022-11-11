package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildLocation
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
class LocationRepositoryTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var remoteDataSource: LocationDataSource
  @Mock lateinit var locationListCollector: FlowCollector<List<Location>>

  @Test fun `Fetch Successful Location List`() = runTest {
    val locationList = arrayListOf(buildLocation())
    val successfulResult = Result.success(locationList)
    remoteDataSource = mock() { onBlocking { fetchLocationList() } doReturn successfulResult }

    val reportRepository = AppLocationRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    reportRepository.fetchLocationList().collect(locationListCollector)

    verifyBlocking(locationListCollector, times(2)) { emit(any()) }
    verifyBlocking(locationListCollector, times(1)) { emit(emptyList()) } // Initial empty list emitted
    verifyBlocking(locationListCollector, times(1)) { emit(locationList) } // Result success value emitted
    verifyBlocking(remoteDataSource, times(1)) { fetchLocationList() }
  }

  @Test fun `Fetch Failure Location List`() = runTest {
    val failureResult: Result<List<Location>> = Result.failure(Exception("Problem"))
    remoteDataSource = mock() { onBlocking { fetchLocationList() } doReturn failureResult }

    val locationRepository = AppLocationRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    locationRepository.fetchLocationList().catch { assertEquals(it.message, "Problem") }
        .collect(locationListCollector)

    verifyBlocking(locationListCollector, times(2)) { emit(any()) }
    // Even on failure when Result's value is null (or simply not set), we should by default get an emptyList
    verifyBlocking(locationListCollector, times(2)) { emit(emptyList()) } // Therefore getting it 2 times!
    verifyBlocking(remoteDataSource, times(1)) { fetchLocationList() }
  }
}
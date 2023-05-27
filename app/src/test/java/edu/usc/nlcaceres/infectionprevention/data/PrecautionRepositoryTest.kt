package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildPrecaution
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
class PrecautionRepositoryTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var remoteDataSource: PrecautionDataSource
  @Mock lateinit var precautionListCollector: FlowCollector<List<Precaution>>

  @Test fun `Fetch Successful Precaution List`() = runTest {
    val precautionList = arrayListOf(buildPrecaution())
    val successfulResult = Result.success(precautionList)
    remoteDataSource = mock { onBlocking { fetchPrecautionList() } doReturn successfulResult }

    val precautionRepository = AppPrecautionRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    precautionRepository.fetchPrecautionList().collect(precautionListCollector)

    verifyBlocking(precautionListCollector, times(2)) { emit(any()) }
    verifyBlocking(precautionListCollector, times(1)) { emit(emptyList()) } // Initial empty list emitted
    verifyBlocking(precautionListCollector, times(1)) { emit(precautionList) } // Result success value emitted
    verifyBlocking(remoteDataSource, times(1)) { fetchPrecautionList() }
  }

  @Test fun `Fetch Failure Precaution List`() = runTest {
    val failureResult: Result<List<Precaution>> = Result.failure(Exception("Problem"))
    remoteDataSource = mock { onBlocking { fetchPrecautionList() } doReturn failureResult }

    val precautionRepository = AppPrecautionRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    precautionRepository.fetchPrecautionList().catch { assertEquals(it.message, "Problem") }
        .collect(precautionListCollector)

    verifyBlocking(precautionListCollector, times(2)) { emit(any()) }
    // Even on failure when Result's value is null (or simply not set), we should by default get an emptyList
    verifyBlocking(precautionListCollector, times(2)) { emit(emptyList()) } // Therefore getting it 2 times!
    verifyBlocking(remoteDataSource, times(1)) { fetchPrecautionList() }
  }
}
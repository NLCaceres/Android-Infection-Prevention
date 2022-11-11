package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildEmployee
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
class EmployeeRepositoryTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var remoteDataSource: EmployeeDataSource
  @Mock lateinit var employeeListCollector: FlowCollector<List<Employee>>

  @Test fun `Fetch Successful Employee List`() = runTest {
    val employeeList = arrayListOf(buildEmployee())
    val successfulResult = Result.success(employeeList)
    remoteDataSource = mock() { onBlocking { fetchEmployeeList() } doReturn successfulResult }

    // Following uses UnconfinedTestDispatcher from the mainDispatcherRule via its testDispatcher prop
    // BUT could pass in StandardTestDispatcher so our tests don't eager launch coroutines
    val reportRepository = AppEmployeeRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    reportRepository.fetchEmployeeList().collect(employeeListCollector)

    verifyBlocking(employeeListCollector, times(2)) { emit(any()) }
    verifyBlocking(employeeListCollector, times(1)) { emit(emptyList()) } // Initial empty list emitted
    verifyBlocking(employeeListCollector, times(1)) { emit(employeeList) } // Result success value emitted
    verifyBlocking(remoteDataSource, times(1)) { fetchEmployeeList() }
  }

  @Test fun `Fetch Failure Employee List`() = runTest {
    val failureResult: Result<List<Employee>> = Result.failure(Exception("Problem"))
    remoteDataSource = mock() { onBlocking { fetchEmployeeList() } doReturn failureResult }

    val employeeRepository = AppEmployeeRepository(remoteDataSource, mainDispatcherRule.testDispatcher)

    employeeRepository.fetchEmployeeList().catch { assertEquals(it.message, "Problem") }
        .collect(employeeListCollector)

    verifyBlocking(employeeListCollector, times(2)) { emit(any()) }
    // Even on failure when Result's value is null (or simply not set), we should by default get an emptyList
    verifyBlocking(employeeListCollector, times(2)) { emit(emptyList()) } // Therefore getting it 2 times!
    verifyBlocking(remoteDataSource, times(1)) { fetchEmployeeList() }
  }
}
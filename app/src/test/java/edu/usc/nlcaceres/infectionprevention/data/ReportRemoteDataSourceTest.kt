package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.mockito.quality.Strictness
import retrofit2.Response

@ExperimentalCoroutinesApi
class ReportRemoteDataSourceTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)

  private lateinit var reportAPI: ReportService.ReportAPI

  @Test fun `Fetch Report List`() = runTest { // Works like runBlocking{} BUT skips delays in any suspend funcs it calls
    // BUT using runTest won't stub out Dispatchers in those funcs UNLESS we inject new Dispatchers in our tests
    val reportList = arrayListOf(ReportsFactory.buildReport(null, null, null))
    reportAPI = mock() { onBlocking { fetchReportList() } doReturn Response.success(reportList) }

    val reportRemoteDataSource = ReportRemoteDataSource(reportAPI)
    val actualResult = reportRemoteDataSource.fetchReportList()
    assertEquals(Result.success(reportList), actualResult)
    verify(reportAPI, times(1)).fetchReportList()

    whenever(reportAPI.fetchReportList()).thenReturn(Response.error(403, ResponseBody.create(null, "Problem!")))
    val errorResult = reportRemoteDataSource.fetchReportList()
    assertEquals(Result.failure<List<Report>>(Exception("Problem!"))
      .exceptionOrNull()!!.message, errorResult.exceptionOrNull()!!.message)
    verify(reportAPI, times(2)).fetchReportList()
  }

  private lateinit var employeeAPI: ReportService.EmployeeAPI

  @Test fun `Fetch Employee List`() = runTest {
    val employeeList = arrayListOf(ReportsFactory.buildEmployee())
    employeeAPI = mock() { onBlocking { fetchEmployeeList() } doReturn Response.success(employeeList) }

    val reportRemoteDataSource = EmployeeRemoteDataSource(employeeAPI)
    val actualResult = reportRemoteDataSource.fetchEmployeeList()
    assertEquals(Result.success(employeeList), actualResult)
    verify(employeeAPI, times(1)).fetchEmployeeList()

    whenever(employeeAPI.fetchEmployeeList()).thenReturn(Response.error(403, ResponseBody.create(null, "Problem!")))
    val errorResult = reportRemoteDataSource.fetchEmployeeList()
    assertEquals(Result.failure<List<Employee>>(Exception("Problem!"))
      .exceptionOrNull()!!.message, errorResult.exceptionOrNull()!!.message)
    verify(employeeAPI, times(2)).fetchEmployeeList()
  }

  private lateinit var healthPracticeAPI: ReportService.HealthPracticeAPI

  @Test fun `Fetch Health Practice List`() = runTest {
    val healthPracticeList = arrayListOf(ReportsFactory.buildHealthPractice())
    healthPracticeAPI = mock() { onBlocking { fetchHealthPracticeList() } doReturn Response.success(healthPracticeList) }

    val healthPracticeRemoteDataSource = HealthPracticeRemoteDataSource(healthPracticeAPI)
    val actualResult = healthPracticeRemoteDataSource.fetchHealthPracticeList()
    assertEquals(Result.success(healthPracticeList), actualResult)
    verify(healthPracticeAPI, times(1)).fetchHealthPracticeList()

    whenever(healthPracticeAPI.fetchHealthPracticeList()).thenReturn(Response.error(403, ResponseBody.create(null, "Problem!")))
    val errorResult = healthPracticeRemoteDataSource.fetchHealthPracticeList()
    assertEquals(Result.failure<List<HealthPractice>>(Exception("Problem!"))
      .exceptionOrNull()!!.message, errorResult.exceptionOrNull()!!.message)
    verify(healthPracticeAPI, times(2)).fetchHealthPracticeList()
  }

  private lateinit var locationAPI: ReportService.LocationAPI

  @Test fun `Fetch Location List`() = runTest {
    val locationList = arrayListOf(ReportsFactory.buildLocation())
    locationAPI = mock() { onBlocking { fetchLocationList() } doReturn Response.success(locationList) }

    val locationRemoteDataSource = LocationRemoteDataSource(locationAPI)
    val actualResult = locationRemoteDataSource.fetchLocationList()
    assertEquals(Result.success(locationList), actualResult)
    verify(locationAPI, times(1)).fetchLocationList()

    whenever(locationAPI.fetchLocationList()).thenReturn(Response.error(403, ResponseBody.create(null, "Problem!")))
    val errorResult = locationRemoteDataSource.fetchLocationList()
    assertEquals(Result.failure<List<Location>>(Exception("Problem!"))
      .exceptionOrNull()!!.message, errorResult.exceptionOrNull()!!.message)
    verify(locationAPI, times(2)).fetchLocationList()
  }

  private lateinit var precautionAPI: ReportService.PrecautionAPI

  @Test fun `Fetch Precaution List`() = runTest {
    val precautionList = arrayListOf(ReportsFactory.buildPrecaution(PrecautionType.Standard))
    precautionAPI = mock() { onBlocking { fetchPrecautionList() } doReturn Response.success(precautionList) }

    val precautionRemoteDataSource = PrecautionRemoteDataSource(precautionAPI)
    val actualResult = precautionRemoteDataSource.fetchPrecautionList()
    assertEquals(Result.success(precautionList), actualResult)
    verify(precautionAPI, times(1)).fetchPrecautionList()

    whenever(precautionAPI.fetchPrecautionList()).thenReturn(Response.error(403, ResponseBody.create(null, "Problem!")))
    val errorResult = precautionRemoteDataSource.fetchPrecautionList()
    assertEquals(Result.failure<List<Precaution>>(Exception("Problem!"))
      .exceptionOrNull()!!.message, errorResult.exceptionOrNull()!!.message)
    verify(precautionAPI, times(2)).fetchPrecautionList()
  }

  // Generic Function that handles most responses
  @Test fun `Successful Generic Get Response`() = runTest {
    val responseBody = 1
    val actualResult = getResponse { Response.success(responseBody) }
    assertEquals(Result.success(1), actualResult)
  }
  @Test fun `Throwing Request in generic GetResponse`() = runTest {
    val result = getResponse<Int> { throw Exception("Problem") }
    val actualException = result.exceptionOrNull() ?: Exception()
    // Due to platform types comparing Exceptions rarely works as expected so best to compare messages
    val expectedExceptionMsg = "Problem" // Which do hold consistent across returns and throws
    assertEquals(expectedExceptionMsg, actualException.message)
  }
  @Test fun `Successful but null body in generic getResponse`() = runTest {
    val responseBody: Int? = null
    val result = getResponse { Response.success(responseBody) }
    val actualException = result.exceptionOrNull() ?: Exception()
    val expectedExceptionMsg = "Empty Response Body"
    assertEquals(expectedExceptionMsg, actualException.message)
  }
  @Test fun `Failure Response in generic getResponse`() = runTest {
    val result = getResponse<List<Precaution>> { Response.error(403, ResponseBody.create(null, "Error!")) }
    val actualException = result.exceptionOrNull() ?: Exception()
    val expectedExceptionMsg = "Error!"
    assertEquals(expectedExceptionMsg, actualException.message)

    // Given the check for a failure response in the generic function, not sure it's possible to have a null errorBody()
    // Therefore it doesn't seem possible for the walrus "Problem" exception message. Can't test this particular edge case
  }
}
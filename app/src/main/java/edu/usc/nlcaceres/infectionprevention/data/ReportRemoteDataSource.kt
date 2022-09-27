package edu.usc.nlcaceres.infectionprevention.data

import javax.inject.Inject
import edu.usc.nlcaceres.infectionprevention.data.ReportService.ReportAPI
import edu.usc.nlcaceres.infectionprevention.data.ReportService.PrecautionAPI
import retrofit2.Response

interface ReportDataSource {
  suspend fun fetchReportList(): Result<List<Report>>
}
class ReportRemoteDataSource @Inject constructor(private val reportAPI: ReportAPI): ReportDataSource {
  override suspend fun fetchReportList(): Result<List<Report>> = getResponse { reportAPI.fetchReportList() }
}

interface PrecautionDataSource {
  suspend fun fetchPrecautionList(): Result<List<Precaution>>
}
class PrecautionRemoteDataSource @Inject constructor(private val precautionAPI: PrecautionAPI): PrecautionDataSource {
  override suspend fun fetchPrecautionList(): Result<List<Precaution>> = getResponse { precautionAPI.fetchPrecautionList() }
}

suspend fun <T> getResponse(request: suspend () -> Response<T>): Result<T> {
  val getRequestResponse = try { request() } catch (e: Exception) { return Result.failure(e) } // In case of Retrofit network err
  return if (getRequestResponse.isSuccessful) {
    val responseBody = getRequestResponse.body()
    if (responseBody != null) Result.success(responseBody) // Return normal body
    else Result.failure(Exception("Empty Response Body")) // Body is empty, something is wrong!
  }
  else { Result.failure(Exception(getRequestResponse.errorBody()?.string() ?: "Problem!")) } // Error response, definitely something wrong!
}

package edu.usc.nlcaceres.infectionprevention.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response

interface ReportService {

  interface ReportAPI {
    @GET("reports") // Suspend funs launch on main thread by default so need to call from a coroutine!
    suspend fun fetchReportList(): Response<List<Report>>
    @POST("reports/create") // Response helper type gets metadata like status codes
    suspend fun createReport(@Body report : Report): Response<Report>
  }
  interface EmployeeAPI {
    @GET("employees")
    suspend fun fetchEmployeeList(): Response<List<Employee>>
  }
  interface HealthPracticeAPI {
    @GET("healthpractices")
    suspend fun fetchHealthPracticeList(): Response<List<HealthPractice>>
  }
  interface LocationAPI {
    @GET("locations")
    suspend fun fetchLocationList(): Response<List<Location>>
  }
  interface PrecautionAPI {
    @GET("precautions")
    suspend fun fetchPrecautionList(): Response<List<Precaution>>
  }
}
package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.util.baseURL
import edu.usc.nlcaceres.infectionprevention.util.snakeCaseGson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response

interface ReportService {

  companion object {
    private val RetrofitInstance by lazy { createBaseRetrofitInstance() } // Thread-safe alternative to a singleton
    private fun createBaseRetrofitInstance() : Retrofit {
      return Retrofit.Builder().baseUrl(baseURL) // BaseUrl must end in '/'
        .addConverterFactory(GsonConverterFactory.create(snakeCaseGson())) // Custom Gson factory based on a GsonBuilder instance
        .build()
    }
    fun createReportApi(): ReportApiInterface = RetrofitInstance.create(ReportApiInterface::class.java)
    fun createPrecautionApi(): PrecautionApiInterface = RetrofitInstance.create(PrecautionApiInterface::class.java)
  }

  interface ReportApiInterface {
    @GET("reports") // Suspend funs launch on main thread by default so need to call from a coroutine!
    suspend fun fetchReportList(): Response<List<Report>>
    @POST("reports/create") // Response helper type gets metadata like status codes
    suspend fun createReport(@Body report : Report): Response<Report>
  }
  interface PrecautionApiInterface {
    @GET("precautions")
    suspend fun fetchPrecautionList(): Response<List<Precaution>>
  }
}
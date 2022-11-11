package edu.usc.nlcaceres.infectionprevention.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ReportRepository {
  fun fetchReportList(): Flow<List<Report>>
  fun createReport()
}
/* Repositories have the job of synthesizing the results of various dataSources
   Usually a remote/network one and a local database file one (Room in Android vs CoreData/Sqlite in iOS)  */
class AppReportRepository(private val reportRemoteDataSource: ReportDataSource,
                          private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): ReportRepository {
  override fun fetchReportList(): Flow<List<Report>> { // No "suspend" keyword needed since
    return flow { // the flow builder can call "suspend" funcs without blocking main via its own bubble/coroutine
      emit(emptyList()) // Could start by emitting an emptyList, then the Room cached list and finally server sent list

      val fetchResult = reportRemoteDataSource.fetchReportList()
      // If success we should GET our list. If fetch fails, we can default to an empty list (so nothing displays)
      emit(fetchResult.getOrDefault(emptyList()))
      // If we default then we know we failed so rethrow our exception for the flow to catch
      if (fetchResult.isFailure) { throw fetchResult.exceptionOrNull()!! }
    }.flowOn(ioDispatcher)
  // If this flow is used in a combine or zip, no problem! This'll still run on ioDispatcher even if the combination block may be on main
  }
  override fun createReport() {

  }
}

interface EmployeeRepository {
  fun fetchEmployeeList(): Flow<List<Employee>>
}
// No @Inject needed in following constructors since the RepositoryModule details the injection process
class AppEmployeeRepository(private val employeeRemoteDataSource: EmployeeDataSource,
                              private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): EmployeeRepository {
  override fun fetchEmployeeList(): Flow<List<Employee>> {
    return flow {
      emit(emptyList())
      val fetchResult = employeeRemoteDataSource.fetchEmployeeList()
      emit(fetchResult.getOrDefault(emptyList()))
      if (fetchResult.isFailure) { throw fetchResult.exceptionOrNull()!! }
    }.flowOn(ioDispatcher)
  }
}

interface HealthPracticeRepository {
  fun fetchHealthPracticeList(): Flow<List<HealthPractice>>
}
class AppHealthPracticeRepository(private val healthPracticeRemoteDataSource: HealthPracticeDataSource,
                              private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): HealthPracticeRepository {
  override fun fetchHealthPracticeList(): Flow<List<HealthPractice>> {
    return flow {
      emit(emptyList())
      val fetchResult = healthPracticeRemoteDataSource.fetchHealthPracticeList()
      emit(fetchResult.getOrDefault(emptyList()))
      if (fetchResult.isFailure) { throw fetchResult.exceptionOrNull()!! }
    }.flowOn(ioDispatcher)
  }
}

interface LocationRepository {
  fun fetchLocationList(): Flow<List<Location>>
}
class AppLocationRepository(private val locationRemoteDataSource: LocationDataSource,
                              private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): LocationRepository {
  override fun fetchLocationList(): Flow<List<Location>> {
    return flow {
      emit(emptyList())
      val fetchResult = locationRemoteDataSource.fetchLocationList()
      emit(fetchResult.getOrDefault(emptyList()))
      if (fetchResult.isFailure) { throw fetchResult.exceptionOrNull()!! }
    }.flowOn(ioDispatcher)
  }
}

interface PrecautionRepository {
  fun fetchPrecautionList(): Flow<List<Precaution>>
}
class AppPrecautionRepository(private val precautionRemoteDataSource: PrecautionDataSource,
                              private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): PrecautionRepository {
  override fun fetchPrecautionList(): Flow<List<Precaution>> {
    return flow {
      emit(emptyList())
      val fetchResult = precautionRemoteDataSource.fetchPrecautionList()
      emit(fetchResult.getOrDefault(emptyList()))
      if (fetchResult.isFailure) { throw fetchResult.exceptionOrNull()!! }
    }.flowOn(ioDispatcher)
  }
}


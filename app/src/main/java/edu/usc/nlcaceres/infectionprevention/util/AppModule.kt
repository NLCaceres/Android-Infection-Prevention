package edu.usc.nlcaceres.infectionprevention.util

import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton
import dagger.Binds
import dagger.Provides
import javax.inject.Qualifier
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.data.ReportService.*
import java.time.Instant

// Whether Dagger or Hilt, @Module establishes how certain types are provided
// In particular, Hilt needs info on interfaces and types that use builders to be instantiated
// Predominantly provides Retrofit Services to activities/fragments that need it
@Module
@InstallIn(SingletonComponent::class) // Inject into Application (available everywhere)
object AppModule {

  @Singleton // Same instance provided across the life of the App
  @Provides // Even though dispatcher in repository constructors as default param value, this lets swap as needed!
  fun provideIoDispatcher() = Dispatchers.IO // Useful for a ton of coroutine off-main-thread work

  @Singleton
  @Provides
  fun provideGson(): Gson = GsonBuilder()
    .registerTypeAdapter(Instant::class.java, JsonDeserializer { json, _, _ -> Instant.parse(json.asString) })
    .create()

  @Singleton
  @Provides // Could add qualifier BUT only will use one type of converter (Gson) so unlikely to matter
  fun provideGsonConverterFactory(gson: Gson): retrofit2.Converter.Factory = GsonConverterFactory.create(gson)

  // The next funcs work together to build Retrofit API interfaces rather than use the typical
  // companion object pattern to create a retrofit instance then create APIs from our interfaces
  // No lazy retrofit instance needed and across the app only one instance per API to request data over the network
  @Singleton
  @Provides
  fun provideBaseRetrofitInstance(gsonConverterFactory: retrofit2.Converter.Factory): Retrofit {
    return Retrofit.Builder().baseUrl(BaseURL) // BaseUrl must end in '/'
        .addConverterFactory(gsonConverterFactory) // Custom Gson factory based on a GsonBuilder instance
        .build()
  }
  @Singleton
  @Provides // Use above retrofitInstance in this func parameter then let Retrofit create instance of our Report API
  fun provideReportAPI(retrofit: Retrofit): ReportAPI = retrofit.create(ReportAPI::class.java)

  @Singleton
  @Provides // Similarly, grab above provided retrofit instance and let it create Employee API from interface
  fun provideEmployeeAPI(retrofit: Retrofit): EmployeeAPI = retrofit.create(EmployeeAPI::class.java)

  @Singleton
  @Provides
  fun provideHealthPracticeAPI(retrofit: Retrofit): HealthPracticeAPI = retrofit.create(HealthPracticeAPI::class.java)

  @Singleton
  @Provides
  fun provideLocationAPI(retrofit: Retrofit): LocationAPI = retrofit.create(LocationAPI::class.java)

  @Singleton
  @Provides
  fun providePrecautionAPI(retrofit: Retrofit): PrecautionAPI = retrofit.create(PrecautionAPI::class.java)
}

//TODO: Merge this DataSourceModule into main AppModule
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
  // Why not Object? Because @Binds must be attached to an abstract function which must be placed in abstract classes
  // Qualifiers (Used to differentiate between specific implementations of various interfaces)
  @Qualifier
  @Retention // By Default = AnnotationRetention.RUNTIME making type visible by reflection (vs BINARY where type is invisible)
  annotation class RemoteDataSource // Remote is server network based data source

  @Qualifier
  @Retention
  annotation class LocalDataSource  // Local will be Room DB related classes

  @Singleton
  @RemoteDataSource // RemoteDataSource implementation sent when injections are marked with this qualifier
  @Binds
  abstract fun bindReportRemoteDataSource(reportRemoteDataSource: ReportRemoteDataSource): ReportDataSource

  @Singleton
  @RemoteDataSource
  @Binds
  abstract fun bindEmployeeRemoteDataSource(employeeRemoteDataSource: EmployeeRemoteDataSource): EmployeeDataSource

  @Singleton
  @RemoteDataSource
  @Binds
  abstract fun bindHealthPracticeRemoteDataSource(precautionRemoteDataSource: HealthPracticeRemoteDataSource): HealthPracticeDataSource

  @Singleton
  @RemoteDataSource
  @Binds
  abstract fun bindLocationRemoteDataSource(locationRemoteDataSource: LocationRemoteDataSource): LocationDataSource

  @Singleton
  @RemoteDataSource
  @Binds
  abstract fun bindPrecautionRemoteDataSource(precautionRemoteDataSource: PrecautionRemoteDataSource): PrecautionDataSource
}

//TODO: Attempt to convert @Provides into @Binds (so object becomes abstract class + implementations need @Inject constructor)
@Module // Separating repository into its own module allows instrumentedTests to swap it out via @TestInstallIn(components, replaces)
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Singleton
  @Provides
  fun provideReportRepository(@DataSourceModule.RemoteDataSource reportRemoteDataSource: ReportDataSource,
                              ioDispatcher: CoroutineDispatcher): ReportRepository =
    AppReportRepository(reportRemoteDataSource, ioDispatcher)

  @Singleton
  @Provides
  fun provideEmployeeRepository(@DataSourceModule.RemoteDataSource employeeRemoteDataSource: EmployeeDataSource,
                                  ioDispatcher: CoroutineDispatcher): EmployeeRepository =
    AppEmployeeRepository(employeeRemoteDataSource, ioDispatcher)

  @Singleton
  @Provides
  fun provideHealthPracticeRepository(@DataSourceModule.RemoteDataSource healthPracticeRemoteDataSource: HealthPracticeDataSource,
                                  ioDispatcher: CoroutineDispatcher): HealthPracticeRepository =
    AppHealthPracticeRepository(healthPracticeRemoteDataSource, ioDispatcher)

  @Singleton
  @Provides
  fun provideLocationRepository(@DataSourceModule.RemoteDataSource locationRemoteDataSource: LocationDataSource,
                                  ioDispatcher: CoroutineDispatcher): LocationRepository =
    AppLocationRepository(locationRemoteDataSource, ioDispatcher)

  @Singleton
  @Provides
  fun providePrecautionRepository(@DataSourceModule.RemoteDataSource precautionRemoteDataSource: PrecautionDataSource,
                                  ioDispatcher: CoroutineDispatcher): PrecautionRepository =
    AppPrecautionRepository(precautionRemoteDataSource, ioDispatcher)
}
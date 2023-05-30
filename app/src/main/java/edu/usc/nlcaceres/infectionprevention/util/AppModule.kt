package edu.usc.nlcaceres.infectionprevention.util

import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton
import dagger.Binds
import dagger.Provides
import javax.inject.Qualifier
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.data.ReportService.*
import java.time.Instant

/* This module helps provide Retrofit Services to Activities/Fragments that need it via DataSources/Repositories */

@Module // Whether Dagger or Hilt, @Module declares how certain types should be injected, built or instantiated
@InstallIn(SingletonComponent::class) // Inject at Application level, making these dependencies available everywhere
abstract class AppModule {

  // Technically, we CAN'T use @Binds and @Provides in the same module! BUT with a few tricks, it IS possible!
  // PROBLEM 1: @Binds never actually implements any methods under Dagger's hood, nor does it ever invoke anything
  // It just scans abstract funcs to make Dagger's Dependency Graph so its factories can grab each interface's supplied implementations
  // PROBLEM 2: Meanwhile @Provides works by letting Dagger's factories simply call our declared builder funcs
  // SOLUTION 1: Pre-Dagger 2.26, this issue could be solved via Java static methods or @Module marked Kotlin companion objects
  // SOLUTION 2: In Dagger 2.26+, Kotlin companion objects don't need @Module, nor should they mark their methods with @JvmStatic
  // Because companion objects translate into Java static classes, so Dagger can easily invoke our @Provide methods, static or not
  companion object {
    // MARK: Retrofit Dependencies
    @Singleton // Same instance provided across the life of the App
    @Provides // Even though repository constructors' use dispatcher as a default param value, this lets swap as needed!
    fun provideIoDispatcher() = Dispatchers.IO // Useful for a ton of coroutine off-main-thread jobs

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
      .registerTypeAdapter(Instant::class.java, JsonDeserializer { json, _, _ -> Instant.parse(json.asString) })
      .create()

    @Singleton
    @Provides // Could add qualifier BUT only will use one type of converter (Gson) so unlikely to matter
    fun provideGsonConverterFactory(gson: Gson): retrofit2.Converter.Factory = GsonConverterFactory.create(gson)

    @Singleton // Excellent example of Dagger's benefits, avoiding a semi-complex companion obj lazily creating a Singleton
    @Provides // Easily making a SINGLE Retrofit instance, designated to create ONLY 1 of each of our @Bind API interfaces
    fun provideBaseRetrofitInstance(gsonConverterFactory: retrofit2.Converter.Factory): Retrofit {
      return Retrofit.Builder().baseUrl(BaseURL) // BaseUrl must end in '/'
        .addConverterFactory(gsonConverterFactory) // Custom Gson factory based on a GsonBuilder instance
        .build()
    }

    // MARK: API Services
    @Singleton
    @Provides // Injects the above retrofitInstance via this func's param, so we can use it to build a Report API concrete instance
    fun provideReportAPI(retrofit: Retrofit): ReportAPI = retrofit.create(ReportAPI::class.java)

    @Singleton
    @Provides // Similarly, grab above provided retrofit instance and let it build the Employee API
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

  // MARK: Data Sources
  @Qualifier // Qualifiers (Used to differentiate between specific implementations of various interfaces)
  @Retention // By Default = AnnotationRetention.RUNTIME making type visible by reflection (vs BINARY where type is invisible)
  annotation class RemoteDataSource // Remote is for Data received from the Server via Retrofit Networking

  @Qualifier
  @Retention
  annotation class LocalDataSource  // Local will be for the Android Room database related classes

  @Singleton
  @RemoteDataSource // The RemoteDataSource implementation will be injected when marked with this qualifier
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

// MARK: Repositories providing data from both the API and Android Room cache
@Module // Separating repositories into its own module lets instrumentedTests stub them with dummy data
@InstallIn(SingletonComponent::class) // Via @Inject, @Module @TestInstallIn(components, replaces), or @BindValue @JvmField var someStub
abstract class RepositoryModule {

  @Singleton
  @Binds
  abstract fun provideReportRepository(appReportRepository: AppReportRepository): ReportRepository

  @Singleton
  @Binds
  abstract fun provideEmployeeRepository(appEmployeeRepository: AppEmployeeRepository): EmployeeRepository

  @Singleton
  @Binds
  abstract fun provideHealthPracticeRepository(appHealthPracticeRepository: AppHealthPracticeRepository): HealthPracticeRepository

  @Singleton
  @Binds
  abstract fun provideLocationRepository(appLocationRepository: AppLocationRepository): LocationRepository

  @Singleton
  @Binds
  abstract fun providePrecautionRepository(appPrecautionRepository: AppPrecautionRepository): PrecautionRepository
}

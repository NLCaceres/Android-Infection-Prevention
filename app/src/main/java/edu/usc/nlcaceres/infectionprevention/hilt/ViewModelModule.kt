package edu.usc.nlcaceres.infectionprevention.hilt

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import edu.usc.nlcaceres.infectionprevention.data.EmployeeDataSource
import edu.usc.nlcaceres.infectionprevention.data.EmployeeRemoteDataSource
import edu.usc.nlcaceres.infectionprevention.data.HealthPracticeDataSource
import edu.usc.nlcaceres.infectionprevention.data.HealthPracticeRemoteDataSource
import edu.usc.nlcaceres.infectionprevention.data.LocationDataSource
import edu.usc.nlcaceres.infectionprevention.data.LocationRemoteDataSource
import edu.usc.nlcaceres.infectionprevention.data.PrecautionDataSource
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRemoteDataSource
import edu.usc.nlcaceres.infectionprevention.data.ReportDataSource
import edu.usc.nlcaceres.infectionprevention.data.ReportRemoteDataSource
import edu.usc.nlcaceres.infectionprevention.data.ReportService.EmployeeAPI
import edu.usc.nlcaceres.infectionprevention.data.ReportService.HealthPracticeAPI
import edu.usc.nlcaceres.infectionprevention.data.ReportService.LocationAPI
import edu.usc.nlcaceres.infectionprevention.data.ReportService.PrecautionAPI
import edu.usc.nlcaceres.infectionprevention.data.ReportService.ReportAPI
import retrofit2.Retrofit
import javax.inject.Qualifier

/** Module for installing Retrofit Services and the DataSources that use them into ViewModels */

//?: Using the ViewModelComponent ensures that these dependencies are instantiated when
// injected into their parent ViewModel and de-init whenever the ViewModel is
// Using SingletonComponent would cause dependencies to be init THE SECOND they are needed
// AND WOULDN'T deinit them until the App itself was de-init, outliving their parent ViewModel
@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {
  companion object {
    // MARK: API Services
    @ViewModelScoped // - Inject a Retrofit instance by adding a retrofit param to this @Provides func
    @Provides // - So I can use it to build a Report API concrete instance
    fun provideReportAPI(retrofit: Retrofit): ReportAPI = retrofit.create(ReportAPI::class.java)

    //?: Scoping a dependency to the ViewModel ensures ONLY 1 instance made per ViewModel instance
    @ViewModelScoped //?: SO ViewModelComponent defines HOW LONG a dependency lives BUT Scope defines HOW MANY live
    @Provides // - Similarly, inject the same Retrofit instance, and create the Employee API
    fun provideEmployeeAPI(retrofit: Retrofit): EmployeeAPI = retrofit.create(EmployeeAPI::class.java)

    @ViewModelScoped
    @Provides
    fun provideHealthPracticeAPI(retrofit: Retrofit): HealthPracticeAPI = retrofit.create(HealthPracticeAPI::class.java)

    @ViewModelScoped
    @Provides
    fun provideLocationAPI(retrofit: Retrofit): LocationAPI = retrofit.create(LocationAPI::class.java)

    @ViewModelScoped
    @Provides
    fun providePrecautionAPI(retrofit: Retrofit): PrecautionAPI = retrofit.create(PrecautionAPI::class.java)
  }
  //?: Technically @Binds and @Provides CANNOT be used in the same module, since @Provides needs an implementation
  // and abstract classes can't be instantiated for Dagger's factories to call the non-abstract funcs defined
  // @Binds works in an abstract class since Dagger DOESN'T implement or invoke the Binded func
  // It just grabs the specific implementation of the desired interface type from the dependency graph Dagger creates
  // Pre-Dagger 2.26: Using a companion object meant needing to mark it with @Module (or in Java, adding static methods)

  // MARK: Data Sources
  @Qualifier // - Qualifiers are used to differentiate between specific implementations of various interfaces
  @Retention // The default Retention value = RUNTIME makes the type visible by reflection
  annotation class RemoteDataSource // - Remote is for Data received from the Server via Retrofit Networking

  @Qualifier
  @Retention // - The other value for Retention = BINARY which makes the type invisible to reflection
  annotation class LocalDataSource  // - Local will be for the Android Room database related classes

  @ViewModelScoped
  @RemoteDataSource // - The RemoteDataSource implementation will be injected when marked with this qualifier
  @Binds
  abstract fun bindReportRemoteDataSource(reportRemoteDataSource: ReportRemoteDataSource): ReportDataSource

  @ViewModelScoped
  @RemoteDataSource
  @Binds
  abstract fun bindEmployeeRemoteDataSource(employeeRemoteDataSource: EmployeeRemoteDataSource): EmployeeDataSource

  @ViewModelScoped
  @RemoteDataSource
  @Binds
  abstract fun bindHealthPracticeRemoteDataSource(precautionRemoteDataSource: HealthPracticeRemoteDataSource): HealthPracticeDataSource

  @ViewModelScoped
  @RemoteDataSource
  @Binds
  abstract fun bindLocationRemoteDataSource(locationRemoteDataSource: LocationRemoteDataSource): LocationDataSource

  @ViewModelScoped
  @RemoteDataSource
  @Binds
  abstract fun bindPrecautionRemoteDataSource(precautionRemoteDataSource: PrecautionRemoteDataSource): PrecautionDataSource
}
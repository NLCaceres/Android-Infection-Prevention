package edu.usc.nlcaceres.infectionprevention.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import edu.usc.nlcaceres.infectionprevention.data.AppEmployeeRepository
import edu.usc.nlcaceres.infectionprevention.data.AppHealthPracticeRepository
import edu.usc.nlcaceres.infectionprevention.data.AppLocationRepository
import edu.usc.nlcaceres.infectionprevention.data.AppPrecautionRepository
import edu.usc.nlcaceres.infectionprevention.data.AppReportRepository
import edu.usc.nlcaceres.infectionprevention.data.EmployeeRepository
import edu.usc.nlcaceres.infectionprevention.data.HealthPracticeRepository
import edu.usc.nlcaceres.infectionprevention.data.LocationRepository
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.data.ReportRepository

@Module // - The Repositories get their own module to make it easier to stub in Instrumented Tests
@InstallIn(ViewModelComponent::class) // - Ideally via @Module @TestInstallIn(components, replaces)
abstract class RepositoryModule { // - BUT @Inject or @BindValue can help grab a mutated ref to a dependency
  @ViewModelScoped
  @Binds
  abstract fun provideReportRepository(appReportRepository: AppReportRepository): ReportRepository

  @ViewModelScoped
  @Binds
  abstract fun provideEmployeeRepository(appEmployeeRepository: AppEmployeeRepository): EmployeeRepository

  @ViewModelScoped
  @Binds
  abstract fun provideHealthPracticeRepository(appHealthPracticeRepository: AppHealthPracticeRepository): HealthPracticeRepository

  @ViewModelScoped
  @Binds
  abstract fun provideLocationRepository(appLocationRepository: AppLocationRepository): LocationRepository

  @ViewModelScoped
  @Binds
  abstract fun providePrecautionRepository(appPrecautionRepository: AppPrecautionRepository): PrecautionRepository
}
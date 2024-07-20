package edu.usc.nlcaceres.infectionprevention.helpers.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import edu.usc.nlcaceres.infectionprevention.data.EmployeeRepository
import edu.usc.nlcaceres.infectionprevention.data.HealthPracticeRepository
import edu.usc.nlcaceres.infectionprevention.data.LocationRepository
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.data.ReportRepository
import edu.usc.nlcaceres.infectionprevention.hilt.RepositoryModule
import javax.inject.Singleton

/** Stubs in data across the InstrumentedTests using Hilt's preferred method of @TestInstallIn */

// - Important to use the SingletonComponent, not the original ViewModelComponent
// Since ViewModelComponent seems to cause ActivityMainRenderProgressTestCase to fail due to Field @Inject
// It MAY be fixable via nested @Install Modules in each TestCase BUT that bloats the other TestCases just to fix 1 TestCase
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
object FakeRepositoryModule {
  @Singleton
  @Provides
  fun provideReportRepository(): ReportRepository = FakeReportRepository()

  @Singleton
  @Provides
  fun provideEmployeeRepository(): EmployeeRepository = FakeEmployeeRepository()

  @Singleton
  @Provides
  fun provideHealthPracticeRepository(): HealthPracticeRepository = FakeHealthPracticeRepository()

  @Singleton
  @Provides
  fun provideLocationRepository(): LocationRepository = FakeLocationRepository()

  @Singleton
  @Provides
  fun providePrecautionRepository(): PrecautionRepository = FakePrecautionRepository()
}
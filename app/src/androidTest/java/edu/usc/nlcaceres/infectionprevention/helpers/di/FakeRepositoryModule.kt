package edu.usc.nlcaceres.infectionprevention.helpers.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import edu.usc.nlcaceres.infectionprevention.data.EmployeeRepository
import edu.usc.nlcaceres.infectionprevention.data.HealthPracticeRepository
import edu.usc.nlcaceres.infectionprevention.data.LocationRepository
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.data.ReportRepository
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule

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
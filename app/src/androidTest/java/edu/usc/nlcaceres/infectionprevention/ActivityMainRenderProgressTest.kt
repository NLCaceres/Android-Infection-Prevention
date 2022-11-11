package edu.usc.nlcaceres.infectionprevention

import androidx.test.core.app.ActivityScenario.launch
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.helpers.di.*
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class ActivityMainRenderProgressTest: RoboTest() {
  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  // Each test gets its own version of the repositories so no variable pollution like the closures
  @BindValue @JvmField
  var employeeRepository: EmployeeRepository = FakeEmployeeRepository()
  @BindValue @JvmField
  var healthPracticeRepository: HealthPracticeRepository = FakeHealthPracticeRepository()
  @BindValue @JvmField
  var locationRepository: LocationRepository = FakeLocationRepository()
  @BindValue @JvmField
  var precautionRepository: PrecautionRepository = FakePrecautionRepository().apply { clearList() }
  @BindValue @JvmField
  var reportRepository: ReportRepository = FakeReportRepository()

  @Test fun view_Loading() { // Probably not a perfect test across devices BUT doesn't slow test suite
    // Freeze load/flow state to run the Espresso checks since EspressoIdling doesn't check during idle state
    (precautionRepository as FakePrecautionRepository).needDelay = true

    // MUST use scenario as follows, NOT the rule version since it'll immediately start & finish the flow before
    // Any updates can be made to the underlying that would affect the view's state
    launch(ActivityMain::class.java).use { // Use will ensure scenario auto-closes on test end
      mainActivity {
        checkSorryMessage("Looking up precautions")
        checkProgressBar(true)
      }
    }
  }

  @Test fun view_Unexpectedly_Receives_No_Data() { // Should expect a list of data but not getting any! Likely err thrown
    // WHEN exception thrown THEN submit emptyList by default AND throw err, updating screen
    val newClosure: () -> Unit =  { throw Exception("Error!") }
    (precautionRepository as FakePrecautionRepository).optionalClosure = newClosure
    launch(ActivityMain::class.java).use {
      mainActivity {
        checkSorryMessage("Sorry! Seems we're having an issue on our end!")
        checkProgressBar()
      }
    }
  }
  @Test fun view_Unexpectedly_Has_Network_Issues() { // Should expect a list of data but not getting any! Likely err thrown
    // WHEN exception thrown THEN submit emptyList by default AND throw err, updating screen
    val newClosure: () -> Unit =  { throw IOException("Error!") }
    (precautionRepository as FakePrecautionRepository).optionalClosure = newClosure
    launch(ActivityMain::class.java).use {
      mainActivity {
        checkSorryMessage("Sorry! Having trouble with the internet connection!")
        checkProgressBar()
      }
    }
  }
  @Test fun view_Receives_No_Data() {
    (precautionRepository as FakePrecautionRepository).someList = emptyList()
    launch(ActivityMain::class.java).use {
      mainActivity {
        checkSorryMessage("Weird! Seems we don't have any available precautions to choose from!")
        checkProgressBar()
      }
    }
  }
  @Test fun view_Receives_Some_Data() {
    (precautionRepository as FakePrecautionRepository).populateList()
    launch(ActivityMain::class.java).use { mainActivity { checkViewLoaded() } }
  }
}

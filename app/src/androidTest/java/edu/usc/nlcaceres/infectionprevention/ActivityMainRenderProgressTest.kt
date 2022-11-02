package edu.usc.nlcaceres.infectionprevention

import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakePrecautionRepository
import edu.usc.nlcaceres.infectionprevention.helpers.di.FakeReportRepository
import edu.usc.nlcaceres.infectionprevention.robots.RoboTest
import edu.usc.nlcaceres.infectionprevention.util.RepositoryModule
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@UninstallModules(RepositoryModule::class)
@HiltAndroidTest
class ActivityMainRenderProgressTest: RoboTest() {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)
  @get:Rule(order = 1)
  val scenarioRule = ActivityScenarioRule(ActivityMain::class.java)

  @BindValue @JvmField // Each test gets its own version of the repo so no variable pollution like the closures
  var precautionRepository: PrecautionRepository = FakePrecautionRepository()
  @BindValue @JvmField
  var reportRepository: ReportRepository = FakeReportRepository()

  @Test fun view_Loading() { // Probably not a perfect test across devices BUT doesn't slow test suite
    // Freeze load/flow state to run the Espresso checks since EspressoIdling doesn't check during idle state
    (precautionRepository as FakePrecautionRepository).needDelay = true
    mainActivity {
      checkSorryMessage("Looking up precautions")
      checkProgressBar(true)
    }
  }

  @Test fun view_Unexpectedly_Receives_No_Data() { // Should expect a list of data but not getting any! Likely err thrown
    // WHEN exception thrown THEN submit emptyList by default AND throw err, updating screen
    val newClosure: () -> Unit =  { throw Exception("Error!") }
    (precautionRepository as FakePrecautionRepository).optionalClosure = newClosure
    mainActivity {
      checkSorryMessage("Sorry! Seems we're having an issue on our end!")
      checkProgressBar()
    }
  }
  @Test fun view_Unexpectedly_Has_Network_Issues() { // Should expect a list of data but not getting any! Likely err thrown
    // WHEN exception thrown THEN submit emptyList by default AND throw err, updating screen
    val newClosure: () -> Unit =  { throw IOException("Error!") }
    (precautionRepository as FakePrecautionRepository).optionalClosure = newClosure
    mainActivity {
      checkSorryMessage("Sorry! Having trouble with the internet connection!")
      checkProgressBar()
    }
  }
  @Test fun view_Receives_No_Data() {
    (precautionRepository as FakePrecautionRepository).someList = emptyList()
    mainActivity {
      checkSorryMessage("Weird! Seems we don't have any available precautions to choose from!")
      checkProgressBar()
    }
  }
  @Test fun view_Receives_Some_Data() {
    val precaution1 = Precaution(null, "Standard", arrayListOf(HealthPractice(null, "Hand Hygiene", null)))
    val precaution2 = Precaution(null, "Isolation", arrayListOf(HealthPractice(null, "Foobar", null)))
    (precautionRepository as FakePrecautionRepository).someList = arrayListOf(precaution1, precaution2)
    mainActivity { checkViewLoaded() }
  }
}

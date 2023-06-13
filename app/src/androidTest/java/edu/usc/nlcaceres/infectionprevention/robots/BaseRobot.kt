package edu.usc.nlcaceres.infectionprevention.robots

import androidx.compose.ui.test.junit4.ComposeTestRule
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

// Common Base for Robots
abstract class BaseRobot {
  lateinit var composeTestRule: ComposeTestRule
}

// COULD use interface but not as simple to store state (i.e. use properties)
abstract class RoboTest {
  val mainActivity = robotRunner(MainActivityRobot::class)
  val createReportActivity = robotRunner(CreateReportRobot::class)
  val reportListFragment = robotRunner(ReportListRobot::class)
  val sortFilterFragment = robotRunner(SortFilterRobot::class)
  val settingsFragment = robotRunner(SettingsRobot::class)

  private fun <T : BaseRobot> robotRunner(cls: KClass<T>) = { composeRule: ComposeTestRule, func: T.() -> Unit ->
    cls.createInstance().apply { composeTestRule = composeRule; func() } // Create instance of robot, then run our closures
  }
}


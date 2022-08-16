package edu.usc.nlcaceres.infectionprevention.robots

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class BaseRobot // Common Base for Robots (Might be best as interface unless props/state needed)

// COULD use interface but not as simple to store state (i.e. use properties)
abstract class RoboTest {
  val mainActivity = robotRunner(MainActivityRobot::class)
  val createReportActivity = robotRunner(CreateReportRobot::class)
  val reportListActivity = robotRunner(ReportListRobot::class)
  val sortFilterActivity = robotRunner(SortFilterRobot::class)
  val settingsActivity = robotRunner(SettingsRobot::class)

  private fun <T : BaseRobot> robotRunner(cls: KClass<T>) = { func: T.() -> Unit ->
    cls.createInstance().apply { func() } // Create instance of robot, then run our closures
  }
}


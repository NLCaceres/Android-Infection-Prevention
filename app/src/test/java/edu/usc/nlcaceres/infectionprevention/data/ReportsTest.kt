package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildEmployee
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildReport
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildHealthPractice
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildLocation
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import java.time.Instant

class ReportsTest {
  @Test fun `Check Models ToString Overridden Output`() {
    val fooLocation = Location(null, "FacilityName", "UnitNum", "RoomNum")
    assertEquals("FacilityName Unit #UnitNum Room #RoomNum", fooLocation.toString())
    val fooProfession = Profession(null, "ObservedOccupation", "ServiceDiscipline")
    assertEquals("ObservedOccupation ServiceDiscipline", fooProfession.toString())
    val fooEmployee = Employee("123", "FirstName", "Surname", fooProfession)
    assertEquals("FirstName Surname, $fooProfession, ID #: 123", fooEmployee.toString())
    val fooHealthPractice = HealthPractice(null, "HealthPractice", null)
    assertEquals("HealthPractice", fooHealthPractice.toString())
  }

  @Test fun `Check Virtual Fields of Each Model`() {
    // Employee names
    val fooEmployee = Employee("123", "FirstName", "Surname", Profession(null,
      "ObservedOccupation", "ServiceDiscipline"))
    assertEquals("FirstName Surname", fooEmployee.fullName)

    // Formatted Date String
    val dateString = "2019-05-19T06:36:05.018Z" // May 19 2019 06:36AM UTC
    val timestamp = Instant.parse(dateString)
    val fooReport = buildReport(buildEmployee(), buildHealthPractice(), buildLocation(), timestamp)
    val formattedDateString = fooReport.formattedDate("America/Los_Angeles")
    val expectedFormattedDate = "May 18, 2019 11:36PM" // In LA, it's -07 hours so 11:36PM May 18 still!
    assertEquals(expectedFormattedDate, formattedDateString)
  }

  @Test fun `Check Overridden Equals Method of Precaution Class`() {
    val healthPracticeList = listOf(HealthPractice(null, "Foobar", null))
    val precaution1 = Precaution("123", "FooPrecaution", healthPracticeList)
    val diffIdAndNamePrecaution = Precaution("234", "BarPrecaution", healthPracticeList)
    assertFalse(precaution1 == diffIdAndNamePrecaution) // No matching ID nor name

    val diffNamePrecaution = Precaution("123", "FoobarPrecaution", healthPracticeList)
    assertFalse(precaution1 == diffNamePrecaution) // No matching name

    val sameIdAndNamePrecaution = Precaution("123", "FooPrecaution", healthPracticeList)
    assertTrue(precaution1 == sameIdAndNamePrecaution) // Matching name and ID

    val diffHealthPracticeList = listOf(HealthPractice(null, "Barfoo", null))
    val sameIdAndNameDiffListPrecaution = Precaution("123", "FooPrecaution", diffHealthPracticeList)
    assertTrue(precaution1 == sameIdAndNameDiffListPrecaution) // Matching name and ID BUT the healthPracticeList changed!
    // Is it fine for List<HealthPractice> changes to not affect the outcome?
    // In practice, this may occur w/ an updated Precaution
    // OR w/ an updated HealthPractice or if a new one declared this Precaution as its parent
  }
}
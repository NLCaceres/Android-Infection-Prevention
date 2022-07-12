package edu.usc.nlcaceres.infectionprevention.data

import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains // Hamcrest ships with jUnit 4.4 by default!

class ReportsTest {

  @Test fun toStringOutput() {
    val fooLocation = Location(null, "FacilityName", "UnitNum", "RoomNum")
    assertEquals("FacilityName UnitNum RoomNum", fooLocation.toString())
    val fooProfession = Profession(null, "ObservedOccupation", "ServiceDiscipline")
    assertEquals("ObservedOccupation ServiceDiscipline", fooProfession.toString())
    val fooEmployee = Employee("123", "FirstName", "Surname", fooProfession)
    assertEquals("FirstName Surname, $fooProfession, ID #: 123", fooEmployee.toString())
    val fooHealthPractice = HealthPractice(null, "HealthPractice", null)
    assertEquals("HealthPractice", fooHealthPractice.toString())
  }

  @Test fun serializationString() {
    val fooLocation = Location("123", "FacilityName", "UnitNum", "RoomNum")
    assertThat(Gson().toJson(fooLocation), StringContains("_id"))
  }

  @Test fun virtualFields() {
    val fooEmployee = Employee("123", "FirstName", "Surname", Profession(null,
      "ObservedOccupation", "ServiceDiscipline"))
    assertEquals("FirstName Surname", fooEmployee.fullName)
  }

  @Test fun precautionClassOverrides() {
    val precaution1 = Precaution("123", "FooPrecaution", null)
    val precaution2 = Precaution("234", "BarPrecaution", null)
    assertFalse(precaution1 == precaution2)
    val precaution3 = Precaution("123", "FoobarPrecaution", null)
    assertFalse(precaution1 == precaution3)
    val precaution4 = Precaution("123", "FooPrecaution", null)
    assertTrue(precaution1 == precaution4)
  }
}
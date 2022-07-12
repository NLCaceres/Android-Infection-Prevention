package edu.usc.nlcaceres.infectionprevention.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.data.Report
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains
import org.hamcrest.core.IsInstanceOf
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory

class DeserializerTest {

  @Test fun healthPracticeDeserialize() {
    val healthPracticeJson = Gson().fromJson(ReportsFactory.buildHealthPracticeJson(), JsonObject::class.java)
    val newHealthPractice = HealthPracticeDeserializer.toInstance(healthPracticeJson)
    assertThat(newHealthPractice, IsInstanceOf(HealthPractice::class.java))
    assertThat(healthPracticeJson.get("name").asString, StringContains(newHealthPractice.name))

    val healthPracticeArrJson = Gson().fromJson(ReportsFactory.buildHealthPracticeArrJson(), JsonArray::class.java)
    val healthPracticeArr = HealthPracticeDeserializer.toArray(healthPracticeArrJson)
    assertThat(healthPracticeArr, IsInstanceOf(ArrayList::class.java))
    assertThat(healthPracticeArr[0], IsInstanceOf(HealthPractice::class.java))
    for (i in healthPracticeArr.indices) assertThat((healthPracticeArrJson[i] as JsonObject).get("name").asString, StringContains(healthPracticeArr[i].name))
  }

  @Test fun precautionDeserialize() {
    val precautionJson = Gson().fromJson(ReportsFactory.buildPrecautionJson(), JsonObject::class.java)
    val newPrecaution = PrecautionDeserializer.toInstance(precautionJson)
    assertThat(newPrecaution, IsInstanceOf(Precaution::class.java))
    assertThat(precautionJson.get("name").asString, StringContains(newPrecaution.name))

    val precautionsArrJson = Gson().fromJson(ReportsFactory.buildPrecautionArrJson(), JsonArray::class.java)
    val precautionsArr = PrecautionDeserializer.toArray(precautionsArrJson)
    assertThat(precautionsArr, IsInstanceOf(ArrayList::class.java))
    assertThat(precautionsArr[0], IsInstanceOf(Precaution::class.java))
    for (i in precautionsArr.indices) assertThat((precautionsArrJson[i] as JsonObject).get("name").asString, StringContains(precautionsArr[i].name))
  }

  @Test fun reportDeserialize() {
    val reportJson = Gson().fromJson(ReportsFactory.buildReportJson(), JsonObject::class.java)
    val newReport = ReportDeserializer.toInstance(reportJson)
    assertThat(newReport, IsInstanceOf(Report::class.java))
    assertThat(reportJson.get("_id").asString, StringContains(newReport.id))

    val reportsArrJson = Gson().fromJson(ReportsFactory.buildReportArrJson(), JsonArray::class.java)
    val reportsArr = ReportDeserializer.toArray(reportsArrJson)
    assertThat(reportsArr, IsInstanceOf(ArrayList::class.java))
    assertThat(reportsArr[0], IsInstanceOf(Report::class.java))
    for (i in reportsArr.indices) assertThat((reportsArrJson[i] as JsonObject).get("_id").asString, StringContains(reportsArr[i].id))
  }
}
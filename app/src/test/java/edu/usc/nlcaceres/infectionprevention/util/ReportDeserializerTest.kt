package edu.usc.nlcaceres.infectionprevention.util

import org.junit.Test
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import edu.usc.nlcaceres.infectionprevention.data.Report
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class ReportDeserializerTest {
  @Test fun `Check Single Report Deserialization`() { // Grab files from a resources directory in Unit Tests
    // Option 1 for reading json - getResourceAsStream.bufferedReader() to pass the Gson instance a reader
    val inputStream = javaClass.classLoader?.getResourceAsStream("report.json")!!
    // Using .bufferedReader() is easier than passing in a BufferedReader(InputStreamReader(inputStream))
    // BUT there's a problem! Turns out using a reader is really only useful if you need it ONCE!
    val inputMarkSupported = inputStream.markSupported() // UNLESS you check if markSupported()
    // IF supported, mark() the start & pass in available() to get the total # of bytes left to read
    if (inputMarkSupported) { inputStream.mark(inputStream.available()) } // SO the total byte count!
    // Passing the full count prevents removing the mark so you can ALWAYS reset back to the start for another read
    val reader = inputStream.bufferedReader()
    val reportJson = Gson().fromJson(reader, JsonObject::class.java)

    if (inputMarkSupported) { inputStream.reset() } // Now we can call reset to go to the stream's start where we marked

    val reportGsonConverter = GsonBuilder().registerTypeAdapter(Report::class.java, ReportDeserializer()).create()
    val report = reportGsonConverter.fromJson(reader, Report::class.java) // Make a Gson instance to use our deserializer

    assertThat(report, IsInstanceOf(Report::class.java))
    inspectReportJson(reportJson, report, "1")
  }

  @Test fun `Check Report List Deserialization`() { // Instead of a bufferedReader, there's an easier solution!
    // Option 2 for reading json - getResource.readText() to pass the Gson instance a string
    val listInput = javaClass.classLoader?.getResource("reports.json")!!
    val listText = listInput.readText()

    val reportsArrJson = Gson().fromJson(listText, JsonArray::class.java)
    val reportsArr = GsonBuilder().registerTypeAdapter(Report::class.java, ReportDeserializer())
      .create().fromJson<List<Report>>(listText, TypeToken.getParameterized(ArrayList::class.java, Report::class.java).type)

    assertThat(reportsArr, IsInstanceOf(ArrayList::class.java))
    for (i in reportsArr.indices) {
      inspectReportJson(reportsArrJson[i] as JsonObject, reportsArr[i], "${i + 1}")
    }
  }

  private fun inspectReportJson(reportJson: JsonObject, report: Report, expectedID: String) {
    assertEquals(reportJson.get("id").asString, report.id) // Could do assertThat(string, StringContains(someVal)) for flexibility
    assertEquals(expectedID, report.id)

    assertNotNull(report.employee)
    val employeeJson = reportJson.get("employee").asJsonObject
    assertEquals(employeeJson.get("id").asString, report.employee?.id)
    assertEquals(expectedID, report.employee?.id)
    assertEquals(employeeJson.get("firstName").asString, report.employee?.firstName)
    assertEquals(employeeJson.get("surname").asString, report.employee?.surname)

    assertNotNull(report.healthPractice)
    val healthPracticeJson = reportJson.get("healthPractice").asJsonObject
    assertEquals(healthPracticeJson.get("id").asString, report.healthPractice?.id)
    assertEquals(expectedID, report.healthPractice?.id)
    assertEquals(healthPracticeJson.get("name").asString, report.healthPractice?.name)

    assertNotNull(report.location)
    val locationJson = reportJson.get("location").asJsonObject
    assertEquals(locationJson.get("id").asString, report.location?.id)
    assertEquals(expectedID, report.location?.id)
    assertEquals(locationJson.get("facilityName").asString, report.location?.facilityName)
    assertEquals(locationJson.get("unitNum").asString, report.location?.unitNum)
    assertEquals(locationJson.get("roomNum").asString, report.location?.roomNum)

    val dateJson = reportJson.get("date").asString
    assertEquals(dateJson, report.date.toString())
  }
}
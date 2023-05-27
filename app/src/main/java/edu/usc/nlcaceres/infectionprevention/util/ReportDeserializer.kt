package edu.usc.nlcaceres.infectionprevention.util

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext
import java.lang.reflect.Type
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.data.Report
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import java.time.Instant

class ReportDeserializer : JsonDeserializer<Report> {
  override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Report? {
    json?.asJsonObject?.run { // Use run instead of let to access contents!
      val id = get("id").asString
      val employee = get("employee").asJsonObject.run {
        Employee(get("id").asString, get("firstName").asString, get("surname").asString, null)
      }
      val healthPractice = get("healthPractice").asJsonObject.run {
        HealthPractice(get("id").asString, get("name").asString, null)
      }
      val location = get("location").asJsonObject.run {
        Location(get("id").asString, get("facilityName").asString, get("unitNum").asString, get("roomNum").asString)
      }
      // Example "2019-05-19T06:36:05.018Z" vs the formatted_date_reported kv-pair
      val date = Instant.parse(get("date").asString)
      return Report(id, employee, healthPractice, location, date)
    }
    return null
  }
}

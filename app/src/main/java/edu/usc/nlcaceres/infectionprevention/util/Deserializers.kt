package edu.usc.nlcaceres.infectionprevention.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale
import edu.usc.nlcaceres.infectionprevention.data.*

class ReportDeserializer : JsonDeserializer<Report> {
  override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Report? {
    json?.asJsonObject?.run { // Use run instead of let to access contents!
      val id = get("_id").asString
      val employee = get("employee").asJsonObject.run {
        Employee(get("_id").asString, get("first_name").asString, get("surname").asString, null)
      }
      val healthPractice = get("healthPractice").asJsonObject.run {
        HealthPractice(get("_id").asString, get("name").asString, null)
      }
      val location = get("location").asJsonObject.run {
        Location(get("_id").asString, get("facilityName").asString, get("unitNum").asString, get("roomNum").asString)
      }
      val date = SimpleDateFormat("MMM dd, yy, h:mma", Locale.getDefault()).parse(get("formatted_date_reported").asString)
      return Report(id, employee, healthPractice, location, date)
    }
    return null
  }
}

class PrecautionDeserializer : JsonDeserializer<Precaution> {
  @Throws(JsonParseException::class)
  override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Precaution {
    val jsonObj = json.asJsonObject
    val name = jsonObj["name"].asString
    val practicesArr = ArrayList<HealthPractice>()
    val practicesJsonArr = jsonObj["practices"].asJsonArray
    for (practiceJson in practicesJsonArr) {
      val practiceObj = practiceJson.asJsonObject
      val practicePrecautionType = if (name == "Standard") PrecautionType.Standard else PrecautionType.Isolation
      practicesArr.add(HealthPractice(practiceObj["_id"].asString, practiceObj["name"].asString, practicePrecautionType))
    }
    return Precaution(jsonObj["_id"].asString, name, practicesArr)
  }
}

class HealthPracticeDeserializer : JsonDeserializer<HealthPractice> { // Not a big problem to return a null as long as you handle it later
  override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): HealthPractice? {
    val jsonObj = json?.asJsonObject
    val id = jsonObj?.get("_id")?.asString
    val precautionTypeStr = jsonObj?.get("precautionType")?.asJsonObject?.get("name")?.asString
    val precautionType = precautionTypeStr?.let { if (it == "Standard") PrecautionType.Standard else PrecautionType.Isolation }
    val healthPracticeName = jsonObj?.get("name")?.asString
    return if (healthPracticeName != null && precautionType != null) HealthPractice(id, healthPracticeName, precautionType) else null
  }
}
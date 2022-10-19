package edu.usc.nlcaceres.infectionprevention.util

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonParseException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.PrecautionType
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.data.Report
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import java.time.Instant

interface JsonDeserialization<T> {
  fun buildGson(): Gson
  fun toArray(jsonResponse: JsonArray): List<T>
  fun toInstance(jsonResponse: JsonObject): T
  fun toMap(jsonResponse: JsonObject): Map<String, *> = buildGson().fromJson(jsonResponse, // As of GSON 2.8.6 jsonResponse.toString() not needed
    TypeToken.getParameterized(Map::class.java, String::class.java).type)
}

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
      // Example "2019-05-19T06:36:05.018Z" vs the formatted_date_reported kv-pair
      val date = Instant.parse(get("date_reported").asString)
      return Report(id, employee, healthPractice, location, date)
    }
    return null
  }
  companion object : JsonDeserialization<Report> {
    override fun buildGson(): Gson = GsonBuilder().registerTypeAdapter(Report::class.java, ReportDeserializer()).create()
    override fun toArray(jsonResponse: JsonArray): List<Report> = buildGson().fromJson(jsonResponse, TypeToken.
      getParameterized(ArrayList::class.java, Report::class.java).type)
    override fun toInstance(jsonResponse: JsonObject): Report = buildGson().fromJson(jsonResponse, Report::class.java)
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
  companion object : JsonDeserialization<Precaution> {
    override fun buildGson(): Gson = GsonBuilder().registerTypeAdapter(Precaution::class.java, PrecautionDeserializer()).create()
    override fun toArray(jsonResponse: JsonArray): List<Precaution> = buildGson().fromJson<List<Precaution>>(jsonResponse.toString(),
      TypeToken.getParameterized(ArrayList::class.java, Precaution::class.java).type)
    override fun toInstance(jsonResponse: JsonObject): Precaution = buildGson().fromJson(jsonResponse.toString(), Precaution::class.java)
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
  companion object : JsonDeserialization<HealthPractice> {
    override fun buildGson(): Gson = GsonBuilder().registerTypeAdapter(HealthPractice::class.java, HealthPracticeDeserializer()).create()
    override fun toArray(jsonResponse: JsonArray): List<HealthPractice> = buildGson().fromJson<List<HealthPractice>>(jsonResponse.toString(),
      TypeToken.getParameterized(ArrayList::class.java, HealthPractice::class.java).type)
    override fun toInstance(jsonResponse: JsonObject): HealthPractice = buildGson().fromJson(jsonResponse.toString(), HealthPractice::class.java)
  }
}
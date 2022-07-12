package edu.usc.nlcaceres.infectionprevention.data

import edu.usc.nlcaceres.infectionprevention.util.HealthPracticeDeserializer
import edu.usc.nlcaceres.infectionprevention.util.PrecautionDeserializer
import edu.usc.nlcaceres.infectionprevention.util.ReportDeserializer
import java.util.Date

class PrecautionDTO(private val map: Map<String, Any>) {
  private val _id : String? by map
  private val name: String by map
  private val practices : List<HealthPractice>? by map

  fun toPrecaution() = Precaution(this._id, this.name, this.practices)
  companion object Deserializer {
    val deserializer: PrecautionDeserializer by lazy { PrecautionDeserializer() }
  }
}

class HealthPracticeDTO(private val map: Map<String, Any>) {
  private val _id : String? by map
  private val name: String by map
  private val precautionType : PrecautionType? by map

  fun toHealthPractice() = HealthPractice(this._id, this.name, this.precautionType)

  companion object Deserializer {
    val deserializer: HealthPracticeDeserializer by lazy { HealthPracticeDeserializer() }
  }
}

class ReportDTO(map: Map<String, Any>) {
  private val _id : String? by map
  private val employee: Employee by map
  private val healthPractice : HealthPractice? by map
  private val location : Location? by map
  private val date: Date? by map

  fun toReport() = Report(this._id, this.employee, this.healthPractice, this.location, this.date)

  companion object Deserializer {
    val deserializer: ReportDeserializer by lazy { ReportDeserializer() }
  }
}
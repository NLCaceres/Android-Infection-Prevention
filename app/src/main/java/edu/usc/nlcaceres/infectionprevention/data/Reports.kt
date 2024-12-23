package edu.usc.nlcaceres.infectionprevention.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// May have to refactor model to accommodate facilityName, UnitNum, and roomNum
data class Location(val id : String?, val facilityName : String, val unitNum : String, val roomNum : String) {
  override fun toString(): String = "$facilityName Unit #$unitNum Room #$roomNum"
}

data class Profession(val id : String?, val observedOccupation : String, val serviceDiscipline : String) {
  override fun toString(): String = "$observedOccupation $serviceDiscipline"
}

data class Employee(val id : String?, val firstName : String, val surname : String, val profession : Profession?) {
  override fun toString(): String = "$firstName $surname, $profession, ID #: $id"
  val fullName get() = "$firstName $surname"
}

data class Precaution(val id : String?, val name : String, val healthPractices : List<HealthPractice>) {
  override fun equals(other: Any?): Boolean {   // If overriding equals then must override hashCode
    // Can NOT change params if overriding fun. CAREFUL
    if (other is Precaution) {
      if (this.id == other.id && this.name == other.name) {
        return true
      }
    }
    return false
  }
  // Kotlin/Java HashCode Algorithm takes hashCode of each var and multiplies by 31 (contentHashCode solves collections/arrays)
  override fun hashCode(): Int {
    var result = (this.id.hashCode() * 31) + (this.name.hashCode() * 31)
    this.healthPractices.apply { result += hashCode() }
    return result
  }
}

enum class PrecautionType { // If other precautionTypes are used then this will not work! Enums best for static, not dynamically generated types
  // Could make a constructor as well with specific vals that can be accessed AND even calculated vals
  Standard, Isolation
} // Sealed Classes also an alternative to enums - especially in When statements (using the result)

data class HealthPractice(val id : String?, val name : String, val precaution : Precaution?) {
  // May simply use Precaution Name as String instead of enum/sealed class for flexibility
  override fun toString(): String = name
}

// SerializedName for date prop ensures proper json format
data class Report(val id : String?, val employee : Employee?, val healthPractice : HealthPractice?,
                  val location : Location?, val date: Instant) {
  fun formattedDate(timeZoneId: String): String {
    // TimeZone database ID can be grabbed from Android via TimeZone.getDefault().id
    val zonedDateTime = date.atZone(ZoneId.of(timeZoneId))
    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mma")
    return zonedDateTime.format(dateTimeFormatter)
  }
}
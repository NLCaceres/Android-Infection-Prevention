package edu.usc.nlcaceres.infectionprevention

import java.util.Date

// This DOES CONFUSE ANDROID - IMPLEMENT AND GET RID OF OLD ONE - DON'T forget Java interop

data class Location(val id : String?, val facilityName : String, val unitNum : String, val roomNum : String) {
    val fullLocation : String = "$facilityName $unitNum $roomNum"
}

data class Profession(val id : String?, val observedOccupation : String, val serviceDiscipline : String) {
    val fullProfession : String = "$observedOccupation $serviceDiscipline"
}

data class Employee(val id : String?, val firstName : String, val surname : String, val profession : Profession?) {
    val fullName : String = "$firstName $surname"
}

data class Precaution(val id : String?, val name : String, val practices : Array<HealthPractice>?) {
    override fun equals(other: Any?): Boolean {
        // Can NOT change params if overriding fun. CAREFUL
        if (other is Precaution) {
            if (this.id == other.id) {
                return true
            }
        }
        return false
    }

//    override fun hashCode(): Int {
//        return super.hashCode()
//    }
}

data class HealthPractice(val id : String?, val name : String, val precaution : Precaution)

data class Report(val id : String?, val employee : Employee?, val healthPractice : HealthPractice, val location : Location, val date: Date)
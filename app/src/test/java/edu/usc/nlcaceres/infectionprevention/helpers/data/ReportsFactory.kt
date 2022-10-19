package edu.usc.nlcaceres.infectionprevention.helpers.data

import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.Profession
import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.data.PrecautionType
import edu.usc.nlcaceres.infectionprevention.data.PrecautionType.Standard
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Report
import java.time.Instant

class ReportsFactory {
  companion object Factory {
    private fun buildArrJson(numItems: Int, jsonStringBuilder: () -> String) = buildString {
      append("[ ")
      for (i in 1 until numItems) append("${jsonStringBuilder()}, ")
      append("${jsonStringBuilder()} ]")
    }

    private var createdLocations = 0
    fun buildLocation() = Location("locationId$createdLocations", "facility$createdLocations",
      "unit$createdLocations", "room${createdLocations++}")
    fun buildLocationJson() = """{ "_id": "locationId$createdLocations", "facilityName": "facility$createdLocations",
      |"unitNum": "unit$createdLocations", "roomNum": "room${createdLocations++}" }""".trimMargin().replace('\n', ' ')
    fun buildLocationArrJson(numLocations: Int = 2) = buildArrJson(numLocations, this::buildLocationJson)

    private var createdProfessions = 0
    fun buildProfession() = Profession("professionId$createdProfessions",
      "occupation$createdProfessions", "discipline${createdProfessions++}")
    fun buildProfessionJson() = """{ "_id": "professionId$createdProfessions", "occupation": "occupation$createdProfessions",
      |"discipline": "discipline${createdProfessions++}" }""".trimMargin().replace('\n', ' ')
    fun buildProfessionArrJson(numProfessions: Int = 2) = buildArrJson(numProfessions, this::buildProfessionJson)

    private var createdEmployees = 0
    fun buildEmployee(profession : Profession? = null) = Employee("employeeId$createdEmployees",
      "firstName$createdEmployees", "surname${createdEmployees++}", profession ?: buildProfession())
    fun buildEmployeeJson() = """{ "_id": "employeeId$createdEmployees", "first_name": "facility$createdEmployees",
      |"surname": "unit${createdEmployees++}" }""".trimMargin().replace('\n', ' ')
    fun buildEmployeeArrJson(numEmployees: Int = 2) = buildArrJson(numEmployees, Factory::buildEmployeeJson) // Turns out the 'this' keyword isn't necessary!

    private var createdPrecautions = 0
    fun buildPrecaution(precautionType : PrecautionType = Standard, numHealthPractices: Int = 2) =
      Precaution("precautionId$createdPrecautions", "precaution${createdPrecautions++}",
        Array(numHealthPractices) { buildHealthPractice(precautionType) }.toCollection(ArrayList()))
    fun buildPrecautionJson() = """{ "_id": "precautionId$createdPrecautions", "name": "facility${createdPrecautions++}",
      |"practices": [] }""".trimMargin().replace('\n', ' ')
    fun buildPrecautionArrJson(numPrecautions: Int = 2) = buildArrJson(numPrecautions, Factory::buildPrecautionJson)

    private var createdHealthPractices = 0
    fun buildHealthPractice(precautionType : PrecautionType = Standard) =
      HealthPractice("healthPracticeId$createdHealthPractices", "healthPractice${createdHealthPractices++}", precautionType)
    fun buildHealthPracticeJson() = """{ "_id": "healthPracticeId$createdHealthPractices", "name": "healthPractice${createdHealthPractices++}",
      |"precautionType": { "name": "Standard" } }""".trimMargin().replace('\n', ' ')
    fun buildHealthPracticeArrJson(numHealthPractices: Int = 2) = buildArrJson(numHealthPractices, Factory::buildHealthPracticeJson)

    private var createdReports = 0
    fun buildReport(employee: Employee? = null, healthPractice: HealthPractice? = null, location: Location? = null,
                    date: Instant = Instant.now()) = Report("reportId${createdReports++}", employee ?: buildEmployee(),
         healthPractice ?: buildHealthPractice(Standard), location ?: buildLocation(), date)
    fun buildReportJson() = """{ "_id": "reportId${createdReports++}", "employee": ${buildEmployeeJson()},
      |"healthPractice": ${buildHealthPracticeJson()}, "location": ${buildLocationJson()},
      |"date_reported": "${dateWithFormat()}" }""".trimMargin().replace('\n', ' ')
    fun dateWithFormat() = "2019-05-19T06:36:05.018Z"
    fun buildReportArrJson(numReports: Int = 2) = buildArrJson(numReports, Factory::buildReportJson)
  }
}
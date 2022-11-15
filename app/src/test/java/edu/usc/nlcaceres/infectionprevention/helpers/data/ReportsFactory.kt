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
    private var createdLocations = 0
    fun buildLocation() = Location("locationId$createdLocations", "facility$createdLocations",
      "unit$createdLocations", "room${createdLocations++}")

    private var createdProfessions = 0
    fun buildProfession() = Profession("professionId$createdProfessions",
      "occupation$createdProfessions", "discipline${createdProfessions++}")

    private var createdEmployees = 0
    fun buildEmployee(profession : Profession? = null) = Employee("employeeId$createdEmployees",
      "firstName$createdEmployees", "surname${createdEmployees++}", profession ?: buildProfession())

    private var createdPrecautions = 0
    fun buildPrecaution(precautionType : PrecautionType = Standard, numHealthPractices: Int = 2) =
      Precaution("precautionId$createdPrecautions", "precaution${createdPrecautions++}",
        Array(numHealthPractices) { buildHealthPractice(precautionType) }.toCollection(ArrayList()))

    private var createdHealthPractices = 0
    fun buildHealthPractice(precautionType : PrecautionType = Standard) =
      HealthPractice("healthPracticeId$createdHealthPractices", "healthPractice${createdHealthPractices++}", precautionType)

    private var createdReports = 0
    fun buildReport(employee: Employee? = null, healthPractice: HealthPractice? = null, location: Location? = null,
                    date: Instant = Instant.now()) = Report("reportId${createdReports++}", employee ?: buildEmployee(),
         healthPractice ?: buildHealthPractice(Standard), location ?: buildLocation(), date)
  }
}
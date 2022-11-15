package edu.usc.nlcaceres.infectionprevention.helpers.di

import edu.usc.nlcaceres.infectionprevention.data.Employee
import edu.usc.nlcaceres.infectionprevention.data.EmployeeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeEmployeeRepository: EmployeeRepository {
  var someList: List<Employee> = emptyList()

  var needDelay: Boolean = false
  var optionalClosure: () -> Unit = { }

  init { populateList() } // Create the list when Hilt binds & creates the fake

  override fun fetchEmployeeList(): Flow<List<Employee>> {
    return flow {
      emit(emptyList())
      emit(someList)
      if (needDelay) { delay(3000) }
      optionalClosure.invoke()
    }
  }

  fun populateList() { someList = makeList() }
  fun clearList() { someList = emptyList() }

  companion object EmployeeFactory {
    fun makeList(): List<Employee> {
      return arrayListOf(Employee(null, "John", "Smith", null),
        Employee(null, "Jill", "Chambers", null),
        Employee(null, "Victor", "Richards", null),
        Employee(null, "Melody", "Rios", null),
        Employee(null, "Brian", "Ishida", null))
    }
  }
}
package edu.usc.nlcaceres.infectionprevention.helpers.di

import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.HealthPracticeRepository
import edu.usc.nlcaceres.infectionprevention.data.PrecautionType.Standard
import edu.usc.nlcaceres.infectionprevention.data.PrecautionType.Isolation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeHealthPracticeRepository: HealthPracticeRepository {
  var someList: List<HealthPractice> = emptyList()

  var needDelay: Boolean = false
  var optionalClosure: () -> Unit = { }

  init { populateList() }

  override fun fetchHealthPracticeList(): Flow<List<HealthPractice>> {
    return flow {
      emit(emptyList())
      emit(someList)
      if (needDelay) { delay(3000) }
      optionalClosure.invoke()
    }
  }

  fun populateList() { someList = makeList() }
  fun clearList() { someList = emptyList() }

  companion object Factory {
    fun makeList(): List<HealthPractice> {
      return arrayListOf(HealthPractice(null, "Hand Hygiene", Standard), HealthPractice(null, "PPE", Standard),
        HealthPractice(null, "Airborne", Isolation), HealthPractice(null, "Droplet", Isolation),
        HealthPractice(null, "Contact", Isolation), HealthPractice(null, "Contact Enteric", Isolation))
    }
  }
}
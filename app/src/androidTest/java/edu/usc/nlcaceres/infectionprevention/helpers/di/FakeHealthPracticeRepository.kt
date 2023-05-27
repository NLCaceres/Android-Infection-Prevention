package edu.usc.nlcaceres.infectionprevention.helpers.di

import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.HealthPracticeRepository
import edu.usc.nlcaceres.infectionprevention.data.Precaution
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

  companion object HealthPracticeFactory {
    fun makeList(): List<HealthPractice> {
      val standardPrecaution = Precaution(null, "Standard", emptyList())
      val isolationPrecaution = Precaution(null, "Isolation", emptyList())

      return arrayListOf(
        HealthPractice(null, "Hand Hygiene", standardPrecaution),
        HealthPractice(null, "PPE", standardPrecaution),
        HealthPractice(null, "Airborne", isolationPrecaution),
        HealthPractice(null, "Droplet", isolationPrecaution),
        HealthPractice(null, "Contact", isolationPrecaution),
        HealthPractice(null, "Contact Enteric", isolationPrecaution)
      )
    }
  }
}
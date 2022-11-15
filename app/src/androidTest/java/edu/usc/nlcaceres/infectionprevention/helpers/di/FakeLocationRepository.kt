package edu.usc.nlcaceres.infectionprevention.helpers.di

import edu.usc.nlcaceres.infectionprevention.data.Location
import edu.usc.nlcaceres.infectionprevention.data.LocationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeLocationRepository: LocationRepository {
  var someList: List<Location> = emptyList()

  var needDelay: Boolean = false
  var optionalClosure: () -> Unit = { }

  init { populateList() }

  override fun fetchLocationList(): Flow<List<Location>> {
    return flow {
      emit(emptyList())
      emit(someList)
      if (needDelay) { delay(3000) }
      optionalClosure.invoke()
    }
  }

  fun populateList() { someList = makeList() }
  fun clearList() { someList = emptyList() }

  companion object LocationFactory {
    fun makeList(): List<Location> {
      val id: String? = null // Simple default null ID
      val usc = "USC"; val hsc = "HSC" // Facility Names
      return arrayListOf(Location(id, usc, "2", "123"), // USC Unit 2 Room: 123
        Location(id, usc, "4", "202"), // USC Unit 4 Room: 202
        Location(id, hsc, "3", "213"), // HSC Unit 3 Room: 213
        Location(id, hsc, "3", "321"), // HSC Unit 3 Room: 321
        Location(id, hsc, "5", "121")) // HSC Unit 5 Room: 121
    }
  }
}
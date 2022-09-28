package edu.usc.nlcaceres.infectionprevention.helpers.di

import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/* Versatile fake repository that avoids having to use real server calls AND
 can avoid depending on Espresso Idling. Attempts to replicate typical repository behavior WHILE
 Also providing a means of checking load state which typically passes too fast for Espresso to check
 The optional closure can't perform Espresso checks BUT could act as a basic callback if needed
 AND the optional closure can throw if flow needs to fail in a particular way */
class FakePrecautionRepository: PrecautionRepository {
  var someList: List<Precaution> = emptyList()

  var needDelay: Boolean = false
  var optionalClosure: () -> Unit = { } // COULD add suspend keyword without affecting tests BUT seemingly unneeded

  override fun fetchPrecautionList(): Flow<List<Precaution>> {
    return flow { // Can't return inside flow builder
      emit(emptyList())
      emit(someList)
      if (needDelay) { delay(3000) }
      // Could call following from helper function BUT this seems to be enough time to let tests set closure ref
      optionalClosure.invoke() // ALSO acts more like actual repository
    }
  }
}
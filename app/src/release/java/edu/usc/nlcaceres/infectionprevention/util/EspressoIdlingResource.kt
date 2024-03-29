package edu.usc.nlcaceres.infectionprevention.util

/* Since release version of app shouldn't care about this, we can allow the obj to be created
 BUT it'll never run anything or contain anything! */
object EspressoIdlingResource {
    fun increment() { }

    fun decrement() { }
}

inline fun <T> idleAppWith(function: () -> T): T {
    EspressoIdlingResource.increment() // Begin idling
    return try { function() } // Wrap in try block so even if it throws, we STILL call finally block
    finally { EspressoIdlingResource.decrement() } // Once count hits 0 then done idling
}
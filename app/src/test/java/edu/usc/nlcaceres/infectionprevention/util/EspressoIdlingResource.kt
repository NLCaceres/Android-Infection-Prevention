package edu.usc.nlcaceres.infectionprevention.util

/* Since the normal CountingIdlingResource from debug uses Android Text functions
* It's important to overwrite it here so that normal unit tests can run as normal without needing those Android Resources
* For Robolectric based unit tests, it remains to be seen any that might be affected by the lack of idleAppWith {} */
object EspressoIdlingResource {
    fun increment() { }

    fun decrement() { }
}

inline fun <T> idleAppWith(function: () -> T): T {
    EspressoIdlingResource.increment() // Begin idling
    return try { function() } // Wrap in try block so even if it throws, we STILL call finally block
    finally { EspressoIdlingResource.decrement() } // Once count hits 0 then done idling
}
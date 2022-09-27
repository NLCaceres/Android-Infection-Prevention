package edu.usc.nlcaceres.infectionprevention.util

import androidx.test.espresso.idling.CountingIdlingResource

/* Unlike the release version, while testing via our debug build (as intended and by default!)
 We need to make sure we properly await network calls in our instrumented tests
 We could also mock out network conditions BUT instrumented tests are high fidelity
 AKA instrumented tests should be fairly similar to real life app use!
 BUT ALSO it's fairly tough to mock out instrumented tests anyway,
 SO Hilt (or a ServiceLocator) is only way if desired! */
object EspressoIdlingResource {
    private const val RESOURCE = "GLOBAL" // Could be any name (all have to be unique if multiple idlingRes

    @JvmField val countingIdlingResource = CountingIdlingResource(RESOURCE)
    // Once above field hits 0, instrumentedTest given clear to keep searching via Espresso
    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

inline fun <T> idleAppWith(function: () -> T): T {
    EspressoIdlingResource.increment() // Begin idling
    return try { function() } // Wrap in try block so even if it throws, we STILL call finally block
    finally { EspressoIdlingResource.decrement() } // Once count hits 0 then done idling
}
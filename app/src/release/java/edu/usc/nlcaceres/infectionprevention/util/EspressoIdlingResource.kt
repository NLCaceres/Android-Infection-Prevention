package edu.usc.nlcaceres.infectionprevention.util

/* Since release version of app shouldn't care about this, we can allow the obj to be created
* BUT it'll never run anything or contain anything! */
object EspressoIdlingResource {
    fun increment() {}

    fun decrement() { }
}
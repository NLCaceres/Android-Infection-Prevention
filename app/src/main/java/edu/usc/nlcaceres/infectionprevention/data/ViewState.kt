package edu.usc.nlcaceres.infectionprevention.data

/* Alternative to combining flows is to combine all the value into a singular piece of data that
  represents a new event for the View to react to, letting each event define particular state on emittance
  Ex: emit(ViewState.Loading(someData)) meanwhile the observing view can handle any data knowing it's in progress
      emit(ViewState.success(someData)) meanwhile the observing view can handle any data knowing all work should be complete
  Naturally this line of thinking lends to there being many ways to represent the view's state in the form of a class
  Here we try to make it as widely applicable as possible, confining the state in a sealed class to 3 general states */
sealed class ViewState<out R> {
  data class Success<out T>(val value: T) : ViewState<T>()
  data class Loading<out T>(val value: T) : ViewState<T>()
  data class Failure(val exception: Exception) : ViewState<Nothing>()

  override fun toString(): String {
    return when (this) {
      is Success -> "Success! Got following data: $value"
      is Loading -> "Loading! Currently have following data: $value"
      is Failure -> "Error! Just threw $exception"
    }
  }

  val isSuccessful get() = this is Success
  val isLoading get() = this is Loading
  val isFailure get() = this is Failure
}

// Returns a default value that CAN be an optional, so ultimately still need to handle nullability
fun <R, T : R> ViewState<T>.getOrDefault(default: R): R {
  return when (this) {
    is ViewState.Failure -> default
    is ViewState.Success -> value
    is ViewState.Loading -> value
  }
}

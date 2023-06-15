package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.*
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ViewModelMain @Inject constructor(private val precautionRepository: PrecautionRepository) : ViewModel() {
  private val _isLoading = MutableLiveData(false)
  private val loadingFlow = _isLoading.asFlow()
  val isLoading: LiveData<Boolean> = _isLoading

  // Even if exception thrown, onCompletion is still called BUT CAN'T use onCompletion next to asLiveData() below,
  // the combine() seems to cause onCompletion to be called inconsistently, an uncombined flow seems to run as expected!
  private val precautionsFlow = precautionRepository.fetchPrecautionList()
    .onCompletion { _isLoading.value = false; EspressoIdlingResource.decrement() }
  // Can use currentCoroutineContext() from inside the flow block to notice that it produces Pair on main thread
  private val loadingPrecautionsFlow = loadingFlow // Combine loading state with precautionList
    .combine(precautionsFlow) { loading, newList -> Pair(loading, newList) }
    .onStart { _isLoading.value = true }
    .catch { e -> // No more flow collection if catch runs so let snackbarMessage handle the rest in view
        _snackbarMessage.value = when (e) { // IOException covers a number of Retrofit or server issues
          is IOException -> "Sorry! Having trouble with the internet connection!"
          else -> "Sorry! Seems we're having an issue on our end!" // Always important to have a default!
        }
    }

  // Let lazy delegate init once with a MutableLiveData list of precautions that starts flow on observe()
  private val _precautionState by lazy { loadingPrecautionsFlow.asLiveData() }
  val precautionState: LiveData<Pair<Boolean, List<Precaution>>> = _precautionState

  fun precautionListEmpty() = precautionState.value?.second?.isEmpty() ?: true
  // Get all precautions' names in a list and each precaution's healthPractices' names in another list
  fun getNamesLists(precautionList: List<Precaution> =
                      precautionState.value?.second ?: emptyList()): Pair<ArrayList<String>, ArrayList<String>> {
    val precautionNames = arrayListOf<String>()
    val healthPracticeNames = arrayListOf<String>()
    precautionList.forEach { precaution ->
      precautionNames.add(precaution.name)
      precaution.healthPractices.forEach { healthPractice -> healthPracticeNames.add(healthPractice.name) }
    }
    return Pair(precautionNames, healthPracticeNames)
  }

  private val _snackbarMessage = MutableLiveData("")
  val snackbarMessage: LiveData<String> = _snackbarMessage // Err msg displayed as Snackbar or alertDialog
  // Can let UI calculate if sorryMsg needs displaying (signaling empty list returned)
}


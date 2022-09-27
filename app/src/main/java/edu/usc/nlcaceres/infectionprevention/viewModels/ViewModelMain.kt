package edu.usc.nlcaceres.infectionprevention.viewModels

import android.util.Log
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

  // Even if exception thrown, onCompletion is still called BUT
  // Can't use onCompletion before asLiveData() below, the combine() seems to cause onCompletion to be called inconsistently
  // If it weren't a combined flow, it seems like it wouldn't be a problem and would be called as expected
  private val precautionsFlow = precautionRepository.fetchPrecautionList()
    .onCompletion {
      _isLoading.value = false
      EspressoIdlingResource.decrement()
    }
  private val loadingPrecautionsFlow = loadingFlow // Combine loading state with precautionList
    .combine(precautionsFlow) { loading, newList -> Pair(loading, newList) }
  // Let lazy delegate init once with a MutableLiveData list of precautions that starts flow on creation
  private val _precautionState by lazy {
    loadingPrecautionsFlow.catch { e -> // No more flow collection if catch runs so let toastMessage handle the rest in view
          _toastMessage.value = when (e) { // IOException covers a number of Retrofit or server issues
            is IOException -> "Sorry! Having trouble with the internet connection!"
            else -> "Sorry! Seems we're having an issue on our end!" // Always important to have a default!
          }
      }.asLiveData().also { _isLoading.value = true }
    // Can use currentCoroutineContext() from inside the flow block to notice that it produces Pair on main thread
  }
  val precautionState: LiveData<Pair<Boolean, List<Precaution>>> = _precautionState

  private val _toastMessage = MutableLiveData("")
  val toastMessage: LiveData<String> = _toastMessage // Err msg displayed as toast or alertDialog
  // Can let UI calculate if sorryMsg needs displaying (signaling empty list returned)
}


package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.data.Report
import edu.usc.nlcaceres.infectionprevention.data.ReportRepository
import edu.usc.nlcaceres.infectionprevention.domain.SortFilterReportsUseCase
import edu.usc.nlcaceres.infectionprevention.util.EspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ViewModelReportList @Inject constructor(private val reportRepository: ReportRepository,
                                              private val sortFilterUseCase: SortFilterReportsUseCase = SortFilterReportsUseCase(),
                                              private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
  // Since Lists can't "add()", best way to keep this state simple.
  var selectedFilters: ArrayList<FilterItem> = arrayListOf()

  private val _isLoading = MutableLiveData(false)
  private val loadingFlow = _isLoading.asFlow() // Just used to combine with reportsFlow
  val isLoading: LiveData<Boolean> = _isLoading

  private val reportsFlow = reportRepository.fetchReportList()
    .onCompletion { _isLoading.value = false; EspressoIdlingResource.decrement() }
  private val loadingReportsFlow = loadingFlow // Combine loading state with reportList
    .combine(reportsFlow) { loading, newList -> Pair(loading, newList) }.onStart { _isLoading.value = true }
    .catch { e -> // No more flow collection if catch runs so let toastMessage handle the rest in view
        _toastMessage.value = when (e) { // IOException covers a number of Retrofit or server issues
          is IOException -> "Sorry! Having trouble with the internet connection!"
          else -> "Sorry! Seems we're having an issue on our end!" // Always important to have a default!
        }
    }

  private val _reportState by lazy { loadingReportsFlow.asLiveData() }
  val reportState: LiveData<Pair<Boolean, List<Report>>> = _reportState
  // Below is an example of the ONLY way to restart a flow in a LiveData if it completes successfully or due to Exception
  fun refreshReportList() { loadingReportsFlow.launchIn(viewModelScope) } // SINCE this'll re-activate the LiveData and emit new values
  fun reportListEmpty() = reportState.value?.second?.isEmpty() ?: true
  fun sortedFilteredList(list: List<Report> = reportState.value?.second ?: emptyList()) =
    sortFilterUseCase.beginSortAndFilter(list, selectedFilters)
  fun textFilteredList(text: String, list: List<Report> = reportState.value?.second ?: emptyList()) =
    sortFilterUseCase.filterReportsByText(text, sortedFilteredList(list))

  private val _toastMessage = MutableLiveData("")
  val toastMessage: LiveData<String> = _toastMessage // Err msg displayed as toast or alertDialog
  // Can let UI calculate if sorryMsg needs displaying (signaling empty list returned)
}
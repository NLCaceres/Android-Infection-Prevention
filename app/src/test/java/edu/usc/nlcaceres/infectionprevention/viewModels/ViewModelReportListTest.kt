package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.lifecycle.Observer
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.usc.nlcaceres.infectionprevention.data.*
import edu.usc.nlcaceres.infectionprevention.domain.SortFilterReportsUseCase
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory
import edu.usc.nlcaceres.infectionprevention.helpers.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import org.mockito.quality.Strictness
import java.io.IOException
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ViewModelReportListTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()
  @get:Rule
  var executorRule = InstantTaskExecutorRule()

  private lateinit var fakeRepository: ReportRepository
  @Mock lateinit var reportObserver: Observer<Pair<Boolean, List<Report>>>
  @Mock lateinit var loadingObserver: Observer<Boolean>
  @Mock lateinit var toastObserver: Observer<String>

  @Test fun `Observe Report State`() {
    val reportsList = arrayListOf(ReportsFactory.buildReport(), ReportsFactory.buildReport())
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { emit(emptyList()); emit(reportsList) } }
    val viewModel = ViewModelReportList(fakeRepository)

    viewModel.reportState.observeForever(reportObserver)
    viewModel.reportState.removeObserver(reportObserver)

    verify(reportObserver, times(3)).onChanged(any())
    val inOrderCheck = inOrder(reportObserver)
    val firstExpectedPair = Pair(true, emptyList<Report>()) // No observation of default false value in loading liveData
    inOrderCheck.verify(reportObserver, times(1)).onChanged(firstExpectedPair)
    val secondExpectedPair = Pair(true, reportsList)
    inOrderCheck.verify(reportObserver, times(1)).onChanged(secondExpectedPair)
    val thirdExpectedPair = Pair(false, reportsList) // OnCompletion called!
    inOrderCheck.verify(reportObserver, times(1)).onChanged(thirdExpectedPair)
  }
  @Test fun `Observe Report State after Refreshing`() {
    val reportList = arrayListOf(ReportsFactory.buildReport(), ReportsFactory.buildReport())
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { emit(emptyList()); emit(reportList) } }
    // Would return two flows w/ different lists BUT mockito consecutive returns don't seem to work with flows
    // Consequently this test mimics the process -> 1st round = 1.(true, [])   2.(true, [filled])   3.(false, [filled])
    // 2nd round is expected to be EXACTLY the same but 2nd fetch() seems to ignore the initial emit emptyList in #1 above
    // Alternative is to use runTest BUT it yields VERY unexpected results due to the async nature of flows AND liveData
    val viewModel = ViewModelReportList(fakeRepository)

    viewModel.reportState.observeForever(reportObserver)

    verify(reportObserver, times(3)).onChanged(any())
    val inOrderCheck = inOrder(reportObserver)
    val firstExpectedPair = Pair(true, emptyList<Report>()) // No observation of default false value in loading liveData
    inOrderCheck.verify(reportObserver, times(1)).onChanged(firstExpectedPair)
    val secondExpectedPair = Pair(true, reportList)
    inOrderCheck.verify(reportObserver, times(1)).onChanged(secondExpectedPair)
    val thirdExpectedPair = Pair(false, reportList) // OnCompletion called!
    inOrderCheck.verify(reportObserver, times(1)).onChanged(thirdExpectedPair)

    viewModel.refreshReportList()

    verify(reportObserver, times(5)).onChanged(any())
    val fourthExpectedPair = Pair(true, reportList) // OnStart called again BUT weirdly already get recently returned reportsList, not emptyList
    inOrderCheck.verify(reportObserver, times(1)).onChanged(fourthExpectedPair)
    val fifthExpectedPair = Pair(false, reportList) // On Complete called again!
    inOrderCheck.verify(reportObserver, times(1)).onChanged(fifthExpectedPair)

    viewModel.reportState.removeObserver(reportObserver)
  }
  @Test fun `Check If Report List is Empty`() {
    val reportList = arrayListOf(ReportsFactory.buildReport(), ReportsFactory.buildReport())
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { emit(reportList) } }
    val viewModel = ViewModelReportList(fakeRepository)

    assertEquals(viewModel.reportState.value?.second?.size, null) // No default list so null
    assert(viewModel.reportListEmpty()) // Default elvis triggers so returns true as if empty since no observation or launching has happened
    viewModel.reportState.observeForever(reportObserver)
    assertEquals(viewModel.reportState.value?.second?.size, 2)
    assertFalse(viewModel.reportListEmpty()) // Launched/Flowing so now we have a list!
    viewModel.reportState.removeObserver(reportObserver)
  }
  @Test fun `Get a Sorted and Filtered Report List`() {
    val report = ReportsFactory.buildReport(); val report2 = ReportsFactory.buildReport()
    val reportList = arrayListOf(report, report2)
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { emit(reportList) } }
    val fakeUseCase = mock<SortFilterReportsUseCase>()
    val viewModel = ViewModelReportList(fakeRepository, fakeUseCase)

    viewModel.sortedFilteredList()
    assertEquals(viewModel.reportState.value?.second, null)
    // No launch/flow so elvis default of emptyList() used as param and returned, no real filtering or sorting done
    verify(fakeUseCase).beginSortAndFilter(emptyList(), emptyList())
    verify(fakeUseCase).beginSortAndFilter(emptyList(), viewModel.selectedFilters) // Just an emptyList!

    viewModel.reportState.observeForever(reportObserver)

    viewModel.sortedFilteredList() // Should now use the flowed in list since no filters currently
    assertEquals(viewModel.reportState.value?.second?.size, 2)
    verify(fakeUseCase).beginSortAndFilter(reportList, emptyList())
    verify(fakeUseCase).beginSortAndFilter(reportList, viewModel.selectedFilters) // Still an emptyList!
    verify(fakeUseCase).beginSortAndFilter(viewModel.reportState.value!!.second, emptyList())

    viewModel.selectedFilters.add(FilterItem("SomeFilter", false, "SomeFilterGroup"))
    verify(fakeUseCase).beginSortAndFilter(reportList, viewModel.selectedFilters) // Now not empty!
    assertEquals(viewModel.selectedFilters.size, 1)

    viewModel.reportState.removeObserver(reportObserver)
  }
  @Test fun `Get a Text Filtered List`() {
    val report = ReportsFactory.buildReport(); val report2 = ReportsFactory.buildReport()
    val reportList = arrayListOf(report, report2)
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { emit(reportList) } }
    // Need to mock returns of useCase since it isn't calling the real methods!
    val fakeUseCase = mock<SortFilterReportsUseCase>() { on { beginSortAndFilter(any(), any()) }
      .thenReturn(emptyList(), reportList) }
    val viewModel = ViewModelReportList(fakeRepository, fakeUseCase)

    val employeeName = report.employee?.firstName?.dropLastWhile { it.isDigit() } ?: ""

    // Calls useCase.filterReportsByText which uses useCase.beginAndSortFilter to create its list 2nd param
    viewModel.textFilteredList(employeeName)
    assertEquals(viewModel.reportState.value?.second, null)
    verify(fakeUseCase, times(1)).filterReportsByText(employeeName, emptyList())
    verify(fakeUseCase, times(1)).beginSortAndFilter(emptyList(), emptyList())
    verify(fakeUseCase, times(1)).beginSortAndFilter(emptyList(), viewModel.selectedFilters)

    viewModel.reportState.observeForever(reportObserver)

    assertEquals(viewModel.reportState.value?.second?.size, 2)
    viewModel.textFilteredList(employeeName) // 2nd call to useCase.filterReportsByText
    verify(fakeUseCase, times(2)).filterReportsByText(any(), any()) // Total # of calls == 2
    verify(fakeUseCase, times(1)).filterReportsByText(employeeName, reportList)

    // UseCase.beginSortAndFilter uses viewModel.reportState.value.second as 1st param list
    // and thanks to the mocking, it returns reportList for use in 2nd param of filterReportsByText above
    verify(fakeUseCase, times(2)).beginSortAndFilter(any(), any()) // Total # of calls == 2
    verify(fakeUseCase, times(1)).beginSortAndFilter(viewModel.reportState.value!!.second, emptyList())
    verify(fakeUseCase, times(1)).beginSortAndFilter(reportList, emptyList())
    verify(fakeUseCase, times(1)).beginSortAndFilter(reportList, viewModel.selectedFilters)

    viewModel.reportState.removeObserver(reportObserver)
  }

  @Test fun `Observe Loading State`() {
    fakeRepository = mock()
    val viewModel = ViewModelReportList(fakeRepository)

    viewModel.isLoading.observeForever(loadingObserver)
    viewModel.reportState.observeForever(reportObserver)

    viewModel.reportState.removeObserver(reportObserver)
    viewModel.isLoading.removeObserver(loadingObserver)

    verify(loadingObserver, times(3)).onChanged(any())
    // 1st time == "false" by default. 2nd == "true" due to precautionLoad starting. 3rd == "false" from onComplete block
    val inOrderCheck = inOrder(loadingObserver)
    inOrderCheck.verify(loadingObserver, times(1)).onChanged(false)
    inOrderCheck.verify(loadingObserver, times(1)).onChanged(true)
    inOrderCheck.verify(loadingObserver, times(1)).onChanged(false)
  }
  @Test fun `Observe Toast Message`() {
    // If we don't mock the returned flow, the combine func throws causing the flow's catch block to emit the generic toast message
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { emptyList<Report>() } }
    val viewModel = ViewModelReportList(fakeRepository)

    viewModel.toastMessage.observeForever(toastObserver)
    viewModel.reportState.observeForever(reportObserver)

    viewModel.reportState.removeObserver(reportObserver)
    viewModel.toastMessage.removeObserver(toastObserver)

    verify(toastObserver, times(1)).onChanged(any()) // Only called on its initial observeForever
    verify(toastObserver, times(1)).onChanged("")
  }
  @Test fun `Observe Toast Message from Basic Exception Thrown`() {
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { throw Exception("Problem") } }
    val viewModel = ViewModelReportList(fakeRepository)

    viewModel.toastMessage.observeForever(toastObserver)
    viewModel.reportState.observeForever(reportObserver)

    viewModel.reportState.removeObserver(reportObserver)
    viewModel.toastMessage.removeObserver(toastObserver)

    verify(toastObserver, times(2)).onChanged(any())
    val inOrderCheck = inOrder(toastObserver)
    // 1st emits default value "" THEN exception throws emitting its message
    inOrderCheck.verify(toastObserver, times(1)).onChanged("")
    inOrderCheck.verify(toastObserver, times(1)).onChanged("Sorry! Seems we're having an issue on our end!")
  }
  @Test fun `Observe Toast Message from IO Exception Thrown`() {
    fakeRepository = mock() { on { fetchReportList() } doReturn flow { throw IOException("Problem") } }
    val viewModel = ViewModelReportList(fakeRepository)

    viewModel.toastMessage.observeForever(toastObserver)
    viewModel.reportState.observeForever(reportObserver)

    viewModel.reportState.removeObserver(reportObserver)
    viewModel.toastMessage.removeObserver(toastObserver)

    verify(toastObserver, times(2)).onChanged(any())
    val inOrderCheck = inOrder(toastObserver)
    inOrderCheck.verify(toastObserver, times(1)).onChanged("")
    inOrderCheck.verify(toastObserver, times(1)).onChanged("Sorry! Having trouble with the internet connection!")
  }
}
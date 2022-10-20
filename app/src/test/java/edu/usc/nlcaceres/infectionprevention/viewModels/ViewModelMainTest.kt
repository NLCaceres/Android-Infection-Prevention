package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.lifecycle.Observer
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.data.PrecautionType
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory
import edu.usc.nlcaceres.infectionprevention.helpers.util.MainDispatcherRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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

@ExperimentalCoroutinesApi // Allows us to use MainDispatcherRule safely
class ViewModelMainTest {
  @get:Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS)
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()
  @get:Rule
  var executorRule = InstantTaskExecutorRule()

  private lateinit var fakeRepository: PrecautionRepository
  @Mock lateinit var precautionObserver: Observer<Pair<Boolean, List<Precaution>>>
  @Mock lateinit var loadingObserver: Observer<Boolean>
  @Mock lateinit var toastObserver: Observer<String>

  @Test fun `Observe Precaution State`() {
    val precautionsList = arrayListOf(ReportsFactory.buildPrecaution(PrecautionType.Standard),
      ReportsFactory.buildPrecaution(PrecautionType.Isolation))
    fakeRepository = mock() { on { fetchPrecautionList() } doReturn flow { emit(emptyList()); emit(precautionsList) } }
    val viewModel = ViewModelMain(fakeRepository)

    viewModel.precautionState.observeForever(precautionObserver)
    viewModel.precautionState.removeObserver(precautionObserver)

    verify(precautionObserver, times(3)).onChanged(any())
    val inOrderCheck = inOrder(precautionObserver)
    val firstExpectedPair = Pair(true, emptyList<Precaution>()) // No observation of default false value in loading liveData
    inOrderCheck.verify(precautionObserver, times(1)).onChanged(firstExpectedPair)
    val secondExpectedPair = Pair(true, precautionsList)
    inOrderCheck.verify(precautionObserver, times(1)).onChanged(secondExpectedPair)
    val thirdExpectedPair = Pair(false, precautionsList) // OnCompletion called!
    inOrderCheck.verify(precautionObserver, times(1)).onChanged(thirdExpectedPair)
  }
  @Test fun `Check If Precaution List is Empty`() {
    val precautionList = arrayListOf(ReportsFactory.buildPrecaution(), ReportsFactory.buildPrecaution())
    fakeRepository = mock() { on { fetchPrecautionList() } doReturn flow { emit(precautionList) } }
    val viewModel = ViewModelMain(fakeRepository)

    assertEquals(viewModel.precautionState.value?.second?.size, null) // No default list so null
    assert(viewModel.precautionListEmpty()) // Default elvis triggers so returns true as if empty since no observation or launching has happened

    viewModel.precautionState.observeForever(precautionObserver)
    assertEquals(viewModel.precautionState.value?.second?.size, 2)
    assertFalse(viewModel.precautionListEmpty()) // Launched/Flowing so now we have a list!
    viewModel.precautionState.removeObserver(precautionObserver)
  }
  @Test fun `Observe Loading State`() {
    fakeRepository = mock()
    val viewModel = ViewModelMain(fakeRepository)

    viewModel.isLoading.observeForever(loadingObserver)
    viewModel.precautionState.observeForever(precautionObserver)

    viewModel.precautionState.removeObserver(precautionObserver)
    viewModel.isLoading.removeObserver(loadingObserver)

    verify(loadingObserver, times(3)).onChanged(any())
    // 1st time == "false" by default. 2nd == "true" due to precautionLoad starting. 3rd == "false" from onComplete block
    val inOrderCheck = inOrder(loadingObserver)
    inOrderCheck.verify(loadingObserver, times(1)).onChanged(false)
    inOrderCheck.verify(loadingObserver, times(1)).onChanged(true)
    inOrderCheck.verify(loadingObserver, times(1)).onChanged(false)
  }
  @Test fun `Observe Toast Messages`() {
    // If we don't mock the returned flow, the combine func throws causing the flow's catch block to emit the generic toast message
    fakeRepository = mock() { on { fetchPrecautionList() } doReturn flow { emptyList<Precaution>() } }
    val viewModel = ViewModelMain(fakeRepository)

    viewModel.toastMessage.observeForever(toastObserver)
    viewModel.precautionState.observeForever(precautionObserver)

    viewModel.precautionState.removeObserver(precautionObserver)
    viewModel.toastMessage.removeObserver(toastObserver)
    // ToastObserver is set by the Mockito Rule, so no need to set it like with fakeRepository above!
    // Changing it overwrites the mock, potentially causing verify to throw and fail the test
    verify(toastObserver, times(1)).onChanged(any()) // Only called on its initial observeForever
    verify(toastObserver, times(1)).onChanged("")
  }
  @Test fun `Observe Basic Exception Toast Message`() {
    fakeRepository = mock() { on { fetchPrecautionList() } doReturn flow { throw Exception("Problem") } }
    val viewModel = ViewModelMain(fakeRepository)

    viewModel.toastMessage.observeForever(toastObserver)
    viewModel.precautionState.observeForever(precautionObserver)

    viewModel.precautionState.removeObserver(precautionObserver)
    viewModel.toastMessage.removeObserver(toastObserver)

    verify(toastObserver, times(2)).onChanged(any())
    val inOrderCheck = inOrder(toastObserver)
    // 1st emits default value "" THEN exception throws emitting its message
    inOrderCheck.verify(toastObserver, times(1)).onChanged("")
    inOrderCheck.verify(toastObserver, times(1)).onChanged("Sorry! Seems we're having an issue on our end!")
  }
  @Test fun `Observe IO Exception Toast Message`() {
    fakeRepository = mock() { on { fetchPrecautionList() } doReturn flow { throw IOException("Problem") } }
    val viewModel = ViewModelMain(fakeRepository)

    viewModel.toastMessage.observeForever(toastObserver)
    viewModel.precautionState.observeForever(precautionObserver)

    viewModel.precautionState.removeObserver(precautionObserver)
    viewModel.toastMessage.removeObserver(toastObserver)

    verify(toastObserver, times(2)).onChanged(any())
    val inOrderCheck = inOrder(toastObserver)
    inOrderCheck.verify(toastObserver, times(1)).onChanged("")
    inOrderCheck.verify(toastObserver, times(1)).onChanged("Sorry! Having trouble with the internet connection!")
  }
}

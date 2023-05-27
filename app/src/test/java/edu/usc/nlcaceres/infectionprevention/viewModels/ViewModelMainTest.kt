package edu.usc.nlcaceres.infectionprevention.viewModels

import androidx.lifecycle.Observer
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.data.PrecautionRepository
import edu.usc.nlcaceres.infectionprevention.helpers.data.ReportsFactory.Factory.buildPrecaution
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
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

// Need to use this to run Robolectric so it can mock Android.TextUtils.isEmpty in the viewModels' public helper funcs
@RunWith(AndroidJUnit4::class)
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
    val precautionsList = arrayListOf(buildPrecaution(), buildPrecaution())
    fakeRepository = mock { on { fetchPrecautionList() } doReturn flow { emit(emptyList()); emit(precautionsList) } }
    val viewModel = ViewModelMain(fakeRepository)

    viewModel.precautionState.observeForever(precautionObserver)
    viewModel.precautionState.removeObserver(precautionObserver)

    // Why only two invocations? Kotlin Flows that are combined ONLY take the MOST RECENTLY emitted values!
    // The above flow {} could be written more asynchronously as `flowOf(emptyList(), precautionList).onEach { delay(10) }
    verify(precautionObserver, times(2)).onChanged(any()) // But an async version could lead to more fickle tests

    // Since flows ONLY combine the most recent values, the loadingFlow skips its default 'false' value and emits the onStart 'true' value
    // Meanwhile the mock flow, skips the emptyList() and sends precautionsList WHICH
    val inOrderCheck = inOrder(precautionObserver)
    val firstExpectedPair = Pair(true, precautionsList) // LEADS to the 1st pair being this
    inOrderCheck.verify(precautionObserver, times(1)).onChanged(firstExpectedPair)

    // AND FINALLY, the flow completes calling the onCompletion block with 2nd pair being the following
    val secondExpectedPair = Pair(false, precautionsList)
    inOrderCheck.verify(precautionObserver, times(1)).onChanged(secondExpectedPair)
  }
  @Test fun `Check If Precaution List is Empty`() {
    val precautionList = arrayListOf(buildPrecaution(), buildPrecaution())
    fakeRepository = mock { on { fetchPrecautionList() } doReturn flow { emit(precautionList) } }
    val viewModel = ViewModelMain(fakeRepository)

    assertEquals(viewModel.precautionState.value?.second?.size, null) // No default list so null
    assert(viewModel.precautionListEmpty()) // Default elvis triggers so returns true as if empty since no observation or launching has happened

    viewModel.precautionState.observeForever(precautionObserver)
    assertEquals(viewModel.precautionState.value?.second?.size, 2)
    assertFalse(viewModel.precautionListEmpty()) // Launched/Flowing so now we have a list!
    viewModel.precautionState.removeObserver(precautionObserver)
  }
  @Test fun `Check if Precaution and HealthPractice Names Split`() {
    val precautionList = arrayListOf(buildPrecaution(numHealthPractices = 2), buildPrecaution(numHealthPractices = 3))
    fakeRepository = mock { on { fetchPrecautionList() } doReturn flow { emit(precautionList) } }
    val viewModel = ViewModelMain(fakeRepository)

    assertEquals(viewModel.precautionState.value?.second?.size, null) // No default list so null and
    val (emptyPrecautionNames, emptyHealthPracticeNames) = viewModel.getNamesLists() // Default elvis emptyList() triggers!
    assertEquals(0, emptyPrecautionNames.size)
    assertEquals(0, emptyHealthPracticeNames.size)

    viewModel.precautionState.observeForever(precautionObserver)
    val (precautionNames, healthPracticeNames) = viewModel.getNamesLists()
    assertEquals(2, precautionNames.size) // 2 built precautions from flow
    assertEquals(5, healthPracticeNames.size) // 2 + 3 from each precaution in flow above!
    viewModel.precautionState.removeObserver(precautionObserver)

    val otherPrecautionList = arrayListOf(buildPrecaution(numHealthPractices = 1),
      buildPrecaution(numHealthPractices = 2), buildPrecaution(numHealthPractices = 3))
    val (otherPrecautionNames, otherHealthPracticeNames) = viewModel.getNamesLists(otherPrecautionList)
    assertEquals(3, otherPrecautionNames.size) // 3 built precautions directly from otherList
    assertEquals(6, otherHealthPracticeNames.size) // 1 + 2 + 3 from each precaution
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
    fakeRepository = mock { on { fetchPrecautionList() } doReturn flow { emptyList<Precaution>() } }
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
    fakeRepository = mock { on { fetchPrecautionList() } doReturn flow { throw Exception("Problem") } }
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
    fakeRepository = mock { on { fetchPrecautionList() } doReturn flow { throw IOException("Problem") } }
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

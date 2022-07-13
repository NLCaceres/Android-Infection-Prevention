package edu.usc.nlcaceres.infectionprevention.adapters

import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.helpers.data.FiltersFactory
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class AdapterDiffCallbackTest {
  // Only test FilterCallback since used by two different classes (only non-private one)
  @Test fun checkDiffCallbacks() {
    val filterDiffCallback = FilterDiffCallback()
    val oldFilter = FiltersFactory.buildFilterItem()
    assertFalse(filterDiffCallback.areItemsTheSame(oldFilter, FiltersFactory.buildFilterItem()))
    val matchingFilter = FilterItem(oldFilter.name, false, "matchingFilterGroup")
    assertTrue(filterDiffCallback.areItemsTheSame(oldFilter, matchingFilter))
  }
}
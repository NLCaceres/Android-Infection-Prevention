package edu.usc.nlcaceres.infectionprevention.data

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/* RViews will not save state due to reuse of views, so let these models do it! */

data class FilterGroup(val name : String, val filters : List<FilterItem>,
                  var isExpanded : Boolean, var singleSelectionEnabled : Boolean)

// Why Parcelize? It allows us to send over Filters from one activity to another once they are selected by user
@Keep @Parcelize
data class FilterItem(val name : String, var isSelected : Boolean, val filterGroupName : String) : Parcelable

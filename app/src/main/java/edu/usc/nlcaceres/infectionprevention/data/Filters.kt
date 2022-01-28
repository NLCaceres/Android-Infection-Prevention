package edu.usc.nlcaceres.infectionprevention.data

import android.os.Parcelable
//import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.Parcelize

/* RViews will not save state due to reuse of views, so let these models do it! */

class FilterGroup(val name : String, val filters : ArrayList<FilterItem>, var isExpanded : Boolean, var singleSelectionEnabled : Boolean)

// Why Parcelize? It allows us to send over Filters from one activity to another once they are selected by user
@Parcelize
class FilterItem(val name : String, var isSelected : Boolean, val filterGroupName : String) : Parcelable

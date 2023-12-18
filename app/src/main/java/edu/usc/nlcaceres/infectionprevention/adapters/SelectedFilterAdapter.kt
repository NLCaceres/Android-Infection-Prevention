package edu.usc.nlcaceres.infectionprevention.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import edu.usc.nlcaceres.infectionprevention.R
import edu.usc.nlcaceres.infectionprevention.data.FilterItem
import edu.usc.nlcaceres.infectionprevention.databinding.ItemSelectedFilterBinding

/* RecyclerView Adapter that fills as options are selected from a different RecyclerView
* Provides a remove button listener to help match state between this RV and the options RV's datasets
* Linked to: ExpandableFilterAdapter and each of its FilterAdapter groups */
class SelectedFilterAdapter(private val removeButtonListener: RemoveFilterListener):
  ListAdapter<FilterItem, SelectedFilterAdapter.SelectedFilterViewHolder>(FilterDiffCallback()) {
  /* NOTE: ListAdapter NEEDS a new list instance EVERY time the data changes, whether it's an addition, removal or update
  * MAYBE a remove button is bad here OR maybe a SortedList is better here! Probably still fast with 10-100 filters BUT worth considering */

  class SelectedFilterViewHolder(private val viewBinding: ItemSelectedFilterBinding): RecyclerView.ViewHolder(viewBinding.root) {
    fun bind(filter: FilterItem, listener : RemoveFilterListener) {
      viewBinding.selectedFilterTextView.text = filter.name
      viewBinding.removeFilterButton.setOnClickListener { button ->
        listener.onRemoveButtonClicked(button, filter, bindingAdapterPosition)
      }
    }
  }
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    SelectedFilterViewHolder(ItemSelectedFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
//  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeSelectedFilterViewHolder {
//    val composeView = ComposeView(parent.context).apply { disposeComposition() }
//    return ComposeSelectedFilterViewHolder(composeView)
//  }

  override fun onBindViewHolder(holder: SelectedFilterViewHolder, position: Int) {
    holder.bind(getItem(position), removeButtonListener)
  }
}

class ComposeSelectedFilterViewHolder(private val composeView: ComposeView): RecyclerView.ViewHolder(composeView) {
  fun bind(filter: FilterItem, listener : RemoveFilterListener) {
    composeView.apply {
      setContent {
        SelectedFilterItem(filter) { listener.onRemoveButtonClicked(composeView, filter, bindingAdapterPosition) }
      }
    }
  }
}

@Composable
fun SelectedFilterItem(filter: FilterItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
  AppCompatTheme {
    Row(Modifier.background(Color.Gray, RoundedCornerShape(30.dp)).border(2.dp, Color.Black, RoundedCornerShape(30.dp)).then(modifier)) {
      Image(painterResource(R.drawable.ic_close), stringResource(R.string.remove_filter_button),
        Modifier.padding(4.dp).background(colorResource(R.color.colorPrimary), RoundedCornerShape(40.dp))
          .border(2.dp, Color.Black, RoundedCornerShape(40.dp))
          .clickable { onClick() }
      )
      Text(filter.name,
        Modifier.padding(start = 5.dp, end = 15.dp).align(Alignment.CenterVertically),
        fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
  }
}
@Preview(widthDp = 200, showBackground = true)
@Composable
fun SelectedFilterItemPreview() {
  val filter = FilterItem("Filter Name", false, "Filter Group")
  SelectedFilterItem(filter, Modifier.background(Color.Yellow, RoundedCornerShape(30.dp))) { }
}

// Adding 'fun' to interfaces w/ only 1 method inside, allows SAM conversion (aka kotlin lambdas) + trailing closures (a la Swift)
fun interface RemoveFilterListener {
  fun onRemoveButtonClicked(view : View, filter : FilterItem, position : Int)
}

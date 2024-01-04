package edu.usc.nlcaceres.infectionprevention.composables.listItems

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.usc.nlcaceres.infectionprevention.R

/** Usable in the HealthPracticeAdapter as an item of the RecyclerView.
 * Alongside ComposeHealthPracticeViewHolder, replaces PracticeViewHolder and its XML-based layout
 * Intended to navigate from FragmentMain to FragmentCreateReport on click
**/

@Composable
fun HealthPracticeCreateReportButton(healthPracticeName: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
  //! Currently doesn't have a Material Design feel, just a large icon above text, clicked as a unit
  Button(onClick, Modifier.widthIn(120.dp, 150.dp).heightIn(120.dp, 175.dp).then(modifier), shape = RoundedCornerShape(20.dp),
    colors = ButtonDefaults.buttonColors(Color.Transparent), contentPadding = PaddingValues(5.dp, 0.dp)
  ) {
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
      // Image beats Icon since Icon is intended for smaller monochrome elements
      Image(painterResource(R.drawable.ic_circle_plus), null,
        Modifier.padding(bottom = 5.dp).background(colorResource(R.color.colorPrimary), CircleShape))
      Text(healthPracticeName, color = Color.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
  }
  //TODO: Decide between FilledTonal or Elevated for this, since both provide emphasis but don't distract from rest of View
  // Tonal is bit less emphasis than Elevated BUT Elevated tries to stand out when other buttons are the normal focus
  // Which makes me feel FilledTonal is the better option. Providing just enough balance to let user flow as they want
  // 1 flaw w/ this logic is BOTH use the MaterialTheme's Secondary color, so ElevatedButton's shadow isn't very prominent anyway
  // BUT at that point, choice is purely aesthetic and based on which performs better in UX testing
  // For a more custom Shadow, can use Button(Modifier.shadow()), rather than rely on any of the base Elevation implementations
  //ElevatedButton(onClick, Modifier.width(120.dp).heightIn(120.dp, 160.dp).then(modifier), shape = RoundedCornerShape(20.dp),
    //colors = elevatedButtonColors(Color(0xFFE5E5E5)), elevation = elevatedButtonElevation(10.dp), contentPadding = PaddingValues(5.dp, 4.dp)
  //) {
    //Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
      //Image(painterResource(R.drawable.ic_circle_plus), null,
        //Modifier.padding(bottom = 2.dp).background(colorResource(R.color.colorPrimary), CircleShape))
      //Text(healthPractice.name, color = Color.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
    //}
  //}
  //FilledTonalButton(onClick, Modifier.then(modifier), shape = RoundedCornerShape(20.dp),
    //colors = filledTonalButtonColors(Color.Yellow), contentPadding = PaddingValues(14.dp, 7.dp)
  //) {
    //Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
      //Image(painterResource(R.drawable.ic_circle_plus), null,
        //Modifier.padding(bottom = 10.dp).background(colorResource(R.color.colorPrimary), CircleShape))
      //Text(healthPractice.name, color = Color.Black, fontSize = 20.sp, textAlign = TextAlign.Center)
    //}
  //}
}

@Preview(widthDp = 200, heightDp = 200, showBackground = true)
@Composable
fun HealthPracticeCreateReportButtonPreview() {
  Column(Modifier.background(Color.White), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
    HealthPracticeCreateReportButton("Health Practice Name") {}
  }
}
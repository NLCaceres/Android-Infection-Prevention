package edu.usc.nlcaceres.infectionprevention.composables.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.composables.common.icons.NavigateArrowForward
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun NavigableTextField(value: String, label: String, modifier: Modifier = Modifier) {
  AppOutlinedTextField(
    value, label, Modifier.then(modifier), readOnly = true,
    trailingIcon = { Icon(NavigateArrowForward, "Foo") }
  )
}

@Preview(widthDp = 320, heightDp = 200, showBackground = true)
@Composable
fun NavigableTextFieldPreview() {
  AppTheme {
    Row(Modifier.padding(start = 20.dp)) {
      NavigableTextField("Foo", "Bar")
    }
  }
}
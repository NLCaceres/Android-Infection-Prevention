package edu.usc.nlcaceres.infectionprevention.composables.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun NavigableTextField(value: String, label: String, modifier: Modifier = Modifier) {
  AppOutlinedTextField(
    value, label, Modifier.then(modifier), readOnly = true,
    trailingIcon = { Icon(NavigateArrowForward, "Foo") }
  )
}

//?: Icon component generally uses 24dp so best to create the path that way
private val NavigateArrowForward: ImageVector =
  materialIcon("AutoMirrored.Filled.NavigateArrowForward", autoMirror = true) {
    materialPath {
      moveTo(3.0f, 2.0f)
      lineTo(8.5f, 12.0f)
      lineTo(3.0f, 22.0f)
      lineTo(23.0f, 12.0f)
      lineTo(3.0f, 2.0f)
    }
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
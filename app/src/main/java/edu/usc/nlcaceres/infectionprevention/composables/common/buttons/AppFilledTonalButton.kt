package edu.usc.nlcaceres.infectionprevention.composables.common.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

//? A Material 3 FilledTonalButton is intended to provide medium-emphasis to buttons that init actions
//? It programmatically adopts your Material colorscheme to better hint at UI intentions to users
@Composable
fun AppFilledTonalButton(
  onClick: () -> Unit, text: String,
  modifier: Modifier = Modifier, colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
  contentPadding: PaddingValues? = null
) {
  FilledTonalButton(
    onClick, Modifier.then(modifier),
    shape = RoundedCornerShape(7.dp), colors = colors,
    contentPadding = contentPadding ?: PaddingValues(15.dp, 8.dp)
  ) {
    Text(text)
  }
}

@Preview(widthDp = 320, heightDp = 200, showBackground = true)
@Composable
private fun AppFilledTonalButtonPreview() {
  AppTheme {
    Column {
      AppFilledTonalButton({}, "Tonal Button")
    }
  }
}
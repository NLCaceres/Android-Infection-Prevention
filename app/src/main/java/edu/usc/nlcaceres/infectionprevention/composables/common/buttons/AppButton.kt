package edu.usc.nlcaceres.infectionprevention.composables.common.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

//? The Jetpack Compose Material 3 default button is a high-emphasis FilledButton
//? Most Composable parameters can have a default OR be null with a default set inline via `?:`
//? BUT Modifier should generally let the caller decide, particularly because padding stacks
//? Padding can't be overridden by the caller, it needs to be offset. Presumably other Modifiers work similarly
@Composable
fun AppButton(
  onClick: () -> Unit, text: String,
  modifier: Modifier = Modifier, colors: ButtonColors = ButtonDefaults.buttonColors(),
  contentPadding: PaddingValues? = null
) {
  Button(
    onClick, Modifier.then(modifier),
    shape = RoundedCornerShape(7.dp), colors = colors,
    contentPadding = contentPadding ?: PaddingValues(15.dp, 8.dp)
  ) {
    Text(text)
  }
}

@Preview(widthDp = 320, heightDp = 200, showBackground = true)
@Composable
private fun AppButtonPreview() {
  AppTheme {
    Column {
      AppButton({}, "Confirm?")
    }
  }
}
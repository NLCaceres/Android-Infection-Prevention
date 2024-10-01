package edu.usc.nlcaceres.infectionprevention.composables.common.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

/**
 A Positive Button used by Dialog Composables containing "OK" as text
 typically placed to the right of a Negative "Cancel" button.
 contentPadding is set to default for buttons, so despite the short text, the size of the
 button should match others, and not look uneven
 */
@Composable
fun PositiveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  AppButton(
    onClick, "OK", Modifier.then(modifier), positiveButtonColors(),
    contentPadding = PaddingValues(24.dp, 8.dp)
  )
}

// - `inverseSurface` is fairly distinct from the `primary` Red color BUT `inversePrimary`
// may actually work just as well for standing out against a Negative `error` Red button
@Composable
fun positiveButtonColors(): ButtonColors {
  return ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.inverseSurface,
    contentColor = MaterialTheme.colorScheme.inverseOnSurface
  )
}

@Preview(widthDp = 320, heightDp = 200, showBackground = true)
@Composable
private fun PositiveButtonPreview() {
  AppTheme {
    Column {
      PositiveButton({})
    }
  }
}
package edu.usc.nlcaceres.infectionprevention.composables.common.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun NegativeButton(onClick: () -> Unit) {
  AppFilledTonalButton(onClick, "Cancel", colors = negativeButtonColors())
}

@Composable
fun negativeButtonColors(): ButtonColors {
  return ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer
  )
}

@Preview(widthDp = 320, heightDp = 200, showBackground = true)
@Composable
private fun NegativeButtonPreview() {
  AppTheme {
    Column {
      NegativeButton {}
    }
  }
}
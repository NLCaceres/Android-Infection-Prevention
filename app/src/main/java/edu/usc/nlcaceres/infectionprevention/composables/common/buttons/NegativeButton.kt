package edu.usc.nlcaceres.infectionprevention.composables.common.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

/**
 A Negative Button used by Dialog Composables containing "Cancel" as text
 typically placed left of a Positive "OK" or "Confirmation" button
 */
@Composable
fun NegativeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  AppFilledTonalButton( // - Setting padding here due to expected placement BUT can be offset via `modifier`
    onClick, "Cancel", Modifier.padding(horizontal = 10.dp, vertical = 0.dp).then(modifier),
    negativeButtonColors()
  )
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
      NegativeButton({})
    }
  }
}
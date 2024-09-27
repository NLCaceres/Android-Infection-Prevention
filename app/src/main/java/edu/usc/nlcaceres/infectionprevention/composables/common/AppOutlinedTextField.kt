package edu.usc.nlcaceres.infectionprevention.composables.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun AppOutlinedTextField(
  value: String, label: String, modifier: Modifier = Modifier, readOnly: Boolean = false,
  trailingIcon: @Composable (() -> Unit)? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val textFieldState = when {
    isFocused -> TextFieldPhase.Focused
    value.isEmpty() -> TextFieldPhase.UnfocusedEmpty
    else -> TextFieldPhase.UnfocusedNotEmpty
  }
  val labelScalingProgress by animateFloatAsState(targetValue = when (textFieldState) {
    TextFieldPhase.Focused -> 1f
    TextFieldPhase.UnfocusedEmpty -> 0f
    TextFieldPhase.UnfocusedNotEmpty -> 1f
  }, animationSpec = tween(150), label = "LabelScalingFloatProgress")
  OutlinedTextField(
    modifier = Modifier.then(modifier),
    value = value, onValueChange = {}, readOnly = readOnly, singleLine = true,
    label = { Text(label, Modifier
      .layout { measurable, constraints ->
        val xOffset = (-4).dp.roundToPx()
        val placeable = measurable.measure(constraints.offset(-xOffset * 2))
        layout(placeable.width + xOffset * 2, placeable.height) {
          placeable.place(xOffset, 1.3.dp.roundToPx())
        }
      }
      .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(2.dp, 2.dp))
      .padding(2.dp, 0.5.dp, 2.dp),
      style = lerp(MaterialTheme.typography.bodyLarge, MaterialTheme.typography.labelSmall, labelScalingProgress))
    },
    trailingIcon = trailingIcon,
    colors = appOutlinedTextFieldColors(),
    interactionSource = interactionSource
  )
}
//? Enums provide plenty power to define UI States while Sealed classes could work for Events carrying data
private enum class TextFieldPhase {
  Focused,
  UnfocusedEmpty,
  UnfocusedNotEmpty
}

@Composable
fun appOutlinedTextFieldColors(): TextFieldColors {
  return OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
    focusedTrailingIconColor = MaterialTheme.colorScheme.tertiary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    focusedLabelColor = MaterialTheme.colorScheme.tertiary
  )
}

@Preview(widthDp = 320, heightDp = 200, showBackground = true)
@Composable
fun AppOutlinedTextFieldPreview() {
  AppTheme {
    Column(Modifier.padding(start = 20.dp)) {
      AppOutlinedTextField("Foo", "Bar", readOnly = true)
      AppOutlinedTextField("", "Foobar", readOnly = false)
      AppOutlinedTextField("", "Fizz", readOnly = true)
    }
  }
}

package edu.usc.nlcaceres.infectionprevention.composables.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

//? Alternatively an OutlinedCard wrapping a Row of Text, Icon and Menu COULD work but isn't ideal
@OptIn(ExperimentalMaterial3Api::class) //? Especially when ExposedDropdownMenu exists
@Composable
fun <T> MaterialSpinner(title: String, options: List<T>, onSelect: (option: T) -> Unit, modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf(options[0]) }

  ExposedDropdownMenuBox(
    expanded = expanded, onExpandedChange = { expanded = it },
    modifier = Modifier.then(modifier)
  ) {
    OutlinedTextField( // - menuAnchor.PrimaryNotEditable helps a Read-Only TextField expand/collapse the menu
      modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
      value = selectedOption.toString(), onValueChange = {}, readOnly = true, singleLine = true,
      label = { Text(title, Modifier
          .layout { measurable, constraints ->
            val xOffset = (-4).dp.roundToPx()
            val placeable = measurable.measure(constraints.offset(-xOffset * 2))
            layout(placeable.width + xOffset * 2, placeable.height) {
              placeable.place(xOffset, 1.3.dp.roundToPx())
            }
          }
          .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(2.dp, 2.dp))
          .padding(2.dp, 0.5.dp, 2.dp),
          style = MaterialTheme.typography.labelSmall)
      },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded, Modifier.scale(1.5f)) },
      colors = appOutlinedTextFieldColors()
    )
    ExposedDropdownMenu(
      expanded = expanded, onDismissRequest = { expanded = false },
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
    ) {
      options.forEachIndexed { i, option ->
        DropdownMenuItem(
          text = { Text(option.toString(), style = MaterialTheme.typography.bodyLarge) },
          onClick = {
            selectedOption = option
            onSelect(selectedOption)
            expanded = false
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
        // - Divider COULD be an issue IF menu lazy-loads BUT its list SHOULD be short in Spinners
        if (i != options.size - 1) {
          HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.tertiary)
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun appOutlinedTextFieldColors(): TextFieldColors {
  return ExposedDropdownMenuDefaults.outlinedTextFieldColors(
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer,
    focusedLabelColor = MaterialTheme.colorScheme.tertiary,
    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
    focusedTrailingIconColor = MaterialTheme.colorScheme.tertiary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
  )
}


@Preview(widthDp = 320, heightDp = 500, showBackground = true)
@Composable
fun MaterialSpinnerPreview() {
  val healthPractices = listOf(
    HealthPractice("123", name = "Hand Hygiene", Precaution(null, "Standard", listOf())),
    HealthPractice(id = null, name = "Contact", precaution = null),
    HealthPractice(id = null, name = "Contact Enteric", precaution = null),
  )
  AppTheme {
    Column {
      MaterialSpinner("Select a Health Practice", healthPractices, {}, Modifier.padding(20.dp))
    }
  }
}

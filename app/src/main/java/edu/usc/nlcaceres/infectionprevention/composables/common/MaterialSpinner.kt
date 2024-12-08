package edu.usc.nlcaceres.infectionprevention.composables.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.data.HealthPractice
import edu.usc.nlcaceres.infectionprevention.data.Precaution
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

//? Alternatively an OutlinedCard wrapping a Row of Text, Icon and Menu COULD work but isn't ideal
@OptIn(ExperimentalMaterial3Api::class) //? Especially when ExposedDropdownMenu exists
@Composable
fun <T> MaterialSpinner(title: String, options: List<T>, onSelect: (index: Int, option: T) -> Unit, modifier: Modifier = Modifier) {
  var expanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf<T?>(options.getOrNull(0)) }
  LaunchedEffect(true) { if (options.isNotEmpty()) { onSelect(0, options[0]) } }

  ExposedDropdownMenuBox(
    expanded = expanded, onExpandedChange = { expanded = it },
    modifier = Modifier.then(modifier)
  ) {
    AppOutlinedTextField(
      selectedOption?.toString() ?: "", title,
      Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable), // - Helps a Read-Only TextField expand/collapse the menu
      readOnly = true,
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded, Modifier.scale(1.5f)) }
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
            selectedOption?.let { onSelect(i, it) }
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

@Preview(widthDp = 320, heightDp = 500, showBackground = true)
@Composable
fun MaterialSpinnerPreview() {
  val healthPractices = listOf(
    HealthPractice("123", name = "Hand Hygiene", Precaution(null, "Standard", listOf())),
    HealthPractice(id = null, name = "Contact", precaution = null),
    HealthPractice(id = null, name = "Contact Enteric", precaution = null),
  )
  var selectedHealthPractice by remember { mutableStateOf(healthPractices[1]) }
  AppTheme {
    Column {
      MaterialSpinner(
        "Select a Health Practice", healthPractices,
        { i, _ -> selectedHealthPractice = healthPractices[i] }, Modifier.padding(20.dp)
      )
      Text(selectedHealthPractice.toString())
    }
  }
}
@Preview(widthDp = 320, heightDp = 500, showBackground = true)
@Composable
fun EmptyMaterialSpinnerPreview() {
  val healthPractices = listOf<HealthPractice>()
  var selectedHealthPractice by remember { mutableStateOf<HealthPractice?>(healthPractices.getOrNull(0))}
  AppTheme {
    Column {
      MaterialSpinner(
        "Select a Health Practice", healthPractices,
        { i, _ -> selectedHealthPractice = healthPractices.getOrNull(0) }, Modifier.padding(20.dp)
      )
      Text(selectedHealthPractice?.toString() ?: "Please select a Health Practice")
    }
  }
}

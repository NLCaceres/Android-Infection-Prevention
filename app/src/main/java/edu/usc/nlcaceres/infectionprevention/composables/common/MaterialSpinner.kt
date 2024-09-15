package edu.usc.nlcaceres.infectionprevention.composables.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    TextField( // - menuAnchor.PrimaryNotEditable helps a Read-Only TextField expand/collapse the menu
      modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
      value = selectedOption.toString(), onValueChange = {}, readOnly = true, singleLine = true,
      label = { Text(title, style = MaterialTheme.typography.labelSmall) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      colors = ExposedDropdownMenuDefaults.textFieldColors(),
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option.toString(), style = MaterialTheme.typography.bodyLarge) },
          onClick = {
            selectedOption = option
            onSelect(selectedOption)
            expanded = false
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
    }
  }
}


@Preview(widthDp = 320, heightDp = 500, showBackground = true)
@Composable
fun MaterialSpinnerPreview() {
  val healthPractices = listOf(
    HealthPractice("123", name = "Hand Hygiene", Precaution(null, "Standard", listOf())),
    HealthPractice(id = null, name = "Contact", precaution = null)
  )
  AppTheme {
    Column {
      MaterialSpinner("Foobar", healthPractices, {}, Modifier.padding(20.dp))
    }
  }
}

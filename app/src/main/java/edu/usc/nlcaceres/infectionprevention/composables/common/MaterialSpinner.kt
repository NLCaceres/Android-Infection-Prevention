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
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

//? Alternatively an OutlinedCard wrapping a Row of Text, Icon and Menu COULD work but isn't ideal
@OptIn(ExperimentalMaterial3Api::class) //? Especially when ExposedDropdownMenu exists
@Composable
fun MaterialSpinner() {
  val options = listOf("Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread")
  var expanded by remember { mutableStateOf(false) }
  var text by remember { mutableStateOf(options[0]) }

  ExposedDropdownMenuBox(
    expanded = expanded, onExpandedChange = { expanded = it },
    modifier = Modifier.padding(10.dp)
  ) {
    TextField( // - menuAnchor.PrimaryNotEditable helps a Read-Only TextField expand/collapse the menu
      modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
      value = text, onValueChange = {}, readOnly = true, singleLine = true,
      label = { Text("Label") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      colors = ExposedDropdownMenuDefaults.textFieldColors(),
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
          onClick = {
            text = option
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
  AppTheme {
    Column {
      MaterialSpinner()
    }
  }
}

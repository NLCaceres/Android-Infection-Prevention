package edu.usc.nlcaceres.infectionprevention.composables.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun LoadingDialog(modifier: Modifier = Modifier) {
  Box(Modifier.fillMaxSize(), Alignment.Center) {
    Surface(modifier.then(Modifier), RoundedCornerShape(15.dp),
      MaterialTheme.colorScheme.primaryContainer, tonalElevation = 10.dp, shadowElevation = 5.dp
    ) {
      Column(Modifier.padding(20.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        CircularProgressIndicator(
          Modifier.size(70.dp), strokeWidth = 5.dp,
          color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primaryContainer,
        )
        Text("Loading", Modifier.padding(top = 15.dp), fontSize = 24.sp, fontWeight = FontWeight.Medium)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun LoadingDialogPreview() {
  AppTheme {
    LoadingDialog()
  }
}
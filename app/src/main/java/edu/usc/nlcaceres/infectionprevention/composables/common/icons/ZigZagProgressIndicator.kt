package edu.usc.nlcaceres.infectionprevention.composables.common.icons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun ZigZagProgressIndicator(
  modifier: Modifier = Modifier, color: Color, trackColor: Color = Color.Transparent
) {
  BoxWithConstraints(Modifier.progressSemantics().then(modifier)) {
    var path by remember { mutableStateOf(Path().apply { // Shape to draw as track for animated path
      val width = this@BoxWithConstraints.constraints.maxWidth
      val height = this@BoxWithConstraints.constraints.maxHeight
      moveTo(width * 0.1f, height * 0.13f)
      lineTo(width * 0.9f, height * 0.13f)
      lineTo(width * 0.1f, height * 0.87f)
      lineTo(width * 0.9f, height * 0.87f)
      close()
    })}
    val pathMeasure = remember { PathMeasure() } // Linear length of shape
    val progress = remember { Animatable(0f) } // How much has been colored-in
    LaunchedEffect(true) {
      progress.animateTo( // Speed up to fully drawn path, slowly finishing and reverse
        1f, infiniteRepeatable(tween(2000, 0, CubicBezierEasing(.45f, .25f, .4f, .9f)), RepeatMode.Reverse)
      ) // cubic-bezier.com is helpful for animation timing
    }
    pathMeasure.setPath(path, false)
    // Actually colored-in path, use derivedState to limit recompositions to path updates
    val animatedPath = remember { derivedStateOf { Path().also {
      pathMeasure.setPath(path, false)
      // Only draw & color over a segment of the full track path to create animation effect
      pathMeasure.getSegment(0f, progress.value * pathMeasure.length, it)
    } } }

    Canvas(modifier.fillMaxWidth()) {
      // Draw both the track path and then draw the most update to colored-in path
      drawPath(path, trackColor, style = Stroke(10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
      drawPath(animatedPath.value, color, style = Stroke(15f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ZigZagPreview() {
  AppTheme {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
      Surface(shape = RoundedCornerShape(10f), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(10.dp)) {
          ZigZagProgressIndicator(Modifier.size(300.dp),
            color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceDim
          )
        }
      }
    }
  }
}
package edu.usc.nlcaceres.infectionprevention.composables.common.icons

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import edu.usc.nlcaceres.infectionprevention.ui.theme.AppTheme

@Composable
fun ZigZagProgressIndicator(
  modifier: Modifier = Modifier, color: Color, trackColor: Color = Color.Transparent
) {
  BoxWithConstraints(Modifier.fillMaxSize().progressSemantics().then(modifier)) {
    var path by remember { mutableStateOf(Path().apply { // Shape to draw as track for animated path
      val width = this@BoxWithConstraints.constraints.maxWidth
      val height = this@BoxWithConstraints.constraints.maxHeight
      moveTo(width * 0.1f, height * 0.13f)
      lineTo(width * 0.9f, height * 0.13f)
      lineTo(width * 0.1f, height * 0.87f)
      lineTo(width * 0.9f, height * 0.87f)
      close()
    })}
    val pathMeasure = PathMeasure().apply { setPath(path, false) } // Linear length of shape
    // Safe to remember infiniteTransition like this since `remember` caches to the current composition
    val progress by rememberInfiniteTransition().animateFloat(
      0f, pathMeasure.length,
      infiniteRepeatable(tween(2000, 0, CubicBezierEasing(.45f, .25f, .4f, .9f)), RepeatMode.Reverse)
    )
    // Actually colored-in path, use derivedState to limit recompositions to path updates
    val animatedPath = remember { derivedStateOf { Path().also {
      pathMeasure.getSegment(0f, progress, it) // Inject this portion of full path to animate/draw
    } } }

    Canvas(modifier.fillMaxWidth()) {
      // Draw both the track path and then draw the most update to colored-in path
      drawPath(path, trackColor, style = Stroke(10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
      drawPath(animatedPath.value, color, style = Stroke(15f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
  }
}

class IndicatorPreviewProvider: PreviewParameterProvider<@Composable () -> Unit> {
  override val values: Sequence<@Composable (() -> Unit)>
    get() = sequenceOf(
      @Composable {
        ZigZagProgressIndicator(
          color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceDim
        )
      },
      @Composable {
        Box(Modifier.fillMaxSize()) { ZigZagProgressIndicator(Modifier.requiredSize(100.dp), Color.Blue) }
      }
    )
}
@Preview(showBackground = true, widthDp = 300, heightDp = 500)
@Composable
private fun ZigZagPreview(
  @PreviewParameter(IndicatorPreviewProvider::class) composable: @Composable (() -> Unit)
) {
  AppTheme { composable() }
}
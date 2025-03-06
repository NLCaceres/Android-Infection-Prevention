package edu.usc.nlcaceres.infectionprevention.composables.common.icons

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.min

@Composable
fun AnimationTracingIcon(
  pathStr: String, modifier: Modifier = Modifier, color: Color, trackColor: Color = Color.Transparent
) {
  BoxWithConstraints(Modifier.fillMaxSize().then(modifier)) {
    var path by remember { mutableStateOf(
      PathParser().parsePathString(pathStr).toPath().also {
        val size = it.getBounds()
        val strokeWidthPixels = 15f
        val scaleWidth = this.constraints.maxWidth / (size.width + strokeWidthPixels)
        val scaleHeight = this.constraints.maxHeight / (size.height + strokeWidthPixels)
        val scale = min(scaleWidth, scaleHeight)
        val horizontalOffset = size.left - (strokeWidthPixels / 2)
        val verticalOffset = size.top - (strokeWidthPixels / 2)
        it.transform(Matrix().apply { scale(scale, scale); translate(-horizontalOffset, -verticalOffset) })
      }
    ) }
    val pathLength = PathMeasure().apply { setPath(path, false) }
    val progress by rememberInfiniteTransition().animateFloat(
      0f, pathLength.length, infiniteRepeatable(tween(1500, 0, EaseInOutQuad), RepeatMode.Reverse)
    )
    val animatedPath = remember { derivedStateOf { Path().also {
      pathLength.getSegment(0f, progress, it)
    } } }
    Canvas(Modifier.fillMaxWidth()) {
      drawPath(path, trackColor, style = Stroke(10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
      drawPath(animatedPath.value, color, style = Stroke(15f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
  }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 200)
@Composable
fun AnimationPathPreview() {
  AnimationTracingIcon("M 1,1 L23,1 L 23, 23 L1,23z", color = Color.Red, trackColor = Color.Gray)
}
package edu.usc.nlcaceres.infectionprevention.composables.common.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

//? Actual MaterialIcons use the following "Backing Property" pattern to get generated ONLY when needed
//? HOWEVER it's unclear if it's better to generate Icons like this or still to use drawables
private var _NavigateArrowForward: ImageVector? = null
public val NavigateArrowForward: ImageVector
  get() {
    if (_NavigateArrowForward != null) {
      return _NavigateArrowForward!!
    }
    //?: The Icon component generally uses a 24dp square so best to create the path with that in mind
    _NavigateArrowForward = materialIcon("AutoMirrored.Filled.NavigateArrowForward", autoMirror = true) {
      materialPath {
        moveTo(3.0f, 2.0f)
        lineTo(8.5f, 12.0f)
        lineTo(3.0f, 22.0f)
        lineTo(23.0f, 12.0f)
        lineTo(3.0f, 2.0f)
      }
    }
    return _NavigateArrowForward!!
  }

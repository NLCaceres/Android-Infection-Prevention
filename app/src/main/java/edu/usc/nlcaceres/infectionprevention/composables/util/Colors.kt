package edu.usc.nlcaceres.infectionprevention.composables.util

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/** RandomColor makes it easy to VISUALLY track Composables' Recompositions when applied to a Composable's .border() Modifier
 * ex: SomeComposable(someMutableState, Modifier.border(2.dp, randomColor()))
 * The example Composable changes colors EVERY recomposition
 * which can happen due to changes in its mutableState param or changes in composition higher in the Composable hierarchy
**/
fun randomColor() = Color(
  Random.nextInt(256),
  Random.nextInt(256),
  Random.nextInt(256),
  alpha = 255
)
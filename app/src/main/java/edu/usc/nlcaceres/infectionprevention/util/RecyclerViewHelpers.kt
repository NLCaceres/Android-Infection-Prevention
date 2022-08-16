package edu.usc.nlcaceres.infectionprevention.util

import android.view.View
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/* Useful funcs for specifically RecyclerViews. Currently expected to help with creating custom ItemDecorators */

// Add consistent margins to individual recyclerview items programmatically! No weird layout issues!
class MarginsItemDecoration(private val leftMargin: Int = 0, private val topMargin: Int = 0, private val rightMargin: Int = 0,
                            private val bottomMargin: Int = 0, private val orientation: Int = 0) : RecyclerView.ItemDecoration() {
    constructor(horizontalMargins: Int = 0, verticalMargins: Int = 0, orientation: Int)
        : this(horizontalMargins, verticalMargins, horizontalMargins, verticalMargins, orientation)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) { // If no return value needed "with" is better than "run" scope fun
            // Need 3 margins ALWAYS: Vertical recyclerViews rows should set top margin, Horizontal should set left. ALWAYS!
            if (orientation == DividerItemDecoration.VERTICAL) {
                top = topMargin
                if (parent.getChildAdapterPosition(view) == 0) left = leftMargin // 1st row gets one extra "start" margin (prevent right from doubling margins)
            }
            else {
                left = leftMargin
                if (parent.getChildAdapterPosition(view) == 0) top = topMargin // Prevents bottom from doubling margin (top + bottom then only bottom rest)
            }
            right = rightMargin
            bottom = bottomMargin
        }
    }
}

// Ensures no final divider. Ex: Item | Item | Item   Rather than ex: Item | Item | Item |
// Mostly the exact same functionality as DividerItemDecoration BUT it differs in the range of the final for loop
class DivideAllButLastItemDecoration(private val divider: Drawable, private val orientation: Int) : RecyclerView.ItemDecoration() {
    private val bounds: Rect = Rect()
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) return
        if (orientation == DividerItemDecoration.VERTICAL) drawVertical(canvas, parent)
        else drawHorizontal(canvas, parent)
    }
    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int; val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft; right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        }
        else { left = 0; right = parent.width }

        for (i in 0 until parent.childCount - 1) { // CHANGED to use "until" to EXCLUDE final item from receiving a divider
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + child.translationY.roundToInt()
            val top = bottom - divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)
        }
        canvas.restore()
    }
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int; val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop; bottom = parent.height - parent.paddingBottom
            canvas.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
        }
        else { top = 0; bottom = parent.height }

        for (i in 0 until parent.childCount - 1) { // CHANGED to use "until" to EXCLUDE final item from receiving a divider
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val right = bounds.right + child.translationX.roundToInt()
            val left = right - divider.intrinsicWidth
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) == state.itemCount - 1) outRect.setEmpty() // Ensure last row container is NOT drawn
        else if (orientation == DividerItemDecoration.VERTICAL) outRect.set(0, 0, 0, divider.intrinsicHeight)
        else outRect.set(0, 0, divider.intrinsicWidth, 0)
    }
}
package com.digiteq.launcherdemo

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.SparseIntArray
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.Integer.max

class PagerLayoutManager(
    private val spanCount: Int = 24,
    private val spanSizeLookup: (position: Int) -> Int = { _ -> 4 }
) : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    private interface LayoutCompleteListener {
        fun onLayoutComplete(page: Int)
    }

    public var canScroll = true

    private var scrollDistance = 0
    private var maxScrollDistance = width
    private val spanHeight = SparseIntArray()
    private var onLayoutCompleteListener: MutableList<LayoutCompleteListener> = ArrayList(0)
    var frames = arrayListOf<VisibleRect>()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        super.onItemsMoved(recyclerView, from, to, itemCount)
    }

    override fun supportsPredictiveItemAnimations(): Boolean {
        return true
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount <= 0 || state.isPreLayout)
            return
        if (frames.size != itemCount) {
            var remainWidth = width
            var remainHeight = height
            var maxHeight = 0
            var page = 0
            frames.clear()
            for (position in 0 until itemCount) {
                val childWidth = spanSizeLookup.invoke(position) * width / spanCount
                val childHeight = if (spanHeight[childWidth, Int.MIN_VALUE] != Int.MIN_VALUE) {
                    spanHeight[childWidth]
                } else {
                    val child = recycler.getViewForPosition(position)
                    assignSpans(child, position)
                    measureChildWithMargins(child, 0, 0)
                    spanHeight.put(childWidth, getDecoratedMeasuredHeight(child))
                    removeAndRecycleView(child, recycler)
                    spanHeight[childWidth]
                }
                if (remainWidth >= childWidth) {
                    frames.add(VisibleRect().apply {
                        left = page * width + width - remainWidth.toFloat()
                        top = height - remainHeight.toFloat()
                        right = left + childWidth
                        bottom = top + childHeight
                        this.page = page
                    })
                    maxHeight = max(maxHeight, childHeight)
                    remainWidth -= childWidth
                } else {
                    remainHeight -= maxHeight
                    if (remainHeight < childHeight) {
                        page += 1
                        remainHeight = height
                        maxScrollDistance += width
                    }
                    remainWidth = width
                    frames.add(VisibleRect().apply {
                        left = page * width + width - remainWidth.toFloat()
                        top = height - remainHeight.toFloat()
                        right = left + childWidth
                        bottom = top + childHeight
                        this.page = page
                    })
                    maxHeight = max(0, childHeight)
                    remainWidth -= childWidth
                }
            }
        }
        fill(recycler, state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        for (listener in onLayoutCompleteListener) listener.onLayoutComplete(if (frames.size > 0) frames.last().page else 0)
    }

    override fun canScrollHorizontally(): Boolean {
        return canScroll
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        val distance = if(scrollDistance <= 0 && dx <= 0 || scrollDistance >= maxScrollDistance && dx >= 0) 0
        else if(scrollDistance + dx < 0) -scrollDistance
        else if(scrollDistance + dx > maxScrollDistance) maxScrollDistance - scrollDistance
        else dx
        fill(recycler, state)
        offsetChildrenHorizontal(-distance)
        scrollDistance += distance
        recycleViewOutOfBounds(recycler)
        return distance
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF {
        val frame = frames[targetPosition]
        return if (frame.visible) PointF(0f, 0f) else PointF(frame.left - scrollDistance, 0f)
    }

    fun onLayoutComplete(listener: (page: Int) -> Unit) {
        onLayoutCompleteListener.add(object : LayoutCompleteListener {
            override fun onLayoutComplete(page: Int) {
                listener.invoke(page)
            }
        })
    }

    fun nextPageItemPosition(): Int {
        val page =
            if (scrollDistance == maxScrollDistance) frames.last().page else scrollDistance / width + 1
        for ((i, frame) in frames.withIndex()) {
            if (frame.page == page)
                return i
        }
        return 0
    }

    fun previousPageItemPosition(): Int {
        for ((i, frame) in frames.withIndex()) {
            if (frame.page == scrollDistance / width)
                return i
        }
        return 0
    }

    private fun assignSpans(view: View, position: Int) {
        val width = spanSizeLookup.invoke(position) * width / spanCount
        view.layoutParams.width = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
    }

    private fun fill(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount <= 0 || state.isPreLayout)
            return

        val displayRect = Rect().apply {
            left = scrollDistance
            top = 0
            right = left + width
            bottom = height
        }

        for (i in 0 until itemCount) {
            if (!frames[i].visible && Rect.intersects(displayRect, frames[i].rect())) {
                val child = recycler.getViewForPosition(i)
                assignSpans(child, i)
                measureChildWithMargins(child, 0, 0)
                addView(child)
                frames[i].apply { visible = true }.rect().apply {
                    layoutDecoratedWithMargins(
                        child,
                        left - scrollDistance,
                        top,
                        right - scrollDistance,
                        bottom
                    )
                }
            }
            if (frames[i].visible && !Rect.intersects(displayRect, frames[i].rect())) {
                frames[i].visible = false
            }
        }
    }

    private fun recycleViewOutOfBounds(recycler: RecyclerView.Recycler) {
        val displayRect = Rect().apply {
            left = scrollDistance
            top = 0
            right = left + width
            bottom = height
        }

        var r = 0
        for (i in 0 until childCount) {
            getChildAt(i - r)?.also {
                val rect = Rect().apply {
                    left = getDecoratedLeft(it) + scrollDistance
                    top = getDecoratedTop(it)
                    right = getDecoratedRight(it) + scrollDistance
                    bottom = getDecoratedBottom(it)
                }
                if (!Rect.intersects(displayRect, rect)) {
                    removeAndRecycleView(it, recycler)
                    r++
                }
            }
        }
    }

    class VisibleRect : RectF() {
        var visible = false
        var page = 0
        fun rect() = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}
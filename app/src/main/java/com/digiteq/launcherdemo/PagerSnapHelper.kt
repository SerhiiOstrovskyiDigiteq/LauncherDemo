package com.digiteq.launcherdemo

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.abs

class PagerSnapHelper: SnapHelper() {
    private var verticalHelper: OrientationHelper? = null
    private var horizontalHelper: OrientationHelper? = null

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val x = if(layoutManager.canScrollVertically()) 0
        else distanceToStart(targetView, getOrientationHelper(layoutManager))
        val y = if(layoutManager.canScrollHorizontally()) 0
        else distanceToStart(targetView, getOrientationHelper(layoutManager))
        return intArrayOf(x, y)
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        var minDistance = Int.MAX_VALUE
        var minChildIndex = Int.MAX_VALUE
        val helper = getOrientationHelper(layoutManager)
        val center = (helper?.totalSpace ?: 0) / 2
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)
            val start = helper?.getDecoratedStart(child) ?: 0
            val end = helper?.getDecoratedEnd(child) ?: 0
            val distance = abs((start + end) / 2 - center)
            if(distance < minDistance) {
                minDistance = distance
                minChildIndex = i
            }
        }

        var page = 0
        layoutManager.getChildAt(minChildIndex)?.apply {
            val position = layoutManager.getPosition(this)
            page = (layoutManager as PagerLayoutManager).frames[position].page
        }
        for((i, frame) in (layoutManager as PagerLayoutManager).frames.withIndex()) {
            if(frame.page == page) return layoutManager.findViewByPosition(i)
        }
        return null
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        var position = 0
        if(layoutManager is PagerLayoutManager) {
            position =
                if (velocityX > 0) layoutManager.nextPageItemPosition() else layoutManager.previousPageItemPosition()
        }
        return position
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper?): Int {
        return (helper?.getDecoratedStart(targetView) ?: 0) - (helper?.startAfterPadding ?: 0)
    }

    private fun getOrientationHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        return when {
            layoutManager.canScrollVertically() -> getVerticalHelper(layoutManager)
            layoutManager.canScrollHorizontally() -> getHorizontalHelper(layoutManager)
            else -> null
        }
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        if(verticalHelper?.layoutManager != layoutManager)
            verticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        return verticalHelper
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        if(horizontalHelper?.layoutManager != layoutManager)
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        return horizontalHelper
    }
}
package com.behaviorule.arturdumchev.library

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * @author arturdumchev on 13/10/2018.
 */
abstract class BehaviorByRules(
        context: Context?,
        attrs: AttributeSet?
) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private var views: List<RuledView> = emptyList()
    private var lastChildHeight = -1
    private var needToUpdateHeight: Boolean = true

    override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
    ): Boolean {
        firstInit(child, dependency)
        val progress = calcProgress(parent)
        views.forEach { performRules(offsetView = it, percent = progress) }
        return true
    }

    override fun onMeasureChild(
            parent: CoordinatorLayout, child: View, parentWidthMeasureSpec: Int,
            widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int
    ): Boolean {

        val canUpdateHeight = canUpdateHeight(calcProgress(parent))
        if (canUpdateHeight) {
            parent.post {
                val newChildHeight = child.height
                if (newChildHeight != lastChildHeight) {
                    lastChildHeight = newChildHeight
                    setUpAppbarHeight(child, parent)
                }
            }
        } else {
            needToUpdateHeight = true
        }
        return super.onMeasureChild(
                parent, child, parentWidthMeasureSpec,
                widthUsed, parentHeightMeasureSpec, heightUsed
        )
    }

    /**
     * If you use fitsSystemWindows=true in your coordinator layout,
     * you will have to include statusBar height in the appbarHeight
     */
    protected abstract fun calcAppbarHeight(child: View): Int
    protected abstract fun View.setUpViews(): List<RuledView>
    protected abstract fun View.provideAppbar(): AppBarLayout
    protected abstract fun View.provideCollapsingToolbar(): CollapsingToolbarLayout

    /**
     * You man not want to update height, if height depends on views, that are currently invisible
     */
    protected open fun canUpdateHeight(progress: Float): Boolean = true

    private fun calcProgress(parent: CoordinatorLayout): Float {
        val appBar = parent.provideAppbar()
        val scrollRange = appBar.totalScrollRange.toFloat()
        val scrollY = Math.abs(appBar.y)
        val scroll = 1 - scrollY / scrollRange
        return when {
            scroll.isNaN() -> 1f
            else -> scroll
        }
    }

    private fun setUpAppbarHeight(child: View, parent: ViewGroup) {
        parent.provideCollapsingToolbar().setHeight(calcAppbarHeight(child))
    }

    private fun firstInit(child: View, dependency: View) {
        if (needToUpdateHeight) {
            setUpAppbarHeight(child, dependency as ViewGroup)
            needToUpdateHeight = false
        }

        if (views.isEmpty()) {
            views = child.setUpViews()
        }
    }

    private fun performRules(offsetView: RuledView, percent: Float) {
        val view = offsetView.view
        val details = offsetView.details
        offsetView.rules.forEach { rule ->
            rule.manage(percent, details, view)
        }
    }
}
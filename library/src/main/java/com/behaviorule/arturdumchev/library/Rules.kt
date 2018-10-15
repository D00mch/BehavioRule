package com.behaviorule.arturdumchev.library

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import java.util.*

/**
 * @author arturdumchev on 13/10/2018.
 */
class InitialViewDetails(
        val x: Float,
        val y: Float,
        var alpha: Float
)

class RuledView(
        val view: View,
        val rules: List<BehaviorRule>,
        val details: InitialViewDetails = InitialViewDetails(
                x = view.x,
                y = view.y,
                alpha = view.alpha
        )
) {
    constructor(
            view: View,
            vararg rules: BehaviorRule
    ) : this(view, rules = rules.toList())
}

interface BehaviorRule {
    /**
     * @param view to be changed
     * @param details view's data when first attached
     * @param ratio in range [0, 1]; 0 when toolbar is collapsed
     */
    fun manage(ratio: Float, details: InitialViewDetails, view: View)
}

abstract class BaseBehaviorRule : BehaviorRule {
    abstract val interpolator: Interpolator
    abstract val min: Float
    abstract val max: Float

    final override fun manage(ratio: Float, details: InitialViewDetails, view: View) {
        val interpolation = interpolator.getInterpolation(ratio)
        val offset = normalize(
                oldValue = interpolation,
                newMin = min, newMax = max
        )
        perform(offset, details, view)
    }

    /**
     * @param offset normalized with range from [min] to [max] with [interpolator]
     */
    abstract fun perform(offset: Float, details: InitialViewDetails, view: View)
}

class ThresholdRule(
        private val rule: BehaviorRule,
        private val from: Float,
        private val to: Float
) : BehaviorRule {

    init {
        require(rule !is BRuleAppear) { "you should not use ThresholdRule with BRuleAppear" }
    }

    override fun manage(ratio: Float, details: InitialViewDetails, view: View) {
        if (ratio in (from..to)) {
            val normalization = normalize(ratio, newMin = 0f, newMax = 1f, oldMin = from, oldMax = to)
            rule.manage(normalization, details, view)
        }
    }
}

/**
 * [min], [max] — values in any range that will proportionally affect view's scale
 */
class BRuleScale(
        override val min: Float,
        override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBehaviorRule() {

    override fun perform(offset: Float, details: InitialViewDetails, view: View) = with(view) {
        scaleX = offset
        scaleY = offset
        pivotX = 0f
        pivotY = 0f
    }
}

/**
 * [min], [max] — values in range [0, 1]
 */
class BRuleAlpha(
        override val min: Float,
        override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBehaviorRule() {

    override fun perform(offset: Float, details: InitialViewDetails, view: View) {
        view.alpha = offset
    }
}


/**
 * [min], [max] — value in pixels
 */
class BRuleYOffset(
        override val min: Float,
        override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBehaviorRule() {

    override fun perform(offset: Float, details: InitialViewDetails, view: View) {
        view.y = offset + details.y
    }
}

/**
 * [min], [max] — value in pixels
 */
class BRuleXOffset(
        override val min: Float,
        override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBehaviorRule() {

    override fun perform(offset: Float, details: InitialViewDetails, view: View) {
        view.x = details.x + offset
    }
}

/**
 * @param appearedUntil value in range [0, 1]
 * @param reverse if true, view will be hidden in range [0, appearedUntil]
 * @param animationDuration in milliseconds
 * @param alphaForVisibility appear with alpha
 */
class BRuleAppear(
        private val appearedUntil: Float,
        private val reverse: Boolean = false,
        private val animationDuration: Long = 0L,
        private val alphaForVisibility: Float = 1f
) : BehaviorRule {

    override fun manage(ratio: Float, details: InitialViewDetails, view: View) = with(view) {
        val shouldAppear = (ratio > appearedUntil).xor(reverse)
        animateAppearance(shouldAppear)
    }

    private fun View.animateAppearance(isVisible: Boolean) {
        val alpha = if (isVisible) alphaForVisibility else 0f

        val prev = hiddenViews.put(this, isVisible)
        if (prev == isVisible && animationDuration != 0L) {
            return
        }

        clearAnimation()
        val animatorListener = object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                if (isVisible) {
                    isEnabled = true
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (isVisible.not()) isEnabled = false
            }
        }
        animate().alpha(alpha)
                .setDuration(animationDuration)
                .setListener(animatorListener)
    }

    companion object {
        private val hiddenViews: WeakHashMap<View, Boolean> = WeakHashMap()
    }
}

fun BehaviorRule.workInRange(from: Float, to: Float): BehaviorRule = ThresholdRule(this, from, to)

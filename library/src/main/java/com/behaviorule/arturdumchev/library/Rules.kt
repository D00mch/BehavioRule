package com.behaviorule.arturdumchev.library

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.annotation.FloatRange
import android.support.annotation.IntRange
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
        val alpha: Float
)

class RuledView(
        val view: View,
        val rules: List<BRule>,
        val details: InitialViewDetails = InitialViewDetails(
                x = view.x,
                y = view.y,
                alpha = view.alpha
        )
) {
    constructor(
            view: View,
            vararg rules: BRule
    ) : this(view, rules = rules.toList())
}

interface BRule {
    /**
     * @param view to be changed
     * @param details view's data when first attached
     * @param ratio 0 when toolbar is collapsed
     */
    fun manage(
            @FloatRange(from = 0.0, to = 1.0) ratio: Float,
            details: InitialViewDetails,
            view: View
    )
}

abstract class BaseBRule : BRule {
    abstract val interpolator: Interpolator
    abstract val min: Float
    abstract val max: Float

    final override fun manage(
            @FloatRange(from = 0.0, to = 1.0) ratio: Float,
            details: InitialViewDetails,
            view: View
    ) {
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
    abstract fun perform(
            @FloatRange(from = 0.0, to = 1.0) offset: Float,
            details: InitialViewDetails,
            view: View
    )

    protected fun requireMinLessThenMax() = require(min < max) { "min should be less then max" }
}

/**
 * @param from do not invoke [rule] when appBar scroll ratio is less than this value
 * @param to do not invoke [rule] when appBar scroll ratio is more than this value
 * @param rule will be invoked with ratio from 0 till 1, it will be normalized with [from] and [to]
 */
class ThresholdRule(
        private val rule: BRule,
        @FloatRange(from = 0.0, to = 1.0) private val from: Float,
        @FloatRange(from = 0.0, to = 1.0) private val to: Float
) : BRule {

    init {
        require(rule !is BRuleAppear) { "You should not use ThresholdRule with BRuleAppear" }
        require(from < to) { "from should be less the to" }
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
        @FloatRange(from = 0.0) override val min: Float,
        @FloatRange(from = 0.0) override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBRule() {

    init {
        requireMinLessThenMax()
    }

    override fun perform(offset: Float, details: InitialViewDetails, view: View) = with(view) {
        scaleX = offset
        scaleY = offset
        pivotX = 0f
        pivotY = 0f
    }
}

class BRuleAlpha(
        @FloatRange(from = 0.0, to = 1.0) override val min: Float,
        @FloatRange(from = 0.0, to = 1.0) override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBRule() {

    init {
        requireMinLessThenMax()
    }

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
) : BaseBRule() {

    init {
        requireMinLessThenMax()
    }

    override fun perform(offset: Float, details: InitialViewDetails, view: View) {
        view.y = offset + details.y
    }
}

/**
 * [min], [max] — value in pixels
 */
class BRuleXOffset(
        @FloatRange(from = 0.0, to = 1.0) override val min: Float,
        override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBRule() {

    init {
        requireMinLessThenMax()
    }

    override fun perform(offset: Float, details: InitialViewDetails, view: View) {
        view.x = details.x + offset
    }
}

/**
 * @param visibleUntil view will be visible from 0 till this value
 * @param reverse if true, view will be hidden (not visible) in range from 0 till [visibleUntil]
 * @param animationDuration in milliseconds
 * @param alphaForVisibility view will appear with this value
 */
class BRuleAppear(
        private val visibleUntil: Float,
        private val reverse: Boolean = false,
        @IntRange(from = 0) private val animationDuration: Long = 0L,
        @FloatRange(from = 0.0, to = 1.0) private val alphaForVisibility: Float = 1f
) : BRule {

    private var wasVisible: Boolean? = null

    override fun manage(ratio: Float, details: InitialViewDetails, view: View) = with(view) {
        val shouldAppear = (ratio > visibleUntil).xor(reverse)
        animateAppearance(shouldAppear)
    }

    private fun View.animateAppearance(isVisible: Boolean) {
        val alpha = if (isVisible) alphaForVisibility else 0f

        val shouldBreak = wasVisible == isVisible
        wasVisible = isVisible
        if (shouldBreak && animationDuration != 0L) {
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
}

fun BRule.workInRange(
        @FloatRange(from = 0.0, to = 1.0) from: Float,
        @FloatRange(from = 0.0, to = 1.0) to: Float
): BRule = ThresholdRule(this, from, to)

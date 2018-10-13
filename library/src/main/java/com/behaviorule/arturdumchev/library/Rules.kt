package com.behaviorule.arturdumchev.library

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

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
    abstract val min: Number
    abstract val max: Number

    final override fun manage(ratio: Float, details: InitialViewDetails, view: View) {
        perform(interpolator.getInterpolation(ratio), details, view)
    }

    abstract fun perform(ratio: Float, details: InitialViewDetails, view: View)
}

/**
 * [min], [max] — values in any range that will proportionally affect view's scale
 */
class BRuleScale(
        override val min: Float,
        override val max: Float,
        override val interpolator: Interpolator = LinearInterpolator()
) : BaseBehaviorRule() {

    override fun perform(ratio: Float, details: InitialViewDetails, view: View) = with(view) {
        val size = normalize(
                oldValue = ratio,
                newMax = max,
                newMin = min
        )
        scaleX = size
        scaleY = size
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
    override fun perform(ratio: Float, details: InitialViewDetails, view: View) = with(view) {
        this.alpha = normalize(oldValue = ratio, newMin = min, newMax = max)
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
    override fun perform(ratio: Float, details: InitialViewDetails, view: View) = with(view) {
        val offset = normalize(
                oldValue = ratio,
                newMin = min, newMax = max
        )
        this.y = offset + details.y
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

    override fun perform(ratio: Float, details: InitialViewDetails, view: View) = with(view) {
        val offset = normalize(
                oldValue = ratio,
                newMin = min, newMax = max
        )
        this.x = details.x + offset
    }
}

/**
 * @param appearedUntil value in range [0, 1]
 * @param reverse if true, view will be hidden in range [0, appearedUntil]
 */
class BRuleAppear(
        private val appearedUntil: Float,
        private val reverse: Boolean = false
) : BehaviorRule {

    private val animationDuration = 0L

    override fun manage(ratio: Float, details: InitialViewDetails, view: View) = with(view) {

        val shouldAppear = (ratio > appearedUntil).xor(reverse)
        animateAppearance(shouldAppear)
    }

    private fun View.animateAppearance(isVisible: Boolean) {
        clearAnimation()
        val alpha = if (isVisible) 1f else 0f
        val animatorListener = object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                if (isVisible) isEnabled = true
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

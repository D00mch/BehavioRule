package com.behaviorule.arturdumchev.library

import android.view.animation.Interpolator

/**
 * @author arturdumchev on 13/10/2018.
 */
class ReverseInterpolator(private val interpolator: Interpolator) : Interpolator {
    override fun getInterpolation(input: Float): Float = 1f - interpolator.getInterpolation(input)
}

class ThresholdInterpolator(
        private val min: Float,
        private val max: Float,
        private val interpolator: Interpolator? = null
) : Interpolator {
    override fun getInterpolation(input: Float): Float = when {
        input < min -> 0f
        input >= max -> 1f
        else -> interpolator?.getInterpolation(input) ?: input
    }
}

class TurnBackInterpolator(
        private val interpolator: Interpolator,
        private val turnRation: Float = 0f
) : Interpolator {
    override fun getInterpolation(input: Float): Float = when {
        input <= turnRation -> interpolator.getInterpolation(input)
        else -> interpolator.getInterpolation(1f - input)
    }
}

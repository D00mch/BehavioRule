package com.behaviorule.arturdumchev.library

import android.support.annotation.FloatRange
import android.view.animation.Interpolator

/**
 * @author arturdumchev on 13/10/2018.
 */
class ReverseInterpolator(private val interpolator: Interpolator) : Interpolator {
    override fun getInterpolation(input: Float): Float = interpolator.getInterpolation(1 - input)
}

/**
 * Use [interpolator] in range from [min] to [max], or return them, if out of range
 */
class ThresholdInterpolator(
        @FloatRange(from = 0.0, to = 1.0) private val min: Float,
        @FloatRange(from = 0.0, to = 1.0) private val max: Float,
        private val interpolator: Interpolator? = null
) : Interpolator {
    override fun getInterpolation(input: Float): Float = when {
        input <= min -> min
        input >= max -> max
        else -> interpolator?.getInterpolation(input) ?: input
    }
}

class TurnBackInterpolator(
        private val interpolator: Interpolator,
        @FloatRange(from = 0.0, to = 1.0) private val turnRation: Float = 0f
) : Interpolator {
    override fun getInterpolation(input: Float): Float = when {
        input <= turnRation -> interpolator.getInterpolation(input)
        else -> interpolator.getInterpolation(1f - input)
    }
}

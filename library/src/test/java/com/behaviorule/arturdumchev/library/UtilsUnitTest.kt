package com.behaviorule.arturdumchev.library

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class UtilsUnitTest {
    @Test
    fun addition_isCorrect() {
        check(75f == normalize(0.5f, 50f, 100f, 0f, 1f))
        check(50f == normalize(0.5f, 0f, 100f, 0f, 1f))
    }
}
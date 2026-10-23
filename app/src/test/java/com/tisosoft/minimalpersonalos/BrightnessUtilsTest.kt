package com.tisosoft.minimalpersonalos

import com.tisosoft.minimalpersonalos.utils.BrightnessUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrightnessUtilsTest {

    @Test
    fun testPerceptualToLinearAndBack() {
        // Test values across the range from 0.0 to 1.0
        val testValues = listOf(0.0f, 0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.0f)
        
        for (perceptual in testValues) {
            val linear = BrightnessUtils.convertPerceptualToLinear(perceptual)
            val backToPerceptual = BrightnessUtils.convertLinearToPerceptual(linear)
            
            // Allow minor floating point inaccuracies (delta of 0.001)
            assertEquals("Failed for perceptual value: $perceptual", perceptual, backToPerceptual, 0.001f)
        }
    }

    @Test
    fun testMonotonicity() {
        // Verify that as perceptual brightness increases, linear brightness also increases
        var previousLinear = -1f
        for (i in 0..100) {
            val perceptual = i / 100f
            val linear = BrightnessUtils.convertPerceptualToLinear(perceptual)
            assertTrue("Linear value should be monotonic: $linear >= $previousLinear", linear >= previousLinear)
            previousLinear = linear
        }
    }
}

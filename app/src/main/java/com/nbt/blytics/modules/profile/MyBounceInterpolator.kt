package com.nbt.blytics.modules.profile

import android.view.animation.Interpolator

class MyBounceInterpolator(amplitude: Double, frequency: Double) :
    Interpolator {
    /**
     * The amplitude of the bounces. The higher value (10, for example) produces more pronounced bounces.
     * The lower values (0.1, for example) produce less noticeable wobbles.
     */
    var mAmplitude = 1.0

    /**
     * The frequency of the bounces. The higher value produces more wobbles during the animation time period.
     */
    var mFrequency = 10.0
    override fun getInterpolation(time: Float): Float {
        var amplitude = mAmplitude
        if (amplitude == 0.0) {
            amplitude = 0.05
        }

        // The interpolation curve equation:
        //    -e^(-time / amplitude) * cos(frequency * time) + 1
        //
        // View the graph live: https://www.desmos.com/calculator/6gbvrm5i0s
        return (-1 * Math.pow(
            Math.E,
            -time / mAmplitude
        ) * Math.cos(mFrequency * time) + 1).toFloat()
    }

    /**
     * Initialize a new interpolator.
     *
     * @param      amplitude   The amplitude of the bounces. The higher value produces more pronounced bounces. The lower values (0.1, for example) produce less noticeable wobbles.
     * @param      frequency   The frequency of the bounces. The higher value produces more wobbles during the animation time period.
     */
    init {
        mAmplitude = amplitude
        mFrequency = frequency
    }
}
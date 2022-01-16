/*
 * *****************************************************************************
 * Copyright (C) 2014-2022 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * ****************************************************************************
 */

package io.github.dsheirer.dsp.mixer;

import org.apache.commons.math3.util.FastMath;

public class ComplexOscillator extends BaseOscillator implements IComplexOscillator
{
    private static final float THREE_HALVES = 3.0f / 2.0f;
    private float mCosineAngle;
    private float mSineAngle;
    private float mPreviousInphase = 1.0f;
    private float mPreviousQuadrature = 0.0f;

    /**
     * Constructs an instance
     *
     * @param frequency in hertz
     * @param sampleRate in hertz
     */
    public ComplexOscillator(double frequency, double sampleRate)
    {
        super(frequency, sampleRate);
    }

    /**
     * Updates the internal values after a frequency or sample rate change
     */
    @Override
    protected void update()
    {
        super.update();
        mCosineAngle = (float)FastMath.cos(getAnglePerSample());
        mSineAngle = (float)FastMath.sin(getAnglePerSample());
    }

    @Override
    public float[] generate(int sampleCount)
    {
        float[] samples = new float[sampleCount * 2];

        float inphase, quadrature, previousInphase, previousQuadrature, gain;

        previousInphase = mPreviousInphase;
        previousQuadrature = mPreviousQuadrature;

        for(int samplePointer = 0; samplePointer < samples.length; samplePointer += 2)
        {
            gain = THREE_HALVES - ((previousInphase * previousInphase) + (previousQuadrature * previousQuadrature));

            samples[samplePointer] = ((previousInphase * mCosineAngle) - (previousQuadrature * mSineAngle)) * gain;
            samples[samplePointer + 1] = ((previousInphase * mSineAngle) - (previousQuadrature * mCosineAngle)) * gain;

            previousInphase = samples[samplePointer];
            previousQuadrature = samples[samplePointer + 1];

//            if(samplePointer % 10 == 0)
//            {
//                gain = THREE_HALVES - ((previousInphase * previousInphase) + (previousQuadrature * previousQuadrature));
//                previousInphase *= gain;
//                previousQuadrature *= gain;
//            }
        }

        mPreviousInphase = previousInphase;
        mPreviousQuadrature = previousQuadrature;

        return samples;
    }
}

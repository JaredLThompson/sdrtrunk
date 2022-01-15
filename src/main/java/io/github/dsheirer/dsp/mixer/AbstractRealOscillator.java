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

/**
 * Real Oscillator base class.
 */
public abstract class AbstractRealOscillator implements IRealOscillator
{
    protected static final double TWO_PI = Math.PI * 2.0d;
    private double mFrequency;
    private double mSampleRate;
    protected float mAnglePerSample;
    protected float mCurrentPhase = 0.0f;

    /**
     * Constructs an instance
     * @param frequency in hertz
     * @param sampleRate in hertz
     */
    public AbstractRealOscillator(double frequency, double sampleRate)
    {
        mFrequency = frequency;
        mSampleRate = sampleRate;

        update();
    }

    /**
     * Angle per sample based on current frequency and sample rate
     * @return angle per sample in radians
     */
    protected float getAnglePerSample()
    {
        return mAnglePerSample;
    }

    /**
     * Current phase angle
     * @return current phase in radians
     */
    protected float getCurrentPhase()
    {
        return mCurrentPhase;
    }

    /**
     * Sets the current phase angle
     * @param currentPhase angle in radians
     */
    protected void setCurrentPhase(float currentPhase)
    {
        mCurrentPhase = currentPhase;
    }

    /**
     * Updates/calculates the angle per sample from the frequency and sample rate values.
     */
    private void update()
    {
        mAnglePerSample = (float)(TWO_PI * getFrequency() / getSampleRate());
    }

    /**
     * Frequency for this oscillator
     * @return frequency in Hertz
     */
    @Override
    public double getFrequency()
    {
        return mFrequency;
    }

    /**
     * Sets the frequency for this oscillator
     * @param frequency in hertz
     */
    @Override
    public void setFrequency(double frequency)
    {
        mFrequency = frequency;
        update();
    }

    /**
     * Sample rate for this oscillator
     * @return sample rate in Hertz
     */
    @Override
    public double getSampleRate()
    {
        return mSampleRate;
    }

    /**
     * Sets the sample rate for this oscillator
     * @param sampleRate in hertz
     */
    @Override
    public void setSampleRate(double sampleRate)
    {
        mSampleRate = sampleRate;
        update();
    }

    /**
     * Indicates if this oscillator is enabled, meaning it has a frequency that is not equal to zero.
     */
    @Override
    public boolean isEnabled()
    {
        return mFrequency != 0.0d;
    }
}

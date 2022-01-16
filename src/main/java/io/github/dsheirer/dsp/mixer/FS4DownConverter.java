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

public class FS4DownConverter
{
    private int mPointer = 0;

    /**
     * Performs complex frequency conversion at one-fourth (-FS/4) of the sample rate using simple value
     * assignments.
     *
     * See: Digital Signal Processing 3e, Lyons, p.674-675
     */
    public FS4DownConverter()
    {
    }

    public float[] mixComplex(float[] samples)
    {
        float real;

        for(int x = 0; x < samples.length; x += 2)
        {
            switch(mPointer)
            {
                case 0:
                    //no-op
                    break;
                case 1:
                    real = samples[x];
                    samples[x] = samples[x + 1];
                    samples[x + 1] = -real;
                    break;
                case 2:
                    samples[x] = -samples[x];
                    samples[x + 1] = -samples[x + 1];
                    break;
                case 3:
                    real = samples[x];
                    samples[x] = -samples[x + 1];
                    samples[x + 1] = real;
                    break;
            }

            mPointer++;
            mPointer %= 4;
        }

        return samples;
    }
}

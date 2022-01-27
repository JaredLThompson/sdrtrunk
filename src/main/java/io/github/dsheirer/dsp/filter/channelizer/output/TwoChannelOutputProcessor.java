/*******************************************************************************
 * sdr-trunk
 * Copyright (C) 2014-2018 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by  the Free Software Foundation, either version 3 of the License, or  (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without even the implied
 * warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License  along with this program.
 * If not, see <http://www.gnu.org/licenses/>
 *
 ******************************************************************************/
package io.github.dsheirer.dsp.filter.channelizer.output;

import io.github.dsheirer.dsp.filter.channelizer.TwoChannelSynthesizerM2;
import io.github.dsheirer.dsp.oscillator.FS4DownConverter;
import io.github.dsheirer.sample.buffer.ComplexSamplesAssembler;
import io.github.dsheirer.sample.buffer.ReusableChannelResultsBuffer;

import java.util.List;

public class TwoChannelOutputProcessor extends ChannelOutputProcessor
{
//    private final static Logger mLog = LoggerFactory.getLogger(TwoChannelOutputProcessor.class);
    private TwoChannelSynthesizerM2 mSynthesizer;
    private FS4DownConverter mFS4DownConverter = new FS4DownConverter();

    private int mChannelOffset1;
    private int mChannelOffset2;

    /**
     * Processor to extract two channels from a polyphase channelizer, synthesize/recombine the channels, apply
     * frequency translation and frequency correction, down-sample by a factor of two and output an I/Q complex sample.
     *
     * @param sampleRate of the output sample stream.
     * @param channelIndexes containing two channel indices.
     * @param gain to apply to output.  Typically this is equal to the channelizer's channel count.
     */
    public TwoChannelOutputProcessor(double sampleRate, List<Integer> channelIndexes, float[] filter, double gain)
    {
        //Set the frequency correction oscillator to 2 x output sample rate since we'll be correcting the frequency
        //after synthesizing both input channels
        super(2, sampleRate, gain);
        setPolyphaseChannelIndices(channelIndexes);
        setSynthesisFilter(filter);
    }

    /**
     * Sets the frequency offset to apply to the incoming samples to mix the desired signal to baseband.
     *
     * @param frequencyOffset in hertz
     */
    @Override
    public void setFrequencyOffset(long frequencyOffset)
    {
        super.setFrequencyOffset(frequencyOffset);
    }


    @Override
    public void setSynthesisFilter(float[] filter)
    {
        mSynthesizer = new TwoChannelSynthesizerM2(filter);
    }

    /**
     * Updates this processor to extract the two specified channel indexes.
     *
     * @param indexes containing a single channel index value.
     * @throws IllegalArgumentException if the list of indexes does not contain a single channel index.
     */
    public void setPolyphaseChannelIndices(List<Integer> indexes)
    {
        if(indexes.size() != 2)
        {
            throw new IllegalArgumentException("Double channel output processor requires two indexes to " +
                "process - provided indexes " + indexes.toString());
        }

        //Set the channelized output results offsets to twice the channel index to account for each channel having
        //an I/Q pair
        mChannelOffset1 = indexes.get(0) * 2;
        mChannelOffset2 = indexes.get(1) * 2;
    }

    /**
     * Extract the channel from the channel results array, apply frequency translation, and deliver the
     * extracted frequency-corrected channel I/Q sample set to the complex sample listener.
     *
     * @param channelResultsBuffers to process containing an array of channel I/Q sample pairs (I0,Q0,I1,Q1...In,Qn)
     * @param complexSamplesAssembler to receive the extracted, frequency-translated channel results
     */
    @Override
    public void process(List<ReusableChannelResultsBuffer> channelResultsBuffers,
                        ComplexSamplesAssembler complexSamplesAssembler)
    {
        for(ReusableChannelResultsBuffer buffer : channelResultsBuffers)
        {
//TODO:            ReusableComplexBuffer channel1 = buffer.getChannel(mChannelOffset1);
//TODO:            ReusableComplexBuffer channel2 = buffer.getChannel(mChannelOffset2);

            //Join the two channels using the synthesizer
//TODO:            ReusableComplexBuffer synthesized = mSynthesizer.process(channel1, channel2);

            //The synthesized channels are centered at +FS/4 ... downconvert to center the spectrum
//TODO:            mFS4DownConverter.mixComplex(synthesized.getSamples());

            //Apply offset and frequency correction to center the signal of interest within the synthesized channel
//TODO:            getFrequencyCorrectionMixer().mixComplex(synthesized.getSamples());

//TODO:            synthesized.applyGain(getGain());

//TODO:            complexSamplesAssembler.receive(synthesized);

            buffer.decrementUserCount();
        }
    }
}

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
package io.github.dsheirer.sample.buffer;

import io.github.dsheirer.sample.Broadcaster;
import io.github.dsheirer.sample.Listener;
import io.github.dsheirer.sample.complex.InterleavedComplexSamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedTransferQueue;

/**
 * Delay buffer/queue with support for pre-loading new listeners (ie channels)
 * with delayed sample buffers and broadcasting of incoming sample buffers to a list of listeners.
 *
 * This class is designed to compensate/account for any delays in processing of a control channel and subsequent
 * channel grants so that when a new channel is allocated, that channel can be pre-loaded with time delayed sample
 * buffers that start as close as possible to the channel grant timestamp, in order to avoid cutting off the
 * beginning of a channel grant transmission.
 *
 * New channel listeners are enqueued with a request timestamp and as new buffers arrive, the new channel listeners
 * are preloaded with delayed buffers from the queue and then added to an internal broadcaster so that they
 * receive all subsequent buffers as normal.
 *
 * This class is designed to be controlled by the sample producer thread for adding new listeners and pre-loading
 * delayed buffers.  Any listeners that are added to this class are expected to implement a non-blocking receive method
 * so as not to delay the stream of sample buffers.  Channel listeners are expected to implement buffer queue processing
 * on another thread.
 */
public class InterleavedComplexSamplesDelayBuffer implements Listener<InterleavedComplexSamples>
{
    private final static Logger mLog = LoggerFactory.getLogger(InterleavedComplexSamplesDelayBuffer.class);

    private Broadcaster<InterleavedComplexSamples> mBroadcaster = new Broadcaster<>();
    private LinkedTransferQueue<ActionRequest> mActionQueue = new LinkedTransferQueue<>();
    private InterleavedComplexSamples[] mDelayBuffer;
    private int mDelayBufferPointer = 0;
    private long mBufferDuration;

    /**
     * Creates a new delay buffer with the specified delay length and the buffer sample rate.
     *
     * @param size of the delay queue
     * @param bufferDuration in milliseconds for each complex buffer processed by this instance
     */
    public InterleavedComplexSamplesDelayBuffer(int size, long bufferDuration)
    {
        mDelayBuffer = new InterleavedComplexSamples[size];
        mBufferDuration = bufferDuration;
    }

    /**
     * Prepares this instance for disposal by releasing all stored sample buffers.
     */
    public void dispose()
    {
        clearBuffer();
        mBroadcaster.clear();
    }

    public void clear()
    {
        //Submit a clear buffer request to be processed upon the next buffer that arrives
        mActionQueue.offer(new ActionRequest());
    }

    /**
     * Clears any delayed/enqueued sample buffers.
     */
    private void clearBuffer()
    {
        for(int x = 0; x < mDelayBuffer.length; x++)
        {
            mDelayBuffer[x] = null;
        }

        mDelayBufferPointer = 0;
    }

    @Override
    public synchronized void receive(InterleavedComplexSamples samples)
    {
        ActionRequest actionRequest = mActionQueue.poll();

        while(actionRequest != null)
        {
            switch(actionRequest.getAction())
            {
                case ADD_LISTENER:
                    processNewListener(actionRequest);
                    break;
                case REMOVE_LISTENER:
                    mBroadcaster.removeListener(actionRequest.getListener());
                    break;
                case CLEAR_BUFFER:
                    clearBuffer();
                    break;
            }

            actionRequest = mActionQueue.poll();
        }

        mBroadcaster.receive(samples);

        //Store the new buffer in the delay queue and increment the pointer
        mDelayBuffer[mDelayBufferPointer++] = samples;

        //Wrap the delay buffer pointer as needed
        mDelayBufferPointer %= mDelayBuffer.length;
    }

    /**
     * Indicates if any listeners are registered with this delay buffer
     */
    public boolean hasListeners()
    {
        return mBroadcaster.hasListeners();
    }

    /**
     * Processes any newly added listeners by checking all buffers in the delay queue and pre-loading the
     * listeners with any buffers that occur on or after the listener's requested start timestamp.
     */
    private void processNewListener(ActionRequest listenerToAdd)
    {
        InterleavedComplexSamples toEvaluate;

        int pointer = mDelayBufferPointer;

        for(int x = 0; x < mDelayBuffer.length; x++)
        {
            toEvaluate = mDelayBuffer[pointer];

            if(toEvaluate != null &&
                ((toEvaluate.timestamp() + mBufferDuration) >= listenerToAdd.getTimestamp()))
            {
                listenerToAdd.getListener().receive(toEvaluate);
            }

            pointer++;

            if(pointer >= mDelayBuffer.length)
            {
                pointer = 0;
            }
        }

        mBroadcaster.addListener(listenerToAdd.getListener());
    }

    /**
     * Adds the listener to receive a copy of each buffer.  The listener will be preloaded with delayed buffers
     * that are at or after the specified timestamp.
     *
     * @param listener to add
     * @param timestamp of the oldest sample buffers to preload to the listener
     */
    public void addListener(Listener<InterleavedComplexSamples> listener, long timestamp)
    {
        mActionQueue.add(new ActionRequest(listener, timestamp));
    }

    /**
     * Removes the listener from receiving sample buffers
     */
    public void removeListener(Listener<InterleavedComplexSamples> listener)
    {
        mActionQueue.add(new ActionRequest(listener));
    }

    private enum Action{ADD_LISTENER, REMOVE_LISTENER, CLEAR_BUFFER};

    /**
     * Actions that must be completed on the incoming sample stream thread.
     */
    public class ActionRequest
    {
        private Action mAction;
        private Listener<InterleavedComplexSamples> mListener;
        private long mTimestamp;

        /**
         * Creates an add listener request
         * @param listener to add
         * @param timestamp for the preloading buffers to the listener from the delay queue
         */
        public ActionRequest(Listener<InterleavedComplexSamples> listener, long timestamp)
        {
            mAction = Action.ADD_LISTENER;
            mListener = listener;
            mTimestamp = timestamp;
        }

        /**
         * Creates a remove listener request
         * @param listener
         */
        public ActionRequest(Listener<InterleavedComplexSamples> listener)
        {
            mAction = Action.REMOVE_LISTENER;
            mListener = listener;
        }

        /**
         * Creates a clearBuffer buffer request
         */
        public ActionRequest()
        {
            mAction = Action.CLEAR_BUFFER;
        }

        public Action getAction()
        {
            return mAction;
        }

        /**
         * Listener to be added/removed
         */
        public Listener<InterleavedComplexSamples> getListener()
        {
            return mListener;
        }

        /**
         * Requested timestamp for buffers to be preloaded.
         */
        public long getTimestamp()
        {
            return mTimestamp;
        }
    }
}

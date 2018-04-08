package com.mtaylor76.pitcher

import android.app.Activity
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import kotlin.experimental.and

class PlaySound: Activity() {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    // Thanks Steve!
    private val duration = 3 // seconds
    private val sampleRate = 8000
    private val numSamples = duration * sampleRate
    private val sample = DoubleArray(numSamples)
    private val freqOfTone = 440.0 // hz
    private val generatedSnd = ByteArray(2 * numSamples)
    internal var handler = Handler()
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }
    override protected fun onResume() {
        super.onResume()
        // Use a new tread as this can take a while
        val thread = Thread(object:Runnable {
            public override fun run() {
                genTone()
                handler.post(object:Runnable {
                    public override fun run() {
                        playSound()
                    }
                })
            }
        })
        thread.start()
    }
    internal fun genTone() {
        // fill out the array
        for (i in 0 until numSamples)
        {
            sample[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (sampleRate / freqOfTone))
        }
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        var idx = 0
        for (dVal in sample)
        {
            // scale to maximum amplitude
            val `val` = ((dVal * 32767)).toInt()
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (`val` and 0x00ff.toInt()).toByte()
            generatedSnd[idx++] = ((`val` and 0xff00.toInt()).ushr(8)).toByte()
        }
    }
    internal fun playSound() {
        val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.size,
                AudioTrack.MODE_STATIC)
        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
    }
}
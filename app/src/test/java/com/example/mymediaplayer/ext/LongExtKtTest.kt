package com.example.mymediaplayer.ext

import org.junit.Assert
import org.junit.Test

internal class LongExtKtTest {

    @Test
    fun durationText() {
        val sec = 1000L
        val min = 60 * sec
        Assert.assertEquals("3:00", (3 * min).durationText())
        Assert.assertEquals("5:10", (5 * min + 10 * sec).durationText())
        Assert.assertEquals("15:59", (15 * min + 59 * sec).durationText())
        Assert.assertEquals("6:01", (5 * min + 61 * sec).durationText())
        Assert.assertEquals("60:00", (60 * min).durationText())
        Assert.assertEquals("61:01", (61 * min + sec).durationText())
    }
}
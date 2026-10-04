// Copyright (c) 2025-2026 fei_cong(https://github.com/feicong/feicong-course)

package com.eltavine.duckdetector.features.tee.data.verification.keystore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProbeMathTest {
    @Test
    fun monoTimer() {
        val timer = StableTimeSource(false, { error("Unexpected register read") }, { 42L })
        assertEquals(42L, timer.readNs())
    }

    @Test
    fun registerTimer() {
        val timer = StableTimeSource(true, { 73L }, { error("Unexpected monotonic read") })
        assertEquals(73L, timer.readNs())
    }

    @Test
    fun missingRegister() {
        val timer = StableTimeSource(true, { null }, { error("Unexpected monotonic read") })
        assertThrows(IllegalStateException::class.java) { timer.readNs() }
    }

    @Test
    fun pairedSamples() {
        assertEquals(listOf(2.0, -1.0), pairedDiffSeries(listOf(5.0, 2.0, 9.0), listOf(3.0, 3.0)))
        assertEquals(emptyList<Double>(), pairedDiffSeries(emptyList(), listOf(1.0)))
    }
}

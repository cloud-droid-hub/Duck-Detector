// Copyright (c) 2025-2026 fei_cong(https://github.com/feicong/feicong-course)

package com.eltavine.duckdetector.features.tee.data.verification.keystore

internal data class StableTimeSource(
    private val registerOnly: Boolean,
    private val readRegister: () -> Long?,
    private val readMono: () -> Long,
) {
    fun readNs(): Long {
        if (registerOnly) {
            return readRegister() ?: throw IllegalStateException(
                "Selected register timer is unavailable.",
            )
        }
        return readMono()
    }
}

internal fun pairedDiffSeries(left: List<Double>, right: List<Double>): List<Double> {
    val count = minOf(left.size, right.size)
    return List(count) { index -> left[index] - right[index] }
}

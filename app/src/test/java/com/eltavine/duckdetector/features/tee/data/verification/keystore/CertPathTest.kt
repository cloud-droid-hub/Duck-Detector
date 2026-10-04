// Copyright (c) 2025-2026 fei_cong(https://github.com/feicong/feicong-course)

package com.eltavine.duckdetector.features.tee.data.verification.keystore

import com.eltavine.duckdetector.features.tee.data.attestation.RootOfTrustSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CertPathTest {
    private fun root(locked: Boolean? = true, state: String? = "Verified") = RootOfTrustSnapshot(
        verifiedBootKeyHex = null,
        deviceLocked = locked,
        verifiedBootState = state,
        verifiedBootHashHex = null,
    )

    @Test
    fun sameRoots() {
        assertEquals(CertPathKind.NONE, classifyCert(root(), root(state = "verified"), true))
        assertTrue(rootDiff(root(), root()).isEmpty())
    }

    @Test
    fun missingRoots() {
        assertEquals(CertPathKind.UNAVAILABLE, classifyCert(root(), null, true))
        assertEquals(CertPathKind.UNAVAILABLE, classifyCert(null, root(), true))
        assertTrue(rootDiff(root(), null).isEmpty())
    }

    @Test
    fun partialRoots() {
        assertEquals(CertPathKind.UNAVAILABLE, classifyCert(root(null, null), root(), true))
        assertTrue(rootDiff(root(null, null), root()).isEmpty())
    }

    @Test
    fun fieldDifferences() {
        val diff = rootDiff(root(), root(false, "Unverified"))
        assertEquals(2, diff.size)
        assertTrue(diff.any { it.startsWith("deviceLocked:") })
        assertTrue(diff.any { it.startsWith("verifiedBootState:") })
        assertEquals(CertPathKind.ROOT_DIFF, classifyCert(root(), root(false, "Unverified"), false))
    }

    @Test
    fun noRkpEvidence() {
        assertEquals(CertPathKind.NO_RKP, classifyCert(root(), root(), false))
        assertEquals(CertPathKind.UNAVAILABLE, classifyCert(root(), root(), null))
    }

    @Test
    fun rkpErrors() {
        assertTrue(rkpFailure("OUT_OF_KEYS_PENDING_INTERNET_CONNECTIVITY"))
        assertTrue(rkpFailure("OUT_OF_KEYS_PERMANENT_ERROR"))
        assertTrue(rkpFailure("OUT_OF_KEYS_TRANSIENT_ERROR"))
        assertTrue(rkpFailure("OUT_OF_KEYS_REQUIRES_SYSTEM_UPGRADE"))
        assertFalse(rkpFailure("SYSTEM_ERROR"))
        assertFalse(rkpFailure("BINDER_DIED"))
        assertFalse(rkpFailure("NoSuchMethodException: generateKey"))
    }
}

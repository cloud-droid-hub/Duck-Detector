// Copyright (c) 2025-2026 fei_cong(https://github.com/feicong/feicong-course)

package com.eltavine.duckdetector.features.tee.data.verification.keystore

import com.eltavine.duckdetector.features.tee.data.attestation.AttestationExtensionParser
import com.eltavine.duckdetector.features.tee.data.attestation.RootOfTrustSnapshot
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.UUID

class CertPathProbe(
    private val client: Keystore2PrivateBinderClient = Keystore2PrivateBinderClient(),
    private val parser: AttestationExtensionParser = AttestationExtensionParser(),
) {
    fun inspect(useStrongBox: Boolean = false): CertPathResult {
        return runCatching {
        val opened = client.openSession(useStrongBox = useStrongBox)
        val session = opened.session ?: return unavailable(
            CertPathKind.UNAVAILABLE,
            opened.failureReason ?: "Keystore2 private binder session unavailable.",
        )
        val made = mutableListOf<Any>()
        try {
            val rkp = generatePath(session, null, made, "rkp")
            if (rkp.failure != null) {
                val message = client.describeThrowable(rkp.failure)
                return unavailable(
                    if (rkpFailure(message)) CertPathKind.RKP_FAILURE else CertPathKind.UNAVAILABLE,
                    message,
                )
            }
            val attKey = makeAttKey(session, made) ?: return unavailable(
                CertPathKind.UNAVAILABLE,
                "Unable to provision a PURPOSE_ATTEST_KEY reference key.",
            )
            val user = generatePath(session, attKey, made, "attested")
            if (user.failure != null) {
                return unavailable(CertPathKind.UNAVAILABLE, client.describeThrowable(user.failure))
            }
            val diff = rootDiff(rkp.root, user.root)
            val kind = classifyCert(rkp.root, user.root, rkp.hasRkp)
            CertPathResult(
                probeRan = true,
                dataReady = knownRoot(rkp.root) && knownRoot(user.root),
                kind = kind,
                pairs = 1,
                rkpObserved = rkp.hasRkp,
                rkpRoot = rkp.root.describe(),
                userRoot = user.root.describe(),
                diff = diff,
                detail = "kind=${kind.name}, pairs=1, rkpObserved=${rkp.hasRkp}, differences=${diff.joinToString()}",
            )
        } finally {
            try {
                made.distinctBy { System.identityHashCode(it) }.forEach {
                    client.deleteKey(session.service, it)
                }
            } finally {
                client.closeSession(session)
            }
        }
        }.getOrElse { unavailable(CertPathKind.UNAVAILABLE, client.describeThrowable(it)) }
    }

    private fun makeAttKey(session: Keystore2PrivateSession, made: MutableList<Any>): Any? {
        val requested = client.createKeyDescriptor(alias("attestkey"))
        made += requested
        return runCatching {
            val metadata = client.generateAttestationKey(session.securityLevel, requested)
            client.resolveFollowUpDescriptor(requested, metadata).also { made += it }
        }.getOrNull()
    }

    private fun generatePath(
        session: Keystore2PrivateSession,
        attKey: Any?,
        made: MutableList<Any>,
        tag: String,
    ): PathSample {
        val requested = client.createKeyDescriptor(alias(tag))
        made += requested
        return runCatching {
            val metadata = client.generateSigningKey(
                securityLevel = session.securityLevel,
                keyDescriptor = requested,
                attestationKeyDescriptor = attKey,
                attest = true,
            )
            val followUp = client.resolveFollowUpDescriptor(requested, metadata)
            made += followUp
            readChainFacts(session, followUp)
        }.getOrElse { PathSample(failure = it) }
    }

    private fun readChainFacts(session: Keystore2PrivateSession, descriptor: Any): PathSample {
        return runCatching {
            val response = client.getKeyEntryResponse(session.service, descriptor)
                ?: return PathSample()
            val factory = CertificateFactory.getInstance("X.509")
            val leaf = client.getCertificateBlob(response)?.let {
                factory.generateCertificate(ByteArrayInputStream(it)) as? X509Certificate
            }
            val chain = client.getCertificateChainBlob(response)?.let {
                factory.generateCertificates(ByteArrayInputStream(it)).filterIsInstance<X509Certificate>()
            } ?: emptyList()
            PathSample(
                root = leaf?.let { parser.parse(listOf(it), ByteArray(0)).rootOfTrust },
                hasRkp = (listOfNotNull(leaf) + chain).any { it.getExtensionValue(PROVISION_OID) != null },
            )
        }.getOrElse { PathSample(failure = it) }
    }

    private fun unavailable(kind: CertPathKind, detail: String) = CertPathResult(
        probeRan = true,
        kind = kind,
        detail = detail,
    )

    private fun alias(tag: String) = "duck_pp_${tag}_${UUID.randomUUID()}"

    private data class PathSample(
        val root: RootOfTrustSnapshot? = null,
        val hasRkp: Boolean? = null,
        val failure: Throwable? = null,
    )

    companion object {
        private const val PROVISION_OID = "1.3.6.1.4.1.11129.2.1.30"
    }
}

enum class CertPathKind { NONE, ROOT_DIFF, RKP_FAILURE, NO_RKP, UNAVAILABLE }

data class CertPathResult(
    val probeRan: Boolean,
    val dataReady: Boolean = false,
    val kind: CertPathKind = CertPathKind.UNAVAILABLE,
    val pairs: Int = 0,
    val rkpObserved: Boolean? = null,
    val rkpRoot: String? = null,
    val userRoot: String? = null,
    val diff: List<String> = emptyList(),
    val detail: String,
)

internal fun classifyCert(
    rkp: RootOfTrustSnapshot?,
    user: RootOfTrustSnapshot?,
    hasRkp: Boolean?,
): CertPathKind = when {
    rootDiff(rkp, user).isNotEmpty() -> CertPathKind.ROOT_DIFF
    !knownRoot(rkp) || !knownRoot(user) -> CertPathKind.UNAVAILABLE
    hasRkp == false -> CertPathKind.NO_RKP
    hasRkp == null -> CertPathKind.UNAVAILABLE
    else -> CertPathKind.NONE
}

private fun knownRoot(root: RootOfTrustSnapshot?): Boolean =
    root?.deviceLocked != null && !root.verifiedBootState.isNullOrBlank()

internal fun rootDiff(rkp: RootOfTrustSnapshot?, user: RootOfTrustSnapshot?): List<String> {
    if (rkp == null || user == null) return emptyList()
    return buildList {
        if (rkp.deviceLocked != null && user.deviceLocked != null && rkp.deviceLocked != user.deviceLocked) {
            add("deviceLocked: rkpPath=${rkp.deviceLocked}, attestKeyPath=${user.deviceLocked}")
        }
        val rkpState = rkp.verifiedBootState
        val userState = user.verifiedBootState
        if (!rkpState.isNullOrBlank() && !userState.isNullOrBlank() && !rkpState.equals(userState, true)) {
            add("verifiedBootState: rkpPath=$rkpState, attestKeyPath=$userState")
        }
    }
}

internal fun rkpFailure(message: String): Boolean {
    val text = message.lowercase(Locale.US)
    return listOf("out_of_keys", "outofkeys", "pending_internet_connectivity", "requires_system_upgrade",
        "remotely_provisioned", "rkpd", "rkp").any { text.contains(it) }
}

private fun RootOfTrustSnapshot?.describe(): String? = this?.let {
    "deviceLocked=${it.deviceLocked ?: "n/a"}, verifiedBootState=${it.verifiedBootState ?: "n/a"}"
}

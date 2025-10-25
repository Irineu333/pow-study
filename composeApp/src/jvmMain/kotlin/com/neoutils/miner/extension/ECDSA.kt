package com.neoutils.miner.extension

import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec

fun generateECDSAKeyPair(
    spec: ECGenParameterSpec = ECGenParameterSpec("secp256r1")
): Pair<PublicKey, PrivateKey> {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC")
    keyPairGenerator.initialize(spec, SecureRandom())
    val keyPair = keyPairGenerator.generateKeyPair()
    return Pair(keyPair.public, keyPair.private)
}

fun PublicKey.toHexString(): String {
    return encoded.joinToString("") { "%02x".format(it) }
}

fun PrivateKey.toHexString(): String {
    return encoded.joinToString("") { "%02x".format(it) }
}

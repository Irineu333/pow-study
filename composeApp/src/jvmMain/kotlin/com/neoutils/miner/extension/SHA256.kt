package com.neoutils.miner.extension

import java.security.MessageDigest

fun String.sha256(): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray())

    return bytes.joinToString("") { "%02x".format(it) }
}
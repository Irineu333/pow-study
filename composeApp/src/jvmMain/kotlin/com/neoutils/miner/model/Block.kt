package com.neoutils.miner.model

import com.neoutils.miner.extension.sha256

data class Block(
    val index: Int = 0,
    val difficulty: Int = 0,
    val prev: String = "0".repeat(64),
    val timestamp: Long = System.currentTimeMillis(),
    val data: String = "",
    val nonce: Long = 0
) {
    val hash = toString().sha256()
    val valid = hash.startsWith("0".repeat(difficulty))

    override fun toString(): String {
        return "$index\n$prev\n$difficulty\n$nonce\n"
    }
}
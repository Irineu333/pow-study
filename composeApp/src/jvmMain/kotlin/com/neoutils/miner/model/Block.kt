package com.neoutils.miner.model

import com.neoutils.miner.extension.sha256

data class Block(
    val index: Int = 0,
    val difficulty: Int = 0,
    val prev: String = "0".repeat(64),
    val timestamp: Long = System.currentTimeMillis(),
    val transactions: List<Transaction> = emptyList(),
    val nonce: Long = 0
) {
    val hash = toString().sha256()

    init {
        check(hash.startsWith("0".repeat(difficulty))) { "Invalid block hash" }
    }

    override fun toString() = listOf(
        index,
        difficulty,
        prev,
        timestamp,
        transactions.joinToString(separator = "\n"),
        nonce
    ).joinToString(separator = "\n")
}



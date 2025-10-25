package com.neoutils.miner.model

import com.neoutils.miner.extension.sha256

data class BlockBuilder(
    var index: Int = 0,
    var difficulty: Int = 0,
    var prev: String = "0".repeat(64),
    var timestamp: Long = System.currentTimeMillis(),
    var transactions: List<Transaction> = listOf(),
    var nonce: Long = 0,
) {
    val hash get() = toString().sha256()

    fun isValid(): Boolean {
        return hash.startsWith("0".repeat(difficulty))
    }

    fun build() = Block(
        index = index,
        difficulty = difficulty,
        prev = prev,
        timestamp = timestamp,
        transactions = transactions,
        nonce = nonce
    )

    override fun toString() = listOf(
        index,
        difficulty,
        prev,
        timestamp,
        transactions.joinToString(separator = "\n"),
        nonce
    ).joinToString(separator = "\n")
}
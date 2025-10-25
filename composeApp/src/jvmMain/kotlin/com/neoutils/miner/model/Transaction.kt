package com.neoutils.miner.model

data class Transaction(
    val from: String,
    val to: String,
    val amount: Long,
    val timestamp: Long = System.currentTimeMillis(),
) {
    override fun toString() = listOf(
        from, to, amount, timestamp
    ).joinToString(separator = ",")
}

fun createCoinbaseTransaction(
    minerPublicKey: String,
    reward: Long = 50L
): Transaction {
    return Transaction(
        from = "COINBASE",
        to = minerPublicKey,
        amount = reward,
    )
}

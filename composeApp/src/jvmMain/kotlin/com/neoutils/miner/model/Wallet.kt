package com.neoutils.miner.model

import com.neoutils.miner.extension.sha256

data class Wallet(
    val publicKey: String,
    val privateKey: String,
) {
    val address = publicKey.sha256()
}
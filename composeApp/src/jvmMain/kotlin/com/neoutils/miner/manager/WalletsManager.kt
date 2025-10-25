package com.neoutils.miner.manager

import com.neoutils.miner.extension.generateECDSAKeyPair
import com.neoutils.miner.extension.toHexString
import com.neoutils.miner.model.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WalletsManager {

    val flow: StateFlow<List<Wallet>>

    fun createWallet(): Wallet
}

class WalletsManagerImpl : WalletsManager {

    private val _flow = MutableStateFlow<List<Wallet>>(emptyList())
    override val flow get() = _flow.asStateFlow()

    override fun createWallet(): Wallet {
        val (publicKey, privateKey) = generateECDSAKeyPair()

        val wallet = Wallet(
            publicKey = publicKey.toHexString(),
            privateKey = privateKey.toHexString()
        )

        _flow.value += wallet

        return wallet
    }
}
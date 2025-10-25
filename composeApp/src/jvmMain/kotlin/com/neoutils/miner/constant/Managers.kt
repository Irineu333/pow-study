package com.neoutils.miner.constant

import com.neoutils.miner.manager.BlockchainManager
import com.neoutils.miner.manager.BlockchainManagerImpl
import com.neoutils.miner.manager.WalletsManager
import com.neoutils.miner.manager.WalletsManagerImpl

object Managers {
    val blockchain: BlockchainManager = BlockchainManagerImpl()
    val wallets: WalletsManager = WalletsManagerImpl()
}
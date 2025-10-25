package com.neoutils.miner.manager

import com.neoutils.miner.model.Block
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface BlockchainManager {

    val flow: StateFlow<List<Block>>
    val blocks: List<Block> get() = flow.value.toList()

    fun addBlock(block: Block)
    fun getBalance(address: String): Long
}

class BlockchainManagerImpl : BlockchainManager {

    private val _flow = MutableStateFlow(listOf(Block()))
    override val flow get() = _flow.asStateFlow()

    override fun addBlock(block: Block) {

        val prev = blocks.last()

        check(prev.hash == block.prev) { "Invalid previous hash" }
        check(prev.index.inc() == block.index) { "Invalid block index" }

        _flow.value += block
    }

    override fun getBalance(address: String): Long {
        var balance = 0L

        for (block in blocks) {
            for (transaction in block.transactions) {
                if (transaction.to == address) {
                    balance += transaction.amount
                }
                if (transaction.from == address) {
                    balance -= transaction.amount
                }
            }
        }

        return balance
    }
}
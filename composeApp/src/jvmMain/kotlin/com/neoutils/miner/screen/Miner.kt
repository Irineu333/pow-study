@file:OptIn(ExperimentalMaterial3Api::class)

package com.neoutils.miner.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neoutils.miner.manager.BlockchainManager
import com.neoutils.miner.constant.Managers
import com.neoutils.miner.manager.WalletsManager
import com.neoutils.miner.model.Block
import com.neoutils.miner.model.BlockBuilder
import com.neoutils.miner.model.Wallet
import com.neoutils.miner.model.createCoinbaseTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MinerViewModel(
    private val blockchainManager: BlockchainManager = Managers.blockchain,
    private val walletsManager: WalletsManager = Managers.wallets
) : ViewModel() {

    private val running = MutableStateFlow(false)

    val uiState = combine(
        blockchainManager.flow,
        running,
    ) { blockchain, running ->
        MinerUiState(
            running = running,
            blocks = blockchain.map { block ->
                block.toUiState()
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = MinerUiState(),
    )

    private var minerJob: Job? = null
    private lateinit var wallet: Wallet

    fun onStartMining() {

        wallet = walletsManager.createWallet()

        running.value = true

        minerJob?.cancel()
        minerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val transactions = listOf(
                    createCoinbaseTransaction(wallet.address)
                )

                val prevBlock = blockchainManager.blocks.last()

                val block = BlockBuilder(
                    index = prevBlock.index + 1,
                    difficulty = 5,
                    prev = prevBlock.hash,
                    transactions = transactions,
                )

                while (isActive && !block.isValid()) block.nonce++

                if (block.isValid()) blockchainManager.addBlock(block.build())
            }
        }
    }

    fun onStopMining() {
        minerJob?.cancel()
        minerJob = null
        running.value = false
    }
}

data class MinerUiState(
    val running: Boolean = false,
    val blocks: List<Block> = listOf(),
) {
    data class Block(
        val index: Int,
        val nonce: Long,
        val hash: String,
    )
}

enum class TabItem(
    val title: String,
) {
    BLOCKCHAIN(
        title = "blockchain"
    ),
    WALLETS(
        title = "wallets"
    ),
    TRANSACTIONS(
        title = "transactions"
    );
}

fun Block.toUiState() = MinerUiState.Block(
    index = index,
    nonce = nonce,
    hash = hash,
)

@Composable
fun MinerRoute(
    viewModel: MinerViewModel = viewModel(),
) = MaterialTheme {

    val uiState = viewModel.uiState.collectAsState().value

    MinerScreen(
        uiState = uiState,
        onStartMining = {
            viewModel.onStartMining()
        },
        onStopMining = {
            viewModel.onStopMining()
        }
    )
}

@Composable
fun MinerScreen(
    uiState: MinerUiState,
    onStartMining: () -> Unit = { },
    onStopMining: () -> Unit = { },
    modifier: Modifier = Modifier
) = Scaffold(
    modifier = modifier,
    topBar = {
        TopAppBar(
            title = { Text(text = "Miner") },
            actions = {
                if (uiState.running) {
                    Button(
                        onClick = { onStopMining() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = LocalContentColor.current,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )

                            Text(text = "Stop")
                        }
                    }
                } else {
                    Button(
                        onClick = { onStartMining() }
                    ) {
                        Text(text = "Start")
                    }
                }
            }
        )
    },
) { padding ->
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
    ) {

        var selected by remember { mutableStateOf(TabItem.BLOCKCHAIN) }

        TabRow(
            selectedTabIndex = TabItem.entries.indexOf(selected),
        ) {
            TabItem.entries.forEach { tab ->
                Tab(
                    selected = selected == tab,
                    onClick = {
                        selected = tab
                    },
                    text = {
                        Text(text = tab.title)
                    }
                )
            }
        }

        val pagerState = rememberPagerState { TabItem.entries.size }

        LaunchedEffect(Unit) {
            snapshotFlow {
                TabItem.entries.indexOf(selected)
            }.collect { page ->
                pagerState.animateScrollToPage(page)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            when (TabItem.entries[index]) {
                TabItem.BLOCKCHAIN -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.blocks) { block ->
                            BlockCard(
                                block = block,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                TabItem.TRANSACTIONS -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(text = "transactions")
                    }
                }

                TabItem.WALLETS -> {
                    WalletsRoute(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun BlockCard(
    block: MinerUiState.Block,
    modifier: Modifier = Modifier
) = Card(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp)
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "nonce: ${block.nonce}",
                maxLines = 1,
            )

            BasicText(
                text = "block #${block.index}",
                maxLines = 1,
            )
        }

        BasicText(
            text = block.hash,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 4.sp),
            modifier = Modifier
                .background(
                    color = Color.LightGray,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
        )
    }
}
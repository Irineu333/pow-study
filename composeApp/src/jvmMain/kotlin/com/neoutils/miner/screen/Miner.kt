@file:OptIn(ExperimentalMaterial3Api::class)

package com.neoutils.miner.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neoutils.miner.model.Block
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MinerViewModel : ViewModel() {

    private val blockchain = mutableListOf(Block())

    private val _uiState = MutableStateFlow(MinerUiState(blocks = blockchain))
    val uiState: StateFlow<MinerUiState> = _uiState.asStateFlow()

    private var minerJob: Job? = null

    fun onStartMining() {
        minerJob?.cancel()
        minerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                var block: Block
                var nonce = 0L

                do {
                    block = Block(
                        index = blockchain.size,
                        nonce = nonce++,
                        difficulty = 6,
                        prev = blockchain.last().hash,
                    )

                    launch(Dispatchers.Main) {
                        _uiState.update { state ->
                            state.copy(
                                block = block,
                            )
                        }
                    }
                } while (!block.valid)

                blockchain.add(block)

                launch(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            blocks = blockchain,
                        )
                    }
                }
            }
        }
    }

    fun onStopMining() {
        minerJob?.cancel()
        minerJob = null

        _uiState.update {
            it.copy(
                block = null,
            )
        }
    }
}

data class MinerUiState(
    val block: Block? = null,
    val blocks: List<Block> = listOf(),
)

@Composable
fun MinerRoute(
    viewModel: MinerViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value

    MaterialTheme {
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
}

@Composable
fun MinerScreen(
    uiState: MinerUiState,
    onStartMining: () -> Unit = { },
    onStopMining: () -> Unit = { },
) = Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(text = "Miner") },
            actions = {
                if (uiState.block != null) {
                    Button(
                        onClick = { onStopMining() }
                    ) {
                        Text(text = "Stop")
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
    bottomBar = {
        uiState.block?.let {
            BlockCard(
                block = uiState.block,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.padding(it),
    ) {
        items(uiState.blocks) { block ->
            BlockCard(
                block = block,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
            )
        }
    }
}

@Composable
fun BlockCard(
    block: Block,
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
            autoSize = TextAutoSize.StepBased(),
            modifier = Modifier
                .background(
                    color = Color.LightGray,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
        )
    }
}
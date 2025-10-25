package com.neoutils.miner.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neoutils.miner.constant.Managers
import com.neoutils.miner.extension.sha256
import com.neoutils.miner.manager.BlockchainManager
import com.neoutils.miner.manager.WalletsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class WalletsViewModel(
    private val walletsManager: WalletsManager = Managers.wallets,
    private val blockchainManager: BlockchainManager = Managers.blockchain,
) : ViewModel() {

    val uiState = combine(
        walletsManager.flow,
        blockchainManager.flow
    ) { wallets, _ ->
        when {
            wallets.isEmpty() -> {
                WalletsUiState.Empty
            }

            else -> {
                WalletsUiState.Wallets(
                    wallets = wallets.map { wallet ->
                        WalletsUiState.Wallet(
                            address = wallet.address,
                            balance = blockchainManager.getBalance(wallet.address),
                        )
                    }
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = WalletsUiState.Empty,
    )

    fun onCreateWallet() {
        walletsManager.createWallet()
    }
}

sealed class WalletsUiState {

    data class Wallets(
        val wallets: List<Wallet> = emptyList(),
    ) : WalletsUiState()

    data object Empty : WalletsUiState()

    data class Wallet(
        val address: String,
        val balance: Long,
    )
}

@Composable
fun WalletsRoute(
    viewModel: WalletsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsState()

    WalletsScreen(
        uiState = uiState,
        onCreate = { viewModel.onCreateWallet() },
        modifier = modifier
    )
}

@Composable
fun WalletsScreen(
    uiState: WalletsUiState,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) = Box(modifier) {
    when (uiState) {
        is WalletsUiState.Empty -> {
            Button(
                onClick = onCreate,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(text = "Create Wallet")
            }
        }

        is WalletsUiState.Wallets -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.wallets) { wallet ->
                    WalletCard(wallet)
                }
            }

            FloatingActionButton(
                onClick = onCreate,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun WalletCard(
    wallet: WalletsUiState.Wallet,
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
        BasicText(
            text = "balance: ${wallet.balance}",
            maxLines = 1,
        )

        BasicText(
            text = wallet.address,
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
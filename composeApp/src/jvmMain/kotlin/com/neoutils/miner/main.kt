package com.neoutils.miner

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.neoutils.miner.screen.MinerRoute

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            width = 400.dp,
            height = 600.dp
        ),
        title = "miner",
    ) {
        MinerRoute()
    }
}

package com.plcoding.cryptotracker.crypto.presentation.coin_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.plcoding.cryptotracker.crypto.domain.Coin
import com.plcoding.cryptotracker.crypto.presentation.coin_list.components.CoinListItem
import com.plcoding.cryptotracker.crypto.presentation.models.CoinUi
import com.plcoding.cryptotracker.crypto.presentation.models.toCoinUi
import com.plcoding.cryptotracker.ui.theme.CryptoTrackerTheme

@Composable
fun CoinListScreen(
    coinState: CoinListState,
    onAction: (CoinListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if(coinState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(coinState.coins) { coinUi ->
                CoinListItem(
                    coinUi = coinUi,
                    onClick = {
                        onAction(CoinListAction.OnCoinClick(coinUi = coinUi))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CoinListScreenPreview() {
    CryptoTrackerTheme {
        CoinListScreen(
            coinState = CoinListState(
                coins = coinList()
            ),
            onAction = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

fun coinList():List<CoinUi> {
    return listOf(
        Coin(
            id = "bitcoin",
            rank = 1,
            name = "Bitcoin",
            symbols = "BTC",
            marketCapUsd = 2145695784.75,
            priceUsd = 65.23,
            changePercentage24Hr = 0.1
        ).toCoinUi(),

        Coin(
            id = "bitcoin",
            rank = 1,
            name = "Bitcoin",
            symbols = "BTC",
            marketCapUsd = 2145695784.75,
            priceUsd = 65.23,
            changePercentage24Hr = -0.1
        ).toCoinUi(),

        Coin(
            id = "bitcoin",
            rank = 1,
            name = "Bitcoin",
            symbols = "BTC",
            marketCapUsd = 2145695784.75,
            priceUsd = 65.23,
            changePercentage24Hr = 0.1
        ).toCoinUi()
    )
}
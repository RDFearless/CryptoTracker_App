package com.plcoding.cryptotracker.crypto.presentation.models

import android.icu.text.NumberFormat
import androidx.annotation.DrawableRes
import com.plcoding.cryptotracker.crypto.domain.Coin
import com.plcoding.cryptotracker.core.presentation.util.getDrawableIdForCoin
import com.plcoding.cryptotracker.crypto.presentation.coin_details.DataPoint
import java.util.Locale


data class CoinUi(
    val id: String,
    val rank: Int,
    val name: String,
    val symbols: String,
    val marketCapUsd: DisplayableFormat,
    val priceUsd: DisplayableFormat,
    val changePercentage24Hr: DisplayableFormat,
    @DrawableRes val iconRes: Int,
    val coinPrice: List<DataPoint> = emptyList()
)

data class DisplayableFormat (
    val value: Double,
    val formatted: String
)

fun Coin.toCoinUi(): CoinUi {
    return CoinUi (
        id = id,
        rank = rank,
        name = name,
        symbols = symbols,
        marketCapUsd = marketCapUsd.toDisplayableFormat(),
        priceUsd = priceUsd.toDisplayableFormat(),
        changePercentage24Hr = changePercentage24Hr.toDisplayableFormat(),
        iconRes = getDrawableIdForCoin(symbols)
    )
}

// Changes 11223344 to 11,223,344
fun Double.toDisplayableFormat(): DisplayableFormat {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    return DisplayableFormat(
        value = this,
        formatted = formatter.format(this)
    )
}

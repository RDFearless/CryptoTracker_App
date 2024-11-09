package com.plcoding.cryptotracker.crypto.domain

data class Coin(
    val id: String,
    val rank: Int,
    val name: String,
    val symbols: String,
    val marketCapUsd: Double,
    val priceUsd: Double,
    val changePercentage24Hr: Double
)

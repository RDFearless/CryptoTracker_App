package com.plcoding.cryptotracker.crypto.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoinPriceResponseDto(
    val data: List<CoinPriceDto>
)

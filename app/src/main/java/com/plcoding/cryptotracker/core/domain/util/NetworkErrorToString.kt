package com.plcoding.cryptotracker.core.domain.util

import android.content.Context
import com.plcoding.cryptotracker.R

fun NetworkError.toString(context: Context): String {
    val resID = when(this) {
        NetworkError.REQUEST_TIMEOUT -> R.string.request_time_out
        NetworkError.TOO_MANY_REQUEST -> R.string.too_many_requests
        NetworkError.NO_INTERNET -> R.string.no_internet
        NetworkError.SERVER_ERROR -> R.string.unknown
        NetworkError.SERIALIZATION -> R.string.serialization_error
        NetworkError.UNKNOWN -> R.string.unknown
    }

    return context.getString(resID)
}
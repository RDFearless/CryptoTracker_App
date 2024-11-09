package com.plcoding.cryptotracker.core.data.networking

import com.plcoding.cryptotracker.core.domain.util.NetworkError
import com.plcoding.cryptotracker.core.domain.util.Result
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import java.io.NotSerializableException
import kotlin.coroutines.coroutineContext

// returns a response on user call
/* checks for initial errors(eg. internet)
        iff everything is good, further sent to server*/
suspend inline fun <reified T>safeCall(
    execute: () -> HttpResponse
): Result<T, NetworkError> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(NetworkError.NO_INTERNET)
    } catch (e: NotSerializableException) {
        return Result.Error(NetworkError.SERIALIZATION)
    }
    // When we don't know the error
    catch (e: Exception) {
        coroutineContext.ensureActive()
        return Result.Error(NetworkError.UNKNOWN)
    }

    // If it passes with no error -> network call is made
    return responseToResult(response)
}
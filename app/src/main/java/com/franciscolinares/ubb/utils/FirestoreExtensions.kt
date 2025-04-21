package com.franciscolinares.ubb.utils
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

suspend fun <T> Task<T>.safeAwait(): T? = try {
    this.await()
} catch (e: Exception) {
    null
}
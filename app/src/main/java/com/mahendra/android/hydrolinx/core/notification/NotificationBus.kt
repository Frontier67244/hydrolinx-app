package com.mahendra.android.hydrolinx.core.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Singleton in-app event bus.
 * Publisher: ViewModel manapun yang ingin memberi feedback ke UI.
 * Subscriber: Composable root tiap fitur via LaunchedEffect + collect.
 */
object NotificationBus {

    private val _events = MutableSharedFlow<NotificationEvent>(
        replay = 0,
        extraBufferCapacity = 16,
    )
    val events: SharedFlow<NotificationEvent> = _events.asSharedFlow()

    fun emit(event: NotificationEvent) {
        _events.tryEmit(event)
    }
}

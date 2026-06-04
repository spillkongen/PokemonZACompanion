package com.pokemonza.companion.update

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class InstallEvent {
    data object Success : InstallEvent()
    data class Failed(val message: String) : InstallEvent()
}

object UpdateInstallNotifier {
    private val _events = MutableSharedFlow<InstallEvent>(extraBufferCapacity = 2)
    val events: SharedFlow<InstallEvent> = _events.asSharedFlow()

    fun onSuccess() {
        _events.tryEmit(InstallEvent.Success)
    }

    fun onFailed(message: String) {
        _events.tryEmit(InstallEvent.Failed(message))
    }
}

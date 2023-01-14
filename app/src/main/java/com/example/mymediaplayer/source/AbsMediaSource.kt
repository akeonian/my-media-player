package com.example.mymediaplayer.source

abstract class AbsMediaSource: MediaSource {

    protected var _state: State = State.STATE_CREATED
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }
    val state get() = _state

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    override fun whenReady(performAction: (Boolean) -> Unit) =
        when (state) {
            State.STATE_CREATED, State.STATE_INITIALIZING -> {
                onReadyListeners += performAction
                false
            }
            else -> {
                performAction(state != State.STATE_ERROR)
                true
            }
        }

    enum class State {
        STATE_CREATED, STATE_INITIALIZING, STATE_INITIALIZED, STATE_ERROR
    }
}
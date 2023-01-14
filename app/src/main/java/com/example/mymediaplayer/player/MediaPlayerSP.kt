package com.example.mymediaplayer.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.mymediaplayer.utils.LogUtils
import java.io.IOException

private const val TAG = "MediaPlayerSP"
class MediaPlayerSP(private val context: Context): SessionPlayer {

    /**
     * Since all the functions should be callable from any state.
     * functions has to check its state and then call MediaPlayer
     * methods, handle error using error states.
     * */

    private var _state: SessionPlayer.State = SessionPlayer.State.INITIALIZED
        set(value) {
            field = value
            sessionStateListener?.onPlaybackStateChanged(value)
        }
    override val state get() = _state
    override var seekPosition: Long
        get() = when (state) {
            SessionPlayer.State.PLAYING, SessionPlayer.State.PAUSED -> currentPlayer.currentPosition.toLong()
            else -> 0L
        }
        set(value) = when (state) {
            SessionPlayer.State.PLAYING, SessionPlayer.State.PAUSED -> currentPlayer.seekTo(value.toInt())
            else -> Unit
        }

    private var _errorCode: Int = 0
    private var _errorMessage: String = ""
    override val errorCode: Int
        get() = _errorCode
    override val errorMessage: String
        get() = _errorMessage

    private var currentPlayer = MediaPlayer()

    override var playWhenReady: Boolean = false
        set(play) {
            field = play
            if (play) {
                if (state == SessionPlayer.State.STOPPED) {
                    _state = SessionPlayer.State.PREPARING
                    currentPlayer.prepareAsync()
                } else if (state == SessionPlayer.State.PREPARED || state == SessionPlayer.State.PAUSED) {
                    _state = SessionPlayer.State.PLAYING
                    currentPlayer.start()
                }
                when (state) {
                    SessionPlayer.State.STOPPED -> {
                        _state = SessionPlayer.State.PREPARING
                        currentPlayer.prepareAsync()
                    }
                    SessionPlayer.State.ENDED -> {
                        currentPlayer.seekTo(0)
                        _state = SessionPlayer.State.PLAYING
                        currentPlayer.start()
                    }
                    SessionPlayer.State.PREPARED, SessionPlayer.State.PAUSED -> {
                        _state = SessionPlayer.State.PLAYING
                        currentPlayer.start()
                    }
                    else -> Unit
                }
            } else if (state == SessionPlayer.State.PLAYING || state == SessionPlayer.State.BUFFERING) {
                _state = SessionPlayer.State.PAUSED
                currentPlayer.pause()
            }
        }

    private var sessionStateListener: SessionPlayer.PlaybackStateListener? = null

    init {
        setupListeners(currentPlayer)
    }

    private fun setupListeners(player: MediaPlayer) {
        player.setOnPreparedListener {
            Log.d(TAG, "OnPrepared")
             if (playWhenReady) {
                 _state = SessionPlayer.State.PLAYING
                 player.start()
            } else {
                 _state = SessionPlayer.State.PREPARED
            }
        }
        player.setOnInfoListener { mp, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    _state = SessionPlayer.State.BUFFERING
                    true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    _state = if (playWhenReady) {
                        SessionPlayer.State.PLAYING
                    } else {
                        SessionPlayer.State.PAUSED
                    }
                    true
                }
                else -> false
            }
        }
        player.setOnErrorListener { _: MediaPlayer, what: Int, _: Int ->
            Log.d(TAG, "OnError")
            _errorCode = what
            _errorMessage = when (what) {
                MediaPlayer.MEDIA_ERROR_IO -> "MediaPlayer: IO Error"
                MediaPlayer.MEDIA_ERROR_MALFORMED -> "MediaPlayer: Media malformed"
                MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "MediaPlayer: Media unsupported"
                MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "MediaPlayer: Timed out"
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "MediaPlayer: Server died"
                MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> "MediaPlayer: Not valid for progressive playback"
                else -> "MediaPlayer: Unknown error occurred"
            }
            _state = SessionPlayer.State.ERROR
            false
        }
        player.setOnCompletionListener {
            _state = SessionPlayer.State.ENDED
        }
    }

    override fun prepare(uri: Uri) {
        when (state) {
            SessionPlayer.State.INITIALIZED -> prepareOrError(uri)
            SessionPlayer.State.PLAYING,
            SessionPlayer.State.PAUSED,
            SessionPlayer.State.STOPPED,
            SessionPlayer.State.BUFFERING,
            SessionPlayer.State.PREPARING,
            SessionPlayer.State.PREPARED,
            SessionPlayer.State.ENDED,
            SessionPlayer.State.ERROR-> {
                currentPlayer.reset()
                prepareOrError(uri)
            }
        }
    }

    // Call this method only after checking the state of the player
    private fun prepareOrError(uri: Uri) {
        val ch = { e: Exception ->
            e.printStackTrace()
            _errorCode = -1
            _errorMessage = e.message ?: "Unknown"
            _state = SessionPlayer.State.ERROR
        }
        try {
            currentPlayer.setDataSource(context, uri)
            _state = SessionPlayer.State.PREPARING
            currentPlayer.prepareAsync()
        } catch (e: IOException) { ch(e) }
        catch (e: IllegalArgumentException) { ch(e) }
        catch (e: IllegalStateException) { ch(e) }
    }

    override fun stop() {
        when(state) {
            SessionPlayer.State.PREPARED,
            SessionPlayer.State.PLAYING,
            SessionPlayer.State.PAUSED,
            SessionPlayer.State.BUFFERING,
            SessionPlayer.State.ENDED -> {
                _state = SessionPlayer.State.STOPPED
                currentPlayer.stop()
            }
            SessionPlayer.State.PREPARING -> reset()
            SessionPlayer.State.INITIALIZED,
            SessionPlayer.State.STOPPED,
            SessionPlayer.State.ERROR -> Unit
        }
    }

    override fun setPlaybackStateListener(
        listener: SessionPlayer.PlaybackStateListener) {
        sessionStateListener = listener
    }

    override fun reset() {
        _state = SessionPlayer.State.INITIALIZED
        currentPlayer.reset()
    }

    override fun release() {
        currentPlayer.release()
    }
}
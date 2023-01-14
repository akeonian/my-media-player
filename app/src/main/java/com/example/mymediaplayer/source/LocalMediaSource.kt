package com.example.mymediaplayer.source

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Genres
import android.provider.MediaStore.Audio.Media
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.mymediaplayer.R
import com.example.mymediaplayer.utils.LogUtils
import com.example.mymediaplayer.utils.MediaMetadataUtils
import com.example.mymediaplayer.utils.UriUtils
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private const val TAG = "LocalMediaSource"
private const val ROOT_ID = "/"
const val ALL_SONGS_ROOT_ID = "/songs"
const val ALL_ALBUMS_ROOT_ID = "/albums"
const val ALL_GENRES_ROOT_ID = "/genres"
const val ALL_ARTISTS_ROOT_ID = "/artists"

const val MEDIA_ITEM_DURATION = "duration"

class LocalMediaSource(private val context: Context): AbsMediaSource() {

    private val contentResolver = context.contentResolver
    private val playableItemById = ConcurrentHashMap<String, MediaItem>()
    private val browseTree = ConcurrentHashMap<String, MutableList<MediaItem>>()

    override suspend fun load() {
        _state = State.STATE_INITIALIZING

        playableItemById.clear()
        browseTree.clear()
        _state = try {

            val songsRoot: MediaItem = run {
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(ALL_SONGS_ROOT_ID)
                        .setTitle(context.getString(R.string.songs))
                        .build(),
                    MediaItem.FLAG_BROWSABLE
                )
            }
            val albumsRoot: MediaItem = run {
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(ALL_ALBUMS_ROOT_ID)
                        .setTitle(context.getString(R.string.albums))
                        .build(),
                    MediaItem.FLAG_BROWSABLE
                )
            }
            val artistsRoot: MediaItem = run {
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(ALL_ARTISTS_ROOT_ID)
                        .setTitle(context.getString(R.string.artists))
                        .build(),
                    MediaItem.FLAG_BROWSABLE
                )
            }
            val genresRoot = run {
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(ALL_GENRES_ROOT_ID)
                        .setTitle(context.getString(R.string.artists))
                        .build(),
                    MediaItem.FLAG_BROWSABLE
                )
            }
            browseTree[ROOT_ID] = mutableListOf(songsRoot, albumsRoot, artistsRoot, genresRoot)

            // Load sub roots
            loadAllSongs()
            loadAllAlbums()
            loadAllArtists()
            loadAllGenres()

            State.STATE_INITIALIZED
        } catch (e: IOException) {
            State.STATE_ERROR
        }
    }

    private fun loadAllSongs() {
        val selection = "${Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(
                5, TimeUnit.SECONDS).toString()
        )
        val sortOrder = "${Media.TITLE} ASC"

        val allSongs = SongsHelper.getSongs(contentResolver, selection, selectionArgs, sortOrder) { id, item ->
            playableItemById[id] = item
        }
        browseTree[ALL_SONGS_ROOT_ID] = allSongs
    }

    private fun loadAllAlbums() {
        val projection = arrayOf(
            Albums._ID,
            Albums.ALBUM,
            Albums.ARTIST
        )
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Albums.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                Albums.EXTERNAL_CONTENT_URI
            }
        val sortOrder = "${Albums.ALBUM} ASC"

        val query = contentResolver.query(
            audioCollection,
            projection,
            null,
            null,
            sortOrder
        )
        val allAlbums = mutableListOf<MediaItem>()
        query?.apply {
            val idColumn = getColumnIndexOrThrow(Albums._ID)
            val titleColumn = getColumnIndexOrThrow(Albums.ALBUM)
            val subtitleColumn = getColumnIndexOrThrow(Albums.ARTIST)
            while (moveToNext()) {
                val albumId = getString(idColumn)
                val id = "$ALL_ALBUMS_ROOT_ID/$albumId"
                val title = getString(titleColumn)
                val subtitle = getString(subtitleColumn)
                val albumArtUri = Uri.withAppendedPath(
                    Uri.parse("content://media/external/audio/albumart"), albumId)

                val description = MediaDescriptionCompat.Builder()
                    .setMediaId(id)
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setIconUri(albumArtUri)
                    .build()
                val item = MediaItem(description, MediaItem.FLAG_BROWSABLE)
                allAlbums += item
                browseTree[id] = getSongsByAlbum(albumId)
            }
            close()
        }
        LogUtils.d(TAG, "allAlbums=${allAlbums.size}")
        browseTree[ALL_ALBUMS_ROOT_ID] = allAlbums
    }

    private fun loadAllArtists() {
        val projection = arrayOf(
            Artists._ID,
            Artists.ARTIST
        )
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Artists.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                Artists.EXTERNAL_CONTENT_URI
            }
        val sortOrder = "${Artists.ARTIST} ASC"

        val query = contentResolver.query(
            audioCollection,
            projection,
            null,
            null,
            sortOrder
        )
        val allArtists = mutableListOf<MediaItem>()
        query?.apply {
            val idColumn = getColumnIndexOrThrow(Artists._ID)
            val titleColumn = getColumnIndexOrThrow(Artists.ARTIST)
            while (moveToNext()) {
                val artistId = getString(idColumn)
                val id = "$ALL_ARTISTS_ROOT_ID/$artistId"
                val songsByArtist = getSongsByArtist(artistId)
                browseTree[id] = songsByArtist
                val artUri = if (songsByArtist.isNotEmpty()) songsByArtist[0].description.iconUri else null
                val title = getString(titleColumn)
                val songsCount = songsByArtist.size
                val songsCountText = context.resources.getQuantityString(R.plurals.songs_count, songsCount, songsCount)
                val description = MediaDescriptionCompat.Builder()
                    .setMediaId(id)
                    .setTitle(title)
                    .setSubtitle(songsCountText)
                    .setIconUri(artUri)
                    .build()
                val item = MediaItem(description, MediaItem.FLAG_BROWSABLE)
                allArtists += item
            }
            close()
        }
        LogUtils.d(TAG, "allArtists=${allArtists.size}")
        browseTree[ALL_ARTISTS_ROOT_ID] = allArtists
    }

    private fun loadAllGenres() {
        val projection = arrayOf(
            Genres._ID,
            Genres.NAME
        )
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Genres.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                Genres.EXTERNAL_CONTENT_URI
            }
        val sortOrder = "${Genres.NAME} ASC"

        val query = contentResolver.query(
            audioCollection,
            projection,
            null,
            null,
            sortOrder
        )
        val allGenres = mutableListOf<MediaItem>()
        query?.apply {
            val idColumn = getColumnIndexOrThrow(Genres._ID)
            val nameColumn = getColumnIndexOrThrow(Genres.NAME)
            val iconUri = UriUtils.drawableUri(R.drawable.ic_music_note)
            while (moveToNext()) {
                val genreId = getString(idColumn)
                val name = getString(nameColumn)
                // genreId and name can be null so null check them
                if (!genreId.isNullOrEmpty() && !name.isNullOrBlank()) {
                    val browseId = "$ALL_GENRES_ROOT_ID/$genreId"
                    val songsByGenre = getSongsByGenre(genreId)
                    browseTree[browseId] = songsByGenre

                    val songsCount = songsByGenre.size
                    val songsCountText = context.resources.getQuantityString(R.plurals.songs_count, songsCount, songsCount)
                    val description = MediaDescriptionCompat.Builder()
                        .setMediaId(browseId)
                        .setTitle(name)
                        .setSubtitle(songsCountText)
                        .setIconUri(iconUri)
                        .build()
                    val item = MediaItem(description, MediaItem.FLAG_BROWSABLE)
                    allGenres += item
                }
            }
            close()
        }
        LogUtils.d(TAG, "allGenres=${allGenres.size}")
        browseTree[ALL_GENRES_ROOT_ID] = allGenres
    }

    private fun getSongsByGenre(genreId: String): MutableList<MediaItem> {
        return SongsHelper.getSongsFromGenre(contentResolver, genreId, null) {_,_ -> }
    }

    private fun getSongsByAlbum(albumId: String): MutableList<MediaItem> {
        val selection = "${Media.ALBUM_ID} = ?"
        val selectionArgs = arrayOf(albumId)
        val sort = "${Media.DATE_ADDED} ASC"
        return SongsHelper.getSongs(contentResolver, selection, selectionArgs, sort) { _, _ -> }
    }

    private fun getSongsByArtist(artistId: String): MutableList<MediaItem> {
        val selection = "${Media.ARTIST_ID} = ?"
        val selectionArgs = arrayOf(artistId)
        val sort = "${Media.DATE_ADDED} ASC"
        return SongsHelper.getSongs(contentResolver, selection, selectionArgs, sort) { _, _ -> }
    }

    override fun findMediaItemById(mediaId: String): MediaMetadataCompat? {
        return playableItemById[mediaId]?.toMetadata()
    }

    override fun getChildren(browseId: String): MutableList<MediaItem> {
        return browseTree[browseId] ?: throw UnknownBrowseIdException(browseId)
    }

    override fun getRootId(): String {
        return ROOT_ID
    }

    private fun MediaItem.toMetadata(): MediaMetadataCompat {
        val extras = description.extras
        return MediaMetadataUtils.createMetadata(
            mediaId ?: throw IllegalMediaItemException("Cannot have null media Id"),
            description.title?.toString() ?: "",
            description.subtitle?.toString() ?: "",
            description.description?.toString() ?: "",
            description.iconUri?.toString() ?: "",
            description.mediaUri?.toString() ?: "",
            extras?.getLong(MEDIA_ITEM_DURATION) ?: 0
        )
    }

    private object SongsHelper {
        val allSongsUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            Media._ID,
            Media.TITLE,
            Media.ALBUM,
            Media.ARTIST,
            Media.ALBUM_ID,
            Media.DURATION
        )

        private fun getSongs(
            contentResolver: ContentResolver,
            uri: Uri,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortString: String?,
            block: (String, MediaItem) -> Unit
        ): MutableList<MediaItem> {
            val query = contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortString
            )
            val allSongs = mutableListOf<MediaItem>()
            query?.apply {
                val idColumn = getColumnIndexOrThrow(Media._ID)
                val titleColumn = getColumnIndexOrThrow(Media.TITLE)
                val subtitleColumn = getColumnIndexOrThrow(Media.ALBUM)
                val artistColumn = getColumnIndexOrThrow(Media.ARTIST)
                val albumIdColumn = getColumnIndexOrThrow(Media.ALBUM_ID)
                val durationColumn = getColumnIndexOrThrow(Media.DURATION)
                while (moveToNext()) {
                    val id = getString(idColumn)
                    val title = getString(titleColumn)
                    val subtitle = getString(subtitleColumn)
                    val artist = getString(artistColumn)
                    val albumId = getString(albumIdColumn)
                    val contentUri = Uri.withAppendedPath(
                        Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = Uri.withAppendedPath(
                        Uri.parse("content://media/external/audio/albumart"), albumId)
                    val duration = getLong(durationColumn)
                    val extras = Bundle().apply {
                        putLong(MEDIA_ITEM_DURATION, duration)
                    }

                    val description = MediaDescriptionCompat.Builder()
                        .setMediaId(id)
                        .setTitle(title)
                        .setSubtitle(subtitle)
                        .setDescription(artist)
                        .setMediaUri(contentUri)
                        .setIconUri(albumArtUri)
                        .setExtras(extras)
                        .build()
                    val item = MediaItem(
                        description, MediaItem.FLAG_PLAYABLE)
                    allSongs += item
                    block(id, item)
                }
                close()
            }
            return allSongs
        }

        fun getSongs(
            contentResolver: ContentResolver,
            selection: String,
            selectionArgs: Array<out String>,
            sortString: String,
            block: (String, MediaItem) -> Unit
        ): MutableList<MediaItem> {
            return getSongs(contentResolver, allSongsUri, selection, selectionArgs, sortString, block)
        }

        fun getSongsFromGenre(
            contentResolver: ContentResolver,
            genreId: String,
            sortString: String?,
            block: (String, MediaItem) -> Unit
        ): MutableList<MediaItem> {
            val genreUri = Genres.Members.getContentUri(
                "external", genreId.toLong())
            return getSongs(contentResolver, genreUri, null, null, sortString, block)
        }
    }

    class IllegalMediaItemException(message: String): Exception(message)
}
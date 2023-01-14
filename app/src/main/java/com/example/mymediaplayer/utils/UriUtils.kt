package com.example.mymediaplayer.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import com.example.mymediaplayer.BuildConfig

object UriUtils {

    fun drawableUri(@DrawableRes resourceId: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(BuildConfig.APPLICATION_ID)
            .appendPath(resourceId.toString())
            .build()
    }
}
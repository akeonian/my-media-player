package com.example.mymediaplayer.helper

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mymediaplayer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "PermissionHelper"

class PermissionHelper(private val permissions: Array<Permission>) {

    /** If it is first time this will ask for permission
     * for all the given permissions(required or not required).
     * If the permission has been asked for before then it will
     * only ask for those permissions which are not granted
     * and are required.
     * Returns true if the app has all the required permissions
    */
    fun askForPermission(activity: Activity, requestCode: Int, onCanceled: (DialogInterface, Int) -> Unit): Boolean {
        val (hasAllRequired, permissionsToRequest, requestMessage) = checkPermissions(activity)
        // If third i.e. message is not empty then there are
        // permissions not granted to the app
        if (requestMessage.isNotEmpty()) {
            MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.permission_required))
                .setMessage(requestMessage)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, onCanceled)
                .setPositiveButton(android.R.string.ok) { _,_ ->
                    ActivityCompat.requestPermissions(
                        activity, permissionsToRequest, requestCode)
                }
                .show()
        } else if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity, permissionsToRequest, requestCode)
        }
        return hasAllRequired
    }

    /** If it is first time this will check for permission
     * for all the given permissions(required or not required).
     * If the permission has been asked for before then it will
     * only check for those permissions which are not granted
     * and are required.
     * Returns a Triple
     */
    private fun checkPermissions(activity: Activity): Triple<Boolean, Array<String>, String> {
        Log.i(TAG, "checking for permissions: ${permissions.map { it.toString() }}")
        var hasAllRequired = true
        val requestMessage = StringBuilder()
        val permissionsToRequest = mutableListOf<String>()
        for (p in permissions) {
            // If shouldShow is true that means the permission has been asked for
            // before but the app still does not have it,
            if (ContextCompat.checkSelfPermission(activity, p.permission) != PackageManager.PERMISSION_GRANTED) {
                val shouldShow = ActivityCompat
                    .shouldShowRequestPermissionRationale(activity, p.permission)
                if (!shouldShow || p.required) {
                    permissionsToRequest.add(p.permission)
                    if (shouldShow) requestMessage.append(p.requestMessage)
                }
                if (p.required) hasAllRequired = false
            }
        }
        return Triple(hasAllRequired, permissionsToRequest.toTypedArray(), requestMessage.toString())
    }


    data class Permission(val permission: String, val requestMessage: String, val required: Boolean)
}
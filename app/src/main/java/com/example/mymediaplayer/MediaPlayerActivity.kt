package com.example.mymediaplayer

import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mymediaplayer.databinding.ActivityMainBinding
import com.example.mymediaplayer.helper.PermissionHelper
import com.example.mymediaplayer.utils.InjectorUtils
import com.example.mymediaplayer.viewmodels.MediaPlayerViewModel

private const val TAG = "MediaPlayerActivity"
private const val PERMISSION_REQUEST_CODE = 12

class MediaPlayerActivity: AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var permissionHelper: PermissionHelper
    private val viewModel: MediaPlayerViewModel by viewModels {
        InjectorUtils.mediaPlayerViewModelFactory(this@MediaPlayerActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volumeControlStream = AudioManager.STREAM_MUSIC

        val navFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navFragment.navController

        supportActionBar?.setDisplayShowHomeEnabled(true)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            supportActionBar?.setDisplayHomeAsUpEnabled(
                destination.id != controller.graph.startDestinationId
            )
            supportFragmentManager.findFragmentById(R.id.playerContainer)?.apply {
                if (destination.id == R.id.fullPlayerFragment) {
                    supportFragmentManager.beginTransaction()
                        .detach(this).commit()
                } else if(isDetached) {
                    supportFragmentManager.beginTransaction()
                        .attach(this).commit()
                }
            }
        }

        permissionHelper = PermissionHelper(
            arrayOf(PermissionHelper.Permission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                getString(R.string.message_permission_read),
                true
            ))
        )
        askForPermission()
    }

    private fun askForPermission() =
        permissionHelper.askForPermission(this, PERMISSION_REQUEST_CODE) { _,_ -> finish() }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            askForPermission()
            viewModel.sendPermissionChanged()
        }
        else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}
package com.example.ui.tile

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.MainActivity
import com.example.R
import com.example.engine.DictationService

class KothaTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val isRunning = DictationService.isRunning()
        if (isRunning) {
            val stopIntent = Intent(this, DictationService::class.java).apply {
                action = DictationService.ACTION_HIDE_ORB
            }
            startService(stopIntent)
        } else {
            // Android 14+ requires starting microphone foreground service from foreground app
            val appIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // PendingIntent on Android 14+
                startActivityAndCollapse(appIntent)
            } else {
                startActivityAndCollapse(appIntent)
            }
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = DictationService.isRunning()
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        val iconRes = if (isRunning) R.drawable.ic_qs_kotha_on else R.drawable.ic_qs_kotha_off
        tile.icon = Icon.createWithResource(this, iconRes)
        tile.updateTile()
    }
}

package com.dolbaeb1488company.fpsunlocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.dolbaeb1488company.fpsunlocker.model.InstalledAppItem
import com.dolbaeb1488company.fpsunlocker.model.OriginSettingsConstants
import com.dolbaeb1488company.fpsunlocker.theme.OriginTweaksTheme
import com.dolbaeb1488company.fpsunlocker.ui.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var is144Enabled by mutableStateOf(false)
    private var currentRawValue by mutableStateOf("")
    private var hasWritePermission by mutableStateOf(true)
    private var displayRefreshRate by mutableFloatStateOf(120f)
    private var supportedRefreshRates = mutableStateListOf<Float>()
    private var isServiceRunning by mutableStateOf(false)
    private var isFirstRun by mutableStateOf(false)

    private val musicAppsList = mutableStateListOf<String>()
    private val fingerprintIconsList = mutableStateListOf<String>()
    private val installedAppsList = mutableStateListOf<InstalledAppItem>()

    private val uiUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val isChecked = intent.getBooleanExtra("isChecked", false)
            is144Enabled = isChecked
            readSystemSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        isFirstRun = prefs.getBoolean("isFirstRun", true)

        detectDisplayCapabilities()
        readSystemSettings()
        loadInstalledApps()

        // Start service if requested or enabled
        try {
            startService(Intent(this, FpsService::class.java))
            isServiceRunning = true
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "Failed to start FpsService", e)
        }

        val filter = IntentFilter(OriginSettingsConstants.ACTION_UPDATE_UI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(uiUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(uiUpdateReceiver, filter)
        }

        setContent {
            OriginTweaksTheme {
                MainScreen(
                    is144Enabled = is144Enabled,
                    currentRawValue = currentRawValue,
                    hasWritePermission = hasWritePermission,
                    displayRefreshRate = displayRefreshRate,
                    supportedRefreshRates = supportedRefreshRates.toList(),
                    isServiceRunning = isServiceRunning,
                    musicApps = musicAppsList.toList(),
                    fingerprintIcons = fingerprintIconsList.toList(),
                    installedApps = installedAppsList.toList(),
                    isFirstRun = isFirstRun,
                    onDismissFirstRun = {
                        isFirstRun = false
                        prefs.edit { putBoolean("isFirstRun", false) }
                    },
                    onToggleFps = { enable -> toggleFpsSetting(enable) },
                    onSetCustomValue = { value -> applyCustomFpsSetting(value) },
                    onToggleService = { enable -> toggleBackgroundService(enable) },
                    onRefreshPermission = { checkPermissions() },
                    onSaveMusicApps = { list -> saveMusicApps(list) },
                    onSaveFingerprintIcons = { list -> saveFingerprintIcons(list) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        readSystemSettings()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(uiUpdateReceiver)
        } catch (e: Exception) {
            Log.w("FPSUnlocker", "Receiver not registered or already unregistered", e)
        }
        super.onDestroy()
    }

    private fun checkPermissions() {
        hasWritePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(this)
        } else {
            true
        }
    }

    private fun detectDisplayCapabilities() {
        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                wm.defaultDisplay
            }

            if (display != null) {
                displayRefreshRate = display.refreshRate
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val modes = display.supportedModes
                    val rates = modes.map { it.refreshRate }.distinct().sorted()
                    supportedRefreshRates.clear()
                    supportedRefreshRates.addAll(rates)
                }
            }
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "Error detecting display", e)
        }
    }

    private fun readSystemSettings() {
        checkPermissions()
        try {
            val fpsVal = Settings.System.getString(contentResolver, OriginSettingsConstants.SETTING_FPS_INTERPOLATION) ?: ""
            currentRawValue = fpsVal
            is144Enabled = (fpsVal == OriginSettingsConstants.VALUE_144_FORCE)

            // Read Music Apps
            val musicRaw = Settings.System.getString(contentResolver, OriginSettingsConstants.SETTING_MUSIC_WIDGET) ?: ""
            musicAppsList.clear()
            if (musicRaw.isNotBlank()) {
                val cleaned = musicRaw.removePrefix("[").removeSuffix("]")
                if (cleaned.isNotEmpty()) {
                    val parts = cleaned.split(",").map { it.trim().removeSurrounding("\"") }
                    musicAppsList.addAll(parts.filter { it.isNotEmpty() })
                }
            }

            // Read FP Icons
            val fpRaw = Settings.System.getString(contentResolver, OriginSettingsConstants.SETTING_FP_ICON) ?: ""
            fingerprintIconsList.clear()
            if (fpRaw.isNotBlank()) {
                val parts = fpRaw.split(OriginSettingsConstants.SPLIT_FP).filter { it.isNotEmpty() }
                fingerprintIconsList.addAll(parts)
            }
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "Error reading settings: ${e.message}")
        }
    }

    private fun toggleFpsSetting(enable: Boolean) {
        val targetValue = if (enable) OriginSettingsConstants.VALUE_144_FORCE else OriginSettingsConstants.VALUE_STOCK
        applyCustomFpsSetting(targetValue)
    }

    private fun applyCustomFpsSetting(targetValue: String) {
        try {
            Settings.System.putString(contentResolver, OriginSettingsConstants.SETTING_FPS_INTERPOLATION, targetValue)
            currentRawValue = targetValue
            is144Enabled = (targetValue == OriginSettingsConstants.VALUE_144_FORCE)
            Toast.makeText(this, "OriginOS Setting Applied: $targetValue", Toast.LENGTH_SHORT).show()

            // Update service notification
            try {
                startService(Intent(this, FpsService::class.java))
            } catch (e: Exception) {
                Log.w("FPSUnlocker", "Service refresh warning", e)
            }
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "Failed to write setting: ${e.message}", e)
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun saveMusicApps(list: List<String>) {
        musicAppsList.clear()
        musicAppsList.addAll(list)
        val newValue = if (list.isEmpty()) "[]" else list.joinToString(separator = "\",\"", prefix = "[\"", postfix = "\"]")
        try {
            Settings.System.putString(contentResolver, OriginSettingsConstants.SETTING_MUSIC_WIDGET, newValue)
            Toast.makeText(this, "Music Apps List Saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "Music Save err: ${e.message}")
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun saveFingerprintIcons(list: List<String>) {
        fingerprintIconsList.clear()
        fingerprintIconsList.addAll(list)
        val newValue = if (list.isEmpty()) "" else list.joinToString(OriginSettingsConstants.SPLIT_FP) + OriginSettingsConstants.SPLIT_FP
        try {
            Settings.System.putString(contentResolver, OriginSettingsConstants.SETTING_FP_ICON, newValue)
            Toast.makeText(this, "Fingerprint Icons Saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "FP Save err: ${e.message}")
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleBackgroundService(enable: Boolean) {
        isServiceRunning = enable
        if (enable) {
            try {
                startService(Intent(this, FpsService::class.java))
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("FPSUnlocker", "Could not start service", e)
            }
        } else {
            try {
                stopService(Intent(this, FpsService::class.java))
                Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("FPSUnlocker", "Could not stop service", e)
            }
        }
    }

    private fun loadInstalledApps() {
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val mapped = apps
                .map { info ->
                    val label = info.loadLabel(pm).toString()
                    val icon = try { info.loadIcon(pm) } catch (e: Exception) { null }
                    val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    InstalledAppItem(
                        packageName = info.packageName,
                        label = label,
                        icon = icon,
                        isSystem = isSystem,
                        isSelected = musicAppsList.contains(info.packageName)
                    )
                }
                .sortedBy { it.label.lowercase() }

            withContext(Dispatchers.Main) {
                installedAppsList.clear()
                installedAppsList.addAll(mapped)
            }
        }
    }
}

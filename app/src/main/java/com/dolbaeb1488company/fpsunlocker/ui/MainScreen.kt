package com.dolbaeb1488company.fpsunlocker.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dolbaeb1488company.fpsunlocker.R
import com.dolbaeb1488company.fpsunlocker.model.InstalledAppItem
import com.dolbaeb1488company.fpsunlocker.theme.AccentCyan
import com.dolbaeb1488company.fpsunlocker.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    is144Enabled: Boolean,
    currentRawValue: String,
    hasWritePermission: Boolean,
    displayRefreshRate: Float,
    supportedRefreshRates: List<Float>,
    isServiceRunning: Boolean,
    isShizukuAvailable: Boolean,
    hasShizukuPermission: Boolean,
    musicApps: List<String>,
    fingerprintIcons: List<String>,
    installedApps: List<InstalledAppItem>,
    isFirstRun: Boolean,
    onDismissFirstRun: () -> Unit,
    onToggleFps: (Boolean) -> Unit,
    onSetCustomValue: (String) -> Unit,
    onToggleService: (Boolean) -> Unit,
    onRefreshPermission: () -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onShizukuGrantedSuccess: () -> Unit,
    onSaveMusicApps: (List<String>) -> Unit,
    onSaveFingerprintIcons: (List<String>) -> Unit
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = if (is144Enabled) AccentGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (is144Enabled) AccentGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (is144Enabled) "144 Hz" else "${displayRefreshRate.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (is144Enabled) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedNavIndex == 0,
                    onClick = { selectedNavIndex = 0 },
                    icon = { Icon(Icons.Rounded.Speed, contentDescription = stringResource(R.string.nav_unlocker)) },
                    label = { Text(stringResource(R.string.nav_unlocker)) },
                    modifier = Modifier.testTag("nav_unlocker_tab")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = { Icon(Icons.Rounded.Tune, contentDescription = stringResource(R.string.nav_tweaks)) },
                    label = { Text(stringResource(R.string.nav_tweaks)) },
                    modifier = Modifier.testTag("nav_tweaks_tab")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 2,
                    onClick = { selectedNavIndex = 2 },
                    icon = { Icon(Icons.Rounded.Terminal, contentDescription = stringResource(R.string.nav_tools)) },
                    label = { Text(stringResource(R.string.nav_tools)) },
                    modifier = Modifier.testTag("nav_tools_tab")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 3,
                    onClick = { selectedNavIndex = 3 },
                    icon = { Icon(Icons.Rounded.Info, contentDescription = stringResource(R.string.nav_device)) },
                    label = { Text(stringResource(R.string.nav_device)) },
                    modifier = Modifier.testTag("nav_device_tab")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedNavIndex,
                transitionSpec = {
                    val duration = 300
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(duration)) { width -> width / 3 } + fadeIn(animationSpec = tween(duration)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { width -> -width / 3 } + fadeOut(animationSpec = tween(duration)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(duration)) { width -> -width / 3 } + fadeIn(animationSpec = tween(duration)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { width -> width / 3 } + fadeOut(animationSpec = tween(duration)))
                    }
                },
                label = "navigation_content"
            ) { target ->
                when (target) {
                    0 -> FpsUnlockerScreen(
                        is144Enabled = is144Enabled,
                        currentRawValue = currentRawValue,
                        hasWritePermission = hasWritePermission,
                        displayRefreshRate = displayRefreshRate,
                        isServiceRunning = isServiceRunning,
                        isShizukuAvailable = isShizukuAvailable,
                        hasShizukuPermission = hasShizukuPermission,
                        onRequestShizukuPermission = onRequestShizukuPermission,
                        onToggleFps = onToggleFps,
                        onSetCustomValue = onSetCustomValue,
                        onToggleService = onToggleService,
                        onRefreshPermission = onRefreshPermission
                    )
                    1 -> OriginTweaksScreen(
                        musicApps = musicApps,
                        fingerprintIcons = fingerprintIcons,
                        installedApps = installedApps,
                        onSaveMusicApps = onSaveMusicApps,
                        onSaveFingerprintIcons = onSaveFingerprintIcons
                    )
                    2 -> AdbToolsScreen(
                        isShizukuAvailable = isShizukuAvailable,
                        hasShizukuPermission = hasShizukuPermission,
                        onRequestShizukuPermission = onRequestShizukuPermission,
                        onShizukuGrantedSuccess = onShizukuGrantedSuccess
                    )
                    3 -> DeviceInfoScreen(
                        displayRefreshRate = displayRefreshRate,
                        supportedRefreshRates = supportedRefreshRates
                    )
                }
            }
        }
    }

    if (isFirstRun) {
        AlertDialog(
            onDismissRequest = onDismissFirstRun,
            title = { Text(stringResource(R.string.setup_guide_title)) },
            text = { Text(stringResource(R.string.setup_guide_msg)) },
            confirmButton = {
                Button(onClick = onDismissFirstRun) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

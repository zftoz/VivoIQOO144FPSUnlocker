package com.dolbaeb1488company.fpsunlocker.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.dolbaeb1488company.fpsunlocker.R
import com.dolbaeb1488company.fpsunlocker.model.FingerprintIconPreset
import com.dolbaeb1488company.fpsunlocker.model.InstalledAppItem
import com.dolbaeb1488company.fpsunlocker.model.OriginSettingsConstants
import com.dolbaeb1488company.fpsunlocker.theme.AccentCyan
import com.dolbaeb1488company.fpsunlocker.theme.AccentGreen
import com.dolbaeb1488company.fpsunlocker.theme.AccentOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginTweaksScreen(
    musicApps: List<String>,
    fingerprintIcons: List<String>,
    installedApps: List<InstalledAppItem>,
    onSaveMusicApps: (List<String>) -> Unit,
    onSaveFingerprintIcons: (List<String>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    var showAppPickerSheet by remember { mutableStateOf(false) }
    var showEditFpDialog by remember { mutableStateOf(false) }
    var editingFpIndex by remember { mutableIntStateOf(-1) }
    var editingFpValue by remember { mutableStateOf("") }
    var showPresetDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = Color.Transparent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LibraryMusic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.music_widget_apps))
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.fingerprint_icons))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> MusicWidgetTabContent(
                musicApps = musicApps,
                installedApps = installedApps,
                onAddClicked = { showAppPickerSheet = true },
                onRemove = { pkg ->
                    val updated = musicApps.filter { it != pkg }
                    onSaveMusicApps(updated)
                }
            )
            1 -> FingerprintTabContent(
                fingerprintIcons = fingerprintIcons,
                onAddClicked = {
                    editingFpIndex = -1
                    editingFpValue = ""
                    showEditFpDialog = true
                },
                onEditClicked = { index, value ->
                    editingFpIndex = index
                    editingFpValue = value
                    showEditFpDialog = true
                },
                onDeleteClicked = { index ->
                    val updated = fingerprintIcons.toMutableList()
                    updated.removeAt(index)
                    onSaveFingerprintIcons(updated)
                },
                onLoadPresetsClicked = {
                    showPresetDialog = true
                }
            )
        }
    }

    // App Picker Sheet
    if (showAppPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppPickerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AppPickerSheetContent(
                installedApps = installedApps,
                currentSelected = musicApps,
                onSave = { selected ->
                    onSaveMusicApps(selected)
                    showAppPickerSheet = false
                },
                onDismiss = { showAppPickerSheet = false }
            )
        }
    }

    // Fingerprint Edit Dialog
    if (showEditFpDialog) {
        AlertDialog(
            onDismissRequest = { showEditFpDialog = false },
            title = {
                Text(if (editingFpIndex == -1) stringResource(R.string.add_icon) else stringResource(R.string.edit_icon))
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.icon_identifier),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingFpValue,
                        onValueChange = { editingFpValue = it },
                        placeholder = { Text("e.g. fp_cyber_glow_v2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingFpValue.isNotBlank()) {
                            val updated = fingerprintIcons.toMutableList()
                            if (editingFpIndex == -1) {
                                updated.add(editingFpValue.trim())
                            } else {
                                updated[editingFpIndex] = editingFpValue.trim()
                            }
                            onSaveFingerprintIcons(updated)
                        }
                        showEditFpDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditFpDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Presets Dialog
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text(stringResource(R.string.preset_fp_icons)) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(OriginSettingsConstants.DEFAULT_FP_PRESETS) { preset ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!fingerprintIcons.contains(preset.iconKey)) {
                                        val updated = fingerprintIcons + preset.iconKey
                                        onSaveFingerprintIcons(updated)
                                        Toast.makeText(context, "Added ${preset.title}", Toast.LENGTH_SHORT).show()
                                    }
                                    showPresetDialog = false
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = AccentCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(preset.title, fontWeight = FontWeight.Bold)
                                    Text(preset.iconKey, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun MusicWidgetTabContent(
    musicApps: List<String>,
    installedApps: List<InstalledAppItem>,
    onAddClicked: () -> Unit,
    onRemove: (String) -> Unit
) {
    val context = LocalContext.current
    val installedMap = remember(installedApps) { installedApps.associateBy { it.packageName } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LibraryMusic, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.music_widget_apps),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.music_widget_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (musicApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.no_music_apps),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                items(musicApps) { pkg ->
                    val appItem = installedMap[pkg]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (appItem?.icon != null) {
                                Image(
                                    bitmap = appItem.icon.toBitmap(64, 64).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appItem?.label ?: pkg,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = pkg,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(onClick = { onRemove(pkg) }) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddClicked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_music_app_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_apps))
        }
    }
}

@Composable
fun FingerprintTabContent(
    fingerprintIcons: List<String>,
    onAddClicked: () -> Unit,
    onEditClicked: (Int, String) -> Unit,
    onDeleteClicked: (Int) -> Unit,
    onLoadPresetsClicked: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = AccentGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.fingerprint_icons),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(onClick = onLoadPresetsClicked) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.preset_fp_icons), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Text(
                            text = stringResource(R.string.fingerprint_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (fingerprintIcons.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.no_fp_icons),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onLoadPresetsClicked) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.preset_fp_icons))
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(fingerprintIcons) { index, iconKey ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Fingerprint,
                                    contentDescription = null,
                                    tint = AccentGreen
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Icon #${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = iconKey,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("FP Icon Key", iconKey))
                                Toast.makeText(context, "Copied $iconKey", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(20.dp))
                            }

                            IconButton(onClick = { onEditClicked(index, iconKey) }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                            }

                            IconButton(onClick = { onDeleteClicked(index) }) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddClicked,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_fp_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_icon))
        }
    }
}

@Composable
fun AppPickerSheetContent(
    installedApps: List<InstalledAppItem>,
    currentSelected: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedPackages = remember { mutableStateListOf<String>().apply { addAll(currentSelected) } }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_music_apps),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search_apps)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .height(400.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (filteredApps.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_apps_found),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredApps) { app ->
                    val isChecked = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isChecked) selectedPackages.remove(app.packageName)
                                else selectedPackages.add(app.packageName)
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (app.icon != null) {
                            Image(
                                bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.MusicNote, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) selectedPackages.add(app.packageName)
                                else selectedPackages.remove(app.packageName)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSave(selectedPackages.toList()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("${stringResource(R.string.add_selected)} (${selectedPackages.size})")
        }
    }
}

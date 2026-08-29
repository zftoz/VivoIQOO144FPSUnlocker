package com.dolbaeb1488company.fpsunlocker.model

import android.graphics.drawable.Drawable

data class InstalledAppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isSystem: Boolean,
    val isSelected: Boolean = false
)

data class FpsPreset(
    val titleResId: Int,
    val value: String,
    val is144: Boolean,
    val description: String
)

data class FingerprintIconPreset(
    val title: String,
    val iconKey: String,
    val previewStyle: String
)

object OriginSettingsConstants {
    const val SETTING_FPS_INTERPOLATION = "gamecube_frame_interpolation_for_sr"
    const val SETTING_FP_ICON = "light_cover_customize_fp_icon"
    const val SETTING_MUSIC_WIDGET = "musicwidget_list_pkg_type_key"
    const val SPLIT_FP = "#split#"

    const val VALUE_144_FORCE = "1:1::72:144"
    const val VALUE_120_MEMC = "1:1::60:120"
    const val VALUE_STOCK = "0:-1:0:0:0"
    const val VALUE_AGGRESSIVE_144 = "1:1::48:144"

    const val ACTION_UPDATE_UI = "com.dolbaeb1488company.fpsunlocker.UPDATE_UI"
    const val ACTION_TOGGLE_FPS = "com.dolbaeb1488company.fpsunlocker.TOGGLE_FPS"

    val DEFAULT_FP_PRESETS = listOf(
        FingerprintIconPreset("Cyber Neon Core", "fp_cyber_glow_v2", "Neon Cyan"),
        FingerprintIconPreset("Quantum Pulse", "fp_quantum_pulse_orange", "Electric Orange"),
        FingerprintIconPreset("Vivo Ultra Ripple", "fp_vivo_origin_ripple", "OriginOS Blue"),
        FingerprintIconPreset("Dark Stealth Minimal", "fp_stealth_minimal_ring", "Minimal Grey"),
        FingerprintIconPreset("Apex Matrix", "fp_matrix_digital_emerald", "Emerald Green")
    )
}

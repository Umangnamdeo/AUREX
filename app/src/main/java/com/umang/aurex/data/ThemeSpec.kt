package com.umang.aurex.data

import org.json.JSONObject

private fun c(v: Long): Int = v.toInt()

data class ThemeSpec(
    val id: String,
    val name: String,
    val tagline: String,
    val style: IslandStyle,
    val bg: Int,
    val fill: Int,
    val icon: Int,
    val accent: Int,
    val alpha: Float = 1f,
    val builtIn: Boolean = true,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("name", name); put("tagline", tagline); put("style", style.name)
        put("bg", bg); put("fill", fill); put("icon", icon); put("accent", accent)
        put("alpha", alpha.toDouble()); put("builtIn", builtIn)
    }

    companion object {
        fun fromJson(j: JSONObject): ThemeSpec = ThemeSpec(
            id = j.optString("id", "custom_" + System.currentTimeMillis()),
            name = j.optString("name", "Custom"),
            tagline = j.optString("tagline", "Your design"),
            style = runCatching { IslandStyle.valueOf(j.optString("style")) }.getOrDefault(IslandStyle.Classic),
            bg = j.getInt("bg"), fill = j.getInt("fill"), icon = j.getInt("icon"), accent = j.getInt("accent"),
            alpha = j.optDouble("alpha", 1.0).toFloat(),
            builtIn = j.optBoolean("builtIn", false),
        )

        val builtIns: List<ThemeSpec> = listOf(
            ThemeSpec("midnight", "Midnight", "Elegant & deep", IslandStyle.Aurex, c(0xFF0B1020), c(0xFFB9C4FF), c(0xFF7F8CC9), c(0xFF6C7BFF)),
            ThemeSpec("obsidian", "Obsidian", "Dark volcanic glass", IslandStyle.Classic, c(0xFF0D0D0F), c(0xFFE6E6EA), c(0xFF77777F), c(0xFFB8B8C0)),
            ThemeSpec("pure_black", "Pure Black", "The AUREX classic", IslandStyle.Classic, c(0xFF000000), c(0xFFFFFFFF), c(0xFF8E8E93), c(0xFFFFFFFF)),
            ThemeSpec("frosted_glass", "Frosted Glass", "Translucent calm", IslandStyle.Glass, c(0xFFFFFFFF), c(0xFFFFFFFF), c(0xFFFFFFFF), c(0xFFA9C7FF), 0.92f),
            ThemeSpec("aurora", "Aurora", "Northern glow", IslandStyle.Classic, c(0xFF06201F), c(0xFF5CF2C8), c(0xFF2DA68A), c(0xFF5CF2C8)),
            ThemeSpec("material_you", "Material You", "Dynamic colors", IslandStyle.Classic, c(0xFF2B2930), c(0xFFD0BCFF), c(0xFFCAC4D0), c(0xFFD0BCFF)),
            ThemeSpec("monochrome", "Monochrome", "Quiet grayscale", IslandStyle.Minimal, c(0xFF1C1C1C), c(0xFFD0D0D0), c(0xFF808080), c(0xFFD0D0D0)),
            ThemeSpec("silver_chrome", "Silver Chrome", "Premium metallic", IslandStyle.Aurex, c(0xFF1A1C20), c(0xFFDDE1E8), c(0xFF9AA0AA), c(0xFFC3CAD6)),
        )
    }
}

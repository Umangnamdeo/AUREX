package com.umang.aurex.data

import org.json.JSONObject

enum class IslandStyle { Classic, Glass, Aurex, Minimal }
enum class Side { Left, Right }
enum class HapticLevel { Off, Light, Medium, Strong }
enum class GestureAction(val label: String) {
    None("Nothing"), Mute("Mute / unmute"), Mixer("Open mixer"), Max("Maximum volume"), Min("Minimum volume")
}

private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, def: T): T =
    runCatching { enumValueOf<T>(optString(key)) }.getOrDefault(def)

private fun JSONObject.optF(key: String, def: Float): Float = optDouble(key, def.toDouble()).toFloat()

data class AurexSettings(
    val serviceEnabled: Boolean = true,
    val islandEnabled: Boolean = true,
    val popsEnabled: Boolean = false,
    val style: IslandStyle = IslandStyle.Classic,
    val side: Side = Side.Right,
    val widthDp: Float = 52f,
    val heightDp: Float = 230f,
    val verticalOffsetDp: Float = 0f,
    val cornerDp: Float = 26f,
    val bg: Int = 0xFF000000.toInt(),
    val fill: Int = 0xFFFFFFFF.toInt(),
    val icon: Int = 0xFF8E8E93.toInt(),
    val accent: Int = 0xFF8FA6FF.toInt(),
    val alpha: Float = 1f,
    val animMs: Int = 300,
    val durationMs: Int = 2200,
    val themeId: String = "pure_black",
    val haptic: HapticLevel = HapticLevel.Medium,
    val landscapeIsland: Boolean = true,
    val landscapePops: Boolean = true,
    val landscapeSide: Side = Side.Right,
    val reducedMotion: Boolean = false,
    val autoUpdateCheck: Boolean = true,
    val popUpColor: Int = 0xFFFFFFFF.toInt(),
    val popDownColor: Int = 0xFF8FA6FF.toInt(),
    val popWidthDp: Float = 6f,
    val popHeightDp: Float = 96f,
    val popCornerDp: Float = 3f,
    val tapAction: GestureAction = GestureAction.Mute,
    val doubleTapAction: GestureAction = GestureAction.Max,
    val longPressAction: GestureAction = GestureAction.Mixer,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("serviceEnabled", serviceEnabled); put("islandEnabled", islandEnabled); put("popsEnabled", popsEnabled)
        put("style", style.name); put("side", side.name)
        put("widthDp", widthDp.toDouble()); put("heightDp", heightDp.toDouble())
        put("verticalOffsetDp", verticalOffsetDp.toDouble()); put("cornerDp", cornerDp.toDouble())
        put("bg", bg); put("fill", fill); put("icon", icon); put("accent", accent)
        put("alpha", alpha.toDouble()); put("animMs", animMs); put("durationMs", durationMs)
        put("themeId", themeId); put("haptic", haptic.name)
        put("landscapeIsland", landscapeIsland); put("landscapePops", landscapePops)
        put("landscapeSide", landscapeSide.name); put("reducedMotion", reducedMotion)
        put("autoUpdateCheck", autoUpdateCheck)
        put("popUpColor", popUpColor); put("popDownColor", popDownColor)
        put("popWidthDp", popWidthDp.toDouble()); put("popHeightDp", popHeightDp.toDouble())
        put("popCornerDp", popCornerDp.toDouble())
        put("tapAction", tapAction.name); put("doubleTapAction", doubleTapAction.name)
        put("longPressAction", longPressAction.name)
    }

    companion object {
        fun fromJson(j: JSONObject): AurexSettings {
            val d = AurexSettings()
            return AurexSettings(
                serviceEnabled = j.optBoolean("serviceEnabled", d.serviceEnabled),
                islandEnabled = j.optBoolean("islandEnabled", d.islandEnabled),
                popsEnabled = j.optBoolean("popsEnabled", d.popsEnabled),
                style = j.optEnum("style", d.style),
                side = j.optEnum("side", d.side),
                widthDp = j.optF("widthDp", d.widthDp),
                heightDp = j.optF("heightDp", d.heightDp),
                verticalOffsetDp = j.optF("verticalOffsetDp", d.verticalOffsetDp),
                cornerDp = j.optF("cornerDp", d.cornerDp),
                bg = j.optInt("bg", d.bg), fill = j.optInt("fill", d.fill),
                icon = j.optInt("icon", d.icon), accent = j.optInt("accent", d.accent),
                alpha = j.optF("alpha", d.alpha),
                animMs = j.optInt("animMs", d.animMs), durationMs = j.optInt("durationMs", d.durationMs),
                themeId = j.optString("themeId", d.themeId),
                haptic = j.optEnum("haptic", d.haptic),
                landscapeIsland = j.optBoolean("landscapeIsland", d.landscapeIsland),
                landscapePops = j.optBoolean("landscapePops", d.landscapePops),
                landscapeSide = j.optEnum("landscapeSide", d.landscapeSide),
                reducedMotion = j.optBoolean("reducedMotion", d.reducedMotion),
                autoUpdateCheck = j.optBoolean("autoUpdateCheck", d.autoUpdateCheck),
                popUpColor = j.optInt("popUpColor", d.popUpColor),
                popDownColor = j.optInt("popDownColor", d.popDownColor),
                popWidthDp = j.optF("popWidthDp", d.popWidthDp),
                popHeightDp = j.optF("popHeightDp", d.popHeightDp),
                popCornerDp = j.optF("popCornerDp", d.popCornerDp),
                tapAction = j.optEnum("tapAction", d.tapAction),
                doubleTapAction = j.optEnum("doubleTapAction", d.doubleTapAction),
                longPressAction = j.optEnum("longPressAction", d.longPressAction),
            )
        }

        fun fromJson(s: String): AurexSettings =
            runCatching { fromJson(JSONObject(s)) }.getOrDefault(AurexSettings())
    }
}

package com.manus.orbitlauncher.data

import android.graphics.drawable.Drawable
import com.manus.orbitlauncher.R

/** The content source displayed around the central surface. */
enum class RingMode(val title: String, val subtitle: String) {
    FAVORITES("Favourites", "Your pinned orbit"),
    MOST_USED("Most used", "Apps you open most"),
    RECENT("Recent", "Apps from your day"),
    ALL_APPS("All apps", "A–Z app library")
}

/** One independently configurable Orbit home page. */
data class LauncherPage(
    val id: String,
    val name: String,
    val source: RingMode
)

/** The content displayed within Orbit's large central circle. */
enum class CenterMode(val title: String, val subtitle: String) {
    CLOCK("Clock", "Time at the centre"),
    CLOCK_AND_DATE("Clock + date", "A more detailed time surface"),
    APP_WIDGET("App widget", "A widget selected from your device")
}

/** Geometry applied to the central circular surface. */
enum class CenterSize(val title: String, val scale: Float) {
    SMALL("Small", 0.82f),
    BALANCED("Balanced", 1f),
    LARGE("Large", 1.16f)
}

/** The activation rule for app targets; the centre long press remains reserved. */
enum class AppTrigger(val title: String, val detail: String) {
    TAP("Tap", "Open an app with one touch"),
    DOUBLE_TAP("Double tap", "Require a deliberate second touch")
}

/** The density of the upper app halo. */
enum class IconScale(val title: String, val podSizeDp: Int) {
    COMPACT("Compact", 46),
    COMFORTABLE("Comfortable", 56),
    EXPRESSIVE("Expressive", 66)
}

/** Colour mode selected independently from the device setting. */
enum class AppearanceMode(val title: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

/** Built-in wallpaper tone used behind the launcher content. */
enum class AmbientBackdrop(val title: String) {
    ORBIT("Orbit"),
    AURORA("Aurora"),
    DUSK("Dusk"),
    TIDAL("Tidal"),
    CLAY("Clay")
}

/** Gesture types available on the central surface. */
enum class CenterGesture(val title: String) {
    TAP("Tap"),
    DOUBLE_TAP("Double tap"),
    TRIPLE_TAP("Triple tap"),
    LONG_PRESS("Long press")
}

/** Actions that can be assigned to the central gesture mapper. */
enum class CenterAction(val title: String) {
    NONE("None"),
    VOICE("Voice launch"),
    SEARCH("App search"),
    SETTINGS("Open settings"),
    ALL_APPS("All apps"),
    NEXT_WALLPAPER("Next wallpaper")
}

/** Number of one-per-position targets on each complete orbit page. */
enum class OrbitCapacity(val title: String, val slots: Int) {
    CALM("8", 8),
    BALANCED("12", 12),
    DENSE("16", 16)
}

/** Visual pace used for full-orbit page rotation. */
enum class RotationSpeed(val title: String, val durationMs: Int) {
    GENTLE("Gentle", 760),
    BALANCED("Balanced", 440),
    QUICK("Quick", 230)
}

/** AI Provider for the BYOK engine. */
enum class AiProvider(val title: String, val endpoint: String?) {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1"),
    GOOGLE("Google Gemini", null),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1"),
    CUSTOM("Custom Endpoint", null)
}

/** Clock styles for the central orbit. */
enum class ClockStyle(val title: String) {
    EXPRESSIVE("Expressive Digital"),
    MODERN_ANALOG("Modern Analog"),
    INFO_CENTRIC("Info-Centric"),
    MINIMALIST("Minimalist")
}

/** Categories for the expanded wallpaper gallery. */
enum class WallpaperCategory(val title: String) {
    LANDSCAPES("Landscapes"),
    DOGS("Dogs"),
    ANIMALS("Animals"),
    FOODS("Foods"),
    FLOWERS("Flowers"),
    OBJECTS("Objects")
}

/** Curated portrait scenes bundled as launcher-only wallpapers. */
enum class BuiltinWallpaper(val title: String, val category: WallpaperCategory, val resourceId: Int) {
    LANDSCAPE_01("Mountain Reflection", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_01),
    LANDSCAPE_02("Lakeside Silence", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_02),
    LANDSCAPE_03("Emerald Forest", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_03),
    LANDSCAPE_04("Blue Peak", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_04),
    LANDSCAPE_05("Canyon Falls", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_05),
    LANDSCAPE_06("Forest Cascade", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_06),
    LANDSCAPE_07("Alpine Falls", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_07),
    LANDSCAPE_08("Autumn Falls", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_08),
    LANDSCAPE_09("Starlit Cliffs", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_09),
    LANDSCAPE_10("Highland Lake", WallpaperCategory.LANDSCAPES, R.drawable.wallpaper_landscape_10),

    DOG_01("Golden Retriever", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_01),
    DOG_02("Labrador", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_02),
    DOG_03("Husky", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_03),
    DOG_04("Poodle", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_04),
    DOG_05("Puppy", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_05),
    DOG_06("Nature Dog", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_06),
    DOG_07("Shepherd", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_07),
    DOG_08("Small Dog", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_08),
    DOG_09("Retriever Portrait", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_09),
    DOG_10("Companion", WallpaperCategory.DOGS, R.drawable.wallpaper_dog_10),

    ANIMAL_01("Owl", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_01),
    ANIMAL_02("Tiger", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_02),
    ANIMAL_03("Lion", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_03),
    ANIMAL_04("Deer", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_04),
    ANIMAL_05("Fox", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_05),
    ANIMAL_06("Squirrel", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_06),
    ANIMAL_07("Eagle", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_07),
    ANIMAL_08("Wildlife", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_08),
    ANIMAL_09("Forest Life", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_09),
    ANIMAL_10("Mountain King", WallpaperCategory.ANIMALS, R.drawable.wallpaper_animal_10),

    FOOD_01("Pastry", WallpaperCategory.FOODS, R.drawable.wallpaper_food_01),
    FOOD_02("Pizza", WallpaperCategory.FOODS, R.drawable.wallpaper_food_02),
    FOOD_03("Burger", WallpaperCategory.FOODS, R.drawable.wallpaper_food_03),
    FOOD_04("Fruit", WallpaperCategory.FOODS, R.drawable.wallpaper_food_04),
    FOOD_05("Vegetables", WallpaperCategory.FOODS, R.drawable.wallpaper_food_05),
    FOOD_06("Coffee", WallpaperCategory.FOODS, R.drawable.wallpaper_food_06),
    FOOD_07("Artisan Bread", WallpaperCategory.FOODS, R.drawable.wallpaper_food_07),
    FOOD_08("Fresh Ingredients", WallpaperCategory.FOODS, R.drawable.wallpaper_food_08),
    FOOD_09("Dessert", WallpaperCategory.FOODS, R.drawable.wallpaper_food_09),
    FOOD_10("Harvest", WallpaperCategory.FOODS, R.drawable.wallpaper_food_10),

    FLOWER_01("Rose", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_01),
    FLOWER_02("Tulip", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_02),
    FLOWER_03("Wildflowers", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_03),
    FLOWER_04("Macro Bloom", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_04),
    FLOWER_05("Spring Meadow", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_05),
    FLOWER_06("Lily", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_06),
    FLOWER_07("Summer Field", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_07),
    FLOWER_08("Petals", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_08),
    FLOWER_09("Botanical", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_09),
    FLOWER_10("Nature Bloom", WallpaperCategory.FLOWERS, R.drawable.wallpaper_flower_10),

    OBJECT_01("Minimalist", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_01),
    OBJECT_02("Texture", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_02),
    OBJECT_03("Geometry", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_03),
    OBJECT_04("Still Life", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_04),
    OBJECT_05("Everyday", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_05),
    OBJECT_06("Aesthetic", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_06),
    OBJECT_07("Design", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_07),
    OBJECT_08("Object Portrait", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_08),
    OBJECT_09("Modern Still Life", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_09),
    OBJECT_10("Composition", WallpaperCategory.OBJECTS, R.drawable.wallpaper_object_10)
}

/** A launchable Android activity shown as a ring item. */
data class LaunchableApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable?,
    val lastUsedAt: Long = 0L,
    val useScore: Long = 0L
) {
    val stableId: String get() = "$packageName/$activityName"
}

/** Configuration for a resizable widget tile. */
data class WidgetTile(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Int = 1,
    val height: Int = 1
)

/** A short-lived voice interaction state rendered over the centre surface. */
sealed interface VoiceUiState {
    data object Idle : VoiceUiState
    data object RequestingPermission : VoiceUiState
    data object Listening : VoiceUiState
    data class Heard(val phrase: String) : VoiceUiState
    data class Failed(val message: String) : VoiceUiState
}

# Cnake-6110

A retro-modern, minimalist re-imagining of the iconic Nokia 6110 Snake game for Android, featuring an elegant 10×20 dot matrix aesthetic, fluid swipe controls, and an ultra-lightweight zero-bloat native architecture.

---

## 📥 Ready-to-Install APKs

Pre-built and signed installable APKs are provided directly in this repository:

- **[Cnake-6110.apk](file:///C:/Users/user/Desktop/Cnake-6110/Cnake-6110.apk)** (Root folder, ~171 KB, signed release build)
- **[apk/Cnake-6110-release.apk](file:///C:/Users/user/Desktop/Cnake-6110/apk/Cnake-6110-release.apk)** (Optimized release build)
- **[apk/Cnake-6110-debug.apk](file:///C:/Users/user/Desktop/Cnake-6110/apk/Cnake-6110-debug.apk)** (Debug build with symbols)

You can copy `Cnake-6110.apk` directly to your phone (e.g. Redmi 10 Prime) and tap to install immediately!

---

## 🎮 Highlights & Features

- **Focused on Redmi 10 Prime (1080×2400)**: Precision-calibrated for a 10-column by 20-row dot matrix grid with symmetrical margins and full cutout support across edge-to-edge displays.
- **Ultra-Lightweight & Zero Bloat**: Built purely with native Android 2D hardware-accelerated Canvas (`android.view.View`) and Kotlin. The optimized release APK is **~171 KB**! Instant startup, zero heavy game engine overhead, and negligible battery consumption.
- **Integrated In-Canvas Play Button**: No separate Android UI buttons. When launching the app, a pulsing dot-matrix styled `▶ PLAY` pill is drawn directly into the matrix canvas, smoothly transitioning into gameplay upon tap or swipe.
- **Full-Screen Immersive Experience**: Configured with `WindowInsetsControllerCompat` sticky immersive mode and `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` to hide all system bars (status & navigation) and extend cleanly across display camera cutouts.
- **Pure Minimalist Immersion (No Score HUD)**: No score text, no numbers, and no popups cluttering the screen.
- **Direction-Aware Head Eyes**: The snake's eyes dynamically gaze towards its current movement direction (UP, DOWN, LEFT, RIGHT), matching classic retro sprites.
- **Graceful Tapering Body**: Body segments smoothly taper in radius and opacity from the head down to the tail.
- **Toroidal Wall Wrapping**: When the snake reaches any border (left, right, top, bottom), it effortlessly wraps around and emerges on the opposite side.
- **Dead Snake Game-Over & Tap Restart**: When self-collision occurs, the game stays purely visual:
  - The head turns muted grey with 'X' 'X' eyes.
  - Body segments desaturate while revealing the matrix dots at their centers.
  - Food dot turns muted grey.
  - No "Game Over" text is shown.
  - Tapping on the dead snake triggers an instant restart with haptic feedback.
- **Fluid Swipe Gestures with Turn Queuing**: Intuitive swiping anywhere on screen. Rapid cornering inputs are buffered so fast turns are never dropped.
- **Adaptive Launcher Icons**: Built-in adaptive and round icons crafted directly from the `APP logo.png` design.

---

## 📐 Grid Specifications

| Parameter | Specification | Notes |
|---|---|---|
| **Columns** | 10 dots | Horizontal matrix width |
| **Rows** | 20 dots | Vertical matrix height |
| **Target Device** | Redmi 10 Prime | 1080 × 2400 px (20:9 aspect ratio) |
| **Dot Spacing** | Dynamic Responsive | Automatically scales and centers on any aspect ratio |
| **Active Color** | `#3566EF` (Electric Blue) | Reference-accurate RGB: `(53, 102, 239)` |
| **Inactive Dots** | `#262626` (Dim Grey) | Subtle OLED/LCD contrast |
| **Dead State** | `#525252` Head / `#3A3A3A` Body | 'X' 'X' black eyes and inner grid dots |

---

## 🕹️ Controls

- **Start Game**: Tap the integrated `▶ PLAY` button or swipe in any direction.
- **Steer**: Swipe **Up**, **Down**, **Left**, or **Right** anywhere on the display.
- **Restart**: When dead, tap on or near the dead snake to immediately begin a new run.

---

## 🏗️ Architecture

```
app/src/main/java/com/snake/cnake6110/
├── MainActivity.kt         # Immersive sticky full screen & lifecycle controller
└── game/
    ├── CnakeEngine.kt      # Pure game logic: grid, snake, food, wrapping, collision, queuing
    ├── CnakeGameView.kt    # Custom Canvas view: dot matrix, snake rendering, gestures, haptics
    ├── Direction.kt        # UP, DOWN, LEFT, RIGHT enum with opposite checks
    ├── GridPoint.kt        # Immutable 2D grid coordinates (x, y)
    └── GameState.kt        # READY, PLAYING, DEAD states
```

---

## 🚀 Building the Project

### Prerequisites
- JDK 17
- Android SDK Platform 36 (or 34+)
- Android SDK Build-Tools 36.0.0 (or 34.0.0+)

### Commands

1. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

3. **Build Optimized Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```
   Output: `app/build/outputs/apk/release/app-release.apk` (~171 KB, signed and installable)

---

## 📄 License
MIT License. Created for the Cnake-6110 project.

# Shape Trail 🎯 Shapes & Memory Puzzle Game

**Shape Trail** is a fast-paced, memory and pattern-recognition puzzle game built natively for Android using **Kotlin** and **Jetpack Compose**. Test your memory, focus, and reflexes by memorizing sequence trails of geometric shapes on an interactive board before time runs out!

---

## 🌟 Key Features

- 🎮 **Dynamic Trail Generation**: Each round generates procedural shape sequences with anti-repetition rules to keep gameplay fresh and challenging.
- 📐 **Custom Canvas Shape Rendering**: Custom canvas drawing for 8 distinct geometric shapes (Circle, Square, Triangle, Diamond, Star, Hexagon, Oval, and Cross) with vibrant color palettes.
- 🎚️ **Multiple Difficulty Modes**:
  - **Easy**: 3x3 Grid | Sequence Length: 3–6 | Time: 15s (+1.0s bonus/tap)
  - **Medium**: 4x4 Grid | Sequence Length: 4–9 | Time: 12s (+0.8s bonus/tap)
  - **Hard**: 5x4 Grid | Sequence Length: 5–12 | Time: 9s (+0.6s bonus/tap)
- 🎵 **Procedural Audio Engine**: Real-time PCM audio synthesis built on Android's `AudioTrack` API. Generates crisp tone pitches, bells, arpeggios, buzzers, and fanfares dynamically without bulky sound assets.
- 📳 **Haptic Feedback Engine**: Tactile feedback for tap interactions, countdowns, mistakes, and victory milestones using Android Vibrator APIs.
- 📊 **Persistent Statistics & Settings**: High score tracking, streak records, total games played, and customizable sound/haptic toggles using Android DataStore / SharedPreferences.
- 🎨 **Modern Material 3 UI**: Clean edge-to-edge Compose UI with fluid animations, glowing trail indicators, custom animated countdowns, and result screens.

---

## 🏗️ Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/) (v2.2.10)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with **Material 3**
- **State Management**: Reactive UI with `ViewModel`, `StateFlow`, and Kotlin Coroutines
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose)
- **Audio & Haptics**: Native `AudioTrack` PCM tone generation & `Vibrator` feedback
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`), Android Gradle Plugin (AGP 9.4.1)
- **Testing**: JUnit 4 unit tests covering game logic, score calculation, sequence generation, and timer state

---

## 🎮 How to Play

1. **Watch the Sequence**: At the start of each round, a sequence bar displays the trail of shapes you need to tap.
2. **Memorize & Find**: Locate and tap the corresponding shapes on the interactive grid in exact order.
3. **Earn Bonus Time**: Correct taps extend your countdown timer and build up your streak multiplier!
4. **Advance Rounds**: Completing a sequence unlocks harder sequences and higher score multipliers.
5. **Beat Your High Score**: Keep your streak going before the timer expires or a wrong tile is tapped!

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug / Jellyfish or newer recommended)
- **JDK 11** or higher
- **Android SDK**: `compileSdk` 37 / `minSdk` 26 (Android 8.0 Oreo+)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Shape-Trail.git
   cd Shape-Trail
   ```

2. Build the app using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

3. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## 📱 App Structure

```
com.example.shapetrail/
├── audio/          # Procedural AudioTrack sound synthesis engine
├── data/           # Game models, difficulty settings, and DataStore preferences
├── game/           # Core trail generation algorithms and game mechanics
├── ui/
│   ├── components/ # Reusable Compose components (ShapeBoard, ShapeRenderer, etc.)
│   ├── screens/    # GameScreen, HomeScreen, SettingsScreen, ResultScreen, AboutScreen
│   └── theme/      # Material 3 Color scheme, Typography, and Shapes
├── util/           # Haptics manager and helper utilities
└── viewmodel/      # ShapeTrailViewModel and GameUiState management
```

---

## 📄 License

```
Copyright 2026 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

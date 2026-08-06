# WeatherApp

A modern, highly-polished Android weather application built with Jetpack Compose. The app features a beautiful glassmorphic UI, real-time weather updates, multi-location support, and interactive weather maps.

## 🌟 Features

- **Dynamic Dashboard**: Real-time weather data with beautiful 3D-styled icons.
- **Detailed Forecasts**: Hourly and 7-day weather predictions with temperature range visualizations.
- **Glassmorphic UI**: Powered by [Haze](https://github.com/chrisbanes/haze) for a modern, frosted glass aesthetic that adapts to light and dark themes.
- **Interactive Maps**: Precipitation radar and wind speed maps using MapLibre and Google Maps.
- **Location Management**: Search for cities worldwide, manage favorite locations, and auto-detect current location.
- **Smart Settings**: Customizable units (Celsius/Fahrenheit, km/h / mph), notification preferences, and manual theme switching.
- **Home Screen Widgets**: Glance-based widgets for quick weather updates at a glance.
- **Offline Support**: Caching mechanism to display the last known weather data when offline.
- **Worker Notifications**: Periodic background updates and smart alerts for rain and air quality.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a fully declarative UI.
- **Image Loading**: [Lottie](https://github.com/airbnb/lottie-android) for smooth animations.
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for clean, testable code.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) for API communication.
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for local persistence and favorites management.
- **Preferences**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for user settings.
- **Concurrency**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html).
- **Widgets**: [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) for app widgets.
- **Glassmorphism**: [Haze](https://github.com/chrisbanes/haze) for advanced blur and frosted glass effects.
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for periodic data fetching and notifications.

## 🏗 Architecture

The project follows the **Clean Architecture** principles and **MVVM** (Model-View-ViewModel) pattern:

- **UI Layer**: Jetpack Compose screens and ViewModels.
- **Domain Layer**: Use cases and business models.
- **Data Layer**: Repositories, API services (Retrofit), and local database (Room).

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- JDK 17+.

### API Keys Setup
This project uses several services to provide weather data and interactive maps:

1. **OpenWeatherMap Tiles**: Used for specialized wind speed map layers.
   - Get an API Key from [OpenWeatherMap](https://openweathermap.org/api).
   - This key is used in `local.properties` for the `MAPS_WIND_API_KEY` field.

2. **MapLibre & OpenFreeMap**: Used for the core map engine and base styles. No API key required.
3. **Open-Meteo**: Powering current, hourly, and 7-day weather forecasts. No API key required for non-commercial use.
4. **RainViewer**: Providing precipitation radar data. No API key required.

### Installation
1. Clone the repository.
2. Create a `local.properties` file in the root directory.
3. Add your OpenWeatherMap API key:
   ```properties
   MAPS_WIND_API_KEY=your_openweathermap_api_key
   ```
4. Sync the project with Gradle.
5. Run the app on an emulator or physical device.

---
Designed and developed with ❤️ using Jetpack Compose.

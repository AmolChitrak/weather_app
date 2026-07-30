# Implementation Plan - Connect ManageLocation FAB to SearchScreen

The goal is to allow users to navigate from the "Manage Locations" screen to a search screen when they click the "Add Location" FAB.

## User Review Required

> [!IMPORTANT]
> `SearchScreen` will now support an optional back button. This button will only be visible when navigation occurs from outside the main dashboard (e.g., from `ManageLocationsScreen`).

## Proposed Changes

### UI Components

#### [MODIFY] [SearchScreen.kt](file:///C:/Users/jenil/OneDrive/Desktop/Weather/app/src/main/java/com/jenil/weather/ui/search/SearchScreen.kt)
- Add an optional `onBackClick: (() -> Unit)? = null` parameter to the `SearchScreen` composable.
- Update the `topBar` to conditionally display a back button if `onBackClick` is provided.
- Ensure the `TextField` remains properly aligned when the back button is present.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/jenil/OneDrive/Desktop/Weather/app/src/main/java/com/jenil/weather/MainActivity.kt)
- Import `SearchScreen`.
- Add a new `composable` destination for `"search_screen"`.
- Implement `onLocationSelected` to pass coordinates back to the previous screen via `savedStateHandle` and then pop the backstack.

### Navigation Logic

#### [VERIFY] [ManageLocationScreen.kt](file:///C:/Users/jenil/OneDrive/Desktop/Weather/app/src/main/java/com/jenil/weather/ui/location/ManageLocationScreen.kt)
- Ensure the Floating Action Button (FAB) navigates to `"search_screen"`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Navigate to "Manage Locations" (likely via the locations tab or a menu).
- Click the "+" FAB.
- Verify that `SearchScreen` opens and has a back button.
- Search for a city and select it.
- Verify that the app navigates back to the weather dashboard and loads the selected city's data.
- Verify that when `SearchScreen` is used as a tab in the dashboard, it does NOT show a back button.

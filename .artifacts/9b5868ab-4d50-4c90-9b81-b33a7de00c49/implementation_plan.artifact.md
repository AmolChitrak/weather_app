# Implementation Plan - Connect Recent Searches and Current Location to Search Screen

This plan details how to integrate the `RecentSearchItem` and `CurrentLocationCard` into the `SearchScreen` UI, providing a richer initial experience when the user hasn't typed a search query yet.

## User Review Required

> [!IMPORTANT]
> The "initial" state of the search screen will now show:
> 1. A "Use current location" card.
> 2. A list of Recent Searches (if any).
> 3. A list of Saved Locations (Favorites) (if any).
> This replaces the current behavior where only Favorites were shown if available, or a simple empty state otherwise.

## Proposed Changes

### UI Components

#### [MODIFY] [SearchScreen.kt](file:///C:/Users/jenil/OneDrive/Desktop/Weather/app/src/main/java/com/jenil/weather/ui/search/SearchScreen.kt)
- Update `navigateWithResult` to call `viewModel.addRecentSearch(result)` so selected locations are tracked.
- Revise the state logic to distinguish between "searching" (query not blank) and "initial" (query blank).
- In the "initial" state (replacing "favorites" and "empty" cases):
    - Display the `CurrentLocationCard` at the top.
    - If `recentSearches` is not empty, display a "Recent Searches" section with `RecentSearchItem`s.
    - If `favoriteLocations` is not empty, display a "Saved Locations" section with `LocationListItem`s.
- Ensure `CurrentLocationCard` click triggers `viewModel.fetchCurrentLocation` and navigates on success.

## Verification Plan

### Manual Verification
1. Open the Search Screen.
2. Verify that the "Use current location" card appears.
3. Click "Use current location" and verify it fetches location and returns to the weather screen.
4. Search for a city and select it.
5. Re-open the Search Screen and verify the city appears in "Recent Searches".
6. Click a recent search item and verify it works.
7. Click the "X" (or the remove icon) on a recent search and verify it's removed.

# Implementation Plan - Add Hilt and KSP Dependencies

This plan outlines the steps to properly integrate Hilt and KSP into the project using Version Catalogs (`libs.versions.toml`). This ensures that dependencies are managed in a single place and follow best practices.

## Proposed Changes

### [Component Name] Gradle Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/jenil/OneDrive/Desktop/Weather/gradle/libs.versions.toml)
- Define versions for Hilt, KSP, and Hilt Navigation Compose.
- Add library definitions for Hilt and KSP compiler.
- Add plugin definitions for Hilt and KSP.

#### [MODIFY] [build.gradle.kts (root)](file:///C:/Users/jenil/OneDrive/Desktop/Weather/build.gradle.kts)
- Use plugin aliases from the version catalog.
- Add KSP plugin declaration.
- Remove hardcoded versions.
- Clean up duplicate plugin declarations.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/jenil/OneDrive/Desktop/Weather/app/build.gradle.kts)
- Use plugin aliases from the version catalog.
- Replace hardcoded Hilt and KSP dependencies with references to the version catalog.
- Clean up duplicate plugin declarations.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project builds correctly with the new dependency management.
- Check that KSP generates the necessary Hilt classes (if any Hilt annotations are added later).

### Manual Verification
- Sync Gradle to ensure no errors in the `build.gradle.kts` files.

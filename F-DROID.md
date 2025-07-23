# F-Droid Publication Guide

This document contains information about ConvertIt's F-Droid publication and build process.

## About F-Droid

F-Droid is a catalog of FOSS (Free and Open Source Software) applications for the Android platform. F-Droid makes it easy to browse, install, and keep track of updates on your device.

## F-Droid Metadata

The F-Droid metadata for ConvertIt is located in the metadata/ directory:

- metadata/com.nasahacker.convertit.yml - Main F-Droid metadata file
- metadata/en-US/ - English metadata and screenshots
- fastlane/metadata/android/ - Fastlane-compatible metadata structure

## Build Requirements

### Dependencies

ConvertIt uses the following key dependencies:

- FFmpeg Kit: For audio/video conversion (included as AAR)
- TagLib: For metadata handling (included as AAR)
- Jetpack Compose: For UI
- Material Design 3: For modern UI components

### Build Configuration

The app is configured to build with:

- Minimum SDK: 21 (Android 5.0)
- Target SDK: 36
- Kotlin: 2.1.20
- Gradle: 8.10.1
- Java: 11

### F-Droid Build Process

F-Droid builds the app from source using the configuration in metadata/com.nasahacker.convertit.yml. The build process:

1. Clones the repository at the specified commit/tag
2. Sets up the build environment with required dependencies
3. Runs the Gradle build with F-Droid's signing keys
4. Verifies the build meets F-Droid's guidelines

## Publishing Process

### Manual Submission

1. Fork the F-Droid Data repository (https://gitlab.com/fdroid/fdroiddata)
2. Add the metadata file (metadata/com.nasahacker.convertit.yml) to the forked repository
3. Submit a merge request to the F-Droid Data repository
4. Wait for F-Droid maintainers to review and approve

### Automated Updates

Once published, F-Droid can automatically detect new releases using:

- AutoUpdateMode: Version - Automatically updates to new version tags
- UpdateCheckMode: Tags - Checks for new Git tags

## F-Droid Guidelines Compliance

ConvertIt complies with F-Droid guidelines by:

### Privacy Friendly
- No tracking or analytics
- No advertising
- No data collection
- No network permissions for core functionality

### Open Source
- Apache 2.0 license
- Full source code available
- No proprietary dependencies in core functionality

### Security
- No unnecessary permissions
- No background network activity
- All processing done locally

### Build Reproducibility
- Standard Gradle build system
- No custom build scripts
- All dependencies specified in build files

## Anti-Features

ConvertIt has no anti-features according to F-Droid criteria:

- No Ads
- No Tracking
- No Non-Free Network Services
- No Non-Free Dependencies (core functionality)
- No Promotional Content

## Support

For F-Droid specific issues:

- Check the F-Droid Forum (https://forum.f-droid.org/)
- Report issues to our GitHub Issues (https://github.com/TheByteArray/ConvertIt/issues)
- Contact F-Droid maintainers for build-related problems

## Links

- F-Droid App Page: Will be available after publication
- Source Code: https://github.com/TheByteArray/ConvertIt
- Issue Tracker: https://github.com/TheByteArray/ConvertIt/issues
- F-Droid Guidelines: https://f-droid.org/docs/Inclusion_Policy/ 
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.1-lts] - 2024-12-30

### Added
- Support for multiple audio and video format conversions (FLAC, ALAC, MP3, WAV, AAC, OGG, M4A, AIFF, OPUS, WMA, MKA, SPX)
- Flexible bitrate selection (64k to 1024k)
- Video-to-audio conversion capability
- Modern Material Design 3 UI with Jetpack Compose
- FFmpeg integration for high-quality conversions
- Foreground service for conversion progress tracking
- Notification system for conversion status
- Custom save location support
- Metadata editing capabilities
- Cover art management
- Bengali (bn) localization support

### Changed
- Improved boolean extra handling in ViewModel
- Enhanced lifecycle-aware state observation with collectAsStateWithLifecycle
- Optimized conversion service performance

### Fixed
- Resolved boolean extra handling issues in AppViewModel
- Improved resource management for better performance

### Removed
- Unused test code cleanup

## Previous Versions

### [1.3.0] - 2024-11-15
- Added video-to-audio conversion support
- Enhanced UI with better navigation
- Improved conversion stability

### [1.2.0] - 2024-10-01
- Added metadata editing features
- Implemented cover art support
- Enhanced notification system

### [1.1.0] - 2024-09-01
- Added custom save location
- Improved bitrate selection
- Enhanced UI design

### [1.0.0] - 2024-08-01
- Initial release
- Basic audio conversion functionality
- Support for common audio formats
- Simple and intuitive UI 
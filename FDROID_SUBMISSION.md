# F-Droid Submission Documentation

This document provides information for submitting ConvertIt to F-Droid.

## Pre-Submission Requirements

### Code & License
- Source code is publicly available on GitHub
- Project uses Apache-2.0 license (F-Droid compatible)
- No proprietary dependencies in core functionality
- All source files include proper license headers

### Build System
- Uses standard Gradle build system
- No custom or proprietary build tools
- Build is reproducible
- No signing keys in repository

### Privacy & Security
- No tracking or analytics
- No advertising networks
- No unnecessary permissions
- All processing done locally
- No network requests for core functionality

### Metadata
- F-Droid metadata file created (metadata/com.nasahacker.convertit.yml)
- App description and summary provided
- Screenshots available in correct format
- Categories properly assigned
- Links to source code, issues, and website provided

### Localization
- English metadata complete
- Proper UTF-8 encoding for all text files

## Submission Process

### Step 1: Prepare Metadata File

The main metadata file metadata/com.nasahacker.convertit.yml contains:

- App information (name, summary, description)
- Build configuration
- Links (source code, issues, website)
- License information
- Update mechanism configuration

### Step 2: Fork F-Droid Data Repository

1. Go to https://gitlab.com/fdroid/fdroiddata
2. Fork the repository to your GitLab account
3. Clone your fork locally

### Step 3: Add Metadata

1. Copy metadata/com.nasahacker.convertit.yml to the metadata/ directory in your fdroiddata fork
2. Ensure the filename matches the app's package name exactly
3. Verify all information is correct

### Step 4: Submit Merge Request

1. Commit your changes
2. Push to your fork
3. Create a merge request on GitLab with clear title and description
4. Include link to source code repository

### Step 5: Review Process

F-Droid maintainers will:
- Review the metadata
- Test the build process
- Check for policy compliance
- Provide feedback if changes are needed

## Common Issues and Solutions

### Build Failures
- Missing dependencies: Ensure all dependencies are properly declared in build.gradle
- Custom build steps: Use only standard Gradle tasks, avoid custom scripts

### Metadata Issues
- Incorrect category: Use only categories from F-Droid's official list
- Missing information: Provide complete app description, summary, and links

### License Issues
- Unclear licensing: Ensure LICENSE file is present and all source files have headers

## Post-Submission

Once approved:

1. Monitor builds: F-Droid will build your app automatically
2. Update process: New versions will be detected via Git tags
3. Maintenance: Respond to any build issues or user reports

## Useful Links

- F-Droid Inclusion Policy: https://f-droid.org/docs/Inclusion_Policy/
- F-Droid Metadata Reference: https://f-droid.org/docs/Build_Metadata_Reference/
- F-Droid Data Repository: https://gitlab.com/fdroid/fdroiddata
- F-Droid Forum: https://forum.f-droid.org/

## Contact

For questions about this submission:
- GitHub Issues: https://github.com/TheByteArray/ConvertIt/issues
- F-Droid Forum: https://forum.f-droid.org/
- Email: tamimh.dev@gmail.com 
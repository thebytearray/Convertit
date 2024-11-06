# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Remove all classes, fields, and methods that are unused
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify

# Remove debugging information for further shrinking
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*

# Enable aggressive optimization (R8 does this by default)
-optimizationpasses 5
-dontoptimize
-allowaccessmodification
-mergeinterfacesaggressively

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}


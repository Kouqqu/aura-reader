# Proguard rules for Aura Reader

# Disable name obfuscation: keeps full, human-readable class/method names and stack traces
-dontobfuscate
-keepattributes SourceFile,LineNumberTable,*Annotation*

-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Preserve data models
-keep class com.aura.reader.data.model.** { *; }

# TelemetryDeck SDK
-keep class com.telemetrydeck.sdk.** { *; }
-dontwarn com.telemetrydeck.sdk.**

# Suppress harmless warnings from 3rd party networking/HTML libraries
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.jsoup.**

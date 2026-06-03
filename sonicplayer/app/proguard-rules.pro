# ProGuard rules for SonicPlayer
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { <init>(); }

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Hilt
-keepclassmembers @dagger.hilt.android.HiltAndroidApp class * { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# General
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application

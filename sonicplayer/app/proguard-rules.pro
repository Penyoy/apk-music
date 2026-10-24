# ProGuard rules for SonicPlayer

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# App components referenced from the manifest
-keep class com.sonicplayer.MainActivity { *; }
-keep class com.sonicplayer.SonicApplication { *; }
-keep class com.sonicplayer.service.MusicPlaybackService { *; }
-keep class com.sonicplayer.artwork.ArtworkProvider { *; }
-keep class androidx.media3.session.MediaButtonReceiver { *; }
-keep class * extends androidx.media3.session.MediaSessionService { *; }
-keep class * extends androidx.media3.session.MediaSession { *; }

# Room entities and generated databases
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { <init>(); }
-keep class com.sonicplayer.data.model.** { *; }

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Hilt
-keepclassmembers @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Media3 ships its own consumer rules; silence optional dependencies only.
-dontwarn androidx.media3.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn org.conscrypt.**

# General Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

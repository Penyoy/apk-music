# SonicPlayer ♪

A minimal, offline music player for Android with a deep-black and gold interface.

<p align="center">
  <b>♪ SonicPlayer</b><br/>
  Black &amp; gold · Responsive · Animated
</p>

## Features

- Local audio library (MP3, FLAC, AAC, WAV, and more)
- Songs, albums, artists, playlists, and favorites
- Media3 playback service with notification controls
- Now playing screen with rotating vinyl artwork
- Sleep timer, equalizer, bass boost, and virtualizer
- Minimal gold `♪` launcher icon
- Responsive layout: bottom navigation on phones, navigation rail on tablets
- Smooth screen transitions and animated player elements

## Build

The APK is built automatically by GitHub Actions on every push to `main`.
You can also run the workflow manually from the **Actions** tab.

The output artifact is named **SonicPlayer-debug-apk**.

```bash
cd sonicplayer
./gradlew assembleDebug
```

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Media3 ExoPlayer / MediaSession
- Room
- Hilt
- Coil

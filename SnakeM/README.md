# Snake M (Android)

A native Android 2D snake game with a dark theme, on-screen joystick, three apples at a time, score counter, growth when an apple is eaten, and wall/self collision.

## Build the APK

This environment does not have the Android SDK or Gradle installed, so I could not compile or verify an APK here. The complete Android Studio project is included.

1. Install **Android Studio** on a computer: https://developer.android.com/studio
2. Extract `SnakeM-project.zip`.
3. In Android Studio, choose **Open** and select the extracted `SnakeM` folder (the one containing `settings.gradle`).
4. Allow Gradle sync. If prompted, install Android SDK Platform 35 and the Android Gradle Plugin dependencies.
5. To make an installable APK, select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. When the build finishes, click **locate** in the notification. The APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.
7. Transfer the APK to your Android phone and open it. If Android asks, allow installation from that file manager.

## Play

- Tap **START**.
- Drag the round joystick at the lower-left to steer. The snake turns in the direction you push.
- Eat the red apples to gain 10 points and grow longer. Three apples are kept on the map.
- Avoid the walls and your own body. Tap START after game over to restart.

## Notes

- App id: `com.example.snakem`
- Minimum Android: Android 6.0 (API 23)
- Portrait orientation; no permissions or internet access required.

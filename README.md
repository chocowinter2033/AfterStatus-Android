# AfterStatus Android starter

This is a native Android Studio project written in Kotlin with Jetpack Compose. It is designed to build an Android APK on a Windows PC, Mac, or Linux machine with Android Studio installed.

## Included
- Native Android Compose interface with Feed, Discover, Chats, and Profile tabs
- Sample fictional characters, search and fandom filters
- Create posts and like posts
- Character profile and chat screens
- Scripted demo replies that work offline
- Optional OpenAI-compatible local AI endpoint configuration
- No purchases, subscriptions, energy meter, or paid message credits

## Build an APK on Windows
1. Install Android Studio from https://developer.android.com/studio
2. Extract this ZIP.
3. In Android Studio choose **Open** and select the extracted `AfterStatus-Android` folder.
4. Let Gradle sync and install any SDK components Android Studio requests.
5. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Android Studio will show a notification with **Locate** when the APK is ready. Common path: `app/build/outputs/apk/debug/app-debug.apk`.
7. Transfer the APK to your Android phone and install it. You may need to allow installs from that source in Android settings.

## Optional local AI
Demo replies are the default and work offline. For real AI replies, run a compatible local model server on a computer on the same trusted Wi-Fi, then set its LAN address in the app settings, such as `http://192.168.1.10:11434/v1/chat/completions`, and model name `qwen2.5:3b`. Replace the sample IP with your computer's LAN IP. Configure firewall/network access carefully; don't expose an unauthenticated endpoint to the public internet.

Local inference has no app token charge but still uses electricity and hardware resources.

## Limitations
- This is project source, not a prebuilt APK. It has not been compiled or device-tested in this environment.
- The feed and character data are sample/demo data. There is no account system, public multi-user feed, cloud sync, push notifications, or moderation backend.
- Chat history currently exists in app memory for the running session; durable chat persistence is not implemented in this starter.
- For Play Store distribution, you will need a signed release build and comply with Google Play policies.

# react-native-unimodules

This library contains the core unimodule infrastructure and a collection of unimodules and interfaces that are commonly depended on by other unimodules. You only need to install `react-native-unimodules` once and then you will be able to use [most of the packages from the Expo SDK](https://docs.expo.io/versions/latest/bare/unimodules-full-list/), like [expo-camera](https://docs.expo.io/versions/latest/sdk/camera/), [expo-media-library](https://docs.expo.io/versions/latest/sdk/media-library/) and many more, [in any React Native project](https://blog.expo.io/you-can-now-use-expo-apis-in-any-react-native-app-7c3a93041331).

The easiest way to use the library is to initialize a "bare" project with `expo-cli` (`npm i -g expo-cli`) using `expo init --template bare-minimum` or `expo init --template expo-template-bare-typescript` if you prefer TypeScript. If you have an existing project you'd like to install it into, please read the following instructions.

## 📦 Installation

**This project requires that you use CocoaPods on iOS**, to set it up see [this gist](https://gist.github.com/brentvatne/b0ea11a36dc423e441b7d36e36eb5a26), or relevant parts of the [this guide](https://facebook.github.io/react-native/docs/integration-with-existing-apps#3-install-cocoapods). React Native >= 0.60 ships with Cocoapods support by default, so this should be easy if you're already on that version.

```bash
npm install react-native-unimodules
```

Now you need to configure the library for iOS and/or Android.

## 🍎 Configure iOS

- Open `ios/Podfile` in your editor and make it look [like this one on react-native <= 0.59](https://gist.github.com/sjchmiela/6c079f2173938a9a61a7c6f053c45000) or [like this one on react-native >= 0.60](https://gist.github.com/brentvatne/d093e440698404803bd9c29d962949b0/revisions#diff-4a25b996826623c4a3a4910f47f10c30).
- Run `pod install` again
- Update your `AppDelegate.h` and `AppDelegate.m` according to [to look like these](https://gist.github.com/brentvatne/1ece8c32a3c5c9d0ac3a470460c65603).
  - If you use [`react-native-navigation`](https://github.com/wix/react-native-navigation), you will need to use its `bridgeManagerDelegate` option [like in this gist](https://gist.github.com/brentvatne/67909ec442121de22c9b81c629a99aa6).

### Add permission usage description keys to Info.plist

In order to submit your app to the App Store, you will need to eventually add permission usage keys to your `Info.plist`. Even if you don't use the APIs described, you need to include the keys because code related to asking the permission will be bundled regardless, and Apple's static analysis tools will detect it and reject your app if the key isn't present. Including the key without using it has no impact to your users - iOS app permissions are requested at runtime and not listed in the app store listing as they are on Android. Test the permission prompts and customize the message as needed.

<details><summary>See a list of keys to add to Info.plist</summary>
<p>

```xml
<key>NSCalendarsUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to access your calendar</string>
<key>NSCameraUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to use the camera</string>
<key>NSContactsUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to access your contacts</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to use your location</string>
<key>NSLocationAlwaysUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to use your location</string>
<key>NSLocationWhenInUseUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to use your location</string>
<key>NSMicrophoneUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to access your microphone</string>
<key>NSMotionUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to access your device's accelerometer</string>
<key>NSPhotoLibraryAddUsageDescription</key>
<string>Give $(PRODUCT_NAME) permission to save photos</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>Give $(PRODUCT_NAME) permission to access your photos</string>
<key>NSRemindersUsageDescription</key>
<string>Allow $(PRODUCT_NAME) to access your reminders</string>
```

</p>
</details>

### Advanced configuration

<details><summary>Need to customize node_modules path?</summary>
<p>

If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param for this: `use_unimodules!(modules_paths: ['./path/to/node_modules'])`

</p>
</details>

<details><summary>Need to exclude some unimodules that are being automatically linked?</summary> 
<p>

If you need to exclude some of the unimodules that you are not using but they got installed by your other dependencies (like `expo`), then you can pass in `exclude` param for this. For example, if you want to exclude `expo-face-detector`, you may want to use this: `use_unimodules!(exclude: ['expo-face-detector'])`

</p>
</details>

## 🤖 Configure Android

__In `android/settings.gradle`__
1. At the top add `apply from: '../node_modules/react-native-unimodules/gradle.groovy'` 
1. Then call `includeUnimodulesProjects()` on the next line.

__In `android/app/build.gradle`__
1. Add `apply from: '../../node_modules/react-native-unimodules/gradle.groovy'` anywhere before the `dependencies {}` block.
1. Add `addUnimodulesDependencies()` inside `dependencies {}` block.
1. We recommend you use Java 1.8, you can set this [like this](https://github.com/expo/expo/commit/e175f870418fc69e8c129168118264439d73d7cc).

__In `android/build.gradle`__
1. Update `minSdkVersion` to `21`.

__In `MainApplication.java`__
Make the changes outlined in the diff that correspondes to your react-native version. 
- [this diff for react-native <= 0.59](https://gist.github.com/tsapeta/9e50a4c2c0083fe8e578959526bfbed3/revisions#diff-a2e7ff8a82f1c4be06f8b8163f2afefa)
- [this diff for react-native >= 0.60](https://gist.github.com/brentvatne/62a9c949aa7d1cda410adbe01cba0554/revisions#diff-a2e7ff8a82f1c4be06f8b8163f2afefa)

### Optional: Add permissions to AndroidManifest.xml

Add permissions you would like to use in your app to `android/app/src/main/AndroidManifest.xml`:

<details><summary>See a list of some Android permissions</summary>
<p>

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
```

</p>
</details>

### Advanced configuration

<details><summary>Need to customize node_modules path?</summary>
<p>

If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param `modulesPaths` for both of these functions: `includeUnimodulesProjects([modulesPaths: ['./path/to/node_modules']])`, `addUnimodulesDependencies([modulesPaths: ['./path/to/node_modules']])`

</p>
</details>

<details><summary>Need to exclude some unimodules that are being automatically linked?</summary>
<p>

If you need to exclude some of the unimodules that you are not using but they got installed by your other dependencies (like `expo`), then you can pass in `exclude` param for this. For example, if you want to exclude `expo-face-detector`, you may want to use this: `addUnimodulesDependencies([exclude: ['expo-face-detector']])`

</p>
</details>

<details><summary>Need to customize configuration of unimodule dependencies?</summary>
<p>

You can also customize the configuration of the unimodules dependencies (the default is `implementation`, if you're using Gradle older than 3.0, you will need to set `configuration: "compile"` in `addUnimodulesDependencies`, like: `addUnimodulesDependencies([configuration: "compile"])`)

</p>
</details>

# API

It's possible that you will not have to use any of the code provided by this package directly, it may be used only by other Unimodules that you install.

But it's likely that you will want to use something like FileSystem or Permissions, and to do that you can import the following modules like so:

```js
import {
  Asset,
  Constants,
  FileSystem,
  Permissions,
} from 'react-native-unimodules';
```

You can import them directly from the specific Unimodule package if you like, but your linter may complain about importing a transitive dependency.

```js
import * as Permissions from 'expo-permissions';
```

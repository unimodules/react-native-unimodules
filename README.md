# react-native-unimodules

This library contains the core Unimodule infrastructure and a collection of Unimodules and interfaces that are commonly depended on by other Unimodules.
You will need to install this before using libraries from Foundation, like `expo-camera`, `expo-media-library`, and others.

**Note: `react-native-unimodules` are not compatible with React Native 0.60.x yet.**

## Installation

The easiest way to do this is to initialize a "bare" project with `expo-cli` using `expo init --template bare-minimum`.

If you have an existing app, you can follow these steps instead.
**This project requires that you use CocoaPods on iOS**, to set it up see [this gist](https://gist.github.com/brentvatne/b0ea11a36dc423e441b7d36e36eb5a26), or relevant parts of the [this guide](https://facebook.github.io/react-native/docs/integration-with-existing-apps#3-install-cocoapods).

### Install the package

```bash
npm install react-native-unimodules
```

### Configure iOS

- Go back to the `ios` directory and open your Podfile, make your Podfile look [like this one](https://gist.github.com/sjchmiela/6c079f2173938a9a61a7c6f053c45000).
  - If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param for this: `use_unimodules!(modules_paths: ['./path/to/node_modules'])`
  - If you need to exclude some of the unimodules that you are not using but they got installed by your other dependencies (like `expo`), then you can pass in `exclude` param for this. For example, if you want to exclude `expo-face-detector`, you may want to use this: `use_unimodules!(exclude: ['expo-face-detector'])`
- Run `pod install` again
- Update your `AppDelegate.h` and `AppDelegate.m` according to [to look like these](https://gist.github.com/brentvatne/1ece8c32a3c5c9d0ac3a470460c65603).
  - If you use [`react-native-navigation`](https://github.com/wix/react-native-navigation), you will need to use its `bridgeManagerDelegate` option [like in this gist](https://gist.github.com/brentvatne/67909ec442121de22c9b81c629a99aa6).

#### Add permission usage description keys to `Info.plist`

In order to submit your app to the App Store, you will need to eventually add these keys to your `Info.plist`. Even if you don't use the APIs described, you need to include the keys because code related to asking the permission will be bundled regardless, and Apple's static analysis tools will detect it and reject your app if the key isn't present. Including the key without using it has no impact to your users - iOS app permissions are requested at runtime and not listed in the app store listing as they are on Android. Test the permission prompts and customize the message as needed.

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

### Configure Android

- At the top of `android/settings.gradle`, add `apply from: '../node_modules/react-native-unimodules/gradle.groovy'` and then on the next line add `includeUnimodulesProjects()`.
- Add `apply from: '../../node_modules/react-native-unimodules/gradle.groovy'` anywhere in `android/app/build.gradle` and then `addUnimodulesDependencies()` inside `dependencies {}` block.
- If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param `modulesPaths` for both of these functions: `includeUnimodulesProjects([modulesPaths: ['./path/to/node_modules']])`, `addUnimodulesDependencies([modulesPaths: ['./path/to/node_modules']])`
- If you need to exclude some of the unimodules that you are not using but they got installed by your other dependencies (like `expo`), then you can pass in `exclude` param for this. For example, if you want to exclude `expo-face-detector`, you may want to use this: `addUnimodulesDependencies([exclude: ['expo-face-detector']])`
- You can also customize the configuration of the unimodules dependencies (the default is `implementation`, if you're using Gradle older than 3.0, you will need to set `configuration: "compile"` in `addUnimodulesDependencies`, like: `addUnimodulesDependencies([configuration: "compile"])`)
- We recommend using Java 1.8, you can set this in `android/app/build.gradle` [like this](https://github.com/expo/expo/commit/e175f870418fc69e8c129168118264439d73d7cc).
- Update `minSdkVersion` in `android/build.gradle` to `21`.
- Update your `MainApplication.java` to according to [this diff](https://gist.github.com/tsapeta/9e50a4c2c0083fe8e578959526bfbed3/revisions#diff-a2e7ff8a82f1c4be06f8b8163f2afefa).

#### Add permissions to AndroidManifest.xml

Add whichever of the following permissions you would like to use in your app to `android/app/src/main`:

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

## API

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

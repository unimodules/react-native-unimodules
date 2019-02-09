# @unimodules/react-native-platform

This library contains the core unimodule infrastructure and a collection of unimodules and interfaces that are commonly depended on by other unimodules.
You will need to install this before using libraries from Foundation, like `expo-camera`, `expo-media-library`, and others.

## Installation

The easiest way to do this is to initialize a "custom" project with `expo-cli`. If you have an existing app, you can follow these steps instead.

### This project requires Cocoapods

**If you're already using Cocoapods for your iOS app, skip ahead to the next step**

- Install Cocoapods on your machine if you haven’t already ([here's how to do it](https://guides.cocoapods.org/using/getting-started.html#getting-started)).
- `cd` into your project’s `ios` directory and run `pod init`
- Make your `Podfile` look like [this one](https://gist.github.com/brentvatne/6a1dcb32f6ca3d478eed4c7dc8fbdd24/b713c136f70f229a3450d3285875c7cb64c0b3d0). You might have to add more subspecs depending on what React Native core libraries you use in your project.
- Run `pod install`
- Open the new `.xcworkspace` file and run your project, it should work. If not, seek help.

### Install and configure

```bash
npm install @unimodules/react-native-platform
```

#### iOS

- Go back to the `ios` directory and open your Podfile, make your Podfile look [like this one](https://gist.github.com/brentvatne/6a1dcb32f6ca3d478eed4c7dc8fbdd24).
  - If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param for this: `use_unimodules!(node_modules_path: '../../../node_modules')`
- Run `pod install` again
- Update your `AppDelegate.h` and `AppDelegate.m` according to [this diff](https://gist.github.com/brentvatne/949d9cc3508cc45f54af5196b3ca497b/revisions).

#### Android

- Add `apply from: '../node_modules/@unimodules/react-native-platform/settings.gradle'` and then `useUnimodules.apply()` to the top of  `android/settings.gradle`
  - If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param for this: `useUnimodules.apply(nodeModulesPath: "../../../node_modules")`
- Add `apply from: '../../node_modules/@unimodules/react-native-platform/build.gradle'` anywhere in `android/app/build.gradle`
- Update `minSdkVersion` in `android/build.gradle` to 21
- Update your `MainApplication.java` to according to [this diff](https://gist.github.com/brentvatne/eb4606e39d5d5e6a764c16acde82198a/revisions#diff-a2e7ff8a82f1c4be06f8b8163f2afefa).

## API

If you need to access some of the unimodules provided by this package you can import them through it.

```js
import {
  Asset,
  Constants,
  FileSystem,
  Permissions,
} from '@unimodules/react-native-platform';
```

Or you can import them directly from the unimodule if you like, but your linter may complain for import a transitive dependency.

```js
import * Permissions as  from 'expo-permissions';
```

# @unimodules/react-native-platform

This library contains the core Unimodule infrastructure and a collection of Unimodules and interfaces that are commonly depended on by other Unimodules.
You will need to install this before using libraries from Foundation, like `expo-camera`, `expo-media-library`, and others.

## Installation

The easiest way to do this is to initialize a "custom" project with `expo-cli`. If you have an existing app, you can follow these steps instead.
**This project requires that you use Cocoapods on iOS**, to set iot up see [this gist](https://gist.github.com/brentvatne/b0ea11a36dc423e441b7d36e36eb5a26), or relevant parts of the [this guide](https://facebook.github.io/react-native/docs/integration-with-existing-apps#3-install-cocoapods).

### Install the package

```bash
npm install @unimodules/react-native-platform
```

### Configure iOS

- Go back to the `ios` directory and open your Podfile, make your Podfile look [like this one](https://gist.github.com/brentvatne/6a1dcb32f6ca3d478eed4c7dc8fbdd24).
  - If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param for this: `use_unimodules!(node_modules_path: '../../../node_modules')`
- Run `pod install` again
- Update your `AppDelegate.h` and `AppDelegate.m` according to [this diff](https://gist.github.com/brentvatne/949d9cc3508cc45f54af5196b3ca497b/revisions).

### Configure Android

- Add `apply from: '../node_modules/@unimodules/react-native-platform/settings.gradle'` and then `useUnimodules.apply()` to the top of  `android/settings.gradle`
  - If you need to customize the path to node_modules, for example because you are using yarn workspaces, then you can pass in a param for this: `useUnimodules.apply(nodeModulesPath: "../../../node_modules")`
- Add `apply from: '../../node_modules/@unimodules/react-native-platform/build.gradle'` anywhere in `android/app/build.gradle`
- Update `minSdkVersion` in `android/build.gradle` to 21
- Update your `MainApplication.java` to according to [this diff](https://gist.github.com/brentvatne/eb4606e39d5d5e6a764c16acde82198a/revisions#diff-a2e7ff8a82f1c4be06f8b8163f2afefa).

## API

It's possible that you will not have to use any of the code provided by this package directly, it may be used only by other Unimodules that you install.

But it's likely that you will want to use something like FileSystem or Permissions, and to do that you can import the following modules like so:

```js
import {
  Asset,
  Constants,
  FileSystem,
  Permissions,
} from '@unimodules/react-native-platform';
```

You can import them directly from the specific Unimodule packag if you like, but your linter may complain for import a transitive dependency.

```js
import * Permissions as  from 'expo-permissions';
```

# Changelog

## 0.3.1

### 🐛 Bug fixes

- Fixed TypeScript definitions of common unimodules not being exported. Thanks [@saadq](https://github.com/saadq)! ([#24](https://github.com/unimodules/react-native-unimodules/pull/24))
- Fixed automatic installation script not finding unimodules when using CocoaPods' `--project-directory` flag. ([#31](https://github.com/unimodules/react-native-unimodules/pull/31))

## 0.3.0

### 🎉 New features

- Automatically generated list of Android packages ([#28](https://github.com/unimodules/react-native-unimodules/pull/28))
  As of this version, you no longer need to add new packages to your `MainApplication.java` file. Just use `new BasePackageList().getPackageList()` instead 🎉. `BasePackageList` is auto-generated with a list of installed unimodules found in your `node_modules` folder during Gradle's Sync operation.

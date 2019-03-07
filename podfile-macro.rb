def use_unimodules!(options = {})
  # Default path for Podfile is project/ios/Podfile, and node_modules is in project/node_modules
  # This may be different when using yarn workspaces or some other custom setup
  node_modules_path = options.fetch(:node_modules_path, '../node_modules')

  # Unimodule core packages
  pod "EXCore", path: "#{node_modules_path}/expo-core/ios"
  pod "EXReactNativeAdapter", path: "#{node_modules_path}/expo-react-native-adapter/ios", inhibit_warnings: true
  pod "EXErrors", path: "#{node_modules_path}/expo-errors/ios"

  # Interfaces
  pod "EXFontInterface", path: "#{node_modules_path}/expo-font-interface/ios"
  pod "EXImageLoaderInterface", path: "#{node_modules_path}/expo-image-loader-interface/ios"
  pod "EXConstantsInterface", path: "#{node_modules_path}/expo-constants-interface/ios"
  pod "EXSensorsInterface", path: "#{node_modules_path}/expo-sensors-interface/ios"
  pod "EXFileSystemInterface", path: "#{node_modules_path}/expo-file-system-interface/ios"
  pod "EXPermissionsInterface", path: "#{node_modules_path}/expo-permissions-interface/ios"
  pod "EXCameraInterface", path: "#{node_modules_path}/expo-camera-interface/ios"
  pod "EXFaceDetectorInterface", path: "#{node_modules_path}/expo-face-detector-interface/ios"
  pod "EXBarCodeScannerInterface", path: "#{node_modules_path}/expo-barcode-scanner-interface/ios"
  pod "EXTaskManagerInterface", path: "#{node_modules_path}/expo-task-manager-interface/ios"

  # Implementations for commonly depended on APIs
  pod "EXPermissions", path: "#{node_modules_path}/expo-permissions/ios"
  pod "EXConstants", path: "#{node_modules_path}/expo-constants/ios"
  pod "EXFileSystem", path: "#{node_modules_path}/expo-file-system/ios"
end

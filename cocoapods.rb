require 'json'

def use_unimodules!(custom_options = {})
  options = {
    modules_paths: ['../node_modules'],
    target: 'react-native',
    exclude: [],
  }.deep_merge(custom_options)

  modules_paths = options.fetch(:modules_paths)
  exclude = options.fetch(:exclude)
  target = options.fetch(:target)

  unimodules = []

  modules_paths.each { |module_path|
    glob_pattern = File.join(module_path, '**', 'unimodule.json')

    Dir.glob(glob_pattern) { |module_config_path|
      unimodule_json = JSON.parse(File.read(module_config_path))
      directory = File.dirname(module_config_path)
      platforms = unimodule_json['platforms'] || ['ios']
      targets = unimodule_json['targets'] || ['react-native']

      if unimodule_supports_platform(platforms, 'ios') && unimodule_supports_target(targets, target) then
        package_name = unimodule_json['name'] || get_package_name(directory)

        if !exclude.include?(package_name) then
          unimodule_config = { "subdirectory" => 'ios' }.merge(unimodule_json.fetch('ios', {}))

          unimodules.push({
            name: package_name,
            directory: directory,
            config: unimodule_config,
          })
        end
      end
    }
  }

  unimodules.sort! { |x,y| x['name'] <=> y['name'] }.each { |unimodule|
    directory = unimodule[:directory]
    config = unimodule[:config]

    subdirectory = config['subdirectory']
    pod_name = config.fetch('podName', find_pod_name(directory, subdirectory))

    pod "#{pod_name}", path: "#{directory}/#{subdirectory}"
  }
end

def get_package_name(package_path)
  package_json_path = File.join(package_path, 'package.json')
  package_json = JSON.parse(File.read(package_json_path))
  return package_json['name']
end

def find_pod_name(package_path, subdirectory)
  podspec_path = Dir.glob(File.join(package_path, subdirectory, '*.podspec')).first
  return podspec_path && File.basename(podspec_path).chomp('.podspec')
end

def unimodule_supports_platform(platforms, platform)
  return platforms.class == Array && platforms.include?(platform)
end

def unimodule_supports_target(targets, target)
  return targets.class == Array && targets.include?(target)
end

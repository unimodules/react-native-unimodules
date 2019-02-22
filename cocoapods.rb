require 'json'

def use_unimodules!(options = {})
  node_modules_paths = options.fetch(:node_modules_paths, ['node_modules'])
  exclude = options.fetch(:exclude, [])
  target = options.fetch(:target, 'react-native')

  unimodules = []

  node_modules_paths.each { |node_module_path|
    globPattern = File.join(node_module_path, '**', 'unimodule.json')

    Dir.glob(globPattern) { |moduleConfigPath|
      unimodule_json = JSON.parse(File.read(moduleConfigPath))
      directory = File.dirname(moduleConfigPath)
      platforms = unimodule_json['platforms']
      targets = unimodule_json['targets']

      if unimodule_supports_platform(platforms) && unimodule_supports_target(targets, target) then
        package_name = unimodule_json['name'] || get_package_name(directory)

        if !exclude.include?(package_name) then
          unimodules.push({
            name: package_name,
            directory: directory,
            config: platforms.class == Array ? {} : platforms['ios'],
          })
        end
      end
    }
  }

  unimodules.each { |unimodule|
    directory = unimodule[:directory]
    config = unimodule[:config]

    subdirectory = config.fetch('subdirectory', 'ios')
    podName = config['podName'] || find_pod_name(directory, subdirectory)

    puts "pod \"#{podName}\", path: \"#{directory}/#{subdirectory}\""
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

def unimodule_supports_platform(platforms)
  return platforms.class == Array && platforms.include?('ios') || !platforms['ios'].nil?
end

def unimodule_supports_target(targets, target)
  return targets.nil? ? target == 'react-native' : targets.include?(target)
end

use_unimodules! # to remove

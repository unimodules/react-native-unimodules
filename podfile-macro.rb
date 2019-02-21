require "json"

def use_unimodules!(options = {})
  node_modules_paths = options.fetch(:node_modules_paths, ['node_modules'])

  unimodules = []

  node_modules_paths.each { |node_module_path|
    globPattern = File.join(node_module_path, '**', 'unimodule.json')

    Dir.glob(globPattern) { |moduleConfigPath|
      json = JSON.parse(File.read(moduleConfigPath))
      directory = File.dirname(moduleConfigPath)

      iosPlatformConfig = json["platforms"]["ios"]

      if !iosPlatformConfig.nil? then
        unimodules.push({
          directory: directory,
          libName: json["libName"],
          config: iosPlatformConfig,
        })
      end
    }
  }

  unimodules.each { |unimodule|
    directory = unimodule[:directory]
    libName = unimodule[:libName]
    config = unimodule[:config]

    podName = config["podName"]
    subdirectory = config.fetch("subdirectory", "ios")

    puts "pod \"#{podName}\", path: \"#{directory}/#{libName}/#{subdirectory}\""
  }
end

use_unimodules! # to remove

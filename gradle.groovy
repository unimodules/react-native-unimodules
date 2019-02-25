import groovy.json.JsonSlurper

def doesUnimoduleSupportPlatform(Map unimoduleJson, String platform) {
  def platforms = unimoduleJson.platforms// || ['android']

  if (platforms instanceof Map) {
    return platforms.containsKey(platform)
  }
  if (platforms instanceof List) {
    return platforms.contains(platform)
  }
  return true
}

def doesUnimoduleSupportTarget(Map unimoduleJson, String target) {
  def targets = unimoduleJson.targets
  return !targets || targets.contains(target)
}

def findUnimodules(String target, List exclude, List modulesPaths) {
  def unimodules = []

  for (modulesPath in modulesPaths) {
    def moduleConfigPaths = new FileNameFinder().getFileNames(modulesPath, '**/unimodule.json', '')

    for (moduleConfigPath in moduleConfigPaths) {
      def unimoduleConfig = new File(moduleConfigPath)
      def unimoduleJson = new JsonSlurper().parseText(unimoduleConfig.text)
      def directory = unimoduleConfig.getParent()
      
      if (doesUnimoduleSupportPlatform(unimoduleJson, 'android') && doesUnimoduleSupportTarget(unimoduleJson, target)) {
        def unimoduleName = unimoduleJson.name

        if (!exclude.contains(unimoduleName)) {
          unimodules.add([
            name: unimoduleJson.name,
            directory: directory,
            config: unimoduleJson.platforms instanceof Map && unimoduleJson.platforms,
          ])
        }
      }
    }
  }

  return unimodules
}

ext.useUnimodules = { Map customOptions = [] ->
  def options = [
    modulesPaths: ['../node_modules'],
    configuration: 'expendency',
    target: 'react-native',
    exclude: [],
  ] << customOptions

  def unimodules = findUnimodules(options.target, options.exclude, options.modulesPaths)

  for (unimodule in unimodules) {
    if (options.configuration == 'expendency') {
      expendency(unimodule.name)
    } else {
      Object dependency = project.project(":${unimodule.name}")
      project.dependencies.add(options.configuration, dependency, null)
    }
  }
}

ext.includeUnimodules = { Map customOptions = [] ->
  def options = [
    modulesPaths: ['../node_modules'],
    target: 'react-native',
    exclude: [],
  ] << customOptions

  def unimodules = findUnimodules(options.target, options.exclude, options.modulesPaths)

  for (unimodule in unimodules) {
    def config = unimodule.config
    def subdirectory = config && config instanceof Map ? config.android.subdirectory : 'android'

    include ":${unimodule.name}"
    project(":${unimodule.name}").projectDir = new File(unimodule.directory, subdirectory)
  }
}

ext.expendency = { String dep, Closure closure = null ->
  Object dependency = null

  if (new File(project.rootProject.projectDir.parentFile, 'package.json').exists()) {
    // Parent directory of the android project has package.json -- probably React Native
    dependency = project.project(":$dep")
  } else {
    // There's no package.json and no pubspec.yaml
    throw new GradleException(
      "'expo-core.gradle' used in a project that seems to be neither a Flutter nor a React Native project."
    )
  }

  String configurationName = project.configurations.findByName("implementation") ? "implementation" : "compile"

  project.dependencies.add(configurationName, dependency, closure)
}

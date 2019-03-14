import groovy.json.JsonSlurper

def doesUnimoduleSupportPlatform(Map unimoduleJson, String platform) {
  def platforms = unimoduleJson.platforms
  return platforms instanceof List && platforms.contains(platform)
}

def doesUnimoduleSupportTarget(Map unimoduleJson, String target) {
  def targets = unimoduleJson.targets
  return !targets || targets.contains(target)
}

def findUnimodules(String target, List modulesToExclude, List modulesPaths) {
  def unimodules = [:]
  def unimodulesDuplicates = []

  for (modulesPath in modulesPaths) {
    def baseDir = new File(rootProject.getBuildFile(), modulesPath).toString()
    def moduleConfigPaths = new FileNameFinder().getFileNames(baseDir, '**/unimodule.json', '')

    for (moduleConfigPath in moduleConfigPaths) {
      def unimoduleConfig = new File(moduleConfigPath)
      def unimoduleJson = new JsonSlurper().parseText(unimoduleConfig.text)
      def directory = unimoduleConfig.getParent()

      if (doesUnimoduleSupportPlatform(unimoduleJson, 'android') && doesUnimoduleSupportTarget(unimoduleJson, target)) {
        def packageJsonFile = new File(directory, 'package.json')
        def packageJson = new JsonSlurper().parseText(packageJsonFile.text)
        def unimoduleName = unimoduleJson.name ?: packageJson.name

        if (!modulesToExclude.contains(unimoduleName)) {
          def platformConfig = [subdirectory: 'android'] << unimoduleJson.get('android', [:])
          def unimoduleVersion = packageJson.version

          if (unimodules[unimoduleName]) {
            unimodulesDuplicates.add(unimoduleName)
          }

          if (!unimodules[unimoduleName] || VersionNumber.parse(unimoduleVersion) >= VersionNumber.parse(unimodules[unimoduleName].version)) {
            unimodules[unimoduleName] = [
              name: unimoduleJson.name,
              directory: directory,
              version: unimoduleVersion,
              config: platformConfig,
            ]
          }
        }
      }
    }
  }
  return [
    unimodules: unimodules.collect { entry -> entry.value },
    duplicates: unimodulesDuplicates.unique()
  ]
}

class Colors {
  static final String NORMAL  = "\u001B[0m"
  static final String RED     = "\u001B[31m"
  static final String GREEN   = "\u001B[32m"
  static final String YELLOW  = "\u001B[33m"
  static final String MAGENTA = "\u001B[35m"
}

ext.addUnimodulesDependencies = { Map customOptions = [:] ->
  if (!(new File(project.rootProject.projectDir.parentFile, 'package.json').exists())) {
    // There's no package.json
    throw new GradleException(
      "'addUnimodulesDependencies()' is being used in a project that doesn't seem to be a React Native project."
    )
  }

  def options = [
    modulesPaths: ['../../node_modules'],
    configuration: 'implementation',
    target: 'react-native',
    exclude: [],
  ] << customOptions

  def results = findUnimodules(options.target, options.exclude, options.modulesPaths)
  def unimodules = results.unimodules
  def duplicates = results.duplicates

  if (unimodules.size() > 0) {
    println()
    println Colors.YELLOW + 'Installing unimodules:' + Colors.NORMAL

    for (unimodule in unimodules) {
      println ' ' + Colors.GREEN + unimodule.name + Colors.YELLOW + '@' + Colors.RED + unimodule.version + Colors.NORMAL + ' from ' + Colors.MAGENTA + unimodule.directory + Colors.NORMAL

      Object dependency = project.project(':' + unimodule.name)
      project.dependencies.add(options.configuration, dependency, null)
    }

    if (duplicates.size() > 0) {
      println()
      println Colors.YELLOW + 'Found some duplicated unimodule packages. Installed the ones with the highest version number.' + Colors.NORMAL
      println Colors.YELLOW + 'Make sure following dependencies of your project are resolving to one specific version:' + Colors.NORMAL

      println ' ' + duplicates
        .collect { unimoduleName -> Colors.GREEN + unimoduleName + Colors.NORMAL }
        .join(', ')
    }
  } else {
    println()
    println Colors.YELLOW + "No unimodules found. Are you sure you've installed JS dependencies?" + Colors.NORMAL
  }
}

ext.includeUnimodulesProjects = { Map customOptions = [:] ->
  def options = [
    modulesPaths: ['../../node_modules'],
    target: 'react-native',
    exclude: [],
  ] << customOptions

  def unimodules = findUnimodules(options.target, options.exclude, options.modulesPaths).unimodules

  for (unimodule in unimodules) {
    def config = unimodule.config
    def subdirectory = config.subdirectory

    include ":${unimodule.name}"
    project(":${unimodule.name}").projectDir = new File(unimodule.directory, subdirectory)
  }
}

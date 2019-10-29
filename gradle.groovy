import groovy.json.JsonSlurper
import org.gradle.util.VersionNumber

import java.util.regex.Pattern

class Colors {
  static final String NORMAL = "\u001B[0m"
  static final String RED = "\u001B[31m"
  static final String GREEN = "\u001B[32m"
  static final String YELLOW = "\u001B[33m"
  static final String MAGENTA = "\u001B[35m"
}

class BuildGradleParser {
  // Returns a map with keys `version` and `group` which reflect
  // `build.gradle` configuration
  Map parseFile(File gradleFile) {
    if (!gradleFile.exists()) {
      return [:]
    }
    def fileReader = new BufferedReader(new FileReader(gradleFile))
    def result = [:]
    for (def line = fileReader.readLine(); line != null; line = fileReader.readLine()) {
      def versionMatch = line.trim() =~ /^version ?= ?'([\w.-]+)'$/
      def groupMatch = line.trim() =~ /^group ?= ?'([\w.]+)'$/
      if (versionMatch.size() == 1 && versionMatch[0].size() == 2) {
        result.version = versionMatch[0][1]
      }
      if (groupMatch.size() == 1 && groupMatch[0].size() == 2) {
        result.group = groupMatch[0][1]
      }
    }
    fileReader.close()
    return result
  }
}

class FileWithPackageParser {
  // Returns package of the file (looks for "package [...]" string)
  String parseFile(File file) {
    def fileReader = new BufferedReader(new FileReader(file))
    def fileContent = ""
    while ((fileContent = fileReader.readLine()) != null) {
      def match = fileContent =~ /^package\s+([0-9a-zA-Z._]*);?$/
      if (match.size() == 1 && match[0].size() == 2) {
        fileReader.close()
        return match[0][1]
      }
    }
    fileReader.close()

    throw new GradleException("Java or Kotlin file $file does not include package declaration")
  }
}

class Unimodule {
  private final static String DEFAULT_VERSION = "0.0.0"
  private final static String DEFAULT_ANDROID_SUBDIRECTORY = "android"
  private final static List PACKAGES_GLOBS = ["*Package.java", "*Package.kt"]

  File directory

  private final Map buildGradleData
  private final Map packageJsonData
  private final Map unimoduleJsonData

  Unimodule(File directory) {
    this.directory = directory
    def unimoduleConfigFile = new File(directory, 'unimodule.json')
    this.unimoduleJsonData = new JsonSlurper().parseText(unimoduleConfigFile.text)

    def packageJsonFile = new File(directory, 'package.json')
    this.packageJsonData = new JsonSlurper().parseText(packageJsonFile.text)

    def buildGradleFile = new File(this.getAndroidDirectory(), 'build.gradle')
    this.buildGradleData = new BuildGradleParser().parseFile(buildGradleFile)
  }

  String getName() { unimoduleJsonData.name ?: packageJson.name }

  String getVersion() {
    VersionNumber.parse(buildGradleData.version ?: packageJson.version ?: DEFAULT_VERSION)
  }
  
  List getTargets() { unimoduleJsonData.targets ?: [] }
  
  List getPlatforms() { unimoduleJsonData.platforms ?: [] }

  List getAndroidPackages() {
    if (unimoduleJsonData.android?.packages != null) {
      return unimoduleJsonData.android?.packages
    }

    // Find Android Packages
    PACKAGES_GLOBS
      .collect { new FileNameFinder().getFileNames(androidDirectory.toString(), "src/**/$it") }
      .flatten()
      .collect {
        File packageFile = new File(it)
        def packageName = new FileWithPackageParser().parseFile(packageFile)
        def className = packageFile.getName().split(Pattern.quote("."))[0]
        return "$packageName.$className"
      }
  }

  File getAndroidDirectory() {
    new File(directory, unimoduleJsonData.android?.subdirectory ?: DEFAULT_ANDROID_SUBDIRECTORY)
  }

  boolean supportsPlatform(String platform) {
    platforms instanceof List && platforms.contains(platform)
  }

  boolean supportsTarget(String target) {
    targets.size() == 0 || targets.contains(target)
  }
}

class BasePackageListGenerator {
  private File directory
  private String packageName
  private List<Unimodule> unimodules

  BasePackageListGenerator(File projectDir, List<Unimodule> unimodules) {
    def mainApplicationFile = findMainApplicationFile(projectDir)
    def mainApplicationDirectory = mainApplicationFile.parentFile

    this.packageName = new FileWithPackageParser().parseFile(mainApplicationFile) + ".generated"
    this.directory = new File(mainApplicationDirectory, "generated")
    this.unimodules = unimodules
  }

  BasePackageListGenerator(File directory, String packageName, List<Unimodule> unimodules) {
    this.unimodules = unimodules
    this.directory = directory
    this.packageName = packageName
  }

  File save() {
    File javaFile = new File(directory, "BasePackageList.java")
    javaFile.parentFile.mkdirs()
    javaFile.createNewFile()
    def javaFileWriter = new BufferedWriter(new FileWriter(javaFile))
    javaFileWriter.write(generate())
    javaFileWriter.close()
    return javaFile
  }

  String generate() {
    List<String> packagesFqdns = unimodules.collect { u -> u.androidPackages }.flatten()

    String packageList = "Collections.emptyList();"
    if (packagesFqdns.size() > 0) {
      packageList = """Arrays.<Package>asList(
    new ${packagesFqdns.join("(),\n      new ")}()
    );"""
    }

"""package ${packageName};

import java.util.${packagesFqdns.size() > 0 ? "Arrays" : "Collections"};
import java.util.List;
import org.unimodules.core.interfaces.Package;

public class BasePackageList {
  public List<Package> getPackageList() {
    return ${packageList}
  }
}
"""
  }

  private static File findMainApplicationFile(File projectDir) {
    String searchPath = projectDir.getPath()
    List<String> fileGlobs = ['**/MainApplication.java', '**/MainApplication.kt']
    List<String> mainApplicationFiles = fileGlobs.collect { fileGlob ->
      new FileNameFinder().getFileNames(searchPath, fileGlob)
    }.flatten()

    if (mainApplicationFiles.size() == 0) {
      throw new GradleException("You need to have a MainApplication in your project.\n" +
          "No results found while searching for $fileGlobs in '$searchPath'.")
    }

    if (mainApplicationFiles.size() > 1) {
      throw new GradleException("Multiple MainApplication files found in your project: $mainApplicationFiles.\n" +
          "Try specifying `package` and `directory` for generated list in `build.gradle`.")
    }

    return new File(mainApplicationFiles[0])
  }
}

class UnimodulesFinder {
  private static final PLATFORM = "android"

  private List modulesDirectories
  private List modulesNamesToExclude = []
  private String target
  private String platform = PLATFORM

  List getAll() {
    modulesDirectories
      .collect { new FileNameFinder().getFileNames(it.toString(), '**/unimodule.json') }
      .flatten()
      .collect { new Unimodule(new File(it).parentFile) }
  }

  List getFiltered() {
    getAll()
      .findAll { it.supportsTarget(target) && it.supportsPlatform(platform) }
      .findAll { !modulesNamesToExclude.contains(it.name) }
  }

  List getInstallable() {
    getFiltered().groupBy { it.name }.collect {
      entry -> entry.value.sort { it.version }.last()
    }
  }

  List getDuplicates() {
    Set knownUnimodulesNames = new HashSet()
    Set duplicateUnimodules = new HashSet()
    for (unimodule in getAll()) {
      if (knownUnimodulesNames.contains(unimodule.name)) {
        duplicateUnimodules.add(unimodule)
      }
      knownUnimodulesNames.add(unimodule.name)
    }
    return new ArrayList(duplicateUnimodules)
  }
}

ext.addUnimodulesDependencies = { Map customOptions = [:] ->
  def options = [
      modulesPaths   : ['../../node_modules'],
      configuration  : 'implementation',
      target         : 'react-native',
      listPackageName: null,
      listDirectory  : null,
      exclude        : [],
      installClosure : { unimodule, configuration ->
        Object dependency = project.project(':' + unimodule.name)
        project.dependencies.add(configuration, dependency)
      },
      skipValidation : false,
      skipPackageListGeneration: false,
      projectDir : getProjectDir()
  ] << customOptions

  if (!options.skipValidation) {
    if (!(new File(rootProject.projectDir.parentFile, 'package.json').exists())) {
      // There's no package.json
      throw new GradleException(
        "'addUnimodulesDependencies()' is being used in a project that doesn't seem to be a React Native project."
      )
    }
  }

  UnimodulesFinder unimodulesFinder = new UnimodulesFinder(
    modulesDirectories: options.modulesPaths.collect { path -> new File(options.projectDir, path) },
    modulesNamesToExclude: options.exclude,
    target: options.target
  )

  List<Unimodule> installedUnimodules = unimodulesFinder.installable.findAll { unimodule ->
    String description =
      Colors.GREEN + unimodule.name + Colors.YELLOW + '@' + Colors.RED + unimodule.version + Colors.NORMAL + ' from ' + Colors.MAGENTA + unimodule.directory + Colors.NORMAL
    Exception savedException = null
    boolean hasBeenInstalled = false
    try {
      hasBeenInstalled = options.installClosure(unimodule, options.configuration)
    } catch (Exception e) {
      savedException = e
    } finally {
      if (hasBeenInstalled) {
        println "Installed : $description"
      } else if (savedException) {
        println "   Failed : $description ($savedException.message)"
      } else {
        println "   Failed : $description"
      }
    }
    return hasBeenInstalled
  }

  def duplicateUnimodules = unimodulesFinder.duplicates
  if (duplicateUnimodules.size() > 0) {
    println()
    println Colors.YELLOW + 'Found some duplicated unimodule packages. Installed the ones with the highest version number.' + Colors.NORMAL
    println Colors.YELLOW + 'Make sure following dependencies of your project are resolving to one specific version:' + Colors.NORMAL

    println ' ' + duplicateUnimodules
        .collect { Colors.GREEN + it.name + "@" + it.version + Colors.NORMAL }
        .join(', ')
  }

  if (unimodulesFinder.all.size() == 0) {
    println()
    println Colors.YELLOW + "No installable unimodules found. Are you sure you've installed JS dependencies?" + Colors.NORMAL
  }

  if (!options.skipPackageListGeneration) {
    BasePackageListGenerator generator = null
    if (options.listPackageName == null || options.listDirectory == null) {
      println()
      println Colors.YELLOW + "Inferring BasePackageList location and package from MainApplication..." + Colors.NORMAL
      generator = new BasePackageListGenerator(options.projectDir, installedUnimodules)
    } else {
      generator = new BasePackageListGenerator(
        new File(projectDir, options.listDirectory),
        options.listPackageName,
        installedUnimodules,
      )
    }

    def file = generator.save()
    println()
    println "Unimodules' packages list " + Colors.YELLOW + generator.packageName + ".BasePackageList" + Colors.NORMAL + " generated and saved at $Colors.YELLOW$file.path$Colors.NORMAL"
  }
  println()
}

ext.includeUnimodulesProjects = { Map customOptions = [:] ->
  Map options = [
    modulesPaths: ['../../node_modules'],
    target      : 'react-native',
    exclude     : [],
  ] << customOptions

  List<File> modulesDirectories = options.modulesPaths.collect { new File(rootProject.buildFile, it) }

  new UnimodulesFinder(
    target: options.target,
    modulesDirectories: modulesDirectories,
    modulesNamesToExclude: options.exclude,
  ).installable.collect {
    include ":${it.name}"
    project(":${it.name}").projectDir = it.androidDirectory
    it
  }
}

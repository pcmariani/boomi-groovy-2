import groovy.json.JsonSlurper
import groovy.json.JsonOutput;

class BoomiGroovy {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'BoomiGroovy [-h] -t file -s script [-d document]')

    cli.with {
      h  longOpt: 'help', 'Show usage'
      t  longOpt: 'testfile', args: 1, argName: 'testFile', 'test file'
      m  longOpt: 'mode', args: 1, argName: 'mode', 'Mode'
      i  longOpt: 'init', args: 1, argName: 'init', 'Init'
      xa longOpt: 'suppress-all-output', type: boolean, 'Suppress all output'
      xd longOpt: 'suppress-data-output', type: boolean, 'Suppresses output of data'
      xp longOpt: 'suppress-props-output', type: boolean, 'Suppresses output of props'
      w  longOpt: 'working-dir', args: 1, argName: 'dir', 'Present Working Directory'
    }

    def options = cli.parse(args)

    if (options.h || !options.t || !options) {
      cli.usage()
      return
    }

    // TODO: maybe - don't println inline. Capture println statements
    // TODO: change datafile, propsfile to just data: file, props: file
    // TODO: allow assertions to have descriptions
    // TODO: allow include file in assertions
    // TODO: allow not just assert, but also assertEquals, etc... if "assert" eval after "assert", else eval all
    // TODO: ALMOST DONE boomi init json myNewScript.groovy
    //    TODO: put template files in templates/json, templates/xml, etc...
    // TODO: DONE discover and run all testsuites recursively
    // TODO: dataContext.writeFile - remove html boilerplate (should be in source output)
    // TODO: make sure print works even if StoreStream is commented

    // --- start v2 --- //

    if (options.i) {
      println "HELLO"
      def initFile = options.i
      Init.createFiles(options.w, initFile.replaceAll(" ", "_"))
      System.exit(0)
    }

    LinkedHashMap OPTS = [
      workingDir: options.w ?: System.getProperty("user.dir"),
      testSuiteFileName: options.t,
      printMode: options.m
    ]

    TestSuiteRunner testSuiteRunner = new TestSuiteRunner(OPTS)

    testSuiteRunner.discoverAndRunTestSuites()
    // if (OPTS.testSuiteFileName) {
    //   testSuiteRunner.runTestSuite()
    // } else {
    //   testSuiteRunner.discoverAndRunTestSuites()
    // }

    // testSuiteRunner.printResultsTemp()

    // if (mode == "testResultsOnly") {
    //   testSuiteRunner.printResults()
    // }

    // --- end v2 --- //

    // // --- start v1 --- //

    // String workingDir = options.w ?: System.getProperty("user.dir")
    // ArrayList testFiles = [options.t ? options.t : null]
    //
    // ExecutionManager executionManager = new ExecutionManager(workingDir)
    //
    // testFiles.each { testFile ->
    //   executionManager.runTestSuite(options.m, testFile)
    // }
    //
    // if (options.m == "testResultsOnly") {
    //   executionManager.printResults()
    // }

    // // --- end v1 --- //

  }

  static String pprettyJson(def thing) {
    return JsonOutput.prettyPrint(JsonOutput.toJson(thing))
  }
}

class Fmt {
  static String json(def thing) {
    return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(thing))
  }
  static void pl(def color, def str) {
    println Color[color] + str + Color.off
  }
  static void p(def color, def str) {
    print Color[color] + str + Color.off
  }
}

class Color {
  static String red = "${(char)27}[31m"
  static String green = "${(char)27}[32m"
  static String yellow = "${(char)27}[33m"
  static String blue = "${(char)27}[34m"
  static String magenta = "${(char)27}[35m"
  static String cyan = "${(char)27}[36m"
  static String grey = "${(char)27}[90m"
  static String white = "${(char)27}[97m"
  static String redReverse = "${(char)27}[31;7m"
  static String greenReverse = "${(char)27}[32;7m"
  static String off = "${(char)27}[39;49;27m"
}


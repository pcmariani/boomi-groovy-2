import groovy.json.JsonSlurper
import groovy.json.JsonOutput;

class BoomiGroovy {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'BoomiGroovy [-h] -t file')

    cli.with {
      m  longOpt: 'mode', args: 1, argName: 'mode', 'Mode'

      // test mode
      h  longOpt: 'help', 'Show usage'
      w  longOpt: 'working-dir', args: 1, argName: 'dir', 'Present Working Directory'
      t  longOpt: 'test-suite-file', args: 1, argName: 'test-suite-file', 'Test Suite File'

      // Run mode
      h  longOpt: 'help', 'Show usage'
      w  longOpt: 'working-dir', args: 1, argName: 'dir', 'Present Working Directory'
      t  longOpt: 'test-suite-file', args: 1, argName: 'test-suite-file', 'Test Suite File'
      s  longOpt: 'script', args: 1, argName: 'script', 'If not using a testsuite file: Script Filename'
      d  longOpt: 'document', args: 1, argName: 'document', 'If not using a testsuite file: Document Filename'
      p  longOpt: 'properties', args: 1, argName: 'properties', 'If not using a testsuite file: Properties Filename'
      xd longOpt: 'suppress-data-output', type: boolean, 'Suppress data output (can also be done inside OPTIONS in a testsuite file)'
      xp longOpt: 'suppress-props-output', type: boolean, 'Suppresses props output (can also be done inside OPTIONS in a testsuite file)'

      // init mode
      h  longOpt: 'help', 'Show usage'
      w  longOpt: 'working-dir', args: 1, argName: 'dir', 'Present Working Directory'
      s  longOpt: 'script', args: 1, argName: 'script-name', 'Name of new script'
      l  longOpt: 'lang', args: 1, argName: 'lang', 'Language of input: xml, json, ff(default)'
    }

    def options = cli.parse(args)

    if (options.h || !options) {
      cli.usage()
      return
    }
    println "options.t: " + options.t
    println "options.arguments(): " + options.arguments()

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
    // NOTE: catch exception when @file isn't found and provide friendly message
    // TODO: props in testSuiteFile override props in props file

    // --- start v2 --- //

    // if (options.i) {
    //   println "HELLO"
    //   def initFile = options.i
    //   Init.createFiles(options.w, initFile.replaceAll(" ", "_"))
    //   System.exit(0)
    // }

    // def workingDir = options.w ?: System.getProperty("user.dir")
    // def testSuiteFileName = options.t
    // def mode = options.m

    LinkedHashMap OPTS = [
      workingDir: options.w ?: System.getProperty("user.dir"),
      testSuiteFileName: options.t,
      mode: options.m == "test" ? "testResultsOnly" : options.m
    ]

    TestSuiteRunner testSuiteRunner = new TestSuiteRunner(OPTS)

    // testSuiteRunner.discoverAndRunTestSuites()

    if (OPTS.testSuiteFileName) {
      println "test or run mode; has file"
      // testSuiteRunner.runTestSuite()
    } else if (OPTS.mode == "testResultsOnly") {
      println "test mode; no file"
      // testSuiteRunner.discoverAndRunTestSuites()
    } else {
      println "run mode; no file"
      cli.usage()
    }


    // testSuiteRunner.printResultsTemp()

    // if (OPTS.mode == "testResultsOnly") {
    //   testSuiteRunner.printResults()
    // }

    // --- end v2 --- //

    // --- start v1 --- //

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

    // --- end v1 --- //

  }

}


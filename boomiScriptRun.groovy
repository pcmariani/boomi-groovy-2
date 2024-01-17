class boomiScriptRun {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'boomiScriptTest [-h] [testSuiteFileName]')

    cli.with {
      h  longOpt: 'help', 'Show usage'
      w  longOpt: 'working-dir', args: 1, argName: 'dir', 'Present Working Directory'
      // t  longOpt: 'test-suite-file', args: 1, argName: 'test-suite-file', 'Test Suite File'
      s  longOpt: 'script', args: 1, argName: 'script', 'If not using a testsuite file: Script Filename'
      d  longOpt: 'document', args: 1, argName: 'document', 'If not using a testsuite file: Document Filename'
      p  longOpt: 'properties', args: 1, argName: 'properties', 'If not using a testsuite file: Properties Filename'
      xd longOpt: 'suppress-data-output', type: boolean, 'Suppress data output (can also be done inside OPTIONS in a testsuite file)'
      xp longOpt: 'suppress-props-output', type: boolean, 'Suppresses props output (can also be done inside OPTIONS in a testsuite file)'
    }

    def options = cli.parse(args)

    if (options.h || !options || options.arguments().size() > 1) {
      cli.usage()
      return
    }

    def workingDir = options.workingDir ?: System.getProperty("user.dir")
    def testSuiteFileName = options.arguments()[0]

    GlobalOptions.workingDir = options.workingDir ?: System.getProperty("user.dir")
    GlobalOptions.mode = "run"

    // TestSuiteRunner testSuiteRunner = new TestSuiteRunner(GlobalOptions.workingDir, testSuiteFileName, GlobalOptions.mode)
    TestSuiteRunner testSuiteRunner = new TestSuiteRunner()

    if (testSuiteFileName) {
      // println "HAS FILE"
      testSuiteRunner.runTestSuite(testSuiteFileName)
    } else {
      testSuiteRunner.discoverAndRunTestSuites()
    }

    testSuiteRunner.printResults()

  }
}


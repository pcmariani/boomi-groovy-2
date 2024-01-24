class BoomiScriptTest {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'boomiScriptTest [-h] [testSuiteFileName]')

    cli.with {
      h  longOpt: 'help', 'Show usage'
      w  longOpt: 'workingDir', args: 1, argName: 'dir', 'Present Working Directory'
    }

    def options = cli.parse(args)

    if (options.h || !options || options.arguments().size() > 1) {
      cli.usage()
      return
    }

    String testSuiteFileName = options.arguments()[0]
    GlobalOptions.workingDir = options.workingDir ?: System.getProperty("user.dir")
    GlobalOptions.mode = "testResultsOnly"

    TestSuiteRunner testSuiteRunner = new TestSuiteRunner()

    if (testSuiteFileName) {
      testSuiteRunner.runTestSuite(testSuiteFileName)
    }
    else {
      testSuiteRunner.discoverAndRunTestSuites()
    }

    testSuiteRunner.printResults()

  }
}


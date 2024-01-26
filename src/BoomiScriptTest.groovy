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
    OPTIONS.workingDir = options.workingDir ?: System.getProperty("user.dir")
    OPTIONS.mode = "testResultsOnly"
    OPTIONS.setSuiteOptsFromMode("testResultsOnly")

    // println OPTIONS.suiteOpts
    // OPTIONS.class.getDeclaredFields().each {println it.getName() + " " + OPTIONS."${it.getName()}"}

    TestSuiteRunner testSuiteRunner = new TestSuiteRunner()

    if (testSuiteFileName) {
      testSuiteRunner.runTestSuite(testSuiteFileName)
    }
    else {
      testSuiteRunner.discoverAndRunTestSuites()
    }

    // testSuiteRunner.printResults()
    // println OPTIONS.suiteOpts

  }
}


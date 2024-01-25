class BoomiScriptRun {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'boomiScriptRun [-h] [testSuiteFileName]'
      // + ' | \n[-h] [-s script] [-d data] [-p properties] [-xp] [-xd]'
    )

    cli.with {
      h  longOpt: 'help', 'Show usage'
      w  longOpt: 'workingDir', args: 1, argName: 'dir', 'Present Working Directory'
      // s  longOpt: 'script', args: 1, argName: 'script', 'If not using a testsuite file: Script Filename'
      // d  longOpt: 'document', args: 1, argName: 'document', 'If not using a testsuite file: Document Filename'
      // p  longOpt: 'properties', args: 1, argName: 'properties', 'If not using a testsuite file: Properties Filename'
      // xd longOpt: 'suppress-data-output', type: boolean, 'Suppress data output (can also be done inside OPTIONS in a testsuite file)'
      // xp longOpt: 'suppress-props-output', type: boolean, 'Suppresses props output (can also be done inside OPTIONS in a testsuite file)'
    }

    def options = cli.parse(args)

    if (options.h || !options || options.arguments().size() > 1) {
      cli.usage()
      return
    }

    String testSuiteFileName = options.arguments()[0]
    GlobalOptions.workingDir = options.workingDir ?: System.getProperty("user.dir")
    GlobalOptions.mode = "run"
    GlobalOptions.setSuiteOptsFromMode("run")

    // GlobalOptions.class.getDeclaredFields().each {println it.getName() + " " + GlobalOptions."${it.getName()}"}

    if (testSuiteFileName) {
      TestSuiteRunner testSuiteRunner = new TestSuiteRunner()
      testSuiteRunner.runTestSuite(testSuiteFileName)
    }
    else {
      println "Legacy?"
    }

  }
}


class BoomiScriptRun {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'boomiScriptRun [-h] [testSuiteFileName]'
      // + ' | \n[-h] [-s script] [-d data] [-p properties] [-xp] [-xd]'
    )

    cli.with {
      h   longOpt: 'help', 'Show usage'
      dbg longOpt: 'debug', type: boolean, argName: 'debug', 'Print Debug Info'
      s  longOpt: 'script', args: 1, argName: 'script', 'If not using a testsuite file: Script Filename'
      d  longOpt: 'document', args: 1, argName: 'document', 'If not using a testsuite file: Document Filename'
      p  longOpt: 'properties', args: 1, argName: 'properties', 'If not using a testsuite file: Properties Filename'
      // xd longOpt: 'suppress-data-output', type: boolean, 'Suppress data output (can also be done inside OPTIONS in a testsuite file)'
      // xp longOpt: 'suppress-props-output', type: boolean, 'Suppresses props output (can also be done inside OPTIONS in a testsuite file)'
    }

    def options = cli.parse(args)

    if (options.h || !options || options.arguments().size() > 1) {
      cli.usage()
      return
    }

    Globals Globals = new Globals(options)

    if (Globals.testSuiteFileName) {
      TestSuiteRunner tsr = new TestSuiteRunner().runTestSuite()
    }

    else {
      String testSuiteText = '''
        OPTS:
          - data
          - DPPs
          - ddps
        GLOBALS:
          DPPs: ''' + options.p + '''
          scripts:
            - ''' + options.s + '''
        Boomi Run:
          data: ''' + options.d + '''
          ddps: ''' + options.p + '''
      '''
      TestSuite ts = new TestSuite(testSuiteText)
      ts.run()
    }

  }
}


class TestSuiteRunner {
  ArrayList resultsTestSuites = []

  public void discoverAndRunTestSuites(String folder) {
    new File(OPTIONS.workingDir).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*.yaml/) {
      OPTIONS.workingDir = it.getParent()
      def testSuiteFileName = it.getName()
      runTestSuite(testSuiteFileName)
    }
  }

  public void runTestSuite(String testSuiteFileName) {
    resultsTestSuites << new TestSuite(testSuiteFileName).run()
  }

  public void printResults() {
    int numSuitesFailed = resultsTestSuites.suiteFailed.count(true)
    int numSuitesPassed = resultsTestSuites.suiteFailed.count(false)

    Fmt.p("white", "Test Suites: ") //+ resultsTestSuites.size())
    Fmt.p("green", numSuitesPassed ? numSuitesPassed + " passed" : "")
    Fmt.p("white", numSuitesPassed ? ", " : "")
    Fmt.p("red", numSuitesFailed ? numSuitesFailed + " failed" : "")
    Fmt.p("white", numSuitesFailed ? ", " : "")
    Fmt.p("white", resultsTestSuites.size() + " total")
    println ""

    int numTestsFailed = resultsTestSuites.numFailedTests.sum()
    int numTestsPassed = resultsTestSuites.numPassedTests.sum()
    int numTests = resultsTestSuites.numTests.sum()
    
    Fmt.p("white", "Tests:       ") //+ testTests.size())
    Fmt.p("green", numTestsPassed ? numTestsPassed + " passed" : "")
    Fmt.p("white", numTestsPassed ? ", " : "")
    Fmt.p("red", numTestsFailed ? numTestsFailed + " failed" : "")
    Fmt.p("white", numTestsFailed ? ", " : "")
    Fmt.p("white", numTests + " total")
    println ""
    println ""
  }

}

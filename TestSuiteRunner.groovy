class TestSuiteRunner {
  ArrayList resultsTestSuites = []
  LinkedHashMap OPTS

  TestSuiteRunner(LinkedHashMap OPTS) {
    this.OPTS = OPTS
    // println OPTS
  }

  public void discoverAndRunTestSuites(String folder) {
    new File(OPTS.workingDir).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*.yaml/) {
      OPTS.workingDir = it.getParent()
      OPTS.testSuiteFileName = it.getName()
      OPTS.printMode = "normal"
      println "TEST SUITE: " + OPTS.testSuiteFileName
      runTestSuite()
      println ""
    }
  }

  public void runTestSuite() {
    TestSuite ts = new TestSuite(this.OPTS)
    ts.run()
    resultsTestSuites << [
      suiteName: this.OPTS.testSuiteFileName,
      suiteFailed: ts.suiteFailed,
      tests: ts.tests
    ]
    // println ts.tests
    // resultsTestSuites << ts.run()
  }

  public void printResultsTemp() {
    println resultsTestSuites
  }

  public void printResults() {
    def l1 = "  "
    def l2 = "     "
    def l3 = "       "
    def l4 = "          "
    def l5 = "            "
    def l6 = "              "

    this.resultsTestSuites.each { ts ->
      if (ts.suiteFailed) {
        Fmt.p("redReverse", " FAIL ")
      } else {
        Fmt.p("greenReverse", " PASS ")
      }
      Fmt.pl("white", " " + ts.suiteName)

      println ""
      def globalScripts = getExecutionScripts(null, null, ts.globals.scriptfiles)?.name
      Fmt.pl("grey", l1 + (globalScripts.size() == 1 ? "Script" : "Scripts") )
      globalScripts.each { script ->
        Fmt.pl("magenta", l2 + script)
      }

      println ""

      // Fmt.pl("white", "  Tests")
      ts.tests.each { t ->
        print l1
        if (t.testFailed) {
          Fmt.p("red", "✗  ")
        } else {
          Fmt.p("green", "✓  ")
        }
        Fmt.pl("yellow", t.testName)

        t.documents.eachWithIndex { doc, m ->

            if (t.testError?.docIndex == m) {

              def e = t.testError
              print l3
              Fmt.pl("white", m + " " + e.docName)

              print l4 
              Fmt.p("grey", "Exception in Script:")
              // print "          "
              Fmt.pl("magenta", " " + e.script)
              print l5
              Fmt.pl("red", e.error
              .replaceAll(/\n\s*at (?!Script1).*/,"")
              .replaceFirst(/(Exception:) /, "\$1\n$l6")
              .replaceFirst(/\n.*at .*?(groovy:\d+).*/," (\$1)")
              .replaceFirst(/\n$/,"")
              )

            }

            else {

            print l3
            Fmt.pl("white", m + " " + doc.desc)

            if (doc.assertions) {
              doc.assertions.each { a ->
                print l4
                if (a.passed == true) {
                  Fmt.p("green", "✓ ")
                  Fmt.pl("grey", " " + a.assert)
                } else if (a.passed == false) {
                  Fmt.p("red", "✗ ")
                  Fmt.pl("grey", " " + a.assert)
                } else {
                  Fmt.pl("grey", "－ " + a.assert + " (not evaluated)")
                }
              }

            } else {
              print l4
              Fmt.pl("grey", "－ no assertions")
            }
          }

        }
      }
    }
    println ""

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

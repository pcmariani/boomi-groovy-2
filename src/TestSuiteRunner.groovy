class TestSuiteRunner {
  ArrayList resultsTestSuites = []

  public void discoverAndRunTestSuites(String folder) {
    new File(Globals.workingDir).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*.yaml/) {
      Globals.workingDir = it.getParent()
      Globals.testSuiteFileName = it.getName()
      runTestSuite()
    }
  }

  public void runTestSuite() {
    TestSuite ts
    try {
      ts = new TestSuite()
      ts.run()
      resultsTestSuites << ts
    } catch(Exception e) {
      println "ERROR RUNNING TESTSUITE: " + Globals.testSuiteFileName
      // org.codehaus.groovy.runtime.StackTraceUtils.sanitize(e).printStackTrace()
      org.codehaus.groovy.runtime.StackTraceUtils.sanitize(e)
      // println e.toString().replaceAll("Exception: ", "Exception: \n")
      // Fmt.pl("red", e.toString().replaceAll("Exception: ", "Exception: \n"))
      println e.getMessage()
      if (Globals.debug) println "     at " + e.stackTrace.head()
      // println e.printStackTrace()
    }
  }

  public void printResults() {

    resultsTestSuites.each { ts ->

      if (ts.suiteFailed) {
        Fmt.p("redReverse", " FAIL ")
      } else {
        Fmt.p("greenReverse", " PASS ")
      }
      Fmt.pl("white", " " + Globals.testSuiteFileName)

      println ""

      def globalScripts = Globals.scripts.collect { it instanceof LinkedHashMap ? it.keySet()[0] : it}
      Fmt.pl("grey", Fmt.l1 + (globalScripts.size() == 1 ? "Script" : "Scripts") )

      globalScripts.each { script ->
        Fmt.pl("magenta", Fmt.l2 + script)
      }

      println ""

      // Fmt.pl("white", "  Tests")

      ts.tests.each { t ->
        print Fmt.l1
        if (t.testFailed) {
          Fmt.p("red", "✗  ")
        } else {
          Fmt.p("green", "✓  ")
        }
        Fmt.pl("yellow", t.desc)
        // println t.dataContext.dataContextArr

        t.dataContext.dataContextArr.eachWithIndex { doc, m ->

          if (t.execError?.docIndex == m) {

            def e = t.execError

            print Fmt.l3
            Fmt.pl("white", m + " " + e.docName)

            print Fmt.l4
            Fmt.p("grey", "Exception in Script:")
            Fmt.pl("magenta", " " + e.script)

            print Fmt.l5
            Fmt.pl("red", e.error
              .replaceAll(/\n\s*at (?!Script1).*/,"")
              .replaceFirst(/(Exception:) /, "\$1\n$Fmt.l6")
              .replaceFirst(/\n.*at .*?(groovy:\d+).*/," (\$1)")
              .replaceFirst(/\n$/,"")
            )

          }

          else {

            print Fmt.l3
            Fmt.pl("white", m + " " + doc.desc)

            if (doc.assertions) {
              doc.assertions.each { a ->
                print Fmt.l4
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
              print Fmt.l4
              Fmt.pl("grey", "－ no assertions")
            }
          }

        }
      }
      println ""

    }


    if (resultsTestSuites) {
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

}

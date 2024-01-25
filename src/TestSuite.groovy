@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml
// import groovy.json.JsonSlurper

class TestSuite {
  LinkedHashMap testSuiteFileRaw
  LinkedHashMap GLOBALS
  ArrayList tests
  Boolean suiteFailed
  int numTests
  int numPassedTests
  int numFailedTests
  String testSuiteFileName

  TestSuite(String testSuiteFileName) {
    this.testSuiteFileName = testSuiteFileName

    def testSuiteFilePath = "${GlobalOptions.workingDir}/${testSuiteFileName}"

    // if (GlobalOptions.testSuiteFileExt == "yaml") {
    this.testSuiteFileRaw = new Yaml().load((testSuiteFilePath as File).text)
    // } else if (GlobalOptions.testSuiteFileExt == "json") {
    //   this.testSuiteFileRaw = new JsonSlurper.parseText((testSuiteFilePath as File).text)
    // }

    GlobalOptions.suiteOpts.addAll(testSuiteFileRaw.remove('OPTIONS') ?: [])

    // GlobalOptions.suiteOpts << testSuiteFileRaw.remove('OPTIONS') ?: [:]
    this.GLOBALS = parseGlobals(testSuiteFileRaw.remove('GLOBALS')) ?: [:]
    LinkedHashMap g = this.GLOBALS

    def scripts = g.scripts ?: g.script
    GlobalOptions.scripts = scripts instanceof String ? [scripts] : scripts
    GlobalOptions.processProps = g.DPPS ?: g.dpps
    GlobalOptions.testFilesDir = g.testFilesDir ?: "."

    // GlobalOptions.class.getDeclaredFields().each {println it.getName() + " " + GlobalOptions."${it.getName()}"}

    this.tests = []
    testSuiteFileRaw.eachWithIndex { testRaw, index ->

      TestMapper mappedTest = new TestMapper(GLOBALS, testRaw, index)
      mappedTest.transformTestYaml()

      this.tests << new Test(
        mappedTest.desc,
        mappedTest.scripts,
        mappedTest.dpps,
        mappedTest.dataContext,
        mappedTest.index
      )
    }
  }

  private LinkedHashMap parseGlobals(GLOBALS) {
    def globals = [:]
    globals.scriptfiles = GLOBALS.scripts ?: GLOBALS.s
    if (globals.scriptfiles instanceof String) {
      globals.scriptfiles = [globals.scriptfiles]
    }
    globals.ProcessProps = GLOBALS.'process-props' ?: GLOBALS.dpps
    globals.DPPsFile = GLOBALS.processPropsFile ?: GLOBALS.dppsFile
    globals.testfilesDir = GLOBALS.testfilesDir ?: GLOBALS.tfDir ?: "."
    return globals
  }

  public def run() {

    tests.each { test ->
      test.run()
    }

    this.numFailedTests = tests.testFailed.count(true)
    this.numPassedTests = tests.testFailed.count(false)
    this.numTests = tests.testFailed.size()
    this.suiteFailed = numFailedTests > 0 ? true : false

    if (GlobalOptions.mode == "testResultsOnly") {
      // this.printResult()
    }

    return this
  }

  public def printResult() {
    if (this.suiteFailed) {
      Fmt.p("redReverse", " FAIL ")
    } else {
      Fmt.p("greenReverse", " PASS ")
    }
    Fmt.pl("white", " " + testSuiteFileName)

    println ""

    def globalScripts = this.GLOBALS.scriptfiles.collect { it instanceof LinkedHashMap ? it.keySet()[0] : it}
    Fmt.pl("grey", Fmt.l1 + (globalScripts.size() == 1 ? "Script" : "Scripts") )
    globalScripts.each { script ->
      Fmt.pl("magenta", Fmt.l2 + script)
    }

    println ""

    // Fmt.pl("white", "  Tests")

    this.tests.each { t ->
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

          def e = t.testError

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


}

// import org.codehaus.groovy.runtime.StackTraceUtils 
@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml
// import groovy.json.JsonSlurper

class TestSuite {
  LinkedHashMap testSuiteFileRaw
  ArrayList tests
  Boolean suiteFailed
  int numTests
  int numPassedTests
  int numFailedTests
  String testSuiteFileName

  TestSuite(rawTestSuiteText="") {
    this.testSuiteFileName = Globals.testSuiteFileName

    def testSuiteFilePath = "${Globals.workingDir}/${testSuiteFileName}"

    // try {
      // if (Globals.testSuiteFileExt == "yaml") {
    if (!rawTestSuiteText) {
      this.testSuiteFileRaw = new Yaml().load((testSuiteFilePath as File).text)
    } else {
      this.testSuiteFileRaw = new Yaml().load(rawTestSuiteText)
    }
      // } else if (Globals.testSuiteFileExt == "json") {
      //   this.testSuiteFileRaw = new JsonSlurper.parseText((testSuiteFilePath as File).text)
      // }
    // } catch(Exception e) {
    //   org.codehaus.groovy.runtime.StackTraceUtils
    //     .sanitize(e).printStackTrace()
    //   // StackTraceUtils.sanitize(e)
    //   // e.stackTrace.head().lineNumber
    //   // Globals.debug ? e.printStackTrace() : "BAD YAML"
    //   // throw new Exception(Globals.debug ? e.getMessage() : "BAD YAML")
    //   // throw new Exception("BAD YAML")
    // }

    def optsRaw = testSuiteFileRaw.find{ it.key =~ /(?i)^_?opt[a-z]*s$/ }
    if (optsRaw) {
      testSuiteFileRaw.remove(optsRaw.key)
      Globals.optsMap.putAll(OptsHelper.processOpts(optsRaw.value))
    }
    else {
      throw new Exception("No Options found. Please include '_OPTS' at the top of your test file.")
    }
    // println Globals.optsMap

    // LinkedHashMap g = testSuiteFileRaw.remove('GLOBALS') ?: testSuiteFileRaw.remove('GLOBAL') ?: [:]

    def globalsRaw = testSuiteFileRaw.find{ it.key =~ /(?i)^_?globals$/ }
    if (globalsRaw) {
      testSuiteFileRaw.remove(globalsRaw.key)
      def g = globalsRaw.value
      // def scripts = g.scripts ?: g.script

      def scripts = g.find{ it.key =~ /(?i)^scripts?$/ } ?: [:]
      if (scripts) {
        // g.remove(scripts.key)
        Globals.scripts = scripts.value instanceof String ? [scripts.value] : scripts.value
      }

      def dpps = g.find{ it.key =~ /(?i)^dpps$/ } ?: [:]
      if (dpps) {
        // testSuiteFileRaw.remove(dpps.key)
        Globals.DPPs = dpps.value
      }


      // Globals.scripts = scripts instanceof String ? [scripts] : scripts
      // Globals.DPPs = g.DPPs ?: g.dpps
      // Globals.DPPsOverride = g.DPPsOverride ?: g.dppsOverride
      // Globals.testFilesDir = g.testFilesDir ?: "."
    }


    def scripts = testSuiteFileRaw.find{ it.key =~ /(?i)^scripts?$/ } ?: [:]
    if (scripts) {
      testSuiteFileRaw.remove(scripts.key)
      Globals.scripts = scripts.value instanceof String ? [scripts.value] : scripts.value
    }

    def dpps = testSuiteFileRaw.find{ it.key =~ /(?i)^dpps$/ } ?: [:]
    if (dpps) {
      testSuiteFileRaw.remove(dpps.key)
      Globals.DPPs = dpps.value
    }

    // Globals.class.getDeclaredFields().each {println it.getName() + " " + Globals."${it.getName()}"}

    this.tests = []
    testSuiteFileRaw.eachWithIndex { testRaw, index ->

      TestMapper mappedTest = new TestMapper(testRaw, index)
      // try {
        mappedTest.transformTestYaml()
      // } catch(Exception e) {
      //   throw new Exception("Test-suite file validation: \n    " + e.getMessage())
      //   println "THERE WAS A BIG ERROR"
      // }


      this.tests << new Test(
        mappedTest.desc,
        mappedTest.scripts,
        mappedTest.dpps,
        mappedTest.dataContext,
        mappedTest.index
      )
    }
  }


  public void run() {
    tests.each { test ->
      test.run()
    }
    this.numFailedTests = tests.testFailed.count(true)
    this.numPassedTests = tests.testFailed.count(false)
    this.numTests = tests.testFailed.size()
    this.suiteFailed = numFailedTests > 0 ? true : false
  }

}

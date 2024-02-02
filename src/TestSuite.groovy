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

  TestSuite() {
    this.testSuiteFileName = Globals.testSuiteFileName

    def testSuiteFilePath = "${Globals.workingDir}/${testSuiteFileName}"

    // try {
      // if (Globals.testSuiteFileExt == "yaml") {
      this.testSuiteFileRaw = new Yaml().load((testSuiteFilePath as File).text)
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


    // Globals.options.addAll(testSuiteFileRaw.remove('OPTIONS') ?: testSuiteFileRaw.remove('OPTS')?: [])
    def optsRaw = testSuiteFileRaw.remove('OPTIONS') ?: testSuiteFileRaw.remove('OPTS')?: []
    // Globals.optsMap.putAll(setOpts(optsRaw))
    Globals.optsMap.putAll(OptsHelper.processOpts(optsRaw))

    // Globals.options.addAll(optsRaw)


    LinkedHashMap g = testSuiteFileRaw.remove('GLOBALS') ?: testSuiteFileRaw.remove('GLOBAL') ?: [:]
    def scripts = g.scripts ?: g.script

    Globals.scripts = scripts instanceof String ? [scripts] : scripts
    Globals.DPPs = g.DPPs ?: g.dpps
    Globals.DPPsOverride = g.DPPsOverride ?: g.dppsOverride
    Globals.testFilesDir = g.testFilesDir ?: "."

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

@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml
import groovy.json.JsonSlurper
import groovy.json.JsonOutput;

class ExecutionManager {
  ArrayList testSuites = []
  String workingDir

  ExecutionManager(String workingDir) {
    this.workingDir = workingDir
  }

  public void runTestSuite(String printMode, String testFile) {

    LinkedHashMap testSuite = [suiteName:testFile, suiteFailed: false]

    def testsFileRoot = new Yaml().load(("$workingDir/$testFile" as File).text)
    LinkedHashMap OPTIONS = testsFileRoot.remove('OPTIONS')
    LinkedHashMap GLOBALS = parseGlobals(testsFileRoot.remove('GLOBALS'))
    // println GLOBALS

    OPTIONS.printMode = printMode

    ArrayList tests = []

    testsFileRoot.each { testYaml ->
      tests << runTest(OPTIONS, GLOBALS, testYaml)

      testSuite.numFailedTests = tests.testFailed.count(true)
      testSuite.numPassedTests = tests.testFailed.count(false)
      testSuite.numTests = tests.testFailed.size()

      if (true in tests.testFailed) {
        testSuite.suiteFailed = true
      }
    }
    testSuite.tests = tests
    
    testSuites << testSuite
  }

  void printResults() {
    println ""
    this.testSuites.each { ts ->
      if (ts.suiteFailed) {
        Fmt.p("redReverse", " FAIL ")
      } else {
        Fmt.p("greenReverse", " PASS ")
      }
      Fmt.pl("white", " " + ts.suiteName)

      ts.tests.each { t ->
        print "  "
        if (t.testFailed) {
          Fmt.p("red", "✗ ")
        } else {
          Fmt.p("green", "✓ ")
        }
        Fmt.pl("white", " " + t.testName)

        t.documents.each { doc ->
          print "      "
          Fmt.pl("white", " " + doc.desc)
          if (doc.assertions) {
            doc.assertions.each { a ->
              print "         "
              if (a.passed) {
                Fmt.p("green", "✓ ")
              } else {
                Fmt.p("red", "✗ ")
              }
              Fmt.pl("grey", " " + a.assert)
            }
          } else {
              print "         "
              Fmt.pl("grey", "－ no assertions")
          }
          
        }
      }
    }
    println ""

    int numSuitesFailed = testSuites.suiteFailed.count(true)
    int numSuitesPassed = testSuites.suiteFailed.count(false)

    Fmt.p("white", "Test Suites: ") //+ testSuites.size())
    Fmt.p("green", numSuitesPassed ? numSuitesPassed + " passed" : "")
    Fmt.p("white", numSuitesPassed ? ", " : "")
    Fmt.p("red", numSuitesFailed ? numSuitesFailed + " failed" : "")
    Fmt.p("white", numSuitesFailed ? ", " : "")
    Fmt.p("white", testSuites.size() + " total")
    println ""

    int numTestsFailed = testSuites.numFailedTests.sum()
    int numTestsPassed = testSuites.numPassedTests.sum()
    int numTests = testSuites.numTests.sum()
    
    Fmt.p("white", "Tests:       ") //+ testTests.size())
    Fmt.p("green", numTestsPassed ? numTestsPassed + " passed" : "")
    Fmt.p("white", numTestsPassed ? ", " : "")
    Fmt.p("red", numTestsFailed ? numTestsFailed + " failed" : "")
    Fmt.p("white", numTestsFailed ? ", " : "")
    Fmt.p("white", numTests + " total")
    println ""
    println ""
  }

  void peekT(test) {
    println Color.white + test.desc + Color.off
    // println test.testfilesDir
    println Color.off + "    script" + (test.scripts.size() > 1 ? "s" : "" ) + ":" + Color.off
    test.scripts.each { script -> println "        " + Color.green + script.name + Color.off }
    println Color.off + "    documents:" + Color.off

    test.dataContext.eachWithIndex { dc, n ->
      println "        " + Color.magenta + dc.getDesc(n) + Color.off
      // if (dci.assertions) println Color.grey + "            assertions:" + Color.off
      dc.getAssertions(n).each { assertion -> println "            " + Color.grey + assertion.assert + Color.off }
    }


    // test.each {
    //   println it.key + " --- " + it.value.getClass()
    // }
    println "------------"
  }


  public def runTest(OPTIONS, GLOBALS, testYaml) {
      def t = parseTestYaml(GLOBALS, testYaml)
      // peekT(t)
      def testResults = new Test(OPTIONS, t.desc, t.scripts, t.dpps, t.dataContext).run()
      def dc = testResults.dataContext

      return [
        testName: t.desc,
        testFailed: dc.hasFailedAssertions,
        testScripts: t.scripts.name,
        documents: dc.dataContextArr.collect{ it.subMap(["desc", "assertions"])}
      ]
  }


  LinkedHashMap parseGlobals(GLOBALS) {
    def global = [:]
    global.scriptfiles = GLOBALS.scripts ?: GLOBALS.s
    if (global.scriptfiles instanceof String) {
      global.scriptfiles = [global.scriptfiles]
    }
    global.ProcessProps = GLOBALS.'process-props' ?: GLOBALS.dpps
    global.DPPsFile = GLOBALS.processPropsFile ?: GLOBALS.dppsFile
    global.testfilesDir = GLOBALS.testfilesDir ?: GLOBALS.tfDir ?: ""
    return global
  }
  

  private def parseTestYaml(GLOBALS, testYaml) {
    def desc = testYaml.key
    def test = testYaml.value

    def dataContext = new DataContext2()

    if (test instanceof String) {

      dataContext.storeStream(
        test,
        getDocumentContents(null, "${GLOBALS.testfilesDir}/${test}.dat"),
        loadProps("ddp", null, null, "${GLOBALS.testfilesDir}/${test}.properties"),
        null,
        null
      )

      return [
      desc: desc,
      scripts: getExecutionScripts(null, GLOBALS.scriptfiles),
      dpps: loadProps(
        "DPP",
        null,
        GLOBALS.ProcessProps,
        GLOBALS.DPPsFile ? "$GLOBALS.testfilesDir/$GLOBALS.DPPsFile"
        : "${GLOBALS.testfilesDir}/${test}.properties" ?: null
      ),
      dataContext: dataContext,
      assertions: null,
      testfilesDir: GLOBALS.testfilesDir ?: ""
      ]
    }

    else {

      if (!test.docs) {
        test.docs = [test.clone()]
        test.remove("assertions")
        test.remove("assert")
        test.remove("a")
      }

      test.docs.eachWithIndex { doc, m ->
        def tfd = doc.testfilesDir ?: doc.tfDir ?: GLOBALS.testfilesDir

        dataContext.storeStream(
          doc.desc ?: doc.files ?: doc.f ?: doc.datafile ?: doc.df ?: "Document " + m,
          getDocumentContents(
            doc.data ?: doc.d ?: null,
            doc.files ? "$tfd/${doc.files}.dat"
            : doc.f ? "$tfd/${doc.f}.dat"
            : doc.datafile ? "$tfd/$doc.datafile"
            : doc.df ? "$tfd/$doc.df"
            : null
          ),
          loadProps(
            "ddp",
            null,
            doc.props ?: doc.p ?: null,
            doc.files ? "$tfd/${doc.files}.properties"
            : doc.f ? "$tfd/${doc.f}.properties"
            : doc.propsfile ? "$tfd/$doc.propsfile"
            : doc.pf ? "$tfd/$doc.pf"
            : null
          ),
          getAssertions(
            doc.assert ?: doc.a ?: null,
            test.assert ?: test.a ?: null
          ),
          doc.ext ?: doc.e ?: doc.extension ?: test.ext ?: test.e ?: test.extension ?: null
        )
      }

      def tfd = test.testfilesDir ?: test.tfDir ?: GLOBALS.testfilesDir

      return [
        desc: desc,
        scripts: getExecutionScripts(
          tfd,
          test.scripts ?: test.s ?: GLOBALS.scriptfiles
        ),
        dpps: loadProps(
          "DPP",
          GLOBALS.ProcessProps,
          test.'process-props' ?: test.dpps ?: null,
          test.processPropsFile ? "$tfd/$test.processPropsFile"
          : test.dppsFile ? "$tfd/$test.dppsFile"
          : test.files ? "$tfd/${test.files}.properties"
          : test.f ? "$tfd/${test.f}.properties"
          : test.propsfile ? "$tfd/$test.propsfile"
          : test.pf ? "$tfd/$test.pf"
          : GLOBALS.DPPsFile ? "$GLOBALS.testfilesDir/$GLOBALS.DPPsFile"
          : null
        ),
        dataContext: dataContext,
        testfilesDir: tfd
      ]

    }
  }



 private def getExecutionScripts(tfd, scriptfiles) {
   def scriptsArr = []
   if (scriptfiles instanceof String) {
     scriptfiles = [scriptfiles] as ArrayList
   }
   scriptfiles.eachWithIndex { scriptfile, m ->
     if (scriptfile instanceof String) {
       scriptsArr << [
         name: scriptfile,
         script: new FileInputStream("$workingDir/$scriptfile"),
         output: m == scriptfiles.size() - 1 ? ["all"] : ["xx"],
       ]
     }
     else if (scriptfile instanceof LinkedHashMap) {
       def scriptfileName = scriptfile.keySet()[0]
       def scriptArgs = scriptfile.values()[0]

       scriptsArr << [
         name: scriptfileName,
         script: new FileInputStream("$workingDir/$scriptfileName"),
         output: scriptArgs ?: [], //+ (m == scriptfiles.size() - 1 ? ["assertions"] : [])
       ]
     }
   }
   return scriptsArr
 }



 private InputStream getDocumentContents( String data, String datafile) {
   if (datafile) {
     return new FileInputStream("$workingDir/$datafile")
   } else if (data) {
     return new ByteArrayInputStream(data.getBytes("UTF-8"))
   } else {
     return new ByteArrayInputStream("".getBytes("UTF-8"))
   }
 }



 private Properties loadProps(type, globalPropsStr, propsStr, propsfile) {
   Properties properties = new Properties()
   
   // if (type == "DPP") println "DPP: " + globalPropsStr

   if (propsfile) {
     BufferedReader reader = new BufferedReader(new FileReader("$workingDir/$propsfile"));
     String line
     while ((line = reader.readLine()) != null) {
       def propArr = line.split(/\s*=\s*/, 2)
       if (line && !(line =~ /^\s*#/)) {
         if (type == "DPP"
           && !(line =~ /^\s*document\.dynamic\.userdefined\./)) {
           properties.load(new StringReader(line))
         }
         else if (type == "ddp" 
           && (line =~ /^\s*document\.dynamic\.userdefined\./)) {
           properties.load(new StringReader(line))
         }
       }
     }
     reader.close();
   }

   if (globalPropsStr) {
     if (globalPropsStr instanceof String) {
       properties.load(new StringReader(globalPropsStr))
     }
     else if (globalPropsStr instanceof LinkedHashMap) {
       properties.putAll(globalPropsStr)
     }
   }

   if (propsStr) {
     if (propsStr instanceof String) {
       properties.load(new StringReader(propsStr))
     }
     else if (propsStr instanceof LinkedHashMap) {
       properties.putAll(propsStr)
     }
   }

   if (properties) {
     String propsSubDir = propsfile ? propsfile.replaceFirst(/(.*)[\/\\].*/, "\$1") : ""
     properties.each { k,v ->
       if (v =~/@file/) {
         String filename = v.replaceFirst(/\s*@file\s*(.*?)\s*/, "\$1")
         properties.setProperty(k, new FileReader("$workingDir/$propsSubDir/$filename").text)
       }
     }
     // if (type == "DPP") properties.each { println "DPP: " + it}
     // if (type == "ddp") properties.each { println "ddp: " + it}
     return properties
   }
 }




 private def getAssertions(docAssertions, testAssertions) {
   def assertionsArr = []
   if (testAssertions instanceof String) {
     assertionsArr << [assert: testAssertions]
   } else {
     testAssertions.each{ assertion ->
       assertionsArr << [assert: assertion]
     }
   }
   if (docAssertions instanceof String) {
     assertionsArr << [assert: docAssertions]
   } else {
     docAssertions.each{ assertion ->
       assertionsArr << [assert: assertion]
     }
   }
   return assertionsArr
   // return assertions instanceof String ? [assertions] : assertions
 }



 def get(index) {
   return this.executions[index]
 }

 def length() {
   return this.executions.size()
 }

}

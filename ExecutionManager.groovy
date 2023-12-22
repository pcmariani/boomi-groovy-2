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
    LinkedHashMap OPTIONS = [userOpts: testsFileRoot.remove('OPTIONS')] ?: [:]
    LinkedHashMap GLOBALS = parseGlobals(testsFileRoot.remove('GLOBALS')) ?: [:]
    // println GLOBALS
    testSuite.globals = GLOBALS

    OPTIONS.printMode = printMode
    OPTIONS.workingDir = workingDir

    ArrayList tests = []

    if (OPTIONS.printMode != "testResultsOnly" && OPTIONS.userOpts?.disjoint(["nothing"])) {
      println ""
    }
    testsFileRoot.eachWithIndex { testYaml, index ->
      tests << runTest(OPTIONS, GLOBALS, testYaml, index)

      testSuite.numFailedTests = tests.testFailed.count(true)
      testSuite.numPassedTests = tests.testFailed.count(false)
      testSuite.numTests = tests.testFailed.size()

      if (true in tests.testFailed) {
        testSuite.suiteFailed = true
      }
    }
    if (OPTIONS.printMode != "testResultsOnly" && OPTIONS.userOpts?.disjoint(["nothing"])) {
      println ""
    }
    testSuite.tests = tests
    
    testSuites << testSuite
  }


  public def runTest(OPTIONS, GLOBALS, testYaml, index) {
      def t = parseTestYaml(OPTIONS, GLOBALS, testYaml, index)
      // peekT(t)
      def testResults = new Test(OPTIONS, t.desc, t.scripts, t.dpps, t.dataContext, t.index).run()
      def dc = testResults.dataContext

      // testResults.scripts.each {
      //   println it.name
      //   if (it.error) println it.error.replaceAll(/\s*at (?!Script1).*/,"").replaceFirst(/\n.*at .*?(groovy:\d+).*\n/," (\$1)")
      //   println ""
      //
      // }

      return [
        testName: t.desc,
        testFailed: true in dc.hasFailedAssertions || testResults.hasFailedExec ? true : false,
        hasFailedExec: testResults.hasFailedExec,
        testScripts: testResults.scripts,
        testError: testResults.execError,
        documents: dc.dataContextArr.collect{ it.subMap(["desc", "assertions"])}
      ]
  }


  void printResults() {
    def l1 = "  "
    def l2 = "     "
    def l3 = "       "
    def l4 = "          "
    def l5 = "            "
    def l6 = "              "
    this.testSuites.each { ts ->
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


  LinkedHashMap parseGlobals(GLOBALS) {
    def global = [:]
    global.scriptfiles = GLOBALS.scripts ?: GLOBALS.s
    if (global.scriptfiles instanceof String) {
      global.scriptfiles = [global.scriptfiles]
    }
    global.ProcessProps = GLOBALS.'process-props' ?: GLOBALS.dpps
    global.DPPsFile = GLOBALS.processPropsFile ?: GLOBALS.dppsFile
    global.testfilesDir = GLOBALS.testfilesDir ?: GLOBALS.tfDir ?: "."
    return global
  }
  

  private def parseTestYaml(OPTIONS, GLOBALS, testYaml, index) {
    def desc = testYaml.key
    def test = testYaml.value
    // println test

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
        index: index,
        desc: desc,
        scripts: getExecutionScripts(OPTIONS, null, GLOBALS.scriptfiles),
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
        // if (doc.testfilesDir) println "DFT " + doc.testfilesDir

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
        index: index,
        desc: desc,
        scripts: getExecutionScripts(
          OPTIONS,
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



 private def getExecutionScripts(OPTIONS, tfd, scriptfiles) {
   // println OPTIONS
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
         output: scriptArgs ?: []
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
         String filename = v.replaceFirst(/\s*@file\s*\(?'?(.*?)'?\)?$/, "\$1")
         properties.setProperty(k, new FileReader("$workingDir/$propsSubDir/$filename").text)
       }
     }
     // if (type == "DPP") properties.each { println "DPP: " + it}
     // if (type == "ddp") properties.each { println "ddp: " + it}
   }
   return properties
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

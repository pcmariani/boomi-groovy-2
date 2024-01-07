@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml

class TestSuite {
  LinkedHashMap OPTS
  LinkedHashMap testSuiteYaml
  LinkedHashMap GLOBALS
  ArrayList tests
  Boolean suiteFailed
  int numTests
  int numPassedTests
  int numFailedTests

  TestSuite(LinkedHashMap OPTS) {
    this.OPTS = OPTS

    def testSuiteFilePath = "${OPTS.workingDir}/${OPTS.testSuiteFileName}"
    this.testSuiteYaml = new Yaml().load((testSuiteFilePath as File).text)

    this.OPTS << [userOpts: testSuiteYaml.remove('OPTIONS')] ?: [:]
    this.GLOBALS = parseGlobals(testSuiteYaml.remove('GLOBALS')) ?: [:]

    this.tests = []
    testSuiteYaml.eachWithIndex { testYaml, index ->
       this.tests << parseTestYaml(testYaml, index)
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

    tests.each { t ->
      def testResults = new Test(OPTS, t.desc, t.scripts, t.dpps, t.dataContext, t.index).run()
      def dc = testResults.dataContext
      t.testFailed = true in dc.hasFailedAssertions || testResults.hasFailedExec ? true : false
      t.hasFailedExec = testResults.hasFailedExec
      t.testError = testResults.execError
      // t.each { println it; println ""}
    }

    this.numFailedTests = tests.testFailed.count(true)
    this.numPassedTests = tests.testFailed.count(false)
    this.numTests = tests.testFailed.size()

    if (true in tests.testFailed) {
      this.suiteFailed = true
    }
    println "I've been run"
  }

  private def parseTestYaml(testYaml, index) {
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
        index: index,
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
         script: new FileInputStream("${OPTS.workingDir}/$scriptfile"),
         output: m == scriptfiles.size() - 1 ? ["all"] : ["xx"],
       ]
     }
     else if (scriptfile instanceof LinkedHashMap) {
       def scriptfileName = scriptfile.keySet()[0]
       def scriptArgs = scriptfile.values()[0]

       scriptsArr << [
         name: scriptfileName,
         script: new FileInputStream("${OPTS.workingDir}/$scriptfileName"),
         output: scriptArgs ?: []
       ]
     }
   }
   return scriptsArr
 }



 private InputStream getDocumentContents( String data, String datafile) {
   if (datafile) {
     return new FileInputStream("${OPTS.workingDir}/$datafile")
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
     BufferedReader reader = new BufferedReader(new FileReader("${OPTS.workingDir}/$propsfile"));
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
         properties.setProperty(k, new FileReader("${OPTS.workingDir}/$propsSubDir/$filename").text)
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


}

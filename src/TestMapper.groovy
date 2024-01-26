class TestMapper {

  def testRaw
  int index
  String desc
  ArrayList scripts = []
  def dpps
  DataContext2 dataContext
  String testfilesDir

  TestMapper(testRaw, index) {
    this.testRaw = testRaw
    this.index = index
  }

  def transformTestYaml() {
    def desc = testRaw.key
    def test = testRaw.value

    def dataContext = new DataContext2()

    if (!test.docs && !test.documents) {
      test.docs = [test.clone()]
      test.remove("assertions")
      test.remove("assert")
    }

    test.docs.eachWithIndex { doc, m ->

      def tfd = doc.testfilesDir ?: Globals.testFilesDir

      InputStream data
      try {
        data = getDocumentContents(doc.data)
      } catch(Exception e) {
        throw new Exception("Check the syntax around the 'data' tag.")
      }

      Properties ddps
      try { 
        ddps = loadProperties(
          "ddp", [doc.ddps ?: doc.props ?: doc.ddpsOverride]
        )
      } catch(Exception e) {
        throw new Exception("Check the syntax around the 'ddps' tag.")
      }


      dataContext.storeStream(
        doc.desc ?: "Document " + m,
        data,
        ddps,
        getAssertions(doc.assert, test.assert),
        doc.ext ?: doc.extension ?: test.ext ?: test.extension ?: null
      )
    }

    def tfd = test.testfilesDir ?: Globals.testFilesDir

    this.index = index
    this.desc = desc
    this.scripts = getExecutionScripts(test.scripts ?: test.script ?: Globals.scripts)
    this.dpps = loadProperties("DPP", [Globals.DPPs, test.DPPs, Globals.DPPsOverride, test.DPPsOverride])
    this.dataContext = dataContext
    this.testfilesDir = tfd
  }



  private def getExecutionScripts(scriptfiles) {
    try {
      def scriptsArr = []
      if (scriptfiles instanceof String) {
        scriptfiles = [scriptfiles] as ArrayList
      }
      scriptfiles.eachWithIndex { scriptfile, m ->
        if (scriptfile instanceof String) {
          scriptsArr << [
            name: scriptfile,
            script: new FileInputStream("${Globals.workingDir}/$scriptfile"),
            output: m == scriptfiles.size() - 1 ? ["all"] : ["xx"],
          ]
        }
        else if (scriptfile instanceof LinkedHashMap) {
          def scriptfileName = scriptfile.keySet()[0]
          def scriptArgs = scriptfile.values()[0]

          scriptsArr << [
            name: scriptfileName,
            script: new FileInputStream("${Globals.workingDir}/$scriptfileName"),
            output: scriptArgs ?: []
          ]
        }
      }
      return scriptsArr
    } catch(Exception e) {
      throw new Exception("Check the syntax around the 'scripts' tag.")
    }
  }



  private String getFilenameFromValue(value) {
    // println value
    def filename = (value =~ /(?s)^\s*(?:@?file)?\s*\(?'?([^@]+\.\w+)'?\)?\s*$/).findAll()*.last()[0]
    // println "---------- " + filename
    return filename
  }



  private InputStream getDocumentContents(String data) {
    def fileName = getFilenameFromValue(data)
    if (fileName) {
      File file = new File("${Globals.workingDir}/$fileName")
      if (file.exists()) {
        return new FileInputStream(file)
      } else {
        return new ByteArrayInputStream(data.getBytes("UTF-8"))
      }
    } else if (data) {
      return new ByteArrayInputStream(data.getBytes("UTF-8"))
    } else {
      return new ByteArrayInputStream("".getBytes("UTF-8"))
    }
  }



  private Properties loadProperties(type, propsSourcesArr) {
    Properties properties = new Properties()

    propsSourcesArr.findAll{it != null}.each {

      Properties propertiesPerSource = new Properties()
      String propertiesFilename = getFilenameFromValue(it)

      if (propertiesFilename) {
        BufferedReader reader = new BufferedReader(new FileReader("${Globals.workingDir}/$propertiesFilename"));
        String line
        while ((line = reader.readLine()) != null) {
          def propArr = line.split(/\s*=\s*/, 2)
          if (line && !(line =~ /^\s*#/)) {
            if      (type == "DPP" && !(line =~ /^\s*document\.dynamic\.userdefined\./)) {
              propertiesPerSource.load(new StringReader(line))
            }
            else if (type == "ddp" &&  (line =~ /^\s*document\.dynamic\.userdefined\./)) {
              propertiesPerSource.load(new StringReader(line))
            }
          }
        }
        reader.close();
      }

      else {
        if (it instanceof String) {
          propertiesPerSource.load(new StringReader(it))
        }
        else if (it instanceof LinkedHashMap) {
          propertiesPerSource.putAll(it)
        }
      }

      if (propertiesPerSource) {
        String propsSubDir = propertiesFilename ? propertiesFilename.replaceFirst(/(.*)[\/\\].*/, "\$1") : ""
        propertiesPerSource.each { k, v ->
          // println k + "     " + v
          def valueFilename = getFilenameFromValue(v)
          if (valueFilename) {
            propertiesPerSource.setProperty(k, new FileReader("${Globals.workingDir}/$propsSubDir/$valueFilename").text)
          }
        }

        properties << propertiesPerSource
      }

    }
    
    // properties.each { println type + ":  " + it }
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

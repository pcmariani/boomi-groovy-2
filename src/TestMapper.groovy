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
      // try {
        data = getDocumentContents(doc.data)
      // } catch(Exception e) {
      //   throw new Exception("Check the syntax around the 'data' tag.")
      // }

      Properties ddps
      // try { 
        ddps = loadProperties(
          "ddp", [doc.ddps ?: doc.props, doc.ddpsOverride]
        )
      // } catch(Exception e) {
      //   throw new Exception("Check the syntax around the 'ddps' tag.")
      // }


      dataContext.storeStream(
        doc.desc ?: "Document " + m,
        data,
        ddps,
        getAssertions([doc.assertions, test.assertions]),
        // getAssertions(doc.assertions, test.assertions),
        doc.ext ?: doc.extension ?: test.ext ?: test.extension ?: null
      )
    }

    def tfd = test.testfilesDir ?: Globals.testFilesDir

    this.index = index
    this.desc = desc
    try {
      this.scripts = getExecutionScripts(test.scripts ?: test.script ?: Globals.scripts)
    } catch(Exception e) {
      throw new Exception("Error with scripts: " + e.getMessage())
    }
    this.dpps = loadProperties("DPP", [Globals.DPPs, test.DPPs, Globals.DPPsOverride, test.DPPsOverride])
    this.dataContext = dataContext
    this.testfilesDir = tfd
  }



  private def getExecutionScripts(scriptfiles) {
    // try {
      def scriptsArr = []
      if (scriptfiles instanceof String) {
        scriptfiles = [scriptfiles] as ArrayList
      }
      scriptfiles.eachWithIndex { scriptfile, m ->
        if (scriptfile instanceof String) {
          scriptsArr << [
            name: scriptfile,
            script: new FileInputStream("${Globals.workingDir}/$scriptfile"),
            opts: [:]
          ]
        }
        else if (scriptfile instanceof LinkedHashMap) {
          def scriptfileName = scriptfile.keySet()[0]
          def scriptOpts = scriptfile.values()[0]

          scriptsArr << [
            name: scriptfileName,
            script: new FileInputStream("${Globals.workingDir}/$scriptfileName"),
            opts: OptsHelper.processOpts(scriptOpts) ?: [:]
          ]
        }
      }
      return scriptsArr
    // } catch(Exception e) {
    //   throw new Exception("Check the syntax around the 'scripts' tag.")
    // }
  }



  private String getFilenameFromValue(value) {
    def filename = (value =~ /(?s)^\s*(?:@?file)?\s*\(?'?([^@]{1,240}\.[A-Za-z]\w{1,14})'?\)?\s*$/).findAll()*.last()[0]
    return filename
  }

  private String getFilenameFromValueNeedsAtFilePrefix(value) {
    def filename = (value =~ /(?s)^\s*@file\s*\(?["']?(.*?)["']?\)?\s*$/).findAll()*.last()[0]
    return filename
  }


  private InputStream getDocumentContents(String data) {
    def fileName = getFilenameFromValue(data)
    if (fileName) {
      File file = new File("${Globals.workingDir}/$fileName")
      if (file.exists()) {
        return new ByteArrayInputStream(file.getText().getBytes("UTF-8"))
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
        String propsSubDir = (propertiesFilename =~ /[\/\\]/) ? propertiesFilename.replaceFirst(/[\/\\].*/, "") : ""
        // println type + " " + propsSubDir
        propertiesPerSource.each { k, v ->
          // println k + " :: " + v
          def valueFilename = getFilenameFromValueNeedsAtFilePrefix(v)
          if (valueFilename) {
            println valueFilename
            // println "filename + " + type + " " + it + " " + valueFilename
            propertiesPerSource.setProperty(k, new FileReader("${Globals.workingDir}/$propsSubDir/$valueFilename").text)
          }
        }

        properties << propertiesPerSource
      }

    }
    
    // properties.each { println type + ":  " + it }
    return properties
  }



  private def getAssertions(assertionsSourcesArr) {
    def assertionsResultArr = []
    assertionsSourcesArr.each { assertionsRaw ->
      // assertionsRaw.each { println it.getClass() }
      if (assertionsRaw instanceof String) {
        assertionsRaw = assertionsRaw.toArray()
      }
      assertionsRaw.each{ assertion ->
        if (assertion instanceof LinkedHashMap) {
          assertionsResultArr << [desc: assertion.desc, assert: assertion.assert]
        } else {
          assertionsResultArr << [desc: null, assert: assertion]
        }
      }
    }
    return assertionsResultArr
  }


}

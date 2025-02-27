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

    this.index = index
    this.desc = desc

    // try {
    //   this.scripts = getExecutionScripts(test.scripts ?: test.script ?: Globals.scripts)
    // } catch(Exception e) {
    //   throw new Exception("Error with scripts: " + e.getMessage())
    // }

    try {
      def testScripts = test.find{ it.key =~ /(?i)^scripts?$/ } ?: [:]
      if (testScripts) {
        test.remove(testScripts.key)
      }
      this.scripts = getExecutionScripts(testScripts.value ?: Globals.scripts)
    } catch(Exception e) {
      throw new Exception("Error with scripts: " + e.getMessage())
    }


    def testDPPs =  test.find{ it.key =~ /(?i)^dpps$/ } ?: [:]
    if (testDPPs) {
      test.remove(testDPPs.key)
    } 
    this.dpps = loadProperties("DPP", [Globals.DPPs, testDPPs.value, Globals.DPPsOverride, test.DPPsOverride])

    def tfd = test.testfilesDir ?: Globals.testFilesDir
    this.testfilesDir = tfd

    def dataContext = new DataContext2()
    this.dataContext = dataContext



    // test.remove("data")
    // test.remove("ddps")
    test.remove("script")
    test.remove("scripts")
    test.remove("DPPsOverride")
    // test.remove("docs")
    // println test

    // println "--------------"

    ArrayList documents = []

    test.each { docRaw ->
      String docDesc = docRaw.key
      LinkedHashMap doc = docRaw.value
      documents << [
        desc: docDesc, // + ": " + doc.data.replaceFirst(/^\.\//,""),
        data: doc.data,
        ddps: doc.ddps
      ]
    }
    // println documents
    // println test


    // if (!test.docs && !test.documents) {
    //   test.docs = [test.clone()]
    //   test.remove("assertions")
    //   test.remove("assert")
    // }

    documents.eachWithIndex { doc, m ->

      // println doc
      // def doc_tfd = doc.testfilesDir ?: Globals.testFilesDir

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
    // def filename = (value =~ /(?s)^\s*@file\s*\(?["']?(.*?)["']?\)?\s*$/).findAll()*.last()[0]
    def filename = (value =~ /^\.?[_\$\?\/\\](?:file)?\s*(.*)/).findAll()*.last()[0]
    println filename
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

  private Properties loadPropertiesFromFile(type, propertiesFilename) {
    Properties resultProperties = new Properties()
    BufferedReader reader = new BufferedReader(new FileReader("${Globals.workingDir}/$propertiesFilename"));
    String line
    while ((line = reader.readLine()) != null) {
      def propArr = line.split(/\s*=\s*/, 2)
      if (line && !(line =~ /^\s*#/)) {
        if      (type == "DPP" && !(line =~ /^\s*document\.dynamic\.userdefined\./)) {
          resultProperties.load(new StringReader(line))
        }
        else if (type == "ddp" &&  (line =~ /^\s*document\.dynamic\.userdefined\./)) {
          resultProperties.load(new StringReader(line))
        }
      }
    }
    reader.close();
    return resultProperties
  }


  private Properties loadProperties(type, propsSourcesArr) {
    Properties properties = new Properties()

    propsSourcesArr.findAll{it != null}.each {

      Properties propertiesPerSource = new Properties()
      String propertiesFilename = getFilenameFromValue(it)

      if (propertiesFilename) {
        propertiesPerSource = loadPropertiesFromFile(type, propertiesFilename)
      }

      else {
        if (it instanceof String) {
          if (type == "ddp") {
            if (!(it =~ /^\s*document\.dynamic\.userdefined\./)) {
              it = "document.dynamic.userdefined." + it
            }
          }
          propertiesPerSource.load(new StringReader(it))
        }
        else if (it instanceof LinkedHashMap) {
          def propsMap = it.collectEntries{ 
            def key = it.key
            def value = it.value.toString()
            if (key ==~ /^\.?[_\$\?\/\\](?:file)?/) {
              loadPropertiesFromFile(type, value)
            }
            else {
              if (type == "ddp") {
                if (!(key =~ /^\s*document\.dynamic\.userdefined\./)) {
                  key = "document.dynamic.userdefined." + key
                }
              }
              [(key), value.replaceAll(/\\n/,"\n")]
            }
          }
          propertiesPerSource.putAll(propsMap)
        }
      }

      if (propertiesPerSource) {
        String propsSubDir = (propertiesFilename =~ /[\/\\]/) ? propertiesFilename.replaceFirst(/[\/\\].*/, "") : ""
        // println type + " " + propsSubDir
        propertiesPerSource.each { k, v ->
          // println k + " :: " + v
          def valueFilename = getFilenameFromValueNeedsAtFilePrefix(v)
          if (valueFilename) {
            // println valueFilename
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

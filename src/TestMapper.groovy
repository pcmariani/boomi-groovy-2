class TestMapper {

  LinkedHashMap GLOBALS
  def testRaw
  int index
  String desc
  ArrayList scripts = []
  def dpps
  DataContext2 dataContext
  String testfilesDir

  TestMapper(GLOBALS, testRaw, index) {
    this.GLOBALS = GLOBALS
    this.testRaw = testRaw
    this.index = index
  }

  def transformTestYaml() {
    def desc = testRaw.key
    def test = testRaw.value

    def dataContext = new DataContext2()

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
        loadProperties("ddp", [doc.props, doc.propsfile]),
        // loadProps(
        //   "ddp",
        //   null,
        //   doc.props ?: null,
        //   : doc.propsfile ? "$tfd/$doc.propsfile"
        //   : doc.pf ? "$tfd/$doc.pf"
        //   : null
        // ),
        getAssertions(
          doc.assert ?: doc.a ?: null,
          test.assert ?: test.a ?: null
        ),
        doc.ext ?: doc.e ?: doc.extension ?: test.ext ?: test.e ?: test.extension ?: null
      )
    }

    def tfd = test.testfilesDir ?: test.tfDir ?: GLOBALS.testfilesDir

    this.index = index
    this.desc = desc
    this.scripts = getExecutionScripts(
      tfd,
      test.scripts ?: test.s ?: GLOBALS.scriptfiles
    )
    // this.dpps = loadProperties(
    //   "DPP", [GLOBALS.ProcessProps, test.DPPs, test.moreDPPs]
    // )
    // this.dpps = loadProps(
    //   "DPP",
    //   GLOBALS.ProcessProps,
    //   test.'process-props' ?: test.dpps ?: null,
    //   test.processPropsFile ? "$tfd/$test.processPropsFile"
    //   : test.dppsFile ? "$tfd/$test.dppsFile"
    //   : test.propsfile ? "$tfd/$test.propsfile"
    //   : test.pf ? "$tfd/$test.pf"
    //   : GLOBALS.DPPsFile ? "$GLOBALS.testfilesDir/$GLOBALS.DPPsFile"
    //   : null
    // )
    this.dataContext = dataContext
    this.testfilesDir = tfd
  }


  private String getFilenameFromValue(value) {
      return value.replaceFirst(/\s*@?file\s*\(?'?(.*?)'?\)?$/, "\$1")
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
          script: new FileInputStream("${GlobalOptions.workingDir}/$scriptfile"),
          output: m == scriptfiles.size() - 1 ? ["all"] : ["xx"],
        ]
      }
      else if (scriptfile instanceof LinkedHashMap) {
        def scriptfileName = scriptfile.keySet()[0]
        def scriptArgs = scriptfile.values()[0]

        scriptsArr << [
          name: scriptfileName,
          script: new FileInputStream("${GlobalOptions.workingDir}/$scriptfileName"),
          output: scriptArgs ?: []
        ]
      }
    }
    return scriptsArr
  }



  private InputStream getDocumentContents( String data, String datafile) {
    if (data =~ /^\s*file/) {
      def filename = getFilenameFromValue(data)
      return new FileInputStream("${GlobalOptions.workingDir}/$filename")
    }
    else if (datafile) {
      return new FileInputStream("${GlobalOptions.workingDir}/$datafile")
    } else if (data) {
      return new ByteArrayInputStream(data.getBytes("UTF-8"))
    } else {
      return new ByteArrayInputStream("".getBytes("UTF-8"))
    }
  }


  private String getDataFilenameFromValue(value) {
    // println value
    def filename = (value =~ /(?s)^\s*(?:@?file)?\s*\(?'?([^@]+\.\w+)'?\)?\s*$/).findAll()*.last()[0]
    // println "---------- " + filename
    return filename
  }

  private Properties loadProperties(type, propsSourcesArr) {
    Properties properties = new Properties()

    propsSourcesArr.findAll{it != null}.each {

      Properties propertiesPerSource = new Properties()
      String propertiesFilename = getDataFilenameFromValue(it)

      if (propertiesFilename) {
        BufferedReader reader = new BufferedReader(new FileReader("${GlobalOptions.workingDir}/$propertiesFilename"));
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
          def valueFilename = getDataFilenameFromValue(v)
          if (valueFilename) {
            propertiesPerSource.setProperty(k, new FileReader("${GlobalOptions.workingDir}/$propsSubDir/$valueFilename").text)
          }
        }

        properties << propertiesPerSource
      }

    }
    
    // properties.each { println type + ":  " + it }
    return properties
  }



  private Properties loadProps(type, globalPropsStr, propsStr, propsfile) {
    Properties properties = new Properties()

    // if (type == "DPP") println "DPP: " + globalPropsStr

    // if (data =~ /^\s*file/) {
    //   def filename = getFilenameFromValue(data)
    //   return new FileInputStream("${GlobalOptions.workingDir}/$filename")
    // }
    //

    if (propsfile) {
      BufferedReader reader = new BufferedReader(new FileReader("${GlobalOptions.workingDir}/$propsfile"));
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

        def filename = getDataFilenameFromValue(v)
        if (filename) {
          properties.setProperty(k, new FileReader("${GlobalOptions.workingDir}/$propsSubDir/$filename").text)
        }


      }
      if (type == "DPP") properties.each { println "DPP: " + it}
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

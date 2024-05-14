class DataContext2 {
  ArrayList dataContextArr = []
  int dcIndex = -1
  int numStores = 0
  // private String scriptName
  Boolean hasFailedAssertions = false

  // void setCurrentScriptName(scriptName) {
  //   this.scriptName = scriptName
  // }
  //
  // String getCurrentScriptName() {
  //   return this.scriptName
  // }

  void storeStream(String desc, InputStream is, Properties props, ArrayList assertions, String extension){
    this.dataContextArr << [
      desc: desc,
      is: is,
      props: props,
      assertions: assertions,
      extension: extension
    ]
    // println "STORESTREAM 1 " + dcIndex
  }

  void storeStream(InputStream is, Properties props){
    this.dataContextArr[dcIndex] = [
      desc: this.dataContextArr[dcIndex].desc,
      is: is,
      props: props,
      assertions: this.dataContextArr[dcIndex].assertions,
      extension: this.dataContextArr[dcIndex].extension
    ]
    numStores++
    // println numStores
    // println "STORESTREAM 2 " + dcIndex
  }

  int getDataCount(){
    return dataContextArr.size()
  }

  int getNumStores() {
    return dataContextArr.size()
  }

  InputStream getStream(int index){
    // println "dcIndex BEFORE: " + this.dcIndex
    this.dcIndex = index
    // println "dcIndex AFTER: " + this.dcIndex
    return this.dataContextArr[index]?.is
  }

  Properties getProperties(int index) {
    return this.dataContextArr[index]?.props
  }

  String getDesc(int index) {
    return this.dataContextArr[index]?.desc
  }

  String getExtension(int index) {
    return this.dataContextArr[index]?.extension
  }

  // Boolean getHasFailedAssertions() {
  //   // println this.dataContextArr?.hasFailedAssertions
  //   return true in this.dataContextArr?.hasFailedAssertions ? true : false
  // }

  // ArrayList getAssertions() {
  //   return this.dataContextArr.assertions
  // }

  // ArrayList getAssertionResults() {
  //   // return this.dataContextArr.assertions
  //   def results = []
  //   dataContextArr.assertions.each{ 
  //     it.each {
  //     results << [
  //       asssrt: it.assert,
  //       passed: it.passed,
  //       error: it.error ?: "-- no error --",
  //     ]
  //     }
  //   }
  //   return results
  // }

  // ArrayList getAssertions(int index) {
  //   if (this.dataContextArr[index].assertions) {
  //     return this.dataContextArr[index].assertions
  //   } else {
  //     return []
  //   }
  // }

  def evalAssertions(int index, ExecutionUtilHelper ExecutionUtil) {
    def dc = dataContextArr[index]

    dc.assertions.each { assertionObj ->
      String assertion = assertionObj.assert.replaceFirst(/^assert\s+/, "")
      String assertionSubjectVar = ""
      String propName

      if (assertion.startsWith("data")) {
        assertionSubjectVar = "def data = is.text;"
      } else {
        propName = (assertion =~ /^(\w+).*/).findAll()*.last()[0]
        // println propName
        if (dc.props."document.dynamic.userdefined.$propName") {
          // println dc.props."document.dynamic.userdefined.$propName"
          assertionSubjectVar = "def $propName = props.getProperty(\"document.dynamic.userdefined.$propName\"); "
        }
        else if (ExecutionUtil.getDynamicProcessProperty(propName)) {
          // println ExecutionUtil.getDynamicProcessProperty(propName)
          assertionSubjectVar = "def $propName = ExecutionUtil.getDynamicProcessProperty(\"$propName\"); "
        } 
      }
      // println assertionSubjectVar

      try {
        if (assertionSubjectVar) {
          Eval.xyz(ExecutionUtil, dc.is, dc.props,
          "def ExecutionUtil = x; InputStream is = y; Properties props = z;"
            + assertionSubjectVar
            + "assert $assertion"
          )
          assertionObj.passed = true
          // assertionObj.error = assertion
        }
        else {
          assertionObj.passed = null
          // assertionObj.error = assertion
        }
      } catch (AssertionError assertionError) {
        this.hasFailedAssertions = true
        assertionObj.passed = false
        assertionObj.error = assertionError.toString().replaceFirst("is.text", "data").take(400)
      }
      dc.is.reset()
    }

  }

  void printAssertions(int index) {
    String prefix = "assert "
    dataContextArr[index].assertions.each{ 
      if (it.passed == false) {
        def errorArr = it.error
            .replaceFirst(/Assertion failed:\s*/, "")
            .replaceFirst("assert ", "")
            .replaceFirst(/\n$/, "")
            .split("\n", 2)

        println Fmt.red + "  ✗  " + Fmt.grey + prefix + Fmt.off + errorArr[0]
        println Fmt.red + errorArr[1].replaceAll(/(^|\n)/, "\$1      ") + Fmt.off
      }
      else if (it.passed) {
        println Fmt.green + "  ✓  " +  Fmt.grey + prefix + Fmt.off + it.assert
      }
      else if (it.passed == null) {
        println Fmt.yellow + "  － " + Fmt.grey + prefix + Fmt.off + it.assert + Fmt.grey + "  <-- not evaluated" + Fmt.off
        println Fmt.yellow + "             |\n             " + "no DPP or ddp with that name" + Fmt.off
      }
    }
  }

  // ArrayList getResultDataArr() {
  //   return dataContextArr.collect{ it.is.text }
  // }
  //
  // def getResult(int index) {
  //   def is = this.dataContextArr[index]?.is
  //   return is
  //   is.reset()
  // }

  void printData(int index) {
    def is = this.dataContextArr[index]?.is
    // println "INDEX " + index
    // println this.getDataCount() + " " + this.numStores
    // if (this.numStores > this.getDataCount()) {
    //   println Fmt.green + "-"*Globals.termWidth + Fmt.off
    // }
    println is?.text
    is.reset()
  }

  void printProperties(int index, String ddpsList, Boolean isPrintingDataAlso) {

    def dc = this.dataContextArr[index]

      if (ddpsList) {
        ArrayList ddpsArr = ddpsList.split(",")

        ddpsArr.each { ddpName ->
          ddpName = ddpName.replaceFirst("document.dynamic.userdefined.","")
          String key = "document.dynamic.userdefined.$ddpName".trim()

          if (key in dc?.props.keySet()) {
            String val = dc?.props."$key"
            if (val =~ /^[\[\{]/) {
              val = Fmt.toPrettyJson(val)
            }
            println Fmt.green + ddpName + Fmt.blue + ": " + Fmt.off + val
          } else {
            println Fmt.red + ddpName + Fmt.blue + ": " + Fmt.yellow + "<-- no ddp with this name" + Fmt.off
          }

        }
      }
      else {
        dc?.props.each { key, val ->
          if (val =~ /^[\[\{]/) {
            val = Fmt.toPrettyJson(val)
          }
          println Fmt.green + key.replaceFirst("document.dynamic.userdefined.","") + Fmt.blue + ": " + Fmt.off + val
        }
      }
    if (dc?.props && isPrintingDataAlso) println ""
  }

  void writeFile(int index, String workingDir, String testDesc, String scriptDesc) {
    String outFilesDirName = "$workingDir/_outfiles"
    File outFilesDir = new File(outFilesDirName);
    if (!outFilesDir.exists()) {
      outFilesDir.mkdir()
    }
    def docDesc = this.dataContextArr[dcIndex].desc.replaceFirst(/(?:^|.*?)(\w+)\..*$/, "\$1")
    def ext = this.dataContextArr[dcIndex].extension
    // def fileName = "$outFilesDirName/OUT${index.toString().padLeft(2,"0")}__${testDesc.take(12)}__${scriptDesc.replaceFirst(".groovy","")}__${docDesc}.${ext.replaceFirst("\\.","")}"
    def fileName = "$outFilesDirName/" +
      "OUT" +
      "__" +
      "${scriptDesc.replaceFirst(".groovy","")}" +
      "__" +
      "${testDesc.take(12)}" +
      "__" +
      "${index.toString().padLeft(2,"0")}" +
      "${docDesc}" +
      ".${ext.replaceFirst("\\.","")}"
    def is = this.dataContextArr[dcIndex].is
    File dataFile = new File(fileName.replaceAll(" ", "_"))
    if (ext == "html" || ext == "htm") {
      def html_out = '''<!DOCTYPE html>
        <html lang="en">
          <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta http-equiv="X-UA-Compatible" content="ie=edge">
            <title>My Website</title>
            <link rel="stylesheet" href="./style.css">
            <link rel="icon" href="./favicon.ico" type="image/x-icon">
          </head>
          <body>''' + is.text + '''</body>
        </html>
      '''
      dataFile.write html_out
    }
    else {
      dataFile.write is.text
    }
    is.reset()
  }

  // void close() {
  //   this.dataContextArr = []
  //   this.dcIndex = -1
  // }
}

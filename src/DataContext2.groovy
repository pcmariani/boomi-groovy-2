class DataContext2 {
  ArrayList dataContextArr = []
  int dcIndex = -1
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
  }

  void storeStream(InputStream is, Properties props){
    this.dataContextArr[dcIndex] = [
      desc: this.dataContextArr[dcIndex].desc,
      is: is,
      props: props,
      assertions: this.dataContextArr[dcIndex].assertions,
      extension: this.dataContextArr[dcIndex].extension
    ]
  }

  int getDataCount(){
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
      try {
        Eval.xyz(ExecutionUtil, dc.is, dc.props,
        "def ExecutionUtil = x; InputStream is = y; Properties props = z; assert "
        + assertionObj.assert
        .replaceFirst(/(document\.dynamic\.userdefined\.\w+)/, "\'\$1\'")
        )
        assertionObj.passed = true
      } catch(AssertionError assertionError) {
        this.hasFailedAssertions = true
        assertionObj.passed = false
        assertionObj.error = assertionError.toString().take(400)
      }
      dc.is.reset()
    }
  }

  void printAssertions(int index) {
    dataContextArr[index].assertions.each{ 
      if (it.error) {
        println ""
        println Fmt.red + it.error + Fmt.off
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
    // println Fmt.yellow + is?.text + Fmt.off
    println is?.text
    is.reset()
  }

  void printProperties(int index, Boolean isPrintingDataAlso) {
    def dc = this.dataContextArr[index]
    dc?.props.each { k,v ->
      println Fmt.green + k.replaceFirst("document.dynamic.userdefined.","") + Fmt.blue + ": " + Fmt.off + v
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

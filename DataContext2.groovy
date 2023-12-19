class DataContext2 {
    ArrayList dataContextArr = []
    private int dcIndex = -1
    private String scriptName
    Boolean hasFailedAssertions = false
    
    void setCurrentScriptName(scriptName) {
      this.scriptName = scriptName
    }

    String getCurrentScriptName() {
      return this.scriptName
    }

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

    InputStream getStream(int index){
      this.dcIndex = index
      return this.dataContextArr[index]?.is
    }

    int getDataCount(){
      return dataContextArr.size()
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

    ArrayList getAssertions() {
      return this.dataContextArr.assertions
    }

    ArrayList getAssertionResults() {
      // return this.dataContextArr.assertions
      def results = []
      dataContextArr.assertions.each{ 
        it.each {
        results << [
          asssrt: it.assert,
          passed: it.passed,
          error: it.error ?: "-- no error --",
        ]
        }
      }
      return results
    }

    ArrayList getAssertions(int index) {
      if (this.dataContextArr[index].assertions) {
        return this.dataContextArr[index].assertions
      } else {
        return []
      }
    }

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
          assertionObj.error = assertionError.toString()
        }
        dc.is.reset()
      }
    }

    void printAssertions(int index) {
      dataContextArr[index].assertions.each{ 
        if (it.error) {
          println ""
          println Color.red + it.error + Color.off
        }
      }
    }

    ArrayList getResultDataArr() {
      return dataContextArr.collect{ it.is.text }
    }

    def getResult(int index) {
      def is = this.dataContextArr[index]?.is
      return is
      is.reset()
    }

    void printData(int index) {
      def is = this.dataContextArr[index]?.is
      println Color.yellow + is?.text + Color.off
      is.reset()
  }

    void printProperties(int index) {
      this.dataContextArr[index]?.props.each { k,v ->
        println Color.green + k.replaceFirst("document.dynamic.userdefined.","") + Color.blue + ": " + Color.off + v
      }
      println ""
    }

    void writeFile(int index, String testfilesDir, String testDesc, String scriptDesc) {
      def docDesc = this.dataContextArr[dcIndex].desc.replaceFirst(/\..*$/, "")
      def ext = this.dataContextArr[dcIndex].extension
      def fileName = "OUT${index.toString().padLeft(2,"0")}__${testDesc.take(12)}__${scriptDesc.replaceFirst(".groovy","")}__${docDesc}.${ext.replaceFirst("\\.","")}"
      def is = this.dataContextArr[dcIndex].is
      File dataFile = new File(testfilesDir + "/" + fileName.replaceAll(" ", "_"))
      dataFile.write is.text
      is.reset()
    }

    void close() {
      this.dataContextArr = []
      this.dcIndex = -1
    }
}

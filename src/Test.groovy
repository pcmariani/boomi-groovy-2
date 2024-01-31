class Test {
  private LinkedHashMap test = [:]
  int index
  String desc
  ArrayList scripts = []
  Properties dpps
  DataContext2 dataContext
  Boolean testFailed = false
  Boolean hasFailedExec = false
  LinkedHashMap execError

  Test (String desc, ArrayList scripts, Properties dpps, DataContext2 dataContext, int index) {
    this.index = index
    this.desc = desc
    this.scripts = scripts
    this.dpps = dpps
    this.dataContext = dataContext
  }

  def run() {
    def outg = Globals.options ?: []
    // println out
    if (outg.disjoint(["no labels"])) {
      if (this.index > 0) {
        println ""
        println Fmt.green + "-------------------------------------------------------------------------" + Fmt.off
        println ""
      }
      Fmt.p("blue", "TEST: ")
      Fmt.pl("blue", this.desc)
    }

    def ExecutionUtil = new ExecutionUtilHelper()
    ExecutionUtil.dynamicProcessProperties = this.dpps

    // def dataContext = this.dataContext

    // this.scripts.eachWithIndex { scriptObj, k ->
    for (int k = 0; k < this.scripts.size(); k++) {

      def scriptObj = this.scripts[k]
      // dataContext.setCurrentScriptName(scriptObj.name)

      ArrayList out = scriptObj.output + outg
      // println out

      if (this.scripts.size() > 1 && out.disjoint(["no labels"])) {
        println ""
        Fmt.p("magenta", scriptObj.name)
        Fmt.pl("grey", " - " + this.desc)
      } 

      String script = scriptObj.script.text
        // remove ExecutionUtil import
        .replaceFirst(/import com\.boomi\.execution\.ExecutionUtil;?/, "")

      if (k == this.scripts.size() - 1) {
        script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
            "\$1; dataContext.evalAssertions(i, ExecutionUtil); ")
      }

      if (!out.disjoint(["no println"])) {
        script = script
        .replaceAll(/(.*?)println/, "// \$1 println")
      }

      if (out.disjoint(["no labels"])) {
        script = script
        .replaceFirst(/(.*dataContext.getDataCount\(\).*)/,
        "\$1; if (dataContext.getDataCount() > 1) println \"${Fmt.blue}DOCUMENT\" + i.toString() + \": ${Fmt.magenta}\" + dataContext.getDesc(i) + \"${Fmt.off}\"")
      }

      if (!out.disjoint(["props", "dpps", "DPPs"])) {
        script = script
        .replaceFirst(/(.*dataContext.storeStream.*)/,
        "\$1; ExecutionUtil.printDynamicProcessProperties(${!out.disjoint(["data"])}); ")
      }

      if (!out.disjoint(["props", "ddps"])) {
        script = script
        .replaceFirst(/(.*dataContext.storeStream.*)/,
        "\$1; dataContext.printProperties(i, ${!out.disjoint(["data"])}); ")
      }

      if (!out.disjoint(["data"])) {
        script = script
        .replaceFirst(/(.*dataContext.storeStream.*)/,
        "\$1; dataContext.printData(i); ")
      }

      if (!out.disjoint(["assertions"])) {
        script = script
        .replaceFirst(/(.*dataContext.storeStream.*)/,
        "\$1; dataContext.printAssertions(i); ")
      }

      if (out.disjoint(["no files"]) && k == this.scripts.size() - 1) {
        script = script
        .replaceFirst(/(.*dataContext.storeStream.*)/,
        "\$1; if (dataContext.getExtension(i)) dataContext.writeFile(i, \"${Globals.workingDir}\", \"$test.desc\", \"$scriptObj.name\"); ")
      }

      try {
        Eval.xy(
          this.dataContext, ExecutionUtil, "def dataContext = x; ExecutionUtil = y; " + script
        )
      } catch(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        org.codehaus.groovy.runtime.StackTraceUtils.sanitize(e).printStackTrace(pw)

        this.hasFailedExec = true
        this.execError = [
          script: scriptObj.name,
          docIndex: this.dataContext.dcIndex,
          docName: dataContext.dataContextArr[dataContext.dcIndex].desc,
          error: sw.toString()
        ]

        // def padChar = "| "
        // println padChar + sw.toString().replaceAll(/\n/, "\n$padChar ").replaceAll(/\n.*?\(Unknown Source\)\n/, "\n").replaceFirst(/\$padChar\s*$/,"")

        if (out.disjoint(["no errors"])) {
          // println sw.toString()
          Fmt.pl("red", sw.toString())
          System.exit(1)
        }

        break
      }

    }

    testFailed = true in this.dataContext.hasFailedAssertions || this.hasFailedExec ? true : false

  }
}

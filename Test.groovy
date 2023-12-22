class Test {
  private def test = [:]
  private def opts = [:]

  Test (LinkedHashMap OPTIONS, String desc, ArrayList scripts, Properties dpps, DataContext2 dataContext, int index) {
    this.test = [
      index: index,
      desc: desc,
      scripts: scripts,
      dpps: dpps,
      dataContext:dataContext,
      hasFailedExec: false,
      execError: null
    ]
    this.opts = OPTIONS
  }

  def run() {

    def outg = opts.userOpts ?: []
    // println outg

    if (this.test.index > 0 && outg.disjoint(["nothing"])) {
      println ""
      println Color.green + "-------------------------------------------------------------------------" + Color.off
      println ""
    }
    if (opts.printMode != "testResultsOnly") {
      Fmt.pl("blue", test.desc)
    }

    def ExecutionUtil = new ExecutionUtilHelper()
    ExecutionUtil.dynamicProcessProperties = this.test.dpps

    def dataContext = this.test.dataContext

    // this.test.scripts.eachWithIndex { scriptObj, k ->
    for (int k = 0; k < this.test.scripts.size(); k++) {

      def scriptObj = this.test.scripts[k]
      // dataContext.setCurrentScriptName(scriptObj.name)

      ArrayList out = scriptObj.output + outg
      // println out

      if (opts.printMode != "testResultsOnly") {
        if (out.disjoint(["nothing"])) {
          println ""
        }
        if (out.disjoint(["nothing"])) {
          Fmt.p("magenta", scriptObj.name)
          Fmt.pl("blue", " - " + test.desc)
        } else {
          Fmt.pl("magenta", scriptObj.name)
        }
        // println Color.blue + "TEST: " + Color.magenta + test.desc + Color.off
        // println Color.blue + "SCRIPT: " + Color.magenta + scriptObj.name + Color.off
      }

      String script = scriptObj.script.text
      // remove ExecutionUtil import
      .replaceFirst(/import com\.boomi\.execution\.ExecutionUtil;?/, "")

      if (k == test.scripts.size() - 1) {
        script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
            "\$1; dataContext.evalAssertions(i, ExecutionUtil); ")
      }

      if (opts.printMode == "testResultsOnly" || !out.disjoint(["nothing", "no println"])) {
        script = script
        .replaceAll("println", "// println")
      }

      // opts.printMode = "testResultsOnly"
      if (opts.printMode != "testResultsOnly") {
        // Document Number
        if (out.disjoint(["nothing"])) {
          script = script
          .replaceFirst(/(.*dataContext.getDataCount\(\).*)/,
          "\$1; if (dataContext.getDataCount() > 1) println \"${Color.blue}DOCUMENT\" + i.toString() + \": ${Color.magenta}\" + dataContext.getDesc(i) + \"${Color.off}\"")
        }

        // if (!out.disjoint(["all", "props", "p", "dpp", "DPP", "dpps", "DPPs"])) {
        if (out.disjoint(["nothing", "no results", "no props"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; ExecutionUtil.printDynamicProcessProperties(); ")
        }

        // if (!out.disjoint(["all", "props", "p", "ddp", "ddps"])) {
        if (out.disjoint(["nothing", "no results", "no props"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; dataContext.printProperties(i); ")
        }

        // if (!out.disjoint(["all", "data", "d", "is"])) {
        if (out.disjoint(["nothing", "no results", "no data"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; dataContext.printData(i); ")
        }

        // if (!out.disjoint(["all", "assert", "assertions", "a"])) {
        if (out.disjoint(["nothing", "no results", "no assertions"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; dataContext.printAssertions(i); ")
        }

        if (k == test.scripts.size() - 1) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; if (dataContext.getExtension(i)) dataContext.writeFile(i, \"${this.opts.workingDir}\", \"$test.desc\", \"$scriptObj.name\"); ")
        }
      }

      try {
        Eval.xyz(
          dataContext, ExecutionUtil, Fmt, "def dataContext = x; ExecutionUtil = y; Fmt = z;"
          + script
        )
      } catch(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        org.codehaus.groovy.runtime.StackTraceUtils.sanitize(e).printStackTrace(pw)

        this.test.hasFailedExec = true
        this.test.execError = [
          script: scriptObj.name,
          docIndex: dataContext.dcIndex,
          docName: dataContext.dataContextArr[dataContext.dcIndex].desc,
          error: sw.toString()
        ]
        // def padChar = "| "
        // println padChar + sw.toString().replaceAll(/\n/, "\n$padChar ").replaceAll(/\n.*?\(Unknown Source\)\n/, "\n").replaceFirst(/\$padChar\s*$/,"")
        // System.exit(1)

        // dataContext.close()
        break
      }

      // if (opts.printMode != "testResultsOnly" && outg.disjoint(["nothing"])) {
      //   println ""
      // }

    }
    // printResult()
    // if (opts.printMode != "testResultsOnly" && outg.disjoint(["nothing"])) {
    //   println ""
    // }

    return this.test
    dataContext.close()

  }

  private void printResult() {
    println ""
    Fmt.p("blue", "Test: ")
    Fmt.p("magenta", this.test.desc)
    println ""
    println this.test.execError
    // this.test.scripts.each { script ->
    //   println script.name
    //   println script.error
    //   println this.test.docFailed
    //
    // }
    println this.test.dataContext.dataContextArr.collect{ it.subMap(["desc", "assertions"])}
    println ""
    println "------------------"

  }


}

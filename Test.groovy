class Test {
  private def test = [:]
  private def opts = [:]

  Test (LinkedHashMap OPTIONS, String desc, ArrayList scripts, Properties dpps, DataContext2 dataContext) {
    this.test = [
      desc: desc,
      scripts: scripts,
      dpps: dpps,
      dataContext:dataContext
    ]
    this.opts = OPTIONS
  }

  def run() {

    if (opts.printMode != "testResultsOnly") {
      println Color.green + "-------------------------------------------------------------------------" + Color.off
    }

    def ExecutionUtil = new ExecutionUtilHelper()
    ExecutionUtil.dynamicProcessProperties = this.test.dpps

    def dataContext = this.test.dataContext

    this.test.scripts.eachWithIndex { scriptObj, k ->
      dataContext.setCurrentScriptName(scriptObj.name)

      if (opts.printMode != "testResultsOnly") {
        println ""
        println Color.blue + "TEST: " + Color.magenta + test.desc + Color.off
        println Color.blue + "SCRIPT: " + Color.magenta + scriptObj.name + Color.off
      }


      ArrayList out = scriptObj.output

      String script = scriptObj.script.text
      // remove ExecutionUtil import
      .replaceFirst(/import com\.boomi\.execution\.ExecutionUtil;?/, "")

      if (k == test.scripts.size() - 1) {
        script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
            "\$1; dataContext.evalAssertions(i, ExecutionUtil); ")
      }

      if (opts.printMode == "testResultsOnly") {
        script = script
        .replaceAll("println", "// println")

      }

      if (opts.printMode != "testResultsOnly") {
        // Document Number
        script = script
        .replaceFirst(/(.*dataContext.getDataCount\(\).*)/,
        "\$1; if (dataContext.getDataCount() > 1) println \"${Color.blue}DOCUMENT\" + i.toString() + \": ${Color.magenta}\" + dataContext.getDesc(i) + \"${Color.off}\"")

        if (!out.disjoint(["all", "props", "p", "dpp", "DPP", "dpps", "DPPs"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; ExecutionUtil.printDynamicProcessProperties(); ")
        }

        if (!out.disjoint(["all", "props", "p", "ddp", "ddps"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; dataContext.printProperties(i); ")
        }

        if (!out.disjoint(["all", "data", "d", "is"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; dataContext.printData(i); ")
        }

        if (!out.disjoint(["all", "assert", "assertions", "a"])) {
          script = script
          .replaceFirst(/(.*dataContext.storeStream.*)/,
          "\$1; dataContext.printAssertions(i); ")
        }
      }

      if (k == test.scripts.size() - 1) {
        script = script
        .replaceFirst(/(.*dataContext.storeStream.*)/,
        "\$1; if (dataContext.getExtension(i)) dataContext.writeFile(i, \"$test.testfilesDir\", \"$test.desc\", \"$scriptObj.name\"); ")
      }

      Eval.xyz(
        dataContext, ExecutionUtil, Fmt, "def dataContext = x; ExecutionUtil = y; Fmt = z;"
        + script
      )

      if (opts.printMode != "testResultsOnly") {
        println ""
      }

    }


    // for (int n = 0; n < dataContext.getDataCount(); n++) {
    //   println dataContext.getDesc(n)
    //   dataContext.getAssertions(n).each {
    //     println it.passed
    //     println it.error
    //   }
    //
    // }

    // return [
    //   desc: this.test.desc,
    //   scripts: this.test.scripts.name,
    //   documents: [
    //       this.test.dataContext.getAssertionResults()
    //   ]
    // ]
    return this.test
    dataContext.close()

  }
}

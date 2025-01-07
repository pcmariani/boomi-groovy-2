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
    LinkedHashMap globalOpts = Globals.optsMap ?: []
    ArrayList globalOptsKeys = globalOpts.keySet()

    if (!("no labels" in globalOptsKeys)) {
      if (this.index > 0) {
        println ""
        println Fmt.green + ("-"*Globals.termWidth) + Fmt.off
        println ""
      }
      Fmt.p("blue", "TEST: ")
      Fmt.pl("blue", this.desc)
    }

    def ExecutionUtil = new ExecutionUtilHelper()
    def ExecutionManager = new ExecutionManager(ExecutionUtil)
    ExecutionUtil.dynamicProcessProperties = this.dpps

    for (int k = 0; k < this.scripts.size(); k++) {

      def scriptObj = this.scripts[k]
      // dataContext.setCurrentScriptName(scriptObj.name)

      LinkedHashMap opts = globalOpts + scriptObj.opts
      ArrayList optsKeys = opts.keySet()

      if (this.scripts.size() > 1 && !("no labels" in optsKeys)) {
        println ""
        Fmt.p("magenta", scriptObj.name)
        Fmt.pl("grey", " - " + this.desc)
      } 

      // println Globals.workingDir

      String script = scriptObj.script.text
        // remove ExecutionUtil import
        .replaceFirst(/import com\.boomi\.execution\.ExecutionUtil;?/, "")
        .replaceFirst(/import com\.boomi\.execution\.ExecutionManager;?/, "")
        .replaceAll(/new File\(/, "new File(\"${Globals.workingDir}/\" + ")

      if (k == this.scripts.size() - 1) {
        String evalAssertions_replacement = " \
                  try { \
                      dataContext.evalAssertions(i, ExecutionUtil); \
                  } catch(Exception e) { \
                      dataContext.evalAssertions(dataContext.getDataCount()-1, ExecutionUtil); \
                  } ".replaceAll(/\s+/, " ")
        script = script
        .replaceAll(/(.*dataContext.storeStream.*)/, "\$1; $evalAssertions_replacement ")
      }

      if ("no println" in optsKeys) {
        script = script
        .replaceAll(/(.*?)println/, "// \$1 println")
      }

      if ("no logger" in optsKeys) {
        script = script
        .replaceAll(/(.*?)logger/, "// \$1 logger")
      }

      if (!("no labels" in optsKeys)) {
        script = script
        .replaceFirst(/(.*dataContext.getDataCount\(\).*)/,
        "\$1; if (dataContext.getDataCount() > 1) println \"${Fmt.blue}DOCUMENT \" + i.toString() + \"${Fmt.off}\"")
        // "\$1; if (dataContext.getDataCount() > 1) println \"${Fmt.blue}DOCUMENT\" + i.toString() + \": ${Fmt.magenta}\" + dataContext.getDesc(i) + \"${Fmt.off}\"")
      }

      if ("DPPs" in optsKeys) {
        script = script
        .replaceAll(/(.*dataContext.storeStream.*)/,
        "\$1; ExecutionUtil.printDynamicProcessProperties(\"${opts.DPPs.join(",")}\", true); ")
      }

      if ("ddps" in optsKeys) {
        String ddps_replacement = " \
                  try { \
                       dataContext.printProperties(i, \"${opts.ddps.join(",")}\", true); \
                  } catch(Exception e) { \
                       dataContext.printProperties(dataContext.getDataCount()-1, \"${opts.ddps.join(",")}\", true); \
                  } ".replaceAll(/\s+/, " ")
        // println ddps_replacement
        script = script
        .replaceAll(/(.*dataContext.storeStream.*)/, "\$1; $ddps_replacement")
      }

      if ("data" in optsKeys) {
        String data_replacement = " \
                  try { \
                      dataContext.printData(i); \
                  } catch(Exception e) { \
                      dataContext.printData(dataContext.getDataCount()-1); \
                  } ".replaceAll(/\s+/, " ")
        script = script
        .replaceAll(/(.*dataContext.storeStream.*)/, "\$1; $data_replacement")
      }

      if ("assertions" in optsKeys) {
        String assertions_replacement = " \
                  try { \
                      dataContext.printAssertions(i); \
                  } catch(Exception e) { \
                      dataContext.printAssertions(dataContext.getDataCount()-1); \
                  } ".replaceAll(/\s+/, " ")
        script = script
        .replaceAll(/(.*dataContext.storeStream.*)/, "\$1; $assertions_replacement")
      }

      if (!("no files" in optsKeys) && k == this.scripts.size() - 1) {
        String noFiles_replacement = " \
                  try { \
                      if (dataContext.getExtension(i)) dataContext.writeFile(i, \"${Globals.workingDir}\", \"$test.desc\", \"$scriptObj.name\"); \
                  } catch(Exception e) { \
                      if (dataContext.getExtension(dataContext.getDataCount()-1)) dataContext.writeFile(i, \"${Globals.workingDir}\", \"$test.desc\", \"$scriptObj.name\"); \
                  } ".replaceAll(/\s+/, " ")
        script = script
        .replaceAll(/(.*dataContext.storeStream.*)/, "\$1; $noFiles_replacement")
      }
      // println script

      try {
        Eval.xyz(
          this.dataContext,
          ExecutionUtil,
          ExecutionManager,
          "def dataContext = x; ExecutionUtil = y; def ExecutionManager = z; " + script
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

      if (!("no errors" in optsKeys)) {
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

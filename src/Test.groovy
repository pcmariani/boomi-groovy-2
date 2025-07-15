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
    String label_prefix_test = "# test:  "
    String label_prefix_script = "## script:  "
    String label_prefix_document = "### document:  "

    String label_suffix_test = " "
    String label_suffix_script = ""
    String label_suffix_document = ""

    String label_filler_char_test = " "
    String label_filler_char_script = "_"
    String label_filler_char_document = " "

    def outg = Globals.options ?: []
    LinkedHashMap globalOpts = Globals.optsMap ?: []
    ArrayList globalOptsKeys = globalOpts.keySet()

    if (!("no labels" in globalOptsKeys)) {
      // if (this.index > 0) {
      //   println ""
      //   println Fmt.green + ("-"*Globals.termWidth) + Fmt.off
      //   println ""
      // }
      String label_test = "${label_prefix_test}${this.desc}${label_suffix_test}"
      // String label_test = "${Fmt.yellow}${label_prefix_test}TEST${label_infix_test}${this.desc}${label_suffix}${Fmt.off}"
      // println label_test_size
      String label_filler_test = label_filler_char_test * (Globals.termWidth - label_test.size())
      println "${Fmt.yellowOnGrey}${label_test}${label_filler_test}${Fmt.off}"
      // println "${Fmt.yellowOnGrey}${label_test}${Fmt.off}"
      // Fmt.p("yellow", label_prefix_test + "TEST" + label_infix_test)
      // Fmt.pl("yellow", this.desc + label_suffix)
    }

    def execTaskCurrent = new ExecutionTask("L4", "component_4", "process_4", "exec_4")
    def execTaskL3c = new ExecutionTask("FWK L3 (Continuation)", "component_3c", "process_3c", "exec_3c")
    def execTaskL3 = new ExecutionTask("FWK L3", "component_3", "process_3", "exec_3")
    def execTaskL2c = new ExecutionTask("L2 (Continuation)", "component_2c", "process_2c", "exec_2c")
    def execTaskL2 = new ExecutionTask("L2", "component_2", "process_2", "exec_2")
    def execTaskL1c = new ExecutionTask("L1 (Continuation)", "component_1c", "process_1c", "exec_1c")
    def execTaskL1 = new ExecutionTask("L1", "component_1", "process_1", "exec_1")
    def execTaskTopLevel = new ExecutionTask("TopLevel", "component_0", "process_0", "exec_0")

    execTaskCurrent.setParent(execTaskL3c)
    execTaskL3c.setParent(execTaskL3)
    execTaskL3.setParent(execTaskL2c)
    execTaskL2c.setParent(execTaskL2)
    execTaskL2.setParent(execTaskL1c)
    execTaskL1c.setParent(execTaskL1)
    execTaskL1.setParent(execTaskTopLevel)
    execTaskTopLevel.setParent(null)

    def ExecutionManager = new ExecutionManager()
    def ExecutionUtil = new ExecutionUtilHelper()
    ExecutionManager.setExecutionUtil(ExecutionUtil)

    ExecutionUtil.dynamicProcessProperties = this.dpps

    for (int k = 0; k < this.scripts.size(); k++) {

      def scriptObj = this.scripts[k]
      // dataContext.setCurrentScriptName(scriptObj.name)

      LinkedHashMap opts = globalOpts + scriptObj.opts
      ArrayList optsKeys = opts.keySet()

      // if (this.scripts.size() > 1 && !("no labels" in optsKeys)) {
      if (!("no labels" in optsKeys)) {
        // println ""
        String label_script = "${label_prefix_script}${scriptObj.name}${label_suffix_script}"
        String label_filler_script = label_filler_char_script * (Globals.termWidth - label_script.size())
        // println "${Fmt.blueOnGrey}${label_script}${label_filler_script}"
        println "${Fmt.blueOnGrey}${label_script}${Fmt.off}" + (k > 0 ? "${Fmt.grey} - ${this.desc}" : "")
        // Fmt.p("magenta", label_prefix_script + "SCRIPT" + label_infix_script + scriptObj.name + label_suffix)
        // Fmt.pl("grey", " - " + this.desc)
      } 

      // println Globals.workingDir

      String script = scriptObj.script.text
        // remove ExecutionUtil import
        .replaceFirst(/import com\.boomi\.execution\.ExecutionUtil;?/, "")
        .replaceFirst(/import com\.boomi\.execution\.ExecutionManager;?/, "")
        .replaceFirst(/import com\.boomi\.execution\.ExecutionTask;?/, "")
        .replaceFirst(/ExecutionTask execTaskCurrent = ExecutionManager.getCurrent\(\);?/, "")
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
        // "\$1; if (dataContext.getDataCount() > 1) println \"${Fmt.blue}${label_prefix_document}DOCUMENT${label_infix_document}\" + dataContext.getDesc(i) + \"${label_suffix}${Fmt.off}\"")
        "\$1; "
        + "if (dataContext.getDesc(i) != \"unnamedDocumentOnTest\") {"
        + "String label_document = \"${label_prefix_document}\" + dataContext.getDesc(i) + \"${label_suffix_document}\";"
        + "String label_filler_document = \"${label_filler_char_document}\" * (${Globals.termWidth} - label_document.size());"
        + "println \"${Fmt.magentaOnGrey}\" + label_document + \"${Fmt.off}\" + label_filler_document; "
        + "}; println \"\" ")
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
        def context = [
          dataContext: this.dataContext,
          executionUtil: ExecutionUtil,
          executionManager: ExecutionManager,
          execTaskCurrent: execTaskCurrent
        ]

        Eval.me("ctx", context, "def dataContext = ctx.dataContext; ExecutionUtil = ctx.executionUtil; " +
        "def ExecutionManager = ctx.executionManager; def execTaskCurrent = ctx.execTaskCurrent; " + script)

        // Eval.xyz(
        //   this.dataContext,
        //   ExecutionUtil,
        //   ExecutionManager,
        //   "def dataContext = x; ExecutionUtil = y; def ExecutionManager = z; " + script
        // )
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

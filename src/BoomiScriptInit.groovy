class BoomiScriptRun {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'boomiScriptRun [-h] [testSuiteFileName]'
      // + ' | \n[-h] [-s script] [-d data] [-p properties] [-xp] [-xd]'
    )

    cli.with {
      h  longOpt: 'help', 'Show usage'
      f  longOpt: 'folder', type: boolean, 'Create folder with script name'
      w  longOpt: 'workingDir', args: 1, argName: 'dir', 'Present Working Directory'
      t  longOpt: 'testSuite', args: 1, argName: 'testSuite', 'Test Suite Filename'
      s  longOpt: 'script', args: 1, argName: 'script', 'Script Filename'
      d  longOpt: 'document', args: 1, argName: 'document', 'Document Filename'
      p  longOpt: 'properties', args: 1, argName: 'properties', 'Properties Filename'
      l  longOpt: 'lang', args: 1, argName: 'lang', 'Language of input: xml, json, ff(default)'
    }

    def options = cli.parse(args)

    if (options.h || !options) {
      cli.usage()
      return
    }

    File userDir = new File(System.getProperty("user.dir"))
    def templatesFolder = userDir.getParent().toString() + "/templates"

    def workingDir = options.w ?: System.getProperty("user.dir")
    def scriptBase = options.s ?: options.arguments()[0] -~/.groovy$/

    if (options.f) {
      workingDir += "/$scriptBase"
      File folder = new File(workingDir)
      if (folder.exists()) {
        println "INFO: Folder \'$scriptBase\' already exists - ABORTING"
        System.exit(0)
      }
      else {
        println "INFO: creating Folder $scriptBase"
        // folder.mkdir()
      }
    }

    println options.t
    def script = scriptBase + ".groovy"
    // println "script:    " + script
    def testSuite = "_test_" + (options.t != false ? options.t : scriptBase) + ".yaml"
    println "testSuite: " + testSuite
    def data = options.d ?: scriptBase + ".dat"
    // println "data:      " + data
    def props = options.p ?: scriptBase + ".properties"
    // println "props:     " + props
    def lang = options.l
    // println lang

    File scriptFile = new File(workingDir + "/" + script)
    if (scriptFile.exists()) {
      println "INFO: Script file exists - SKIPPING"
    } else {
      File templateFile
      if (lang) {
        templateFile = new File("$templatesFolder/_template_${lang}.b.groovy")
        if (!templateFile.exists()) {
          println "WARNING: \'$templateFile\' does not exist"
          println "WHAT TO DO NOW"
        }
      } else {
        templateFile = new File("$templatesFolder/template.b.groovy")
      }
      println "INFO: Writing \'${scriptFile.getName()}\'"
      scriptFile.write templateFile.text
    }

    File testSuiteFile = new File(workingDir + "/" + testSuite)
    println testSuiteFile
    if (testSuiteFile.exists()) {
      println "WARNING: Test file exists - SKIPPING"
    } else {
      def testSuiteTemplateFile = new File("$templatesFolder/template_testSuite.yaml")
      testSuiteFile.write testSuiteTemplateFile.text
        .replaceAll("###script", script)
        .replaceAll("###data", data)
        .replaceAll("###props", props)
    }



  }






    // String testSuiteFileName = options.arguments()[0]
    // OPTIONS.workingDir = options.workingDir ?: System.getProperty("user.dir")
    // OPTIONS.mode = "init"
    //
    // def initFile = options.i
    // Init.createFiles(options, initFile.replaceAll(" ", "_"))

}


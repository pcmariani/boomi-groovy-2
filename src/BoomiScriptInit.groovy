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

    def workingDir = options.w ?: System.getProperty("user.dir")

    File userDir = new File(System.getProperty("user.dir"))
    def templatesFolder = userDir.getParent().toString() + "/templates"

    String existingPropsFile
    String existingDataFile

    new File(workingDir).traverse(type: groovy.io.FileType.FILES, maxDepth: 0) { file ->
      if (file.getName() =~ /\.properties/) {
        existingPropsFile = file.getName()
      } else {
        existingDataFile = file.getName()
      }
    }
    // println propsFile
    // println dataFile

    // println ""
    print "Name your new boomi groovy script > "
    String scriptName = System.in.newReader().readLine()
    
    // println "Create a folder?"
    // String optCreateFolder = System.in.newReader().readLine()
    // println "Your name is ${System.in.newReader().readLine()}"
    // println name


    def scriptBase = scriptName ?: options.s ?: options.arguments()[0] -~/.groovy$/

    if (options.f) {
      workingDir += "/$scriptBase"
      File folder = new File(workingDir)
      if (folder.exists()) {
        println "INFO: Folder \'$scriptBase\' already exists - ABORTING"
        System.exit(0)
      }
      else {
        println "INFO: creating Folder $scriptBase"
        folder.mkdir()
      }
    }

    // println options.t
    def script = scriptBase + ".groovy"
    // println "script:    " + script
    def testSuite = "_test_" + (options.t != false ? options.t : scriptBase) + ".yaml"
    // println "testSuite: " + testSuite
    def data = existingDataFile ?: options.d ?: scriptBase + ".dat"
    // println "data:      " + data
    def props = existingPropsFile ?: options.p ?: scriptBase + ".properties"
    // println "props:     " + props
    def lang = existingDataFile -~/^.*\./ ?: options.l
    // println lang

    File testSuiteFile = new File(workingDir + "/" + testSuite)
    if (testSuiteFile.exists()) {
      println "INFO: Test file exists - SKIPPING"
    } else {
      def testSuiteTemplateFile = new File("$templatesFolder/_testSuite.yaml")
      println "INFO: Writing \'${testSuiteFile.getName()}\'"
      testSuiteFile.write testSuiteTemplateFile.text
      .replaceAll("###script", script)
      .replaceAll("###data", data)
      .replaceAll("###props", props)
    }


    File scriptFile = new File(workingDir + "/" + script)
    if (scriptFile.exists()) {
      println "INFO: Script file exists - SKIPPING"
    } else {
      File scriptTemplateFile
      if (lang) {
        scriptTemplateFile = new File("$templatesFolder/_script_${lang}.groovy")
        if (!scriptTemplateFile.exists()) {
          println "WARNING: \'$scriptTemplateFile\' does not exist"
          println "WHAT TO DO NOW"
        }
      } else {
        scriptTemplateFile = new File("$templatesFolder/_script_ff.groovy")
      }
      println "INFO: Writing \'${scriptFile.getName()}\'"
      scriptFile.write scriptTemplateFile.text
    }


    File dataFile = new File(workingDir + "/" + data)
    if (dataFile.exists()) {
      println "INFO: data file exists - SKIPPING"
    } else {
      File dataTemplateFile
      if (lang) {
        dataTemplateFile = new File("$templatesFolder/_data_${lang}.groovy")
        if (!dataTemplateFile.exists()) {
          println "WARNING: \'$dataTemplateFile\' does not exist"
          println "WHAT TO DO NOW"
        }
      } else {
        dataTemplateFile = new File("$templatesFolder/_data.csv")
      }
      println "INFO: Writing \'${dataFile.getName()}\'"
      dataFile.write dataTemplateFile.text
    }

    if (props) {
      File propsFile = new File(workingDir + "/" + props)
      if (propsFile.exists()) {
        println "INFO: props file exists - SKIPPING"
      } else {
        File propsTemplateFile = new File("$templatesFolder/_props.properties")
        println "INFO: Writing \'${propsFile.getName()}\'"
        propsFile.write propsTemplateFile.text
      }
    }


  }






    // String testSuiteFileName = options.arguments()[0]
    // OPTIONS.workingDir = options.workingDir ?: System.getProperty("user.dir")
    // OPTIONS.mode = "init"
    //
    // def initFile = options.i
    // Init.createFiles(options, initFile.replaceAll(" ", "_"))

}


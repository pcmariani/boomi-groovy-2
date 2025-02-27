class BoomiScriptRun {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'boomiScriptRun [-h] [testSuiteFileName]'
      // + ' | \n[-h] [-s script] [-d data] [-p properties] [-xp] [-xd]'
    )

    cli.with {
      h   longOpt: 'help', 'Show usage'
      dbg longOpt: 'debug', type: boolean, argName: 'debug', 'Print Debug Info'
      s   longOpt: 'script', args: 1, argName: 'script', 'If not using a testsuite file: Script Filename'
      d   longOpt: 'document', args: 1, argName: 'document', 'If not using a testsuite file: Document Filename'
      p   longOpt: 'properties', args: 1, argName: 'properties', 'If not using a testsuite file: Properties Filename'
      i   longOpt: 'init', type: boolean, argName: 'init', 'Intialize new Boomi scripting project'
      l   longOpt: 'lang', args: 1, argName: 'lang', 'Language of input: xml, json, ff(default)'
      // xd longOpt: 'suppress-data-output', type: boolean, 'Suppress data output (can also be done inside OPTIONS in a testsuite file)'
      // xp longOpt: 'suppress-props-output', type: boolean, 'Suppresses props output (can also be done inside OPTIONS in a testsuite file)'
    }

    def options = cli.parse(args)

    if (options.h || !options || options.arguments().size() > 1) {
      cli.usage()
      return
    }

    Globals Globals = new Globals(options)

    // if (options.init) {
    if (options.arguments()[0] == "init") {

      println "${Fmt.magenta}INFO: Starting init...${Fmt.off}"
      print "> What flavor? (none,json,xml,csv) > "
      String lang = System.in.newReader().readLine()
      if (lang == "none") {
        lang = "dat"
      }
      // println "lang: $lang"

      print "> Name your new boomi groovy script > "
      String script = System.in.newReader().readLine()
      script = script.replaceFirst(/\.groovy$/, "") + ".groovy"
      // String script = "myNewScript.groovy"

      // println "INFO: Script name: " + script
      // print "> Create new folder? (y/n) > "
      // Boolean createNewFolder = System.in.newReader().readLine().toBoolean()
      // String createNewFolder = 'yes'
      String createNewFolder = 'no'
      if (createNewFolder =~ /(?i)^y/) {
        print "> Folder name > "
        String newFolderName = System.in.newReader().readLine()
        // String newFolderName = "newFolderTest"
        File folder = new File(Globals.workingDir + "/" + newFolderName)
        // println Globals.workingDir
        // println folder
        if (!folder.exists()) {
          folder.mkdir()
          println "INFO: Folder \"${newFolderName}\" created"
        } else {

          println "INFO: Folder \"${newFolderName}\" already exists. Continuing..."
        }
        Globals.workingDir = folder.toString()
        println Globals.workingDir
      }

      print "> Create separate properties file? (y/n) > "
      Boolean createPropertiesFile = System.in.newReader().readLine().toBoolean()

      Boolean includeSampleProps = true
      if (!createPropertiesFile) {
        print "> Include Sample properties in test suite file? > "
        includeSampleProps = System.in.newReader().readLine().toBoolean()
      }

      // def lang = options.l ?: ""
      // println "lang: $lang"
      def testSuite = "test.boomi.yaml"
      // println "testSuite: " + testSuite
      def data = "doc1." + (lang ?: "dat")
      // println "data:      " + data
      def props = "doc1.properties"
      // println "props:     " + props
      File userDir = new File(System.getProperty("user.dir"))
      def templatesFolder = userDir.getParent().toString() + "/templates"


      File testSuiteFile = new File(Globals.workingDir + "/" + testSuite)
      if (testSuiteFile.exists()) {
        println "INFO: Test file exists - SKIPPING"
      } else {
        def testSuiteTemplateFile = new File("$templatesFolder/_testSuite.yaml")
        println "INFO: Writing \'${testSuiteFile.getName()}\'"
        String testSuiteFileToWrite = testSuiteTemplateFile.text
        .replaceAll("###script", script)
        .replaceAll("###data", data)

        if (createPropertiesFile) {
          testSuiteFileToWrite = testSuiteFileToWrite
          .replaceAll("###DPPs", props)
          .replaceAll("###ddps", props)
        } else if (includeSampleProps) {
          testSuiteFileToWrite = testSuiteFileToWrite
          .replaceAll("###DPPs", "\n" \
            + "    DPP_ExampleDPP: Example DPP Value\n")
          .replaceAll("###ddps", "\n" \
            + "      ddp_exampleDdp: Example ddp Value\n" \
            + "      ddp_exampleDdpFromFile: example_ddp_file.txt")
        }
        testSuiteFile.write testSuiteFileToWrite
      }


      File scriptFile = new File(Globals.workingDir + "/" + script)
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
          scriptTemplateFile = new File("$templatesFolder/_script_dat.groovy")
        }
        println "INFO: Writing \'${scriptFile.getName()}\'"
        scriptFile.write scriptTemplateFile.text
      }


      File dataFile = new File(Globals.workingDir + "/" + data)
      if (dataFile.exists()) {
        println "INFO: data file exists - SKIPPING"
      } else {
        File dataTemplateFile
        if (lang) {
          dataTemplateFile = new File("$templatesFolder/_data.${lang}")
          if (!dataTemplateFile.exists()) {
            println "WARNING: \'$dataTemplateFile\' does not exist"
            println "WHAT TO DO NOW"
          }
        } else {
          dataTemplateFile = new File("$templatesFolder/_data.dat")
        }
        println "INFO: Writing \'${dataFile.getName()}\'"
        dataFile.write dataTemplateFile.text
      }

      if (createPropertiesFile) {
        File propsFile = new File(Globals.workingDir + "/" + props)
        if (propsFile.exists()) {
          println "INFO: props file exists - SKIPPING"
        } else {
          File propsTemplateFile = new File("$templatesFolder/_props.properties")
          // println "INFO: Writing \'${propsFile.getName()}\'"
          propsFile.write propsTemplateFile.text
        }
      }

      println "INFO: Done."


      return
    }

    if (Globals.testSuiteFileName) {
      TestSuiteRunner tsr = new TestSuiteRunner().runTestSuite()
    }

    else {

      String testSuiteText = '''
        OPTS:
          - data
          - DPPs
          - ddps
        Boomi Run:
          script: ''' + options.s + '''
          ''' + ( options.p ? "DPPs: " + options.p : "" ) + '''
          document 0
            ''' + ( options.p ? "ddps: " + options.p : "" ) + '''
            ''' + ( options.d ? "data: " + options.d : "" ) + '''
      '''

      TestSuite ts = new TestSuite(testSuiteText)
      ts.run()
    }

  }
}


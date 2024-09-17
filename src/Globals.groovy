class Globals {

  static String os
  static Boolean debug
  static String workingDir
  static String testSuiteFileName
  static String mode
  static Set options = []
  static LinkedHashMap optsMap = [:]
  static ArrayList scripts
  static def DPPs
  static def DPPsOverride
  static String testFilesDir
  static int termWidth

  Globals (options) {
    // println options.arguments()
    // println options.s
    debug = options.debug
    testSuiteFileName = options.arguments()[0]

    mode = "run"

    this.os = System.getProperty("os.name")
    if (!os.contains("Windows")) {
      this.termWidth = System.getenv('COLS') as int
      this.workingDir = System.getenv('WORKING_DIR')
    }
    // else if (os.contains("Windows")) {
    // }
  }

  // static setOptionsFromMode(mode) {
  //   if (mode == "testResultsOnly") {
  //     this.options.addAll(["no labels", "no println", "no files", "no errors", "no assertions"])
  //   }
  // }

}

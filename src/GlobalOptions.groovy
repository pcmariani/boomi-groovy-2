class GlobalOptions {
    static String workingDir
    static String mode
    static Set suiteOpts = []
    static ArrayList scripts = []
    static def processProps
    static String testFilesDir


    static setSuiteOptsFromMode(mode) {
      if (mode == "testResultsOnly") {
        this.suiteOpts.addAll(["no guides", "no results", "no data", "no props", "no assertions", "no println", "no errors", "no files"])
      }
    }
}

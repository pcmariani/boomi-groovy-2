class Globals {
    static String workingDir
    static String mode
    static Set suiteOpts = []
    static ArrayList scripts
    static def DPPs
    static def DPPsOverride
    static String testFilesDir

    static setSuiteOptsFromMode(mode) {
      if (mode == "testResultsOnly") {
        this.suiteOpts.addAll(["no guides", "no results", "no data", "no props", "no assertions", "no println", "no errors", "no files"])
      }
    }
}

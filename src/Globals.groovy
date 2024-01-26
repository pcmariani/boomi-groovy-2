class Globals {
    static String workingDir
    static String testSuiteFileName
    static String mode
    static Set options = []
    static ArrayList scripts
    static def DPPs
    static def DPPsOverride
    static String testFilesDir

    static setOptionsFromMode(mode) {
      if (mode == "testResultsOnly") {
        this.options.addAll(["no guides", "no results", "no data", "no props", "no assertions", "no println", "no errors", "no files"])
      }
    }
}

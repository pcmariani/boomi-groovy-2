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
      this.os = System.getProperty("os.name")
      if (!os.contains("Windows")) {
        this.termWidth = System.getenv('COLS') as int
        this.workingDir = System.getenv('WORKING_DIR')
      }
      // else if (os.contains("Windows")) {
      // }
    }

    static setOptionsFromMode(mode) {
      if (mode == "testResultsOnly") {
        this.options.addAll(["no labels", "no println", "no files", "no errors", "no assertions"])
      }
    }


    // static WinSize () {
      /*
      /bin/stty -echo; /bin/stty -icanon; /bin/stty min 1; java WinSize; /bin/stty echo; /bin/stty icanon;
       */
      // String[] signals = new String[] {
      //   "\u001b[s",            // save cursor position
      //   "\u001b[5000;5000H",   // move to col 5000 row 5000
      //   "\u001b[6n",           // request cursor position
      //   "\u001b[u",            // restore cursor position
      // };
      // for (String s : signals) {
      //   System.out.print(s);
      // }
      // int read = -1;
      // StringBuilder sb = new StringBuilder();
      // byte[] buff = new byte[1];
      // while ((read = System.in.read(buff, 0, 1)) != -1) {
      //   sb.append((char) buff[0]);
      //   //System.err.printf("Read %s chars, buf size=%s%n", read, sb.length());
      //   if ('R' == buff[0]) {
      //     break;
      //   }
      // }
      // String size = sb.toString();
      // int rows = Integer.parseInt(size.substring(size.indexOf("\u001b[") + 2, size.indexOf(';')));
      // int cols = Integer.parseInt(size.substring(size.indexOf(';') + 1, size.indexOf('R')));
      // System.err.printf("rows = %s, cols = %s%n", rows, cols);
    // }
}

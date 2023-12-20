import groovy.json.JsonSlurper
import groovy.json.JsonOutput;

class Boomi_groovy {
  static void main(String[] args) throws Exception {

    def cli = new CliBuilder(usage: 'boomi-groovy-test.groovy [-h] -t file -s script [-d document]')

    cli.with {
      h  longOpt: 'help', 'Show usage'
      t  longOpt: 'testfile', args: 1, argName: 'testFile', 'test file'
      m  longOpt: 'mode', args: 1, argName: 'mode', 'Mode'
      xa longOpt: 'suppress-all-output', type: boolean, 'Suppress all output'
      xd longOpt: 'suppress-data-output', type: boolean, 'Suppresses output of data'
      xp longOpt: 'suppress-props-output', type: boolean, 'Suppresses output of props'
      w  longOpt: 'working-dir', args: 1, argName: 'dir', 'Present Working Directory'
    }

    def options = cli.parse(args)

    if (options.h || !options.t || !options) {
      cli.usage()
      return
    }


    String workingDir = options.w ? options.w : System.getProperty("user.dir")
    ArrayList testFiles = [options.t ? options.t : null]

    ExecutionManager executionManager = new ExecutionManager(workingDir)

    testFiles.each { testFile ->
      executionManager.runTestSuite(options.m, testFile)
    }

    if (options.m == "testResultsOnly") {
      executionManager.printResults()
    }
  

  }

  static String pprettyJson(def thing) {
    return JsonOutput.prettyPrint(JsonOutput.toJson(thing))
  }
}

class Fmt {
  static String json(def thing) {
    return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(thing))
  }
  static void pl(def color, def str) {
    println Color[color] + str + Color.off
  }
  static void p(def color, def str) {
    print Color[color] + str + Color.off
  }
}

class Color {
  static String red = "${(char)27}[31m"
  static String green = "${(char)27}[32m"
  static String yellow = "${(char)27}[33m"
  static String blue = "${(char)27}[34m"
  static String magenta = "${(char)27}[35m"
  static String cyan = "${(char)27}[36m"
  static String grey = "${(char)27}[90m"
  static String white = "${(char)27}[97m"
  static String redReverse = "${(char)27}[31;7m"
  static String greenReverse = "${(char)27}[32;7m"
  static String off = "${(char)27}[39;49;27m"
}


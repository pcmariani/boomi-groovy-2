import groovy.json.JsonSlurper
import groovy.json.JsonOutput;

class Fmt {

  static String l1 = "  "
  static String l2 = "     "
  static String l3 = "       "
  static String l4 = "          "
  static String l5 = "            "
  static String l6 = "              "

  static String red =           "${(char)27}[31m"
  static String green =         "${(char)27}[32m"
  static String yellow =        "${(char)27}[33m"
  static String blue =          "${(char)27}[34m"
  static String magenta =       "${(char)27}[35m"
  static String cyan =          "${(char)27}[36m"
  static String grey =          "${(char)27}[90m"
  static String white =         "${(char)27}[97m"
  static String redReverse =    "${(char)27}[31;7m"
  static String greenReverse =  "${(char)27}[32;7m"
  static String off =           "${(char)27}[39;49;27m"

  static colors = [
    red:          red,
    green:        green,
    yellow:       yellow,
    blue:         blue,
    magenta:      magenta,
    cyan:         cyan,
    grey:         grey,
    white:        white,
    redReverse:   redReverse,
    greenReverse: greenReverse,
    off:          off
  ]

  static void pl(def color, def str) {
    println colors[color] + str + colors.off
  }
  static void p(def color, def str) {
    print colors[color] + str + colors.off
  }

  static String toPrettyJson(def thing) {
    def root = new JsonSlurper().parseText(thing)
    return JsonOutput.prettyPrint(JsonOutput.toJson(root))
  }

  static String json(def thing) {
    return JsonOutput.prettyPrint(JsonOutput.toJson(thing))
  }
}


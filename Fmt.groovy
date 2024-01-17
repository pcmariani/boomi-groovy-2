import groovy.json.JsonOutput;

class Fmt {

  static String l1 = "  "
  static String l2 = "     "
  static String l3 = "       "
  static String l4 = "          "
  static String l5 = "            "
  static String l6 = "              "

  static colors = [
    red:          "${(char)27}[31m",
    green:        "${(char)27}[32m",
    yellow:       "${(char)27}[33m",
    blue:         "${(char)27}[34m",
    magenta:      "${(char)27}[35m",
    cyan:         "${(char)27}[36m",
    grey:         "${(char)27}[90m",
    white:        "${(char)27}[97m",
    redReverse:   "${(char)27}[31;7m",
    greenReverse: "${(char)27}[32;7m",
    off:          "${(char)27}[39;49;27m"
  ]

  static void pl(def color, def str) {
    println colors[color] + str + colors.off
  }
  static void p(def color, def str) {
    print colors[color] + str + colors.off
  }


  static String json(def thing) {
    return JsonOutput.prettyPrint(JsonOutput.toJson(thing))
  }
}


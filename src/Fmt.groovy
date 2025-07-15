import groovy.json.JsonSlurper
import groovy.json.JsonOutput;

class Fmt {


  static String l1 = "  "
  static String l2 = "     "
  static String l3 = "       "
  static String l4 = "          "
  static String l5 = "            "
  static String l6 = "              "

  static String red             = "${(char)27}[31m"
  static String green           = "${(char)27}[32m"
  static String yellow          = "${(char)27}[0;33m"
  // static String yellowOnGrey    = "${(char)27}[2;33;40m"
  static String yellowOnGrey    = "${(char)27}[37m"
  static String blue            = "${(char)27}[34m"
  static String blueOnGrey      = "${(char)27}[37m"
  static String magenta         = "${(char)27}[35m"
  static String magentaOnGrey   = "${(char)27}[37m"
  static String cyan            = "${(char)27}[36m"
  static String grey            = "${(char)27}[90m"
  static String white           = "${(char)27}[97m"
  static String redReverse      = "${(char)27}[31;7m"
  static String blueReverse     = "${(char)27}[34;7m"
  static String greyReverse     = "${(char)27}[37;7m"
  static String greenReverse    = "${(char)27}[32;7m"
  static String magentaReverse  = "${(char)27}[35;7m"
  static String off             = "${(char)27}[0;39;49;27m"

  static colors = [
    red:             red,
    green:           green,
    yellow:          yellow,
    blue:            blue,
    magenta:         magenta,
    cyan:            cyan,
    grey:            grey,
    white:           white,
    redReverse:      redReverse,
    blueReverse:     redReverse,
    greenReverse:    greenReverse,
    magentaReverse:  greenReverse,
    off:             off
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

  static String wrapText(String indent="", String color="", String text) {
    String NEWLINE = System.lineSeparator()
    int width = Globals.termWidth - indent.size() - 1
    int segmentStart = 0
    StringBuilder sb = new StringBuilder()

    sb.append(color)
    while (segmentStart < text.size()) {
      if (segmentStart + width < text.size()) {
        sb.append(indent + text[segmentStart..segmentStart + width] + NEWLINE)
      }
      else {
        sb.append(indent + text[segmentStart..text.size()-1])
      }
      segmentStart += width
    }
    sb.append(off)
    return sb.toString()
  }


  static String out(String indent="", def stringIn) {
    String NEWLINE = System.lineSeparator()
    int width = Globals.termWidth - indent.size() - 1
    StringBuilder sb = new StringBuilder()

    // println stringIn.getClass()
    def stringArr = []
    if (stringIn instanceof String) {
      if (stringIn =~ /^[\[\{]/) {
        stringArr = "\n" + Fmt.toPrettyJson(stringIn).split(/\n/)
      }
    }
    else {
      stringArr = stringIn
    }
    // stringArr = [
    //   [Fmt.green, "Hello my name is inigo montoya and the princess bride was a great movie."],
    //   [Fmt.blue, " Prepare to die."],
    //   [Fmt.red, " You killed my father, you are the six fingered man."],
    //   [Fmt.magenta, "\nq1a1aq1a1aone\ntwooo\nthree"],
    // ]



    int start = 0

    stringArr.eachWithIndex { str, j ->
      def color = str[0]
      def text = str[1]//  - ~/^ {7}/

      int remainder = text.size()
      // println text - ~/^ {7}/

      sb.append(color)

      while (remainder > 0) {

        // def multi = text.split('\n')
        // println multi
        // if (multi.size() > 1) {
        //   multi[0..-2].each {
        //     sb.append(indent + it + NEWLINE)
        //   }
        //   sb.append(indent)
        //   text = multi[-1]
        //   remainder = text.size()
        // } else {
        //
        // }

        // println remainder + " " + str
        if (text.startsWith("\n")) {
          // println "SW0"
          remainder = text.size()
          start = 0
        }

        if (start == 0) {
          sb.append(indent)
        }

        if (remainder > width - start) {
          sb.append(text[text.size()-remainder..width - start - remainder] + NEWLINE)
          remainder -= width - start + 1
          start = 0
        }
        else {
          sb.append(text[text.size()-remainder..-1])
          start += remainder
          remainder = 0
        }
      }

      sb.append(off)
    }

    return sb.toString()
  }

}


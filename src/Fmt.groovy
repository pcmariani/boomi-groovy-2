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


  static String out(String indent="", ArrayList stringArr=[]) {
    String NEWLINE = System.lineSeparator()
    int width = Globals.termWidth - indent.size() - 1
    StringBuilder sb = new StringBuilder()
    String outText = ""

    stringArr = [
      // [Fmt.blue, " prepare to die"],
      [Fmt.green, "hello my name is inigo montoya: hello my name is inigo montoya: hello my name is inigo montoya"],
      // [Fmt.red, " you killed my father: you killed my father: you killed my father: you killed my father"],
    ]
    int remainder = 0
    stringArr.eachWithIndex { str, j ->
      def color = str[0]
      def text = str[1]

      sb.append(color)

      int segmentStart = 0
      while (segmentStart < text.size()) {

        def wrapLimit = segmentStart + width - remainder
        println ""
        // println "remainder " + remainder
        println "width " + width
        println "wrapLimit " + wrapLimit
        println "textSize " + text.size()
        // println "segmentStart " + segmentStart
        // println "segStart + textSize: " + (segmentStart + text.size())
        // println "segStart + textSize: " + (segmentStart + text.size())

        if (remainder == 0) {
          println "INDENT"
          sb.append(indent) 
          if (text[segmentStart] == " ") {
            segmentStart += 1
          }
        }

        if (indent.size() + text.size() < width) {
          println "TOO SHORT TO WRAP"
        }
        else if (indent.size() + text.size() > wrapLimit) {
          println "WRAPS WITH MORE LINES"
        }
        else if (indent.size() + text.size() > wrapLimit) {
          println "LAST WRAPPED LINE"
        }

        if (segmentStart + width - remainder < text.size()) {
          def segmentEnd = segmentStart + width - remainder
          println "COND 1"
          // if (text[segmentEnd-1].contains(" ") && !text[segmentEnd].contains(" ")) {
          //   // segmentEnd -=1
          //   // segmentStart -=1
          // }
          // println text[segmentStart + width - remainder-1..segmentStart + width - remainder]
          sb.append(text[segmentStart..segmentEnd] + NEWLINE)
          segmentStart += width - remainder + 1
        }

        else {
          println "COND 2"
          if (text.size() < remainder) {
            sb.append(text[segmentStart..text.size()-1])
            println "LESS"
            remainder = 0
            // remainder = text.size() - segmentStart
            // remainder = width + text.size() - segmentStart
          } else {
            sb.append(text[segmentStart..text.size()-1])
            remainder = text.size() - segmentStart
          }
          segmentStart += width
        }
        println "remainder " + remainder

      }
      sb.append(off)

    }

    return sb.toString()

  }

}


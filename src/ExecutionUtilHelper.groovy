import java.util.logging.Logger;

class ExecutionUtilHelper {
    static def dynamicProcessProperties = new Properties();

    static void setDynamicProcessProperty(String key, String value, boolean persist) {
        this.dynamicProcessProperties.setProperty(key, value)
    }

    static def getDynamicProcessProperty(String key) {
        return this.dynamicProcessProperties.getProperty(key)
    }

    static void printDynamicProcessProperties(String DPPsList, Boolean isPrintingDataAlso) {
      if (DPPsList) {
        ArrayList DPPsArr = DPPsList.split(",")
        DPPsArr.each { DPPName ->
          if (DPPName in this.dynamicProcessProperties.keySet()) {
            println Fmt.cyan + DPPName + Fmt.blue + ": " + Fmt.off + this.dynamicProcessProperties[DPPName]
          } else {
            println Fmt.red + DPPName + Fmt.blue + ": " + Fmt.yellow + "<-- NO DPP WITH THIS NAME" + Fmt.off
          }
        }
      }
      else {
        this.dynamicProcessProperties.each { k,v ->
          println Fmt.cyan + k + Fmt.blue + ": " + Fmt.off + v
        }
        if (this.dynamicProcessProperties && isPrintingDataAlso) println ""
      }
    }

    static Logger getBaseLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5\$s %n")
        return Logger.getAnonymousLogger()
    }
}

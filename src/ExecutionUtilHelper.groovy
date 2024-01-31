import java.util.logging.Logger;

class ExecutionUtilHelper {
    static def dynamicProcessProperties = new Properties();

    static void setDynamicProcessProperty(String key, String value, boolean persist) {
        this.dynamicProcessProperties.setProperty(key, value)
    }

    static def getDynamicProcessProperty(String key) {
        return this.dynamicProcessProperties.getProperty(key)
    }

    static void printDynamicProcessProperties(Boolean isPrintingDataAlso) {
      // println out
        this.dynamicProcessProperties.each { k,v ->
            println Fmt.cyan + k.replaceFirst("document.dynamic.userdefined.","") + Fmt.blue + ": " + Fmt.off + v
        }
        if (this.dynamicProcessProperties && isPrintingDataAlso) println ""
    }

    static Logger getBaseLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5\$s %n")
        return Logger.getAnonymousLogger()
    }
}

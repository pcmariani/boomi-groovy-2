import java.util.logging.Logger;

class ExecutionUtilHelper {
    static def dynamicProcessProperties = new Properties();

    static void setDynamicProcessProperty(String key, String value, boolean persist) {
        dynamicProcessProperties.setProperty(key, value)
    }

    static def getDynamicProcessProperty(String key) {
        return dynamicProcessProperties.getProperty(key)
    }

    static void printDynamicProcessProperties() {
        dynamicProcessProperties.each { k,v ->
            println Fmt.cyan + k.replaceFirst("document.dynamic.userdefined.","") + Fmt.blue + ": " + Fmt.off + v
        }
        println ""
    }

    static Logger getBaseLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5\$s %n")
        return Logger.getAnonymousLogger()
    }
}

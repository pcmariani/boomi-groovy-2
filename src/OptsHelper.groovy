class OptsHelper {

  static LinkedHashMap processOpts(ArrayList opts) {
    def optsMap = [:]
    if (opts instanceof String) {
      throw new Exception("Problem with OPTS. OPTS can't be a String. OPTS must be an Array, which can Strings and Maps.")
    }
    opts.each { opt ->
      // println opt
      if (opt instanceof String) {
        optsMap["$opt"] = []
      }
      else if (opt instanceof LinkedHashMap) {
        def key = opt.keySet()[0]
        def val = opt[key]
        if (!val) {
          optsMap["$key"] = []
        }
        else if (val instanceof String) {
          optsMap["$key"] = [val]
        }
        else if (val instanceof ArrayList) {
          optsMap["$key"] = val
        }
        else {
          throw new Exception("Problem with OPTS. $key can't contain a Map. Use a String or Array instaed")
        }
      }
    }
    // println optsMap
    return optsMap
  }

}

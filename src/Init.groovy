class Init {
  static void createFiles(String workingDir, String initFile) {

    def script = initFile + ".groovy"
    def test = "_test_" + initFile + ".yaml"
    def data = initFile + ".dat"
    def props = initFile + ".properties"
    
    File scriptFile = new File(workingDir + "/" + script)
    if (scriptFile.exists()) {
      println "WARNING: Script file exists - SKIPPING"
    } else {
      scriptFile.write '''import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

logger = ExecutionUtil.getBaseLogger()

def NEWLINE = System.lineSeparator()
def IFS = /\\|\\^\\|/  // Input Field Separator
def OFS = "|^|"     // Output Field Separator

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    def reader = new BufferedReader(new InputStreamReader(is))
    def outData = new StringBuffer()

    while ((line = reader.readLine()) != null ) {
        outData.append(line + NEWLINE)
    }

    is = new ByteArrayInputStream(outData.toString().getBytes("UTF-8"));
    dataContext.storeStream(is, props);
}
    '''
    }


    File testFile = new File(workingDir + "/" + test)
    if (testFile.exists()) {
      println "WARNING: Test file exists - SKIPPING"
    } else {
      testFile.write '''OPTIONS:
  # - no data
  # - no props
  # - no assertions
  # - no guides

GLOBALS:
  scripts:
    - ''' + script + '''
# ---

Test 1:
  dppsFile: ''' + props + '''
  docs:
    - datafile: ''' + data + '''
      propsfile: ''' + props + '''
# ---
    '''
    }


    File dataFile = new File(workingDir + "/" + data)
    if (dataFile.exists()) {
      println "WARNING: Data file exists - SKIPPING"
    } else {
       dataFile.write '''Sample Data'''
    }


    File propsFile = new File(workingDir + "/" + props)
    if (propsFile.exists()) {
      println "WARNING: Props file exists - SKIPPING"
    } else {
      propsFile.write '''DPP_hello=world
document.dynamic.userdefined.ddp_test=sample dynamic document property
    '''
    }
  }
}




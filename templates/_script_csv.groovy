import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

logger = ExecutionUtil.getBaseLogger()

def NEWLINE = System.getProperty("line.separator")
def IFS = /\|\^\|/  // Input Field Separator
def OFS = "|^|"     // Output Field Separator

// String exampleDynamicProcessProp = ExecutionUtil.getDynamicProcessProperty("DPP_ExampleDPP") ?: ""
// ExecutionUtil.setDynamicProcessProperty("DPP_ExampleDPP", exampleDynamicProcessProp, false)

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)

    // String exampleDynamicDocumentProp = props.getProperty("document.dynamic.userdefined.ddp_exampleDdp") ?: ""
    // props.setProperty("document.dynamic.userdefined.ddp_exampleDdp", exampleDynamicDocumentProp)

    reader = new BufferedReader(new InputStreamReader(is))
    outData = new StringBuffer()

    while ((line = reader.readLine()) != null ) {
        def lineArr = line.split(/\s*$IFS\s*/)
        outData.append(lineArr.join(OFS) + NEWLINE)
    }

    is = new ByteArrayInputStream(outData.toString().getBytes("UTF-8"))
    dataContext.storeStream(is, props)
}


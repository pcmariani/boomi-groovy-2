import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

logger = ExecutionUtil.getBaseLogger()

// String exampleDynamicProcessProp = ExecutionUtil.getDynamicProcessProperty("DPP_ExampleDPP") ?: ""
// ExecutionUtil.setDynamicProcessProperty("DPP_ExampleDPP", exampleDynamicProcessProp, false)

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i)
    Properties props = dataContext.getProperties(i)

    // String exampleDynamicDocumentProp = props.getProperty("document.dynamic.userdefined.ddp_exampleDdp") ?: ""
    // props.setProperty("document.dynamic.userdefined.ddp_exampleDdp", exampleDynamicDocumentProp)

    dataContext.storeStream(is, props)
}


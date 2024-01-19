/* @data
@file('')
*/
/* @props
    #DPP_Key=val
    #document.dynamic.userdefined.ddp_Key=val
*/
import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

logger = ExecutionUtil.getBaseLogger();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    def root = new XmlParser().parse(is)

    

    def outData = groovy.xml.XmlUtil.serialize(root).replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim()

    is = new ByteArrayInputStream(outData.toString().getBytes("UTF-8"));
    dataContext.storeStream(is, props);
}



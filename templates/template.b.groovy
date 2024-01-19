/* @params-----------------------------------------------------
        print_pretty = true
        print_dynDocumentProps = f
        print_dynProcessProps = true
        useInlineProps = true
 */
/* @props------------------------------------------------------
    # Dynamic Document Properites
        document.dynamic.userdefined.DDP1 = docProp1
        document.dynamic.userdefined.DDP2 = docProp2

    # Dynamic Process Properties
        dpp1 = dpp1val
        dpp2 = dpp2val
        #dpp3 = @file("somefilename.something")
 */
import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;
logger = ExecutionUtil.getBaseLogger();

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    dataContext.storeStream(is, props);
}
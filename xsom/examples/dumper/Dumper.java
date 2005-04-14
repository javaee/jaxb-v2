import java.io.File;
import java.io.OutputStreamWriter;

import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * Parses all the schemas specified as the command line arguments,
 * then dumps it (to see if the parsing was done correctly.)
 */
public class Dumper {
    public static void main(String[] args) throws Exception {
        XSOMParser reader = new XSOMParser();
        // set an error handler so that you can receive error messages
        reader.setErrorHandler(new ErrorReporter(System.out));
        
        try {
            // the parse method can by called many times
            for( int i=0; i<args.length; i++ )
                reader.parse(new File(args[i]));
            
            XSSchemaSet xss = reader.getResult();
            if(xss==null)
                System.out.println("error");
            else
                new SchemaWriter(new OutputStreamWriter(System.out)).visit(xss);
        } catch( SAXException e ) {
            if(e.getException()!=null)
                e.getException().printStackTrace();
            else
                e.printStackTrace();
            throw e;
        }
    }
}

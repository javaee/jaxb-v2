
import com.sun.xml.xsom.parser.XSOMParser;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class ERDriver {
    public static void main(String[] args) throws Exception {
        XSOMParser p = new XSOMParser();
        p.setEntityResolver(new EntityResolverImpl());

        // SAX parser -> XSOM ContentHandler
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setContentHandler(p.getParserHandler());

        for( String arg : args )
            xr.parse(arg);

        System.out.println("done");
    }

    private static class EntityResolverImpl implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            System.out.printf("p:%s s:%s\n",publicId,systemId);
            return null;
        }
    }
}

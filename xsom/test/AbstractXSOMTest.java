import junit.framework.TestCase;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractXSOMTest extends TestCase {
    /**
     * Loads a schema set from XSDs in the resource.
     */
    protected final XSSchemaSet load(String... resourceNames) throws SAXException {
        XSOMParser p = new XSOMParser();
        for (String n : resourceNames) {
            p.parse(getClass().getResource(n));
        }
        return p.getResult();
    }
}

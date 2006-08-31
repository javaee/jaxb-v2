import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.ComponentNameFunction;
import org.xml.sax.Locator;

import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.Collection;

/**
 * Tests SCD.
 * @author Kohsuke Kawaguchi
 */
public class SCDTest {
    public static void main(String[] args) throws Exception {
        XSOMParser p = new XSOMParser();

        for( int i=1; i<args.length; i++ )
            p.parse(args[i]);

        XSSchemaSet r = p.getResult();
        SCD scd = SCD.create(args[0], new DummyNSContext());
        Collection<XSComponent> result = scd.select(r);
        for( XSComponent c : result) {
            System.out.println(c.apply(new ComponentNameFunction()));
            print(c.getLocator());
            System.out.println();
        }
        System.out.printf("%1d match(s)\n",result.size());
    }

    private static void print(Locator locator) {
        System.out.printf("line %1d of %2s\n", locator.getLineNumber(), locator.getSystemId());

    }

    private static class DummyNSContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            return prefix;
        }

        public String getPrefix(String namespaceURI) {
            return namespaceURI;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return Collections.singletonList(namespaceURI).iterator();
        }
    }
}

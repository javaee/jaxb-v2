import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Kohsuke Kawaguchi
 */
public class MapNamespaceContext implements NamespaceContext {

    private final Map<String,String> core = new HashMap<String, String>();

    public MapNamespaceContext(String... mapping) {
        for( int i=0; i<mapping.length; i+=2 )
            core.put(mapping[i],mapping[i+1]);
    }

    public String getNamespaceURI(String prefix) {
        return core.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException();
    }
}

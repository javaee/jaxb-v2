
import com.sun.tools.jxc.gen.xmlschema.Schema;
import com.sun.tools.jxc.gen.xmlschema.TopLevelElement;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.StreamSerializer;

import javax.xml.namespace.QName;

/**
 * @author Kohsuke Kawaguchi
 */
public class XsdTest {
    private static final StringBuilder newline = new StringBuilder("\n");

    public static void main(String[] args) {
        Schema root = TXW.create(Schema.class,new StreamSerializer(System.out));

        root._namespace("http://www.w3.org/2001/XMLSchema","xs");

        root.version("1.0").targetNamespace("http://foobar/");
        root._pcdata(newline);
        root.complexType().name("ink").sequence();
        root._pcdata(newline);

        TopLevelElement e = root.element();
        e.name("foo").type(new QName("abc","def"));
        e.commit();
        System.out.print("[committed]");

        root.commit();
    }
}

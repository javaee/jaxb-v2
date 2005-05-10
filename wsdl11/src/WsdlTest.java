
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.StreamSerializer;
import wsdl.Definitions;

/**
 * @author Kohsuke Kawaguchi
 */
public class WsdlTest {
    public static void main(String[] args) {
        Definitions root = TXW.create(Definitions.class,new StreamSerializer(System.out));

        root._namespace("http://schemas.xmlsoap.org/wsdl/","wsdl");
        root._namespace("foo","bar");

        root._comment("GENERATED. DO NOT MODIFY");
        root._pcdata("\n");

        root.targetNamespace("abc");
        root.message().name("msg1");
        root.message().name("msg2");

        root.commit();
    }
}

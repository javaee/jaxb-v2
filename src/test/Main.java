
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.DumpSerializer;

import javax.xml.namespace.QName;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) {
        NameCards root = TXW.create(NameCards.class,new DumpSerializer(System.out));

        NameCard nc = root.nameCard();
        nc.id(3);
        nc.test2(new QName("uri","local"));
        nc.name("Kohsuke");
        nc.address("California");
        nc.test(new QName("","local"));

        root.commit();
    }
}

package test.t1;

import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.DumpSerializer;

import javax.xml.namespace.QName;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) {
        Foo root = TXW.create(Foo.class,new DumpSerializer(System.out));

        root.hello(5);
        root.foo();
        
        root.commit();
    }
}


import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class NestedClassTest {
    public static void main(String[] args) throws Exception {
        JCodeModel cm = new JCodeModel();
        JDefinedClass c = cm._package("foo")._class(0,"Foo");
        c._extends(cm.ref(Bar.class));
        cm.build(new SingleStreamCodeWriter(System.out));
    }

    public static class Bar {}
}

import java.io.IOException;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class PackageJavadocTest {
    public static void main(String[] args) throws IOException {
        JCodeModel cm = new JCodeModel();
        cm._package("foo").javadoc().add("String");
        cm.build(new SingleStreamCodeWriter(System.out));
    }
}

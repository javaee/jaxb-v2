
import java.lang.annotation.Inherited;
import java.io.IOException;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class PackageAnnotationTest {
    public static void main(String[] args) throws IOException {
        JCodeModel cm = new JCodeModel();
        cm._package("foo").annotate(Inherited.class);
        cm.build(new SingleStreamCodeWriter(System.out));
    }
}

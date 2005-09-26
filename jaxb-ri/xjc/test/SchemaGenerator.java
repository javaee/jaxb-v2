
/**
 * Alias of {@link com.sun.tools.jxc.SchemaGenerator}, just to make testing easier.
 *
 * @author Kohsuke Kawaguchi
 */
public class SchemaGenerator {
    public static void main( String[] args ) throws Exception {
        // since this is only used for testing/debugging,
        // this is a good place to enable assertions.
        ClassLoader loader = Driver.class.getClassLoader();
        if (loader != null)
            loader.setPackageAssertionStatus("com.sun", true);

        // do not write anything here.
        com.sun.tools.jxc.SchemaGenerator.main(args);
    }
}

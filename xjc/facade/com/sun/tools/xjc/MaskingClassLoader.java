package com.sun.tools.xjc;



/**
 * Dummy class that masks classes which are loaded by the parent
 * class loader so that the child class loader
 * classes living in different folders are loaded
 * before the parent class loader loads classes living the jar file publicly
 * visible
 *
 *
 * <p>
 * For example, with the following jar file:
 * <pre>
 *  /
 *  +- foo
 *     +- X.class
 *  +- bar
 *     +-foo
 *        +- X.class
 * </pre>
 * <p>
 * This is chained with the  {@link ParallelWorldClassLoader}(MaskingClassLoader.class.getClassLoader()) and
 * would load <tt>foo.X.class<tt> from
 * <tt>/bar/foo.X.class</tt> not the <tt>foo.X.class<tt> in the publicly visible place in the jar file, thus
 * masking the parent classLoader from loading the class from  <tt>foo.X.class<tt>
 * (note that X is defined in the  package foo, not
 * <tt>bar.foo.X</tt>.
 *
 *
 *
 * @author Bhakti Mehta
 */
final class MaskingClassLoader extends ClassLoader {


    protected MaskingClassLoader(ClassLoader parent) {
        super(parent);
    }

    protected Class findClass(String name) {
      return null;
    }

    public Class loadClass (String s) {
        return null ;
    }

}

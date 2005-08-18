package org.jvnet.jaxb.reflection;

import java.util.Collection;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;

/**
 * Factory methods to build JAXB models.
 *
 * @author Kohsuke Kawaguchi
 */
// this is a facade to ModelBuilder
public abstract class JAXBModelFactory {
    private JAXBModelFactory() {} // no instanciation please

    /**
     * Creates a new JAXB model from
     * classes represented in arbitrary reflection library.
     *
     * @param reader
     *      used to read annotations from classes. must not be null.
     * @param navigator
     *      abstraction layer of the underlying Java reflection library.
     *      must not be null.
     * @param errorHandler
     *      Receives errors found during the processing.
     *
     * @return
     *      null if any error was reported during the processing.
     *      If no error is reported, a non-null valid object.
     */
    public static <T,C,F,M> TypeInfoSet<T,C,F,M> create(
        AnnotationReader<T,C,F,M> reader,
        Navigator<T,C,F,M> navigator,
        ErrorHandler errorHandler,
        Collection<C> classes ) {

        ModelBuilder<T,C,F,M> builder = new ModelBuilder(reader,navigator,null);
        builder.setErrorHandler(errorHandler);
        for( C c : classes )
            builder.getTypeInfo(new Ref<T,C>(navigator.use(c)));

        return builder.link();
    }

    /**
     * Creates a new JAXB model from
     * classes represented in <tt>java.lang.reflect</tt>.
     *
     * @param reader
     *      used to read annotations from classes. must not be null.
     * @param errorHandler
     *      Receives errors found during the processing.
     *
     * @return
     *      null if any error was reported during the processing.
     *      If no error is reported, a non-null valid object.
     */
    public static RuntimeTypeInfoSet create(
        RuntimeAnnotationReader reader,
        ErrorHandler errorHandler,
        Class... classes ) {

        RuntimeModelBuilder builder = new RuntimeModelBuilder(reader,null);
        builder.setErrorHandler(errorHandler);
        for( Class c : classes )
            builder.getTypeInfo(new Ref<Type,Class>(c));

        return builder.link();
    }

    /**
     * Creates a new JAXB model from
     * classes represented in <tt>java.lang.reflect</tt>.
     *
     * <p>
     * This version reads annotations from the classes directly.
     *
     * @param errorHandler
     *      Receives errors found during the processing.
     *
     * @return
     *      null if any error was reported during the processing.
     *      If no error is reported, a non-null valid object.
     */
    public static RuntimeTypeInfoSet create(
        ErrorHandler errorHandler,
        Class... classes ) {

        return create( new RuntimeInlineAnnotationReader(), errorHandler, classes );
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.jvnet.jaxb.reflection;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;

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

        ModelBuilder<T,C,F,M> builder = new ModelBuilder<T,C,F,M>(reader,navigator,Collections.<C,C>emptyMap(),null);
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

        RuntimeModelBuilder builder = new RuntimeModelBuilder(null,reader,Collections.<Class,Class>emptyMap(),null);
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

    /**
     * Creates a new JAXB model from
     * classes represented in <tt>java.lang.reflect</tt>.
     *
     * <p>
     * This version reads annotations from the classes directly,
     * and throw any error reported as an exception
     *
     * @return
     *      null if any error was reported during the processing.
     *      If no error is reported, a non-null valid object.
     * @throws IllegalAnnotationsException
     *      if there was any incorrect use of annotations in the specified set of classes.
     */
    public static RuntimeTypeInfoSet create(Class... classes ) throws IllegalAnnotationsException {
        IllegalAnnotationsException.Builder errorListener = new IllegalAnnotationsException.Builder();
        RuntimeTypeInfoSet r = create(errorListener, classes);
        errorListener.check();
        return r;
    }
}

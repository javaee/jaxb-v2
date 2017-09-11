/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.api.ClassResolver;

/**
 * Miniture DI container.
 *
 * @author Kohsuke Kawaguchi
 */
@XmlRootElement
public class Container {
    @XmlElement(name="value")
    private List<Value> values = new ArrayList<Value>();

    private static class Value {
        /**
         * ID to identify {@link #value}.
         */
        @XmlAttribute(required=true)
        @XmlID
        private String id;

        /**
         * This annotation causes JAXB to trigger {@link ClassResolver}
         * on this field.
         */
        @XmlAnyElement(lax=true)
        private Object value;
    }

    public static Container load(File file) throws JAXBException {
        Unmarshaller u = CONTEXT.createUnmarshaller();
        // register ClassResolver
        u.setProperty(ClassResolver.class.getName(), new ClassResolverImpl());
        u.setProperty(IDResolver.class.getName(), new IDResolverImpl());
        return (Container)u.unmarshal(file);
    }

    /**
     * Informs JAXB lazily to use such and such class for unmarshalling.
     */
    static final class ClassResolverImpl extends ClassResolver {
        public Class<?> resolveElementName(String nsUri, String localName) throws Exception {
            // assume that element names look like
            // <p:ClassName xmlns:p="java:package.name">
            // and try to load that class.
            if(nsUri.startsWith("java:")) {
                String className = nsUri.substring(5)+'.'+localName;
                // if an exception is thrown from here, it will be passed to
                // ValidationEventHandler
                return Class.forName(className);
            }

            // returning null means 'I have no clue about this element'
            return null;
        }
    }

    /**
     * Notice that this example places the ID attribute on {@link Value},
     * not on the bean object ({@link Value#value}.) So we use
     * a custom {@link IDResolver} so that {@link XmlIDREF} resolves
     * into the {@link Value#value}.
     */
    static final class IDResolverImpl extends IDResolver {
        private final Map<String,Object> table = new HashMap<String,Object>();
        public void bind(String id, Object obj) {
            table.put(id,obj);
        }

        public Callable<?> resolve(final String id, Class targetType) {
            return new Callable<Object>() {
                public Object call() throws Exception {
                    // if IDREF resolves to a Value object,
                    // use the inner value
                    Object o = table.get(id);
                    if(o instanceof Value) {
                        return ((Value)o).value;
                    }
                    return o;
                }
            };
        }
    }


    /**
     * Gets the object for the ID.
     */
    public Object get(String id) {
        for (Value v : values) {
            if(v.id.equals(id))
                return v.value;
        }
        return null;
    }

    /**
     * Note that this {@link JAXBContext} only knows about
     * {@link Container}.
     */
    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(Container.class);
        } catch (JAXBException e) {
            // this is a deployment error
            throw new Error(e);
        }
    }
}

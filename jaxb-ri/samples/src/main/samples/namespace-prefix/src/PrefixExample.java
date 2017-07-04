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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;


/**
 * This example shows you how you can change the way
 * the marshaller assigns prefixes to namespace URIs.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PrefixExample {
    
    public static void main( String[] args ) throws Exception {
        // in this example, I skip the error check entirely
        // for the sake of simplicity. In reality, you should
        // do a better job of handling errors.
        for( int i=0; i<args.length; i++ )
            test(args[i]);      // run through all the files one by one.
    }
    
    private static void test( String fileName ) throws Exception {
        JAXBContext context = JAXBContext.newInstance("foo:bar"); 
        
        // unmarshal a file specified by the command line argument
        Object o = context.createUnmarshaller().unmarshal(new File(fileName));
        
        Marshaller marshaller = context.createMarshaller();
        
        // to specify the URI->prefix mapping, you'll need to provide an
        // implementation of NamespaecPrefixMapper, which determines the
        // prefixes used for marshalling.
        // 
        // you specify this as a property of Marshaller to
        // tell the marshaller to consult your mapper
        // to assign a prefix for a namespace.
        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NamespacePrefixMapperImpl());
        } catch( PropertyException e ) {
            // if the JAXB provider doesn't recognize the prefix mapper,
            // it will throw this exception. Since being unable to specify
            // a human friendly prefix is not really a fatal problem,
            // you can just continue marshalling without failing
            ;
        }
        
        // make the output indented. It looks nicer on screen.
        // this is a JAXB standard property, so it should work with any JAXB impl.
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        
        // print it out to the console since we are just testing the behavior.
        marshaller.marshal( o, System.out );
    }
}

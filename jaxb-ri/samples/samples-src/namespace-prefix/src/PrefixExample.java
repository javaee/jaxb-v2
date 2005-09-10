/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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

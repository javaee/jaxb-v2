import java.io.File;

import javax.xml.bind.JAXBContext;

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

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class RELAXNGExample {

    public static void main(String[] args) throws Exception {
        // in this example, I skip the error check entirely
        // for the sake of simplicity. In reality, you should
        // do a better job of handling errors.
        for( int i=0; i<args.length; i++ ) {
            test(args[i]);
        }
    }
    
    private static void test( String fileName ) throws Exception {
        
        // there's really nothing special about the code generated
        // from RELAX NG. So I'll just do the basic operation
        // to show that it actually feels exactly the same no matter
        // what schema language you use.
        
        JAXBContext context = JAXBContext.newInstance("formula");
        
        // unmarshal a file. Just like you've always been doing.
        Object o = context.createUnmarshaller().unmarshal(new File(fileName)); 
        
        // valdiate it. Again, the same procedure regardless of the schema language
        context.createValidator().validate(o);
        
        // marshal it. Nothing new.
        context.createMarshaller().marshal(o,System.out);
    }
}

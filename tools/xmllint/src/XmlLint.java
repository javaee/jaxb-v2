/*
 * @(#)$Id: XmlLint.java,v 1.2 2005-09-10 19:08:43 kohsuke Exp $
 */

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
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Read XML from stdin and check if it's well-formed.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XmlLint {
    public static void main(String[] args) {
        try {
        	SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            
            XMLReader r = factory.newSAXParser().getXMLReader();
            r.setErrorHandler(new DraconianErrorHandler());
            r.setContentHandler(new DefaultHandler());
            r.parse(new InputSource(System.in));
            System.exit(0);
        } catch( Exception e ) {
            System.exit(-1);
        }
    }
    
    private static class DraconianErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
}

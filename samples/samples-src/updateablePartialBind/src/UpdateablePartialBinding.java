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

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Binder;

import java.math.BigDecimal;

// imported schema-derived classes
import binder.*;
/*
 * DOM load/save adapted from JAXP 1.4 DOMLS Sample.
 *
 * Use DOMLevel 3 LSParser. DOMLS sample
 * uses implementation of LSParser to parse a XML file and writes to an outputfile
 * using implementation of LSSerializer. 
 *
 */

public class UpdateablePartialBinding {
    /**
     *
     * @param argv
     */
    public static void main(String[] argv) {
	JAXBContext jc=null;
	Binder<Node> binder;
	String inputFilename = "po.xml";
	String outputFilename = "processedpo.xml";
	
	// create a JAXBContext capable of handling classes generated into
	// the binder package
	try {
	    jc = JAXBContext.newInstance( "binder" );
	} catch( JAXBException je ) {
            je.printStackTrace();
	    System.exit(1);
        }
	binder = jc.createBinder();

	// Load XML document into DOM
        org.w3c.dom.Document doc = loadDocument(inputFilename);

	// Partially bind XML document to Java.
	// Only Nodes matching XPath are bound to JAXB objects.
	// Binder retains association between DOM and JAXB views.
	NodeList itemNodeSet = xpath(doc, ".//item[USPrice>=25.00]");
	try {
	    for (int onNode=0; onNode < itemNodeSet.getLength(); onNode++){
		
		// Unmarshal by declared type since element Item corresponds with a local element declaration in schema.
		// Partial Bind DOM Element"item" to instance of JAXB mapped binder.Items.Item class
		JAXBElement<binder.Items.Item> itemE = 
		    (JAXBElement<binder.Items.Item>)binder.unmarshal(itemNodeSet.item(onNode), binder.Items.Item.class);
		binder.Items.Item item = itemE.getValue();

		// Modify in Java
 	        item.setComment("qualifies for free shipping");

		// Sync changes made in Java back to XML document
		binder.updateXML(itemE, itemNodeSet.item(onNode));
	    }
	} catch( JAXBException je ) {
            je.printStackTrace();
        }


	// Add $2 shipping per item under $25.
	itemNodeSet = xpath(doc, ".//item[USPrice<25.00]");
	try {
	    for (int onNode=0; onNode < itemNodeSet.getLength(); onNode++){
		
		// Unmarshal by declared type since element Item corresponds with a local element declaration in schema.
		// Partial Bind DOM Element"item" to instance of JAXB mapped binder.Items.Item class
		JAXBElement<binder.Items.Item> itemE = 
		    (JAXBElement<binder.Items.Item>)binder.unmarshal(itemNodeSet.item(onNode), binder.Items.Item.class);
		binder.Items.Item item = itemE.getValue();

		// Modify in Java
 	        item.setUSPrice(item.getUSPrice().add(new BigDecimal("2.00")));

		// Sync changes made in Java back to XML document
		binder.updateXML(itemE, itemNodeSet.item(onNode));
	    }
	} catch( JAXBException je ) {
            je.printStackTrace();
        }
	
	saveDocument(doc,outputFilename);
    }
    
    //*****************************************************************************************
    // Methods adapted from JAXP 1.4 DOM L3 LoadSave Sample
    /**
     *
     * @return DOMImplementation from Document object created using JAXP DocumentBuilderFactory.
     */
    static public DOMImplementationLS createDOMImplementation(){
        DocumentBuilder parser = null;
        Document doc = null;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = parser.newDocument();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return (DOMImplementationLS) doc.getImplementation().getFeature("LS","3.0");
    }
    

    /**
     *
     * @param inputFile XML file
     * @return DOM view of inputFile
     */
    static Document loadDocument(String inputFile) {
        DOMImplementationLS implLS = createDOMImplementation();
        LSParser lsParser = implLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS,null);
        LSInput src = implLS.createLSInput();
        src.setSystemId(inputFile);
        return lsParser.parse(src);
    }

    
    /**
     *
     * @param document
     * @param outputFile
     */
    static public void saveDocument(Document document, String outputFile){
        Output out = new Output();
        DOMImplementationLS implLS = (DOMImplementationLS) document.getImplementation().getFeature("LS","3.0");
        LSSerializer writer = implLS.createLSSerializer();
        writer.getDomConfig().setParameter("error-handler",new DOMErrorHandlerImpl());
        out.setSystemId(outputFile);
        writer.write(document,out);
    }
    
    static class Output implements LSOutput {
        
        OutputStream bs;
        Writer cs;
        String sId;
        String enc;
        
        public Output() {
            bs = null;
            cs = null;
            sId = null;
            enc = "UTF-8";
        }
        
        public OutputStream getByteStream() {
            return bs;
        }
        public void setByteStream(OutputStream byteStream) {
            bs = byteStream;
        }
        public Writer getCharacterStream() {
            return cs;
        }
        public void setCharacterStream(Writer characterStream) {
            cs = characterStream;
        }
        public String getSystemId() {
            return sId;
        }
        public void setSystemId(String systemId) {
            sId = systemId;
        }
        public String getEncoding() {
            return enc;
        }
        public void setEncoding(String encoding) {
            enc = encoding;
        }
    }
    
    static class DOMErrorHandlerImpl implements DOMErrorHandler {
        public boolean handleError(DOMError error) {
            System.out.println("Error occured : "+error.getMessage());
            return true;
        }
    }
    
    //*****************************************************************************************
    // XPATH helper methods adapted from JAXP 1.4 XPath Sample

    static NodeList xpath(Document document, String xpathExpression) {
	
	// create XPath
	XPathFactory xpf = XPathFactory.newInstance();
	XPath xpath = xpf.newXPath();
	xpath.setNamespaceContext(createNamespaceContext());
	
	
	// DOM as data model
	NodeList nodeList = null;
	try {
	    nodeList = (NodeList)xpath.evaluate(xpathExpression,
						document,
						XPathConstants.NODESET);
	} catch (XPathExpressionException xpathExpressionException) {
	    xpathExpressionException.printStackTrace();
	    System.exit(1);
	}
	//Comment out debug statement
	//dumpNode("DOM xpath", null, xpathExpression, nodeList);
	return nodeList;
    }

    static void dumpNode(String objectModel,
			 String inputFile,
			 String xpathExpression,
			 NodeList nodeList) {
		
	System.out.println("Object model: " + objectModel + "created from: " + inputFile + "\n"
			   + "XPath expression: " + xpathExpression + "\n"
			   + "NodeList.getLength(): " + nodeList.getLength());
		
	// dump each Node's info
	for (int onNode = 0; onNode < nodeList.getLength(); onNode++) {
	    
	    Node node = nodeList.item(onNode);
	    String nodeName = node.getNodeName();
	    String nodeValue = node.getNodeValue();
	    if (nodeValue == null) {
		nodeValue = "null";
	    }
	    String namespaceURI = node.getNamespaceURI();
	    if (namespaceURI == null) {
		namespaceURI = "null";
	    }
	    String namespacePrefix = node.getPrefix();
	    if (namespacePrefix == null) {
		namespacePrefix = "null";
	    }
	    String localName = node.getLocalName();
	    if (localName == null) {
		localName = "null";
	    }
	    
	    System.out.println("result #: " + onNode + "\n"
			       + "\tNode name: " + nodeName + "\n"
			       + "\tNode value: " + nodeValue + "\n"
			       + "\tNamespace URI: " + namespaceURI + "\n"
			       + "\tNamespace prefix: " + namespacePrefix + "\n"
			       + "\tLocal name: " + localName);
	}
	// dump each Node's info
    }
    
    static NamespaceContextImpl createNamespaceContext() {
	NamespaceContextImpl namespaceContextImpl = new NamespaceContextImpl();
        
        namespaceContextImpl.bindPrefixToNamespaceURI(
                XMLConstants.DEFAULT_NS_PREFIX,
                "http://schemas.xmlsoap.org/wsdl/");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "tns",
                "http://hello.org/wsdl");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "ns2",
                "http://hello.org/types");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "xsd",
                "http://www.w3.org/2001/XMLSchema");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "soap",
                "http://schemas.xmlsoap.org/wsdl/soap/");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "soap11-enc",
                "http://schemas.xmlsoap.org/soap/encoding/");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "xsi",
                "http://www.w3.org/2001/XMLSchema-instance");
        namespaceContextImpl.bindPrefixToNamespaceURI(
                "wsdl",
                "http://schemas.xmlsoap.org/wsdl/");
                
	return namespaceContextImpl;
    }
}

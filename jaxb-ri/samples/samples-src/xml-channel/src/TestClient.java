/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.IOException;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

import message.*;

/**
 * Test client.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TestClient implements Runnable {

    private ObjectFactory of;
    private Marshaller marshaller;

    public TestClient() {
        try {
	    JAXBContext jc = JAXBContext.newInstance("message");
	    marshaller = jc.createMarshaller();
	    of = new ObjectFactory();
        } catch( JAXBException e ) {
            e.printStackTrace(); // impossible
        }
    }
    
    public void run() {
        try {
            // create a socket connection and start conversation
            Socket socket = new Socket("localhost",38247);
            XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(socket.getOutputStream());

            // write the dummy start tag
            xsw.writeStartDocument();
            xsw.writeStartElement("conversation");

            sendMessage(xsw,"1st message");
            Thread.sleep(1000);
            sendMessage(xsw,"2nd message");

            xsw.writeEndElement();
            xsw.writeEndDocument();
            xsw.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private void sendMessage( XMLStreamWriter xsw, String msg ) throws JAXBException, XMLStreamException {
        JAXBElement<String> m = of.createMessage(msg);
        marshaller.marshal(m,xsw);
        xsw.flush();    // send it now
    }
}

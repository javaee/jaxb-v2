/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import message.*;

/**
 * Server program that displays the messages sent from clients.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TestServer implements Runnable {

    private final XMLInputFactory xif;

    public TestServer() {
        this.xif = XMLInputFactory.newInstance();
    }


    public void run() {
        try {
            ServerSocket ss = new ServerSocket(38247);
            JAXBContext context = JAXBContext.newInstance("message");

            // notify test driver that we are ready to accept
            synchronized( Test.lock ) {
                Test.ready = true;
                Test.lock.notifyAll();
            }
            
            while(true) {
                new Worker(ss.accept(),context).start();
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    class Worker extends Thread {
        private final XMLStreamReader xsr;
        private final Unmarshaller unmarshaller;
        
        Worker( Socket socket, JAXBContext context ) throws IOException, JAXBException, XMLStreamException {
            System.out.println("accepted a connection from a client");
            synchronized(TestServer.this) {
                xsr = xif.createXMLStreamReader(socket.getInputStream());
            }
            this.unmarshaller = context.createUnmarshaller();
        }
        
        public void run() {
            try {
                xsr.nextTag();  // get to the first <conversation> tag

                xsr.next();     // wait for the first message to come

                while( xsr.isStartElement() ) {
                    // unmarshal a new object
                    JAXBElement<String> msg = (JAXBElement<String>)unmarshaller.unmarshal(xsr);
                    System.out.println("Message: "+ msg.getValue());
                }
                System.out.println("Bye!");
                xsr.close();

                // notify the driver that we are done processing
                synchronized( Test.lock ) {
                    Test.ready = true;
                    Test.lock.notifyAll();
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}

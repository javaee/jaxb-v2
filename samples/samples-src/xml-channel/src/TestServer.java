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
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

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
        die();
    }
    
    class Worker extends Thread {
        private final XMLEventReader xer;
        private final Unmarshaller unmarshaller;
        
        Worker( Socket socket, JAXBContext context ) throws IOException, JAXBException, XMLStreamException {
            System.out.println("accepted a connection from a client");
            synchronized(TestServer.this) {
                xer = xif.createXMLEventReader(socket.getInputStream());
            }
            this.unmarshaller = context.createUnmarshaller();
        }
        
        public void run() {
            try {
                xer.nextEvent();    // read the start document
                xer.nextTag(); // get to the first <conversation> tag, and skip

                while( xer.peek().isStartElement() ) {
                    // unmarshal a new object
                    JAXBElement<String> msg = (JAXBElement<String>)unmarshaller.unmarshal(xer);
                    System.out.println("Message: "+ msg.getValue());
                }
                System.out.println("Bye!");
                xer.close();

                die();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private void die() {
        // notify the driver that we are done processing
        synchronized( Test.lock ) {
            Test.ready = true;
            Test.lock.notifyAll();
        }
    }
}

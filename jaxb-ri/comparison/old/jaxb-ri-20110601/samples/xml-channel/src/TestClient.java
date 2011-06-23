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
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);
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

            for( int i=1; i<=10; i++ ) {
                Thread.sleep(1000);
                sendMessage(xsw,"message "+i);
            }

            Thread.sleep(1000);

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

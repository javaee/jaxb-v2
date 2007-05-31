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

/*
 * Copyright 2004-5 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;

import javax.xml.stream.*;
import static javax.xml.stream.XMLStreamConstants.*;
import contact.Contact;

/*
 * Use is subject to the license terms.
 */

/**
 * 
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        
        String nameToLookFor = args[0];
        
        JAXBContext jaxbContext = JAXBContext.newInstance("contact"); 
        Unmarshaller um = jaxbContext.createUnmarshaller();

        // set up a parser
	XMLInputFactory xmlif = XMLInputFactory.newInstance();
	XMLStreamReader xmlr = 
	    xmlif.createXMLStreamReader(new FileReader("contact.xml"));
 
	// move to the root element and check its name.
        xmlr.nextTag(); 
        xmlr.require(START_ELEMENT, null, "addressBook");

        xmlr.nextTag(); // move to the first <contact> element.
        while (xmlr.getEventType() == START_ELEMENT) {

            // unmarshall one <contact> element into a JAXB Contact object
	    xmlr.require(START_ELEMENT, null, "contact");
            Contact contact = (Contact) um.unmarshal(xmlr);
            if( contact.getName().equals(nameToLookFor)) {
                // we found what we wanted to find. show it and quit now.
                System.out.println("the e-mail address is "+contact.getEmail());
                return;
            }
            if (xmlr.getEventType() == CHARACTERS) {
                xmlr.next(); // skip the whitespace between <contact>s.
	    }
        }
        System.out.println("Unable to find "+nameToLookFor);
    }
}

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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import primer.PurchaseOrderType;

/*
 * @(#)$Id: Main.java,v 1.2 2009-11-11 14:17:28 pavel_bucek Exp $
 */

public class Main {
    public static void main( String[] args ) throws Exception {
        
        // create JAXBContext for the primer.xsd
        JAXBContext context = JAXBContext.newInstance("primer");
        
        // unmarshal a document, just to marshal it back again.
        JAXBElement poe = (JAXBElement)context.createUnmarshaller().unmarshal(
            new File(args[0]));
        // we don't need to check the return value, because the unmarshal
        // method should haven thrown an exception if anything went wrong.
	PurchaseOrderType po = (PurchaseOrderType)poe.getValue();
        
        
        // Here's the real meat.
        // we configure marshaller not to print out xml decl,
        // we then print out XML decl plus stylesheet header on our own,
        // then have the marshaller print the real meat.
        
        System.out.println("<?xml version='1.0'?>");
        System.out.println("<?xml-stylesheet type='text/xsl' href='foobar.xsl' ?>");
        // if you need to put DOCTYPE decl, it can be easily done here.
        
        // create JAXB marshaller.
        Marshaller m = context.createMarshaller();
        // configure it
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        // marshal
        m.marshal(poe,System.out);
    }

}

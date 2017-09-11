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

/**
 *  Author: Sekhar Vajjhala
 *
 *  $Id: USAddress.java,v 1.1 2007-12-05 00:49:37 kohsuke Exp $
 */  

import javax.xml.bind.annotation.XmlRootElement;

/**
 * NOTES: If @XmlRootElement is not present, and an attempt is made to
 * marshal the type, then the marshalled output is:
 *
 *     <xml:name>Alice Smith</xml:name>
 *     <xml:street>123 Maple Street</xml:street>
 *     <xml:city>Mill Valley</xml:city>
 *     <xml:state>CA</xml:state>
 *     <xml:zip>90952</xml:zip>
 *
 * In other words, the type is marshaled.
 *
 * OBSERVATIONS:
 * a. At first I did not notice the xml: prefix.
 * b. For a few seconds I was puzzled why xml:prefix was being output.
 *    Note, I did not run schemagen.sh to generate the schema. I
 *    was interested in only going from java -> XML representation.
 * 
 *    Why is there a xml: prefix ? 
 *
 * c. A few seconds later I realized that this was because I was
 *    was attempting to marshal a type. Although the use of xml:prefix
 *    Since I am closely involved with JAXB 2.0, I was able to figure
 *    this out. I am not sure whether that this would be obvious to a
 *    user trying out JAXB 2.0 for the first time.
 *
 * d. I accidentally put a @XmlElement instead of @XmlRootElement on
 *    the USAddress type. I got the message 
 * 
 *        USAddress.java:3: annotation type not applicable to this kind of declaration
 *        @XmlElement
 *        ^
 *    Of course, I knew @XmlRootElement not @XmlElement must be
 *    used. However, that would not be immediately obvious to a new
 *    JAXB 2.0 user. An output of annotations possible on a program
 *    element might be more helpful.
 *
 *    But the above error message is generated from javac not the JAXB 2.0
 *    runtime. Hence, there is not much that the JAXB 2.0 runtime can
 *    do about the error message. Such errors can be caught by a
 *    consistency checker run as a pre-processor.
 * 
 * e. After I put a @XmlRootElement on the type, I got the expected
 *    output:
 *
 *        <usAddress>
 *            <name>Alice Smith</name>
 *            <street>123 Maple Street</street>
 *            <city>Mill Valley</city>
 *            <state>CA</state>
 *            <zip>90952</zip>
 *        </usAddress>
 *
 */

@XmlRootElement
public class USAddress extends Address {
    public String state;
    public int zip;
}


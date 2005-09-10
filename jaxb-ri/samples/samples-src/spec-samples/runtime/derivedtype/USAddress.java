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
 *  Author: Sekhar Vajjhala
 *
 *  $Id: USAddress.java,v 1.2 2005-09-10 19:08:14 kohsuke Exp $
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


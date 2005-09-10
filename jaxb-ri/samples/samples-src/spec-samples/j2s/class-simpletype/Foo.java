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

/*
 *  $Id: Foo.java,v 1.2 2005-09-10 19:08:13 kohsuke Exp $
 *  Author: Sekhar Vajjhala  
 */  

/**
 * Map a class with a single property that has been marked with
 * @XmlValue to simple schema type.
 */
import javax.xml.bind.annotation.XmlValue;
 
public class Foo {
    /**
     * @XmlValue can only be used on a property whose type (int in
     * this case) is mapped to a simple schema type.
     */
    @XmlValue 
    public int bar;
}

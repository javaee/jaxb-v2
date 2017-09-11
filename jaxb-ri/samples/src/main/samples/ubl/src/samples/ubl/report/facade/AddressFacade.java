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

package samples.ubl.report.facade;

import org.oasis.ubl.commonaggregatecomponents.AddressType;

/**
 * The <code>AddressFacade</code> class provides a set of read-only
 * methods for accessing data in a UBL <code>AddressType</code>.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class AddressFacade {

    private AddressType address;
  
    /**
     * Creates a new <code>AddressFacade</code> instance.
     *
     * @param addr an <code>AddressType</code> value
     */
    public AddressFacade(AddressType addr) {
        address = addr;
    }

    /**
     * <code>getStreet</code> returns a <code>String</code> representing the
     * street in a UBL address.
     *
     * @return a <code>String</code> representing the street in a UBL address.
     */
    public String getStreet() {
        String result = "";
        try {
            result = address.getStreetName().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * <code>getState</code> returns a <code>String</code> representing the
     * state in a UBL address.
     *
     * @return a <code>String</code> representing the state in a UBL address.
     */
    public String getState() {
        String result = "";
        try {
            result = address.getCountrySubentityCode().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * <code>getCity</code> returns a <code>String</code> representing the city
     * in a UBL address.
     *
     * @return a <code>String</code> representing the city in a UBL address.
     */
    public String getCity() {
        String result = "";
        try {
            result = address.getCityName().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * <code>getZip</code> returns a <code>String</code representing the postal
     * zone in a UBL address.
     *
     * @return a <code>String</code> representing the postal zone in a UBL address.
     */
    public String getZip() {
        String result = "";
        try {
            result = address.getPostalZone().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }
}

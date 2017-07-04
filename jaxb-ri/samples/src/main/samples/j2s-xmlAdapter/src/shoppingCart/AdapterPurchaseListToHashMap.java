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

/*
 * $Id: AdapterPurchaseListToHashMap.java,v 1.1 2007-12-05 00:49:28 kohsuke Exp $
 */


package shoppingCart;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/*
 *
 *  PurchaseList - ValueType
 *  HashMap - BoundType
 */
public class AdapterPurchaseListToHashMap extends XmlAdapter<PurchaseList, HashMap> {
    public AdapterPurchaseListToHashMap(){}
    
    // Convert a value type to a bound type.
    // read xml content and put into Java class.
    public HashMap unmarshal(PurchaseList v){        
       HashMap aHashMap = new HashMap();
       int cnt = v.entry.size();
       for(int i=0; i < cnt; i++){
            PartEntry tmpE = (PartEntry)v.entry.get(i);
            aHashMap.put(new Integer(tmpE.key), tmpE.value);
        } 
       return aHashMap;
    }
    
    // Convert a bound type to a value type.
    // write Java content into class that generates desired XML 
    public PurchaseList marshal(HashMap v){
        PurchaseList pList = new PurchaseList();
        // For QA consistency order the output. 
        TreeMap tMap = new TreeMap(v);
        for(Iterator i=tMap.keySet().iterator(); i.hasNext();){
            Integer tmpI = (Integer)i.next();
            pList.entry.add(new PartEntry(tmpI.intValue(), (String)tMap.get(tmpI)));
        }
        return pList;
    }
}

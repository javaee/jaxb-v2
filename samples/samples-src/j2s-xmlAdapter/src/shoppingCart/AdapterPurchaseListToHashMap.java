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
 * $Id: AdapterPurchaseListToHashMap.java,v 1.1 2005-12-20 15:03:56 rebeccas Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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

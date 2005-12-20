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
 * $Id: PurchaseList.java,v 1.1 2005-12-20 15:03:58 rebeccas Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package shoppingCart;

import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name="PurchaseListType")
public class PurchaseList {
    //- this must be a public field for the adapter to function
    //- When it is public the generated xml uses the variable name
    //- as the element tag.
    //- If the entry is not public the generic identifier is used
    //- as the element tag.  Settter/getter methods would be
    //- needed.
    public List<PartEntry> entry;
    
    public PurchaseList(){
        entry = new Vector<PartEntry>();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        int cnt = entry.size();
        for(int i=0; i < cnt; i++){
            buf.append(entry.get(i).toString());
            buf.append("\n");
        }
        return buf.toString();
    }
}


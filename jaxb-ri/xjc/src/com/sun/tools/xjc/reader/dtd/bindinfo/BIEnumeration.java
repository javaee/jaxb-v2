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
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;

import org.w3c.dom.Element;

/**
 * &lt;enumeration> declaration in the binding file.
 */
public final class BIEnumeration implements BIConversion
{
    /** Creates an object from &lt;enumeration> declaration. */
    private BIEnumeration( Element _e, TypeUse _xducer ) {
        this.e = _e;
        this.xducer = _xducer;
    }
    
    /** &lt;enumeration> element in DOM. */
    private final Element e;
    
    private final TypeUse xducer;
    
    public String name() { return DOMUtil.getAttribute(e,"name"); }
    
    /** Returns a transducer for this enumeration declaration. */
    public TypeUse getTransducer() { return xducer; }
    
    
    
    
    /** Creates a global enumeration declaration. */
    static BIEnumeration create( Element dom, BindInfo parent ) {
        // create a class in the target package.
        return new BIEnumeration(
            dom,
            new CEnumLeafInfo(
                parent.model,
                null,
                new CClassInfoParent.Package(parent.getTargetPackage()),
                DOMUtil.getAttribute(dom,"name"),
                CBuiltinLeafInfo.STRING,
                buildMemberList(parent.model,dom),
                null, null/*TODO*/,
                DOMLocator.getLocationInfo(dom)));
    }
    
    /** Creates an element-local enumeration declaration. */
    static BIEnumeration create( Element dom, BIElement parent ) {
        // create a class as a nested class
        return new BIEnumeration(
            dom,
            new CEnumLeafInfo(
                parent.parent.model,
                null,
                parent.clazz,
                DOMUtil.getAttribute(dom,"name"),
                CBuiltinLeafInfo.STRING,
                buildMemberList(parent.parent.model,dom),
                null, null/*TODO*/,
                DOMLocator.getLocationInfo(dom) ));
    }
    
    private static List<CEnumConstant> buildMemberList( Model model, Element dom ) {
        List<CEnumConstant> r = new ArrayList<CEnumConstant>();

        String members = DOMUtil.getAttribute(dom,"members");
        if(members==null) members="";   // TODO: error handling
        
        StringTokenizer tokens = new StringTokenizer(members);
        while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            r.add(new CEnumConstant(model.getNameConverter().toConstantName(token),
                    null,token,null));
        }
        
        return r;
    }
}

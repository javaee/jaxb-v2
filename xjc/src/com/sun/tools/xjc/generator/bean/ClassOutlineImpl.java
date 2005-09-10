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
package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.ClassOutline;

/**
 * {@link ClassOutline} enhanced with schema2java specific
 * information.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ClassOutlineImpl extends ClassOutline {
    private final BeanGenerator _parent;


    public MethodWriter createMethodWriter() {
        return _parent.getModel().strategy.createMethodWriter(this);
    }
    
    /**
     * Gets {@link #_package} as {@link PackageOutlineImpl},
     * since it's guaranteed to be of that type.
     */
    public PackageOutlineImpl _package() {
        return (PackageOutlineImpl)super._package();
    }

    ClassOutlineImpl( BeanGenerator _parent,
        CClassInfo _target, JDefinedClass exposedClass, JDefinedClass _implClass, JClass _implRef ) {
        super(_target,exposedClass,_implRef,_implClass);
        this._parent = _parent;
        _package().classes.add(this);
    }

    public BeanGenerator parent() {
        return _parent;
    }
}

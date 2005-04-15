/*
 * @(#)$Id: UntypedListFieldRenderer.java,v 1.1 2005-04-15 20:09:10 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class UntypedListFieldRenderer implements FieldRenderer {

    private JClass coreList;

    public UntypedListFieldRenderer( JClass coreList ) {
        this.coreList = coreList;
    }
    
    public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo prop) {
        return new UntypedListField(context,prop,coreList);
    }
}

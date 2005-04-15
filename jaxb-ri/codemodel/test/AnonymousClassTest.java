/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.util.Iterator;

import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class AnonymousClassTest {

    public static void main(String[] args) throws Exception {
        JCodeModel cm = new JCodeModel();
        JDefinedClass cls = cm._class("Test");
        JMethod m = cls.method(JMod.PUBLIC,cm.VOID,"foo");
        
        JDefinedClass c = cm.anonymousClass(cm.ref(Iterator.class));
        c.method(0,cm.VOID,"bob");
        c.field(JMod.PRIVATE,cm.DOUBLE,"y");
        m.body().decl(cm.ref(Object.class),"x",
            JExpr._new(c));
        
        cm.build(new SingleStreamCodeWriter(System.out));
    }
}

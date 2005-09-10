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

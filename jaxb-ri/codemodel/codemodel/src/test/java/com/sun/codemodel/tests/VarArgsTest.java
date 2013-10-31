package com.sun.codemodel.tests;
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import java.io.IOException;

import org.junit.Test;

import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * 
 * Simple program to test the generation of
 * the varargs feature in jdk 1.5
 * @author Bhakti Mehta Bhakti.Mehta@sun.com
 *
 */
/*======================================================
 * This is how the output from this program looks like
 * Still need to learn how to work on instantiation and args
 * =========================================================
 * public class Test {


     public void foo(java.lang.String param1, 
         java.lang.Integer param2, java.lang.String param5,
         java.lang.Object... param3) {
      for (int count = 0; (count<(param3.length)); count ++) {
          java.lang.System.out.println((param3[count]));
      }
  }

    public static void main(java.lang.String[] args) {
    }

}
*==========================================================
**/

public class VarArgsTest {

	@Test
	public void main() throws Exception {

        try {
            JCodeModel cm = new JCodeModel();
            JDefinedClass cls = cm._class("Test");
            JMethod m = cls.method(JMod.PUBLIC, cm.VOID, "foo");
            m.param(String.class, "param1");
            m.param(Integer.class, "param2");
            JVar var = m.varParam(Object.class, "param3");
            System.out.println("First varParam " + var);
            
            // checking for param after varParam it behaves ok
            //JVar[] var1 = m.varParam(Float.class, "param4");
            JClass string = cm.ref(String.class);
            JClass stringArray = string.array();
//            JVar param5 =
            m.param(String.class, "param5");
            
            JForLoop forloop = m.body()._for();
            
            JVar $count = forloop.init(cm.INT, "count", JExpr.lit(0));
            
            forloop.test($count.lt(JExpr.direct("param3.length")));
            forloop.update($count.incr());
            
            JFieldRef out = cm.ref(System.class).staticRef("out");
            
//            JVar typearray = 
            m.listVarParam();
            
//            JInvocation invocation =
            forloop.body().invoke(out, "println").arg(
                    JExpr.direct("param3[count]"));
            
            JMethod main = cls.method(JMod.PUBLIC | JMod.STATIC, cm.VOID, "main");
            main.param(stringArray, "args");
            main.body().directStatement("new Test().foo(new String(\"Param1\"),new Integer(5),null,new String(\"Param3\"),new String(\"Param4\"));" );//new String("Param1"))"");//                "new Integer(5),+//                "null," +//                "new String("first")," +//                " new String("Second"))");
            
            cm.build(new SingleStreamCodeWriter(System.out));
        } catch (JClassAlreadyExistsException e) {
            
            e.printStackTrace();
        } catch (IOException e) {
            
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

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



import java.util.ArrayList;
import com.sun.codemodel.*;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * 
 * Simple program to test the generation of
 * the enhanced for loop in jdk 1.5
 * @author Bhakti Mehta Bhakti.Mehta@sun.com
 *
 */

public class ForEachTest {

	public static void main(String[] args) throws Exception {
		
		JCodeModel cm = new JCodeModel();
		JDefinedClass cls = cm._class("Test");
		
		JMethod m = cls.method(JMod.PUBLIC, cm.VOID, "foo");
		m.body().decl(cm.INT, "getCount");
		
        // This is not exactly right because we need to 
        // support generics
		JClass arrayListclass = cm.ref(ArrayList.class);
		JVar $list =
			m.body().decl(arrayListclass, "alist", JExpr._new(arrayListclass));
		
		JClass $integerclass = cm.ref(Integer.class);
		JForEach foreach = m.body().forEach($integerclass, "count", $list);
		JVar $count1 = foreach.var();
		foreach.body().assign(JExpr.ref("getCount"), JExpr.lit(10));
		
		//printing out the variable
		JFieldRef out1 = cm.ref(System.class).staticRef("out");
		JInvocation invocation = foreach.body().invoke(out1,"println").arg($count1);
		
		cm.build(new SingleStreamCodeWriter(System.out));
	}
}


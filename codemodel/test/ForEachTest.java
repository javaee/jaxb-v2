/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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


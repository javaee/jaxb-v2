package com.sun.tools.xjc.reader;

import java.util.List;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author Kohsuke Kawaguchi
 */
public class TypeUtilJUTest extends TestCase {
    public static void main(String[] args) {
        TestRunner.run(TypeUtilJUTest.class);
    }

    public void test1() {
        JCodeModel cm = new JCodeModel();
        JType t = TypeUtil.getCommonBaseType(cm,
                    cm.ref(List.class).narrow(Integer.class),
                    cm.ref(List.class).narrow(Float.class),
                    cm.ref(List.class).narrow(Number.class) );
        System.out.println(t.fullName());
        assertEquals("java.util.List<? extends java.lang.Number>",t.fullName());
    }
}

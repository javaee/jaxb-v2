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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * A test program for the annotation use features Note: Not all the generated
 * code would make sense but just checking in all the different ways you can use
 * an annotation
 * 
 * @author Bhakti Mehta
 */
public class AnnotationUseTest {

	@Test
	public void main() throws Exception {
		JCodeModel cm = new JCodeModel();
		JDefinedClass cls = cm._class("Test");
		// JMethod m =
		cls.method(JMod.PUBLIC, cm.VOID, "foo");

		// Annotating a class
		// Using the existing Annotations from java.lang.annotation package
		JAnnotationUse use = cls.annotate(cm.ref(Retention.class));

		// declaring an enum class and an enumconstant as a membervaluepair
		JDefinedClass enumcls = cls._enum("Iamenum");
		JEnumConstant ec = enumcls.enumConstant("GOOD");
		JEnumConstant ec1 = enumcls.enumConstant("BAD");
		JEnumConstant ec2 = enumcls.enumConstant("BAD");
		ec1.equals(ec2);

		use.param("value", ec);
		// adding another param as an enum
		use.param("value1", RetentionPolicy.RUNTIME);

		// Adding annotation for fields
		// will generate like
		// @String(name = "book") private double y;
		//
		JFieldVar field = cls.field(JMod.PRIVATE, cm.DOUBLE, "y");

		// Adding more annotations which are member value pairs
		JAnnotationUse ause = field.annotate(Retention.class);
		ause.param("name", "book");
		ause.param("targetNamespace", 5);

		// Adding arrays as member value pairs
		JAnnotationArrayMember arrayMember = ause.paramArray("names");
		arrayMember.param("Bob");
		arrayMember.param("Rob");
		arrayMember.param("Ted");

		JAnnotationArrayMember arrayMember1 = ause.paramArray("namesno");
		arrayMember1.param(4);
		arrayMember1.param(5);
		arrayMember1.param(6);

		JAnnotationArrayMember arrayMember2 = ause.paramArray("values");
		// adding an annotation as a member value pair
		arrayMember2.annotate(Target.class).param("type", Integer.class);
		arrayMember2.annotate(Target.class).param("type", Float.class);

		// test typed annotation writer
		XmlElementW w = cls.annotate2(XmlElementW.class);
		w.ns("##default").value("foobar");

		// adding an annotation as a member value pair
		JAnnotationUse myuse = ause.annotationParam("foo", Target.class);
		myuse.param("junk", 7);

		cm.build(new SingleStreamCodeWriter(System.out));
	}

	@interface XmlElement {
		String value();

		String ns();
	}

	interface XmlElementW extends JAnnotationWriter<XmlElement> {
		XmlElementW value(String s);

		XmlElementW ns(String s);
	}
}

/*
 * *********************************************************************
 * Generates this
 * **********************************************************************
 * 
 * @java.lang.annotation.Retention(value1 =
 * java.lang.annotation.RetentionPolicy.RUNTIME, value = Test.Iamenum.GOOD)
 * public class Test {
 * 
 * @java.lang.annotation.Retention(foo = @java.lang.annotation.Target(junk = 7)
 * 
 * , targetNamespace = 5, namesno = { 4, 5, 6 }, values =
 * {@java.lang.annotation.Target(type = java.lang.Integer) ,
 * @java.lang.annotation.Target(type = java.lang.Float) }, names = {"Bob",
 * "Rob", "Ted"}, name = "book") private double y;
 * 
 * public void foo() { }
 * 
 * public enum Iamenum {
 * 
 * BAD, GOOD; }
 * 
 * }
 * 
 * }
 */

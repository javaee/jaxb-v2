/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package jaxb.core.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Set of simple tests which should guarantee that JAXB RI imported/exported
 * packages can be properly loaded by OSGi framework.
 *
 * @author Martin Vojtek
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiBundleTest {

    @Inject
    private static BundleContext ctx;
    private static final String JAXB_SPEC_VERSION = System.getProperty("jaxb.spec.version");

    @Configuration
    public static Option[] config() {
        return options(
                repositories("http://repo1.maven.org/maven2",
                        "http://maven.java.net/content/repositories/snapshots/"),
                localRepository(getLocalRepository()),
                mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").version("4.3.0"),
                //JDK internal dependencies
                systemPackage("com.sun.org.apache.xml.internal.resolver"),
                systemPackage("com.sun.org.apache.xml.internal.resolver.tools"),
                systemPackage("com.sun.source.tree"),
                systemPackage("com.sun.source.util"),
                //JAXB APIs
                mavenBundle("javax.xml.bind", "jaxb-api", JAXB_SPEC_VERSION),
                //JAXB CORE bundles
                bundle(new File(System.getProperty("osgi.dist")).toURI().toString() + ".jar"),
                junitBundles(),
                felix());
    }

    @Test
    public void testIstackBuilder() {
        Class<?> c = loadClass("com.sun.istack.Builder");
        assertClassLoadedByBundle(c, "com.sun.xml.bind.jaxb-core");
    }

    @Test
    public void testDataWriter() {
        Class<?> c = loadClass("com.sun.xml.bind.marshaller.DataWriter");
        assertClassLoadedByBundle(c, "com.sun.xml.bind.jaxb-core");
    }

    private Class<?> loadClass(String className) {
        try {
            return ctx.getBundle().loadClass(className);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OSGiBundleTest.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            Assert.fail("Cannot find and load class: " + className);
        }
        return null;
    }

    private void assertClassLoadedByBundle(Class<?> c, String bundle) {
        Bundle b = FrameworkUtil.getBundle(c);
        Assert.assertEquals("Class '" + c.getName() + "' was loaded by '"
                        + b.getSymbolicName() + "', expected was '" + bundle + "'",
                bundle, b.getSymbolicName());
        Assert.assertEquals("Bundle '" + bundle + "' is not running", Bundle.ACTIVE, b.getState());
    }

    private static String getLocalRepository() {
        String path = System.getProperty("maven.repo.local");
        return (path != null && path.trim().length() > 0)
                ? path
                : System.getProperty("user.home") + File.separator
                + ".m2" + File.separator
                + "repository";
    }
}

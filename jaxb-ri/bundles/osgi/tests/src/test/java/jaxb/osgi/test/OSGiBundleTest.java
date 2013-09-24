/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package jaxb.osgi.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.localRepository;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repositories;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Set of simple tests which should guarantee that JAXB RI imported/exported
 * packages can be properly loaded by OSGi framework.
 *
 * @author lukas
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiBundleTest {

    @Inject
    private static BundleContext ctx;
    private static final String JAXB_SPEC_VERSION = System.getProperty("jaxb.spec.version");
    private static final String JAXB_IMPL_VERSION = System.getProperty("jaxb.impl.version");
    private static final String HK2_VERSION = "2.1.64";
    private static final String GF_VERSION = System.getProperty("glassfish.version");

    @Configuration
    public static Option[] config() {
        return options(
                repositories("http://repo1.maven.org/maven2",
                        //                "http://repository.apache.org/content/groups/snapshots-group",
                        //                "https://maven.java.net/content/groups/staging",
                        //                "http://repository.ops4j.org/maven2",
                        //                "http://svn.apache.org/repos/asf/servicemix/m2-repo",
                        //                "http://repository.springsource.com/maven/bundles/release",
                        //                "http://repository.springsource.com/maven/bundles/external",
                        "http://maven.java.net/content/repositories/snapshots/"),
                localRepository(System.getProperty("mvn.repo")),
                mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").version("4.3.0"),
                systemPackage("com.sun.nio"),
                systemPackage("sun.nio.cs"),
                systemPackage("sun.rmi.rmic"),
                systemPackage("sun.misc"),
                systemPackage("com.ibm.security.util"),
                systemPackage("com.ibm.security.x509"),
                systemPackage("com.sun.net.httpserver"),
                systemPackage("com.sun.tools.javac"),
                systemPackage("sun.tools.javac"),
                // GlassFish
                mavenBundle().groupId("org.glassfish.hk2").artifactId("config").version("2.0.5"),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-locator").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-core").version("2.0.5"),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("auto-depends").version("2.0.5"),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("config-types").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2.external").artifactId("bean-validator").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2.external").artifactId("cglib").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2.external").artifactId("asm-all-repackaged").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2.external").artifactId("javax.inject").version("2.0.5"),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-api").version("2.0.5"),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-api").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-runlevel").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-config").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-utils").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("class-model").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.ha").artifactId("ha-api").version("3.1.9"),
                mavenBundle().groupId("org.glassfish.external").artifactId("management-api").version("3.2.0-b001"),
                mavenBundle().groupId("javax.annotation").artifactId("javax.annotation-api").version("1.2-b03"),
                //                mavenBundle().groupId("org.glassfish.main").artifactId("javax.enterprise.deploy").version(GF_VERSION),
                //                mavenBundle().groupId("org.glassfish.main").artifactId("javax.resource").version(GF_VERSION),
                //                mavenBundle().groupId("org.glassfish.main").artifactId("javax.transaction").version(GF_VERSION),
                mavenBundle().groupId("org.eclipse.persistence").artifactId("javax.persistence").version("2.0.101.v201206242320"),
                mavenBundle().groupId("javax.servlet").artifactId("javax.servlet-api").version("3.0.1"),
                mavenBundle().groupId("org.glassfish.gmbal").artifactId("gmbal").version("3.1.0-b001"),
                mavenBundle().groupId("org.glassfish.main.grizzly").artifactId("nucleus-grizzly-all").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("glassfish-api").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("annotation-framework").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("osgi-resource-locator").version("1.0.1"),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("internal-api").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("core").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.main.deployment").artifactId("deployment-common").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.external").artifactId("ant").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("common-util").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.admin").artifactId("config-api").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main").artifactId("simple-glassfish-api").version(GF_VERSION),
                mavenBundle().groupId("javax.transaction").artifactId("javax.transaction-api").version("1.2-b01"),
                mavenBundle().groupId("javax.resource").artifactId("javax.resource-api").version("1.7-b03"),
                mavenBundle().groupId("javax.enterprise.deploy").artifactId("javax.enterprise.deploy-api").version("1.6"),
                mavenBundle().groupId("org.glassfish.main.deployment").artifactId("dol").version(GF_VERSION),
                mavenBundle().groupId("javax.xml.rpc").artifactId("javax.xml.rpc-api").version("1.1"),
                mavenBundle().groupId("javax.xml.registry").artifactId("javax.xml.registry-api").version("1.0.4"),
                mavenBundle("com.sun.mail", "javax.mail", "1.4.5"),
                //Metro APIs
                mavenBundle("javax.xml.bind", "jaxb-api", JAXB_SPEC_VERSION),
                //XML Resolver
//                mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.xmlresolver", "1.2_5"),
                //Metro OSGi bundles
                mavenBundle("com.sun.xml.bind", "jaxb-osgi", JAXB_IMPL_VERSION),
                //                mavenBundle("com.sun.xml.bind", "jaxb-extra-osgi", JAXB_IMPL_VERSION),
                junitBundles(),
                felix());
    }

    @Test
    public void testLogger() {
        Class<?> c = loadClass("com.sun.istack.logging.Logger");
        assertClassLoadedByBundle(c, "com.sun.xml.bind.jaxb-osgi");
    }

    @Test
    public void testContextFactory101() {
        Class<?> c = loadClass("com.sun.xml.bind.ContextFactory_1_0_1");
        assertClassLoadedByBundle(c, "com.sun.xml.bind.jaxb-osgi");
    }

//    @Test
    public void DONTtestMsvDatatypes() {
        loadClass("com.sun.msv.datatype.xsd.IntegerType");
    }

//    @Test
    public void DONTtestXmlResolver() {
        loadClass("com.sun.org.apache.xml.internal.resolver.CatalogManager");
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
    }

}

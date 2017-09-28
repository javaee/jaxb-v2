/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

/**
 * The XML Binding (JAXB) RI modularization implementation.
 *
 * <p> This module is upgradeable.
 *
 * @uses javax.xml.bind.JAXBContextFactory
 *
 * @moduleGraph
 * @since 9
 */
module java.xml.bind.imp {
    requires java.xml.bind;
    requires java.compiler;
    requires java.desktop;
    requires java.logging;
    requires jdk.unsupported;

    requires transitive java.activation;
    requires transitive java.xml;

    exports com.sun.istack;
    exports com.sun.istack.localization;
    exports com.sun.istack.logging;
    exports com.sun.xml.bind;
    exports com.sun.xml.bind.annotation;
    exports com.sun.xml.bind.api;
    exports com.sun.xml.bind.api.impl;
    exports com.sun.xml.bind.marshaller;
    exports com.sun.xml.bind.unmarshaller;
    exports com.sun.xml.bind.util;
    exports com.sun.xml.bind.v2;
    exports com.sun.xml.bind.v2.model.annotation;
    exports com.sun.xml.bind.v2.model.core;
    exports com.sun.xml.bind.v2.model.impl;
    exports com.sun.xml.bind.v2.model.nav;
    opens com.sun.xml.bind.v2.model.nav;
    exports com.sun.xml.bind.v2.model.runtime;
    exports com.sun.xml.bind.v2.model.util;
    exports com.sun.xml.bind.v2.runtime;
    exports com.sun.xml.bind.v2.runtime.unmarshaller;
    exports com.sun.xml.bind.v2.schemagen;
    exports com.sun.xml.bind.v2.schemagen.episode;
    exports com.sun.xml.bind.v2.schemagen.xmlschema;
    exports com.sun.xml.bind.v2.util;
    exports com.sun.xml.txw2;
    exports com.sun.xml.txw2.annotation;
    exports com.sun.xml.txw2.output;

    exports com.sun.xml.fastinfoset;
    exports com.sun.xml.fastinfoset.stax;
    exports com.sun.xml.fastinfoset.vocab;
    exports org.jvnet.fastinfoset;
    exports org.jvnet.mimepull;
    exports org.jvnet.staxex;
    exports org.jvnet.staxex.util;

    uses javax.xml.bind.JAXBContextFactory;
}

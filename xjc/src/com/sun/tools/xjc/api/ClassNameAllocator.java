/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.api;

/**
 * Callback interface that allows the driver of the XJC API
 * to rename JAXB-generated classes/interfaces/enums.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ClassNameAllocator {
    /**
     * Hook that allows the client of the XJC API to rename some of the JAXB-generated classes.
     *
     * <p>
     * When registered, this calllbcak is consulted for every package-level
     * classes/interfaces/enums (hereafter, simply "classes")
     * that the JAXB RI generates. Note that
     * the JAXB RI does not use this allocator for nested/inner classes.
     *
     * <p>
     * If the allocator chooses to rename some classes. It is
     * the allocator's responsibility to find unique names.
     * If the returned name collides with other classes, the JAXB RI will
     * report errors.
     *
     * @param packageName
     *      The package name, such as "" or "foo.bar". Never be null.
     * @param className
     *      The short name of the proposed class name. Such as
     *      "Foo" or "Bar". Never be null, never be empty.
     *      Always a valid Java identifier.
     *
     * @return
     *      The short name of the class name that should be used.
     *      The class will be generated into the same package with this name.
     *      The return value must be a valid Java identifier. May not be null.
     */
    String assignClassName( String packageName, String className );
}

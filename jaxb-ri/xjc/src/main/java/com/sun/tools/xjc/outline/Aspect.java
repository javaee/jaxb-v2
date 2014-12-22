/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.outline;

import com.sun.tools.xjc.generator.bean.ImplStructureStrategy;

/**
 * Sometimes a single JAXB-generated bean spans across multiple Java classes/interfaces.
 * We call them "aspects of a bean".
 *
 * <p>
 * This is an enumeration of all possible aspects.
 *
 * @author Kohsuke Kawaguchi
 *
 * TODO: move this to the model package. We cannot do this before JAXB3 because of old plugins
 */
public enum Aspect {
    /**
     * The exposed part of the bean.
     * <p>
     * This corresponds to the content interface when we are geneting one.
     * This would be the same as the {@link #IMPLEMENTATION} when we are
     * just generating beans.
     *
     * <p>
     * This could be an interface, or it could be a class.
     *
     * We don't have any other {@link ImplStructureStrategy} at this point,
     * but generally you can't assume anything about where this could be
     * or whether that's equal to {@link #IMPLEMENTATION}.
     */
    EXPOSED,
    /**
     * The part of the bean that holds all the implementations.
     *
     * <p>
     * This is always a class, never an interface.
     */
    IMPLEMENTATION
}

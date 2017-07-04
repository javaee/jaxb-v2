/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.runtime.Location;

/**
 * {@link Location} that is chained.
 *
 * <p>
 * {@link Locatable} forms a tree structure, where each {@link Locatable}
 * points back to the upstream {@link Locatable}.
 * For example, imagine {@link Locatable} X that points to a particular annotation,
 * whose upstream is {@link Locatable} Y, which points to a particular method
 * (on which the annotation is put), whose upstream is {@link Locatable} Z,
 * which points to a particular class (in which the method is defined),
 * whose upstream is {@link Locatable} W,
 * which points to another class (which refers to the class Z), and so on.
 *
 * <p>
 * This chain will be turned into a list when we report the error to users.
 * This allows them to know where the error happened
 * and why that place became relevant.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Locatable {
    /**
     * Gets the upstream {@link Location} information.
     *
     * @return
     *      can be null.
     */
    Locatable getUpstream();

    /**
     * Gets the location object that this object points to.
     *
     * This operation could be inefficient and costly.
     */
    Location getLocation();
}

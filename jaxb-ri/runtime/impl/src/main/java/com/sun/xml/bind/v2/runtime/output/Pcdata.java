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

package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;

/**
 * Text data in XML.
 *
 * <p>
 * This class is used inside the marshaller/unmarshaller to
 * send/receive text data.
 *
 * <p>
 * On top of {@link CharSequence}, this class has an
 * ability to write itself to the {@link XmlOutput}. This allows
 * the implementation to choose the most efficient way possible
 * when writing to XML (for example, it can skip the escaping
 * of buffer copying.)
 *
 * TODO: visitor pattern support?
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Pcdata implements CharSequence {

    /**
     * Writes itself to {@link UTF8XmlOutput}.
     *
     * <p>
     * This is the most performance critical path for the marshaller,
     * so it warrants its own method.
     */
    public abstract void writeTo(UTF8XmlOutput output) throws IOException;

    /**
     * Writes itself to the character array.
     *
     * <p>
     * This method is used by most other {@link XmlOutput}.
     * The default implementation involves in one extra char[] copying.
     *
     * <p>
     * The caller must provide a big enough buffer that can hold
     * enough characters returned by the {@link #length()} method.
     */
    public void writeTo(char[] buf, int start) {
        toString().getChars(0,length(),buf,start);
    }

    public abstract String toString();
}

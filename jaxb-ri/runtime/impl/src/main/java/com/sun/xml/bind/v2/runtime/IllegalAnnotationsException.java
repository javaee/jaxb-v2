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

package com.sun.xml.bind.v2.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.xml.bind.v2.model.core.ErrorHandler;

/**
 * A list of {@link IllegalAnnotationException} wrapped in one exception.
 *
 * <p>
 * This exception is used to report all the errors to the client application
 * through {@link JAXBContext#newInstance}.
 *
 * @since JAXB 2.0 EA1
 * @author Kohsuke Kawaguchi
 */
public class IllegalAnnotationsException extends JAXBException {
    private final List<IllegalAnnotationException> errors;

    private static final long serialVersionUID = 1L;

    public IllegalAnnotationsException(List<IllegalAnnotationException> errors) {
        super(errors.size()+" counts of IllegalAnnotationExceptions");
        assert !errors.isEmpty() : "there must be at least one error";
        this.errors = Collections.unmodifiableList(new ArrayList<IllegalAnnotationException>(errors));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append('\n');
        
        for( IllegalAnnotationException error : errors )
            sb.append(error.toString()).append('\n');

        return sb.toString();
    }

    /**
     * Returns a read-only list of {@link IllegalAnnotationException}s
     * wrapped in this exception.
     *
     * @return
     *      a non-null list.
     */
    public List<IllegalAnnotationException> getErrors() {
        return errors;
    }

    public static class Builder implements ErrorHandler {
        private final List<IllegalAnnotationException> list = new ArrayList<IllegalAnnotationException>();
        public void error(IllegalAnnotationException e) {
            list.add(e);
        }
        /**
         * If an error was reported, throw the exception.
         * Otherwise exit normally.
         */
        public void check() throws IllegalAnnotationsException {
            if(list.isEmpty())
                return;
            throw new IllegalAnnotationsException(list);
        }
    }
}

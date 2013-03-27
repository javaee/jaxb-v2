/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.xjc.reader;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;

/**
 * Checks errors on model classes.
 *
 * <p>
 * This should be used as a {@link Ring} component.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ModelChecker {
    private final Model model = Ring.get(Model.class);
    private final ErrorReceiver errorReceiver = Ring.get(ErrorReceiver.class);

    public ModelChecker() {
    }

    public void check() {
        for( CClassInfo ci : model.beans().values() )
            check(ci);
    }

    private void check( CClassInfo ci ) {
        List<CPropertyInfo> props = ci.getProperties();
        Map<QName,CPropertyInfo> collisionTable = new HashMap<QName,CPropertyInfo>();

        OUTER:
        for( int i=0; i<props.size(); i++ ) {
            CPropertyInfo p1 = props.get(i);

            if(p1.getName(true).equals("Class")) {
                errorReceiver.error(p1.locator,Messages.PROPERTY_CLASS_IS_RESERVED.format());
                continue;
            }

            QName n = p1.collectElementNames(collisionTable);
            if(n!=null) {
                CPropertyInfo p2 = collisionTable.get(n);
                
                if (p2.getName(true).equals(n.toString()) || p2.getName(false).equals(n.toString())) {
                    errorReceiver.error(p1.locator, Messages.DUPLICATE_ELEMENT.format(n));
                    errorReceiver.error(p2.locator, Messages.ERR_RELEVANT_LOCATION.format());
                }
            }

            for( int j=i+1; j<props.size(); j++ ) {
                if(checkPropertyCollision(p1,props.get(j)))
                    continue OUTER;
            }
            for( CClassInfo c=ci.getBaseClass(); c!=null; c=c.getBaseClass() ) {
                for( CPropertyInfo p2 : c.getProperties() )
                    if(checkPropertyCollision(p1,p2))
                        continue OUTER;
            }
        }
    }

    private boolean checkPropertyCollision(CPropertyInfo p1, CPropertyInfo p2) {
        if(!p1.getName(true).equals(p2.getName(true)))
            return false;
        errorReceiver.error(p1.locator,Messages.DUPLICATE_PROPERTY.format(p1.getName(true)));
        errorReceiver.error(p2.locator,Messages.ERR_RELEVANT_LOCATION.format());
        return true;
    }
}

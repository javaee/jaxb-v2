/*
 * @(#)$Id: CollisionInfo.java,v 1.3 2007-11-22 00:54:05 kohsuke Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.tools.xjc.reader.xmlschema;

import org.xml.sax.Locator;

/**
 * Details of a name collision.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class CollisionInfo {
    private final String name;
    private final Locator source1;
    private final Locator source2;

    public CollisionInfo(String name, Locator source1, Locator source2) {
        this.name = name;
        this.source1 = source1;
        this.source2 = source2;
    }

    /**
     * Returns a localized message that describes the collision.
     */
    public String toString() {
        return Messages.format( Messages.MSG_COLLISION_INFO,
                name, printLocator(source1), printLocator(source2) );
    }

    private String printLocator(Locator loc) {
        if( loc==null )     return "";

        int line = loc.getLineNumber();
        String sysId = loc.getSystemId();
        if(sysId==null)     sysId = Messages.format(Messages.MSG_UNKNOWN_FILE);

        if( line!=-1 )
            return Messages.format( Messages.MSG_LINE_X_OF_Y,
                    Integer.toString(line), sysId );
        else
            return sysId;
    }
}

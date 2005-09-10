/*
 * @(#)$Id: CollisionInfo.java,v 1.2 2005-09-10 19:08:59 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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

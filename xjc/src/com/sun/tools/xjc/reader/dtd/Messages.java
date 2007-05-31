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

package com.sun.tools.xjc.reader.dtd;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
class Messages
{
    /** Loads a string resource and formats it with specified arguments. */
    static String format( String property, Object... args ) {
        String text = ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".MessageBundle").getString(property);
        return MessageFormat.format(text,args);
    }
    

    public static final String ERR_NO_ROOT_ELEMENT = // arg:0
        "TDTDReader.NoRootElement";

    public static final String ERR_UNDEFINED_ELEMENT_IN_BINDINFO = // arg:1
        "TDTDReader.UndefinedElementInBindInfo";

    public static final String ERR_CONVERSION_FOR_NON_VALUE_ELEMENT = // arg:1
        "TDTDReader.ConversionForNonValueElement";

    public static final String ERR_CONTENT_PROPERTY_PARTICLE_MISMATCH = // arg:1
        "TDTDReader.ContentProperty.ParticleMismatch";

    public static final String ERR_CONTENT_PROPERTY_DECLARATION_TOO_SHORT = // arg:1
        "TDTDReader.ContentProperty.DeclarationTooShort";
    
    public static final String ERR_BINDINFO_NON_EXISTENT_ELEMENT_DECLARATION = // arg:1
        "TDTDReader.BindInfo.NonExistentElementDeclaration";

    public static final String ERR_BINDINFO_NON_EXISTENT_INTERFACE_MEMBER = // arg:1
        "TDTDReader.BindInfo.NonExistentInterfaceMember";
        
}

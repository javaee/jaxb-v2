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

package com.sun.xml.bind.v2.runtime;


import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;


/**
 * {@link XmlAdapter} that binds the value as a SOAP attachment.
 *
 * <p>
 * On the user classes the SwA handling is done by using the {@link XmlAttachmentRef}
 * annotation, but internally we treat it as a {@link XmlJavaTypeAdapter} with this
 * adapter class. This is true with both XJC and the runtime.
 *
 * <p>
 * the model builder code and the code generator does the conversion and
 * shield the rest of the RI from this mess.
 * Also see @see <a href="http://webservices.xml.com/pub/a/ws/2003/09/16/wsbp.html?page=2">http://webservices.xml.com/pub/a/ws/2003/09/16/wsbp.html?page=2</a>.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SwaRefAdapter extends XmlAdapter<String,DataHandler> {

    public SwaRefAdapter() {
    }

    public DataHandler unmarshal(String cid) {
        AttachmentUnmarshaller au = UnmarshallingContext.getInstance().parent.getAttachmentUnmarshaller();
        // TODO: error check
        return au.getAttachmentAsDataHandler(cid);
    }

    public String marshal(DataHandler data) {
        if(data==null)      return null;
        AttachmentMarshaller am = XMLSerializer.getInstance().attachmentMarshaller;
        // TODO: error check
        return am.addSwaRefAttachment(data);
    }
}

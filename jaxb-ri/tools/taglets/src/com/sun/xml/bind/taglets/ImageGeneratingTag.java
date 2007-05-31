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

package com.sun.xml.bind.taglets;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import com.sun.tools.doclets.internal.toolkit.taglets.TagletOutput;
import com.sun.tools.doclets.internal.toolkit.taglets.TagletWriter;
import com.sun.javadoc.Tag;
import com.sun.javadoc.PackageDoc;

/**
 * Taglet that generates an image by calling a remote service.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ImageGeneratingTag extends FileGeneratingTag {

    /**
     * Seed of the file name.
     */
    private final String baseFileName;

    private final URL serviceURL;

    protected ImageGeneratingTag(String serviceURL, String baseFileName) {
        try {
            this.serviceURL = new URL(serviceURL);
            this.baseFileName = baseFileName;
        } catch (MalformedURLException e) {
            throw new Error(e);
        }
    }

    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        TagletOutput output = writer.getOutputInstance();

        // where does this tag belong to?
        PackageDoc pkg = getPackage(tag.holder());

        String imageFileName = baseFileName+getImageIndex(pkg)+".png";

        File imageFile = getOutputFile(writer, pkg, imageFileName);

        try {
            System.out.println("Generating an image to "+imageFile);
            ImageGenerator.generateImage(serviceURL,getContents(tag),imageFile);
            System.out.println("done");
        } catch (IOException e) {
            throw new Error(e);
        }

        output.setOutput("<div><center><img src='"+imageFileName+"'></center></div>");
        return output;
    }

    /**
     * Gets the text that generates image.
     */
    protected String getContents(Tag tag) {
        return tag.text();
    }

}

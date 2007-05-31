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
package com.sun.tools.xjc.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.awt.*;

import javax.swing.*;

/**
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Reader in =
            new InputStreamReader(
                Main.class.getResourceAsStream("/license.txt"));
        
        boolean accepted;
        
        if (args.length>0 && args[0].equals("-console")) {
            accepted = doConsole(in);
        } else {
            try {
                setUILookAndFeel();
                LicenseForm form = new LicenseForm(in);
                form.show();
                accepted = form.isAccepted();
            } catch( InternalError e ) {
                // if there's no GUI, I get InternalError or HeadlessException
                accepted = doConsole(in);
            } catch( HeadlessException e ) {
                accepted = doConsole(in);
            }
        }
        
        if(accepted)
            install();
        
        System.exit(accepted?0:1);
    }

    private static boolean doConsole(Reader in) throws IOException {
        boolean accepted;
        ConsoleForm form = new ConsoleForm(in);
        form.show();
        accepted = form.isAccepted();
        return accepted;
    }

    /**
     * Sets to the platform native look and feel.
     *
     * see http://javaalmanac.com/egs/javax.swing/LookFeelNative.html
     */
    private static void setUILookAndFeel() {
        // Get the native look and feel class name
        String nativeLF = UIManager.getSystemLookAndFeelClassName();

        // Install the look and feel
        try {
            UIManager.setLookAndFeel(nativeLF);
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        } catch (UnsupportedLookAndFeelException e) {
        } catch (IllegalAccessException e) {
        }
    }

    /**
     * Does the actual installation.
     */
    private static void install() throws IOException {
        ZipInputStream zip =
            new ZipInputStream(Main.class.getResourceAsStream("/package.zip"));
        ZipEntry e;
        while ((e = zip.getNextEntry()) != null) {
            File name = new File(e.getName());
            System.out.println(name);
            if (e.isDirectory()) {
                name.mkdirs();
            } else {
                File parent = name.getParentFile();
                if(parent!=null && !parent.exists())
                    parent.mkdirs();
                if (!name.exists())
                    copyStream(zip, new FileOutputStream(name));
            }
        }
        zip.close();
        System.out.println("installation complete");
    }

    public static void copyStream(InputStream in, OutputStream out)
        throws IOException {
        byte[] buf = new byte[256];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
        out.close();
    }
}

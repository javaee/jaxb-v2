import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.regex.Pattern;

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

/**
 * Clean up the specified Java source files.
 * 
 * Specifically, we currently:
 * <ul>
 *  <li>strips TODO comments
 * </ul>
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SourceTidy {
    
    private static final Pattern todoComment = Pattern.compile(".*//.*TODO.*");

    public static void main(String[] args) throws IOException {
        for( int i=0; i<args.length; i++ )
            process(new File(args[i]));
    }
    
    private static void process( File file ) throws IOException {
        if( file.isDirectory() ) {
            File[] files = file.listFiles();
            for( int i=0; i<files.length; i++ )
            	process(files[i]);
        } else
            processFile(file);
    }
    
    private static void processFile( File file ) throws IOException {
        if( !file.getName().endsWith(".java") )
            return; // skip
        
        // System.out.println("processing "+file);
        
        BufferedReader in = new BufferedReader(new FileReader(file));
        File tmp = new File(file.getPath()+".tmp");
        
        try {
            PrintWriter out = new PrintWriter(new FileWriter(tmp));
            
            String line;
            do {
                line = in.readLine();
            } while( !line.startsWith("package") );
            
            out.println(line);
            
            while((line=in.readLine())!=null) {
                if( todoComment.matcher(line).matches() )
                    ;   // ignore this line
                else
                    out.println(line);
            }
            
            in.close();
            out.close();
        } catch( IOException e ) {
            tmp.delete();
            throw e;
        }
        
        // replace in with out
        file.delete();
        tmp.renameTo(file);
    }
}

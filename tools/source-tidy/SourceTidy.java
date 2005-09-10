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
 * @(#)$Id: SourceTidy.java,v 1.2 2005-09-10 19:08:39 kohsuke Exp $
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

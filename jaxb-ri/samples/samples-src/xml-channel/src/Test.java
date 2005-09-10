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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;


/**
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Test {

    public static Object lock = new Object();
    public static boolean ready = false;
    
    public static void main(String[] args) throws Exception {
        // launch the server
        new Thread(new TestServer()).start();

        // wait for the server to become ready
        while( !ready ) {
            synchronized( lock ) {
                lock.wait(1000);
            }
        }
        
        // reset the flag
        ready = false;
        
        // run the client
        new TestClient().run();

        // wait for the server to finish processing data  
        // from the client 
        while( !ready ) {
            synchronized( lock ) {
                lock.wait(1000);
            }
        }

        System.exit(0);
    }
}

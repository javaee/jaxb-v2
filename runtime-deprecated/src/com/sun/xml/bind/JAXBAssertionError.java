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

/*
 * Created on 2003/06/18
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sun.xml.bind;

import java.io.PrintWriter;

/**
 * Signals an assertion failure in the RI.
 * 
 * @since 1.0.2
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JAXBAssertionError extends Error {
	private final Throwable linkedException;
	
	public JAXBAssertionError() {
		super();
		linkedException = null;
	}
	
	public JAXBAssertionError( String errorMessage ) {
		super(errorMessage);
		linkedException = null;
	}
	
	public JAXBAssertionError( Throwable _linkedException ) {
		super();
		this.linkedException = _linkedException;
	}

 
	public void printStackTrace( java.io.PrintStream s ) {
		if( linkedException != null ) {
		  linkedException.printStackTrace(s);
		  s.println("--------------- linked to ------------------");
		}

		super.printStackTrace(s);
	}
 
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	public void printStackTrace(PrintWriter s) {
		if( linkedException != null ) {
		  linkedException.printStackTrace(s);
		  s.println("--------------- linked to ------------------");
		}

		super.printStackTrace(s);
	}
}

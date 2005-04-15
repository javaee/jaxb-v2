/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

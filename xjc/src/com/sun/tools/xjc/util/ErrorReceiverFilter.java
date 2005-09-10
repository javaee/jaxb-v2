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
package com.sun.tools.xjc.util;

import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.api.ErrorListener;

import org.xml.sax.SAXParseException;

/**
 * Filter implementation of the ErrorReceiver.
 * 
 * If an error is encountered, this filter sets a flag.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ErrorReceiverFilter extends ErrorReceiver {

    public ErrorReceiverFilter() {}

    public ErrorReceiverFilter( ErrorListener h ) {
        setErrorReceiver(h);
    }

    private ErrorListener core;
    public void setErrorReceiver( ErrorListener handler ) {
        core = handler;
    }

    private boolean hadError = false;
    public final boolean hadError() { return hadError; }

    public void info(SAXParseException exception) {
        if(core!=null)  core.info(exception);
    }

    public void warning(SAXParseException exception) {
        if(core!=null)  core.warning(exception);
    }

    public void error(SAXParseException exception) {
        hadError = true;
        if(core!=null)  core.error(exception);
    }

    public void fatalError(SAXParseException exception) {
        hadError = true;
        if(core!=null)  core.fatalError(exception);
    }

}

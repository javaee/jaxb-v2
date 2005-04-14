/*
 * @(#)$Id: ContentTypeImpl.java,v 1.1 2005-04-14 22:06:25 kohsuke Exp $
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.xml.xsom.impl;

import com.sun.xml.xsom.XSContentType;

/**
 * Marker interface that says this implementation
 * implements XSContentType.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface ContentTypeImpl extends Ref.ContentType, XSContentType {

}

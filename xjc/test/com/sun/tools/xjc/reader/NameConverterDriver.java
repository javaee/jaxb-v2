/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader;

import com.sun.xml.bind.v2.NameConverter;


public class NameConverterDriver
{
    public static void main( String[] args ) {
        for( int i=0; i<args.length; i++ )
        System.out.println( NameConverter.standard.toConstantName(args[i]) );
    }
}

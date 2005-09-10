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

package com.sun.codemodel;


/**
 * Modifier constants.
 */
public final class JMod {
    public final static int NONE         = 0x000;
    public final static int PUBLIC       = 0x001;
    public final static int PROTECTED    = 0x002;
    public final static int PRIVATE      = 0x004;
    public final static int FINAL        = 0x008;
    public final static int STATIC       = 0x010;
    public final static int ABSTRACT     = 0x020;
    public final static int NATIVE       = 0x040;
    public final static int SYNCHRONIZED = 0x080;
    public final static int TRANSIENT    = 0x100;
    public final static int VOLATILE     = 0x200;
}

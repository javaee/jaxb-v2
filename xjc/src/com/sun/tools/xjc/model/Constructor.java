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
package com.sun.tools.xjc.model;

/**
 * Constructor declaration.
 * 
 * <p>
 * a constructor declaration consists of a set of fields to be initialized.
 * For example, if a class is defined as:
 * 
 * <pre>
 * Class: Foo
 *   Field: String a
 *   Field: int b
 *   Field: BigInteger c
 * </pre>
 * 
 * Then a constructor declaration of {"a","c"} will conceptually
 * generate the following consturctor:
 * 
 * <pre>
 * Foo( String _a, BigInteger _c ) {
 *   a=_a; c=_c;
 * }
 * </pre>
 * 
 * (Only conceptually, because Foo will likely to become an interface
 * so we can't simply generate a constructor like this.)
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class Constructor
{
    // Since Constructor is typically built when there is no FieldItem
    // nor FieldUse, we need to rely on Strings.
    public Constructor( String[] _fields ) { this.fields = _fields; }
    
    /** array of field names to be initialized. */
    public final String[] fields;
}

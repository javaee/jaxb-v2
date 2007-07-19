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

/*
Copyright (c) 2001, 2002 Thai Open Source Software Center Ltd
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

    Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in
    the documentation and/or other materials provided with the
    distribution.

    Neither the name of the Thai Open Source Software Center Ltd nor
    the names of its contributors may be used to endorse or promote
    products derived from this software without specific prior written
    permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.sun.xml.xsom.impl.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class Uri {
  private static String utf8 = "UTF-8";

  public static boolean isValid(String s) {
    return isValidPercent(s) && isValidFragment(s) && isValidScheme(s);
  }

  private static final String HEX_DIGITS = "0123456789abcdef";

  public static String escapeDisallowedChars(String s) {
    StringBuffer buf = null;
    int len = s.length();
    int done = 0;
    for (;;) {
      int i = done;
      for (;;) {
        if (i == len) {
          if (done == 0)
            return s;
          break;
        }
        if (isExcluded(s.charAt(i)))
          break;
        i++;
      }
      if (buf == null)
        buf = new StringBuffer();
      if (i > done) {
        buf.append(s.substring(done, i));
        done = i;
      }
      if (i == len)
        break;
      for (i++; i < len && isExcluded(s.charAt(i)); i++)
        ;
      String tem = s.substring(done, i);
      byte[] bytes;
      try {
        bytes = tem.getBytes(utf8);
      }
      catch (UnsupportedEncodingException e) {
        utf8 = "UTF8";
        try {
          bytes = tem.getBytes(utf8);
        }
        catch (UnsupportedEncodingException e2) {
          // Give up
          return s;
        }
      }
      for (int j = 0; j < bytes.length; j++) {
        buf.append('%');
        buf.append(HEX_DIGITS.charAt((bytes[j] & 0xFF) >> 4));
        buf.append(HEX_DIGITS.charAt(bytes[j] & 0xF));
      }
      done = i;
    }
    return buf.toString();
  }

  private static String excluded = "<>\"{}|\\^`";

  private static boolean isExcluded(char c) {
    return c <= 0x20 || c >= 0x7F || excluded.indexOf(c) >= 0;
  }

  private static boolean isAlpha(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }

  private static boolean isHexDigit(char c) {
    return ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || isDigit(c);
  }

  private static boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }
  
  private static boolean isSchemeChar(char c) {
    return isAlpha(c) || isDigit(c) || c == '+' || c == '-' || c =='.';
  }

  private static boolean isValidPercent(String s) {
    int len = s.length();
    for (int i = 0; i < len; i++)
      if (s.charAt(i) == '%') {
        if (i + 2 >= len)
          return false;
        else if (!isHexDigit(s.charAt(i + 1))
             || !isHexDigit(s.charAt(i + 2)))
          return false;
      }
    return true;
  }

  private static boolean isValidFragment(String s) {
    int i = s.indexOf('#');
    return i < 0 || s.indexOf('#', i + 1) < 0;
  }

  private static boolean isValidScheme(String s) {
    if (!isAbsolute(s))
      return true;
    int i = s.indexOf(':');
    if (i == 0
    || i + 1 == s.length()
    || !isAlpha(s.charAt(0)))
      return false;
    while (--i > 0)
      if (!isSchemeChar(s.charAt(i)))
    return false;
    return true;
  }

    public static String resolve(String baseUri, String uriReference) throws IOException {
        if (isAbsolute(uriReference))
            return uriReference;

        if(baseUri==null)
            throw new IOException("Unable to resolve relative URI "+uriReference+" without a base URI");

        if(!isAbsolute(baseUri))
            throw new IOException("Unable to resolve relative URI "+uriReference+" because base URI is not absolute: "+baseUri);

        return new URL(new URL(baseUri), uriReference).toString();
    }

    public static boolean hasFragmentId(String uri) {
    return uri.indexOf('#') >= 0;
  }

  public static boolean isAbsolute(String uri) {
    int i = uri.indexOf(':');
    if (i < 0)
      return false;
    while (--i >= 0) {
      switch (uri.charAt(i)) {
      case '#':
      case '/':
      case '?':
    return false;
      }
    }
    return true;
  }
}

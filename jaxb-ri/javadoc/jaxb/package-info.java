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

/**
 * <h1>JAXB RI Architecture Document</h1>.
 *
 * <h2>JAXB RI Major Modules and Libraries</h2>
 * {@DotDiagram
     digraph G {
       "JAX-WS" [label="JAX-WS RI"];

       // libraries
       node [style=filled,color=lightblue];
       XSOM; RNGOM; TXW; "DTD parser";

       // modules
       node [style=filled,color=lightpink];
       XJC; CodeModel; runtime; schemagen; "XJC API"; "runtime API";

       "JAX-WS" -> "XJC API" -> CodeModel;
       "JAX-WS" -> "runtime API";
       XJC -> XSOM;
       XJC -> "DTD parser";
       XJC -> RNGOM;
       XJC -> "XJC API";
       XJC -> CodeModel;
       XJC -> runtime -> schemagen -> TXW;
       runtime -> "runtime API";
     }
 * }
 * <div align=right>
 * <b>Legend:</b> blue: external library, pink: module
 * </div>
 * 
 *
 * <h2>Modules</h2>
 * <p>
 * The JAXB RI consists of the following major modules.
 *
 * <dl>
 *  <dt>{@link com.sun.xml.bind.v2 runtime}
 *  <dd>
 *    runtime module is available at application runtime and provide the actual
 *    XML unmarshalling/marshalling capability. Notably, it implements {@link JAXBContext}.
 *
 *  <dt>{@link com.sun.codemodel CodeModel}
 *  <dd>
 *    Library for generating Java source code
 *
 *  <dt>{@link com.sun.tools.xjc.api XJC-API}
 *  <dd>
 *    A part of the XJC that defines the contract between the JAXB RI and
 *    the JAX-WS RI.
 *
 *  <dt>{@link com.sun.tools.xjc XJC}
 *  <dd>
 *    The schema compiler.
 *
 *  <dt>{@link com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator schemagen}
 *  <dd>
 *    The XML Schema generator. For historical reason it lives in its own module.
 *
 *  <dt>{@link com.sun.xml.bind.api runtime-API}</a>
 *  <dd>
 *    A part of the runtime that defines the contract between the JAXB RI and
 *    the JAX-WS RI.
 *
 * </dl>
 *
 * <h2>Libraries</h2>
 * <p>
 * JAXB RI uses the following major libraries extensively.
 * <dl>
 *  <dt><a href="#">XSOM</a>
 *  <dd>
 *    Library for parsing XML Schema into in-memory representations
 *
 *  <dt><a href="http://rngom.dev.java.net/">RNGOM</a>
 *  <dd>
 *    Library for parsing RELAX NG into in-memory representation
 *
 *  <dt><a href="http://dtd-parser.dev.java.net/">DTD parser</a>
 *  <dd>
 *    Library for parsing DTD into in-memory representation
 *
 *  <dt><a href="#">TXW</a>
 *  <dd>
 *    Library for writing XML
 * </dl>
 *
 *
 * <h2>About This Document</h2>
 * <p>
 * See {@link jaxb.MetaArchitectureDocument} for how to contribute to this document.
 *
 * @ArchitectureDocument
 */
package jaxb;

import javax.xml.bind.JAXBContext;

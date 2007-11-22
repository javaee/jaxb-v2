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

package jaxb;

/**
 * <h1>Meta Architecture Document</h1>.
 *
 * <p>
 * This document explains how this architecture document is written.
 *
 * <p>
 * All of the JAXB RI architecture document is built as javadoc.
 * So contributing to this document means writing/modifying the javadoc and commiting it.
 * <p>
 * Using javadoc makes it easier to keep the documents in sync, thanks to modern IDEs,
 * and it also allows us to link to various code pieces more easily.
 *
 * <h3>The <tt>javadoc</tt> module</h3>
 * <p>
 * The JAXB RI workspace contains a special 'javadoc' module, which is used exclusively
 * for hosting architecture document javadoc. Normally, the architecture document is
 * written on a package/class that provides the functionality, but this module can be
 * used to write a part of the document that doesn't relate to any particular code
 * (such as this document itself.)
 *
 *
 * <h3>Javadoc Hacks</h3>
 * <p>
 * The out-of-box javadoc tool lacks several features for writing documentations,
 * so we wrote a few custom javadoc tags to enhance the expressiveness.
 *
 *
 * <h3>ArchitectureDocument tag</h3>
 * <p>
 * The {@code ArchitectureDocument} tag is a block tag that can be written on
 * a class or a package. When present, it changes the way the javadoc is generated
 * for that class/package. Specifically, it brings the description page to the very
 * top of the HTML page (whereas normally the description is after the list of members.)
 * <p>
 * This improves the readability by eliminating the needs to scroll down.
 *
 *
 *
 * <h3>SequenceDiagram tag</h3>
 * <p>
 * The {@code SequenceDiagram} tag is an inline tag that can be written anywhere.
 * This tag allows you to write an UML sequence diagram in a declarative fashion.
 *
 * <p>
 * For example, the following javadoc will produce the following picture:
 * <pre>{&#64;SequenceDiagram
# Define the objects
object(O,"o:Toolkit");
placeholder_object(P);
step();

# Message sequences
active(O);
step();
active(O);
message(O,O,"callbackLoop()");
inactive(O);
create_message(O,P,"p:Peer");
message(O,P,"handleExpose()");
active(P);
return_message(P,O,"");
inactive(P);
destroy_message(O,P);
inactive(O);

# Complete the lifelines
step();
complete(O);
 * }</pre>
 * {@SequenceDiagram
# Define the objects
object(O,"o:Toolkit");
placeholder_object(P);
step();

# Message sequences
active(O);
step();
active(O);
message(O,O,"callbackLoop()");
inactive(O);
create_message(O,P,"p:Peer");
message(O,P,"handleExpose()");
active(P);
return_message(P,O,"");
inactive(P);
destroy_message(O,P);
inactive(O);

# Complete the lifelines
step();
complete(O);
 * }
 *
 * <p>
 * For details about the syntax, see
 * <a href="http://www.spinellis.gr/sw/umlgraph/doc/index.html">this document</a>.
 *
 * <p>
 * When you are writing a diagram, it's convenient to use
 * <a href="http://kohsuke.sfbay.sun.com/sequence-diagram/">a scratch pad (Sun-internal)</a>
 * to quickly check the output of your description.
 *
 *
 *
 *
 *
 * <h3>DotDiagram tag</h3>
 * <p>
 * The {@code DotDiagram} tag is similar to the {@code SequenceDiagram} tag in its concept.
 * It is an inline tag that can be written anywhere.
 * This tag allows you to write a box and arrow diagram by using
 * the dot program in the <a href="http://www.graphviz.org/">GraphViz</a> package.
 *
 * <p>
 * For example, the following javadoc will produce the following picture:
 * <pre>{&#64;DotDiagram
digraph G {
	size ="4,4";
	main [shape=box]; // this is a comment
	main -> parse [weight=8];
	parse -> execute;
	main -> init [style=dotted];
	main -> cleanup;
	execute -> { make_string; printf}
	init -> make_string;
	edge [color=red]; // so is this
	main -> printf [style=bold,label="100 times"];
	make_string [label="make a\nstring"];
	node [shape=box,style=filled,color=".7 .3 1.0"];
	execute -> compare;
}
 * }</pre>
 * {@DotDiagram
digraph G {
	size ="4,4";
	main [shape=box]; // this is a comment
	main -> parse [weight=8];
	parse -> execute;
	main -> init [style=dotted];
	main -> cleanup;
	execute -> { make_string; printf}
	init -> make_string;
	edge [color=red]; // so is this
	main -> printf [style=bold,label="100 times"];
	make_string [label="make a\nstring"];
	node [shape=box,style=filled,color=".7 .3 1.0"];
	execute -> compare;
}
 * }
 *
 * <p>
 * For details about the syntax, see
 * <a href="http://www.graphviz.org/Documentation/dotguide.pdf">this document</a>.
 *
 * <p>
 * When you are writing a diagram, it's convenient to use
 * <a href="http://kohsuke.sfbay.sun.com/graphviz-server/">a scratch pad (Sun-internal)</a>
 * to quickly check the output of your description.
 *
 *
 * <h2>Generating the document</h2>
 * <p>
 * To generate the document locally, run {@code ant architecture-document} from the command line.
 * Because of the way the custom javadoc tags work, this only works for computers
 * running inside Sun.
 *
 * <p>
 * Alternatively, Hudson automatically posts a new architecture document to
 * <a href="https://jaxb-architecture-document.dev.java.net/nonav/doc/">the java.net</a>
 * every time a commit is made. This process typically takes 10 minutes
 * (so in the worst case, it takes 20 minutes for your change to be reflected.)
 *
 *
 *
 * <h2>Browse the document</h2>
 * <p>
 * The up-to-date version of the document is always available at
 * <a href="https://jaxb-architecture-document.dev.java.net/nonav/doc/">the java.net</a>.
 *
 *
 * @ArchitectureDocument
 * @author Kohsuke Kawaguchi
 */
public class MetaArchitectureDocument {
}

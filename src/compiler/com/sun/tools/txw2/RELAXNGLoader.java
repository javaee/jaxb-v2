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

package com.sun.tools.txw2;

import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.ast.util.CheckingSchemaBuilder;
import org.kohsuke.rngom.dt.CascadingDatatypeLibraryFactory;
import org.kohsuke.rngom.dt.builtin.BuiltinDatatypeLibraryFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import com.sun.tools.txw2.model.NodeSet;
import com.sun.tools.txw2.model.Leaf;
import com.sun.tools.txw2.builder.relaxng.SchemaBuilderImpl;

/**
 * @author Kohsuke Kawaguchi
 */
class RELAXNGLoader implements SchemaBuilder {
    private final Parseable parseable;

    public RELAXNGLoader(Parseable parseable) {
        this.parseable = parseable;
    }

    public NodeSet build(TxwOptions options) throws IllegalSchemaException {
        SchemaBuilderImpl stage1 = new SchemaBuilderImpl(options.codeModel);
        Leaf pattern = (Leaf)parseable.parse(new CheckingSchemaBuilder(stage1,options.errorListener,
            new CascadingDatatypeLibraryFactory(
                new BuiltinDatatypeLibraryFactory(new DatatypeLibraryLoader()),
                new DatatypeLibraryLoader())));

        return new NodeSet(options,pattern);
    }
}

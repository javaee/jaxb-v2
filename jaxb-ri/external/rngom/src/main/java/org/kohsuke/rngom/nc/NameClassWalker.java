package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

/**
 * @author Kohsuke Kawaguchi
 */
public class NameClassWalker implements NameClassVisitor<Void> {

    public Void visitChoice(NameClass nc1, NameClass nc2) {
        nc1.accept(this);
        return nc2.accept(this);
    }

    public Void visitNsName(String ns) {
        return null;
    }

    public Void visitNsNameExcept(String ns, NameClass nc) {
        return nc.accept(this);
    }

    public Void visitAnyName() {
        return null;
    }

    public Void visitAnyNameExcept(NameClass nc) {
        return nc.accept(this);
    }

    public Void visitName(QName name) {
        return null;
    }

    public Void visitNull() {
        return null;
    }
}

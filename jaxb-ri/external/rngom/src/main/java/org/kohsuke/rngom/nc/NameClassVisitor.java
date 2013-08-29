package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

/**
 * Visitor pattern over {@link NameClass} and its subclasses.
 */
public interface NameClassVisitor<V> {
    /**
     * Called for {@link ChoiceNameClass}
     */
    V visitChoice(NameClass nc1, NameClass nc2);
    /**
     * Called for {@link NsNameClass}
     */
    V visitNsName(String ns);
    /**
     * Called for {@link NsNameExceptNameClass}
     */
    V visitNsNameExcept(String ns, NameClass nc);
    /**
     * Called for {@link NameClass#ANY}
     */
    V visitAnyName();
    /**
     * Called for {@link AnyNameExceptNameClass}
     */
    V visitAnyNameExcept(NameClass nc);
    /**
     * Called for {@link SimpleNameClass}
     */
    V visitName(QName name);
    /**
     * Called for {@link NameClass#NULL}.
     */
    V visitNull();
}

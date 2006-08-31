package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSComponent;

import java.util.Iterator;

/**
 * Schema component designator.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SCDImpl extends SCD {
    /**
     * SCD is fundamentally a list of steps.
     */
    private final Step[] steps;

    public SCDImpl(Step[] steps) {
        this.steps = steps;
    }

    public Iterator<XSComponent> select(Iterator<? extends XSComponent> contextNode) {
        Context context = new Context();
        context.nodeSet = (Iterator)contextNode;

        int len = steps.length;
        for( int i=0; i<len; i++ ) {
            if(i!=0 && i!=len-1) {
                // expand the current nodeset by adding abbreviatable complex type and model groups.
                // TODO: this step is not needed if the next step is known not to react to
                // complex type nor model groups, such as, say Axis.FACET
                context.nodeSet = new Iterators.Unique<XSComponent>(
                    new Iterators.Map<XSComponent,XSComponent>(context.nodeSet) {
                        protected Iterator<XSComponent> apply(XSComponent u) {
                            return new Iterators.Union<XSComponent>(
                                Iterators.singleton(u),
                                Axis.INTERMEDIATE_SKIP.iterator(u) );
                        }
                    }
                );
            }
            context.nodeSet = steps[i].evaluate(context);
        }

        return context.nodeSet;
    }
}

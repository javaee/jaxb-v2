package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSComponent;

import java.util.List;
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
    private final List<Step> steps;

    public SCDImpl(List<Step> steps) {
        this.steps = steps;
    }

    public Iterator<XSComponent> select(Iterator<? extends XSComponent> contextNode) {
        Context context = new Context();
        context.nodeSet = (Iterator)contextNode;

        for (Step step : steps)
            context.nodeSet = step.evaluate(context);

        return context.nodeSet;
    }
}

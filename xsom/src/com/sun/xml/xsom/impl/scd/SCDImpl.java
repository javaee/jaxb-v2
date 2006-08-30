package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSComponent;

import java.util.List;

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

    public List<XSComponent> select(List<XSComponent> contextNode) {
        Context context = new Context();
        context.nodeSet = contextNode;

        for (Step step : steps)
            context.nodeSet = step.evaluate(context);

        return context.nodeSet;
    }
}

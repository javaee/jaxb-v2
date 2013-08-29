package org.kohsuke.rngom.digested;

/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class DDefine {
    private final String name;

    private DPattern pattern;
    private Boolean nullable;
    DAnnotation annotation;

    public DDefine(String name) {
        this.name = name;
    }

    public DPattern getPattern() {
        return pattern;
    }

    public DAnnotation getAnnotation() {
        if(annotation==null)
            return DAnnotation.EMPTY;
        return annotation;
    }

    public void setPattern(DPattern pattern) {
        this.pattern = pattern;
        this.nullable = null;
    }

    /**
     * Gets the name of the pattern block.
     */
    public String getName() {
        return name;
    }

    public boolean isNullable() {
        if(nullable==null)
            nullable = pattern.isNullable()?Boolean.TRUE:Boolean.FALSE;
        return nullable.booleanValue();
    }
}

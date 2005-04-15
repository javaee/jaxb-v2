package com.sun.istack;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * {@link AnnotationProcessorFactory} that counts the current
 * round.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class CountingAnnotationProcessorFactory implements AnnotationProcessorFactory {

    public final AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env) {
        return getProcessorFor(calcRound(),atds,env);
    }

    private static final Map<Class,Integer> rounds = new WeakHashMap<Class,Integer>();

    /**
     * Returns the current round index and increments it by one.
     *
     * APT creates a new instance of factory for each round, so
     * we have to store this information outside this instance.
     */
    private final int calcRound() {
        Integer i = rounds.get(getClass());
        if(i==null)     i=0;

        rounds.put(getClass(),i+1);

        return i;
    }

    /**
     * Does the work of {@link AnnotationProcessorFactory#getProcessorFor(Set<AnnotationTypeDeclaration>, AnnotationProcessorEnvironment)}
     * but also receives the round number.
     *
     * @param round
     *      The number of the current round. The first round is 0, the second round is 1, and so on.
     */
    protected abstract AnnotationProcessor getProcessorFor(int round, Set<AnnotationTypeDeclaration> atds, AnnotationProcessorEnvironment env);
}

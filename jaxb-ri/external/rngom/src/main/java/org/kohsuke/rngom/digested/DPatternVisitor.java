package org.kohsuke.rngom.digested;



/**
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface DPatternVisitor<V> {
    V onAttribute( DAttributePattern p );
    V onChoice( DChoicePattern p );
    V onData( DDataPattern p );
    V onElement( DElementPattern p );
    V onEmpty( DEmptyPattern p );
    V onGrammar( DGrammarPattern p );
    V onGroup( DGroupPattern p );
    V onInterleave( DInterleavePattern p );
    V onList( DListPattern p );
    V onMixed( DMixedPattern p );
    V onNotAllowed( DNotAllowedPattern p );
    V onOneOrMore( DOneOrMorePattern p );
    V onOptional( DOptionalPattern p );
    V onRef( DRefPattern p );
    V onText( DTextPattern p );
    V onValue( DValuePattern p );
    V onZeroOrMore( DZeroOrMorePattern p );
}

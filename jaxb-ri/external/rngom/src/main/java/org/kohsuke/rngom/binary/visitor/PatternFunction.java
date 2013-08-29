package org.kohsuke.rngom.binary.visitor;

import org.kohsuke.rngom.binary.AfterPattern;
import org.kohsuke.rngom.binary.AttributePattern;
import org.kohsuke.rngom.binary.ChoicePattern;
import org.kohsuke.rngom.binary.DataExceptPattern;
import org.kohsuke.rngom.binary.DataPattern;
import org.kohsuke.rngom.binary.ElementPattern;
import org.kohsuke.rngom.binary.EmptyPattern;
import org.kohsuke.rngom.binary.ErrorPattern;
import org.kohsuke.rngom.binary.GroupPattern;
import org.kohsuke.rngom.binary.InterleavePattern;
import org.kohsuke.rngom.binary.ListPattern;
import org.kohsuke.rngom.binary.NotAllowedPattern;
import org.kohsuke.rngom.binary.OneOrMorePattern;
import org.kohsuke.rngom.binary.RefPattern;
import org.kohsuke.rngom.binary.TextPattern;
import org.kohsuke.rngom.binary.ValuePattern;

public interface PatternFunction {
    Object caseEmpty(EmptyPattern p);
    Object caseNotAllowed(NotAllowedPattern p);
    Object caseError(ErrorPattern p);
    Object caseGroup(GroupPattern p);
    Object caseInterleave(InterleavePattern p);
    Object caseChoice(ChoicePattern p);
    Object caseOneOrMore(OneOrMorePattern p);
    Object caseElement(ElementPattern p);
    Object caseAttribute(AttributePattern p);
    Object caseData(DataPattern p);
    Object caseDataExcept(DataExceptPattern p);
    Object caseValue(ValuePattern p);
    Object caseText(TextPattern p);
    Object caseList(ListPattern p);
    Object caseRef(RefPattern p);
    Object caseAfter(AfterPattern p);
}

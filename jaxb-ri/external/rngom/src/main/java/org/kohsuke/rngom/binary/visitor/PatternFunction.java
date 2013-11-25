/*
 * Copyright (C) 2004-2011
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

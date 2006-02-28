package com.sun.tools.xjc.maven2;

import java.io.FilenameFilter;
import java.io.File;
import java.util.regex.Pattern;

/**
 * A FilenameFilter that uses a regular expression for filtering.  Matching
 * is not case senstive.
 *
 * See http://www.regular-expressions.info/java.html
 */
class PatternFilenameFilter implements FilenameFilter
{
    /**
     * The regular expression pattern to match.
     */
    private Pattern pattern;

    /**
     * Constructor.
     * @param expression the regular expression for the file filtering.
     */
    public PatternFilenameFilter (String expression)
    {
        pattern = Pattern.compile(expression);
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param   dir    the directory in which the file was found.
     * @param   name   the name of the file.
     * @return  <code>true</code> if and only if the name should be
     * included in the file list; <code>false</code> otherwise.
     */
    public boolean accept(File dir, String name)
    {
        // Strip path information, search for regex:
        return pattern.matcher(new File(name).getName()).matches();
    }
}

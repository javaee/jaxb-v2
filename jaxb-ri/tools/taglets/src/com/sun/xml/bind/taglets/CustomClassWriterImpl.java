package com.sun.xml.bind.taglets;

import com.sun.javadoc.ClassDoc;
import com.sun.tools.doclets.formats.html.ClassWriterImpl;
import com.sun.tools.doclets.internal.toolkit.ClassWriter;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;

/**
 * Generate the Class Information Page.
 * @see com.sun.javadoc.ClassDoc
 * @see java.util.Collections
 * @see java.util.List
 * @see java.util.ArrayList
 * @see java.util.HashMap
 *
 * @author Atul M Dambalkar
 * @author Robert Field
 */
public class CustomClassWriterImpl extends ClassWriterImpl
        implements ClassWriter {

    public CustomClassWriterImpl(ClassDoc classDoc, ClassDoc prevClass, ClassDoc nextClass, ClassTree classTree) throws Exception {
        super(classDoc, prevClass, nextClass, classTree);
    }

    @Override
    public void writeHeader(String header) {
        String pkgname = (classDoc.containingPackage() != null)?
            classDoc.containingPackage().name(): "";
        String clname = classDoc.name();
        printHtmlHeader(clname,
            configuration.metakeywords.getMetaKeywords(classDoc), true);
        navLinks(true);
        hr();
        println("<!-- ======== START OF CLASS DATA ======== -->");
//        h2();
//        if (pkgname.length() > 0) {
//            font("-1"); print(pkgname); fontEnd(); br();
//        }
//        print(header + getTypeParameterLinks(new LinkInfoImpl(
//            LinkInfoImpl.CONTEXT_CLASS_HEADER,
//            classDoc, false)));
//        h2End();
    }


    @Override
    protected void printSummaryDetailLinks() {
        // noop
    }
}






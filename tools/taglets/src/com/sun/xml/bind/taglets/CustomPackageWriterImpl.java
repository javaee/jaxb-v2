package com.sun.xml.bind.taglets;

import java.io.IOException;

import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.PackageWriterImpl;

/**
 * Class to generate file for each package contents in the right-hand
 * frame. This will list all the Class Kinds in the package. A click on any
 * class-kind will update the frame with the clicked class-kind page.
 *
 * @author Atul M Dambalkar
 */
public class CustomPackageWriterImpl extends PackageWriterImpl {

    public CustomPackageWriterImpl(ConfigurationImpl configuration, PackageDoc packageDoc, PackageDoc prev, PackageDoc next) throws IOException {
        super(configuration, packageDoc, prev, next);
    }

    /**
     * {@inheritDoc}
     */
    public void writePackageDescription() {
        if (packageDoc.inlineTags().length > 0) {
            anchor("package_description");
//            h2(configuration.getText("doclet.Package_Description", packageDoc.name()));
            p();
            printInlineComment(packageDoc);
            p();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writePackageHeader(String heading) {
        String pkgName = packageDoc.name();
        String[] metakeywords = { pkgName + " " + "package" };
        printHtmlHeader(pkgName, metakeywords,true);
        navLinks(true);
        hr();
        writeAnnotationInfo(packageDoc);
//        h2(configuration.getText("doclet.Package") + " " + heading);
//        if (packageDoc.inlineTags().length > 0 && ! configuration.nocomment) {
//            printSummaryComment(packageDoc);
//            p();
//            bold(configuration.getText("doclet.See"));
//            br();
//            printNbsps();
//            printHyperLink("", "package_description",
//                configuration.getText("doclet.Description"), true);
//            p();
//        }
    }

}

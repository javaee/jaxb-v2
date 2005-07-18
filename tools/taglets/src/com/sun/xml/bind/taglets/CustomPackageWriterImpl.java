package com.sun.xml.bind.taglets;

import java.io.IOException;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDocletWriter;
import com.sun.tools.doclets.internal.toolkit.PackageSummaryWriter;
import com.sun.tools.doclets.internal.toolkit.util.DirectoryManager;

/**
 * Class to generate file for each package contents in the right-hand
 * frame. This will list all the Class Kinds in the package. A click on any
 * class-kind will update the frame with the clicked class-kind page.
 *
 * @author Atul M Dambalkar
 */
public class CustomPackageWriterImpl extends HtmlDocletWriter
    implements PackageSummaryWriter {

    /**
     * The prev package name in the alpha-order list.
     */
    protected PackageDoc prev;

    /**
     * The next package name in the alpha-order list.
     */
    protected PackageDoc next;

    /**
     * The package being documented.
     */
    protected PackageDoc packageDoc;

    /**
     * The name of the output file.
     */
    private static final String OUTPUT_FILE_NAME = "package.html";

    /**
     * Constructor to construct PackageWriter object and to generate
     * "package-summary.html" file in the respective package directory.
     * For example for package "java.lang" this will generate file
     * "package-summary.html" file in the "java/lang" directory. It will also
     * create "java/lang" directory in the current or the destination directory
     * if it doesen't exist.
     *
     * @param configuration the configuration of the doclet.
     * @param packageDoc    PackageDoc under consideration.
     * @param prev          Previous package in the sorted array.
     * @param next            Next package in the sorted array.
     */
    public CustomPackageWriterImpl(ConfigurationImpl configuration,
        PackageDoc packageDoc, PackageDoc prev, PackageDoc next)
    throws IOException {
        super(configuration, DirectoryManager.getDirectoryPath(packageDoc), OUTPUT_FILE_NAME,
             DirectoryManager.getRelativePath(packageDoc.name()));
        this.prev = prev;
        this.next = next;
        this.packageDoc = packageDoc;
    }

    /**
     * Return the name of the output file.
     *
     * @return the name of the output file.
     */
    public String getOutputFileName() {
        return OUTPUT_FILE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public void writeSummaryHeader() {}

    /**
     * {@inheritDoc}
     */
    public void writeSummaryFooter() {}

    /**
     * {@inheritDoc}
     */
    public void writeClassesSummary(ClassDoc[] classes, String label) {
        // noop
    }
//    public void writeClassesSummary(ClassDoc[] classes, String label) {
//        if(classes.length > 0) {
//            Arrays.sort(classes);
//            tableIndexSummary();
//            boolean printedHeading = false;
//            for (ClassDoc c : classes) {
//                if (!printedHeading) {
//                    printFirstRow(label);
//                    printedHeading = true;
//                }
//                if (!Util.isCoreClass(c) ||
//                        !configuration.isGeneratedDoc(c)) {
//                    continue;
//                }
//                trBgcolorStyle("white", "TableRowColor");
//                summaryRow(15);
//                bold();
//                printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_PACKAGE,
//                        c, false));
//                boldEnd();
//                summaryRowEnd();
//                summaryRow(0);
//                if (Util.isDeprecated(c)) {
//                    boldText("doclet.Deprecated");
//                    if (c.tags("deprecated").length > 0) {
//                        space();
//                        printSummaryDeprecatedComment(c,
//                                c.tags("deprecated")[0]);
//                    }
//                } else {
//                    printSummaryComment(c);
//                }
//                summaryRowEnd();
//                trEnd();
//            }
//            tableEnd();
//            println("&nbsp;");
//            p();
//        }
//    }

    /**
     * Print the table heading for the class-listing.
     *
     * @param label Label for the Class kind listing.
     */
    protected void printFirstRow(String label) {
        tableHeaderStart("#CCCCFF");
        bold(label);
        tableHeaderEnd();
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
    public void writePackageTags() {
        printTags(packageDoc);
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

    /**
     * {@inheritDoc}
     */
    public void writePackageFooter() {
        hr();
        navLinks(false);
        printBottom();
        printBodyHtmlEnd();
    }

    /**
     * Print "Use" link for this pacakge in the navigation bar.
     */
    protected void navLinkClassUse() {
        navCellStart();
        printHyperLink("package-use.html", "", configuration.getText("doclet.navClassUse"),
                       true, "NavBarFont1");
        navCellEnd();
    }

    /**
     * Print "PREV PACKAGE" link in the navigation bar.
     */
    protected void navLinkPrevious() {
        if (prev == null) {
            printText("doclet.Prev_Package");
        } else {
            String path = DirectoryManager.getRelativePath(packageDoc.name(),
                                                           prev.name());
            printHyperLink(path + "package-summary.html", "",
                configuration.getText("doclet.Prev_Package"), true);
        }
    }

    /**
     * Print "NEXT PACKAGE" link in the navigation bar.
     */
    protected void navLinkNext() {
        if (next == null) {
            printText("doclet.Next_Package");
        } else {
            String path = DirectoryManager.getRelativePath(packageDoc.name(),
                                                           next.name());
            printHyperLink(path + "package-summary.html", "",
                configuration.getText("doclet.Next_Package"), true);
        }
    }

    /**
     * Print "Tree" link in the navigation bar. This will be link to the package
     * tree file.
     */
    protected void navLinkTree() {
        navCellStart();
        printHyperLink("package-tree.html", "", configuration.getText("doclet.Tree"),
                       true, "NavBarFont1");
        navCellEnd();
    }

    /**
     * Highlight "Package" in the navigation bar, as this is the package page.
     */
    protected void navLinkPackage() {
        navCellRevStart();
        fontStyle("NavBarFont1Rev");
        boldText("doclet.Package");
        fontEnd();
        navCellEnd();
    }
}

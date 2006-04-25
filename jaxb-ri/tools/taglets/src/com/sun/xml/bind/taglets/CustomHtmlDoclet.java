package com.sun.xml.bind.taglets;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDoclet;
import com.sun.tools.doclets.formats.html.PackageFrameWriter;
import com.sun.tools.doclets.formats.html.PackageIndexFrameWriter;
import com.sun.tools.doclets.internal.toolkit.builders.AbstractBuilder;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;
import com.sun.tools.doclets.internal.toolkit.util.DocletConstants;
import com.sun.tools.doclets.internal.toolkit.util.PackageListWriter;
import com.sun.tools.doclets.internal.toolkit.util.Util;

/**
 * @author Kohsuke Kawaguchi
 */
public class CustomHtmlDoclet extends HtmlDoclet {
    // those two methods are necessary to handle command-line options
    // see http://www.developer.com/java/other/article.php/3085991

    public static boolean validOptions(String[][] options,
                                   DocErrorReporter reporter) {
        return HtmlDoclet.validOptions(options, reporter);
    }

    public static LanguageVersion languageVersion() {
        return HtmlDoclet.languageVersion();
    }

    public static boolean start(RootDoc root) {
        CustomHtmlDoclet doclet = new CustomHtmlDoclet();
        // return doclet.start(doclet,root);

        doclet.configuration.root = root;
        try {
            doclet.startGeneration(root);
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }

    private void startGeneration(RootDoc root) throws Exception {
        if (root.classes().length == 0) {
            configuration.message.
                error("doclet.No_Public_Classes_To_Document");
            return;
        }
        configuration.setOptions();
        configuration.getDocletSpecificMsg().notice("doclet.build_version",
            configuration.getDocletSpecificBuildDate());

        configuration.tagletManager.addCustomTag(new SequenceDiagramTag());
        configuration.tagletManager.addCustomTag(new DotDiagramTag());
        configuration.tagletManager.addNewSimpleCustomTag("ArchitectureDocument","","X");

        ClassTree classtree = new ClassTree(configuration, configuration.nodeprecated);

        generateClassFiles(root, classtree);
        if (configuration.sourcepath != null && configuration.sourcepath.length() > 0) {
            StringTokenizer pathTokens = new StringTokenizer(configuration.sourcepath,
                String.valueOf(File.pathSeparatorChar));
            boolean first = true;
            while(pathTokens.hasMoreTokens()){
                Util.copyDocFiles(configuration,
                    pathTokens.nextToken() + File.separator,
                    DocletConstants.DOC_FILES_DIR_NAME, first);
                first = false;
            }
        }

        PackageListWriter.generate(configuration);
        generatePackageFiles(classtree);

        generateOtherFiles(root, classtree);
        configuration.tagletManager.printReport();
    }


    /*
        Most of the code is ripped from the base class. I just want to
        generate a different kind of the summary page here.
    */
    @Override
    protected void generatePackageFiles(ClassTree classtree) throws Exception {
        // super.generatePackageFiles(classtree);

        PackageDoc[] packages = configuration.packages;
        if (packages.length > 1) {
            PackageIndexFrameWriter.generate(configuration);
        }
        PackageDoc prev = null, next;
        for(int i = 0; i < packages.length; i++) {
            PackageFrameWriter.generate(configuration, packages[i]);

            next = (i + 1 < packages.length && packages[i+1].name().length() > 0) ?
                packages[i+1] : null;
            //If the next package is unnamed package, skip 2 ahead if possible
            next = (i + 2 < packages.length && next == null) ?
                packages[i+2]: next;

            AbstractBuilder packageSummaryBuilder;

            if(containsArchDocTag(packages[i])) {
                packageSummaryBuilder = CustomPackageSummaryBuilder.getInstance(configuration, packages[i],
                    new CustomPackageWriterImpl(ConfigurationImpl.getInstance(), packages[i], prev, next ));
            } else {
                packageSummaryBuilder = configuration.getBuilderFactory().getPackageSummaryBuilder(
                    packages[i], prev, next);
            }


            packageSummaryBuilder.build();

            prev = packages[i];
        }
    }

    /**
     * Checks if the package javadoc contains @ArchitectureDocument tag
     */
    private boolean containsArchDocTag(Doc doc) {
        for (Tag tag : doc.tags()) {
            if(tag.name().equals("@ArchitectureDocument"))
                return true;
        }
        return false;
    }

    /*
        Generate description-only HTML from class javadoc,
        instead of normal ones
    */
    @Override
    protected void generateClassFiles(ClassDoc[] arr, ClassTree classtree) {

        // split the array into two kinds --- one for normal classes
        // and the other for document classes
        List<ClassDoc> normals = new ArrayList<ClassDoc>();
        List<ClassDoc> archdocs = new ArrayList<ClassDoc>();

        for (ClassDoc cd : arr) {
            if(containsArchDocTag(cd))
                archdocs.add(cd);
            else
                normals.add(cd);
        }

        // generate normal javadoc for normal classes
        super.generateClassFiles(normals.toArray(new ClassDoc[0]),classtree);

        // for others...
        arr = archdocs.toArray(new ClassDoc[0]);
        Arrays.sort(arr);
        for(int i = 0; i < arr.length; i++) {
            if (!(configuration.isGeneratedDoc(arr[i]) && arr[i].isIncluded())) {
                continue;
            }
            ClassDoc prev = (i == 0)?
                null:
                arr[i-1];
            ClassDoc curr = arr[i];
            ClassDoc next = (i+1 == arr.length)?
                null:
                arr[i+1];
            try {
                AbstractBuilder classBuilder = CustomClassBuilder.getInstance(configuration, curr,
                                        new CustomClassWriterImpl(curr, prev, next,classtree));
                classBuilder.build();
            } catch (Exception e) {
                e.printStackTrace();
                throw new DocletAbortException();
            }
        }
    }
}

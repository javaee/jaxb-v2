/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.bind.taglets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.PackageSummaryWriter;
import com.sun.tools.doclets.internal.toolkit.builders.AbstractBuilder;
import com.sun.tools.doclets.internal.toolkit.util.DirectoryManager;
import com.sun.tools.doclets.internal.toolkit.util.DocletConstants;
import com.sun.tools.doclets.internal.toolkit.util.Util;

/**
 * Builds the summary for a given package.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @author Jamie Ho
 * @since 1.5
 */
public class CustomPackageSummaryBuilder extends AbstractBuilder {

	/**
	 * The root element of the package summary XML is {@value}.
	 */
	public static final String ROOT = "PackageDoc";

	/**
	 * The package being documented.
	 */
	private PackageDoc packageDoc;

	/**
	 * The doclet specific writer that will output the result.
	 */
	private PackageSummaryWriter packageWriter;

	private CustomPackageSummaryBuilder(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Construct a new PackageSummaryBuilder.
	 * @param configuration the current configuration of the doclet.
	 * @param pkg the package being documented.
	 * @param packageWriter the doclet specific writer that will output the
	 *        result.
	 *
	 * @return an instance of a PackageSummaryBuilder.
	 */
	public static CustomPackageSummaryBuilder getInstance(
		Configuration configuration,
		PackageDoc pkg,
		PackageSummaryWriter packageWriter) {
		CustomPackageSummaryBuilder builder =
			new CustomPackageSummaryBuilder(configuration);
		builder.packageDoc = pkg;
		builder.packageWriter = packageWriter;
		return builder;
	}

	/**
	 * {@inheritDoc}
	 */
	public void invokeMethod(
		String methodName,
		Class[] paramClasses,
		Object[] params)
		throws Exception {
		if (DEBUG) {
			configuration.root.printError(
				"DEBUG: " + this.getClass().getName() + "." + methodName);
		}
		Method method = this.getClass().getMethod(methodName, paramClasses);
		method.invoke(this, params);
	}

	/**
	 * Build the package summary.
	 */
	public void build() throws IOException {
		if (packageWriter == null) {
			//Doclet does not support this output.
			return;
		}

/*
    <PackageDoc>
        <PackageHeader/>
        <Summary>
            <SummaryHeader/>
            <InterfaceSummary/>
            <ClassSummary/>
            <EnumSummary/>
            <ExceptionSummary/>
            <ErrorSummary/>
            <AnnotationTypeSummary/>
            <SummaryFooter/>
        </Summary>
        <PackageDescription/>
        <PackageTags/>
        <PackageFooter/>
    </PackageDoc>
*/

		build(Arrays.asList(
            Arrays.asList("PackageDoc",
                "PackageHeader",
                "PackageDescription",
                Arrays.asList("Summary",
                    "SummaryHeader",
                    "InterfaceSummary",
                    "ClassSummary",
                    "EnumSummary",
                    "ExceptionSummary",
                    "ErrorSummary",
                    "AnnotationTypeSummary",
                    "SummaryFooter"),
                "PackageFooter")));
//                LayoutParser.getInstance(configuration).parseXML(ROOT));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return ROOT;
	}

	/**
	 * Build the package documentation.
	 */
	public void buildPackageDoc(List elements) throws Exception {
		build(elements);
		packageWriter.close();
		Util.copyDocFiles(
			configuration,
			Util.getPackageSourcePath(configuration, packageDoc),
			DirectoryManager.getDirectoryPath(packageDoc)
				+ File.separator
				+ DocletConstants.DOC_FILES_DIR_NAME,
			true);
	}

	/**
	 * Build the header of the summary.
	 */
	public void buildPackageHeader() {
		packageWriter.writePackageHeader(Util.getPackageName(packageDoc));
	}

	/**
	 * Build the description of the summary.
	 */
	public void buildPackageDescription() {
		if (configuration.nocomment) {
			return;
		}
		packageWriter.writePackageDescription();
	}

	/**
	 * Build the tags of the summary.
	 */
	public void buildPackageTags() {
		if (configuration.nocomment) {
			return;
		}
		packageWriter.writePackageTags();
	}

	/**
	 * Build the package summary.
	 */
	public void buildSummary(List elements) {
		build(elements);
	}

	/**
	 * Build the overall header.
	 */
	public void buildSummaryHeader() {
		packageWriter.writeSummaryHeader();
	}

	/**
	 * Build the overall footer.
	 */
	public void buildSummaryFooter() {
		packageWriter.writeSummaryFooter();
	}

	/**
	 * Build the summary for the classes in this package.
	 */
	public void buildClassSummary() {
		ClassDoc[] classes =
			packageDoc.isIncluded()
				? packageDoc.ordinaryClasses()
				: configuration.classDocCatalog.ordinaryClasses(
					Util.getPackageName(packageDoc));
		if (classes.length > 0) {
			packageWriter.writeClassesSummary(
				classes,
				configuration.getText("doclet.Class_Summary"));
		}
	}

	/**
	 * Build the summary for the interfaces in this package.
	 */
	public void buildInterfaceSummary() {
		ClassDoc[] interfaces =
			packageDoc.isIncluded()
				? packageDoc.interfaces()
				: configuration.classDocCatalog.interfaces(
					Util.getPackageName(packageDoc));
		if (interfaces.length > 0) {
			packageWriter.writeClassesSummary(
				interfaces,
				configuration.getText("doclet.Interface_Summary"));
		}
	}

	/**
	 * Build the summary for the enums in this package.
	 */
	public void buildAnnotationTypeSummary() {
		ClassDoc[] annotationTypes =
			packageDoc.isIncluded()
				? packageDoc.annotationTypes()
				: configuration.classDocCatalog.annotationTypes(
					Util.getPackageName(packageDoc));
		if (annotationTypes.length > 0) {
			packageWriter.writeClassesSummary(
				annotationTypes,
				configuration.getText("doclet.Annotation_Types_Summary"));
		}
	}

	/**
	 * Build the summary for the enums in this package.
	 */
	public void buildEnumSummary() {
		ClassDoc[] enums =
			packageDoc.isIncluded()
				? packageDoc.enums()
				: configuration.classDocCatalog.enums(
					Util.getPackageName(packageDoc));
		if (enums.length > 0) {
			packageWriter.writeClassesSummary(
				enums,
				configuration.getText("doclet.Enum_Summary"));
		}
	}

	/**
	 * Build the summary for the exceptions in this package.
	 */
	public void buildExceptionSummary() {
		ClassDoc[] exceptions =
			packageDoc.isIncluded()
				? packageDoc.exceptions()
				: configuration.classDocCatalog.exceptions(
					Util.getPackageName(packageDoc));
		if (exceptions.length > 0) {
			packageWriter.writeClassesSummary(
				exceptions,
				configuration.getText("doclet.Exception_Summary"));
		}
	}

	/**
	 * Build the summary for the errors in this package.
	 */
	public void buildErrorSummary() {
		ClassDoc[] errors =
			packageDoc.isIncluded()
				? packageDoc.errors()
				: configuration.classDocCatalog.errors(
					Util.getPackageName(packageDoc));
		if (errors.length > 0) {
			packageWriter.writeClassesSummary(
				errors,
				configuration.getText("doclet.Error_Summary"));
		}
	}

	/**
	 * Build the footer of the summary.
	 */
	public void buildPackageFooter() {
		packageWriter.writePackageFooter();
	}
}

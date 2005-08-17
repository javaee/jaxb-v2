/**
 * Implementation of the {@link com.sun.xml.bind.v2.model.core} package for XJC.
 *
 * <p>
 * This model is the recipes for the code generation.
 * It captures the essence of the JAXB-bound beans,
 * so that the actual Java code can be generated from this object model
 * mechanically without knowing anything about how the model was built.
 *
 * <p>
 * Most of the classes/interfaces in this package has one-to-one relationship
 * with the parameterized core model in the {@link com.sun.xml.bind.v2.model.core} package.
 * Refer to the core model for better documentation.
 *
 * <p>
 * The model for XJC also exposes a few additional information on top of the core model.
 * Those are defined in this package. This includes such information as:
 *
 * <dl>
 *  <dt>Source location information
 *  <dd>{@link Locator} object that can be used to tell where the model components
 *      are created from in terms of the source file. Useful for error reporting.
 *
 *  <dt>Source schema component
 *  <dd>{@link XSComponent} object from which the model components are created from.
 *      See {@link CCustomizable#getSchemaComponent()} for example.
 *
 *  <dt>Plugin customizations
 *  <dd>See {@link CCustomizable}.
 * </dl>
 */
package com.sun.tools.xjc.model;

import com.sun.xml.xsom.XSComponent;

import org.xml.sax.Locator;
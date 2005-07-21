/**
 * <h1>Schema to Java compiler</h1>.
 *
 * <p>
 * This module contains the code that implements the schema compiler 'XJC'.
 *
 *
 * <h2>XJC Architecture Diagram</h2>
 * {@DotDiagram
     digraph G {
         rankdir=TB;

         // data
         node [shape=box]; // style=filled,color=lightpink];
         schema -> "DOM forest" [label="DOMForest.parse()"];
         "DOM forest" -> "schema OM" [label="SOM specific parser"];
         "schema OM" -> model [label="language specific builder"];

         model -> codeModel [label="BeanGenerator.generate()"];
         codeModel -> "Java source files" [label="JCodeModel.build()"];
         model -> outline [label="BeanGenerator.generate()"];

         edge [style=dotted,label="associate"]
         outline -> codeModel;
         outline -> model;
       }
 * }
 *
 * <h2>Overview</h2>
 * <p>
 * XJC consists of the following major components.
 * <dl>
 *  <dt>{@link com.sun.tools.xjc.reader Schema reader}
 *  <dd>
 *   Schema readers read XML Schema documents (or DTD, RELAX NG, ...)
 *   and builds a model.
 *
 *  <dt>{@link com.sun.tools.xjc.model Model}
 *  <dd>
 *   Model represents the 'blueprint' of the code to be generated.
 *   Model talks in terms of higher level constructs like 'class' and 'property'
 *   without getting too much into the details of the Java source code.
 *
 *  <dt>{@link com.sun.tools.xjc.generator Code generator}
 *  <dd>
 *   Code generators use a model as an input and builds Java code AST
 *   into CodeModel. It also produces an {@link com.sun.tools.xjc.outline.Outline} which captures
 *   this work.
 *
 *  <dt>{@link com.sun.tools.xjc.outline.Outline Outline}
 *  <dd>
 *   Outline can be thought as a series of links between a model
 *   and CodeModel.
 * </dl>
 *
 * {@DotDiagram
 *   digraph G {
 *      rankdir = LR;
 *      schema -> reader -> model -> backend -> outline;
 *   }
 * }
 *
 * @ArchitectureDocument
 */
package com.sun.tools.xjc;

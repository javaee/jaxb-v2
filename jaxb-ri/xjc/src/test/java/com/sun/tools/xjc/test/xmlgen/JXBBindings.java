/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.tools.xjc.test.xmlgen;

import java.util.Map;

/**
 * JAXB bindings.
 */
public class JXBBindings extends XMLElementsContainer implements XMLElementsContainer.Element {
    /** Name spaces. */
    private final Map<String, String> xmlns;

    /** Version string. */
    private final String version;

    /**
     * Creates an instance of JAXB bindings.
     * @param version Version string.
     * @param xmlns Name spaces as {@code Map<String, String>} with {@code name} as a key and {@code URL} as a value.
     */
    public JXBBindings(final String version, final Map<String, String> xmlns) {
         this.version = version;
         this.xmlns = xmlns;
    }

    /**
     * Creates an instance of JAXB bindings.
     * @param version Version string.
     * @param xmlns Name spaces as {@code String[2]} arrays containing space name as element {@code [0]}
     *              and space URL as element {@code [1]}.
     */
    public JXBBindings(final String version, final String[]... xmlns) {
        this.version = version;
        this.xmlns = Utils.buildArgumentsMap(xmlns);
    }

    /**
     * Generate JAXB bindings code.
     * @param writer Target XSD document writer.
     */
    @Override
    public void generate(final XMLWritter writer) {
        writer.print("<jxb:bindings");
        writer.print(" xmlns:jxb=\"http://java.sun.com/xml/ns/jaxb\"");
        if (xmlns != null) {
            for (final String name : xmlns.keySet()) {
                final String url = xmlns.get(name);
                writer.print(" xmlns:", name, "=\"", url, "\"");
            }
        }
        if (version != null) {
            writer.print(" version=\"", version, "\"");
        }
        writer.println(">");
        writer.indentUp();
        super.generate(writer);
        writer.indentDown();
        writer.println("</jxb:bindings>");
    }

}

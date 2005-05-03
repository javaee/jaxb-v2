package com.sun.xml.bind.v2.runtime.reflect.opt;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;

import org.xml.sax.SAXException;

/**
 * Template {@link TransducedAccessor} for a byte field.
 *
 * <p>
 * All the TransducedAccessor_field are generated from <code>TransducedAccessor_field_B y t e</code>
 *
 * @author Kohsuke Kawaguchi
 *
 * @see TransducedAccessor#get
 */
public final class TransducedAccessor_method_Integer extends TransducedAccessor {
    public String print(Object o) {
        return DatatypeConverterImpl._printInt( ((Bean)o).get_int() );
    }

    public void parse(Object o, CharSequence lexical) {
        ((Bean)o).set_int(DatatypeConverterImpl._parseInt(lexical));
    }

    public boolean hasValue(Object o) {
        return true;
    }

    @Override
    public void writeLeafElement(Object o, Name tagName, String fieldName, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        w.leafElement(tagName, ((Bean)o).get_int(), fieldName );
    }
}

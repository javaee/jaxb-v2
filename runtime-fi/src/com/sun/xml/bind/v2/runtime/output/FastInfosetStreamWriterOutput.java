package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;

import org.xml.sax.SAXException;

/**
 * {@link XmlOutput} for {@link StAXDocumentSerializer}. 
 *
 * @author Paul Sandoz.
 */
public final class FastInfosetStreamWriterOutput extends XMLStreamWriterOutput {
    private final StAXDocumentSerializer fiout;

    /**
     * @param out
     *      this needs to be {@link StAXDocumentSerializer}.
     *      but the signature is weak so that we can call this easily from the runtime
     *      via reflection.
     */
    public FastInfosetStreamWriterOutput(StAXDocumentSerializer out) {
        super(out);
        this.fiout = out;
    }

    public void text(CharSequence value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        if(needsSeparatingWhitespace) {
            fiout.writeCharacters(" ");
        }

        /*
         * Check if the CharSequence is from a base64Binary data type
         */
        if (!(value instanceof Base64Data)) {
            // Write out characters
            fiout.writeCharacters(value.toString());
        } else {
            final Base64Data dataValue = (Base64Data)value;
            // Write out the octets using the base64 encoding algorithm
            fiout.writeOctets(dataValue.get(), 0, dataValue.getDataLen());
        }
    }
}

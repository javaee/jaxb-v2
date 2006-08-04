package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.fastinfoset.EncodingConstants;
import com.sun.xml.fastinfoset.stax.enhanced.EnhancedStAXDocumentSerializer;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import javax.xml.bind.JAXBContext;
import org.jvnet.fastinfoset.VocabularyApplicationData;
import org.xml.sax.SAXException;

/**
 * {@link XmlOutput} for {@link EnhancedStAXDocumentSerializer}.
 * <p>
 * This class is responsible for managing the indexing of elements, attributes
 * and local names that are known to JAXB by way of the JAXBContext (generated
 * from JAXB beans or schema). The pre-encoded UTF-8 representations of known
 * local names are also utilized.
 * <p>
 * The lookup of  elements, attributes and local names with respect to a context
 * is very efficient. It relies on an incrementing base line so that look up is
 * performed in O(1) time and only uses static memory. When the base line reaches
 * a point where integer overflow will occur the arrays and base line are reset
 * (such an event is rare and will have little impact on performance).
 * <p>
 * A weak map of JAXB contexts to optimized tables for attributes, elements and
 * local names is utilized and stored on the enhanced StAX serializer. Thus,
 * optimized serializing can work other multiple serializing of JAXB beans using
 * the same enhanced StAX serializer instance. This approach works best when JAXB
 * contexts are only created once per schema or JAXB beans (which is the recommended
 * practice as the creation JAXB contexts are expensive, they are thread safe and
 * can be reused).
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class EnhancedFastInfosetStreamWriterOutput extends XMLStreamWriterOutput {
    private final EnhancedStAXDocumentSerializer fiout;
    private final Encoded[] localNames;
    private final TablesPerJAXBContext tables;
    
    /**
     * Holder for the optimzed element, attribute and
     * local name tables.
     */
    final static class TablesPerJAXBContext {
        final int[] elementIndexes;
        final int[] attributeIndexes;
        final int[] localNameIndexes;
        
        int offset;
        
        TablesPerJAXBContext(JAXBContextImpl context) {
            elementIndexes = new int[context.getNumberOfElementNames()];
            attributeIndexes = new int[context.getNumberOfAttributeNames()];
            localNameIndexes = new int[context.getNumberOfLocalNames()];
            
            offset = 1;
        }

        /**
         * Clear the tables.
         */
        public void clearTables() {
            offset += elementIndexes.length + attributeIndexes.length;
            // If overflow
            if (offset < 0) {
                clear(elementIndexes);
                clear(attributeIndexes);
                clear(localNameIndexes);
                offset = 1;
            }
        }
        
        private void clear(int[] array) {
            for (int i = 0; i < array.length; i++) {
                array[i] = 0;
            }
        }
        
        /**
         * Reset the tables.
         * <p>
         * The indexes are preserved.
         */
        public void resetTables() {
            offset += elementIndexes.length + attributeIndexes.length;
            // If overflow
            if (offset < 0) {
                reset(elementIndexes);
                reset(attributeIndexes);
                reset(localNameIndexes);
                offset = 1;
            }
        }
        
        private void reset(int[] array) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] > offset) {
                    array[i] = array[i] - offset + 1;
                } else {
                    array[i] = 0;
                }
            }
        }
        
    }

    /**
     * Holder of JAXB contexts -> tables.
     * <p>
     * An instance will be registered with the 
     * {@link EnhancedStAXDocumentSerializer}.
     */
    final static class AppData implements VocabularyApplicationData {
        final Map<JAXBContext, TablesPerJAXBContext> contexts =
                new WeakHashMap<JAXBContext, TablesPerJAXBContext>();
        final Collection<TablesPerJAXBContext> collectionOfContexts = contexts.values();

        /**
         * Clear all the tables.
         */
        public void clear() {
            for(TablesPerJAXBContext c : collectionOfContexts)
                c.clearTables();
        }
    }
    
    public EnhancedFastInfosetStreamWriterOutput(EnhancedStAXDocumentSerializer out,
            JAXBContextImpl context) {
        super(out);
        
        this.fiout = out;
        this.localNames = context.getUTF8NameTable();
        
        final VocabularyApplicationData vocabAppData = fiout.getVocabularyApplicationData();
        AppData appData = null;
        if (vocabAppData == null || !(vocabAppData instanceof AppData)) {
            appData = new AppData();
            fiout.setVocabularyApplicationData(appData);
        } else {
            appData = (AppData)vocabAppData;
        }
        
        final TablesPerJAXBContext tablesPerContext = appData.contexts.get(context);
        if (tablesPerContext != null) {
            tables = tablesPerContext;
        } else {
            tables = new TablesPerJAXBContext(context);
            appData.contexts.put(context, tables);
        }
    }
    
    public void startDocument(XMLSerializer serializer, boolean fragment, 
            int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) 
            throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
        
        if (fragment)
            fiout.initiateElementFragment();
    }
    
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        super.endDocument(fragment);
    }
    
    public void beginStartTag(Name name) throws IOException {
        fiout.writeEnhancedTerminationAndMark();
        
        if (nsContext.getCurrent().count() == 0) {
            final int qNameIndex = tables.elementIndexes[name.qNameIndex] - tables.offset;
            if (qNameIndex >= 0) {
                fiout.writeEnhancedStartElementIndexed(EncodingConstants.ELEMENT, qNameIndex);
            } else {
                tables.elementIndexes[name.qNameIndex] = fiout.getNextElementIndex() + tables.offset;
                
                final int prefix = nsUriIndex2prefixIndex[name.nsUriIndex];
                writeLiteral(EncodingConstants.ELEMENT | EncodingConstants.ELEMENT_LITERAL_QNAME_FLAG,
                        name,
                        nsContext.getPrefix(prefix),
                        nsContext.getNamespaceURI(prefix));
            }
        } else {
            beginStartTagWithNamespaces(name);
        }
    }
    
    public void beginStartTagWithNamespaces(Name name) throws IOException {
        final NamespaceContextImpl.Element nse = nsContext.getCurrent();
        
        fiout.writeEnhancedStartNamespaces();
        for (int i = nse.count() - 1; i >= 0; i--) {
            final String uri = nse.getNsUri(i);
            if (uri.length() == 0 && nse.getBase() == 1)
                continue;   // no point in definint xmlns='' on the root
            fiout.writeEnhancedNamespace(nse.getPrefix(i), uri);
        }
        fiout.writeEnhancedEndNamespaces();
        
        final int qNameIndex = tables.elementIndexes[name.qNameIndex] - tables.offset;
        if (qNameIndex >= 0) {
            fiout.writeEnhancedStartElementIndexed(0, qNameIndex);
        } else {
            tables.elementIndexes[name.qNameIndex] = fiout.getNextElementIndex() + tables.offset;
            
            final int prefix = nsUriIndex2prefixIndex[name.nsUriIndex];
            writeLiteral(EncodingConstants.ELEMENT_LITERAL_QNAME_FLAG,
                    name,
                    nsContext.getPrefix(prefix),
                    nsContext.getNamespaceURI(prefix));
        }
    }
    
    public void attribute(Name name, String value) throws IOException {
        fiout.writeEnhancedStartAttributes();
        
        final int qNameIndex = tables.attributeIndexes[name.qNameIndex] - tables.offset;
        if (qNameIndex >= 0) {
            fiout.writeEnhancedAttributeIndexed(qNameIndex);
        } else {
            tables.attributeIndexes[name.qNameIndex] = fiout.getNextAttributeIndex() + tables.offset;
            
            final int namespaceURIId = name.nsUriIndex;
            if (namespaceURIId == -1) {
                writeLiteral(EncodingConstants.ATTRIBUTE_LITERAL_QNAME_FLAG,
                        name,
                        "",
                        "");
            } else {
                final int prefix = nsUriIndex2prefixIndex[namespaceURIId];
                writeLiteral(EncodingConstants.ATTRIBUTE_LITERAL_QNAME_FLAG,
                        name,
                        nsContext.getPrefix(prefix),
                        nsContext.getNamespaceURI(prefix));
            }
        }
        
        fiout.writeEnhancedAttributeValue(value);
    }
    
    private void writeLiteral(int type, Name name, String prefix, String namespaceURI) throws IOException {
        final int localNameIndex = tables.localNameIndexes[name.localNameIndex] - tables.offset;
        
        if (localNameIndex < 0) {
            tables.localNameIndexes[name.localNameIndex] = fiout.getNextLocalNameIndex() + tables.offset;
            
            fiout.writeEnhancedStartNameLiteral(
                    type,
                    prefix,
                    localNames[name.localNameIndex].buf,
                    namespaceURI);
        } else {
            fiout.writeEnhancedStartNameLiteral(
                    type,
                    prefix,
                    localNameIndex,
                    namespaceURI);
        }
    }
    
    public void endStartTag() throws IOException {
        fiout.writeEnhancedEndStartElement();
    }
    
    public void endTag(Name name) throws IOException {
        fiout.writeEnhancedEndElement();
    }
    
    public void endTag(int prefix, String localName) throws IOException {
        fiout.writeEnhancedEndElement();
    }
    
    
    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException {
        if (needsSeparatingWhitespace)
            fiout.writeEnhancedText(" ");
        
        /*
         * Check if the CharSequence is from a base64Binary data type
         */
        if (!(value instanceof Base64Data)) {
            final int len = value.length();
            if(len <buf.length) {
                value.writeTo(buf, 0);
                fiout.writeEnhancedText(buf, len);
            } else {
                fiout.writeEnhancedText(value.toString());
            }
        } else {
            final Base64Data dataValue = (Base64Data)value;
            // Write out the octets using the base64 encoding algorithm
            fiout.writeEnhancedOctets(dataValue.get(), dataValue.getDataLen());
        }
    }
    
    
    public void text(String value, boolean needsSeparatingWhitespace) throws IOException {
        if (needsSeparatingWhitespace)
            fiout.writeEnhancedText(" ");
        
        fiout.writeEnhancedText(value);
    }
    
    
    public void beginStartTag(int prefix, String localName) throws IOException {
        fiout.writeEnhancedTerminationAndMark();
        
        int type = EncodingConstants.ELEMENT;
        if (nsContext.getCurrent().count() > 0) {
            final NamespaceContextImpl.Element nse = nsContext.getCurrent();
            
            fiout.writeEnhancedStartNamespaces();
            for (int i = nse.count() - 1; i >= 0; i--) {
                final String uri = nse.getNsUri(i);
                if (uri.length() == 0 && nse.getBase() == 1)
                    continue;   // no point in definint xmlns='' on the root
                fiout.writeEnhancedNamespace(nse.getPrefix(i), uri);
            }
            fiout.writeEnhancedEndNamespaces();
            
            type= 0;
        }
        
        fiout.writeEnhancedStartElement(
                type,
                nsContext.getPrefix(prefix),
                localName,
                nsContext.getNamespaceURI(prefix));
    }
    
    public void attribute(int prefix, String localName, String value) throws IOException {
        fiout.writeEnhancedStartAttributes();
        
        if (prefix == -1)
            fiout.writeEnhancedAttribute("", "", localName);
        else
            fiout.writeEnhancedAttribute(
                    nsContext.getPrefix(prefix),
                    nsContext.getNamespaceURI(prefix),
                    localName);
        
        fiout.writeEnhancedAttributeValue(value);
    }
}
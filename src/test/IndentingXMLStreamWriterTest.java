import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

/**
 * @author Kohsuke Kawaguchi
 */
public class IndentingXMLStreamWriterTest {
    public static void main(String[] args) throws Exception {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter w = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(System.out));

        w.writeStartDocument();
        w.writeStartElement("foo");
        w.writeEmptyElement("bar");
        w.writeStartElement("x");
        w.writeCharacters("body");
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();

        w.close();
    }
}

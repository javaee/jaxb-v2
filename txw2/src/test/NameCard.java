
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.TypedXmlWriter;

import javax.xml.namespace.QName;

/**
 * @author Kohsuke Kawaguchi
 */
@XmlElement(ns="beep",value="nameCard")
public interface NameCard extends TypedXmlWriter {
    void name( String name );
    void address( String address );
    @XmlElement("aaa")
    void test(QName v);

    @XmlAttribute
    void test2(QName n);

    @XmlAttribute("iid")
    void id( int id );
}

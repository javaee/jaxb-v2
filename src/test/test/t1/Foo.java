package test.t1;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.annotation.XmlAttribute;

/**
 * @author Kohsuke Kawaguchi
 */
@XmlElement(value="foo")
public interface Foo extends TypedXmlWriter {
    Foo foo();
    @XmlAttribute
    void hello(int min);
}

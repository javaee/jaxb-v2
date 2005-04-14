
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

/**
 * @author Kohsuke Kawaguchi
 */
public interface NameCards extends TypedXmlWriter {
    @XmlElement(ns="beep",value="name-card")
    NameCard nameCard();
}

import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.bind.CycleRecoverable;

@XmlRootElement
public class Person implements CycleRecoverable {

    public int id;

    public String name;

    public Person parent;

    // this method is called by JAXB when a cycle is detected
    public Person onCycleDetected(Context context) {
        // when a cycle is detected, let's just write out an ID
        Person replacement = new Person();
        replacement.id = this.id;
        return replacement;
    }
}

package com.sun.xml.bind;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * JAXB-bound objects can implement this interface
 * to receive additional event callbacks from the runtime.
 *
 * <p>
 * This allows the object to perform additional processing
 * at certain key point in the unmarshalling/marshalling operation.
 *
 * <p>
 * Note that this interface is recognized only by the JAXB RI.
 * If your application relies on the events to be generated,
 * you have to make sure that you always use the JAXB RI at the runtime.
 *
 * TODO: think about a better name.
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB RI 2.0
 */
/*
    Internally, the lifecycle method invocation is handled by SAXUnmarshallerImpl
*/
public interface ObjectLifeCycle {
    /**
     * Called on the start of the unmarshalling.
     *
     * <p>
     * This event is fired before the unmarshalling of this object
     * begins. For the normal unmarshalling operation, this is
     * usually immediately after the object is created.
     *
     * @param unmarshaller
     *      the unmarshaller in charge of this unmarshalling operation.
     * @param parent
     *      if this object is a child object of another object,
     *      this parameter points to the parent object to which
     *      this object will be set.
     *
     *      this parameter is null when this object is the root object.
     */
    void beforeUnmarshalling( Unmarshaller unmarshaller, Object parent );

    /**
     * Called on the completion of the unmarshalling.
     *
     * <p>
     * This event is fired after all the properties (except IDREF)
     * are unmarshalled, but before this object is set to the parent
     * object.
     *
     * <p>
     * This event does not mean that the unmarshalling of the whole
     * XML document has finished; it just means that the unmarshalling
     * of this object (plus all its descendants except IDREF) has finished.
     *
     * @param unmarshaller
     *      the unmarshaller in charge of this unmarshalling operation.
     * @param parent
     *      if this object is a child object of another object,
     *      this parameter points to the parent object to which
     *      this object will be set.
     *
     *      this parameter is null when this object is the root object.
     */
    void afterUnmarshalling( Unmarshaller unmarshaller, Object parent );

    /**
     * Called on the start of the marshalling.
     *
     * <p>
     * This event is fired before the marshalling of this object starts.
     *
     * @param marshaller
     *      the marshaller in charge of this marshalling operation.
     */
    void beforeMarshalling( Marshaller marshaller );

    /**
     * Called on the end of the marshalling.
     *
     * <p>
     * This event is fired after the marshalling of this object
     * (and all its descendants) has finished.
     *
     * @param marshaller
     *      the marshaller in charge of this marshalling operation.
     */
    void afterMarshalling( Marshaller marshaller );
}

package com.sun.xml.bind.unmarshaller;

import org.xml.sax.SAXException;

/**
 * Runs by UnmarshallingContext after all the parsing is done.
 *
 * Primarily used to resolve forward IDREFs, but it can run any action.
 *
 * @author Kohsuke Kawaguchi
 */
interface Patcher {
    /**
     * Runs an post-action.
     *
     * @throws SAXException
     *      if an error is found during the action, it should be reporeted to the context.
     *      The context may then throw a {@link SAXException} to abort the processing,
     *      and that's when you can throw a {@link SAXException}.
     */
    void run() throws SAXException;
}

package com.sun.xml.bind.v2.runtime.unmarshaller;

import static com.sun.xml.bind.v2.runtime.unmarshaller.Messages.ERRORS_LIMIT_EXCEEDED;
import static com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerProperties.ENABLE_ERROR_REPORT_LIMIT;
import static com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext.DEFAULT_ERROR_COUNTER;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.sun.xml.bind.utils.Person;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;

import junit.framework.TestCase;


public class LimitErrorTest extends TestCase {

    private static final String EXPECTED_ERROR_MESSAGE = "unexpected element (uri:\"\", local:\"unexpectedChild\"). Expected elements are (none)";
    private static final String GENERIC_ERROR_MESSAGE = ERRORS_LIMIT_EXCEEDED.format();
    private final ValidationEventCollector validationCollector = new JAXB2ValidationEventCollector();
    private static final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                                        "<person>" +
                                            "<unexpectedChild></unexpectedChild>" +
                                        "</person>";

    private Unmarshaller unmarshaller = null;

    protected void setUp() throws Exception {
        unmarshaller = JAXBContext.newInstance(Person.class).createUnmarshaller();
        unmarshaller.setEventHandler(validationCollector);
    }

    public void testDisableErrorReportLimit() throws Exception
    {
        unmarshaller.setProperty(ENABLE_ERROR_REPORT_LIMIT, false);

        for (int i = 0; i < DEFAULT_ERROR_COUNTER + 1; i ++) {
            try {
                unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            }
            catch (JAXBException e) {
                assertThat(e.getMessage(), is(EXPECTED_ERROR_MESSAGE));
            }
        }
    }

    public void testEnableErrorReportLimit() {

        for (int i = 0; i < DEFAULT_ERROR_COUNTER; i ++) {
            try {
                unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            }
            catch (JAXBException e) {
                assertThat(e.getMessage(), is(EXPECTED_ERROR_MESSAGE));
            }
        }

        try {
            unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
        }
        catch (JAXBException e) {
            assertThat(e.getMessage(), is(GENERIC_ERROR_MESSAGE));
        }
    }

    public void testCustomErrorReportLimit() {
        int customErrorReportLimit = 5;
        for (int i = 0; i < customErrorReportLimit; i ++) {
            try {
                unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
            }
            catch (JAXBException e) {
                assertThat(e.getMessage(), is(EXPECTED_ERROR_MESSAGE));
            }
        }

        try {
            unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
        }
        catch (JAXBException e) {
            assertThat(e.getMessage(), is(GENERIC_ERROR_MESSAGE));
        }
    }


    private class JAXB2ValidationEventCollector extends ValidationEventCollector {
        @Override
        public boolean handleEvent(ValidationEvent event) {
            super.handleEvent(event);
            return false;
        }
    }

}
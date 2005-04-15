package com.sun.tools.xjc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.IntrospectionHelper;

/**
 * Captures the properties and then delegate to XJC1 or XJC2 by looking at
 * the source attribute.
 *
 * @author Bhakti Mehta
 */
public class XJCTask extends Task implements DynamicConfigurator {

    private String source = "2.0";

    private final AntElement root = new AntElement("root");

    public void setDynamicAttribute(String name, String value) throws BuildException {
        root.setDynamicAttribute(name,value);
    }

    public Object createDynamicElement(String name) throws BuildException {
        return root.createDynamicElement(name);
    }

    /**
     * The version of the compiler to run
     */
    public void setSource(String version) {
        if(version.equals("1.0") || version.equals("2.0")) {
            this.source = version;
            return;
        }
        throw new BuildException("Illegal version "+version);
    }

    public void execute() throws BuildException {
        //Leave XJC2 in the publicly visible place
        // and then isolate XJC1 in a child class loader,
        // then use a MaskingClassLoader
        // so that the XJC2 classes in the parent class loader
        //  won't interfere with loading XJC1 classes in a child class loader
        ClassLoader cl;
        Class driver;
        try {
            if (source.equals("2.0")) {
                cl = new ParallelWorldClassLoader(XJCTask.class.getClassLoader(),source);
                driver = cl.loadClass("com.sun.tools.xjc.XJC2Task");
            } else {
                cl = new ParallelWorldClassLoader(MaskingClassLoader.class.getClassLoader(),source);
                driver = cl.loadClass("com.sun.tools.xjc.XJCTask");
            }
            Task t = (Task)driver.newInstance();
            t.setProject(getProject());
            t.setTaskName(getTaskName());
            root.configure(t);
            t.execute();
        } catch (UnsupportedClassVersionError e) {
            throw new BuildException("XJC requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
        } catch (ClassNotFoundException e) {
            throw new BuildException(e);
        } catch (InstantiationException e) {
            throw new BuildException(e);
        } catch (IllegalAccessException e) {
            throw new BuildException(e);
        }

    }

    /**
     * Captures the elements and attributes.
     */
    private class AntElement implements DynamicConfigurator {
        private final String name;

        private final Map/*<String,String>*/ attributes = new HashMap();

        private final List/*<AntElement>*/ elements = new ArrayList();

        public AntElement(String name) {
            this.name = name;
        }

        public void setDynamicAttribute(String name, String value) throws BuildException {
            attributes.put(name,value);
        }

        public Object createDynamicElement(String name) throws BuildException {
            AntElement e = new AntElement(name);
            elements.add(e);
            return e;
        }

        /**
         * Copies the properties into the Ant task.
         */
        public void configure(Object antObject) {
            IntrospectionHelper ih = IntrospectionHelper.getHelper(antObject.getClass());

            // set attributes first
            for( Iterator itr=attributes.entrySet().iterator(); itr.hasNext(); ) {
                Map.Entry att = (Map.Entry)itr.next();
                ih.setAttribute(getProject(), antObject, (String)att.getKey(),(String)att.getValue());
            }

            // then nested elements
            for (Iterator itr = elements.iterator(); itr.hasNext();) {
                AntElement e = (AntElement) itr.next();
                Object child = ih.createElement(getProject(), antObject, e.name);
                e.configure(child);
                ih.storeElement(getProject(), antObject, child, e.name);
            }
        }
    }
}


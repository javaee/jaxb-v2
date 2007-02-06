package com.sun.tools.xjc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import com.sun.istack.tools.ProtectedTask;
import com.sun.istack.tools.ParallelWorldClassLoader;
import com.sun.istack.tools.MaskingClassLoader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

/**
 * Captures the properties and then delegate to XJC1 or XJC2 by looking at
 * the source attribute.
 *
 * @author Bhakti Mehta
 */
public class XJCTask extends ProtectedTask {

    private String source = "2.0";

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


    protected ClassLoader createClassLoader() throws ClassNotFoundException, IOException {
        return ClassLoaderBuilder.createProtectiveClassLoader(XJCTask.class.getClassLoader(),source);
    }

    protected String getCoreClassName() {
        if (source.equals("2.0"))
            return "com.sun.tools.xjc.XJC2Task";
        else
            return "com.sun.tools.xjc.XJCTask";
    }
}


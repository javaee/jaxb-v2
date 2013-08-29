package org.kohsuke.rngom.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Localizes messages.
 */
public class Localizer {
    private final Class cls;
    private ResourceBundle bundle;
    /**
     * If non-null, any resources that weren't found in this localizer
     * will be delegated to the parent.
     */
    private final Localizer parent;
    
    public Localizer(Class cls) {
        this(null,cls);
    }
    
    public Localizer(Localizer parent, Class cls) {
        this.parent = parent;
        this.cls = cls;
    }

    private String getString(String key) {
        try {
            return getBundle().getString(key);
        } catch( MissingResourceException e ) {
            // delegation
            if(parent!=null)
                return parent.getString(key);
            else
                throw e;
        }
    }
    
    public String message(String key) {
        return MessageFormat.format(getString(key), new Object[]{});
    }

    public String message(String key, Object arg) {
        return MessageFormat.format(getString(key),
            new Object[]{arg});
    }

    public String message(String key, Object arg1, Object arg2) {
        return MessageFormat.format(getString(key), new Object[]{
                arg1, arg2});
    }

    public String message(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }

    private ResourceBundle getBundle() {
        if (bundle == null) {
            String s = cls.getName();
            int i = s.lastIndexOf('.');
            if (i > 0)
                s = s.substring(0, i + 1);
            else
                s = "";
            bundle = ResourceBundle.getBundle(s + "Messages");
        }
        return bundle;
    }
}
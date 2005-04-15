package com.sun.tools.xjc;

import java.io.PrintStream;

import com.sun.tools.xjc.api.ErrorListener;

/**
 * Call-back interface that can be implemented by the caller of {@link Driver}
 * to receive output from XJC.
 *
 * <p>
 * Most of the messages XJC produce once the real work starts is structured
 * as (message,source). Those outputs will be reported to various methods on
 * {@link ErrorListener}, which is inherited by this interface.
 *
 * <p>
 * The other messages (such as the usage screen when there was an error in
 * the command line option) will go to the {@link #message(String)} method.
 *
 * @author Kohsuke Kawaguchi
 * @since JAXB 2.0 EA
 */
public interface XJCListener extends ErrorListener {
    /**
     * Other miscellenous messages that do not have structures
     * will be reported through this method.
     *
     * This method is used like {@link PrintStream#println(String)}.
     * The callee is expected to add '\n'. 
     */
    void message(String msg);
}

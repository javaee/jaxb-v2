/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.webapp;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Servlet that displays the progress message while the task is
 * running in a separate thread.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class LongProcessServlet extends HttpServletEx {

    protected void run() throws ServletException, IOException {
        Thread task = getTask();
        if( task==null ) {
            // start the processing
            task = createTask();
            setTaskThread(task);
            task.start();
            
            try { // wait for the completion some time to save a round trip if the task ends quickly.
                task.join(3000);
            } catch( InterruptedException e ) {
                ;   // ignore
            }
        }
        
        // check if the task is still running
        if(task.isAlive()) {
            request.setAttribute("title", getProgressTitle() );
            request.setAttribute("message", getProgressMessage() );
            request.setAttribute("comebackto",
                n(request.getContextPath())+
                n(request.getServletPath())+
                n(request.getPathInfo()) );
            // the task is still working
            forward("/checkback.jsp");
        } else
            // it is finished
            renderResult(getTask());
    }
    
    private String n(String s) {
        if(s==null) return "";
        else        return s;
    }
    
    protected abstract String getProgressTitle();
    protected abstract String getProgressMessage();

    /**
     * Creates a task that does the actual processing.
     */
    protected abstract Thread createTask() throws ServletException, IOException;
    
    /**
     * The task is completed. Render the result.
     */
    protected abstract void renderResult( Thread task ) throws ServletException, IOException; 
    
    
    /**
     * Gets the task associated with the current session or null.
     */
    protected final Thread getTask() {
        return (Thread)request.getSession().getAttribute(this.getClass().getName());
    }

    private void setTaskThread( Thread t ) {
        request.getSession().setAttribute(this.getClass().getName(),t);
    }
}

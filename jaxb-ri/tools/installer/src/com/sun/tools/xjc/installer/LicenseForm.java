/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * LicenseForm.java
 *
 * Created on 2003/11/06, 21:17
 */
package com.sun.tools.xjc.installer;

import java.awt.Adjustable;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * License screen.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class LicenseForm extends JDialog {
    
    private boolean accepted = false;
    
    public LicenseForm( Reader text ) throws IOException {
        super((JFrame)null,"License Agreement",true);
        final JScrollPane scrollPane = new JScrollPane();
        JTextArea licenseTextArea = new JTextArea();
        JPanel buttonPanel = new JPanel();
        final JButton acceptButton = new JButton();
        final JButton cancelButton = new JButton();
        
        
        {// load the license text
            BufferedReader reader = new BufferedReader(text);
            String line;
            StringBuffer buf = new StringBuffer();
            while((line=reader.readLine())!=null) {
                buf.append(line);
                buf.append('\n');
            }
            licenseTextArea.setText(buf.toString());
            licenseTextArea.setLineWrap(true);
        }
        
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(scrollPane);
        scrollPane.setViewportView(licenseTextArea);
        
        licenseTextArea.setEditable(false);
        
        getContentPane().add(buttonPanel);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        buttonPanel.add(acceptButton);
        acceptButton.setText("Accept");
        acceptButton.setEnabled(false);

        buttonPanel.add(cancelButton);
        cancelButton.setText("Decline");
        
        acceptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitDialog(true);
             }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitDialog(false);
            }
        });
        
        pack();
        
        // don't enable the yes button until the scroll bar has been dragged
        // to the bottom or the window was enlarged enough to make the scroll
        // bar disappear
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.addAdjustmentListener( new AdjustmentListener() {
            Adjustable a;
            public void adjustmentValueChanged(AdjustmentEvent e) {
                a = e.getAdjustable();
                if( a.getValue() + a.getVisibleAmount() >= a.getMaximum() ) 
                    acceptButton.setEnabled(true);
            }
        });
        
        java.awt.Dimension screenSize = 
            java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new java.awt.Dimension(550, 450));
        setLocation((screenSize.width-550)/2,(screenSize.height-450)/2);
    }

    private void exitDialog(boolean accepted) {
        this.accepted = accepted;
        dispose();
    }
    
    /**
     * Returns true if the license is accepted by the user.
     */
    public boolean isAccepted() {
        return accepted;
    }
}

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

package com.sun.tools.xjc.servlet;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Reports the usage to us.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Dialer extends Thread {
    
    private final String remoteHost;
    private final Compiler compiler;
    
    public Dialer( Compiler compiler, String remoteHost ) {
        this.compiler = compiler;
        this.remoteHost = remoteHost;
    }
    
    /**
     * Phones home.
     */
    public void run() {
        try {
            System.out.println("JAXB on the web phones home...");
            Properties props = new Properties();
            props.put("mail.smtp.host", Mode.mailServer); 
            Session session = Session.getDefaultInstance(props);
        
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("kohsuke.kawaguchi@sun.com","JAXB on the web"));
        
            msg.setRecipient(Message.RecipientType.TO,
                new InternetAddress(Mode.homeAddress));
            
            Multipart mp = new MimeMultipart();
            
            {// main message
                MimeBodyPart body = new MimeBodyPart();
                body.setText(
                    "Remote IP: "+remoteHost+"\n" );
                mp.addBodyPart(body);
            }
            
            {// status message part
                MimeBodyPart status = new MimeBodyPart();
                status.setText( compiler.getStatusMessages() );
                mp.addBodyPart(status);
            }
            
            // TODO: add other parts
            
            msg.setContent(mp);
            msg.setSentDate(new Date());
            
            msg.setSubject("JAXBotW: phone home");
            
            Transport.send(msg);    // send the message 
        } catch (MessagingException e) {
            e.printStackTrace();
            // TODO: can we do something? maybe log an error?
        } catch( UnsupportedEncodingException e ) {
            e.printStackTrace();
            // can't happen
        }
    }
}

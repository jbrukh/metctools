package org.kohera.metctools.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class Emailer {
	
	private Properties config;
	private final Authenticator auth;
		
	public Emailer( final Properties config ) {
		this.config = config;
		this.config.setProperty("mail.transport.protocol","smtp");
		auth = new Authenticator() {
			  public PasswordAuthentication getPasswordAuthentication() {
			    return new PasswordAuthentication(config.getProperty("user"), 
			    		config.getProperty("password"));
			  }
			};
	}	
	
	public void sendMail( String[] recipients, String subject, String message ) throws MessagingException {
		/* get the session and transport */	
		Session mailSession = Session.getDefaultInstance(config, auth);
		Transport transport = mailSession.getTransport();
		
		/* make the message */
		MimeMessage mimeMessage = new MimeMessage(mailSession);
		mimeMessage.setSubject(subject);
		mimeMessage.setContent(message, "text/plain");
		mimeMessage.setSender(new InternetAddress(config.getProperty("mail.from")));
	
		for ( String recipient : recipients ) {
			mimeMessage.addRecipient(Message.RecipientType.TO, 
					new InternetAddress(recipient));
		}
		
		/* send it */
		transport.connect();
		transport.sendMessage(mimeMessage, 
				mimeMessage.getRecipients(Message.RecipientType.TO));
		transport.close();
	}

}
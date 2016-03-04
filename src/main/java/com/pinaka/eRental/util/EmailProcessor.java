package com.pinaka.eRental.util;

import java.util.Properties;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.ejb.Stateless;



@Stateless
public class EmailProcessor {

	@Inject Logger log;
	
	public void sendEmail(String toAddress, String subject, String content) {
		String host = PropertyManager.getProp("host");
		log.info(PropertyManager.getProp("port"));
		int port = Integer.parseInt(PropertyManager.getProp("port"));
		final String username = PropertyManager.getProp("username");
		final String password = PropertyManager.getProp("password");
 
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
 
		Session session = Session.getInstance(props,new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username,password);
			}
		});
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(PropertyManager.getProp("fromaddress")));
			//TODO - implement multiple addresses
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(toAddress));
			message.setSubject(subject);
			message.setText(content);
 
			Transport transport = session.getTransport("smtp");
			transport.connect(host, port, username, password);
 
			Transport.send(message);
 
			System.out.println("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}		
	}

}

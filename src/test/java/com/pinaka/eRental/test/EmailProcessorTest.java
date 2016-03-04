package com.pinaka.eRental.test;

import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.Nestable;
import org.apache.commons.lang.exception.NestableDelegate;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.pinaka.eRental.util.EmailProcessor;
import com.pinaka.eRental.util.PropertyManager;
import com.pinaka.eRental.util.Resources;

@RunWith(Arquillian.class)
public class EmailProcessorTest {
	   @Deployment
	   public static Archive<?> createTestArchive() {
	      return ShrinkWrap.create(WebArchive.class, "test.war")
	            .addClasses(EmailProcessor.class, PropertyManager.class, StringEscapeUtils.class, NestableRuntimeException.class, Nestable.class, NestableDelegate.class, Resources.class)
	            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
	            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
	            .addAsResource("ExceptionMessages.properties");
	   }

	@Inject
	EmailProcessor ep;
	@Inject
	Logger log;
	@Inject
	PropertyManager pm;
	
	
	private static ResourceBundle rb =  ResourceBundle.getBundle("ExceptionMessages");
	
	
	@Test
	public void sendEmailTest() {
		
		log.info("Subject"+PropertyManager.getProp("rentalRequestSubject"));
		
		log.info("realm"+PropertyManager.getProp("realm"));
		
				
		ep.sendEmail("veer.muchandi@gmail.com", 
				PropertyManager.getMessage("RentedSubject",1 , "myitem")
				, PropertyManager.getMessage("RentedEmailContent", "Borrower", 1, "myitem", new Date(), new Date(), "name", "address \n line2 \n nocity ns", "111-111-1111", "222-222-2222"));
		//RentedEmailContent=Dear {0}, \n \n This is the confirmation that the item {1} {2} has been rented to you with: \n\n \t Rental Start Date: {3} and \n \t Rental End Date: {4}. \n\n The following are the contact details to pickup: \n Name: {5}, \n  Address: {6}, \n Phone:  {7} , {8}. \n \n Thank you  \n Administrator
	}

}

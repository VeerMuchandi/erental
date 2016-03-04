package com.pinaka.eRental.test;

import static org.junit.Assert.*;

import java.io.File;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.transaction.UserTransaction;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.pinaka.UserManagement.Address;
import com.pinaka.UserManagement.Phone;
import com.pinaka.UserManagement.PhoneType;
import com.pinaka.UserManagement.Role;
import com.pinaka.UserManagement.User;
import com.pinaka.UserManagement.UserManager;
import com.pinaka.UserManagement.UserManagerBean;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.exception.InvalidBusinessStateException;
import com.pinaka.eRental.model.PenaltyTerm;
import com.pinaka.eRental.model.RejectionReasonType;
import com.pinaka.eRental.model.RentalCost;
import com.pinaka.eRental.model.RentalInstance;
import com.pinaka.eRental.model.RentalItem;
import com.pinaka.eRental.model.RentalItem_;
import com.pinaka.eRental.model.RentalLocation;
import com.pinaka.eRental.model.RentalPeriodType;
import com.pinaka.eRental.model.RentalStatusType;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.model.RentalUser_;
import com.pinaka.eRental.service.RentalItemManager;
import com.pinaka.eRental.service.RentalItemManagerBean;
import com.pinaka.eRental.service.RentalUserManager;
import com.pinaka.eRental.service.RentalUserManagerBean;
import com.pinaka.eRental.serviceFacade.RentalManagerService;
import com.pinaka.eRental.serviceFacade.RentalManagerServiceBean;
import com.pinaka.eRental.serviceFacade.RentalUserMgmtService;
import com.pinaka.eRental.serviceFacade.RentalUserMgmtServiceBean;
import com.pinaka.eRental.util.EmailProcessor;
import com.pinaka.eRental.util.PropertyManager;
import com.pinaka.eRental.util.Resources;
import com.pinaka.testutil.JBossLoginContextFactory;

/**
 * @filename RentalManagerServiceTest.java
 * @author Veer Muchandi
 * @created Mar 27, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
@RunWith(Arquillian.class)
public class RentalManagerServiceTest {
	
	@Deployment
	   public static Archive<?> createTestArchive() {
		  final String WEBAPP_SRC = "src/main/webapp";
	      return ShrinkWrap.create(WebArchive.class, "test.war")
	            .addClasses( 
	            		EmailProcessor.class, 
	            		PropertyManager.class, 
	            		RejectionReasonType.class, 
	            		RentalStatusType.class, 
	            		RentalItem.class, 
	            		RentalItem_.class,
	            		RentalCost.class,
	            		RentalPeriodType.class, 
	            		Address.class, 
	            		PenaltyTerm.class, 
	            		RentalLocation.class, 
	            		RentalStatusType.class, 
	            		UserManager.class,
	            		UserManagerBean.class, 
	            		RentalUser.class, 
	            		User.class,
	            		Role.class,
	            		Phone.class, 
	            		PhoneType.class, 
	            		RentalItemManager.class,
	            		RentalItemManagerBean.class, 
	            		RentalUserManager.class,
	            		RentalUserManagerBean.class,
	            		RentalUserMgmtService.class,
	            		RentalUserMgmtServiceBean.class,
	            		RentalManagerService.class,
	            		RentalManagerServiceBean.class,
	            		BadInputDataException.class, 
	            		InvalidBusinessStateException.class, 
	            		RentalInstance.class, 
	            		StringEscapeUtils.class, 
	            		NestableRuntimeException.class, 
	            		Nestable.class, 
	            		NestableDelegate.class,
	            		JBossLoginContextFactory.class,
	            		Resources.class)
	            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
	            .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/jboss-ejb3.xml"))
	            .addAsResource("META-INF/ejb-jar.xml")
	            .addAsResource("META-INF/jboss-app.xml")
	            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
	            .addAsResource("ExceptionMessages.properties")
	            .addAsResource("config.properties");
	   }
	
	
	@Inject 
	RentalUser user, addedUser, owner, addedOwner, addedBorrower;
	@Inject
	Address address, address1, rentalAddress, rentalAddress1;
	@Inject
	Phone phone1, phone2;
	@Inject
	UserManager userManager;
	@Inject
	RentalItem rentalItem, addedItem, updatedItem, addedItem2, updatedItem2;
	@Inject
	RentalCost rentalCost, rentalCost1, rentalCost2, rentalCost3, rentalCost4;
	@Inject
	PenaltyTerm penaltyTerm;
	@Inject
	RentalLocation location1, location2;
	@Inject
	RentalItemManager rentalItemManager;
	@Inject
	RentalManagerService rentalManager;
	@Inject
	RentalUserManager rentalUserManager;
	@Inject
	Logger log;
	@Inject
	UserTransaction utx;
	@Inject
	PropertyManager pm;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before 
	public void beforeTesting() throws Exception {
		log.info("in Before");
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("Admin");
		user.setLastName("Singh");
		user.setEmail("pinakallc@gmail.com");
		user.setUserName("pinakallc@gmail.com");
		user.setPassword(JBossLoginContextFactory.generateDigestPassword("pinakallc@gmail.com","password",PropertyManager.getProp("realm")));
		user.setActivated(true);
		Role role = new Role("admin", "Roles");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		user.setRoles(roles);
		

		User administrator=userManager.addUser(user);
		utx.commit();
		assertEquals(JBossLoginContextFactory.generateDigestPassword("pinakallc@gmail.com","password",PropertyManager.getProp("realm")), administrator.getPassword());
		log.info("end of Before");
	}
	
	@After 
	public void afterTesting() throws Exception {
		 log.info("in After");
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 
		 if(utx.getStatus()!=6) utx.rollback();
		 utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		    			 
							@Override
							public Void run(){				
		    			   	userManager.removeUser("pinakallc@gmail.com");
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
		    		   
			  utx.commit();
			  loginContext.logout(); }
		    		  
		 
		 log.info("end of After");
	}
	

	@Test
	public void testRequestRental() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 2);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 3);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 4);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
		
		assertFalse(rentalRequest.getId()==0l);
		assertEquals(addedBorrower.getUser().getUserName(), rentalRequest.getBorrower().getUser().getUserName());
//		assertEquals(null, rentalRequest.getRentedItem().getOwner());
		assertEquals(updatedItem.getId(), rentalRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, rentalRequest.getActualRentalStart());
		assertEquals(null, rentalRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.REQUESTED, rentalRequest.getStatus());
		
		//TODO add calculated value test
		 
		
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUserId());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								

								

		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
			
	}
	
	
	@Test
	public void testRequestRentalItemNotExists() throws Exception {
		addedBorrower = this.addBorrower();
						
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("NoRentalItem", 999));
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		RentalInstance rentalRequest; 
		
	    try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(999, 
	    		   												plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	    
				
	
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}

	@Test
	public void testRequestRentalItemDisabledRenting() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		//addedItem.setEnableRenting(true);  RENTING DISABLED
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);
		
		thrown.expect(InvalidBusinessStateException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentingDisabled", updatedItem.getId(), updatedItem.getName()));
		

		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		RentalInstance rentalRequest; 
		
	    try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
													plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
	    
	    
					
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}

	@Test
	public void testRequestRentalItemFutureRentable() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testRequestRentalRightCostsApplied() {
		fail("Not yet implemented");
	}

	@Test
	public void testRequestRentalInvalidRentalDates() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 2);
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidRentalDates", plannedStartDate.getTime(), plannedReturnDate.getTime()));
			
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		RentalInstance rentalRequest; 
		
	    try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
													plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
	    
	    
					
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}
	


	@Test
	public void testApproveRentalRequest() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }

		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), approvedRequest.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), approvedRequest.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), approvedRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-approvedRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-approvedRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, approvedRequest.getActualRentalStart());
		assertEquals(null, approvedRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.OWNER_APPROVED, approvedRequest.getStatus());
		assertEquals(true, approvedRequest.getRentedItem().isCurrentlyRented());
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(approvedRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	}

	@Test
	public void testApproveRentalRequestNotOwner() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		
		//Login as borrower to approve - which is not a valid user/owner
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("NotAnOwner",addedBorrower.getUser().getUserName()));

		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 

		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}

	
	
	@Test
	public void testApproveNonExistingRentalRequest() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);
			
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalInstanceIdInvalid", 9999));
		

		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(9999); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
	    
	    
	    			
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
		 
	}
	
	
	

	
	@Test
	public void testApproveRentalRequestInvalidState() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }

		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), approvedRequest.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), approvedRequest.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), approvedRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-approvedRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-approvedRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, approvedRequest.getActualRentalStart());
		assertEquals(null, approvedRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.OWNER_APPROVED, approvedRequest.getStatus());
		assertEquals(true, approvedRequest.getRentedItem().isCurrentlyRented());
	
		
		thrown.expect(InvalidBusinessStateException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidStatus","RentalInstance",RentalStatusType.OWNER_APPROVED));
		
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance approveAgain;
		 try {
			 approveAgain = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
	
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(approvedRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }

	}
	
	@Test
	public void testRejectRentalRequest() throws Exception {
			
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
	
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to reject
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance rejectedRequest;
		 try {
			 rejectedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rejectRentalRequest(rentalRequest.getId(),
													RejectionReasonType.BORROWERS_INSUFFICIENT_EXPERIENCE, 
													true); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
//		RentalInstance rejectedRequest = rentalManager.rejectRentalRequest(rentalRequest.getId(),
//														RejectionReasonType.BORROWERS_INSUFFICIENT_EXPERIENCE, 
//														true);

		
		assertNotNull(rejectedRequest);
		assertFalse(rejectedRequest.getId()==0l);
		
		assertNull(rejectedRequest.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), rejectedRequest.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), rejectedRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rejectedRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rejectedRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, rejectedRequest.getActualRentalStart());
		assertEquals(null, rejectedRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.OWNER_REJECTED, rejectedRequest.getStatus());
		assertEquals(RejectionReasonType.BORROWERS_INSUFFICIENT_EXPERIENCE, rejectedRequest.getRejectionReason());
		assertEquals(false, rejectedRequest.getRentedItem().isCurrentlyRented());
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rejectedRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }

	}
	
	@Test
	public void testRejectRentalRequestNotAnOwner() throws Exception {
			
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login NOT as owner to reject
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("NotAnOwner",addedBorrower.getUser().getUserName()));

		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm"));
//		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance rejectedRequest;
		 try {
			 rejectedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rejectRentalRequest(rentalRequest.getId(),
													RejectionReasonType.BORROWERS_INSUFFICIENT_EXPERIENCE, 
													true); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
	
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }

	}

	@Test
	public void testRejectRentalRequestOtherReason() throws Exception {
			
		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to reject
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance rejectedRequest;
		 try {
			 rejectedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rejectRentalRequest(rentalRequest.getId(),
													RejectionReasonType.OTHER_REASON, 
													"I don't feel comfortable with the borrower",
													true); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }

		
		assertNotNull(rejectedRequest);
		assertFalse(rejectedRequest.getId()==0l);
		
		assertNull(rejectedRequest.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), rejectedRequest.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), rejectedRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rejectedRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rejectedRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, rejectedRequest.getActualRentalStart());
		assertEquals(null, rejectedRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.OWNER_REJECTED, rejectedRequest.getStatus());
		assertEquals(RejectionReasonType.OTHER_REASON, rejectedRequest.getRejectionReason());
		assertEquals("I don't feel comfortable with the borrower", rejectedRequest.getOtherRejectionReason());
		assertEquals(false, rejectedRequest.getRentedItem().isCurrentlyRented());
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rejectedRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }

		
	}
	
	@Test
	public void testRejectRentalRequestOtherReasonNull() throws Exception {
		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("ProvideOtherReason"));		
		
		//Login as owner to reject
	
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance rejectedRequest;
		 try {
			 rejectedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rejectRentalRequest(rentalRequest.getId(),
													RejectionReasonType.OTHER_REASON, 
													null,
													true);
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     

			loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
			
			loginContext.login();
			final RentalInstance rejectedRequest1;
			 try {
				 rejectedRequest1 = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
		 
		    		   						@Override
		    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
		    		   							return rentalManager.rejectRentalRequest(rentalRequest.getId(),
														RejectionReasonType.OTHER_REASON, 
														true);
																						
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		     }  finally {
		          loginContext.logout();
		     

		
	
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
		     }
	     }
	}
	
	@Test
	public void testRejectRentalRequestInvalidState() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }

		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), approvedRequest.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), approvedRequest.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), approvedRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-approvedRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-approvedRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, approvedRequest.getActualRentalStart());
		assertEquals(null, approvedRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.OWNER_APPROVED, approvedRequest.getStatus());
		assertEquals(true, approvedRequest.getRentedItem().isCurrentlyRented());
		
		thrown.expect(InvalidBusinessStateException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidStatus","RentalInstance",RentalStatusType.OWNER_APPROVED));
		
	
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance rejectedRequest;
		 try {
			 rejectedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rejectRentalRequest(approvedRequest.getId(), RejectionReasonType.SAFETY_CONCERNS, false);
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
		
	
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(approvedRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}
	
	@Test
	public void testRejectRentalRequestInvalidId() throws Exception {		

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		
		
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalInstanceIdInvalid", 9999));
			

		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 	
		loginContext.login();
		final RentalInstance rejectedRequest;
		 try {
			 rejectedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rejectRentalRequest(9999, RejectionReasonType.SAFETY_CONCERNS, false);
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     

			
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }

	
	}
	
	@Test
	public void testRent() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), rented.getBorrower().getUser().getUserName());
		assertNull(rented.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), rented.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rented.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rented.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, rented.getActualRentalStart());
		assertEquals(null, rented.getActualRentalReturn());
		assertEquals(RentalStatusType.RENTED, rented.getStatus());
		
	
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rented.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	}
	
	@Test
	public void testRentWithPlannedDateChanges() throws Exception {
		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		plannedStartDate.add(Calendar.DAY_OF_MONTH, 3);		
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 4);
		
		
		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId(), plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
			assertNotNull(rented);
			assertFalse(rented.getId()==0l);
			
			assertEquals(addedBorrower.getUser().getUserName(), rented.getBorrower().getUser().getUserName());
			assertNull(rented.getRentedItem().getOwner().getUser().getUserName());
			assertEquals(updatedItem.getId(), rented.getRentedItem().getId());
			assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rented.getPlannedStartDate().getTimeInMillis())<1000);
			assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rented.getPlannedReturnDate().getTimeInMillis())<1000);
			assertEquals(null, rented.getActualRentalStart());
			assertEquals(null, rented.getActualRentalReturn());
			assertEquals(RentalStatusType.RENTED, rented.getStatus());		
	

	
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rented.getId());
						
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());

								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }

	}
	
	@Test
	public void testRentWithPlannedDateChangesInvalidDates() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		final Calendar newPlannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 3);	
		final Calendar newPlannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 2);
		
	
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidRentalDates", newPlannedStartDate.getTime(), newPlannedReturnDate.getTime()));		
		
		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId(), newPlannedStartDate, newPlannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
	
	
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(approvedRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }

	}
	
	@Test
	public void testRentInvalidRentalId() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalInstanceIdInvalid", 9999));
		
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(9999);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
     
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(approvedRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }

	}
	
	@Test
	public void testRentInvalidState() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		
	
		
		thrown.expect(InvalidBusinessStateException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidStatus","RentalInstance",RentalStatusType.REQUESTED));

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(rentalRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());

								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }	
	     }
	}
	
	@Test
	public void testWithdrawRentalRequestRented() throws Exception {	
		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
		
		final String withdrawalReason = "My wish";

		//login as borrower to withdraw
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance withdrawn;
		 
	     try {
	    	 withdrawn = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.withdrawRentalRequest(rented.getId(), withdrawalReason);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
		assertNotNull(withdrawn);
		assertFalse(withdrawn.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), withdrawn.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), withdrawn.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), withdrawn.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-withdrawn.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-withdrawn.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, withdrawn.getActualRentalStart());
		assertEquals(null, withdrawn.getActualRentalReturn());
		assertEquals(RentalStatusType.BORROWER_CANCELLED, withdrawn.getStatus());
		assertEquals(withdrawalReason, withdrawn.getRequestWithdrawalReason());
		assertEquals(false, withdrawn.getRentedItem().isCurrentlyRented());
		
		

		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								
								rentalManager.removeRentalInstance(withdrawn.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	}
	
	@Test
	public void testWithdrawRentalRequestAfterRequested() throws Exception {
	
		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		
		final String withdrawalReason = "My wish";

		//login as borrower to withdraw
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance withdrawn;
		 
	     try {
	    	 withdrawn = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.withdrawRentalRequest(rentalRequest.getId(), withdrawalReason);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
		assertNotNull(withdrawn);
		assertFalse(withdrawn.getId()==0l);
		
		assertNotNull(withdrawn);
		assertFalse(withdrawn.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), withdrawn.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), withdrawn.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), withdrawn.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-withdrawn.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-withdrawn.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, withdrawn.getActualRentalStart());
		assertEquals(null, withdrawn.getActualRentalReturn());
		assertEquals(RentalStatusType.BORROWER_CANCELLED, withdrawn.getStatus());
		assertEquals(withdrawalReason, withdrawn.getRequestWithdrawalReason());
		assertEquals(false, withdrawn.getRentedItem().isCurrentlyRented());
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
							
								rentalManager.removeRentalInstance(withdrawn.getId());						
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
									
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	}
	
	@Test
	public void testWithdrawRentalRequestOwnerApproved() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		
		final String withdrawalReason = "My wish";

		//login as borrower to withdraw
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance withdrawn;
		 
	     try {
	    	 withdrawn = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.withdrawRentalRequest(approvedRequest.getId(), withdrawalReason);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
		assertNotNull(withdrawn);
		assertFalse(withdrawn.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), withdrawn.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), withdrawn.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), withdrawn.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-withdrawn.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-withdrawn.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, withdrawn.getActualRentalStart());
		assertEquals(null, withdrawn.getActualRentalReturn());
		assertEquals(RentalStatusType.BORROWER_CANCELLED, withdrawn.getStatus());
		assertEquals(withdrawalReason, withdrawn.getRequestWithdrawalReason());
		assertEquals(false, withdrawn.getRentedItem().isCurrentlyRented());
		
		

		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								
								rentalManager.removeRentalInstance(withdrawn.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	}
	
	@Test
	public void testWithdrawRentalRequestNoReason() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		
		final String withdrawalReason = null;

		//login as borrower to withdraw
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance withdrawn;
		 
	     try {
	    	 withdrawn = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.withdrawRentalRequest(approvedRequest.getId(), withdrawalReason);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
			assertNotNull(withdrawn);
			assertFalse(withdrawn.getId()==0l);
			
			assertEquals(addedBorrower.getUser().getUserName(), withdrawn.getBorrower().getUser().getUserName());
			assertEquals(addedOwner.getUser().getUserName(), withdrawn.getRentedItem().getOwner().getUser().getUserName());
			assertEquals(updatedItem.getId(), withdrawn.getRentedItem().getId());
			assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-withdrawn.getPlannedStartDate().getTimeInMillis())<1000);
			assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-withdrawn.getPlannedReturnDate().getTimeInMillis())<1000);
			assertEquals(null, withdrawn.getActualRentalStart());
			assertEquals(null, withdrawn.getActualRentalReturn());
			assertEquals(RentalStatusType.BORROWER_CANCELLED, withdrawn.getStatus());
			assertEquals(withdrawalReason, withdrawn.getRequestWithdrawalReason());
			assertEquals(false, withdrawn.getRentedItem().isCurrentlyRented());
		

		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								
								rentalManager.removeRentalInstance(withdrawn.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }

	}
	
	@Test
	public void testWithdrawRentalRequestAfterRejection() throws Exception {
		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
	
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to reject
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance rejectedRequest;
		 try {
			 rejectedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rejectRentalRequest(rentalRequest.getId(),
													RejectionReasonType.BORROWERS_INSUFFICIENT_EXPERIENCE, 
													true); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		
		assertNotNull(rejectedRequest);
		assertFalse(rejectedRequest.getId()==0l);
		
		assertNull(rejectedRequest.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), rejectedRequest.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), rejectedRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rejectedRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rejectedRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, rejectedRequest.getActualRentalStart());
		assertEquals(null, rejectedRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.OWNER_REJECTED, rejectedRequest.getStatus());
		assertEquals(RejectionReasonType.BORROWERS_INSUFFICIENT_EXPERIENCE, rejectedRequest.getRejectionReason());
		assertEquals(false, rejectedRequest.getRentedItem().isCurrentlyRented());
		
		
	
		thrown.expect(InvalidBusinessStateException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidStatus","RentalInstance",RentalStatusType.OWNER_REJECTED));

		final String withdrawalReason = null;

		//login as borrower to withdraw
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance withdrawn;
		 
	     try {
	    	 withdrawn = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.withdrawRentalRequest(rejectedRequest.getId(), withdrawalReason);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
				
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rejectedRequest.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
								
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}
	
	@Test
	public void testWithdrawRentalRequestInvalidId() throws Exception {
	thrown.expect(BadInputDataException.class);
	thrown.expectMessage(PropertyManager.getMessage("RentalInstanceIdInvalid", 9999));
	
	addedBorrower = this.addBorrower();
	
	//login as borrower to withdraw
	LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
	loginContext.login();
	final RentalInstance withdrawn;
	 
     try {
    	 withdrawn = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
 
    		   						@Override
    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
    		   							return rentalManager.withdrawRentalRequest(9999, "bummer");
							
    		   						} });
     } catch (PrivilegedActionException pe) {
    	 throw pe.getException();
     }  finally {
          loginContext.logout();
     
	 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
	 loginContext.login();
	 if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
	 try {
	   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
	    			 
						@Override
						public Void run() throws BadInputDataException {				
	
							rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
							userManager.removeUser(addedBorrower.getUser().getUserName());
							
	    	   			return null;
						}
	    	   }); }  
	    	   
	  finally {
		  loginContext.logout();		   
		  utx.commit();
		   }
     }
	}
	
	@Test
	public void testReturnRental() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
			
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		
	
		//login as borrower to return
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance returned;
		 
	     try {
	    	 returned = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.returnRental(rented.getId(), actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(returned);
		assertFalse(returned.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), returned.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), returned.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), returned.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-returned.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-returned.getPlannedReturnDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualStartTime.getTimeInMillis()-returned.getActualRentalStart().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualReturnTime.getTimeInMillis()-returned.getActualRentalReturn().getTimeInMillis())<1000);

		assertEquals(RentalStatusType.MARKED_RETURNED, returned.getStatus());
		assertEquals(true, returned.getRentedItem().isCurrentlyRented());
		
			
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(returned.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	}
	
	@Test
	public void testReturnRentalInvalidId() throws Exception {
		
		addedBorrower = this.addBorrower();
		
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalInstanceIdInvalid", 9999));

		
		//login as borrower to return
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance returned;
		 
	     try {
	    	 returned = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.returnRental(9999, actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	    
	     
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
															
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
		  }
			   }
	
	}
	
	@Test
	public void testReturnRentalInvalidStatus() throws Exception {

		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		
		thrown.expect(InvalidBusinessStateException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidStatus","RentalInstance",RentalStatusType.REQUESTED));
		
		
		//login as borrower to return
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance returned;
		 
	     try {
	    	 returned = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.returnRental(rentalRequest.getId(), actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
	     
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());						
														
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
															
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
		

	}
	
	@Test
	public void testReturnRentalAfterClosing() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
		
	
		
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		
	
		//Login as owner to complete
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance completed;
		 try {
			 completed = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.completeRental(rented.getId(), actualStartTime, actualReturnTime); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(completed);
		assertFalse(completed.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), completed.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), completed.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), completed.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-completed.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-completed.getPlannedReturnDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualStartTime.getTimeInMillis()-completed.getActualRentalStart().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualReturnTime.getTimeInMillis()-completed.getActualRentalReturn().getTimeInMillis())<1000);

		assertEquals(RentalStatusType.CLOSED, completed.getStatus());
		assertEquals(false, completed.getRentedItem().isCurrentlyRented());
		
		final Calendar actualStartTimeDiff = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 3);
		
		final Calendar actualReturnTimeDiff = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 4);	
		
		//login as borrower to return
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance returned;
		 
	     try {
	    	 returned = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.returnRental(completed.getId(), actualStartTimeDiff, actualReturnTimeDiff);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }

		
		
		assertNotNull(returned);
		assertFalse(returned.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), returned.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), returned.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), returned.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-returned.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-returned.getPlannedReturnDate().getTimeInMillis())<1000);
		assertFalse(Math.abs(actualStartTimeDiff.getTimeInMillis()-returned.getActualRentalStart().getTimeInMillis())<1000);
		assertFalse(Math.abs(actualReturnTimeDiff.getTimeInMillis()-returned.getActualRentalReturn().getTimeInMillis())<1000);

		assertEquals(RentalStatusType.CLOSED, returned.getStatus());
		assertEquals(false, returned.getRentedItem().isCurrentlyRented());
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {
								
								rentalManager.removeRentalInstance(completed.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }	
	
	}
	
	@Test
	public void testReturnRentalInvalidDates() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 3);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 2);	

		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidRentalDates", actualStartTime.getTime(), actualReturnTime.getTime()));
		
		//login as borrower to return
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance returned;
		 
	     try {
	    	 returned = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.returnRental(rented.getId(), actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
		
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {
								
								rentalManager.removeRentalInstance(rented.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }

	}
	
	@Test
	public void testCompleteRental() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
			
			
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		

		//Login as owner to complete
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance completed;
		 
	     try {
	    	 completed = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.completeRental(rented.getId(), actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }

		
		assertNotNull(completed);
		assertFalse(completed.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), completed.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), completed.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), completed.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-completed.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-completed.getPlannedReturnDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualStartTime.getTimeInMillis()-completed.getActualRentalStart().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualReturnTime.getTimeInMillis()-completed.getActualRentalReturn().getTimeInMillis())<1000);

		assertEquals(RentalStatusType.CLOSED, completed.getStatus());
		assertEquals(false, completed.getRentedItem().isCurrentlyRented());
		
		

		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(completed.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
								return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }

	}
	
	@Test
	public void testCompleteRentalAfterReturn() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
			
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		
	
		//login as borrower to return
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance returned;
		 
	     try {
	    	 returned = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.returnRental(rented.getId(), actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(returned);
		assertFalse(returned.getId()==0l);
		
	
	
		final Calendar actualStartTimeDiff = new GregorianCalendar();
		actualStartTimeDiff.add(Calendar.DAY_OF_MONTH, 1);
		
		final Calendar actualReturnTimeDiff = new GregorianCalendar();
		actualReturnTimeDiff.add(Calendar.DAY_OF_MONTH, 2);	

		
		//Login as owner to complete
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance completed;
		 
	     try {
	    	 completed = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.completeRental(returned.getId(), actualStartTimeDiff, actualReturnTimeDiff);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(completed);
		assertFalse(completed.getId()==0l);
		
		assertEquals(addedBorrower.getUser().getUserName(), completed.getBorrower().getUser().getUserName());
		assertEquals(addedOwner.getUser().getUserName(), completed.getRentedItem().getOwner().getUser().getUserName());
		assertEquals(updatedItem.getId(), completed.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-completed.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-completed.getPlannedReturnDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualStartTimeDiff.getTimeInMillis()-completed.getActualRentalStart().getTimeInMillis())<1000);
		assertTrue(Math.abs(actualReturnTimeDiff.getTimeInMillis()-completed.getActualRentalReturn().getTimeInMillis())<1000);

		assertEquals(RentalStatusType.CLOSED, completed.getStatus());
		assertEquals(false, completed.getRentedItem().isCurrentlyRented());
		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(completed.getId());
								
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }		
	
	
	}
	
	@Test
	public void testCompleteRentalInvalidId() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();	
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalInstanceIdInvalid", 9999));
	
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance completed;
		 
	     try {
	    	 completed = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.completeRental(9999, actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     
	     
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {											
								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
																
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }	
	     }
	
	}
	
	@Test
	public void testCompleteRentalInvalidDates() throws Exception {

		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
			
		


		//Login as owner to approve
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		
		loginContext.login();
		final RentalInstance approvedRequest;
		 try {
	    	approvedRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.approveRentalRequest(rentalRequest.getId()); 
																					
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(approvedRequest);
		assertFalse(approvedRequest.getId()==0l);

		//login as borrower to rent
		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rented;
		 
	     try {
	    	   rented = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.rent(approvedRequest.getId());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rented);
		assertFalse(rented.getId()==0l);
			
			
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 3);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 2);	
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidRentalDates", actualStartTime.getTime(), actualReturnTime.getTime()));		

		//Login as owner to complete
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance completed;
		 
	     try {
	    	 completed = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.completeRental(rented.getId(), actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     


		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rented.getId());							

								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
								return null;
								
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}
	
	@Test
	public void testCompleteRentalInvalidStatus() throws Exception {
		
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 1);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 3);


		//Login as borrower to request
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		assertNotNull(rentalRequest);
		assertFalse(rentalRequest.getId()==0l);
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		
		final Calendar actualStartTime = new GregorianCalendar();
		actualStartTime.add(Calendar.DAY_OF_MONTH, 2);
		
		final Calendar actualReturnTime = new GregorianCalendar();
		actualReturnTime.add(Calendar.DAY_OF_MONTH, 3);	
		
		thrown.expect(InvalidBusinessStateException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidStatus","RentalInstance",RentalStatusType.REQUESTED));
		
	
		//Login as owner to complete
		loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance completed;
		 
	     try {
	    	 completed = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.completeRental(rentalRequest.getId(), actualStartTime, actualReturnTime);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     

		
		
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());

								rentalUserManager.removeUser(addedBorrower.getUser().getUserName());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
	
								return null;
								
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
	     }
	}
	
	@Test
	public void testGetRentalRequests() throws Exception {
		addedBorrower = this.addBorrower();
		addedItem = this.addRentalItem();
		addedItem2 = this.addRentalItem2();
				
		GregorianCalendar availableFrom = new GregorianCalendar();
		availableFrom.add(Calendar.DAY_OF_MONTH, 2);
		
		addedItem.setAvailableFrom(availableFrom);
		addedItem.setEnableRenting(true);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		updatedItem = rentalItemManager.updateRentalItem(addedItem);
		utx.commit();
		
		final Calendar plannedStartDate = new GregorianCalendar();
		plannedStartDate.add(Calendar.DAY_OF_MONTH, 3);
		
		final Calendar plannedReturnDate = new GregorianCalendar();
		plannedReturnDate.add(Calendar.DAY_OF_MONTH, 4);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final RentalInstance rentalRequest;
		 
	     try {
	    	   rentalRequest = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
	 
	    		   						@Override
	    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
	    		   							return rentalManager.requestRental(updatedItem.getId(), 
									     			                           plannedStartDate, plannedReturnDate);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
		
		assertFalse(rentalRequest.getId()==0l);
		assertEquals(addedBorrower.getUser().getUserName(), rentalRequest.getBorrower().getUser().getUserName());
//		assertEquals(null, rentalRequest.getRentedItem().getOwner());
		assertEquals(updatedItem.getId(), rentalRequest.getRentedItem().getId());
		assertTrue(Math.abs(plannedStartDate.getTimeInMillis()-rentalRequest.getPlannedStartDate().getTimeInMillis())<1000);
		assertTrue(Math.abs(plannedReturnDate.getTimeInMillis()-rentalRequest.getPlannedReturnDate().getTimeInMillis())<1000);
		assertEquals(null, rentalRequest.getActualRentalStart());
		assertEquals(null, rentalRequest.getActualRentalReturn());
		assertEquals(RentalStatusType.REQUESTED, rentalRequest.getStatus());
		

		loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
		loginContext.login();
		final List<RentalInstance> borrowals;
		 
	     try {
	    	   borrowals = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	 
	    		   						@Override
	    		   						public List<RentalInstance> run() throws BadInputDataException {													
	    		   							return rentalManager.getMyBorrowals();
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
		
		 assertEquals(1,borrowals.size());
		 
		 loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
			loginContext.login();
			final List<RentalInstance> leases;
			 
		     try {
		    	   leases = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
		 
		    		   						@Override
		    		   						public List<RentalInstance> run() throws BadInputDataException {													
		    		   							return rentalManager.getMyLeases();
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		     }  finally {
		          loginContext.logout();
		     }
			
			 assertEquals(1,leases.size());
			 
		
				GregorianCalendar availableFrom2 = new GregorianCalendar();
				availableFrom2.add(Calendar.DAY_OF_MONTH, 2);
				
				addedItem2.setAvailableFrom(availableFrom2);
				addedItem2.setEnableRenting(true);
				
				if(utx.getStatus()!=6) utx.rollback();
				utx.begin();
				updatedItem2 = rentalItemManager.updateRentalItem(addedItem2);
				utx.commit();
				
				final Calendar plannedStartDate2 = new GregorianCalendar();
				plannedStartDate2.add(Calendar.DAY_OF_MONTH, 3);
				
				final Calendar plannedReturnDate2 = new GregorianCalendar();
				plannedReturnDate2.add(Calendar.DAY_OF_MONTH, 4);
				
				loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
				loginContext.login();
				final RentalInstance rentalRequest2;
				 
			     try {
			    	   rentalRequest2 = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalInstance>() {
			 
			    		   						@Override
			    		   						public RentalInstance run() throws BadInputDataException,InvalidBusinessStateException {													
			    		   							return rentalManager.requestRental(updatedItem2.getId(), 
											     			                           plannedStartDate2, plannedReturnDate2);
										
			    		   						} });
			     } catch (PrivilegedActionException pe) {
			    	 throw pe.getException();
			     }  finally {
			          loginContext.logout();
			     }
			     
				
				assertFalse(rentalRequest2.getId()==0l);
				assertEquals(addedBorrower.getUser().getUserName(), rentalRequest2.getBorrower().getUser().getUserName());
//				assertEquals(null, rentalRequest.getRentedItem().getOwner());
				assertEquals(updatedItem2.getId(), rentalRequest2.getRentedItem().getId());
				assertTrue(Math.abs(plannedStartDate2.getTimeInMillis()-rentalRequest2.getPlannedStartDate().getTimeInMillis())<1000);
				assertTrue(Math.abs(plannedReturnDate2.getTimeInMillis()-rentalRequest2.getPlannedReturnDate().getTimeInMillis())<1000);
				assertEquals(null, rentalRequest2.getActualRentalStart());
				assertEquals(null, rentalRequest2.getActualRentalReturn());
				assertEquals(RentalStatusType.REQUESTED, rentalRequest2.getStatus());	 
			 
				loginContext = JBossLoginContextFactory.createLoginContext(addedBorrower.getUser().getUserName(),"borrowerpassword", PropertyManager.getProp("realm")); 
				loginContext.login();
				final List<RentalInstance> borrowals2;
				 
			     try {
			    	   borrowals2 = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
			 
			    		   						@Override
			    		   						public List<RentalInstance> run() throws BadInputDataException {													
			    		   							return rentalManager.getMyBorrowals();
										
			    		   						} });
			     } catch (PrivilegedActionException pe) {
			    	 throw pe.getException();
			     }  finally {
			          loginContext.logout();
			     }
				
				 assertEquals(2,borrowals2.size());
				 
				 loginContext = JBossLoginContextFactory.createLoginContext(addedItem.getOwner().getUser().getUserName(),"ownerpassword", PropertyManager.getProp("realm")); 
					loginContext.login();
					final List<RentalInstance> leases2;
					 
				     try {
				    	   leases2 = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
				 
				    		   						@Override
				    		   						public List<RentalInstance> run() throws BadInputDataException {													
				    		   							return rentalManager.getMyLeases();
											
				    		   						} });
				     } catch (PrivilegedActionException pe) {
				    	 throw pe.getException();
				     }  finally {
				          loginContext.logout();
				     }
					
					 assertEquals(1,leases2.size());
					 
					 
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException {				
								rentalManager.removeRentalInstance(rentalRequest.getId());
								rentalManager.removeRentalInstance(rentalRequest2.getId());
								rentalUserManager.removeUser(addedBorrower.getUserId());
								userManager.removeUser(addedBorrower.getUser().getUserName());
								rentalItemManager.removeRentalItem(addedItem.getId());
								rentalItemManager.removeRentalItem(addedItem2.getId());
								rentalUserManager.removeUser(addedItem.getOwner().getUser().getUserName());
								rentalUserManager.removeUser(addedItem2.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem.getOwner().getUser().getUserName());
								userManager.removeUser(addedItem2.getOwner().getUser().getUserName());
								

								

		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
			  loginContext.logout();		   
			  utx.commit();
			   }
			
	}
	
	
	private RentalUser addOwner() throws Exception {
		//add user to use as owner
		User user = new User();
		user.setFirstName("Owner");
		user.setLastName("Singh");
		user.setEmail("veer.muchandi@gmail.com");
		user.setPassword(JBossLoginContextFactory.generateDigestPassword("veer.muchandi@gmail.com","ownerpassword",PropertyManager.getProp("realm")));
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);
		Role role = new Role("user", "Roles");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		user.setRoles(roles);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User addedUser=userManager.addUser(user);
		utx.commit();
		
		assertNotNull(addedUser.getUserName());
		assertEquals("Owner", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("veer.muchandi@gmail.com",addedUser.getEmail());
		assertEquals("veer.muchandi@gmail.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		owner.setUser(addedUser);
		utx.begin();
		addedOwner = rentalUserManager.addUser(owner);
		utx.commit();
		return addedOwner;

	}
	
	private RentalUser addOwner2() throws Exception {
		//add user to use as owner
		User user = new User();
		user.setFirstName("OwnerTwo");
		user.setLastName("Singh");
		user.setEmail("veer2.muchandi@gmail.com");
		user.setPassword(JBossLoginContextFactory.generateDigestPassword("veer2.muchandi@gmail.com","ownerpassword",PropertyManager.getProp("realm")));
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);
		Role role = new Role("user", "Roles");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		user.setRoles(roles);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User addedUser=userManager.addUser(user);
		utx.commit();
		
		assertNotNull(addedUser.getUserName());
		assertEquals("OwnerTwo", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("veer2.muchandi@gmail.com",addedUser.getEmail());
		assertEquals("veer2.muchandi@gmail.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		owner.setUser(addedUser);
		utx.begin();
		addedOwner = rentalUserManager.addUser(owner);
		utx.commit();
		return addedOwner;

	}

	
	private RentalUser addBorrower() throws Exception {
		//add user to use as borrower
		User user = new User();
		user.setFirstName("Borrower");
		user.setLastName("Singh");
		user.setEmail("veer_muchandi@hotmail.com");
		user.setPassword(JBossLoginContextFactory.generateDigestPassword("veer_muchandi@hotmail.com","borrowerpassword",PropertyManager.getProp("realm")));
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);
		Role role = new Role("user", "Roles");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		user.setRoles(roles);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User addedUser= userManager.addUser(user);
		utx.commit();
		
		assertNotNull(addedUser.getUserName());
		assertEquals("Borrower", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("veer_muchandi@hotmail.com",addedUser.getEmail());
		assertEquals("veer_muchandi@hotmail.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		RentalUser borrower = new RentalUser();
		borrower.setUser(addedUser);
		utx.begin();
		RentalUser addedBorrower = rentalUserManager.addUser(borrower);
		utx.commit();
		return addedBorrower;

	}
	
	private RentalItem addRentalItem() throws Exception {
		
		addedOwner = this.addOwner();
		
		
		//create and rental item 
		rentalItem.setName("Super Plier");
		rentalItem.setItemCategory("Metal");
		rentalItem.setItemSubCategory("Tool");
		rentalItem.setItemBrand("Bosch");
		rentalItem.setAvailableFrom(null);
		rentalItem.setConsolidatedRating(0);
		rentalItem.setEnableRenting(false);
		rentalItem.setItemAge(1);
		rentalItem.setPastRentalCount(0);
		rentalItem.setReplacementCost(0);
		
		rentalCost.setCost(0.50);
		rentalCost.setPeriod(RentalPeriodType.HOUR);
		rentalCost1.setCost(5.00);
		rentalCost1.setPeriod(RentalPeriodType.DAY);
		rentalCost2.setCost(0.50);
		rentalCost2.setPeriod(RentalPeriodType.WEEK);
		rentalCost3.setCost(0.50);
		rentalCost3.setPeriod(RentalPeriodType.MONTH);
		rentalCost4.setCost(0.50);
		rentalCost4.setPeriod(RentalPeriodType.YEAR);
		
		List<RentalCost> costs = new ArrayList<RentalCost>();
		costs.add(rentalCost);
		costs.add(rentalCost1);
		costs.add(rentalCost2);
		costs.add(rentalCost3);
		costs.add(rentalCost4);
		rentalItem.setCosts(costs);
		
		penaltyTerm.setPenaltyCharge(5.00);
		penaltyTerm.setPenaltyTerm("misuse");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		rentalItem.setPenaltyTerms(pterms);
		
		location1.setContactName("One K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		rentalAddress.setAddress1("line 1");
		rentalAddress.setCity("nowhere 1");
		rentalAddress.setState("nostate 1");
		rentalAddress.setZip("12345");
		location1.setAddress(rentalAddress);
		location1.setEmail("pk1@singh.com");
		
		
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		
		location2.setContactName("Two K Singh");
		location2.setContactPhoneNumber("111-111-1111");
		rentalAddress1.setAddress1("line 2");
		rentalAddress1.setCity("nowhere2");
		rentalAddress1.setState("nostate2");
		rentalAddress1.setZip("12345");
		location2.setAddress(rentalAddress1);
		location2.setEmail("pk2@singh.com");
		locations.add(location2);
		
		rentalItem.setRentalLocations(locations);	
		
		rentalItem.setOwner(addedOwner);
		
		if(utx.getStatus()!=6) utx.rollback();
		
		utx.begin();
		addedItem = rentalItemManager.addRentalItem(rentalItem);
		utx.commit();
		
		return addedItem;

	}
	
private RentalItem addRentalItem2() throws Exception {
		
		RentalUser addedOwner = this.addOwner2();
		
		RentalItem rentalItem2 = new RentalItem();
		//create and rental item 
		rentalItem2.setName("Super Plier Two");
		rentalItem2.setItemCategory("Metal");
		rentalItem2.setItemSubCategory("Tool");
		rentalItem2.setItemBrand("Bosch");
		rentalItem2.setAvailableFrom(null);
		rentalItem2.setConsolidatedRating(0);
		rentalItem2.setEnableRenting(false);
		rentalItem2.setItemAge(1);
		rentalItem2.setPastRentalCount(0);
		rentalItem2.setReplacementCost(0);
		
		rentalCost.setCost(0.50);
		rentalCost.setPeriod(RentalPeriodType.HOUR);
		rentalCost1.setCost(5.00);
		rentalCost1.setPeriod(RentalPeriodType.DAY);
		rentalCost2.setCost(0.50);
		rentalCost2.setPeriod(RentalPeriodType.WEEK);
		rentalCost3.setCost(0.50);
		rentalCost3.setPeriod(RentalPeriodType.MONTH);
		rentalCost4.setCost(0.50);
		rentalCost4.setPeriod(RentalPeriodType.YEAR);
		
		List<RentalCost> costs = new ArrayList<RentalCost>();
		costs.add(rentalCost);
		costs.add(rentalCost1);
		costs.add(rentalCost2);
		costs.add(rentalCost3);
		costs.add(rentalCost4);
		rentalItem2.setCosts(costs);
		
		penaltyTerm.setPenaltyCharge(5.00);
		penaltyTerm.setPenaltyTerm("misuse");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		rentalItem2.setPenaltyTerms(pterms);
		
		location1.setContactName("One K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		rentalAddress.setAddress1("line 1");
		rentalAddress.setCity("nowhere 1");
		rentalAddress.setState("nostate 1");
		rentalAddress.setZip("12345");
		location1.setAddress(rentalAddress);
		location1.setEmail("pk1@singh.com");
		
		
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		
		location2.setContactName("Two K Singh");
		location2.setContactPhoneNumber("111-111-1111");
		rentalAddress1.setAddress1("line 2");
		rentalAddress1.setCity("nowhere2");
		rentalAddress1.setState("nostate2");
		rentalAddress1.setZip("12345");
		location2.setAddress(rentalAddress1);
		location2.setEmail("pk2@singh.com");
		locations.add(location2);
		
		rentalItem2.setRentalLocations(locations);	
		
		rentalItem2.setOwner(addedOwner);
		
		if(utx.getStatus()!=6) utx.rollback();
		
		utx.begin();
		RentalItem addedItem = rentalItemManager.addRentalItem(rentalItem2);
		utx.commit();
		
		return addedItem;

	}
}

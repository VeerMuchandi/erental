/**
 *
 * @filename UserMgmtServiceTest.java
 * @author Veer Muchandi
 * @created May 20, 2012
 * © Copyright 2012 Pinaka LLC
 *
 */
package com.pinaka.UserManagement.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.Nestable;
import org.apache.commons.lang.exception.NestableDelegate;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import javax.persistence.EntityManager;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import java.util.logging.Logger;

import com.pinaka.UserManagement.ActivationDTO;
import com.pinaka.UserManagement.Address;
import com.pinaka.UserManagement.AdminUserDTO;
import com.pinaka.UserManagement.PasswordChangeDTO;
import com.pinaka.UserManagement.Phone;
import com.pinaka.UserManagement.PhoneType;
import com.pinaka.UserManagement.Role;
import com.pinaka.UserManagement.User;
import com.pinaka.UserManagement.UserManager;
import com.pinaka.UserManagement.UserManagerBean;
import com.pinaka.UserManagement.UserMgmtService;
import com.pinaka.UserManagement.UserMgmtServiceBean;
import com.pinaka.UserManagement.User_;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.PenaltyTerm;
import com.pinaka.eRental.model.RejectionReasonType;
import com.pinaka.eRental.model.RentalCost;
import com.pinaka.eRental.model.RentalInstance;
import com.pinaka.eRental.model.RentalItem;
import com.pinaka.eRental.model.RentalLocation;
import com.pinaka.eRental.model.RentalPeriodType;
import com.pinaka.eRental.model.RentalStatusType;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.model.RentalUser_;
import com.pinaka.eRental.util.EmailProcessor;
import com.pinaka.eRental.util.PropertyManager;
import com.pinaka.eRental.util.Resources;
import com.pinaka.testutil.JBossLoginContextFactory;

/**
 * @filename UserMgmtServiceTest.java
 * @author Veer Muchandi
 * @created May 20, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
@RunWith(Arquillian.class)
public class UserMgmtServiceTest {
	@Deployment
	   public static Archive<?> createTestArchive() {
		final String WEBAPP_SRC = "src/main/webapp";  
	    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
		            .addClasses(
		            		UserMgmtService.class,
		            		UserMgmtServiceBean.class, 
		            		User.class,
		            		User_.class,
		            		RentalUser.class,
		            		AdminUserDTO.class,
		            		Role.class,
		            		Phone.class,
		            		PhoneType.class, 
		            		Address.class,
		            		RentalItem.class, 
		            		PenaltyTerm.class, 
		            		RentalCost.class,
		            		RentalPeriodType.class, 
		            		RentalInstance.class, 
		            		PenaltyTerm.class, 
		            		RentalLocation.class, 
		            		RentalStatusType.class, 
		            		UserManager.class,
		            		UserManagerBean.class, 
		            		UserMgmtServiceBean.class,
		            		PasswordChangeDTO.class,
		            		ActivationDTO.class,
		            		BadInputDataException.class, 
		            		RejectionReasonType.class, 
		            		PropertyManager.class,
		            		Resources.class,
		            		EmailProcessor.class,
		            		StringEscapeUtils.class,
		            		NestableRuntimeException.class, 
		            		Nestable.class, 
		            		NestableDelegate.class,
		            		JBossLoginContextFactory.class)
		            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
		            .addAsResource("META-INF/persistence.xml")
	                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/jboss-ejb3.xml"))
	                .addAsResource("META-INF/ejb-jar.xml")
	                .addAsResource("META-INF/jboss-app.xml")
       	            .addAsResource("ExceptionMessages.properties")
       	            .addAsResource("config.properties")
		            ;
	    
	    System.out.println("Contents of war: " + webArchive.toString(true));
	    return webArchive;
	   }
	
	@Inject
	UserMgmtService userMgmtService;
	@Inject
	UserManager userManager;
	@Inject
	UserTransaction utx;
	@Inject
	Logger log;
	@Inject 
	User administrator;
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
	public void testSignup() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		final User foundUser = userManager.findUser(addedUser.getUserName());
		
		
		assertEquals("Mr.",foundUser.getTitle());
		assertEquals("PK", foundUser.getFirstName());
		assertEquals("Singh",foundUser.getLastName());
		assertEquals("pk@singh.com",foundUser.getEmail());
		assertEquals("pk@singh.com",foundUser.getUserName());
		assertEquals("my lane",foundUser.getAddress().getAddress1());
		assertNull(foundUser.getAddress().getAddress2());
		assertEquals("mycity",foundUser.getAddress().getCity());
		assertEquals("mystate",foundUser.getAddress().getState());
		assertEquals("99999",foundUser.getAddress().getZip());
		assertEquals("USA",foundUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", foundUser.generateFullName());
		
		assertEquals(false, foundUser.isActivated());
		assertEquals(JBossLoginContextFactory.generateDigestPassword("pk@singh.com","p123",PropertyManager.getProp("realm")), foundUser.getPassword());
		assertEquals(2, foundUser.getPhones().size());
		
		for (Phone phone: foundUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, foundUser.getRoles().size());
		
		for (Role r: foundUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		utx.commit();
		
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 
		 if(utx.getStatus()!=6) utx.rollback();
		 utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		    			 
							@Override
							public Void run(){				
								userManager.removeUser(foundUser.getUserName());
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
		    		   
			  utx.commit();
			  loginContext.logout(); }
		    		  
	}
	
	@Test
	public void testSignupWithRole() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		Role role = new Role("admin", "Roles");
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		user.setRoles(roles);
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		
		
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser(addedUser.getUserName());
//		utx.commit();	
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 
		 if(utx.getStatus()!=6) utx.rollback();
		 utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		    			 
							@Override
							public Void run(){				
								userManager.removeUser(addedUser.getUserName());
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
		    		   
			  utx.commit();
			  loginContext.logout(); }
			
	}
	
	@Test
	public void testSignupDuplicate() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		User user1 = new User();
		user1.setTitle("Mr.");
		user1.setFirstName("PK");
		user1.setLastName("Singh");
		user1.setEmail("pk@singh.com");
		user1.setPassword("p123");
		Address address1 = new Address();
		address1.setAddress1("my lane");
		address1.setCity("mycity");
		address1.setState("mystate");
		address1.setZip("99999");
		user1.setAddress(address1);
		user1.setActivated(true);
		
		try{
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("UserExists", user1.getEmail()));
		
		User addedUser1 = userMgmtService.signup(user1);
		}
		catch (Exception e){
		
//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser(addedUser.getUserName());
//		utx.commit();
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 
		 if(utx.getStatus()!=6) utx.rollback();
		 utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		    			 
							@Override
							public Void run(){				
								userManager.removeUser(addedUser.getUserName());
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
		    		   
			  utx.commit();
			  loginContext.logout(); }
		
		log.info("removed user");
		throw e;
		}
		
	}
	
	@Test
	public void testFindUser() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		//assertNull(addedUser.getRoles());
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		

		 
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pk@singh.com","p123",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 User foundUser;
		 
	     try {
	    	   foundUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						@Override
	    		   						public User run() throws BadInputDataException {													
	    		   							return userMgmtService.findUser(addedUser.getUserName());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
							
							assertEquals("Mr.",foundUser.getTitle());
							assertEquals("PK", foundUser.getFirstName());
							assertEquals("Singh",foundUser.getLastName());
							assertEquals("pk@singh.com",foundUser.getEmail());
							assertEquals("pk@singh.com",foundUser.getUserName());
							assertEquals("my lane",foundUser.getAddress().getAddress1());
							assertNull(foundUser.getAddress().getAddress2());
							assertEquals("mycity",foundUser.getAddress().getCity());
							assertEquals("mystate",foundUser.getAddress().getState());
							assertEquals("99999",foundUser.getAddress().getZip());
							assertEquals("USA",foundUser.getAddress().getCountry());
							assertEquals("Mr. PK Singh", foundUser.generateFullName());
							
							assertEquals(false, foundUser.isActivated());
							assertNull(foundUser.getPassword());
							assertEquals(2, foundUser.getPhones().size());
							
							for (Phone phone: foundUser.getPhones()) {
								if(phone.getPhoneType()==PhoneType.HOME) 
									assertEquals("111-111-1111", phone.getPhoneNumber());
								else 
									assertEquals("222-222-2222", phone.getPhoneNumber());
							}
							
							//assertEquals(1, foundUser.getRoles().size());
							
							for (Role r: foundUser.getRoles()) {
								assertEquals("user",r.getRole());
								assertEquals("Roles", r.getRoleGroup());
							}
	 
		
//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser(addedUser.getUserName());
//		utx.commit();	
		
		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
			Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
   			 
					@Override
					public Void run(){				
						userManager.removeUser(addedUser.getUserName());
   	   			
   	   			return null;
					}
			});  }	
		finally {
   		   loginContext.logout();
   		   utx.commit();
   	   }
	          
	     

		
			
	}
	
	@Test
	public void testFindUserNoAccess() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		//assertNull(addedUser.getRoles());
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		

		User user1 = new User();
		user1.setTitle("Mr.");
		user1.setFirstName("Nokay");
		user1.setLastName("Singh");
		user1.setEmail("nokay@singh.com");
		user1.setPassword("nokay123");
		Address address1 = new Address();
		address1.setAddress1("my lane");
		address1.setCity("mycity");
		address1.setState("mystate");
		address1.setZip("99999");
		user1.setAddress(address1);
		user1.setActivated(true);
		
		final User addedUser1 = userMgmtService.signup(user1);
		
		assertEquals("nokay@singh.com",addedUser1.getEmail());
		assertEquals("nokay@singh.com",addedUser1.getUserName());
		
		try{
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("UserMismatch"));
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("nokay@singh.com","nokay123",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 User foundUser;
		 
	     try {
	    	   foundUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						@Override
	    		   						public User run() throws BadInputDataException {													
	    		   							return userMgmtService.findUser(addedUser.getUserName());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
							 
		} finally {
//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser(addedUser.getUserName());
//			userManager.removeUser(addedUser1.getUserName());
//		utx.commit();	
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
			Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
   			 
					@Override
					public Void run(){				
						userManager.removeUser(addedUser.getUserName());
						userManager.removeUser(addedUser1.getUserName());
   	   			
   	   			return null;
					}
			});  }	
		finally {
   		   loginContext.logout();
   		   utx.commit();
   	   }
		}
			
	}
	
	@Test
	/**
	 * The admin has to log in and find a user.
	 * @throws Exception
	 */
	public void testFindUserByAdministrator() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		//assertNull(addedUser.getRoles());
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		

		User user1 = new User();
		user1.setTitle("Mr.");
		user1.setFirstName("Nokay");
		user1.setLastName("Singh");
		user1.setEmail("nokay@singh.com");
		user1.setPassword("nokay123");
		Address address1 = new Address();
		address1.setAddress1("my lane");
		address1.setCity("mycity");
		address1.setState("mystate");
		address1.setZip("99999");
		user1.setAddress(address1);
		user1.setActivated(true);
		
		final User addedUser1 = userMgmtService.signup(user1);
		
		assertEquals("nokay@singh.com",addedUser1.getEmail());
		assertEquals("nokay@singh.com",addedUser1.getUserName());
		
		final ActivationDTO activationInfo = new ActivationDTO("ee69e");
		
		User activatedUser; //= userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

		
		
		final AdminUserDTO admin = new AdminUserDTO();
		admin.setEmail(addedUser1.getEmail());
		admin.setUserName(addedUser1.getUserName());
		
		final User addedAdmin; // = userMgmtService.addAdministrator(admin);
		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	 addedAdmin = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.addAdministrator(admin);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

		
		
		
		try{
		
			loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 User foundUser;
		 
	     try {
	    	   foundUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						@Override
	    		   						public User run() throws BadInputDataException {													
	    		   							return userMgmtService.findUser(addedUser.getUserName());
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
			assertEquals("Mr.",foundUser.getTitle());
			assertEquals("PK", foundUser.getFirstName());
			assertEquals("Singh",foundUser.getLastName());
			assertEquals("pk@singh.com",foundUser.getEmail());
			assertEquals("pk@singh.com",foundUser.getUserName());
			assertEquals("my lane",foundUser.getAddress().getAddress1());
			assertNull(foundUser.getAddress().getAddress2());
			assertEquals("mycity",foundUser.getAddress().getCity());
			assertEquals("mystate",foundUser.getAddress().getState());
			assertEquals("99999",foundUser.getAddress().getZip());
			assertEquals("USA",foundUser.getAddress().getCountry());
			assertEquals("Mr. PK Singh", foundUser.generateFullName());
			
			assertEquals(false, foundUser.isActivated());
			assertNull(foundUser.getPassword());
			assertEquals(2, foundUser.getPhones().size());
			
			for (Phone phone: foundUser.getPhones()) {
				if(phone.getPhoneType()==PhoneType.HOME) 
					assertEquals("111-111-1111", phone.getPhoneNumber());
				else 
					assertEquals("222-222-2222", phone.getPhoneNumber());
			}
			
			//assertEquals(1, foundUser.getRoles().size());
			
			for (Role r: foundUser.getRoles()) {
				assertEquals("user",r.getRole());
				assertEquals("Roles", r.getRoleGroup());
			}
							 
		} finally {

//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser(addedUser.getUserName());
//			userManager.removeUser(addedAdmin.getUserName());
//		utx.commit();	
		
		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
			Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
   			 
					@Override
					public Void run(){				
						userManager.removeUser(addedUser.getUserName());
						userManager.removeUser(addedAdmin.getUserName());
   	   			
   	   			return null;
					}
			});  }	
		finally {
   		   loginContext.logout();
   		   utx.commit();
   	   }
		}
			
	}
	
	@Test
	/**
	 * The admin has to log in and find a user.
	 * @throws Exception
	 */
	public void testFindUserNonExistingByAdministrator() throws Exception {
	
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 User foundUser;
		 
	     try {
	    	   foundUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						@Override
	    		   						public User run() throws BadInputDataException {													
	    		   							return userMgmtService.findUser("pk@singh.com");
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
			assertNull(foundUser);
							 
	} 
			
	
	
	@Test
	public void testGetUserDetails() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		//assertNull(addedUser.getRoles());
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		

		 
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pk@singh.com","p123",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 User foundUser;
		 
	     try {
	    	   foundUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						@Override
	    		   						public User run() throws BadInputDataException {													
	    		   							return userMgmtService.getUserDetails();
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
							
							assertEquals("Mr.",foundUser.getTitle());
							assertEquals("PK", foundUser.getFirstName());
							assertEquals("Singh",foundUser.getLastName());
							assertEquals("pk@singh.com",foundUser.getEmail());
							assertEquals("pk@singh.com",foundUser.getUserName());
							assertEquals("my lane",foundUser.getAddress().getAddress1());
							assertNull(foundUser.getAddress().getAddress2());
							assertEquals("mycity",foundUser.getAddress().getCity());
							assertEquals("mystate",foundUser.getAddress().getState());
							assertEquals("99999",foundUser.getAddress().getZip());
							assertEquals("USA",foundUser.getAddress().getCountry());
							assertEquals("Mr. PK Singh", foundUser.generateFullName());
							
							assertEquals(false, foundUser.isActivated());
							assertNull(foundUser.getPassword());
							assertEquals(2, foundUser.getPhones().size());
							
							for (Phone phone: foundUser.getPhones()) {
								if(phone.getPhoneType()==PhoneType.HOME) 
									assertEquals("111-111-1111", phone.getPhoneNumber());
								else 
									assertEquals("222-222-2222", phone.getPhoneNumber());
							}
							
							//assertEquals(1, foundUser.getRoles().size());
							
							for (Role r: foundUser.getRoles()) {
								assertEquals("user",r.getRole());
								assertEquals("Roles", r.getRoleGroup());
							}
	 
		
//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser(addedUser.getUserName());
//		utx.commit();
		
		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
			Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
   			 
					@Override
					public Void run(){				
						userManager.removeUser(addedUser.getUserName());
   	   			
   	   			return null;
					}
			});  }	
		finally {
   		   loginContext.logout();
   		   utx.commit();
   	   }
		
			
	}
	
	@Test
	public void testGetUserDetailsWithoutLoggingIn() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		//assertNull(addedUser.getRoles());
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		

	
		try{
		thrown.expect(javax.ejb.EJBAccessException.class);
//		thrown.expectMessage(PropertyManager.getMessage("UserMismatch"));
		
		

	    User foundUser = userMgmtService.getUserDetails();
								
								 
		} finally {
//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser(addedUser.getUserName());
//		utx.commit();
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
			Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
   			 
					@Override
					public Void run(){				
						userManager.removeUser(addedUser.getUserName());
   	   			
   	   			return null;
					}
			});  }	
		finally {
   		   loginContext.logout();
   		   utx.commit();
   	   }

		
		}
			
	}
	
	@Test
	public void testActivateUser() throws Exception {
		
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		User activatedUser; 
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
	     
	     
		assertEquals("Mr.",activatedUser.getTitle());
		assertEquals("PK", activatedUser.getFirstName());
		assertEquals("Singh",activatedUser.getLastName());
		assertEquals("pk@singh.com",activatedUser.getEmail());
		assertEquals("pk@singh.com",activatedUser.getUserName());
		assertEquals("my lane",activatedUser.getAddress().getAddress1());
		assertNull(activatedUser.getAddress().getAddress2());
		assertEquals("mycity",activatedUser.getAddress().getCity());
		assertEquals("mystate",activatedUser.getAddress().getState());
		assertEquals("99999",activatedUser.getAddress().getZip());
		assertEquals("USA",activatedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", activatedUser.generateFullName());
		
		assertEquals(true, activatedUser.isActivated());
		assertNull(activatedUser.getPassword());
		assertEquals(2, activatedUser.getPhones().size());
		
		for (Phone phone: activatedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, activatedUser.getRoles().size());
		
		for (Role r: activatedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
			
		
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							 userManager.removeUser("pk@singh.com");
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();


			
	}
	
	@Test
	public void testActivateUserInvalidKey() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		try{
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("InvalidActivationKey"));
		
		final ActivationDTO activationInfo = new ActivationDTO("12345");
		
		User activatedUser; 
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		
		}
		catch (Exception e) {
		
			if(utx.getStatus()!=6) utx.rollback();
			utx.begin();
			
				 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
				 loginContext.login(); 
				 
			     try {
			    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
			 
			    		   						public Void run(){													
			    		   							 userManager.removeUser("pk@singh.com");
			    		   							 return null;
			    		   							
										
			    		   						} });
			     }  finally {
			 		
			 		loginContext.logout();
			    	 
			     }
			utx.commit();
			throw e;
		}
		
			
	}
	
	@Test
	public void testChangePassword() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		final User activatedUser; 
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		
		assertEquals("Mr.",activatedUser.getTitle());
		assertEquals("PK", activatedUser.getFirstName());
		assertEquals("Singh",activatedUser.getLastName());
		assertEquals("pk@singh.com",activatedUser.getEmail());
		assertEquals("pk@singh.com",activatedUser.getUserName());
		assertEquals("my lane",activatedUser.getAddress().getAddress1());
		assertNull(activatedUser.getAddress().getAddress2());
		assertEquals("mycity",activatedUser.getAddress().getCity());
		assertEquals("mystate",activatedUser.getAddress().getState());
		assertEquals("99999",activatedUser.getAddress().getZip());
		assertEquals("USA",activatedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", activatedUser.generateFullName());
		
		assertEquals(true, activatedUser.isActivated());
		assertNull(activatedUser.getPassword());
		assertEquals(2, activatedUser.getPhones().size());
		
		for (Phone phone: activatedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, activatedUser.getRoles().size());
		
		for (Role r: activatedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		
		String oldPassword = "p123";
		String newPassword = "newp123";
		String retypedPassword = "newp123";
		final PasswordChangeDTO pwdChangeInfo = new PasswordChangeDTO(oldPassword, newPassword, retypedPassword);
		
		User pwdChangedUser;
		final String userName= activatedUser.getUserName();
		
		loginContext = JBossLoginContextFactory.createLoginContext(activatedUser.getUserName(),oldPassword,PropertyManager.getProp("realm")); 
		loginContext.login();
	     try {
	    	   pwdChangedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	               @Override
	               public User run() throws BadInputDataException {							
								return userMgmtService.changePassword(userName, pwdChangeInfo);
								}  
	               } 
		          );
	    		 
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();	     
			 }	finally {
		          loginContext.logout();
		     }
    
	               

		
		assertEquals("Mr.",pwdChangedUser.getTitle());
		assertEquals("PK", pwdChangedUser.getFirstName());
		assertEquals("Singh",pwdChangedUser.getLastName());
		assertEquals("pk@singh.com",pwdChangedUser.getEmail());
		assertEquals("pk@singh.com",pwdChangedUser.getUserName());
		assertEquals("my lane",pwdChangedUser.getAddress().getAddress1());
		assertNull(pwdChangedUser.getAddress().getAddress2());
		assertEquals("mycity",pwdChangedUser.getAddress().getCity());
		assertEquals("mystate",pwdChangedUser.getAddress().getState());
		assertEquals("99999",pwdChangedUser.getAddress().getZip());
		assertEquals("USA",pwdChangedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", pwdChangedUser.generateFullName());
		
		assertEquals(true, pwdChangedUser.isActivated());
		assertNull(pwdChangedUser.getPassword());
		assertEquals(2, pwdChangedUser.getPhones().size());
		
		for (Phone phone: pwdChangedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, pwdChangedUser.getRoles().size());
		
		for (Role r: pwdChangedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User foundUser = userManager.findUser(pwdChangedUser.getUserName());
		
		
		assertEquals("Mr.",foundUser.getTitle());
		assertEquals("PK", foundUser.getFirstName());
		assertEquals("Singh",foundUser.getLastName());
		assertEquals("pk@singh.com",foundUser.getEmail());
		assertEquals("pk@singh.com",foundUser.getUserName());
		assertEquals("my lane",foundUser.getAddress().getAddress1());
		assertNull(foundUser.getAddress().getAddress2());
		assertEquals("mycity",foundUser.getAddress().getCity());
		assertEquals("mystate",foundUser.getAddress().getState());
		assertEquals("99999",foundUser.getAddress().getZip());
		assertEquals("USA",foundUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", foundUser.generateFullName());
		
		assertEquals(true, foundUser.isActivated());
		assertEquals(JBossLoginContextFactory.generateDigestPassword("pk@singh.com", newPassword, PropertyManager.getProp("realm")), foundUser.getPassword());
		assertEquals(2, foundUser.getPhones().size());
		
		for (Phone phone: foundUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, foundUser.getRoles().size());
		
		for (Role r: foundUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		utx.commit();
		
		
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							userManager.removeUser(activatedUser.getUserName());
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();
		
			
	}

	@Test
	public void testChangePasswordInvalidUser() throws Exception {
		String oldPassword = "p123";
		String newPassword = "newp123";
		String retypedPassword = "newp123";
		final PasswordChangeDTO pwdChangeInfo = new PasswordChangeDTO(oldPassword, newPassword, retypedPassword);
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("UserNotFound"));
		
		User pwdChangedUser; 
	
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	 pwdChangedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.changePassword("invalid@singh.com", pwdChangeInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		

		
		
	}
	
	@Test
	public void testChangePasswordIncorrectPassword() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		User activatedUser;
		
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

		
		assertEquals("Mr.",activatedUser.getTitle());
		assertEquals("PK", activatedUser.getFirstName());
		assertEquals("Singh",activatedUser.getLastName());
		assertEquals("pk@singh.com",activatedUser.getEmail());
		assertEquals("pk@singh.com",activatedUser.getUserName());
		assertEquals("my lane",activatedUser.getAddress().getAddress1());
		assertNull(activatedUser.getAddress().getAddress2());
		assertEquals("mycity",activatedUser.getAddress().getCity());
		assertEquals("mystate",activatedUser.getAddress().getState());
		assertEquals("99999",activatedUser.getAddress().getZip());
		assertEquals("USA",activatedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", activatedUser.generateFullName());
		
		assertEquals(true, activatedUser.isActivated());
		assertNull(activatedUser.getPassword());
		assertEquals(2, activatedUser.getPhones().size());
		
		for (Phone phone: activatedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, activatedUser.getRoles().size());
		
		for (Role r: activatedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		String oldPassword = "invalidp";
		String newPassword = "newp123";
		String retypedPassword = "newp123";
		final PasswordChangeDTO pwdChangeInfo = new PasswordChangeDTO(oldPassword, newPassword, retypedPassword);
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("PasswordIncorrect"));
		
		User pwdChangedUser;
		final String userName = activatedUser.getUserName();
		
		loginContext = JBossLoginContextFactory.createLoginContext(activatedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		loginContext.login();
	    try {
		pwdChangedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   public User run() throws BadInputDataException {
	              								return userMgmtService.changePassword(userName, pwdChangeInfo);
																}
	    		    	    			   
	    		   }
	    	   );
	    } catch (PrivilegedActionException pe) {
	    	throw pe.getException();
	    	   	
	    		 
		     } 	     
	     		finally {
		        loginContext.logout();
		  		if(utx.getStatus()!=6) utx.rollback();
				utx.begin();
				
					 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
					 loginContext.login(); 
					 
				     try {
				    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
				 
				    		   						public Void run(){													
				    		   							userManager.removeUser(addedUser.getUserName());
				    		   							 return null;
				    		   							
											
				    		   						} });
				     }  finally {
				 		
				 		loginContext.logout();
				    	 
				     }
				utx.commit();

		     }
	        
	}

	@Test
	public void testChangePasswordMismatchPassword() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		User activatedUser; // = userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

		
		assertEquals("Mr.",activatedUser.getTitle());
		assertEquals("PK", activatedUser.getFirstName());
		assertEquals("Singh",activatedUser.getLastName());
		assertEquals("pk@singh.com",activatedUser.getEmail());
		assertEquals("pk@singh.com",activatedUser.getUserName());
		assertEquals("my lane",activatedUser.getAddress().getAddress1());
		assertNull(activatedUser.getAddress().getAddress2());
		assertEquals("mycity",activatedUser.getAddress().getCity());
		assertEquals("mystate",activatedUser.getAddress().getState());
		assertEquals("99999",activatedUser.getAddress().getZip());
		assertEquals("USA",activatedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", activatedUser.generateFullName());
		
		assertEquals(true, activatedUser.isActivated());
		assertNull(activatedUser.getPassword());
		assertEquals(2, activatedUser.getPhones().size());
		
		for (Phone phone: activatedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, activatedUser.getRoles().size());
		
		for (Role r: activatedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		String oldPassword = "p123";
		String newPassword = "newp123";
		String retypedPassword = "newpmismatch";
		final PasswordChangeDTO pwdChangeInfo = new PasswordChangeDTO(oldPassword, newPassword, retypedPassword);
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("PasswordMismatch"));
		
		User pwdChangedUser; 
		final String userName = activatedUser.getUserName();
		
		
		loginContext = JBossLoginContextFactory.createLoginContext(activatedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		loginContext.login();
	    try {
		pwdChangedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   public User run() throws BadInputDataException {
	              								return userMgmtService.changePassword(userName, pwdChangeInfo);
																}
	    		    	    			   
	    		   }
	    	   );
	    } catch (PrivilegedActionException pe) {
	    	throw pe.getException();	    	   	
	    		 
		} 	     
	    finally {
		loginContext.logout();
  		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							userManager.removeUser(addedUser.getUserName());
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();
		}
		
			
	}
	
	@Test
	public void testAddAdministrator() throws Exception {
		final AdminUserDTO user = new AdminUserDTO();
		user.setTitle("Mr.");
		user.setFirstName("Veer");
		user.setLastName("Muchandi");
		user.setEmail("veer.muchandi@gmail.com");
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser; 
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	 addedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.addAdministrator(user);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("Veer", addedUser.getFirstName());
		assertEquals("Muchandi",addedUser.getLastName());
		assertEquals("veer.muchandi@gmail.com",addedUser.getEmail());
		assertEquals("veer.muchandi@gmail.com",addedUser.getUserName());
		assertNull(addedUser.getAddress());
		assertEquals("Mr. Veer Muchandi", addedUser.generateFullName());
		
		assertEquals(true, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("admin",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User foundUser = userManager.findUser(addedUser.getUserName());
		
		
		assertEquals("Mr.",foundUser.getTitle());
		assertEquals("Veer", foundUser.getFirstName());
		assertEquals("Muchandi",foundUser.getLastName());
		assertEquals("veer.muchandi@gmail.com",foundUser.getEmail());
		assertEquals("veer.muchandi@gmail.com",foundUser.getUserName());
		assertNull(foundUser.getAddress());
		assertEquals("Mr. Veer Muchandi", foundUser.generateFullName());
		
		assertNotNull(foundUser.getPassword());
	
		assertEquals(2, foundUser.getPhones().size());
		
		for (Phone phone: foundUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, foundUser.getRoles().size());
		
		for (Role r: foundUser.getRoles()) {
			assertEquals("admin",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		utx.commit();
		
	
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
				
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login();
			 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							 userManager.removeUser(addedUser.getUserName());
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		          loginContext.logout();
		     }
		utx.commit();	
	}
	
	
	@Test
	public void testAddAdministratorAlreadyExists() throws Exception {
		final AdminUserDTO user = new AdminUserDTO();
		user.setTitle("Mr.");
		user.setFirstName("Veer");
		user.setLastName("Muchandi");
		user.setEmail("veer.muchandi@gmail.com");
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser;
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	 addedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.addAdministrator(user);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("Veer", addedUser.getFirstName());
		assertEquals("Muchandi",addedUser.getLastName());
		assertEquals("veer.muchandi@gmail.com",addedUser.getEmail());
		assertEquals("veer.muchandi@gmail.com",addedUser.getUserName());
		assertNull(addedUser.getAddress());
		assertEquals("Mr. Veer Muchandi", addedUser.generateFullName());
		
		assertEquals(true, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("admin",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		try{
			thrown.expect(BadInputDataException.class);
			thrown.expectMessage(PropertyManager.getMessage("AlreadyAdmin", user.getEmail()));
			
			final User againAdded; // = userMgmtService.addAdministrator(user);
			loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			loginContext.login(); 
			 
		     try {
		    	 againAdded = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
		 
		    		   						public User run() throws BadInputDataException{													
		    		   							return userMgmtService.addAdministrator(user);
		    		   							     		   							
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		    	 
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }

		}
		catch (Exception e) {
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
				
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login();
			 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							userManager.removeUser("veer.muchandi@gmail.com");
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		          loginContext.logout();
		     }
		utx.commit();	

		
		throw e;
		}
	}
	
	@Test
	public void testAddAdministratorRoleToCurrentUser() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		final User activatedUser; // = userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	 activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

		
		
		final AdminUserDTO admin = new AdminUserDTO();
		admin.setEmail(addedUser.getEmail());
		admin.setUserName(addedUser.getUserName());
		
		final User addedAdmin;
		
		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	 addedAdmin = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.addAdministrator(admin);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		
		assertEquals("Mr.",addedAdmin.getTitle());
		assertEquals("PK", addedAdmin.getFirstName());
		assertEquals("Singh",addedAdmin.getLastName());
		assertEquals("pk@singh.com",addedAdmin.getEmail());
		assertEquals("pk@singh.com",addedAdmin.getUserName());
		assertEquals("my lane",addedAdmin.getAddress().getAddress1());
		assertNull(addedAdmin.getAddress().getAddress2());
		assertEquals("mycity",addedAdmin.getAddress().getCity());
		assertEquals("mystate",addedAdmin.getAddress().getState());
		assertEquals("99999",addedAdmin.getAddress().getZip());
		assertEquals("USA",addedAdmin.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedAdmin.generateFullName());
		
		assertEquals(true, addedAdmin.isActivated());
		assertNull(addedAdmin.getPassword());
		assertEquals(2, addedAdmin.getPhones().size());
		
		for (Phone phone: addedAdmin.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(2, addedAdmin.getRoles().size());
		
		for (Role r: addedAdmin.getRoles()) {
		if(r.getRole().equals("user")){
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		} else
			assertEquals("admin",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		
//		if(utx.getStatus()!=6) utx.rollback();
//		utx.begin();
//			userManager.removeUser("pk@singh.com");
//		utx.commit();
		
			
		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm"));
			 loginContext.login();
			 
			 
		     try {
		    	 if(utx.getStatus()!=6) utx.rollback();
					utx.begin();
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		 
		    		   						public Void run(){	
		    		   							
		    		   							 userManager.removeUser("pk@singh.com");	    		   							
		    		   							 return null;
		    		   							
									
		    		   						} });
		    	   utx.commit();
		     }  finally {
		          loginContext.logout();
		     }
		     
		     		
		
	}
	
	@Test
	public void testFindAllUsers() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		

		User user1 = new User();
		user1.setTitle("Mr.");
		user1.setFirstName("Nokay");
		user1.setLastName("Singh");
		user1.setEmail("nokay@singh.com");
		user1.setPassword("nokay123");
		Address address1 = new Address();
		address1.setAddress1("my lane");
		address1.setCity("mycity");
		address1.setState("mystate");
		address1.setZip("99999");
		user1.setAddress(address1);
		user1.setActivated(true);
		
		final User addedUser1 = userMgmtService.signup(user1);
		
		assertEquals("nokay@singh.com",addedUser1.getEmail());
		assertEquals("nokay@singh.com",addedUser1.getUserName());
		
		final ActivationDTO activationInfo = new ActivationDTO("ee69e");
		
		User activatedUser;// = userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

		
		
		final AdminUserDTO admin = new AdminUserDTO();
		admin.setEmail(addedUser1.getEmail());
		admin.setUserName(addedUser1.getUserName());
		
		final User addedAdmin; // = userMgmtService.addAdministrator(admin);
		 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	 addedAdmin = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.addAdministrator(admin);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		
		
		
		try{
		
		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 List<User> usersList;
		 
	     try {
	    	    	 
	    	   usersList = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	 
	    		   						@Override
	    		   						public List<User> run() throws BadInputDataException {													
	    		   							return userMgmtService.findAllUsers();
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	    	 		
//			assertEquals(3, usersList.size());
			for (User a : usersList) {
				log.info("**** User Name:"+a.getUserName());
				if(addedUser.getUserName().equals(a.getUserName())) {
					assertEquals(addedUser.generateFullName(), a.generateFullName());
				} else if(addedAdmin.getUserName().equals(a.getUserName())){
					assertEquals(addedAdmin.generateFullName(), a.generateFullName());
					}
					else	{
								assertEquals("Mr. Admin Singh", a.generateFullName());
				}
			}
	     							 
		} finally {
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
	    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
	    			 
						@Override
						public Void run(){				
	    			   	userManager.removeUser(addedUser.getUserName());
	    	   			userManager.removeUser(addedAdmin.getUserName());
	    	   			return null;
						}
	    	   });  
	    	   
			}	finally {
	    		   loginContext.logout();
	    		   utx.commit();
	    	   }
	 	          
	 	     }
		
	}

	@Test
	public void testFindAllUsersByNonAdmin() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		assertEquals("Mr.",addedUser.getTitle());
		assertEquals("PK", addedUser.getFirstName());
		assertEquals("Singh",addedUser.getLastName());
		assertEquals("pk@singh.com",addedUser.getEmail());
		assertEquals("pk@singh.com",addedUser.getUserName());
		assertEquals("my lane",addedUser.getAddress().getAddress1());
		assertNull(addedUser.getAddress().getAddress2());
		assertEquals("mycity",addedUser.getAddress().getCity());
		assertEquals("mystate",addedUser.getAddress().getState());
		assertEquals("99999",addedUser.getAddress().getZip());
		assertEquals("USA",addedUser.getAddress().getCountry());
		assertEquals("Mr. PK Singh", addedUser.generateFullName());
		
		assertEquals(false, addedUser.isActivated());
		assertNull(addedUser.getPassword());
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		assertEquals(1, addedUser.getRoles().size());
		
		for (Role r: addedUser.getRoles()) {
			assertEquals("user",r.getRole());
			assertEquals("Roles", r.getRoleGroup());
		}
		

		User user1 = new User();
		user1.setTitle("Mr.");
		user1.setFirstName("Nokay");
		user1.setLastName("Singh");
		user1.setEmail("nokay@singh.com");
		user1.setPassword("nokay123");
		Address address1 = new Address();
		address1.setAddress1("my lane");
		address1.setCity("mycity");
		address1.setState("mystate");
		address1.setZip("99999");
		user1.setAddress(address1);
		user1.setActivated(true);
		
		final User addedUser1 = userMgmtService.signup(user1);
		
		assertEquals("nokay@singh.com",addedUser1.getEmail());
		assertEquals("nokay@singh.com",addedUser1.getUserName());
		
		final ActivationDTO activationInfo = new ActivationDTO("ee69e");
		
		final User activatedUser; // = userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

//		AdminUserDTO admin = new AdminUserDTO();
//		admin.setEmail(addedUser1.getEmail());
//		admin.setUserName(addedUser1.getUserName());
//		
//		final User addedAdmin = userMgmtService.addAdministrator(admin);

		
		
		
		try{
		thrown.expect(javax.ejb.EJBAccessException.class);
		
		 loginContext = JBossLoginContextFactory.createLoginContext(activatedUser.getUserName(),"nokay123",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 List<User> usersList;
		 
	     try {
	    	 
	    	 loginContext.getSubject().getPublicCredentials().add(JBossLoginContextFactory.generateDigestPassword(activatedUser.getUserName(), "nokay123",PropertyManager.getProp("realm")));
	    	 
	    	   usersList = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	 
	    		   						@Override
	    		   						public List<User> run() throws BadInputDataException {													
	    		   							return userMgmtService.findAllUsers();
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	    	 		
			assertEquals(3, usersList.size());
			for (User a : usersList) {
				if(addedUser.getUserName().equals(a.getUserName())) {
					assertEquals(addedUser.generateFullName(), a.generateFullName());
				} else if(activatedUser.getUserName().equals(a.getUserName())){
					assertEquals(activatedUser.generateFullName(), a.generateFullName());
					}
					else	{
								assertEquals("Mr. Admin Singh", a.generateFullName());
				}
			}
	     							 
		} finally {
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
	    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
	    			 
						@Override
						public Void run(){				
	    			   	userManager.removeUser(addedUser.getUserName());
	    	   			userManager.removeUser(activatedUser.getUserName());
	    	   			return null;
						}
	    	   });  
	    	   
			}	finally {
	    		   loginContext.logout();
	    		   utx.commit();
	    	   }
	 	          
	 	     }
		
	}

	@Test
	public void testFindUsers() throws Exception {
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		

		User user1 = new User();
		user1.setTitle("Mr.");
		user1.setFirstName("Nokay");
		user1.setLastName("Singh");
		user1.setEmail("nokay@singh.com");
		user1.setPassword("nokay123");
		Address address1 = new Address();
		address1.setAddress1("my lane");
		address1.setCity("mycity");
		address1.setState("mystate");
		address1.setZip("99999");
		user1.setAddress(address1);
		user1.setActivated(true);
		
		final User addedUser1 = userMgmtService.signup(user1);
			
		final ActivationDTO activationInfo = new ActivationDTO("ee69e");
		
		final User activatedUser; // = userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
		
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser1.getUserName(),"nokay123",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser1.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
		
		final AdminUserDTO admin = new AdminUserDTO();
		admin.setEmail(addedUser1.getEmail());
		admin.setUserName(addedUser1.getUserName());
		
		final User addedAdmin; // = userMgmtService.addAdministrator(admin);

		loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login(); 
		 
	     try {
	    	 addedAdmin = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.addAdministrator(admin);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }

		
		
		try{
		
//		 loginContext = JBossLoginContextFactory.createLoginContext(addedAdmin.getUserName(),"nokay123",PropertyManager.getProp("realm")); 
			loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		loginContext.login();
		 List<User> usersList;
		 
	     try {
	    	 
	    	 loginContext.getSubject().getPublicCredentials().add(JBossLoginContextFactory.generateDigestPassword(addedAdmin.getUserName(), "nokay123",PropertyManager.getProp("realm")));
	    	 
	    	   usersList = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	 
	    		   						@Override
	    		   						public List<User> run() throws BadInputDataException {													
	    		   							return userMgmtService.findUsers("NoKay", null, null);
								
	    		   						} });
	    	   assertEquals(1,usersList.size());
	    	   
	    	   usersList = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	    			 
						@Override
						public List<User> run() throws BadInputDataException {													
							return userMgmtService.findUsers(null, "Singh", null);
			
						} });
	    	   assertEquals(3,usersList.size());
	    	   
	    	   usersList = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	    			 
						@Override
						public List<User> run() throws BadInputDataException {													
							return userMgmtService.findUsers(null, null, "singh.com");
			
						} });
	    	   assertEquals(2,usersList.size());

	    	   usersList = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	    			 
						@Override
						public List<User> run() throws BadInputDataException {													
							return userMgmtService.findUsers(null, "Singh", "singh.com");
			
						} });
	    	   
	    	   usersList = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	    			 
						@Override
						public List<User> run() throws BadInputDataException {													
							return userMgmtService.findUsers("Nokay", "Singh", null);
			
						} });
	    	   
	    	   assertEquals(1,usersList.size());
	    	   
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
	     thrown.expect(javax.ejb.EJBAccessException.class);
	     userMgmtService.findUsers(null, "Singh", null);
	          							 
		} finally {
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		try {
	    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
	    			 
						@Override
						public Void run(){				
	    			   	userManager.removeUser(addedUser.getUserName());
	    	   			userManager.removeUser(addedAdmin.getUserName());
	    	   			return null;
						}
	    	   });  
	    	   
			}	finally {
	    		   loginContext.logout();
	    		   utx.commit();
	    	   }
	 	          
	 	     }
		
	}
	
	@Test
	public void testUpdateUser() throws Exception {
		
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		final User activatedUser; 
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
	     
		
	     
	     activatedUser.setTitle("Mrs.");
	     activatedUser.setFirstName("PiK");
	     activatedUser.setLastName("Singhress");
	     address.setAddress2("line 2");
	     address.setCity("New City");
	     activatedUser.setAddress(address);
		 for (Phone phone: activatedUser.getPhones()) {
				if(phone.getPhoneType()==PhoneType.HOME) 
					phone.setPhoneNumber("111-111-3333");
		}
	     
		 User updatedUser;
		 
	        loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
			loginContext.login(); 
			 
		     try {
		    	   updatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
		 
		    		   						public User run() throws BadInputDataException{													
		    		   							return userMgmtService.updateUser(addedUser.getUserName(), activatedUser);
		    		   							     		   							
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		    	 
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		     
		     
				assertEquals("Mrs.",updatedUser.getTitle());
				assertEquals("PiK", updatedUser.getFirstName());
				assertEquals("Singhress",updatedUser.getLastName());
				assertEquals("pk@singh.com",updatedUser.getEmail());
				assertEquals("pk@singh.com",updatedUser.getUserName());
				assertEquals("my lane",updatedUser.getAddress().getAddress1());
				assertEquals("line 2",updatedUser.getAddress().getAddress2());
				assertEquals("New City",updatedUser.getAddress().getCity());
				assertEquals("mystate",updatedUser.getAddress().getState());
				assertEquals("99999",updatedUser.getAddress().getZip());
				assertEquals("USA",updatedUser.getAddress().getCountry());
				assertEquals("Mrs. PiK Singhress", updatedUser.generateFullName());
				
				assertEquals(true, updatedUser.isActivated());
				assertNull(updatedUser.getPassword());
				assertEquals(2, updatedUser.getPhones().size());
				
				for (Phone phone: updatedUser.getPhones()) {
					if(phone.getPhoneType()==PhoneType.HOME) 
						assertEquals("111-111-3333", phone.getPhoneNumber());
					else 
						assertEquals("222-222-2222", phone.getPhoneNumber());
				}
				
				assertEquals(1, updatedUser.getRoles().size());
				
				for (Role r: updatedUser.getRoles()) {
					assertEquals("user",r.getRole());
					assertEquals("Roles", r.getRoleGroup());
				}
			
		
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							 userManager.removeUser("pk@singh.com");
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();


			
	}

	@Test
	public void testUpdateUserUserMismatch() throws Exception {
		
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		final User activatedUser; 
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
	     
		
	     
	     activatedUser.setTitle("Mrs.");
	     activatedUser.setFirstName("PiK");
	     activatedUser.setLastName("Singhress");
	     address.setAddress2("line 2");
	     address.setCity("New City");
	     activatedUser.setAddress(address);
		 for (Phone phone: activatedUser.getPhones()) {
				if(phone.getPhoneType()==PhoneType.HOME) 
					phone.setPhoneNumber("111-111-3333");
		}
	     
		 
		 try{
				thrown.expect(BadInputDataException.class);
				thrown.expectMessage(PropertyManager.getMessage("UserMismatch"));
				
				User updatedUser;
		       loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		    	loginContext.login(); 
			 
		     try {
		    	   updatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
		 
		    		   						public User run() throws BadInputDataException{													
		    		   							return userMgmtService.updateUser(addedUser.getUserName(), activatedUser);
		    		   							     		   							
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		    	 
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		 } catch (Exception e) {
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							 userManager.removeUser("pk@singh.com");
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();
		throw e;
		 }

			
	}
	
	@Test
	public void testUpdateUserUserNull() throws Exception {
		
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		final User activatedUser; 
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
	     
		
	     
//	     activatedUser.setTitle("Mrs.");
//	     activatedUser.setFirstName("PiK");
//	     activatedUser.setLastName("Singhress");
//	     address.setAddress2("line 2");
//	     address.setCity("New City");
//	     activatedUser.setAddress(address);
//		 for (Phone phone: activatedUser.getPhones()) {
//				if(phone.getPhoneType()==PhoneType.HOME) 
//					phone.setPhoneNumber("111-111-3333");
//		}
	     
		 
		 try{
				thrown.expect(BadInputDataException.class);
				thrown.expectMessage(PropertyManager.getMessage("UserNull"));
				
				User updatedUser;
		       loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm"));
		    	loginContext.login(); 
			 
		     try {
		    	   updatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
		 
		    		   						public User run() throws BadInputDataException{													
		    		   							return userMgmtService.updateUser(addedUser.getUserName(), null);
		    		   							     		   							
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		    	 
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		 } catch (Exception e) {
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							 userManager.removeUser("pk@singh.com");
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();
		throw e;
		 }

			
	}
	
	@Test
	public void testUpdateUserUserInactive() throws Exception {
		
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
//		final User activatedUser; 
//		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
//		loginContext.login(); 
//		 
//	     try {
//	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
//	 
//	    		   						public User run() throws BadInputDataException{													
//	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
//	    		   							     		   							
//								
//	    		   						} });
//	     } catch (PrivilegedActionException pe) {
//	    	 throw pe.getException();
//	    	 
//	     }  finally {
//	 		
//	 		loginContext.logout();
//	    	 
//	     }
	     
		
	     
	     addedUser.setTitle("Mrs.");
	     addedUser.setFirstName("PiK");
	     addedUser.setLastName("Singhress");
	     address.setAddress2("line 2");
	     address.setCity("New City");
	     addedUser.setAddress(address);
		 for (Phone phone: addedUser.getPhones()) {
				if(phone.getPhoneType()==PhoneType.HOME) 
					phone.setPhoneNumber("111-111-3333");
		}
	     
		 
		 try{
				thrown.expect(BadInputDataException.class);
				thrown.expectMessage(PropertyManager.getMessage("UserNotActive"));
				
				User updatedUser;
		       LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm"));
		    	loginContext.login(); 
			 
		     try {
		    	   updatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
		 
		    		   						public User run() throws BadInputDataException{													
		    		   							return userMgmtService.updateUser(addedUser.getUserName(), addedUser);
		    		   							     		   							
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		    	 
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		 } catch (Exception e) {
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							 userManager.removeUser("pk@singh.com");
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();
		throw e;
		 }

			
	}
	
	@Test
	public void testUpdateUserInconsistentId() throws Exception {
		
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		user.setPassword("p123");
		Address address = new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		Phone phone1 = new Phone(), phone2 = new Phone();
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);	
		
		final User addedUser = userMgmtService.signup(user);
		
		
		final ActivationDTO activationInfo = new ActivationDTO("8b21a5");
		
		final User activatedUser; 
		LoginContext loginContext = JBossLoginContextFactory.createLoginContext(addedUser.getUserName(),"p123",PropertyManager.getProp("realm")); 
		loginContext.login(); 
		 
	     try {
	    	   activatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
	 
	    		   						public User run() throws BadInputDataException{													
	    		   							return userMgmtService.activateUser(addedUser.getUserName(), activationInfo);
	    		   							     		   							
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	    	 
	     }  finally {
	 		
	 		loginContext.logout();
	    	 
	     }
	     
		
	     addedUser.setUserName("piksinghress@singh.com");
	     
	     addedUser.setTitle("Mrs.");
	     addedUser.setFirstName("PiK");
	     addedUser.setLastName("Singhress");
	     address.setAddress2("line 2");
	     address.setCity("New City");
	     addedUser.setAddress(address);
		 for (Phone phone: addedUser.getPhones()) {
				if(phone.getPhoneType()==PhoneType.HOME) 
					phone.setPhoneNumber("111-111-3333");
		}
	     
		 
		 try{
				thrown.expect(BadInputDataException.class);
				thrown.expectMessage(PropertyManager.getMessage("InconsistentId", "pk@singh.com", addedUser.getUserName()));
				
				User updatedUser;
		        loginContext = JBossLoginContextFactory.createLoginContext("pk@singh.com","p123",PropertyManager.getProp("realm"));
		    	loginContext.login(); 
			 
		     try {
		    	   updatedUser = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<User>() {
		 
		    		   						public User run() throws BadInputDataException{													
		    		   							return userMgmtService.updateUser("pk@singh.com", addedUser);
		    		   							     		   							
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		    	 
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		 } catch (Exception e) {
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
			 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
			 loginContext.login(); 
			 
		     try {
		    	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		 
		    		   						public Void run(){													
		    		   							 userManager.removeUser("pk@singh.com");
		    		   							 return null;
		    		   							
									
		    		   						} });
		     }  finally {
		 		
		 		loginContext.logout();
		    	 
		     }
		utx.commit();
		throw e;
		 }

			
	}
	
}
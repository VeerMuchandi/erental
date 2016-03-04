/**
 * 
 */
package com.pinaka.UserManagement.test;

import static org.junit.Assert.*;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.inject.Any;
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
import com.pinaka.UserManagement.User_;
import com.pinaka.eRental.model.PenaltyTerm;
import com.pinaka.eRental.model.RejectionReasonType;
import com.pinaka.eRental.model.RentalCost;
import com.pinaka.eRental.model.RentalInstance;
import com.pinaka.eRental.model.RentalItem;
import com.pinaka.eRental.model.RentalLocation;
import com.pinaka.eRental.model.RentalPeriodType;
import com.pinaka.eRental.model.RentalStatusType;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.util.PropertyManager;
import com.pinaka.eRental.util.Resources;
import com.pinaka.eRental.exception.*;
import com.pinaka.testutil.JBossLoginContextFactory;

/**
 * @author Muchandi
 *
 */
@RunWith(Arquillian.class)
public class UserManagerTest {
	@Deployment
	   public static Archive<?> createTestArchive() {
	      return ShrinkWrap.create(WebArchive.class, "test.war")
	            .addClasses( 
	            		User.class,
	            		User_.class,
	            		RentalUser.class,
	            		Role.class,
	            		Phone.class,
	            		PhoneType.class, 
	            		Address.class,
	            		UserManager.class,
	            		UserManagerBean.class, 
	            		RentalItem.class, 
	            		PenaltyTerm.class, 
	            		RentalCost.class,
	            		RentalPeriodType.class, 
	            		RentalInstance.class, 
	            		PenaltyTerm.class, 
	            		RentalLocation.class, 
	            		RentalStatusType.class, 
	            		BadInputDataException.class, 
	            		RejectionReasonType.class, 
	            		PropertyManager.class, 
	            		StringEscapeUtils.class,
	            		NestableRuntimeException.class, 
	            		Nestable.class, 
	            		NestableDelegate.class,
	            		Resources.class,
	            		JBossLoginContextFactory.class)
	            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
	            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
	            .addAsResource("ExceptionMessages.properties");
	   }

	@Inject 
	UserManager userManager;
	@Inject
	Address address;
	@Inject
	Phone phone1,phone2;
	@Inject
	Logger log;
	@Inject
	UserTransaction utx;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testNullUser() throws Exception {
		User user = null;
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("UserNull"));
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		userManager.addUser(user);
		utx.rollback();
	}

		
	@Test
	public void testAddFindUpdateAndRemoveUser() throws Exception {
		//user.setUserName("pk@singh.com");
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		user.setActivated(true);
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("111-111-1111");
		Set<Phone> phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("222-222-2222");
		phones.add(phone2);
		user.setPhones(phones);
		

		final User addedUser=userManager.addUser(user);
		utx.commit();
		
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
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User foundUser = userManager.findUser(addedUser.getUserName());
		utx.commit();
		
		assertNotNull(foundUser.getUserName());
		assertEquals("PK", foundUser.getFirstName());
		
		foundUser.setFirstName("Peekay");
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User updatedUser = userManager.updateUser(foundUser);
		utx.commit();
		
		assertNotNull(updatedUser.getUserName());
		assertEquals("Peekay", updatedUser.getFirstName());
		

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

		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		assertNull(userManager.findUser(addedUser.getUserName()));
		utx.commit();
		
	}
	
	@Test 
	public void testAddFindUsers() throws Exception {
		//user.setUserName("pk@singh.com");
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		
		User user = new User();
		user.setFirstName("PK");
		user.setLastName("Singh");
		user.setEmail("pk@singh.com");
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
		user.setActivated(true);
		

		final User addedUser=userManager.addUser(user);
		utx.commit();
		
		assertNotNull(addedUser.getUserName());
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
		assertEquals(2, addedUser.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("111-111-1111", phone.getPhoneNumber());
			else 
				assertEquals("222-222-2222", phone.getPhoneNumber());
		}
		
		User user2 = new User();
		user2.setFirstName("Remove");
		user2.setLastName("Singh");
		user2.setEmail("remove@me.com");
		address.setAddress1("no lane");
		address.setCity("no city");
		address.setState("nostate");
		address.setZip("11111");
		user2.setAddress(address);
		
		phone1.setPhoneType(PhoneType.HOME);
		phone1.setPhoneNumber("333-333-3333");
		phones = new HashSet<Phone>();
		phones.add(phone1);
		phone2.setPhoneType(PhoneType.MOBILE);
		phone2.setPhoneNumber("444-444-4444");
		phones.add(phone2);
		user2.setPhones(phones);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		final User addedUser2=userManager.addUser(user2);
		utx.commit();
		
		assertNotNull(addedUser2.getUserName());
		assertEquals("Remove", addedUser2.getFirstName());
		assertEquals("Singh",addedUser2.getLastName());
		assertEquals("remove@me.com",addedUser2.getEmail());
		assertEquals("remove@me.com",addedUser2.getUserName());
		assertEquals("no lane",addedUser2.getAddress().getAddress1());
		assertNull(addedUser2.getAddress().getAddress2());
		assertEquals("no city",addedUser2.getAddress().getCity());
		assertEquals("nostate",addedUser2.getAddress().getState());
		assertEquals("11111",addedUser2.getAddress().getZip());
		assertEquals("USA",addedUser2.getAddress().getCountry());
		assertEquals(2, addedUser2.getPhones().size());
		
		for (Phone phone: addedUser.getPhones()) {
			if(phone.getPhoneType()==PhoneType.HOME) 
				assertEquals("333-333-3333", phone.getPhoneNumber());
			else 
				assertEquals("444-444-4444", phone.getPhoneNumber());
		}
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		List<User> usersList = userManager.findAllUsers();
		assertEquals(3, usersList.size());
		for (User a : usersList) {
			log.info("user-name:"+a.getUserName());
		if(addedUser.getUserName().equals(a.getUserName())) {
				assertEquals(addedUser.generateFullName(), a.generateFullName());
			} else	{
				if (addedUser2.getUserName().equals(a.getUserName())) {
					assertEquals(addedUser2.getUserName(), a.getUserName()); }
				else
					assertEquals("pinakallc@gmail.com", a.getUserName());
			}
		}

	
		usersList = userManager.findUsers("PK", null);
		assertEquals(1, usersList.size());
		for (User a : usersList) {
		assertEquals(addedUser.getUserName(), a.getUserName());
		}
		
		usersList = userManager.findUsers(null, "Singh");
		assertEquals(2, usersList.size());
		for (User a : usersList) {
			if(addedUser.getUserName().equals(a.getUserName())) {
				assertEquals(addedUser.generateFullName(), a.generateFullName());
			} else	{
				assertEquals(addedUser2.getUserName(), a.getUserName());
			}
		}
		
		usersList = userManager.findUsers("Remove", "Singh");
		assertEquals(1, usersList.size());
		for (User a : usersList) {
		assertEquals(addedUser2.getUserName(), a.getUserName());
		}
		
		usersList = userManager.findUserBySomeValue("PK");
		assertEquals(1, usersList.size());
		for (User a : usersList) {
		assertEquals(addedUser.getUserName(), a.getUserName());
		}
		
		usersList = userManager.findUserBySomeValue("Sin");
		assertEquals(2, usersList.size());
		for (User a : usersList) {
		if(addedUser.getUserName().equals(a.getUserName())) {
			assertEquals(addedUser.generateFullName(), a.generateFullName());
		} else	{
			assertEquals(addedUser2.getUserName(), a.getUserName());
		}
		}
		
		usersList = userManager.findUserBySomeValue("@");
		assertEquals(3, usersList.size());
		for (User a : usersList) {
			if(addedUser.getUserName().equals(a.getUserName())) {
				assertEquals(addedUser.generateFullName(), a.generateFullName());
			} else	{
				if (addedUser2.getUserName().equals(a.getUserName())) {
					assertEquals(addedUser2.getUserName(), a.getUserName()); }
				else
					assertEquals("pinakallc@gmail.com", a.getUserName());
			}
		}
		
//		User a = userManager.findUser("pk@singh.com");
//		assertEquals(addedUser.getUserName(), a.getUserName());
//		
//		thrown.expect(BadInputDataException.class);
//		thrown.expectMessage(PropertyManager.getMessage("InputNull", "FirstName and LastName"));
//		usersList = userManager.findUsers(null, null);

//		utx.rollback();
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 
		 if(utx.getStatus()!=6) utx.rollback();
		 utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		    			 
							@Override
							public Void run(){				
								userManager.removeUser(addedUser.getUserName());
								userManager.removeUser(addedUser2.getUserName());
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
		    		   
			  utx.commit();
			  loginContext.logout(); }

	}
	
	@Test
	public void testFindUsersWithNoInputs() throws Exception{
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("InputNull", "FirstName and LastName"));
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		List<User> usersList = userManager.findUsers(null, null);
		utx.rollback();
	}
	
	@Before 
	public void beforeTesting() throws Exception {
		log.info("in Before");
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User user = new User();
		user.setTitle("Mr.");
		user.setFirstName("Admin");
		user.setLastName("Istrator");
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
}

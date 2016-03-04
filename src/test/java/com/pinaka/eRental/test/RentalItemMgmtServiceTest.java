/**
 *
 * @filename RentalItemMgmtServiceTest.java
 * @author Veer Muchandi
 * @created Sep 17, 2012
 * © Copyright 2012 Pinaka LLC
 *
 */
package com.pinaka.eRental.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.inject.New;
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
//import com.pinaka.eRental.model.Rental;
import com.pinaka.eRental.model.RejectionReasonType;
import com.pinaka.eRental.model.RentalCost;
import com.pinaka.eRental.model.RentalInstance;
import com.pinaka.eRental.model.RentalItem;
import com.pinaka.eRental.model.RentalLocation;
import com.pinaka.eRental.model.RentalPeriodType;
import com.pinaka.eRental.model.RentalStatusType;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.service.RentalItemManager;
import com.pinaka.eRental.service.RentalItemManagerBean;
import com.pinaka.eRental.service.RentalUserManager;
import com.pinaka.eRental.service.RentalUserManagerBean;
import com.pinaka.eRental.serviceFacade.RentalItemMgmtService;
import com.pinaka.eRental.serviceFacade.RentalItemMgmtServiceBean;
import com.pinaka.eRental.serviceFacade.RentalUserMgmtService;
import com.pinaka.eRental.serviceFacade.RentalUserMgmtServiceBean;
import com.pinaka.eRental.util.EmailProcessor;
import com.pinaka.eRental.util.PropertyManager;
import com.pinaka.eRental.util.Resources;
import com.pinaka.testutil.JBossLoginContextFactory;

/**
 * @filename RentalItemMgmtServiceTest.java
 * @author Veer Muchandi
 * @created Sep 17, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
@RunWith(Arquillian.class)
public class RentalItemMgmtServiceTest {

	@Deployment
	public static Archive<?> createTestArchive() {

		final String WEBAPP_SRC = "src/main/webapp";  
	    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
		            .addClasses(UserMgmtServiceBean.class, 
		            		User.class,
		            		User_.class,
		            		AdminUserDTO.class,
		            		Role.class,
		            		Phone.class,
		            		PhoneType.class, 
		            		Address.class,
		            		UserManager.class,
		            		UserManagerBean.class, 
		            		UserMgmtServiceBean.class,
		            		RentalUserMgmtService.class,
		            		RentalUserMgmtServiceBean.class,
		            		RentalItem.class,
		            		RentalItemManager.class,
		            		RentalItemManagerBean.class,
		            		RentalItemMgmtService.class,
		            		RentalItemMgmtServiceBean.class,
		            		RentalUserManager.class,
		            		RentalUserManagerBean.class,
		            		RentalUser.class,
		            		RentalInstance.class,
		            		RentalStatusType.class,
		            		RentalLocation.class,
		            		RentalCost.class,
		            		RentalPeriodType.class,
		            		PenaltyTerm.class,
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
	RentalItemManager rentalItemManager;
	@New
	UserMgmtService userMgmtService;
	@Inject
	RentalItemMgmtService rentalItemMgmtService;
	@Inject
	UserManager userManager;
	@Inject
	RentalUserManager rentalUserManager;
	@Inject
	UserTransaction utx;
	@Inject
	Logger log;
	@Inject
	PropertyManager pm;
	@New
	User owner;
	
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
	public void testAddUpdateRentalItem() throws Exception {
		
		owner = this.addOwner();
		
		final RentalItem rentalItem = new RentalItem();
		rentalItem.setName("Plier");
		rentalItem.setItemCategory("Metal");
		rentalItem.setItemSubCategory("Tool");
		rentalItem.setItemBrand("Bosch");
		rentalItem.setAvailableFrom(null);
		rentalItem.setConsolidatedRating(0);
		rentalItem.setEnableRenting(false);
		rentalItem.setItemAge(0);
		rentalItem.setPastRentalCount(0);
		rentalItem.setReplacementCost(0);
		
		RentalCost rentalCost = new RentalCost();
		rentalCost.setCost(0.50);
		rentalCost.setPeriod(RentalPeriodType.HOUR);
		RentalCost rentalCost1 = new RentalCost();
		rentalCost1.setCost(5.00);
		rentalCost1.setPeriod(RentalPeriodType.DAY);
		RentalCost rentalCost2 = new RentalCost();
		rentalCost2.setCost(0.50);
		rentalCost2.setPeriod(RentalPeriodType.WEEK);
		RentalCost rentalCost3 = new RentalCost();
		rentalCost3.setCost(0.50);
		rentalCost3.setPeriod(RentalPeriodType.MONTH);
		RentalCost rentalCost4 = new RentalCost();
		rentalCost4.setCost(0.50);
		rentalCost4.setPeriod(RentalPeriodType.YEAR);
		List<RentalCost> costs = new ArrayList<RentalCost>();
		costs.add(rentalCost);
		costs.add(rentalCost1);
		costs.add(rentalCost2);
		costs.add(rentalCost3);
		costs.add(rentalCost4);
		rentalItem.setCosts(costs);
		
		PenaltyTerm penaltyTerm = new PenaltyTerm();
		penaltyTerm.setPenaltyCharge(5.00);
		penaltyTerm.setPenaltyTerm("misuse");
		PenaltyTerm penaltyTerm1 = new PenaltyTerm();
		penaltyTerm1.setPenaltyCharge(1.00);
		penaltyTerm1.setPenaltyTerm("screws lost");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		pterms.add(penaltyTerm1);
		rentalItem.setPenaltyTerms(pterms);
		
		RentalLocation location1 = new RentalLocation();
		location1.setContactName("P K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		
		Address address = new Address();
		address.setAddress1("line 1");
		address.setCity("nowhere");
		address.setState("nostate");
		address.setZip("12345");
		location1.setAddress(address);
		
		location1.setEmail("pk@singh.com");
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		rentalItem.setRentalLocations(locations);
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext(owner.getUserName(),"ownerpassword",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 final RentalItem added;
		 
	     try {
	    	   added = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalItem>() {
	 
	    		   						@Override
	    		   						public RentalItem run() throws BadInputDataException {													
	    		   							return rentalItemMgmtService.addRentalItem(rentalItem);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
			assertNotNull(added.getId());
			assertEquals(5, added.getCosts().size());
			assertEquals(2, added.getPenaltyTerms().size());
			assertEquals(1, added.getRentalLocations().size());
			assertEquals("Plier", added.getName());
			assertEquals("Metal", added.getItemCategory());
			assertEquals("Tool", added.getItemSubCategory());
			assertEquals("Bosch", added.getItemBrand());
				    
			log.info(added.getName() + " was persisted with id " + added.getId());
			log.info("No of RentalCosts added " + added.getCosts().size());
			log.info("No of PenaltyTerms added" + added.getPenaltyTerms().size());


			for(RentalCost addedCost: added.getCosts()) {
				switch (addedCost.getPeriod()) {
				case HOUR:
					assertEquals(0.50, addedCost.getCost(),0.001);
					assertEquals(RentalPeriodType.HOUR,addedCost.getPeriod());
					log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
					break;
				case DAY:
					assertEquals(5.00, addedCost.getCost(),0.001);
					assertEquals(RentalPeriodType.DAY,addedCost.getPeriod());
					log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
					break;
				case WEEK:
					assertEquals(0.50, addedCost.getCost(),0.001);
					assertEquals(RentalPeriodType.WEEK,addedCost.getPeriod());
					log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
					break;
				case MONTH:
					assertEquals(0.50, addedCost.getCost(),0.001);
					assertEquals(RentalPeriodType.MONTH,addedCost.getPeriod());
					log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
					break;
				case YEAR:
					assertEquals(0.50, addedCost.getCost(),0.001);
					assertEquals(RentalPeriodType.YEAR,addedCost.getPeriod());
					log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
					break;
					
				}

			}
			
			for(PenaltyTerm addedTerm: added.getPenaltyTerms()) {
				log.info("Penalty Term:"+addedTerm.getPenaltyTerm()+"charge"+addedTerm.getPenaltyCharge());
				if (addedTerm.getPenaltyTerm().equals("misuse")) {
					assertEquals(5.00, addedTerm.getPenaltyCharge(), 0.001);
					assertEquals("misuse", addedTerm.getPenaltyTerm());
				} else {
					assertEquals(1.00, addedTerm.getPenaltyCharge(), 0.001);
					assertEquals("screws lost", addedTerm.getPenaltyTerm());	
				}

			}
			
			for(RentalLocation addedLoc: added.getRentalLocations()) {
				assertEquals("P K Singh",addedLoc.getContactName());
				assertEquals("999-999-9999", addedLoc.getContactPhoneNumber());
				assertEquals("line 1",addedLoc.getAddress().getAddress1());
				assertEquals("nowhere",addedLoc.getAddress().getCity());
				assertEquals("nostate",addedLoc.getAddress().getState());
				assertEquals("USA",addedLoc.getAddress().getCountry());
				assertEquals("12345",addedLoc.getAddress().getZip());
				assertEquals("pk@singh.com",addedLoc.getEmail());
			}
			
			assertEquals(owner.getUserName(), added.getOwner().getUserId());
	
			//update rental item
			added.setName("M Plier");

			 loginContext = JBossLoginContextFactory.createLoginContext(owner.getUserName(),"ownerpassword",PropertyManager.getProp("realm")); 
			 loginContext.login();
			 final RentalItem updated;
			 
		     try {
		    	   updated = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalItem>() {
		 
		    		   						@Override
		    		   						public RentalItem run() throws BadInputDataException {													
		    		   							return rentalItemMgmtService.updateRentalItem(added.getId(), added);
									
		    		   						} });
		     } catch (PrivilegedActionException pe) {
		    	 throw pe.getException();
		     }  finally {
		          loginContext.logout();
		     }
		     
		     
				assertNotNull(updated.getId());
				assertEquals(5, updated.getCosts().size());
				assertEquals(2, updated.getPenaltyTerms().size());
				assertEquals(1, updated.getRentalLocations().size());
				
				assertEquals("M Plier", updated.getName());
				assertEquals("Metal", updated.getItemCategory());
				assertEquals("Tool", updated.getItemSubCategory());
				assertEquals("Bosch", updated.getItemBrand());
					    
				for(RentalCost updatedCost: updated.getCosts()) {
					switch (updatedCost.getPeriod()) {
					case HOUR:
						assertEquals(0.50, updatedCost.getCost(),0.001);
						assertEquals(RentalPeriodType.HOUR,updatedCost.getPeriod());
						
						break;
					case DAY:
						assertEquals(5.00, updatedCost.getCost(),0.001);
						assertEquals(RentalPeriodType.DAY,updatedCost.getPeriod());
						
						break;
					case WEEK:
						assertEquals(0.50, updatedCost.getCost(),0.001);
						assertEquals(RentalPeriodType.WEEK,updatedCost.getPeriod());
						
						break;
					case MONTH:
						assertEquals(0.50, updatedCost.getCost(),0.001);
						assertEquals(RentalPeriodType.MONTH,updatedCost.getPeriod());
						
						break;
					case YEAR:
						assertEquals(0.50, updatedCost.getCost(),0.001);
						assertEquals(RentalPeriodType.YEAR,updatedCost.getPeriod());
						
						break;
						
					}

				}
				
				for(PenaltyTerm updatedTerm: updated.getPenaltyTerms()) {
					
					if (updatedTerm.getPenaltyTerm().equals("misuse")) {
						assertEquals(5.00, updatedTerm.getPenaltyCharge(), 0.001);
						assertEquals("misuse", updatedTerm.getPenaltyTerm());
					} else {
						assertEquals(1.00, updatedTerm.getPenaltyCharge(), 0.001);
						assertEquals("screws lost", updatedTerm.getPenaltyTerm());	
					}

				}
				
				for(RentalLocation addedLoc: updated.getRentalLocations()) {
					assertEquals("P K Singh",addedLoc.getContactName());
					assertEquals("999-999-9999", addedLoc.getContactPhoneNumber());
					assertEquals("line 1",addedLoc.getAddress().getAddress1());
					assertEquals("nowhere",addedLoc.getAddress().getCity());
					assertEquals("nostate",addedLoc.getAddress().getState());
					assertEquals("USA",addedLoc.getAddress().getCountry());
					assertEquals("12345",addedLoc.getAddress().getZip());
					assertEquals("pk@singh.com",addedLoc.getEmail());
				}
				
				assertEquals(owner.getUserName(), added.getOwner().getUserId());
		     
		     
			 
				 loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
				 loginContext.login();
				 
				 if(utx.getStatus()!=6) utx.rollback();
				 utx.begin();
				 try {
					 Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
				    			 
									@Override
									public Void run() throws BadInputDataException{				
										rentalItemManager.removeRentalItem(added.getId());
				    	   			return null;
									}
				    	   }); }  
				 catch (PrivilegedActionException pe) {
			    	 throw pe.getException(); }  	   
				  finally {
				    		   
					  utx.commit();
					  loginContext.logout(); }
	
				 this.removeOwner();
			
			
	}
	
	@Test
	public void testFindMyRentalItems() throws Exception {
		
		owner = this.addOwner();
		
		final RentalItem rentalItem = new RentalItem();
		rentalItem.setName("Plier");
		rentalItem.setItemCategory("Metal");
		rentalItem.setItemSubCategory("Tool");
		rentalItem.setItemBrand("Bosch");
		rentalItem.setAvailableFrom(null);
		rentalItem.setConsolidatedRating(0);
		rentalItem.setEnableRenting(true);
		rentalItem.setItemAge(0);
		rentalItem.setPastRentalCount(0);
		rentalItem.setReplacementCost(0);
		
		RentalCost rentalCost = new RentalCost();
		rentalCost.setCost(0.50);
		rentalCost.setPeriod(RentalPeriodType.HOUR);
		RentalCost rentalCost1 = new RentalCost();
		rentalCost1.setCost(5.00);
		rentalCost1.setPeriod(RentalPeriodType.DAY);
		RentalCost rentalCost2 = new RentalCost();
		rentalCost2.setCost(0.50);
		rentalCost2.setPeriod(RentalPeriodType.WEEK);
		RentalCost rentalCost3 = new RentalCost();
		rentalCost3.setCost(0.50);
		rentalCost3.setPeriod(RentalPeriodType.MONTH);
		RentalCost rentalCost4 = new RentalCost();
		rentalCost4.setCost(0.50);
		rentalCost4.setPeriod(RentalPeriodType.YEAR);
		List<RentalCost> costs = new ArrayList<RentalCost>();
		costs.add(rentalCost);
		costs.add(rentalCost1);
		costs.add(rentalCost2);
		costs.add(rentalCost3);
		costs.add(rentalCost4);
		rentalItem.setCosts(costs);
		
		PenaltyTerm penaltyTerm = new PenaltyTerm();
		penaltyTerm.setPenaltyCharge(5.00);
		penaltyTerm.setPenaltyTerm("misuse");
		PenaltyTerm penaltyTerm1 = new PenaltyTerm();
		penaltyTerm1.setPenaltyCharge(1.00);
		penaltyTerm1.setPenaltyTerm("screws lost");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		pterms.add(penaltyTerm1);
		rentalItem.setPenaltyTerms(pterms);
		
		RentalLocation location1 = new RentalLocation();
		location1.setContactName("P K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		
		Address address = new Address();
		address.setAddress1("line 1");
		address.setCity("nowhere");
		address.setState("nostate");
		address.setZip("12345");
		location1.setAddress(address);
		
		location1.setEmail("pk@singh.com");
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		rentalItem.setRentalLocations(locations);
		
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext(owner.getUserName(),"ownerpassword",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 final RentalItem added;
		 
	     try {
	    	   added = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalItem>() {
	 
	    		   						@Override
	    		   						public RentalItem run() throws BadInputDataException {													
	    		   							return rentalItemMgmtService.addRentalItem(rentalItem);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
	     final RentalItem rentalItem2 = rentalItem.deepCopy();
	     rentalItem2.setName("Another Plier");
	     rentalItem2.setId(0);
	     
		 loginContext = JBossLoginContextFactory.createLoginContext(owner.getUserName(),"ownerpassword",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 final RentalItem added2;
		 
	     try {
	    	   added2 = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<RentalItem>() {
	 
	    		   						@Override
	    		   						public RentalItem run() throws BadInputDataException {													
	    		   							return rentalItemMgmtService.addRentalItem(rentalItem2);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }

	     loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm"));
	     final List<RentalItem> rentalItems;
	     try {
	    	   rentalItems = Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<List>() {
	 
	    		   						@Override
	    		   						public List<RentalItem> run() throws BadInputDataException {													
	    		   							return rentalItemMgmtService.findRentalItems(null, null, null);
								
	    		   						} });
	     } catch (PrivilegedActionException pe) {
	    	 throw pe.getException();
	     }  finally {
	          loginContext.logout();
	     }
	     
	     assertEquals(2, rentalItems.size());
	     loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 
		 if(utx.getStatus()!=6) utx.rollback();
		 utx.begin();
		 try {
			 Subject.doAs(loginContext.getSubject(), new PrivilegedExceptionAction<Void>() {
		    			 
							@Override
							public Void run() throws BadInputDataException{				
								rentalItemManager.removeRentalItem(added.getId());
								rentalItemManager.removeRentalItem(added2.getId());
		    	   			return null;
							}
		    	   }); }  
		 catch (PrivilegedActionException pe) {
	    	 throw pe.getException(); }  	   
		  finally {
		    		   
			  utx.commit();
			  loginContext.logout(); }

		 this.removeOwner();
	}
	
	private User addOwner() throws Exception {
		//add user to use as owner

		User user= new User();
		user.setFirstName("Owner");
		user.setLastName("Singh");
		user.setEmail("veer.muchandi@gmail.com");
		user.setPassword(JBossLoginContextFactory.generateDigestPassword("veer.muchandi@gmail.com","ownerpassword",PropertyManager.getProp("realm")));
		Address address= new Address();
		address.setAddress1("my lane");
		address.setCity("mycity");
		address.setState("mystate");
		address.setZip("99999");
		user.setAddress(address);
		
		Phone phone1=new Phone(), phone2=new Phone();
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
		
		return addedUser;

	}
	
	private void removeOwner() throws Exception {
		 log.info("in remove owner");
		 LoginContext loginContext = JBossLoginContextFactory.createLoginContext("pinakallc@gmail.com","password",PropertyManager.getProp("realm")); 
		 loginContext.login();
		 
		 if(utx.getStatus()!=6) utx.rollback();
		 utx.begin();
		 try {
		   	   Subject.doAs(loginContext.getSubject(), new PrivilegedAction<Void>() {
		    			 
							@Override
							public Void run(){
							rentalUserManager.removeUser("veer.muchandi@gmail.com");
							userManager.removeUser("veer.muchandi@gmail.com");
		    	   			return null;
							}
		    	   }); }  
		    	   
		  finally {
		    		   
			  utx.commit();
			  loginContext.logout(); }
		 
		 log.info("end of remove owner");
	}
	
	
}
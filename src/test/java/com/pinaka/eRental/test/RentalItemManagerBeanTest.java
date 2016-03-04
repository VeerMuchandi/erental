package com.pinaka.eRental.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
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
import com.pinaka.eRental.service.RentalItemManager;
import com.pinaka.eRental.service.RentalItemManagerBean;
import com.pinaka.eRental.service.RentalUserManager;
import com.pinaka.eRental.service.RentalUserManagerBean;
import com.pinaka.eRental.util.PropertyManager;
import com.pinaka.eRental.util.Resources;

@RunWith(Arquillian.class)
public class RentalItemManagerBeanTest {
	
	@Deployment
	   public static Archive<?> createTestArchive() {
	      return ShrinkWrap.create(WebArchive.class, "test.war")
	            .addClasses(RentalItem.class, 
	            			RentalItem_.class,
	            			RentalCost.class,
	            			RentalPeriodType.class, 
	            			Address.class, 
	            			PenaltyTerm.class, 
	            			RentalLocation.class, 
	            			RentalStatusType.class,
	            			UserManager.class,
	            			UserManagerBean.class,
	            			User.class,
	            			Role.class,
	            			RentalUser.class, 
	            			Phone.class, 
	            			PhoneType.class, 
	            			RentalItemManager.class,
	            			RentalUserManager.class,
	            			RentalUserManagerBean.class,
	            			RentalItemManagerBean.class, 
	            			BadInputDataException.class, 
	            			RentalInstance.class, 
	            			RejectionReasonType.class,
	            			PropertyManager.class,
	            			StringEscapeUtils.class, 
		            		NestableRuntimeException.class, 
		            		Nestable.class, 
		            		NestableDelegate.class,
		            		Resources.class)
	            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
	            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
	            .addAsResource("ExceptionMessages.properties");
	   }
	

	@Inject
	RentalItemManager rentalItemManager;
	@Inject
	RentalItem rentalItem, rentalItem2;
	@Inject
	RentalCost rentalCost, rentalCost1, rentalCost2, rentalCost3, rentalCost4;
	@Inject
	PenaltyTerm penaltyTerm, penaltyTerm1, penaltyTerm2;
	@Inject
	RentalLocation location1;
	@Inject
	Address address;
	@Inject
	RentalUser owner, user, addedUser;
	@Inject
	Phone phone1, phone2;
	@Inject
	UserManager userManager;
	@Inject
	RentalUserManager rentalUserManager;
	@Inject
	UserTransaction utx;
	
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Inject
	Logger log;
	
	@Test
	public void testNullAddRentalItem() throws Exception {
		rentalItem = null;
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalItemNull"));
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		rentalItemManager.addRentalItem(rentalItem);
		utx.rollback();
		

	
	}

	@Test
	public void testAddRentalItemNoOwner() throws Exception {
		

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
		
		rentalCost.setCost(0.50);
		rentalCost.setPeriod(RentalPeriodType.HOUR);
		List<RentalCost> costs = new ArrayList<RentalCost>();
		costs.add(rentalCost);
		rentalItem.setCosts(costs);
		
		penaltyTerm.setPenaltyCharge(5.00);
		penaltyTerm.setPenaltyTerm("misuse");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		rentalItem.setPenaltyTerms(pterms);
		
		location1.setContactName("P K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		
		address.setAddress1("line 1");
		address.setCity("nowhere");
		address.setState("nostate");
		address.setZip("12345");
		location1.setAddress(address);
		
		location1.setEmail("pk@singh.com");
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		rentalItem.setRentalLocations(locations);	
		
		
		//no owner
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("OwnerEmpty"));
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		rentalItemManager.addRentalItem(rentalItem);
		utx.rollback();
		
	}
	
	@Test
	public void testAddRentalItemInvalidOwner() throws Exception {
		

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
		
		rentalCost.setCost(0.50);
		rentalCost.setPeriod(RentalPeriodType.HOUR);
		List<RentalCost> costs = new ArrayList<RentalCost>();
		costs.add(rentalCost);
		rentalItem.setCosts(costs);
		
		penaltyTerm.setPenaltyCharge(5.00);
		penaltyTerm.setPenaltyTerm("misuse");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		rentalItem.setPenaltyTerms(pterms);
		
		location1.setContactName("P K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		
		address.setAddress1("line 1");
		address.setCity("nowhere");
		address.setState("nostate");
		address.setZip("12345");
		location1.setAddress(address);
		
		location1.setEmail("pk@singh.com");
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		rentalItem.setRentalLocations(locations);	
		
		
		//set a non existing owner id
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("OwnerIdInvalid","invalid@user.com"));
		User user = new User();
		user.setUserName("invalid@user.com");
		owner.setUser(user);
		rentalItem.setOwner(owner);
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		rentalItemManager.addRentalItem(rentalItem);
		utx.rollback();

	}
	
	
	@Test
	public void testAddFindAndRemoveRentalItem() throws Exception {
		
		if(utx.getStatus()!=6) utx.rollback();
		addedUser = this.addUser();
		
		//create and rental item 
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
		penaltyTerm1.setPenaltyCharge(1.00);
		penaltyTerm1.setPenaltyTerm("screws lost");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		pterms.add(penaltyTerm1);
		rentalItem.setPenaltyTerms(pterms);
		
		location1.setContactName("P K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		
		address.setAddress1("line 1");
		address.setCity("nowhere");
		address.setState("nostate");
		address.setZip("12345");
		location1.setAddress(address);
		
		location1.setEmail("pk@singh.com");
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		rentalItem.setRentalLocations(locations);	
		
		rentalItem.setOwner(addedUser);
		
		utx.begin();
		log.info("*****Added RUser:" +rentalUserManager.findUser(addedUser.getUser()).getUserId());
		utx.commit();
		
		utx.begin();
		RentalItem added = rentalItemManager.addRentalItem(rentalItem);
		utx.commit();
		assertNotNull(added.getId());
		assertEquals(5, added.getCosts().size());
		assertEquals(2, added.getPenaltyTerms().size());
		assertEquals(1, added.getRentalLocations().size());
			    
		log.info(added.getName() + " was persisted with id " + added.getId());
		log.info("No of RentalCosts added " + added.getCosts().size());
		log.info("No of PenaltyTerms added" + added.getPenaltyTerms().size());
		log.info("Owners RentalUser Name for RentalItem:" + added.getOwner().getUser().getUserName());

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
			
			if (addedTerm.getPenaltyTerm() == "misuse") {
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
		
		assertEquals(addedUser.getUser().getUserName(), added.getOwner().getUser().getUserName());
//		assertEquals(addedUser.getUserName(), added.getOwner().getUserName());
		
		utx.begin();
		RentalUser foundUser = rentalUserManager.findUser(addedUser.getUser()); 	
//		utx.commit();		
		assertEquals(1, foundUser.getOwnedItems().size());
		log.info("Number of Items : "+foundUser.getOwnedItems().size());
		for (RentalItem i: foundUser.getOwnedItems()) {
			log.info("Item id : "+i.getId()+" ItemName : "+i.getName());
		}
		utx.commit();
		
		
		utx.begin();
		RentalItem found = rentalItemManager.findRentalItem(added.getId());
		utx.commit();
		
		assertNotNull(found.getId());
		assertEquals(5, found.getCosts().size());
		assertEquals(2, found.getPenaltyTerms().size());
		assertEquals(1, found.getRentalLocations().size());
			    
		log.info(found.getName() + " was persisted with id " + found.getId());
		log.info("No of RentalCosts found " + found.getCosts().size());
		log.info("No of PenaltyTerms found" + found.getPenaltyTerms().size());

		for(RentalCost foundCost: found.getCosts()) {
			switch (foundCost.getPeriod()) {
			case HOUR:
				assertEquals(0.50, foundCost.getCost(),0.001);
				assertEquals(RentalPeriodType.HOUR,foundCost.getPeriod());
				log.info(added.getName()+"costs $"+foundCost.getCost()+"for each"+foundCost.getPeriod());
				break;
			case DAY:
				assertEquals(5.00, foundCost.getCost(),0.001);
				assertEquals(RentalPeriodType.DAY,foundCost.getPeriod());
				log.info(added.getName()+"costs $"+foundCost.getCost()+"for each"+foundCost.getPeriod());
				break;
			case WEEK:
				assertEquals(0.50, foundCost.getCost(),0.001);
				assertEquals(RentalPeriodType.WEEK,foundCost.getPeriod());
				log.info(added.getName()+"costs $"+foundCost.getCost()+"for each"+foundCost.getPeriod());
				break;
			case MONTH:
				assertEquals(0.50, foundCost.getCost(),0.001);
				assertEquals(RentalPeriodType.MONTH,foundCost.getPeriod());
				log.info(added.getName()+"costs $"+foundCost.getCost()+"for each"+foundCost.getPeriod());
				break;
			case YEAR:
				assertEquals(0.50, foundCost.getCost(),0.001);
				assertEquals(RentalPeriodType.YEAR,foundCost.getPeriod());
				log.info(added.getName()+"costs $"+foundCost.getCost()+"for each"+foundCost.getPeriod());
				break;
				
			}

		}
		for(RentalLocation foundLoc: found.getRentalLocations()) {
			assertEquals("P K Singh",foundLoc.getContactName());
			assertEquals("999-999-9999", foundLoc.getContactPhoneNumber());
			assertEquals("line 1",foundLoc.getAddress().getAddress1());
			assertEquals("nowhere",foundLoc.getAddress().getCity());
			assertEquals("nostate",foundLoc.getAddress().getState());
			assertEquals("USA",foundLoc.getAddress().getCountry());
			assertEquals("12345",foundLoc.getAddress().getZip());
			assertEquals("pk@singh.com",foundLoc.getEmail());
		}
		
		for(RentalCost toUpdateCost: added.getCosts()) {
			if (toUpdateCost.getPeriod()==RentalPeriodType.DAY) toUpdateCost.setCost(6.00);
		}
		
		for(PenaltyTerm toUpdatePT:added.getPenaltyTerms()) {
			if(toUpdatePT.getPenaltyTerm()=="misuse") toUpdatePT.setPenaltyCharge(7.00);
		}
		
		utx.begin();
		RentalItem updated = rentalItemManager.updateRentalItem(added);
		utx.commit();
		
		assertNotNull(updated.getId());
		assertEquals(5, updated.getCosts().size());
		assertEquals(2, updated.getPenaltyTerms().size());
		assertEquals(1, updated.getRentalLocations().size());
		log.info("No of RentalCosts added " + updated.getCosts().size());
		log.info("No of PenaltyTerms added" + updated.getPenaltyTerms().size());
		
		for(RentalCost addedCost: updated.getCosts()) {
			switch (addedCost.getPeriod()) {
			case HOUR:
				assertEquals(0.50, addedCost.getCost(),0.001);
				assertEquals(RentalPeriodType.HOUR,addedCost.getPeriod());
				log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
				break;
			case DAY:
				assertEquals(6.00, addedCost.getCost(),0.001);
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
		
		for(PenaltyTerm addedTerm: updated.getPenaltyTerms()) {
			
			if (addedTerm.getPenaltyTerm() == "misuse") {
				assertEquals(7.00, addedTerm.getPenaltyCharge(), 0.001);
				assertEquals("misuse", addedTerm.getPenaltyTerm());
			} else 
				//if (addedTerm.getPenaltyTerm() == "screws lost") 
				{
				assertEquals(1.00, addedTerm.getPenaltyCharge(), 0.001);
				assertEquals("screws lost", addedTerm.getPenaltyTerm());	
			}

		}
		
		utx.begin();
		RentalItem found1 = rentalItemManager.findRentalItem(updated.getId());
		utx.commit();
		
		assertNotNull(found1.getId());
		assertEquals(5, found1.getCosts().size());
		assertEquals(2, found1.getPenaltyTerms().size());
		assertEquals(1, found1.getRentalLocations().size());
		log.info("No of RentalCosts found " + found1.getCosts().size());
		log.info("No of PenaltyTerms found" + found1.getPenaltyTerms().size());
		
		for(RentalCost addedCost: found1.getCosts()) {
			switch (addedCost.getPeriod()) {
			case HOUR:
				assertEquals(0.50, addedCost.getCost(),0.001);
				assertEquals(RentalPeriodType.HOUR,addedCost.getPeriod());
				log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
				break;
			case DAY:
				assertEquals(6.00, addedCost.getCost(),0.001);
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
		
		for(PenaltyTerm addedTerm: updated.getPenaltyTerms()) {
			
			if (addedTerm.getPenaltyTerm() == "misuse") {
				assertEquals(7.00, addedTerm.getPenaltyCharge(), 0.001);
				assertEquals("misuse", addedTerm.getPenaltyTerm());
			} else 
				//if (addedTerm.getPenaltyTerm() == "screws lost") 
				{
				assertEquals(1.00, addedTerm.getPenaltyCharge(), 0.001);
				assertEquals("screws lost", addedTerm.getPenaltyTerm());	
			}

		}
		
		utx.begin();
		//TODO - Only admin can remove rentalitems or users
		rentalItemManager.removeRentalItem(updated.getId());
		assertNull(rentalItemManager.findRentalItem(updated.getId()));
		
		rentalUserManager.removeUser(addedUser.getUserId());
		userManager.removeUser(addedUser.getUser().getUserName());
		
		utx.commit();
	}
		
	@Test
	public void testNullUpdateRentalItem() throws Exception {
		rentalItem = null;
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalItemNull"));
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		rentalItemManager.updateRentalItem(rentalItem);
		utx.rollback();
	}
	
	@Test 
	public void testUpdateNonExistingRentalItem() throws Exception{
		
		if(utx.getStatus()!=6) utx.rollback();		
		//add owner first
		addedUser = this.addUser();
		

		//update non-existing rental item should work like add
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
		
		rentalItem.setOwner(addedUser);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		RentalItem added = rentalItemManager.updateRentalItem(rentalItem);
		utx.commit();
		assertNotNull(added.getId());
	    log.info(added.getName() + " was persisted with id " + added.getId());
	    assertEquals("Plier", added.getName());
	    
	    utx.begin();
        rentalItemManager.removeRentalItem(added.getId());
        rentalUserManager.removeUser(addedUser.getUserId());
	    userManager.removeUser(addedUser.getUser().getUserName());	
	    utx.commit();
	}
	
	@Test
	public void testRemoveNull() throws Exception {
		
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("RentalItemIdNull"));
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		rentalItemManager.removeRentalItem(0l);
		utx.rollback();
	}	
	
	@Test
	public void testRemoveNonExistingRentalItem() throws Exception {
		
		long dummyId = 99999l;
		thrown.expect(BadInputDataException.class);
		thrown.expectMessage(PropertyManager.getMessage("NoRentalItem",dummyId));
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		rentalItemManager.removeRentalItem(dummyId);
		utx.rollback();
	}	
	
	@Test 
	public void testUpdateRentalItem() throws Exception{
		if(utx.getStatus()!=6) utx.rollback();
		//add owner first
		addedUser = this.addUser();
		
		//add rental item
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
		
		rentalItem.setOwner(addedUser);
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		RentalItem added = rentalItemManager.addRentalItem(rentalItem);
		utx.commit();
		assertNotNull(added.getId());
	    log.info(added.getName() + " was persisted with id " + added.getId());
	    assertEquals("Plier", added.getName());
	    
	    added.setName("M Plier");
	    
	    utx.begin();
	    RentalItem updated = rentalItemManager.updateRentalItem(added);
	    utx.commit();
	    assertEquals("M Plier", updated.getName());
	    
	    utx.begin();
	    rentalItemManager.removeRentalItem(updated.getId());
	    rentalUserManager.removeUser(addedUser.getUserId());
	    userManager.removeUser(addedUser.getUser().getUserName());
	    utx.commit();
		
	}
	
	@Test
	public void testAddFindRentalItems() throws Exception {
		
		
		addedUser = this.addUser();
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		//create and rental item 
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
		
		rentalCost.setCost(0.50);
		rentalCost.setPeriod(RentalPeriodType.HOUR);
		List<RentalCost> costs = new ArrayList<RentalCost>();
		costs.add(rentalCost);
		rentalItem.setCosts(costs);
		
		penaltyTerm.setPenaltyCharge(5.00);
		penaltyTerm.setPenaltyTerm("misuse");
		List<PenaltyTerm> pterms= new ArrayList<PenaltyTerm>();
		pterms.add(penaltyTerm);
		rentalItem.setPenaltyTerms(pterms);
		
		location1.setContactName("P K Singh");
		location1.setContactPhoneNumber("999-999-9999");
		
		address.setAddress1("line 1");
		address.setCity("nowhere");
		address.setState("nostate");
		address.setZip("12345");
		location1.setAddress(address);
		
		location1.setEmail("pk@singh.com");
		List<RentalLocation> locations = new ArrayList<RentalLocation>();
		locations.add(location1);
		rentalItem.setRentalLocations(locations);	
		
		rentalItem.setOwner(addedUser);
		
		RentalItem added = rentalItemManager.addRentalItem(rentalItem);
		utx.commit();
		assertNotNull(added.getId());
		assertEquals(1, added.getCosts().size());
		assertEquals(1, added.getPenaltyTerms().size());
		assertEquals(1, added.getRentalLocations().size());
			    
		log.info(added.getName() + " was persisted with id " + added.getId());
		log.info("No of RentalCosts added " + added.getCosts().size());
		log.info("No of PenaltyTerms added" + added.getPenaltyTerms().size());
		log.info("Owners RentalUser Name for RentalItem:" + added.getOwner().getUser().getUserName());

		for(RentalCost addedCost: added.getCosts()) {
			assertEquals(0.50, addedCost.getCost(),0.001);
			assertEquals(RentalPeriodType.HOUR,addedCost.getPeriod());
			log.info(added.getName()+"costs $"+addedCost.getCost()+"for each"+addedCost.getPeriod());
		}
		
		for(PenaltyTerm addedTerm: added.getPenaltyTerms()) {
			assertEquals(5.00, addedTerm.getPenaltyCharge(), 0.001);
			assertEquals("misuse", addedTerm.getPenaltyTerm());
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
		
		assertEquals(addedUser.getUser().getUserName(), added.getOwner().getUser().getUserName());
		
		rentalItem2.setName("Cutter");
		rentalItem2.setItemCategory("Wood");
		rentalItem2.setItemSubCategory("Tool");
		rentalItem2.setItemBrand("Cipher");
		rentalItem2.setAvailableFrom(null);
		rentalItem2.setConsolidatedRating(0);
		rentalItem2.setEnableRenting(false);
		rentalItem2.setItemAge(0);
		rentalItem2.setPastRentalCount(0);
		rentalItem2.setReplacementCost(0);
		
		rentalItem2.setOwner(addedUser);
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		RentalItem added2 = rentalItemManager.addRentalItem(rentalItem2);
		utx.commit();
		assertNotNull(added2.getId());
		assertEquals("Cutter", added2.getName());
		
		utx.begin();
		List<RentalItem> rentalItemsList = rentalItemManager.findAllRentalItems();
		assertEquals(2, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
			if (added.getId()==a.getId()) {
				assertEquals("Plier", a.getName());
			} else {
				assertEquals(added2.getId(), a.getId());
				assertEquals(added2.getName(), a.getName());
			}
		}
		
		rentalItemsList = rentalItemManager.findAllRentableItems();
		assertEquals(0, rentalItemsList.size());
		
		rentalItemsList = rentalItemManager.findRentalItems(null, "Tool", null);
		assertEquals(2, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
			if (added.getId()==a.getId()) {
				assertEquals("Plier", a.getName());
			} else {
				assertEquals(added2.getId(), a.getId());
				assertEquals(added2.getName(), a.getName());
			}
		}
		
		rentalItemsList = rentalItemManager.findRentalItems("Metal",null,null);
		assertEquals(1, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
				assertEquals(added.getId(), a.getId());
				assertEquals(added.getName(), a.getName());
		}
		
		rentalItemsList = rentalItemManager.findRentalItems(null, null,"Bosch");
		assertEquals(1, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
				assertEquals(added.getId(), a.getId());
				assertEquals(added.getName(), a.getName());
		}
		
		rentalItemsList = rentalItemManager.findRentalItems("Wood","Tool",null);
		assertEquals(1, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
				assertEquals(added2.getId(), a.getId());
				assertEquals(added2.getName(), a.getName());
		}
		
		rentalItemsList = rentalItemManager.findRentalItems("Metal", null,"Bosch");
		assertEquals(1, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
				assertEquals(added.getId(), a.getId());
				assertEquals(added.getName(), a.getName());
		}
		
		rentalItemsList = rentalItemManager.findRentalItems("Wood", null, "Bosch");
		assertEquals(0,rentalItemsList.size());

		rentalItemsList = rentalItemManager.findRentalItems("Wood","Tool","Bosch");
		assertEquals(0,rentalItemsList.size());
		
		rentalItemsList = rentalItemManager.findRentalItems("Wood","Tool","Cipher");
		assertEquals(1, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
				assertEquals(added2.getId(), a.getId());
				assertEquals(added2.getName(), a.getName());
		}
		
		rentalItemsList = rentalItemManager.findRentalItems(null, "Tool", null,true);
		assertEquals(0,rentalItemsList.size());
		
		rentalItemsList = rentalItemManager.findRentalItems(null, "Tool", null,false);
		assertEquals(2, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
			if (added.getId()==a.getId()) {
				assertEquals("Plier", a.getName());
			} else {
				assertEquals(added2.getId(), a.getId());
				assertEquals(added2.getName(), a.getName());
			}
		}
		
		utx.commit();
		added.setEnableRenting(true);
		
		utx.begin();
		rentalItemManager.updateRentalItem(added);
		utx.commit();
		
		utx.begin();
		rentalItemsList = rentalItemManager.findAllRentableItems();
		assertEquals(1,rentalItemsList.size());
		
		rentalItemsList = rentalItemManager.findRentalItems(null, "Tool", null,true);
		assertEquals(1,rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
			assertEquals(added.getId(), a.getId());
			assertEquals(added.getName(), a.getName());
		}
		
		utx.commit();
		
		added2.setEnableRenting(true);
		utx.begin();
		rentalItemManager.updateRentalItem(added2);
		utx.commit();
		
		utx.begin();
		rentalItemsList = rentalItemManager.findRentalItems(null, "Tool", null,true);
		utx.commit();
		assertEquals(2,rentalItemsList.size());
		
		utx.begin();
		rentalItemsList = rentalItemManager.findRentalItemsBySomeValue("Wood");
		assertEquals(1, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
				assertEquals(added2.getId(), a.getId());
				assertEquals(added2.getName(), a.getName());
		}
		
		rentalItemsList = rentalItemManager.findRentalItemsBySomeValue("Bosch");
		assertEquals(1, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
				assertEquals(added.getId(), a.getId());
				assertEquals(added.getName(), a.getName());
		}
		
		rentalItemsList = rentalItemManager.findRentalItemsBySomeValue("Tool");
		assertEquals(2, rentalItemsList.size());
		for(RentalItem a:rentalItemsList) {
			if (added.getId()==a.getId()) {
				assertEquals("Plier", a.getName());
			} else {
				assertEquals(added2.getId(), a.getId());
				assertEquals(added2.getName(), a.getName());
			}
		}
		utx.commit();
		utx.begin();
	    rentalItemManager.removeRentalItem(added.getId());
	    rentalItemManager.removeRentalItem(added2.getId());
	    rentalUserManager.removeUser(addedUser.getUserId());
	    userManager.removeUser(addedUser.getUser().getUserName());
	    utx.commit();
	}
	
	private RentalUser addUser() throws Exception {
		//add user to use as owner
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
		
		if(utx.getStatus()!=6) utx.rollback();
		utx.begin();
		User addedUser=userManager.addUser(user);
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
		
		RentalUser rUser = new RentalUser();
		rUser.setUser(addedUser);
		
		utx.begin();
			RentalUser rAddedUser = rentalUserManager.addUser(rUser);
		utx.commit();
		
		return rAddedUser;
		
	}
		
	

}

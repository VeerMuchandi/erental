
package com.pinaka.eRental.serviceFacade;


import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.annotation.security.*;

import org.jboss.annotation.security.SecurityDomain;

import com.pinaka.UserManagement.User;
import com.pinaka.UserManagement.UserManager;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.RentalItem;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.service.RentalItemManager;
import com.pinaka.eRental.service.RentalUserManager;
import com.pinaka.eRental.util.PropertyManager;
import com.pinaka.eRental.serviceFacade.RentalUserMgmtService;
import com.pinaka.eRental.model.Rental;

/**
 * 
 * Service Facade that handles RentalItems. Provides services to Add, Update and Find RentalItems as services
 * Exposes these services as Restful interfaces for consumption over HTTP
 * Allows remote connections
 * 
 * @filename RentalItemMgmtServiceBean.java
 * @author Veer Muchandi
 * @created Apr 1, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */

@Path("/")
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@SecurityDomain("other")
@DeclareRoles({"user","admin"})
@Local(RentalItemMgmtService.class)
public class RentalItemMgmtServiceBean implements RentalItemMgmtService {

	@Inject 
	RentalItemManager rentalItemManager;
	@Inject
	RentalUserManager rentalUserManager;
	@Inject
	UserManager userManager; 
	@Inject
	EntityManager em;
	@Inject
	Logger log;
	@Resource 
	SessionContext ctx;
	@Resource
	UserTransaction utx;
	@Inject 
	RentalUserMgmtService rentalUserMgmtService;
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.serviceFacade.RentalItemMgmtService#findRentalItem(long)
	 */
	@Override
	public RentalItem findRentalItem(@PathParam("id")long rentalItemId) throws BadInputDataException {
		
		log.info("In FindRentalItem by Id");
		
		RentalItem rentalItem =  rentalItemManager.findRentalItem(rentalItemId);
		
		//if the rental item is not found return null
		if(rentalItem == null) return null;
		
		//if renting is disabled then return the rental item only if the caller is the Owner. Others should not see the disabled items
		if(!rentalItem.getEnableRenting()) { 
			if (!ctx.getCallerPrincipal().getName().equals(rentalItem.getOwner().getUser().getUserName())) return null;
		}
		
		//if rental item is found, detach it and remove dependencies on the lazy loaded collections and return the detached entity
		//this makes sure the client does not lazy load errors once the transaction ends at the end of this method scope
		em.detach(rentalItem);
		rentalItem.setRentalInstances(null);
		rentalItem.setOwner(null);
		return rentalItem;
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.serviceFacade.RentalItemMgmtService#findRentalItems(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<RentalItem> findRentalItems(@QueryParam("category") String itemCategory, 
			 								@QueryParam("subcategory") String itemSubCategory,
			 								@QueryParam("brand") String itemBrand, @QueryParam("fullSearch") String textSearchParam) throws BadInputDataException{

		if (textSearchParam != null) return findRentalItemsBySomeValue(textSearchParam);
		else return findRentalItems(itemCategory, itemSubCategory, itemBrand);
		
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.serviceFacade.RentalItemMgmtService#findRentalItems(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<RentalItem> findRentalItems(String itemCategory, 
			 								String itemSubCategory,
			 								String itemBrand) throws BadInputDataException{

		log.info("In FindRentalItems by category, subcategory and brand");
		List<RentalItem> rentalItemsList = null;
		
		//if both itemCategory and SubCategory are null list all rentable items
		if(itemCategory==null && itemSubCategory==null && itemBrand==null) rentalItemsList = rentalItemManager.findAllRentableItems();
		else rentalItemsList = rentalItemManager.findRentalItems(itemCategory, itemSubCategory, itemBrand, true); //return only rentable items by default
		
		em.detach(rentalItemsList);
		for(RentalItem a : rentalItemsList) {
			em.detach(a);
			a.setRentalInstances(null);
			a.setOwner(null);
		}
		return rentalItemsList;
		
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.serviceFacade.RentalItemMgmtService#findRentalItemsBySomeValue(java.lang.String)
	 */
	@Override
	public List<RentalItem> findRentalItemsBySomeValue(String textSearchParam){
		
		log.info("In FindRentalItem full search");
		List<RentalItem> rentalItemsList = null;
		//if (textSearchParam == null) return this.findAllRentalItems();
		rentalItemsList = rentalItemManager.findRentableItemsBySomeValue(textSearchParam);
			
		em.detach(rentalItemsList);
		for(RentalItem a : rentalItemsList) {
			em.detach(a);
			a.setRentalInstances(null);
			a.setOwner(null);
		}
		return rentalItemsList;
						
	}
	
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.serviceFacade.RentalItemMgmtService#findMyRentalItems()
	 */
	@Override
//	@RolesAllowed("user")
	public List<RentalItem> findMyRentalItems() throws BadInputDataException{
		
		log.info("In FindMyRentalItems");
		List<RentalItem> rentalItemsList = null;
		String userName = ctx.getCallerPrincipal().getName();
		
		User loggedInUser = userManager.findUser(userName);

		if(loggedInUser==null) 
			throw new BadInputDataException(PropertyManager.getMessage("UserNotRegistered", userName), 
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		RentalUser owner = rentalUserManager.findUser(loggedInUser);

		//if the loggedInUser is not a rentalUser, it means that there are no rental items added by the user yet
		if(owner==null) return null;
		
		rentalItemsList = owner.getOwnedItems();
		
		if (rentalItemsList == null) return null;
		
		
		em.detach(rentalItemsList);
		for(RentalItem a : rentalItemsList) {
			em.detach(a);
			
			a.setRentalInstances(null);
			a.setOwner(null);
		}
		em.detach(owner);
			
		return rentalItemsList;
		
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.serviceFacade.RentalItemMgmtService#addRentalItem(com.pinaka.eRental.model.RentalItem)
	 */
	@Override
//	@RolesAllowed("user")
	public RentalItem addRentalItem(RentalItem rentalItem) throws BadInputDataException {
		
		log.info("In addRentalItem");
		if (rentalItem==null) 
			throw new BadInputDataException(PropertyManager.getMessage("RentalItemNull"), 
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		String userName = ctx.getCallerPrincipal().getName();
		
		User loggedInUser = userManager.findUser(userName);
		RentalUser owner = rentalUserManager.findUser(loggedInUser);
		
		if(owner == null)
			owner = rentalUserMgmtService.createRentalUser(loggedInUser);
		log.info("**** Added user:"+owner.getUserId());
						
		rentalItem.setOwner(owner);
		RentalItem addedItem = rentalItemManager.addRentalItem(rentalItem);
						
		RentalItem deepCopy = (RentalItem) addedItem.deepCopy();
		deepCopy.getOwner().setUser(null); //to prevent lazy initialization exceptions.
		deepCopy.getOwner().setOwnedItems(null);
		deepCopy.getOwner().setRentedItems(null);
		return deepCopy;
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.serviceFacade.RentalItemMgmtService#updateRentalItem(long, com.pinaka.eRental.model.RentalItem)
	 */
	@Override
//	@RolesAllowed("user")
	public RentalItem updateRentalItem(@PathParam("id")long rentalItemId, RentalItem rentalItem) throws BadInputDataException {
		
		log.info("In updateRentalItem");
		if (rentalItem==null) 
				throw new BadInputDataException(PropertyManager.getMessage("RentalItemNull"), 
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if (rentalItem.getId()!=0) {
			if (rentalItem.getId() != rentalItemId) 
				throw new BadInputDataException(PropertyManager.getMessage("InconsistentId", rentalItemId, rentalItem.getId()), 
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		else
			rentalItem.setId(rentalItemId);
		
		RentalItem toUpdate = rentalItemManager.findRentalItem(rentalItemId);
		
		if (toUpdate == null)
			throw new BadInputDataException(PropertyManager.getMessage("NoRentalItem", rentalItemId), 
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		String userName = ctx.getCallerPrincipal().getName();
		log.info("RentalUser Name: " + userName);
		if(!userName.equals(toUpdate.getOwner().getUserId())) 
			throw new BadInputDataException(PropertyManager.getMessage("NotAnOwner"), 
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		rentalItem.setOwner(toUpdate.getOwner());
		
		RentalItem updatedItem = rentalItemManager.updateRentalItem(rentalItem);
		
		RentalItem deepCopy = (RentalItem) updatedItem.deepCopy();
		deepCopy.getOwner().setUser(null); //to prevent lazy initialization exceptions.
		deepCopy.getOwner().setOwnedItems(null);
		deepCopy.getOwner().setRentedItems(null);
		return deepCopy;
			
	}
	


	
}

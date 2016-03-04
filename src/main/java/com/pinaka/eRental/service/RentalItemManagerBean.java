/**
 * 
 */
package com.pinaka.eRental.service;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.pinaka.UserManagement.User;
import com.pinaka.UserManagement.UserManager;
import com.pinaka.eRental.model.RentalItem;
import com.pinaka.eRental.model.RentalItem_;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.util.PropertyManager;

import com.pinaka.eRental.exception.*;


/**
 * @filename RentalItemManagerBean.java
 * @author Veer Muchandi
 * @created Mar 27, 2012
 *
 * This is a business service to manage rental items
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */

@Stateless
@Local(RentalItemManager.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class RentalItemManagerBean implements RentalItemManager {

	@Inject
	private EntityManager em;
	@Inject
	Logger log;
	@Inject
	UserManager userManager;
	@Inject
	RentalUserManager rentalUserManager;
	
	

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#addRentalItem(com.pinaka.eRental.model.RentalItem)
	 */
	@Override
	public RentalItem addRentalItem(RentalItem rentalItem) throws BadInputDataException {
		
		if (rentalItem==null) 
				throw new BadInputDataException(PropertyManager.getMessage("RentalItemNull"), 
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if (rentalItem.getOwner() == null) 
				throw new BadInputDataException(PropertyManager.getMessage("OwnerEmpty"), 
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		User ownerUser = userManager.findUser(rentalItem.getOwner().getUser().getUserName());
		
		if(ownerUser == null) 
				throw new BadInputDataException(PropertyManager.getMessage("OwnerIdInvalid",rentalItem.getOwner().getUser().getUserName()),
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		RentalUser owner =  rentalUserManager.findUser(rentalItem.getOwner().getUser());
		if (owner == null) 
			throw new BadInputDataException(PropertyManager.getMessage("NoRentalUser",rentalItem.getOwner().getUser().getUserName()),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		

		rentalItem.setOwner(owner);

			
		try {
			em.persist(rentalItem);
			em.flush();
			return em.find(RentalItem.class, rentalItem.getId());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#updateRentalItem(com.pinaka.eRental.model.RentalItem)
	 */
	@Override
	public RentalItem updateRentalItem(RentalItem rentalItem) throws BadInputDataException {
		
		if (rentalItem==null) 
				throw new BadInputDataException(PropertyManager.getMessage("RentalItemNull"), 
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
			
		// if the input rentalItem for update does not exist, go ahead and add it.
		RentalItem toUpdate = em.find(RentalItem.class, rentalItem.getId()); 
		if(toUpdate == null) {
			RentalItem added =  this.addRentalItem(rentalItem);
			log.info("UpdateRentalItem() - Rental Item not found. New item added with Id:" + added.getId());
			return added;
		} else {
			if (toUpdate.getRentalInstances().size() > 0) 
				rentalItem.setRentalInstances(toUpdate.getRentalInstances());
			rentalItem.setOwner(toUpdate.getOwner());
			RentalItem updated = em.merge(rentalItem);
			return updated;
		}
		
		
		
	}
	

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#removeRentalItem(long)
	 */
	@RolesAllowed("admin")
	@Override
	public void removeRentalItem(long rentalItemId) throws BadInputDataException {
			
		if (rentalItemId==0l) 
				throw new BadInputDataException(PropertyManager.getMessage("RentalItemIdNull"),
															  "RentalItemManagerBean.removeRentalItem");
		
		RentalItem rentalItem =  em.find(RentalItem.class, rentalItemId);
		
		if(rentalItem == null) 
				throw new BadInputDataException(PropertyManager.getMessage("NoRentalItem", rentalItemId),
												"RentalItemManagerBean.removeRentalItem");
		
		em.remove(rentalItem);		
	}

	
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#findAllRentalItems()
	 */
	@RolesAllowed("admin")
	@Override
	public List<RentalItem> findAllRentalItems() {
		
		TypedQuery<RentalItem> query = em.createNamedQuery("RentalItem.findAll", RentalItem.class);
		List<RentalItem> rentalItemsList = query.getResultList();
		
		return rentalItemsList;
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#findAllRentableItems()
	 */
		@Override
	public List<RentalItem> findAllRentableItems() {
		
		TypedQuery<RentalItem> query = em.createNamedQuery("RentalItem.findAllRentable", RentalItem.class);
		List<RentalItem> rentalItemsList = query.getResultList();
		
		return rentalItemsList;
	}
	

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#findRentalItem(long)
	 */
	public RentalItem findRentalItem(long rentalItemId) throws BadInputDataException {
		
		if (rentalItemId==0l) 
					throw new BadInputDataException(PropertyManager.getMessage("RentalItemIdNull"), 
													this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		return em.find(RentalItem.class, rentalItemId);
		
	}

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#findRentalItemsByItemCategoryAndItemSubCategory(java.lang.String, java.lang.String)
	 */
		@Override
		public List<RentalItem> findRentalItems(String itemCategory, String itemSubCategory, String itemBrand) throws BadInputDataException{
			
			if (itemSubCategory==null && itemCategory == null && itemBrand == null)
				throw new BadInputDataException(PropertyManager.getMessage("InputNull", "ItemCategory, ItemSubCategory, and ItemBrand"), 
												this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
			
			TypedQuery<RentalItem> query;		

			query = em.createNamedQuery("RentalItem.findByParams", RentalItem.class);
			
			if (itemCategory != null) 
				query.setParameter("itemCategory", itemCategory);
			else
				query.setParameter("itemCategory", "%"); 
			
			if (itemSubCategory != null)
				query.setParameter("itemSubCategory", itemSubCategory);
			else
				query.setParameter("itemSubCategory", "%"); 
			
			if (itemBrand != null)
				query.setParameter("itemBrand", itemBrand);
			else
				query.setParameter("itemBrand", "%"); 
				
			log.info("ItemCategory : "+ itemCategory + " ItemSubCategory : "+itemSubCategory+" ItemBrand : "+itemBrand);
			
			return query.getResultList();	
		}
		
	
/* (non-Javadoc)
 * @see com.pinaka.eRental.service.RentalItemManager#findRentalItemsByItemCategoryAndItemSubCategory(java.lang.String, java.lang.String)
 */
	@Override
	public List<RentalItem> findRentalItems(String itemCategory, String itemSubCategory, String itemBrand, boolean rentable) throws BadInputDataException{
		
		if (itemSubCategory==null && itemCategory == null && itemBrand == null)
			throw new BadInputDataException(PropertyManager.getMessage("InputNull", "ItemCategory, ItemSubCategory, and ItemBrand"), 
											this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		TypedQuery<RentalItem> query;
		
		query = em.createNamedQuery("RentalItem.findRentableByParams", RentalItem.class);

		
		if (itemCategory != null) 
			query.setParameter("itemCategory", itemCategory);
		else
			query.setParameter("itemCategory", "%"); 
		
		if (itemSubCategory != null)
			query.setParameter("itemSubCategory", itemSubCategory);
		else
			query.setParameter("itemSubCategory", "%"); 
		
		if (itemBrand != null)
			query.setParameter("itemBrand", itemBrand);
		else
			query.setParameter("itemBrand", "%"); 
		
		query.setParameter("rentable", rentable);
			
		log.info("ItemCategory : "+ itemCategory + " ItemSubCategory : "+itemSubCategory+" ItemBrand : "+itemBrand);
		
		return query.getResultList();	
	}
	
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#findRentalItemsByItemSubCategory(java.lang.String)
	 */
	@Override
	public List<RentalItem> findRentalItemsByItemSubCategory(String itemSubCategory) throws BadInputDataException{
		
		if (itemSubCategory==null) 
			throw new BadInputDataException(PropertyManager.getMessage("InputNull", "ItemSubCategory"), 
											this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());

		TypedQuery<RentalItem> query = em.createNamedQuery("RentalItem.findByItemSubCategory", RentalItem.class);
		query.setParameter("itemSubCategory", itemSubCategory);
		return query.getResultList();
	}
	

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#findRentalItemsByItemCategory(java.lang.String)
	 */
	@Override
	public List<RentalItem> findRentalItemsByItemCategory(String itemCategory) throws BadInputDataException{
		
		if (itemCategory==null) 
			throw new BadInputDataException(PropertyManager.getMessage("InputNull", "ItemCategory"), 
											this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());

		TypedQuery<RentalItem> query = em.createNamedQuery("RentalItem.findByItemCategory", RentalItem.class);
		query.setParameter("itemCategory", itemCategory);
		return query.getResultList();
	}
	


/* (non-Javadoc)
 * @see com.pinaka.eRental.service.RentalItemManager#findRentalItemsBySomeValue(java.lang.String)
 */

	
	@Override
	public List<RentalItem> findRentalItemsBySomeValue(String textSearchParam){
		
		if (textSearchParam == null) return this.findAllRentalItems();
			
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RentalItem> cq = cb.createQuery(RentalItem.class);
		Root<RentalItem> rentalItem = cq.from(RentalItem.class);
		// entity type not working in JBoss AS7. So Static model is used.
		//EntityType<RentalItem> RentalItem_ = em.getMetamodel().entity(RentalItem.class);//rentalItem.getModel();
		
		cq.select(rentalItem);
		cq.where(cb.or(cb.like(rentalItem.get(RentalItem_.name), "%"+textSearchParam+"%"),
				       cb.like(rentalItem.get(RentalItem_.itemBrand), "%"+textSearchParam+"%"),
				       cb.like(rentalItem.get(RentalItem_.itemCategory), "%"+textSearchParam+"%"),
				       cb.like(rentalItem.get(RentalItem_.itemSubCategory), "%"+textSearchParam+"%")
				));
				
		TypedQuery<RentalItem> query = em.createQuery(cq);
		List<RentalItem> rentalItemsList = query.getResultList();
		em.detach(rentalItemsList);
		for(RentalItem a : rentalItemsList) {
			a.setRentalInstances(null);
		}
		return rentalItemsList;
						
	}

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalItemManager#findRentableItemsBySomeValue(java.lang.String)
	 */
	@Override
	public List<RentalItem> findRentableItemsBySomeValue(String textSearchParam){
		
		if (textSearchParam == null) return this.findAllRentalItems();
			
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RentalItem> cq = cb.createQuery(RentalItem.class);
		Root<RentalItem> rentalItem = cq.from(RentalItem.class);
		// entity type not working in JBoss AS7. So Static model is used.
		//EntityType<RentalItem> RentalItem_ = em.getMetamodel().entity(RentalItem.class);//rentalItem.getModel();
		
		cq.select(rentalItem);
		cq.where(cb.and(cb.isTrue(rentalItem.get(RentalItem_.enableRenting)),
					    (cb.or(cb.like(rentalItem.get(RentalItem_.name), "%"+textSearchParam+"%"),
						       cb.like(rentalItem.get(RentalItem_.itemBrand), "%"+textSearchParam+"%"),
				               cb.like(rentalItem.get(RentalItem_.itemCategory), "%"+textSearchParam+"%"),
				               cb.like(rentalItem.get(RentalItem_.itemSubCategory), "%"+textSearchParam+"%")))
				 ));
				
		TypedQuery<RentalItem> query = em.createQuery(cq);
		List<RentalItem> rentalItemsList = query.getResultList();
		em.detach(rentalItemsList);
		for(RentalItem a : rentalItemsList) {
			a.setRentalInstances(null);
		}
		return rentalItemsList;
						
	}
	
//TODO - add search by availability
//TODO - add search by dates
//TODO - add search by location
	

}

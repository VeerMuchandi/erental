/**
 *
 * @filename RentalUserManagerBean.java
 * @author Veer Muchandi
 * @created Nov 3, 2012
 * © Copyright 2012 Pinaka LLC
 *
 */
package com.pinaka.eRental.service;

import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.annotation.security.SecurityDomain;

import com.pinaka.UserManagement.User;
import com.pinaka.UserManagement.UserManager;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.util.PropertyManager;

/**
 * @filename RentalUserManagerBean.java
 * @author Veer Muchandi
 * @created Nov 3, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
@Stateless
@Local(RentalUserManager.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@SecurityDomain("other")
@DeclareRoles({"admin"})
public class RentalUserManagerBean implements RentalUserManager {

	
	@Inject
	EntityManager em;
	@Inject
	Logger log;
	@Inject 
	UserManager userManager;
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.RentalUserManager#findUser(java.lang.String)
	 */
	@Override
	public RentalUser findUser(User user) {
		return em.find(RentalUser.class, user.getUserName());
	}

	@Override
	public RentalUser addUser(RentalUser rentalUser) throws BadInputDataException {
		
		if (rentalUser==null) 
			throw new BadInputDataException(PropertyManager.getMessage("RentalUserNull"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if (rentalUser.getUser() == null)
			throw new BadInputDataException(PropertyManager.getMessage("UserNull"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if (userManager.findUser(rentalUser.getUser().getUserName()) == null)
			throw new BadInputDataException(PropertyManager.getMessage("UserNotFound", rentalUser.getUser().getUserName()),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
	
	try {
		//use email as the user name by default	
		rentalUser.setUserId(rentalUser.getUser().getUserName());
		rentalUser.setUser(rentalUser.getUser());
		em.persist(rentalUser);
		em.flush();
		
		return em.find(RentalUser.class, rentalUser.getUser().getUserName());
	} catch (Exception e) {
		e.printStackTrace();
		return null;
	}
	}

	
	@Override
	@RolesAllowed("admin")
	public void removeUser(String userName) {
		RentalUser ruser =	em.find(RentalUser.class, userName);
		em.remove(ruser);
		em.flush();
		
	}
	

	

}

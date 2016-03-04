package com.pinaka.eRental.serviceFacade;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.annotation.security.SecurityDomain;

import com.pinaka.UserManagement.ActivationDTO;
import com.pinaka.UserManagement.User;
import com.pinaka.UserManagement.UserManager;
import com.pinaka.UserManagement.UserMgmtServiceBean;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.Rental;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.service.RentalUserManager;
import com.pinaka.eRental.util.PropertyManager;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@SecurityDomain("other")
@DeclareRoles({"user","admin"})
public class RentalUserMgmtServiceBean implements RentalUserMgmtService {
	
		@Inject
		UserManager userManager;
		@Inject
		RentalUserManager rentalUserManager;
		@Inject
		Logger log;
		@Resource 
		SessionContext ctx;

		
		/* (non-Javadoc)
		 * @see com.pinaka.eRental.serviceFacade.RentalUserMgmtService#createRentalUser(com.pinaka.UserManagement.User)
		 */
		@Override
		public RentalUser createRentalUser(User user) throws BadInputDataException {
			
			if(user==null)
				throw new BadInputDataException(PropertyManager.getMessage("UserNull"),
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
			
			if(userManager.findUser(user.getUserName())==null)
				throw new BadInputDataException(PropertyManager.getMessage("UserNotFound", user.getUserName()),
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
			
			//if rental user already exists, return the existing return user
			RentalUser ru = rentalUserManager.findUser(user);		
			if (ru != null) return ru;
			
			//create a new rental user
			RentalUser rentalUser = new RentalUser();
			rentalUser.setUser(user);
			rentalUser.setUserId(user.getUserName());
			
			RentalUser added = rentalUserManager.addUser(rentalUser);
			return added;
			
		}
}

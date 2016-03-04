package com.pinaka.UserManagement;

import java.util.List;

import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
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

import org.jboss.annotation.security.SecurityDomain;

import com.pinaka.eRental.exception.BadInputDataException;

import com.pinaka.eRental.util.PropertyManager;

@Stateless
@Local(UserManager.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
@SecurityDomain("other")
@DeclareRoles({"admin"})
public class UserManagerBean implements UserManager {

	@Inject
	EntityManager em;
	@Inject
	Logger log;
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#addUser(com.pinaka.eRental.model.User)
	 */
	@Override
	public User addUser(User user) throws BadInputDataException{
		
		if (user==null) 
				throw new BadInputDataException(PropertyManager.getMessage("UserNull"),
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		try {
			//use email as the user name by default	
			if (user.getUserName()==null) user.setUserName(user.getEmail().toLowerCase());
			else user.setUserName(user.getUserName().toLowerCase());
			user.setEmail(user.getEmail().toLowerCase());
			em.persist(user);
			em.flush();
			
			return em.find(User.class, user.getUserName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#updateUser(com.pinaka.eRental.model.User)
	 */
	@Override
	public User updateUser(User user) throws BadInputDataException {
		
		if (user==null) 
					throw new BadInputDataException(PropertyManager.getMessage("UserNull"),
							this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if(em.find(User.class, user.getUserName()) == null) {
			User added =  this.addUser(user);
			log.info("UpdateUser() - User not found. New User added with Id:" + added.getUserName());
			return added;
		} else {
			User updated = em.merge(user);
			return updated;
		}
	}

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#removeUser(long)
	 */
	@Override
	@RolesAllowed("admin")
	public void removeUser(String userName) {
		User user =	em.find(User.class, userName);
		em.remove(user);
	}

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#findAllUsers()
	 */
	@Override
	public List<User> findAllUsers() {
		
		TypedQuery<User> query = em.createNamedQuery("User.findAll", User.class);
		
		List<User> usersList = query.getResultList();
		
		return usersList;
	}

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#findUser(long)
	 */
	@Override
	public User findUser(String userName) {

		return em.find(User.class, userName);
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#findUserBySomeValue(java.lang.String)
	 */
	@Override

	public List<User> findUserBySomeValue(String textSearchParam){
		
		if (textSearchParam == null) return this.findAllUsers();
			
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> user = cq.from(User.class);
		
		cq.select(user);
		cq.where(cb.or(cb.like(user.get(User_.userName), "%"+textSearchParam+"%"),
				       cb.like(user.get(User_.firstName), "%"+textSearchParam+"%"),
				       cb.like(user.get(User_.lastName), "%"+textSearchParam+"%"),
				       cb.like(user.get(User_.email), "%"+textSearchParam+"%")
				       ));
				
		TypedQuery<User> query = em.createQuery(cq);
		return query.getResultList();		
					
	}
	

	
	@Override
	public List<User> findUsers(String firstName,
											   String lastName
													) throws BadInputDataException {
													
		if (firstName==null && lastName==null) 
			throw new BadInputDataException(PropertyManager.getMessage("InputNull", "FirstName and LastName"), 
											this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());

		TypedQuery<User> query;

		query = em.createNamedQuery("User.findByParams", User.class);
		
		if (firstName != null)
			query.setParameter("firstName", firstName);
		else
			query.setParameter("firstName", "%");
		
		if (lastName != null)
			query.setParameter("lastName", lastName);
		else
			query.setParameter("lastName", "%");
		
		query.setParameter("email", "%");
		query.setParameter("userName", "%");

		return query.getResultList();
		}
		
			
}

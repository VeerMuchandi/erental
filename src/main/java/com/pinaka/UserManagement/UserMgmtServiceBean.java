package com.pinaka.UserManagement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.annotation.security.SecurityDomain;

import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.util.EmailProcessor;
import com.pinaka.eRental.util.PropertyManager;


/**
 * @filename UserMgmtServiceBean.java
 * @author Veer Muchandi
 * @created Apr 18, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * UserMgmtService
 */

@Path("/")
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@SecurityDomain("other")
@DeclareRoles({"user","admin"})
public class UserMgmtServiceBean implements UserMgmtService {
	
	@Inject
	UserManager userManager; 
	@Inject
	EntityManager em;
	@Inject
	Logger log;
	@Inject
	PropertyManager pm;
	@Inject
	EmailProcessor emailProcessor;
	@Resource 
	SessionContext ctx;
	
	
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#findUser(java.lang.String)
	 */
	@Override
//	@RolesAllowed({"user","admin"})
	public User findUser(@PathParam("userName") String userName) throws BadInputDataException {
		
		log.info("Principal: "+ ctx.getCallerPrincipal());		
		
		
		if (!userName.equals(ctx.getCallerPrincipal().getName())){
				if (!ctx.isCallerInRole("admin")) 
						throw new BadInputDataException(PropertyManager.getMessage("UserMismatch"),
									this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		
		User user = userManager.findUser(userName);
		
		if (user == null) return null;
		
		log.info("Roles size:"+ user.getRoles().size());
				
		User shallowCopy = (User) user.clone();
		shallowCopy.setPassword(null); //we don't want to send the password back

		return (User)shallowCopy;
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#getUserDetails()
	 */
	@Override
//	@RolesAllowed({"user","admin"})
	public User getUserDetails() throws BadInputDataException {
			if (ctx.getCallerPrincipal().getName().equals("anonymous"))
				throw new BadInputDataException(PropertyManager.getMessage("UserMismatch"),
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());

			return findUser(ctx.getCallerPrincipal().getName());
	}

	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#findAllUsers()
	 */
	@Override
//	@RolesAllowed("admin")
	public List<User> findAllUsers() {
		
		List<User> usersList = userManager.findAllUsers();
		
		// returning the detached rentalItemsList with the lazy loading objects removed
		em.detach(usersList);
		for(User a : usersList) {
			em.detach(a);
			a.setPassword(null);
			}
		return usersList;
	}
	

	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#findUsers(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
//	@RolesAllowed("admin")
	public List<User> findUsers(@QueryParam("firstname") String firstName,
								@QueryParam("lastname") String lastName,
								@QueryParam("fullSearch") String textSearchParam) throws BadInputDataException{
		
		if (textSearchParam != null) return this.findUserBySomeValue(textSearchParam);
		else return this.findUserByFirstLastName(firstName, lastName);
		
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#findUserBySomeValue(java.lang.String)
	 */
	@Override
//	@RolesAllowed("admin")
	public List<User> findUserBySomeValue(String textSearchParam){
		
		log.info("In FindUser full search");
		
		List <User> usersList = null;
		
		usersList = userManager.findUserBySomeValue(textSearchParam);
			
		// returning the detached rentalItemsList with the lazy loading objects removed
		em.detach(usersList);
		for(User a : usersList) {
			em.detach(a);
			a.setPassword(null);
			}
		return usersList;
						
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#findUserByFirstLastName(java.lang.String, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#findUserByFirstLastName(java.lang.String, java.lang.String)
	 */
	@Override
//	@RolesAllowed("admin")
	public List<User> findUserByFirstLastName ( String firstName,
												String lastName) throws BadInputDataException {
		
		log.info("In FindUsers by firstName and lastName");
		List<User> usersList = null;
		//if the params are null return all users											
		if (firstName==null && lastName==null) usersList = userManager.findAllUsers();
		else usersList = userManager.findUsers(firstName, lastName);

		// returning the detached rentalItemsList with the lazy loading objects removed
		em.detach(usersList);
		for(User a : usersList) {
			em.detach(a);
			a.setPassword(null);
		}
		return usersList;
			
		}
	
	//TODO - this call should be allowed only on SSL
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#signup(com.pinaka.UserManagement.User)
	 */
	@Override
	@Produces("text/xml")
	public User signup(User user) throws BadInputDataException{

		if(user==null) 
			throw new BadInputDataException(PropertyManager.getMessage("UserNull"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());		
		
		//User cannot set the role while signing up. The system assigns (forces) a default role of "user"
		if(user.getRoles().size()>0) user.setRoles(null);
		user.setActivated(false);
			
		if (user.getUserName()==null) user.setUserName(user.getEmail().toLowerCase());
		
		if(userManager.findUser(user.getUserName())!=null)
			throw new BadInputDataException(PropertyManager.getMessage("UserExists",user.getUserName()),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		log.info("realm: "+PropertyManager.getProp("realm"));
		//Password is passed as plain text inside the user object. Hence the call should be over SSL
		String encryptedPassword = UserMgmtServiceBean.md5Hex(user.getUserName()+":"+PropertyManager.getProp("realm")+":"+user.getPassword());

		user.setPassword(encryptedPassword);
		User added = userManager.addUser(user);
		//TODO - check if the user is successfully added here. The added.setRoles below fails if the user was not added due to constraints on the user object.
		
		Role userRole = new Role("user", "Roles");
		
		List<Role> roles = new ArrayList<Role>();
		roles.add(userRole);
		added.setRoles(roles);
		
		User updated = userManager.addUser(added);

		emailProcessor.sendEmail(updated.getEmail(), 
				PropertyManager.getMessage("UserSignedUpSubject"), 
				PropertyManager.getMessage("UserSignedUpContent", updated.generateFullName(), this.createActivationKey(updated)));
		//TODO - add activation url in the message above
		
		log.info("Act key:"+this.createActivationKey(updated));
		
		User shallowCopy = (User) updated.clone();
		shallowCopy.setPassword(null); //we don't want to send the password back

		return shallowCopy;
				
	}
	
	//TODO - change the query in the standalone config to prevent an inactive user logging in.
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#activateUser(java.lang.String, com.pinaka.UserManagement.ActivationDTO)
	 */
	@Override
//	@RolesAllowed("user")
	public User activateUser(@PathParam("userName") String userName, ActivationDTO activationInfo) throws BadInputDataException {
		
		User user = userManager.findUser(userName);
		if (user==null) 
			throw new BadInputDataException(PropertyManager.getMessage("UserNotFound"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		log.info("Act key from user: "+ userName +" is "+activationInfo.getActivationKey());
		if (!activationInfo.getActivationKey().equals(UserMgmtServiceBean.createActivationKey(user))) 
			throw new BadInputDataException(PropertyManager.getMessage("InvalidActivationKey"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		user.setActivated(true);
		
		User updated = userManager.updateUser(user);
		
		User shallowCopy = (User) updated.clone();
		shallowCopy.setPassword(null); //we don't want to send the password back
		//TODO - Send an email to user's email address that the user is activated
		return shallowCopy;
		
	}
	
	//TODO - SSL call only
	
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#changePassword(java.lang.String, com.pinaka.UserManagement.PasswordChangeDTO)
	 */
	@Override
//	@RolesAllowed({"user","admin"})
	public User changePassword(@PathParam("userName") String userName, PasswordChangeDTO pwdChangeInfo) throws BadInputDataException{
				
		User user = userManager.findUser(userName);
		if (user==null) 
			throw new BadInputDataException(PropertyManager.getMessage("UserNotFound"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if (!userName.equals(ctx.getCallerPrincipal().getName()))
			throw new BadInputDataException(PropertyManager.getMessage("UserMismatch"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if(!user.isActivated())
			throw new BadInputDataException(PropertyManager.getMessage("UserNotActive"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if (!user.getPassword().equals(UserMgmtServiceBean.md5Hex(user.getUserName()+":"+PropertyManager.getProp("realm")+":"+pwdChangeInfo.getOldPassword())))
			throw new BadInputDataException(PropertyManager.getMessage("PasswordIncorrect"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());			
		
		if(!pwdChangeInfo.getNewPassword().equals(pwdChangeInfo.getRetypedPassword())) 
			throw new BadInputDataException(PropertyManager.getMessage("PasswordMismatch"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());				
			
		user.setPassword(UserMgmtServiceBean.md5Hex(user.getUserName()+":"+PropertyManager.getProp("realm")+":"+pwdChangeInfo.getNewPassword()));
		
		User updated = userManager.updateUser(user);
		
		User shallowCopy = (User) updated.clone();
		shallowCopy.setPassword(null); //we don't want to send the password back
		//TODO - Send an email to user's email address to that the password has been reset
		return shallowCopy;	
	
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#addAdministrator(com.pinaka.UserManagement.AdminUserDTO)
	 */
	@Override
//	@RolesAllowed("admin")
	public User addAdministrator(AdminUserDTO admin) throws BadInputDataException{
		
		if (admin.getUserName()==null) admin.setUserName(admin.getEmail());
		if(userManager.findUser(admin.getEmail())!=null) {
			
			//if the user is already an admin, throw exception
			User currentUser = userManager.findUser(admin.getEmail());
			if (currentUser.isAdministrator())
						throw new BadInputDataException(PropertyManager.getMessage("AlreadyAdmin",admin.getEmail()),
								this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
			
			//otherwise make the existing user an administrator
			Role userRole = new Role("admin", "Roles");
			currentUser.addRole(userRole);
			
			emailProcessor.sendEmail(currentUser.getEmail(), 
					PropertyManager.getMessage("AdminAssignedSubject"), 
					PropertyManager.getMessage("AdminAssignedContent", currentUser.generateFullName()));
			
			
			User shallowCopy = (User) currentUser.clone();
			shallowCopy.setPassword(null); //we don't want to send the password back

			return shallowCopy; 
			
		}
		//generate a random password
		String tempPassword = UserMgmtServiceBean.md5Hex(admin.getUserName()+Calendar.getInstance().toString()).substring(3,10);
		
		//encrypt and store the tempPassword so that changePassword can compare it using regular logic
		String encryptedPassword = UserMgmtServiceBean.md5Hex(admin.getUserName()+":"+PropertyManager.getProp("realm")+":"+tempPassword);
		
		User administrator = new User(admin.getUserName(), 
										encryptedPassword, 
										admin.getTitle(),
										admin.getFirstName(), 
										admin.getLastName(), 
										admin.getEmail(),
										null,
										admin.getPhones());
		
		administrator.setActivated(true); //activate the administrator by default
		Role userRole = new Role("admin", "Roles");
		
		List<Role> roles = new ArrayList<Role>();
		roles.add(userRole);
		administrator.setRoles(roles);
		
		User added = userManager.addUser(administrator);
		
		//Send an email to admin's email address with the tempPassword (don't send encrypted password)
		emailProcessor.sendEmail(added.getEmail(), 
				PropertyManager.getMessage("AdminAddedSubject"), 
				PropertyManager.getMessage("AdminAddedContent", added.generateFullName(),added.getUserName(), tempPassword));
		
		
		User shallowCopy = (User) administrator.clone();
		shallowCopy.setPassword(null); //we don't want to send the password back

		return shallowCopy; 
		
	}
	
	/* (non-Javadoc)
	 * @see com.pinaka.UserManagement.UserMgmtService#updateUser(java.lang.String, com.pinaka.UserManagement.User)
	 */
	@Override
//	@RolesAllowed({"user","admin"})
	public User updateUser(@PathParam("userName") String userName, User user) throws BadInputDataException {
		
		if (!userName.equals(ctx.getCallerPrincipal().getName()))
			throw new BadInputDataException(PropertyManager.getMessage("UserMismatch"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if(user==null) 
			throw new BadInputDataException(PropertyManager.getMessage("UserNull"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());		
		
		User foundUser = userManager.findUser(userName);
		if(foundUser == null)
			throw new BadInputDataException(PropertyManager.getMessage("UserNotFound", user.getUserName()),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if(!foundUser.isActivated())
			throw new BadInputDataException(PropertyManager.getMessage("UserNotActive"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
				
		if (user.getPassword() != null)
			throw new BadInputDataException(PropertyManager.getMessage("UsePwdChange"),
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		
		if (user.getUserName()!=null) {
			if (!user.getUserName().equals(userName)) 
				throw new BadInputDataException(PropertyManager.getMessage("InconsistentId", userName, user.getUserName()), 
						this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		else
			user.setUserName(userName);
			
		
		user.setActivated(foundUser.isActivated()); //Cannot activate using this method if the user is not already activated!
		user.setPassword(foundUser.getPassword()); //set password to the current value
		user.setRoles(foundUser.getRoles());
		
		User updated = userManager.updateUser(user);
		
		User shallowCopy = (User) updated.clone();
		shallowCopy.setPassword(null); //we don't want to send the password back

		return shallowCopy; 
		
	}
	
	//TODO - implement reset password using secret questions
	
    private static String md5Hex(String data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
 
        return new String(encodeStringToHex(digest.digest(data.getBytes())));
    }

   private static String encodeStringToHex(byte[] rawData){

	StringBuffer hexText = new StringBuffer();
	String initialHex = null;
	int initHexLength = 0;
	for (int i = 0; i < rawData.length; i++) {
		int positiveValue = rawData[i] & 0x000000FF;
		initialHex = Integer.toHexString(positiveValue);
		initHexLength = initialHex.length();
		while (initHexLength++ < 2) {
			hexText.append("0");
			}

		hexText.append(initialHex);
		}
	return hexText.toString();
	}
   
   private static String createActivationKey(User user) {
		String temp = UserMgmtServiceBean.md5Hex(user.getUserName()+":"+user.generateFullName()+":"+user.getPassword()).toString();
		if(temp.length()<8) temp.concat(temp);
		String activationKey = temp.substring(Math.round(user.generateFullName().length()/5),8);
		return activationKey;
   }

}

package com.pinaka.UserManagement;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;

import com.pinaka.eRental.exception.BadInputDataException;

public interface UserMgmtService {

	@GET
	@Path("/users/{user-name}")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user", "admin" })
	public abstract User findUser(@PathParam("user-name") String userName)
			throws BadInputDataException;

	@GET
	@Path("/users/my-user-information")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user", "admin" })
	public abstract User getUserDetails() throws BadInputDataException;

	@RolesAllowed("admin")
	public abstract List<User> findAllUsers();

	@GET
	@Produces({"application/xml", "application/json"})
	@Path("/users")
	@RolesAllowed("admin")
	public abstract List<User> findUsers(
			@QueryParam("first-name") String firstName,
			@QueryParam("last-name") String lastName,
			@QueryParam("fullSearch") String textSearchParam)
			throws BadInputDataException;

	@RolesAllowed("admin")
	public abstract List<User> findUserBySomeValue(String textSearchParam);

	/* (non-Javadoc)
	 * @see com.pinaka.eRental.service.UserManager#findUserByFirstLastName(java.lang.String, java.lang.String)
	 */
	@RolesAllowed("admin")
	public abstract List<User> findUserByFirstLastName(String firstName,
			String lastName) throws BadInputDataException;

	//TODO - this call should be allowed only on SSL
	/**
	 * @param user
	 * @return
	 * @throws BadInputDataException
	 * 
	 * A user can register oneself using this method. The user will not be activated here. An activation key is sent back to the user
	 * over email.
	 * 
	 */
	@POST
	@Path("/users")
	@Produces({"application/xml", "application/json"})
	public abstract User signup(User user) throws BadInputDataException;

	//TODO - change the query in the standalone config to prevent an inactive user logging in.
	/**
	 * @param userName
	 * @param activationInfo
	 * @return
	 * @throws BadInputDataException
	 * 
	 * Activates a user that is added. The activation key was sent to the user over email - no one else knows about it. 
	 * Here we expect the same activation key to be sent back by the user. Here the activation key is verified before
	 * activating the user.
	 * 
	 */
	@PUT
	@Path("/users/{user-name}/activate")
	@Consumes({"text/xml","application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@RolesAllowed("user")
	public abstract User activateUser(@PathParam("user-name") String userName,
			ActivationDTO activationInfo) throws BadInputDataException;

	/**
	 * @param userName
	 * @param pwdChangeInfo
	 * @return
	 * @throws BadInputDataException
	 * 
	 * The client can change password by providing old password, new password and a re-confirmation of the new password. The user must have 
	 * logged in in order to perform this operation.
	 * 
	 */
	@PUT
	@Path("/users/{user-name}/pwdchange")
	@Consumes({"text/xml","application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user", "admin" })
	public abstract User changePassword(@PathParam("user-name") String userName,
			PasswordChangeDTO pwdChangeInfo) throws BadInputDataException;

	/**
	 * @param admin
	 * @return
	 * @throws BadInputDataException
	 * 
	 * An Administrator can add another administrator using this method. A temporary password is generated and sent privately to the
	 * administrator being added. The newly added administrator is expected to change this password to one of their choice 
	 * using change password method.
	 * 
	 */
	@POST
	@Path("/administrators")
	@Consumes({"text/xml","application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@RolesAllowed("admin")
	public abstract User addAdministrator(AdminUserDTO admin)
			throws BadInputDataException;

	/**
	 * @param userName
	 * @param user
	 * @return
	 * @throws BadInputDataException
	 * 
	 * A user can update their information using this method. A user is allowed to change their own information - no others
	 * This method does not allow activation or password change.
	 * 
	 */
	@PUT
	@Path("/users/{user-name}")
	@Consumes({"text/xml","application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user", "admin" })
	public abstract User updateUser(@PathParam("user-name") String userName,
			User user) throws BadInputDataException;

}
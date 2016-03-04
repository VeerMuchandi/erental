package com.pinaka.UserManagement;

import java.util.List;

import javax.annotation.security.DeclareRoles;

import org.jboss.annotation.security.SecurityDomain;

import com.pinaka.eRental.exception.BadInputDataException;

@SecurityDomain("other")
@DeclareRoles({"admin"})
public interface UserManager {

	public abstract User addUser(User user) throws BadInputDataException;

	public abstract User updateUser(User user) throws BadInputDataException;

	public abstract void removeUser(String userName);

	public abstract List<User> findAllUsers();

	public abstract User findUser(String userName);

	public abstract List<User> findUserBySomeValue(String textSearchParam);

	public abstract List<User> findUsers(String firstName, String lastName)
					throws BadInputDataException;


}
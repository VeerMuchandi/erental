package com.pinaka.eRental.service;

import javax.ejb.Stateless;

import com.pinaka.UserManagement.User;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.RentalUser;


public interface RentalUserManager {
	
	public abstract RentalUser addUser(RentalUser user) throws BadInputDataException;
	
	public abstract RentalUser findUser(User user);
	
	public abstract void removeUser(String userName);

}

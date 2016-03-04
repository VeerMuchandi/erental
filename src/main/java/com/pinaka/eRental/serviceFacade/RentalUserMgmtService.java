package com.pinaka.eRental.serviceFacade;

import com.pinaka.UserManagement.User;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.RentalUser;

public interface RentalUserMgmtService {

	public abstract RentalUser createRentalUser(User user)
			throws BadInputDataException;

}
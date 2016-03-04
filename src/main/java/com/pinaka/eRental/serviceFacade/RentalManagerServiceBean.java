package com.pinaka.eRental.serviceFacade;

import java.util.Calendar;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.jboss.annotation.security.SecurityDomain;

import com.pinaka.UserManagement.User;
import com.pinaka.UserManagement.UserManager;
import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.exception.InvalidBusinessStateException;
import com.pinaka.eRental.model.RejectionReasonType;
import com.pinaka.eRental.model.RentalInstance;
import com.pinaka.eRental.model.RentalItem;
import com.pinaka.eRental.model.RentalLocation;
import com.pinaka.eRental.model.RentalStatusType;
import com.pinaka.eRental.model.RentalUser;
import com.pinaka.eRental.service.RentalItemManager;
import com.pinaka.eRental.util.EmailProcessor;
import com.pinaka.eRental.util.PropertyManager;



/**
 * @filename RentalManagerServiceBean.java
 * @author Veer Muchandi
 * @created Mar 27, 2012
 *
 * This business service manages the life cycle of rental requests
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
@Stateless
@Local(RentalManagerService.class)
public  class RentalManagerServiceBean implements RentalManagerService {
	

@Inject
EntityManager em;
@Inject
EmailProcessor emailProcessor;
@Inject
UserManager userManager;
@Inject 
RentalUserMgmtService rentalUserMgmtService;
@Inject 
PropertyManager pm;
@Inject
Logger log;
@Resource 
SessionContext ctx;

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#requestRental(long, java.util.Calendar, java.util.Calendar)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance requestRental(long rentalItemId, 
									Calendar plannedStartDate, 
									Calendar plannedReturnDate) throws BadInputDataException, InvalidBusinessStateException {
	
	String userName = ctx.getCallerPrincipal().getName();
		
	RentalUser renter = em.find(RentalUser.class, userName);
	
	if (renter == null) {
		//Sometimes the user logged in is borrowing for the first time. User may be a valid user of the system but not registered as a
		//rental user. For such cases, make borrower a rental user.
		User loggedInUser = userManager.findUser(userName);
		renter = rentalUserMgmtService.createRentalUser(loggedInUser);
	}
			
	
	RentalItem ri = em.find(RentalItem.class, rentalItemId);
	
	if (ri == null) 
			throw new BadInputDataException(PropertyManager.getMessage("NoRentalItem",
																		rentalItemId),
																		"RentalManager.makeARentalRequest");
	
	if (!ri.getEnableRenting()) 
			throw new InvalidBusinessStateException(PropertyManager.getMessage("RentingDisabled", 
																				ri.getId(), 
																				ri.getName()), 
																				"RentalManager.makeARentalRequest");
	
	// TODO - if currently rented
	// TODO - for some items owner may not want to verify the borrower's information. In such cases, add logic to directly rent
	
	// TODO - Add rental location chosen by the borrower to rentalInstance
	
	if(!plannedReturnDate.after(plannedStartDate)) throw new BadInputDataException(PropertyManager.getMessage("InvalidRentalDates", plannedStartDate.getTime(), plannedReturnDate.getTime()),"RentalManager.makeARentalRequest");
	
	RentalInstance rentalInstance = new RentalInstance();
	
	rentalInstance.setStatus(RentalStatusType.REQUESTED);
	rentalInstance.setPlannedStartDate(plannedStartDate);
	rentalInstance.setPlannedReturnDate(plannedReturnDate);
	rentalInstance.setBorrower(renter);
	rentalInstance.setRentedItem(ri);
	
	// TODO- calculate expected rent amount
	
	RentalUser owner = ri.getOwner();
	
	emailProcessor.sendEmail(owner.getUser().getEmail(), 
								PropertyManager.getMessage("RentalRequestSubject", ri.getId(), ri.getName()),
								PropertyManager.getMessage("RentalRequestEmailContent", ri.getId(), ri.getName()));
	
	em.persist(rentalInstance);
	em.flush();
	
	
	//detaching and hiding the owner information before returning as the owner is hidden from the borrower till the owner approves the request
	
	RentalInstance deepCopy = rentalInstance.deepCopy();
	deepCopy.getRentedItem().setOwner(null);		
	deepCopy.getBorrower().setRentedItems(null);
	deepCopy.getBorrower().setOwnedItems(null);
	deepCopy.getRentedItem().setRentalInstances(null);
	deepCopy.getBorrower().getUser().setPassword(null);
		
	return deepCopy;
	
}

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#approveRentalRequest(long)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance approveRentalRequest(long rentalRequestId) throws BadInputDataException, InvalidBusinessStateException {
	

	//TODO - how does owner suggest different time?
	
	RentalInstance rentalRequest = em.find(RentalInstance.class, rentalRequestId);
	if (rentalRequest == null) throw new BadInputDataException(PropertyManager.getMessage("RentalInstanceIdInvalid",rentalRequestId), "RentalManager.approveRentalRequest");
	

	//verify that the user is owner
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("OwnerIdInvalid", userName), 
																		"RentalManager.approveRentalRequest");
	if (!loggedInUser.getUser().getUserName().equals(rentalRequest.getRentedItem().getOwner().getUser().getUserName()))
			throw new BadInputDataException(PropertyManager.getMessage("NotAnOwner", userName), 
																		"RentalManager.approveRentalRequest");		
	
	
	if(rentalRequest.getStatus() != RentalStatusType.REQUESTED) throw new InvalidBusinessStateException(PropertyManager.getMessage("InvalidStatus", "RentalInstance",rentalRequest.getStatus()),"RentalManager.approveRentalRequest");
	
	//owner approved renting.
	rentalRequest.setStatus(RentalStatusType.OWNER_APPROVED);
	rentalRequest.getRentedItem().setCurrentlyRented(true);
	//TODO - add log to record the status change
	//TODO - add logic to clear the currently rented flag after holdTime
	
	RentalUser borrower = rentalRequest.getBorrower();
	
	//TODO - make holdTime configurable by the owner
	String holdTime = "1 day";
	//TODO - We may want to track no responses from borrower as well
	
	//send an email to the borrower that the rental request is approved
	emailProcessor.sendEmail(borrower.getUser().getEmail(), 
			PropertyManager.getMessage("RentalApprovalSubject",rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName()), 
			PropertyManager.getMessage("RentalApprovalEmailContent", borrower.getUser().getFirstName()+" "+borrower.getUser().getLastName(),rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName(), holdTime));
	
	log.info("approval email sent");
	
	RentalInstance updated = em.merge(rentalRequest);
	em.flush();
	
		
	RentalInstance deepCopy = updated.deepCopy();
	
	deepCopy.getRentedItem().getOwner().setRentedItems(null);
	deepCopy.getRentedItem().getOwner().setOwnedItems(null);
	deepCopy.getBorrower().setRentedItems(null);
	deepCopy.getBorrower().setOwnedItems(null);
	
//	deepCopy.getBorrower().setEmail(null);
//	deepCopy.getBorrower().setUserName(null);
	deepCopy.getBorrower().getUser().setPhones(null);
	deepCopy.getBorrower().getUser().setPassword(null);
	deepCopy.getBorrower().getUser().setRoles(null);
	deepCopy.getBorrower().getUser().getAddress().setAddress1(null);
	deepCopy.getBorrower().getUser().getAddress().setAddress2(null);
	
	deepCopy.getRentedItem().getOwner().getUser().setPassword(null);
	deepCopy.getRentedItem().setRentalInstances(null);
	
	return deepCopy;
	
}


/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#rejectRentalRequest(long, com.pinaka.eRental.model.RejectionReasonType, boolean)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance rejectRentalRequest(long rentalRequestId, 
										RejectionReasonType reasonForRejection, 
										boolean shareRejectionReasonWithBorrower) throws BadInputDataException, InvalidBusinessStateException {

	if(reasonForRejection == RejectionReasonType.OTHER_REASON) 
			throw new BadInputDataException(PropertyManager.getMessage("ProvideOtherReason"), 
																		"RentalManager.rejectRentalRequest");
	return this.rejectRentalRequest(rentalRequestId, reasonForRejection, null, shareRejectionReasonWithBorrower);
	
}	

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#rejectRentalRequest(long, com.pinaka.eRental.model.RejectionReasonType, java.lang.String, boolean)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance rejectRentalRequest(long rentalRequestId, RejectionReasonType reasonForRejection, String otherReason, boolean shareRejectionReasonWithBorrower) throws BadInputDataException, InvalidBusinessStateException {
	
	RentalInstance rentalRequest = em.find(RentalInstance.class, rentalRequestId);
	if (rentalRequest == null) throw new BadInputDataException(PropertyManager.getMessage("RentalInstanceIdInvalid",rentalRequestId), "RentalManager.rejectRentalRequest");
	
	//verify that the user is owner
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("OwnerIdInvalid", userName), 
																		"RentalManager.rejectRentalRequest");
	if (!loggedInUser.getUser().getUserName().equals(rentalRequest.getRentedItem().getOwner().getUser().getUserName()))
			throw new BadInputDataException(PropertyManager.getMessage("NotAnOwner", userName), 
																		"RentalManager.rejectRentalRequest");	
	
	if(rentalRequest.getStatus() != RentalStatusType.REQUESTED) throw new InvalidBusinessStateException(PropertyManager.getMessage("InvalidStatus", "RentalInstance",rentalRequest.getStatus()),"RentalManager.rejectRentalRequest");
	
	if(reasonForRejection == RejectionReasonType.OTHER_REASON && otherReason == null) 
		throw new BadInputDataException(PropertyManager.getMessage("ProvideOtherReason"), 
																	"RentalManager.rejectRentalRequest");
	
	//owner rejected renting.
	rentalRequest.setStatus(RentalStatusType.OWNER_REJECTED);
	rentalRequest.getRentedItem().setCurrentlyRented(false);
	rentalRequest.setRejectionReason(reasonForRejection);
	if(reasonForRejection == RejectionReasonType.OTHER_REASON) rentalRequest.setOtherRejectionReason(otherReason);
	rentalRequest.setRejectionReasonSharable(shareRejectionReasonWithBorrower);
	
	//TODO - add log to record the status change
	
	RentalUser borrower = rentalRequest.getBorrower();
	
	//TODO - We may want to track no responses from owner 
	
	//send an email to the borrower that the rental request is rejected
	
	String rejectionReasonInEmail;
	if(shareRejectionReasonWithBorrower)  {
		rejectionReasonInEmail = reasonForRejection.toString();
		if (reasonForRejection == RejectionReasonType.OTHER_REASON) rejectionReasonInEmail = otherReason;
	} else
		rejectionReasonInEmail = "OWNER DID NOT AUTHORIZE DISCLOSURE OF REASON";
	//TODO - future inquires need to account for this flag. Don't show the RejectionReason to borrowers
	
	emailProcessor.sendEmail(borrower.getUser().getEmail(), 
			PropertyManager.getMessage("RentalRejectionSubject",rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName()), 
			PropertyManager.getMessage("RentalRejectionEmailContent", borrower.getUser().generateFullName(),rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName(), rejectionReasonInEmail));
	
	RentalInstance updated = em.merge(rentalRequest);
	em.flush();
	
	RentalInstance deepCopy = updated.deepCopy();
	
	deepCopy.getRentedItem().getOwner().setRentedItems(null);
	deepCopy.getRentedItem().getOwner().setOwnedItems(null);
	deepCopy.getBorrower().setRentedItems(null);
	deepCopy.getBorrower().setOwnedItems(null);
	
	deepCopy.getBorrower().getUser().setEmail(null);
	deepCopy.getBorrower().getUser().setUserName(null);
	deepCopy.getBorrower().getUser().setPhones(null);
	deepCopy.getBorrower().getUser().setPhones(null);
	deepCopy.getBorrower().getUser().setPassword(null);
	deepCopy.getBorrower().getUser().setRoles(null);
	deepCopy.getBorrower().getUser().getAddress().setAddress1(null);
	deepCopy.getBorrower().getUser().getAddress().setAddress2(null);
	
	deepCopy.getRentedItem().getOwner().getUser().setPassword(null);
	deepCopy.getRentedItem().setRentalInstances(null);
	
	return deepCopy;
	
}

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#rent(long)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance rent(long rentalRequestId) throws BadInputDataException, InvalidBusinessStateException  {
	return this.rent(rentalRequestId, null, null);
}

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#rent(long, java.util.Calendar, java.util.Calendar)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance rent(long rentalRequestId,Calendar plannedStartDate, Calendar plannedReturnDate) throws BadInputDataException, InvalidBusinessStateException  {
	

	RentalInstance rentalRequest = em.find(RentalInstance.class, rentalRequestId);
	if(rentalRequest == null) throw new BadInputDataException(PropertyManager.getMessage("RentalInstanceIdInvalid",rentalRequestId), "RentalManager.rent");

	//verify that the user is borrower
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("RenterIdInvalid", userName), 
																		"RentalManager.rent");
	if (!loggedInUser.getUser().getUserName().equals(rentalRequest.getBorrower().getUser().getUserName()))
			throw new BadInputDataException(PropertyManager.getMessage("UserMismatch", userName), 
																		"RentalManager.rent");	
	
	if(rentalRequest.getStatus() != RentalStatusType.OWNER_APPROVED) throw new InvalidBusinessStateException(PropertyManager.getMessage("InvalidStatus", "RentalInstance", rentalRequest.getStatus()),"RentalManager.rent");
		
	//TODO - allow planned start date to vary slightly without requiring re-approval
	if(plannedReturnDate != null && plannedStartDate != null) {
		if (!plannedReturnDate.after(plannedStartDate))
			throw new BadInputDataException(PropertyManager.getMessage("InvalidRentalDates",plannedStartDate.getTime(), plannedReturnDate.getTime()), "RentalManager.rent");
	}
		
	
	if(plannedStartDate != null && rentalRequest.getPlannedStartDate() != plannedStartDate) rentalRequest.setPlannedStartDate(plannedStartDate);
	if(plannedReturnDate !=null && rentalRequest.getPlannedReturnDate() != plannedReturnDate) rentalRequest.setPlannedReturnDate(plannedReturnDate);
	rentalRequest.setStatus(RentalStatusType.RENTED);
	rentalRequest.getRentedItem().setCurrentlyRented(true);
	
	//TODO - calculate expected rental based on the new dates
	
	//Send email to renter and owner about the rental with pickup information and contact details of the owner and renter.
	
	String pickupLocations="";
	for (RentalLocation rl : rentalRequest.getRentedItem().getRentalLocations()){
		pickupLocations += "\n\n\n\n\t\tName: "+rl.getContactName();
		pickupLocations += "\n\t\tPhone: "+rl.getContactPhoneNumber();
		pickupLocations += "\n\n\t\t Address:"+rl.getAddress().getAddress1()
							+"\n\t\t"+(rl.getAddress().getAddress2()==null?"":rl.getAddress().getAddress2())
							+"\n\t\t"+rl.getAddress().getCity()
							+"\n\t\t"+rl.getAddress().getState()
							+"\n\t\t"+rl.getAddress().getZip()
							+"\n\t\t"+rl.getAddress().getCountry();
	}
	
	//TODO - add owner's email address once multiple addresses are implemented
	emailProcessor.sendEmail(rentalRequest.getBorrower().getUser().getEmail(), 
				PropertyManager.getMessage("RentedSubject",rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName()), 
				PropertyManager.getMessage("RentedEmailContent", 
											rentalRequest.getBorrower().getUser().generateFullName(), 
											rentalRequest.getRentedItem().getId(),
											rentalRequest.getRentedItem().getName(), 
											rentalRequest.getPlannedStartDate().getTime(), 
											rentalRequest.getPlannedReturnDate().getTime(), 
											pickupLocations));
	
	RentalInstance updated = em.merge(rentalRequest);
	em.flush();
	
	RentalInstance deepCopy = updated.deepCopy();
	
	deepCopy.getRentedItem().getOwner().setRentedItems(null);
	deepCopy.getRentedItem().getOwner().setOwnedItems(null);
	deepCopy.getRentedItem().setRentalInstances(null);
	deepCopy.getBorrower().setRentedItems(null);
	deepCopy.getBorrower().setOwnedItems(null);
	deepCopy.getBorrower().getUser().setPassword(null);
	
	deepCopy.getRentedItem().getOwner().getUser().setEmail(null);
	deepCopy.getRentedItem().getOwner().getUser().setUserName(null);
	deepCopy.getRentedItem().getOwner().getUser().setPhones(null);
	deepCopy.getRentedItem().getOwner().getUser().setPhones(null);
	deepCopy.getRentedItem().getOwner().getUser().setPassword(null);
	deepCopy.getRentedItem().getOwner().getUser().setRoles(null);
	deepCopy.getRentedItem().getOwner().getUser().getAddress().setAddress1(null);
	deepCopy.getRentedItem().getOwner().getUser().getAddress().setAddress2(null);
	return deepCopy;
	
	
}

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#returnRental(long, java.util.Calendar, java.util.Calendar)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance returnRental(long rentalRequestId,Calendar actualStartTime, Calendar actualReturnTime) throws BadInputDataException, InvalidBusinessStateException {
	
	log.info("Where "+this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
	
	RentalInstance rentalRequest = em.find(RentalInstance.class, rentalRequestId);
	
	if(rentalRequest == null) 
			throw new BadInputDataException(PropertyManager.getMessage("RentalInstanceIdInvalid",rentalRequestId), "RentalManager.returnRental");

	//verify that the user is borrower
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("RenterIdInvalid", userName), 
																		"RentalManager.returnRental");
	if (!loggedInUser.getUser().getUserName().equals(rentalRequest.getBorrower().getUser().getUserName()))
			throw new BadInputDataException(PropertyManager.getMessage("UserMismatch", userName), 
																		"RentalManager.returnRental");	
	
	if(!(rentalRequest.getStatus() == RentalStatusType.RENTED || 
			rentalRequest.getStatus() == RentalStatusType.CLOSED))
			throw new InvalidBusinessStateException(PropertyManager.getMessage("InvalidStatus", "RentalInstance", rentalRequest.getStatus()),"RentalManager.returnRental");
	
	
	if(rentalRequest.getStatus() == RentalStatusType.CLOSED) return rentalRequest; //TODO - capture borrower's review comments.
	
	if(actualReturnTime != null && actualStartTime != null) {
		if (!actualReturnTime.after(actualStartTime))
			throw new BadInputDataException(PropertyManager.getMessage("InvalidRentalDates",actualStartTime.getTime(), actualReturnTime.getTime()), "RentalManager.returnRental");
	}
			
	if(actualStartTime != null) rentalRequest.setActualRentalStart(actualStartTime);
	if(actualReturnTime !=null) rentalRequest.setActualRentalReturn(actualReturnTime);
	rentalRequest.setStatus(RentalStatusType.MARKED_RETURNED);
	//TODO - capture borrowers review comments
	
	//email owner to ask for completing the rental
	emailProcessor.sendEmail(rentalRequest.getRentedItem().getOwner().getUser().getEmail(), 
			PropertyManager.getMessage("MarkedReturnedSubject",rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName()), 
			PropertyManager.getMessage("MarkedReturnedContent", 
										rentalRequest.getRentedItem().getOwner().getUser().generateFullName(), 
										rentalRequest.getRentedItem().getId(),
										rentalRequest.getRentedItem().getName(),
										rentalRequest.getActualRentalStart().getTime(),
										rentalRequest.getActualRentalReturn().getTime()
										));
	
	RentalInstance updated = em.merge(rentalRequest);
	em.flush();
	
	RentalInstance deepCopy = updated.deepCopy();
	
	deepCopy.getRentedItem().getOwner().setRentedItems(null);
	deepCopy.getRentedItem().getOwner().setOwnedItems(null);
	deepCopy.getRentedItem().setRentalInstances(null);
	deepCopy.getBorrower().setRentedItems(null);
	deepCopy.getBorrower().setOwnedItems(null);
	deepCopy.getBorrower().getUser().setPassword(null);
	
	//deepCopy.getRentedItem().getOwner().setEmail(null);
//	deepCopy.getRentedItem().getOwner().setUserName(null);
	deepCopy.getRentedItem().getOwner().getUser().setPhones(null);
	deepCopy.getRentedItem().getOwner().getUser().setPhones(null);
	deepCopy.getRentedItem().getOwner().getUser().setPassword(null);
	deepCopy.getRentedItem().getOwner().getUser().setRoles(null);
	deepCopy.getRentedItem().getOwner().getUser().getAddress().setAddress1(null);
	deepCopy.getRentedItem().getOwner().getUser().getAddress().setAddress2(null);
	return deepCopy;
	
		
}

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#completeRental(long, java.util.Calendar, java.util.Calendar)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance completeRental(long rentalRequestId,Calendar actualStartTime, Calendar actualReturnTime) throws BadInputDataException, InvalidBusinessStateException {
	
	//TODO - verify that the user is the Owner
	RentalInstance rentalRequest = em.find(RentalInstance.class, rentalRequestId);
	
	if(rentalRequest == null) 
			throw new BadInputDataException(PropertyManager.getMessage("RentalInstanceIdInvalid",rentalRequestId), this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
	
	//verify that the user is owner
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("OwnerIdInvalid", userName), 
																		"RentalManager.completeRental");
	if (!loggedInUser.getUser().getUserName().equals(rentalRequest.getRentedItem().getOwner().getUser().getUserName()))
			throw new BadInputDataException(PropertyManager.getMessage("NotAnOwner", userName), 
																		"RentalManager.completeRental");	

	if(!(rentalRequest.getStatus() == RentalStatusType.RENTED || 
			rentalRequest.getStatus() == RentalStatusType.MARKED_RETURNED))
			throw new InvalidBusinessStateException(PropertyManager.getMessage("InvalidStatus", "RentalInstance", rentalRequest.getStatus()),this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
	
	
	if(actualReturnTime != null && actualStartTime != null) {
		if (!actualReturnTime.after(actualStartTime))
			throw new BadInputDataException(PropertyManager.getMessage("InvalidRentalDates",actualStartTime.getTime(), actualReturnTime.getTime()), this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
	}
			
	if(actualStartTime != null) rentalRequest.setActualRentalStart(actualStartTime);
	if(actualReturnTime !=null) rentalRequest.setActualRentalReturn(actualReturnTime);
	rentalRequest.setStatus(RentalStatusType.CLOSED);
	rentalRequest.getRentedItem().setCurrentlyRented(false);
	//TODO - capture owner's review comments
	
	//TODO - email both borrower and owner of completed rental
	emailProcessor.sendEmail(rentalRequest.getRentedItem().getOwner().getUser().getEmail(), 
			PropertyManager.getMessage("CompletedRentalSubject",rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName()), 
			PropertyManager.getMessage("CompletedRentalContent", 
										rentalRequest.getRentedItem().getOwner().getUser().generateFullName(), 
										rentalRequest.getRentedItem().getId(),
										rentalRequest.getRentedItem().getName(),
										rentalRequest.getActualRentalStart().getTime(),
										rentalRequest.getActualRentalReturn().getTime()
										));
	
	RentalInstance updated = em.merge(rentalRequest);
	em.flush();
	
	RentalInstance deepCopy = updated.deepCopy();
	
	deepCopy.getRentedItem().getOwner().setRentedItems(null);
	deepCopy.getRentedItem().getOwner().setOwnedItems(null);
	deepCopy.getRentedItem().setRentalInstances(null);
	deepCopy.getBorrower().setRentedItems(null);
	deepCopy.getBorrower().setOwnedItems(null);
	deepCopy.getBorrower().getUser().setPassword(null);
	
	deepCopy.getBorrower().getUser().setPhones(null);
	deepCopy.getBorrower().getUser().setPhones(null);
	deepCopy.getBorrower().getUser().setPassword(null);
	deepCopy.getBorrower().getUser().setRoles(null);
	deepCopy.getBorrower().getUser().getAddress().setAddress1(null);
	deepCopy.getBorrower().getUser().getAddress().setAddress2(null);
	return deepCopy;
	
	
}

/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#withdrawRentalRequest(long, java.lang.String)
 */
@Override
//@RolesAllowed({"user"})
public RentalInstance withdrawRentalRequest(long rentalRequestId, String withdrawalReason) throws BadInputDataException, InvalidBusinessStateException {
	
	RentalInstance rentalRequest = em.find(RentalInstance.class, rentalRequestId);
	if(rentalRequest == null) throw new BadInputDataException(PropertyManager.getMessage("RentalInstanceIdInvalid",rentalRequestId), "RentalManager.approveRentalRequest");
	
	//verify that the user is borrower
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("RenterIdInvalid", userName), 
																		"RentalManager.returnRental");
	if (!loggedInUser.getUser().getUserName().equals(rentalRequest.getBorrower().getUser().getUserName()))
			throw new BadInputDataException(PropertyManager.getMessage("UserMismatch", userName), 
																		"RentalManager.returnRental");	
	
	if(!(rentalRequest.getStatus() == RentalStatusType.REQUESTED ||
			rentalRequest.getStatus() == RentalStatusType.OWNER_APPROVED ||
			rentalRequest.getStatus() == RentalStatusType.RENTED))
				throw new InvalidBusinessStateException(PropertyManager.getMessage("InvalidStatus", 
																					"RentalInstance", 
																					 rentalRequest.getStatus())
																					 ,"RentalManager.approveRentalRequest");
	/*
	 * 
	 * If the rental request is withdrawn after the status is changed to RENTED, it is assumed that the owner has not rented out the item yet.
	 * If the borrower has already collected the item from the owner, the owner needs to make sure that they have the safety deposit before renting.
	 * When the money is collected by this application, if there is a conflict of borrower canceling the rental after picking up the item from the owner
	 * they will loose the safety deposit i.e, the system will give the safety deposit to the owner. At some point the insurance may also help in this situation.
	 * 
	 */
	
	rentalRequest.setStatus(RentalStatusType.BORROWER_CANCELLED);
	if (withdrawalReason != null) rentalRequest.setRequestWithdrawalReason(withdrawalReason);
	rentalRequest.getRentedItem().setCurrentlyRented(false);
	
	// TODO - keep track of number of withdrawals before approval and after approval
	
	//send separate emails to owner and borrower so that their email info is kept confidential
	emailProcessor.sendEmail(rentalRequest.getBorrower().getUser().getEmail(), 
				PropertyManager.getMessage("WithdrawalSubject",rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName()), 
				PropertyManager.getMessage("WithdrawalEmailContent", 
											rentalRequest.getBorrower().getUser().generateFullName(), 
											rentalRequest.getRentedItem().getId(),
											rentalRequest.getRentedItem().getName(), 
											rentalRequest.getRequestWithdrawalReason()==null?"No reason provided":rentalRequest.getRequestWithdrawalReason()
											));
	
	emailProcessor.sendEmail(rentalRequest.getRentedItem().getOwner().getUser().getEmail(), 
			PropertyManager.getMessage("WithdrawalSubject",rentalRequest.getRentedItem().getId(),rentalRequest.getRentedItem().getName()), 
			PropertyManager.getMessage("WithdrawalEmailContent", 
										rentalRequest.getRentedItem().getOwner().getUser().generateFullName(), 
										rentalRequest.getRentedItem().getId(),
										rentalRequest.getRentedItem().getName(), 
										rentalRequest.getRequestWithdrawalReason()==null?"No reason provided":rentalRequest.getRequestWithdrawalReason()
										));
	
	RentalInstance updated = em.merge(rentalRequest);
	em.flush();
	
	RentalInstance deepCopy = updated.deepCopy();
	
	deepCopy.getRentedItem().getOwner().setRentedItems(null);
	deepCopy.getRentedItem().getOwner().setOwnedItems(null);
	deepCopy.getRentedItem().setRentalInstances(null);
	deepCopy.getBorrower().setRentedItems(null);
	deepCopy.getBorrower().setOwnedItems(null);
	deepCopy.getBorrower().getUser().setPassword(null);
	
	//deepCopy.getRentedItem().getOwner().setEmail(null);
//	deepCopy.getRentedItem().getOwner().setUserName(null);
	deepCopy.getRentedItem().getOwner().getUser().setPhones(null);
	deepCopy.getRentedItem().getOwner().getUser().setPhones(null);
	deepCopy.getRentedItem().getOwner().getUser().setPassword(null);
	deepCopy.getRentedItem().getOwner().getUser().setRoles(null);
	deepCopy.getRentedItem().getOwner().getUser().getAddress().setAddress1(null);
	deepCopy.getRentedItem().getOwner().getUser().getAddress().setAddress2(null);
	return deepCopy;
}


/* (non-Javadoc)
 * @see com.pinaka.eRental.serviceFacade.RentalManagerService#removeRentalInstance(long)
 */
@Override
//@RolesAllowed({"admin"})
public void removeRentalInstance(long rentalInstanceId) throws BadInputDataException {
	
	if (rentalInstanceId==0l) 
			throw new BadInputDataException(PropertyManager.getMessage("RentalInstanceIdNull"),
														  "RentalManagerServiceBean.removeRentalInstance");
	
	RentalInstance rentalInstance =  em.find(RentalInstance.class, rentalInstanceId);
	
	if(rentalInstance == null) 
			throw new BadInputDataException(PropertyManager.getMessage("NoRentalInstance", rentalInstanceId),
											"RentalManagerServiceBean.removeRentalInstance");
	
	em.remove(rentalInstance);	
	em.flush();
}

@Override
//@RolesAllowed({"user"})
public List<RentalInstance> getMyBorrowals() throws BadInputDataException {
	
	log.info("In getMyBorrowals");
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("RenterIdInvalid", userName), 
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
	List<RentalInstance> borrowals = loggedInUser.getRentedItems();
	em.detach(borrowals);
	
	for(RentalInstance borrowal: borrowals){
		em.detach(borrowal);
		em.detach(borrowal.getRentedItem());
		em.detach(borrowal.getBorrower());
		em.detach(borrowal.getBorrower().getUser());
		borrowal.getRentedItem().setOwner(null);
		borrowal.getBorrower().getUser().setPassword(null);
		borrowal.getRentedItem().setRentalInstances(null);
		borrowal.getBorrower().setRentedItems(null);
		borrowal.getBorrower().setOwnedItems(null);
	}	
	return borrowals;
}

@Override
public List<RentalInstance> getMyLeases() throws BadInputDataException {
	// TODO Auto-generated method stub
	
	log.info("In getMyLeases");
	String userName = ctx.getCallerPrincipal().getName();
	RentalUser loggedInUser = em.find(RentalUser.class, userName);
	
	if (loggedInUser == null) 
			throw new BadInputDataException(PropertyManager.getMessage("RenterIdInvalid", userName), 
					this.getClass()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
	
	
	TypedQuery<RentalInstance> query;

	query = em.createNamedQuery("RentalInstance.findRentalInstancesByOwner", RentalInstance.class);
	
	query.setParameter("ownerId", loggedInUser.getUserId());

	List<RentalInstance> leases = query.getResultList();
	em.detach(leases);
	
	for(RentalInstance lease: leases){
		em.detach(lease);
		em.detach(lease.getRentedItem());
		em.detach(lease.getBorrower());
		em.detach(lease.getBorrower().getUser());
		lease.getRentedItem().setOwner(null);
		lease.getBorrower().getUser().setPassword(null);
		lease.getRentedItem().setRentalInstances(null);
		lease.getBorrower().setRentedItems(null);
		lease.getBorrower().setOwnedItems(null);
	}	
	return leases;
	
}
	
}

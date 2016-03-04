package com.pinaka.eRental.serviceFacade;

import java.util.Calendar;
import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.DELETE;
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

import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.exception.InvalidBusinessStateException;
import com.pinaka.eRental.model.RejectionReasonType;
import com.pinaka.eRental.model.RentalInstance;

@Path("/")
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@SecurityDomain("other")
@DeclareRoles({"user","admin"})
public interface RentalManagerService {

	/**
	 * 
	 * Borrower uses this method to make a rental request. A rental item should have been selected. 
	 * A planned rental start date and rental end date should be decided and provided by the borrower.
	 * Borrower should have logged into perform this operation.
	 * 
	 * @param rentalItemId
	 * @param plannedStartDate
	 * @param plannedReturnDate
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException 
	 * 
	 */
	@POST
	@Path("/rental-requests")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	public abstract RentalInstance requestRental(
			@QueryParam("rental-item-id") long rentalItemId,
			@QueryParam("planned-start-time") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar plannedStartDate,
			@QueryParam("planned-return-time") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar plannedReturnDate)
			throws BadInputDataException, InvalidBusinessStateException;

	/**
	 * 
	 * Owner uses this method to approve a pending rental request.
	 * Owner should have verified the rental request, verified the borrower's profile be satisfied with the borrower before approving
	 * 
	 * @param rentalRequestId
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */
	@PUT
	@Path("/rental-requests/{rental-request-id}/approve")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	public abstract RentalInstance approveRentalRequest(
			@PathParam("rental-request-id") long rentalRequestId)
			throws BadInputDataException, InvalidBusinessStateException;

	/**
	 * 
	 * Owner rejects the Rental Request after reviewing the request and borrower's profile
	 * Owner provides a reason for rejection. Owner may choose not to share the rejection reason with the borrower.
	 * 
	 * @param rentalRequestId
	 * @param reasonForRejection
	 * @param shareRejectionReasonWithBorrower
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */

	@RolesAllowed({ "user" })
	public abstract RentalInstance rejectRentalRequest(
											long rentalRequestId,
											RejectionReasonType reasonForRejection,
											boolean shareRejectionReasonWithBorrower)
			throws BadInputDataException, InvalidBusinessStateException;

	
	
	/**
	 * 
	 * Owner rejects the Rental Request after reviewing the request and borrower's profile
	 * Owner provides a reason for rejection. Owner may choose not to share the rejection reason with the borrower.
	 * 
	 * @param rentalRequestId
	 * @param reasonForRejection
	 * @param otherReason
	 * @param shareRejectionReasonWithBorrower
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */
	@PUT
	@Path("/rental-requests/{rental-request-id}/reject")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	public abstract RentalInstance rejectRentalRequest(
			@PathParam("rental-request-id") long rentalRequestId,
			@QueryParam("rejection-reason") RejectionReasonType reasonForRejection, 
			@QueryParam("other-reason") String otherReason,
			@QueryParam("share-reason-with-borrower") boolean shareRejectionReasonWithBorrower)
			throws BadInputDataException, InvalidBusinessStateException;
//TODO- convert boolean to YES and NO strings instead of 1 and 0

	/**
	 *
	 * Borrower chooses to rent
	 * 
	 * @param rentalRequestId
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */
	@RolesAllowed({ "user" })
	public abstract RentalInstance rent(long rentalRequestId)
			throws BadInputDataException, InvalidBusinessStateException;

	/**
	 * 
	 * Borrower completes rental. Can edit the planned start date and return date.
	 * TODO - how much can the dates vary?
	 * 
	 * @param rentalRequestId
	 * @param plannedStartDate
	 * @param plannedReturnDate
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */
	@PUT
	@Path("/rental-requests/{rental-request-id}/rent")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	public abstract RentalInstance rent(
			@PathParam("rental-request-id") long rentalRequestId,
			@QueryParam("planned-start-date") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar plannedStartDate, 
			@QueryParam("planned-return-date") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar plannedReturnDate)
			throws BadInputDataException, InvalidBusinessStateException;

	/**
	 * 
	 * The borrower informs that the rentalItem is returned. The system marks it as returned. To close, the owner needs to inform the same.
	 * If the owner had already closed it, this method will not change any thing. It just returns the rentalInstance.
	 * 
	 * This is optional (but encouraged) step as the borrower may not always care to perform this step. The owner has to complete the rental.
	 * 
	 * @param rentalRequestId
	 * @param actualStartTime
	 * @param actualReturnTime
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */
	@PUT
	@Path("/rental-requests/{rental-request-id}/return")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	public abstract RentalInstance returnRental(
			@PathParam("rental-request-id") long rentalRequestId,
			@QueryParam("actual-start-time") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar actualStartTime, 
			@QueryParam("actual-return-time") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar actualReturnTime)
			throws BadInputDataException, InvalidBusinessStateException;

	/**
	 * The Owner informs that the rental item is returned and the rental is complete.
	 * 
	 * @param rentalRequestId
	 * @param actualStartTime
	 * @param actualReturnTime
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */
	@PUT
	@Path("/rental-requests/{rental-request-id}/complete")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	public abstract RentalInstance completeRental(
			@PathParam("rental-request-id") long rentalRequestId,
			@QueryParam("actual-start-time") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar actualStartTime, 
			@QueryParam("actual-return-time") @CalendarFormat("MM-dd-yyyy 'at' HH:mm z") Calendar actualReturnTime)
			throws BadInputDataException, InvalidBusinessStateException;

	/**
	 *
	 * Borrower withdraws the rental request. This can happen at any stage after the request
	 * 
	 * @param rentalRequestId
	 * @param withdrawalReason
	 * @return
	 * @throws BadInputDataException
	 * @throws InvalidBusinessStateException
	 */
	@PUT
	@Path("/rental-requests/{rental-request-id}/withdraw")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	public abstract RentalInstance withdrawRentalRequest(
			@PathParam("rental-request-id") long rentalRequestId,
			@QueryParam("withdrawal-reason") String withdrawalReason) 
					throws BadInputDataException, InvalidBusinessStateException;
	
	@GET
	@Path("/rental-requests")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	@XmlElementWrapper(name = "borrowals")
	@XmlElement(name = "borrowal")
	public abstract List<RentalInstance> getMyBorrowals() throws BadInputDataException;

	@GET
	@Path("/rental-requests/leases")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "user" })
	@XmlElementWrapper(name = "leases")
	@XmlElement(name = "lease")
	public abstract List<RentalInstance> getMyLeases() throws BadInputDataException;

	@DELETE
	@Path("/rental-requests/{rental-request-id}")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed({ "admin" })
	public abstract void removeRentalInstance(@PathParam("rental-request-id") long rentalInstanceId)
			throws BadInputDataException;

}
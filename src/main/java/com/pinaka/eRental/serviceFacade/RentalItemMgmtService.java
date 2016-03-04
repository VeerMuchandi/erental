package com.pinaka.eRental.serviceFacade;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.RentalItem;

public interface RentalItemMgmtService {

	@GET
	@Path("/rental-items/{id:[0-9][0-9]*}")
	@Produces({"application/xml", "application/json"})
	public abstract RentalItem findRentalItem(@PathParam("id") long rentalItemId)
			throws BadInputDataException;

	@GET
	@Produces({"application/xml", "application/json"})
	@Path("/rental-items")
	public abstract List<RentalItem> findRentalItems(
			@QueryParam("category") String itemCategory,
			@QueryParam("sub-category") String itemSubCategory,
			@QueryParam("brand") String itemBrand,
			@QueryParam("fullSearch") String textSearchParam)
			throws BadInputDataException;

	public abstract List<RentalItem> findRentalItems(String itemCategory,
			String itemSubCategory, String itemBrand)
			throws BadInputDataException;

	public abstract List<RentalItem> findRentalItemsBySomeValue(
			String textSearchParam);

	@GET
	@Path("/my-rental-items")
	@Produces({"application/xml", "application/json"})
	@RolesAllowed("user")
	public abstract List<RentalItem> findMyRentalItems()
			throws BadInputDataException;

	@POST
	@Path("/rental-items")
	@Consumes({"text/xml","application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@RolesAllowed("user")
	public abstract RentalItem addRentalItem(RentalItem rentalItem)
			throws BadInputDataException;

	@PUT
	@Path("/rental-items/{id:[0-9][0-9]*}")
	@Consumes({"text/xml","application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@RolesAllowed("user")
	public abstract RentalItem updateRentalItem(
			@PathParam("id") long rentalItemId, RentalItem rentalItem)
			throws BadInputDataException;

}
package com.pinaka.eRental.service;

import java.util.List;

import com.pinaka.eRental.exception.BadInputDataException;
import com.pinaka.eRental.model.RentalItem;

public interface RentalItemManager {

	/**
	 *
	 * Adds a new rental item. Expects that the owner is already available and is sent along with the add request.
	 * 
	 * @param rentalItem
	 * @return
	 * @throws BadInputDataException
	 */
	public abstract RentalItem addRentalItem(RentalItem rentalItem)
			throws BadInputDataException;

	/**
	 * 
	 * Update an existing Rental Item. If one does not exist, create one.
	 * 
	 * @param rentalItem
	 * @return
	 * @throws BadInputDataException
	 */
	public abstract RentalItem updateRentalItem(RentalItem rentalItem)
			throws BadInputDataException;

	/**
	 * 
	 * Remove or delete a rental item. To be used only by eRental Admin
	 * Do not expose this as a business method. We don't want owners removing rental items. 
	 * They should just disable it if they don't need to use it.
	 * 
	 * @param rentalItemId
	 * @throws BadInputDataException
	 * 
	 */

	public abstract void removeRentalItem(long rentalItemId)
			throws BadInputDataException;

	/**
	 * Returns all rental items. This method should not be used in general as it lists all available rental items.
	 * Use findAllRentableItems for finding only rentable items.
	 * 
	 * @return
	 */
	public abstract List<RentalItem> findAllRentalItems();

	/**
	 * searches rental item based on the primary key
	 * 
	 * @param rentalItemId
	 * @return
	 * @throws BadInputDataException
	 */

	public abstract RentalItem findRentalItem(long rentalItemId)
			throws BadInputDataException;

	/**
	 * Searches rental items based on category, subcategory and brand 
	 * Returns all items that match the criteria.
	 * This method should be used only by administrators as it lists even non-rentable items
	 * 
	 * @param itemCategory
	 * @param itemSubCategory
	 * @return
	 * searches rental items by category and/or subcategory
	 * 
	 */
	public abstract List<RentalItem> findRentalItems( String itemCategory,
														String itemSubCategory,
														String itemBrand) throws BadInputDataException;

	/**
	 * Searches rental items based on category, subcategory and brand 
	 * Returns rentable items that match the criteria.
	 * 
	 * @param itemCategory
	 * @param itemSubCategory
	 * @param itemBrand
	 * @param rentingEnabledOnly
	 * @return
	 * @throws BadInputDataException
	 */
	public abstract List<RentalItem> findRentalItems( String itemCategory,
														String itemSubCategory,
														String itemBrand,
														boolean rentable) throws BadInputDataException;

	public abstract List<RentalItem> findRentalItemsByItemSubCategory(
			String itemSubCategory) throws BadInputDataException;

	public abstract List<RentalItem> findRentalItemsByItemCategory(
			String itemCategory) throws BadInputDataException;

	/**
	 * Performs text search of RentalItems by any value across name, brand, category and subcategory
	 * Returns all items that match the criteria.
	 * This method should be used only by administrators as it lists even non-rentable items
	 *  
	 * @param textSearchParam
	 * @return
	 */
	public abstract List<RentalItem> findRentalItemsBySomeValue(String textSearchParam);

	
	/**
	 * Performs text search of RentalItems by any value across name, brand, category and subcategory
	 * Returns all rentable items that match the criteria.
	 * @param textSearchParam
	 * @return
	 */
	public abstract List<RentalItem> findRentableItemsBySomeValue(String textSearchParam);

	/**
	 * Returns all rentable items
	 * @return
	 */
	public abstract List<RentalItem> findAllRentableItems();

}
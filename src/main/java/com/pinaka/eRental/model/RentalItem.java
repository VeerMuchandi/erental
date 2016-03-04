package com.pinaka.eRental.model;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Entity implementation class for Entity: RentalItem
 *
 * @filename RentalItem.java
 * @author Veer Muchandi
 * @created Mar 27, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */

@Entity
@XmlRootElement
@XmlType(propOrder = { "id", "name","availableFrom","itemCategory","itemSubCategory","itemBrand",
		"itemDescription","workingCondition", "usageInstructions","termsAndConditions","enableRenting","consolidatedRating",
		"currentlyRented","costs","penaltyTerms", "rentalLocations", "pastRentalCount","owner","rentalInstances" })
//TODO - add to list itemAge, replacementCost
@NamedQueries({
	@NamedQuery(name = "RentalItem.findAll", query = "SELECT ri FROM RentalItem ri"),
	@NamedQuery(name = "RentalItem.findAllRentable", query = "SELECT ri FROM RentalItem ri where ri.enableRenting = true"),
	@NamedQuery(name = "RentalItem.findByName", query = "SELECT ri FROM RentalItem ri where ri.name = :name AND ri.enableRenting = true"),
	@NamedQuery(name = "RentalItem.findByItemCategory", query = "SELECT ri FROM RentalItem ri where ri.itemCategory = :itemCategory AND ri.enableRenting = true"),
	@NamedQuery(name = "RentalItem.findByItemSubCategory", query = "SELECT ri FROM RentalItem ri where ri.itemSubCategory = :itemSubCategory AND ri.enableRenting = true"),
	@NamedQuery(name = "RentalItem.findByParams", query = "SELECT ri FROM RentalItem ri WHERE ri.itemCategory LIKE :itemCategory AND ri.itemSubCategory LIKE :itemSubCategory AND ri.itemBrand LIKE :itemBrand"),
	@NamedQuery(name = "RentalItem.findRentableByParams", query = "SELECT ri FROM RentalItem ri WHERE ri.itemCategory LIKE :itemCategory AND ri.itemSubCategory LIKE :itemSubCategory AND ri.itemBrand LIKE :itemBrand AND ri.enableRenting LIKE :rentable" )
})
public class RentalItem implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	private long id;
	
	@NotNull
	@Size(min = 1, max = 25)
	@Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
	private String name;
	
	@NotNull
	private String itemCategory;
	
	@NotNull
	private String itemSubCategory;
	
	@Size(min = 0, max = 25)
	//@Pattern(regexp = "[A-Za-z1-0 ]*", message = "must contain only numbers, letters and spaces")	
	private String itemBrand;
	
	@Size(min = 0, max = 255)
	private String itemDescription;
	
	@Size(min = 0, max = 1000)
	private String usageInstructions;
	
	@Size(min = 0, max = 1000)
	private String termsAndConditions;
	
	private int consolidatedRating = 0;
	
	@Temporal(TemporalType.DATE)
	@Future
	private Calendar availableFrom;
	
	private boolean enableRenting = true;
	
	private boolean currentlyRented = false; //TODO - add to hashcode and equals
	
	@Digits(integer=6, fraction=2)
	private double replacementCost = 0.0;
	
	private long pastRentalCount = 0;
	
	@Digits(integer=2, fraction=1)
	private double itemAge = 0.0 ;
	
	@Size(min = 0, max = 1000)
	private String workingCondition;
	
	@ElementCollection(fetch=FetchType.EAGER ,targetClass=RentalCost.class)
	@CollectionTable(name="RentalItemCost", joinColumns = {@JoinColumn(name="rentalItemId")})
	@OrderColumn(name="costOrder") //this is added to prevent duplicates exception
	private List<RentalCost> costs = new ArrayList<RentalCost>();
	
	@ElementCollection(fetch=FetchType.EAGER ,targetClass=PenaltyTerm.class)
	@CollectionTable(name="RentalPenaltyTerm", joinColumns = {@JoinColumn(name="rentalItemId")})
	@OrderColumn(name="penaltyTermOrder") //added to prevent duplicates exception
	private List<PenaltyTerm> penaltyTerms = new ArrayList<PenaltyTerm>();
	
	@ElementCollection(fetch=FetchType.EAGER ,targetClass=RentalLocation.class)
	@CollectionTable(name="RentalLocation", joinColumns = {@JoinColumn(name="rentalItemId")})
	@OrderColumn(name="locationOrder")
	private List<RentalLocation> rentalLocations = new ArrayList<RentalLocation>();
	
	@ManyToOne
	@JoinColumn(name="ownerId")
	private RentalUser owner;
	
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="rentedItem")
	private Set<RentalInstance> rentalInstances = new HashSet<RentalInstance>();
	

	public RentalItem() {
		super();
	}   
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}   
	public String getItemCategory() {
		return this.itemCategory;
	}

	public void setItemCategory(String itemCategory) {
		this.itemCategory = itemCategory;
	}   
	public String getItemSubCategory() {
		return this.itemSubCategory;
	}

	public void setItemSubCategory(String itemSubCategory) {
		this.itemSubCategory = itemSubCategory;
	}   
	public String getItemBrand() {
		return this.itemBrand;
	}

	public void setItemBrand(String itemBrand) {
		this.itemBrand = itemBrand;
	}   
	public String getItemDescription() {
		return this.itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}   
	public String getUsageInstructions() {
		return this.usageInstructions;
	}

	public void setUsageInstructions(String usageInstructions) {
		this.usageInstructions = usageInstructions;
	}   
	public String getTermsAndConditions() {
		return this.termsAndConditions;
	}

	public void setTermsAndConditions(String termsAndConditions) {
		this.termsAndConditions = termsAndConditions;
	}   
	public int getConsolidatedRating() {
		return this.consolidatedRating;
	}

	public void setConsolidatedRating(int consolidatedRating) {
		this.consolidatedRating = consolidatedRating;
	}   
	public Calendar getAvailableFrom() {
		return this.availableFrom;
	}

	public void setAvailableFrom(Calendar availableFrom) {
		this.availableFrom = availableFrom;
	}   
	public boolean getEnableRenting() {
		return this.enableRenting;
	}

	public void setEnableRenting(boolean enableRenting) {
		this.enableRenting = enableRenting;
	}   
	/**
	 * @return the currentlyRented
	 */
	public boolean isCurrentlyRented() {
		return currentlyRented;
	}
	/**
	 * @param currentlyRented the currentlyRented to set
	 */
	public void setCurrentlyRented(boolean currentlyRented) {
		this.currentlyRented = currentlyRented;
	}
	public double getReplacementCost() {
		return this.replacementCost;
	}

	public void setReplacementCost(float replacementCost) {
		this.replacementCost = replacementCost;
	}   
	public long getPastRentalCount() {
		return this.pastRentalCount;
	}

	public void setPastRentalCount(long pastRentalCount) {
		this.pastRentalCount = pastRentalCount;
	}   
	public double getItemAge() {
		return this.itemAge;
	}

	public void setItemAge(float itemAge) {
		this.itemAge = itemAge;
	}   
	public String getWorkingCondition() {
		return this.workingCondition;
	}

	public void setWorkingCondition(String workingCondition) {
		this.workingCondition = workingCondition;
	}
	/**
	 * @return the costs
	 */
	public List<RentalCost> getCosts() {
		return costs;
	}
	/**
	 * @param costs the costs to set
	 */
	public void setCosts(List<RentalCost> costs) {
		this.costs = costs;
	}
	/**
	 * @return the penaltyTerms
	 */
	public List<PenaltyTerm> getPenaltyTerms() {
		return penaltyTerms;
	}
	/**
	 * @param penaltyTerms the penaltyTerms to set
	 */
	public void setPenaltyTerms(List<PenaltyTerm> penaltyTerms) {
		this.penaltyTerms = penaltyTerms;
	}

	/**
	 * @return the rentalLocations
	 */
	public List<RentalLocation> getRentalLocations() {
		return rentalLocations;
	}
	/**
	 * @param rentalLocations the rentalLocations to set
	 */
	public void setRentalLocations(List<RentalLocation> rentalLocations) {
		this.rentalLocations = rentalLocations;
	}
	/**
	 * @return the owner
	 */
	public RentalUser getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(RentalUser owner) {
		this.owner = owner;
	}
	/**
	 * @return the renters
	 */
	public Set<RentalInstance> getRentalInstances() {
		return rentalInstances;
	}
	/**
	 * @param rentalInstances the renters to set
	 */
	public void setRentalInstances(Set<RentalInstance> rentalInstances) {
		this.rentalInstances = rentalInstances;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        RentalItem ri = (RentalItem) obj;
        
        return ((Double.doubleToLongBits(itemAge) == Double.doubleToLongBits(ri.getItemAge())) && 
        		(Double.doubleToLongBits(replacementCost) == Double.doubleToLongBits(ri.getReplacementCost())) &&
        		(consolidatedRating == ri.getConsolidatedRating()) &&
        		(pastRentalCount == ri.getPastRentalCount()) &&
        		(availableFrom == ri.getAvailableFrom() || (availableFrom != null && availableFrom.equals(ri.getAvailableFrom()))) &&
        		(costs == ri.getCosts() || (costs != null && costs.equals(ri.getCosts()))) &&
        		(itemBrand == ri.getItemBrand() || (itemBrand != null && itemBrand.equals(ri.getItemBrand()))) &&
        		(itemCategory == ri.getItemCategory() || (itemCategory != null && itemCategory.equals(ri.getItemCategory()))) &&
        		(itemDescription == ri.getItemDescription() || (itemDescription != null && itemDescription.equals(ri.getItemDescription()))) &&
        		(itemSubCategory == ri.getItemSubCategory() || (itemSubCategory != null && itemSubCategory.equals(ri.getItemSubCategory())))&&
        		(name == ri.getName() || (name != null && name.equals(ri.getName())))&&
        		(owner == ri.getOwner() || (owner != null && owner.equals(ri.getOwner())))&&
        		(penaltyTerms == ri.getPenaltyTerms() || (penaltyTerms != null && penaltyTerms.equals(ri.getPenaltyTerms())))&&
        		(rentalLocations == ri.getRentalLocations() || (rentalLocations != null && rentalLocations.equals(ri.getRentalLocations())))&&
        		(termsAndConditions == ri.getTermsAndConditions() || (termsAndConditions != null && termsAndConditions.equals(ri.getTermsAndConditions())))&&
        		(usageInstructions == ri.getUsageInstructions() || (usageInstructions != null && usageInstructions.equals(ri.getUsageInstructions())))&&
        		(workingCondition == ri.getWorkingCondition() || (workingCondition != null && workingCondition.equals(ri.getWorkingCondition())))&&
        		(owner == ri.getOwner() || (owner != null && owner.equals(ri.getOwner())))&&
        		(enableRenting == ri.getEnableRenting())
        		);
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	long bits = Double.doubleToLongBits(itemAge);
    	int var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	bits = Double.doubleToLongBits(replacementCost);
    	var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	bits = consolidatedRating;
    	var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	bits = pastRentalCount;
    	var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	
    	hash = 31 * hash + (null == availableFrom ? 0 : availableFrom.hashCode());
    	hash = 31 * hash + (null == costs ? 0 : costs.hashCode());
    	hash = 31 * hash + (null == itemBrand ? 0 : itemBrand.hashCode());
    	hash = 31 * hash + (null == itemCategory ? 0 : itemCategory.hashCode());
    	hash = 31 * hash + (null == itemDescription ? 0 : itemDescription.hashCode());
    	hash = 31 * hash + (null == itemSubCategory ? 0 : itemSubCategory.hashCode());
    	hash = 31 * hash + (null == name ? 0 : name.hashCode());
    	hash = 31 * hash + (null == owner ? 0 : owner.hashCode());
    	hash = 31 * hash + (null == penaltyTerms ? 0 : penaltyTerms.hashCode());
    	hash = 31 * hash + (null == rentalLocations ? 0 : rentalLocations.hashCode());
    	hash = 31 * hash + (null == termsAndConditions ? 0 : termsAndConditions.hashCode());
    	hash = 31 * hash + (null == usageInstructions ? 0 : usageInstructions.hashCode());
    	hash = 31 * hash + (null == workingCondition ? 0 : workingCondition.hashCode());
    	hash = 31 * hash + (null == owner ? 0 : owner.hashCode());
    	hash = 31 * hash + (enableRenting ? 1 : 0);
   	         	
    	return hash;	
       
    }
    
    @SuppressWarnings("unchecked")
	public <T> T deepCopy()
	{
	        try
	        {
	                ObjectOutputStream oos = null;
	                ObjectInputStream ois = null;
	                try
	                {
	                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	                        oos = new ObjectOutputStream(bos);
	                        oos.writeObject(this);
	                        oos.flush();
	                        ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
	                        return (T) ois.readObject();
	                }
	                finally
	                {
	                        oos.close();
	                        ois.close();
	                }
	        }
	        catch ( ClassNotFoundException cnfe )
	        {
	                // Impossible, since both sides deal in the same loaded classes.
	                return null;
	        }
	        catch ( IOException ioe )
	        {
	                // This has to be "impossible", given that oos and ois wrap a *byte array*.
	                return null;
	        }
	}
    
   	public Object clone() {
   		try{
   		return super.clone();
   		} catch (CloneNotSupportedException e) {
   		  return null;
    	 }
   	}


   
}

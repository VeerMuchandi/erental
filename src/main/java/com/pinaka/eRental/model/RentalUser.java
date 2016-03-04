package com.pinaka.eRental.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;

import com.pinaka.UserManagement.User;

/**
 * Entity implementation class for Entity: RentalUser
 * RentalUser is local to this application. It extends User with one to one relationship.
 *
 */
@Entity
@XmlRootElement
@Rental
public class RentalUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
                 message="{invalid.email}")
	private String userId;
	
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="PrincipalID", nullable=false)
	private User user;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="owner")
	private List<RentalItem> ownedItems = new ArrayList<RentalItem>();

	@XmlElementWrapper(name = "borrowals")
	@XmlElement(name = "borrowal")
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "borrower")
	private List<RentalInstance> rentalInstances = new ArrayList<RentalInstance>();
	
	public RentalUser() {
		super();
	}   


	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}


	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}


	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return the rentalItems
	 */
	public List<RentalItem> getOwnedItems() {
		return ownedItems;
	}
	/**
	 * @param rentalItems the rentalItems to set
	 */
	public void setOwnedItems(List<RentalItem> ownedItems) {
		this.ownedItems = ownedItems;
	}
	/**
	 * @return the rentedItems
	 */
	public List<RentalInstance> getRentedItems() {
		return rentalInstances;
	}
	/**
	 * @param rentedItems the rentedItems to set
	 */
	public void setRentedItems(List<RentalInstance> rentedItems) {
		this.rentalInstances = rentedItems;
	}
	

	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        RentalUser u = (RentalUser) obj;
        
        return (
        		(ownedItems == u.getOwnedItems() || (ownedItems != null && ownedItems.equals(u.getOwnedItems()))) &&
        		(rentalInstances == u.getRentedItems() || (rentalInstances != null && rentalInstances.equals(u.getRentedItems()))));
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	hash = 31 * hash + (null == rentalInstances ? 0 : rentalInstances.hashCode());
    	hash = 31 * hash + (null == ownedItems ? 0 : ownedItems.hashCode());
  	         	
    	return hash;	
       
    }

}

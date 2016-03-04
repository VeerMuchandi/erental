package com.pinaka.eRental.model;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import com.pinaka.UserManagement.Address;

/**
 * Entity implementation class for Entity: RentalItemLocation
 *
 */
@Embeddable
@XmlRootElement
public class RentalLocation implements Serializable{

	
	private static final long serialVersionUID = 1L;

	@Size(min = 1, max = 50)
	@Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
	private String contactName;
	
    @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$",
            message="{invalid.phonenumber}")
	private String contactPhoneNumber;
	  
	@Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
                 message="{invalid.email}")
	private String email;
	
	@Embedded
	private Address address;
	
	public RentalLocation() {
		super();
	}   
	public String getContactName() {
		return this.contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}   
	public String getContactPhoneNumber() {
		return this.contactPhoneNumber;
	}

	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}   

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(Address address) {
		this.address = address;
	}
	
    @Override
    public boolean equals(Object obj) {
       	if (this == obj) return true;
    		if((obj == null) || (obj.getClass() != this.getClass())) return false;
    		      
            RentalLocation loc = (RentalLocation) obj;
            
            return ((contactName == loc.getContactName() || (contactName != null && contactName.equals(loc.getContactName()))) &&
            		(contactPhoneNumber == loc.getContactPhoneNumber() || (contactPhoneNumber != null && contactPhoneNumber.equals(loc.getContactPhoneNumber()))) &&
            		(email == loc.getEmail() || (email != null && email.equals(loc.getEmail()))) &&
            		(address == loc.getAddress() || (address != null && address.equals(loc.getAddress())))
            		);
  	
    }

    @Override
    public int hashCode() {
        
    	int hash = 7;
    	hash = 31 * hash + (null == contactName ? 0 : contactName.hashCode());
    	hash = 31 * hash + (null == contactPhoneNumber ? 0 : contactPhoneNumber.hashCode());
    	hash = 31 * hash + (null == email ? 0 : email.hashCode());
    	hash = 31 * hash + (null == address ? 0 : address.hashCode());
    	return hash;	
       
    }
   
}

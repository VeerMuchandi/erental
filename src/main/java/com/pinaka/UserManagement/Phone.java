/**
 * 
 */
package com.pinaka.UserManagement;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Pattern;


/**
 * @author Muchandi
 *
 */

@Embeddable
public class Phone implements Serializable{

	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	private PhoneType phoneType;
	
    @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$",
            message="{invalid.phonenumber}")
	private String phoneNumber;
	/**
	 * 
	 */
	public Phone() {
		super();
	}
	
	public Phone(PhoneType phoneType, String phoneNumber) {
		super();
		this.phoneType = phoneType;
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the phoneType
	 */
	public PhoneType getPhoneType() {
		return phoneType;
	}
	/**
	 * @param phoneType the phoneType to set
	 */
	public void setPhoneType(PhoneType phoneType) {
		this.phoneType = phoneType;
	}
	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}
	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        Phone ph = (Phone) obj;
        
        return ((phoneNumber == ph.getPhoneNumber() || (phoneNumber != null && phoneNumber.equals(ph.getPhoneNumber()))) && 
         		(phoneType == ph.getPhoneType() || (phoneType != null && phoneType.equals(ph.getPhoneType()))));
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	hash = 31 * hash + (null == phoneType ? 0 : phoneType.hashCode());
    	hash = 31 * hash + (null == phoneNumber ? 0 : phoneNumber.hashCode());
    	
    	return hash;	
       
    }

}

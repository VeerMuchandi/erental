/**
 * 
 */
package com.pinaka.UserManagement;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Muchandi
 *
 */

@Embeddable
@XmlRootElement

public class Address implements Serializable{


	private static final long serialVersionUID = 1L;
	@NotNull
    @Size(min = 1, max = 30)
	private String address1;
    @Size(min = 1, max = 30)
	private String address2;
    @NotNull
    @Size(min = 1, max = 25)
	private String city;
    @NotNull
    @Size(min = 1, max = 25)
	private String state;
    @NotNull
    @Size(min = 1, max = 25 )
	private String country = "USA";
	
	@Size(min=5, max=10)
	@Pattern(regexp = "[1-0A-Za-z ]*", message = "must contain only numbers, letters and spaces")
		private String zip;

    public Address() {
		super();
	}
	
	public Address(String address1, String address2, String city, String state,
			String country, String zip) {
		super();
		this.address1 = address1;
		this.address2 = address2;
		this.city = city;
		this.state = state;
		this.country = country;
		this.zip = zip;
	}

	/**
	 * @return the address1
	 */
	public String getAddress1() {
		return address1;
	}

	/**
	 * @param address1 the address1 to set
	 */
	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	/**
	 * @return the address2
	 */
	public String getAddress2() {
		return address2;
	}

	/**
	 * @param address2 the address2 to set
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the zip
	 */
	public String getZip() {
		return zip;
	}

	/**
	 * @param zip the zip to set
	 */
	public void setZip(String zip) {
		this.zip = zip;
	}

    public boolean equals(Object obj) {
        
    	if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        Address ad = (Address) obj;
        
        return ((address1 == ad.getAddress1() || (address1 != null && address1.equals(ad.getAddress1()))) &&
        		(address2 == ad.getAddress2() || (address2 != null && address2.equals(ad.getAddress2()))) &&
        		(city == ad.getCity() || (city != null && city.equals(ad.getCity()))) &&
        		(state == ad.getState() || (state != null && state.equals(ad.getState()))) &&
        		(zip == ad.getZip() || (zip != null && zip.equals(ad.getZip()))) &&
        		(country == ad.getCountry() || (country != null && country.equals(ad.getCountry()))) 
        		);
     
    }

    @Override
    public int hashCode() {
    	int hash = 7;
    	hash = 31 * hash + (null == address1 ? 0 : address1.hashCode());
    	hash = 31 * hash + (null == address2 ? 0 : address2.hashCode());
    	hash = 31 * hash + (null == city ? 0 : city.hashCode());
    	hash = 31 * hash + (null == state ? 0 : state.hashCode());
    	hash = 31 * hash + (null == zip ? 0 : zip.hashCode());
    	hash = 31 * hash + (null == country ? 0 : country.hashCode());
    	return hash;	
       
    }

	
}

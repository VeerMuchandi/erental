/**
 *
 * @filename ActivationDTO.java
 * @author Veer Muchandi
 * @created Jun 16, 2012
 * © Copyright 2012 Pinaka LLC
 *
 */
package com.pinaka.UserManagement;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @filename ActivationDTO.java
 * @author Veer Muchandi
 * @created Jun 16, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
@XmlRootElement
public class ActivationDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String activationKey;
	
	/**
	 * 
	 */
	public ActivationDTO() {
		// TODO Auto-generated constructor stub
	}
	public ActivationDTO(String activationKey) {
		super();
		this.activationKey = activationKey;
	}
	
	/**
	 * @return the activationKey
	 */
	public String getActivationKey() {
		return activationKey;
	}

	/**
	 * @param activationKey the activationKey to set
	 */
	public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}



}

package com.pinaka.UserManagement;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PasswordChangeDTO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String oldPassword, newPassword, retypedPassword;

	/**
	 * @return the oldPassword
	 */
	public String getOldPassword() {
		return oldPassword;
	}

	/**
	 * @param oldPassword the oldPassword to set
	 */
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	/**
	 * @return the newPassword
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * @return the retypedPassword
	 */
	public String getRetypedPassword() {
		return retypedPassword;
	}

	/**
	 * @param retypedPassword the retypedPassword to set
	 */
	public void setRetypedPassword(String retypedPassword) {
		this.retypedPassword = retypedPassword;
	}

	public PasswordChangeDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PasswordChangeDTO(String oldPassword, String newPassword,
			String retypedPassword) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.retypedPassword = retypedPassword;
	}
	

}

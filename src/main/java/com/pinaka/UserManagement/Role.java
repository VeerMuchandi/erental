/**
 *
 * @filename Role.java
 * @author Veer Muchandi
 * @created May 6, 2012
 * © Copyright 2012 Pinaka LLC
 *
 */
package com.pinaka.UserManagement;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * @filename Role.java
 * @author Veer Muchandi
 * @created May 6, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 */
@Embeddable
public class Role implements Serializable {
	private static final long serialVersionUID = 1L;	
	
	
	@Column(name = "Role")
	private String role;
	
	@Column(name = "RoleGroup")
	private String roleGroup;

	public Role() {
		super();
	}
	
	

	public Role(String role, String roleGroup) {
		super();
		this.role = role;
		this.roleGroup = roleGroup;
	}

	/**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * @return the roleGroup
	 */
	public String getRoleGroup() {
		return roleGroup;
	}

	/**
	 * @param roleGroup the roleGroup to set
	 */
	public void setRoleGroup(String roleGroup) {
		this.roleGroup = roleGroup;
	}


	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        Role u = (Role) obj;
        
        return ((role == u.getRole() || (role != null && role.equals(u.getRole()))) &&
        		(roleGroup == u.getRoleGroup() || (roleGroup != null && roleGroup.equals(u.getRoleGroup()))));
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	hash = 31 * hash + (null == role ? 0 : role.hashCode());
    	hash = 31 * hash + (null == roleGroup ? 0 : roleGroup.hashCode());
      	         	
    	return hash;	
       
    }

}

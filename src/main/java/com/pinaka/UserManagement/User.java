/**
 *
 * @filename User.java
 * @author Veer Muchandi
 * @created May 6, 2012
 * © Copyright 2012 Pinaka LLC
 *
 */
package com.pinaka.UserManagement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.pinaka.eRental.util.PropertyManager;

/**
 * @filename User.java
 * @author Veer Muchandi
 * @created May 6, 2012
 *
 * © Copyright 2012 Pinaka LLC
 * 
 * 
 */
@Entity
@XmlRootElement
@XmlType(propOrder = {"userName","roles", "title", "firstName", "lastName","email", "address", "phones","activated"})
@NamedQueries({
	@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
	@NamedQuery(name = "User.findByUserName", query = "SELECT user FROM User user where user.userName = :userName"),
	@NamedQuery(name = "User.findByFirstName", query = "SELECT user FROM User user where user.firstName = :firstName"),
	@NamedQuery(name = "User.findByLastName", query = "SELECT user FROM User user where user.lastName = :lastName"),
	@NamedQuery(name = "User.findByEmail", query = "SELECT user FROM User user where user.email = :email"),
	@NamedQuery(name = "User.findByFirstAndLastName", query = "SELECT user FROM User user where user.lastName = :lastName AND user.firstName = :firstName" ),
	@NamedQuery(name = "User.findByParams", query = "SELECT user FROM User user where user.lastName LIKE :lastName AND user.firstName LIKE :firstName AND user.email LIKE :email AND user.userName LIKE :userName")
})
@Table(name="Principals")
@Inheritance(strategy=InheritanceType.JOINED)
public class User implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;	
	
	@Id
	@Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
                 message="{invalid.email}")
	@Column(name = "PrincipalID")
	private String userName;
	
	@Column(name = "Password")
	private String password;
	
	//@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="principal")
	@ElementCollection(fetch=FetchType.EAGER, targetClass=Role.class)
	@CollectionTable(name="Roles", joinColumns = {@JoinColumn(name="PrincipalID")})
	@OrderColumn(name="roleOrder")
	private List<Role> roles = new ArrayList<Role>();
	//TODO - make roles a set 
	
	@Size(min = 1, max = 25)
	@Pattern(regexp = "[A-Za-z. ]*", message = "must contain only letters, spaces and .")
	private String title;
	
	@Size(min = 1, max = 25)
	@Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
	private String firstName;
	
	@Size(min = 1, max = 25)
	@Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
	private String lastName;
	

	@Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
                 message="{invalid.email}")
	private String email;
	
	@Embedded
	private Address address;
	
	@ElementCollection(fetch=FetchType.EAGER, targetClass=Phone.class)
	@CollectionTable(name="UserPhone", joinColumns = {@JoinColumn(name="userName")})
	@OrderColumn(name="phoneOrder")
	private Set<Phone> phones = new HashSet<Phone>();
	
	private boolean activated = false;


	public User() {
		super();
	}
	

	public User(String userName, String password, List<Role> roles,
			String title, String firstName, String lastName, String email,
			Address address, Set<Phone> phones) {
		super();
		this.userName = userName;
		this.password = password;
		this.roles = roles;
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.address = address;
		this.phones = phones;
		this.activated = false;
	}


	public User(String userName, String password, String title,
			String firstName, String lastName, String email, Address address,
			Set<Phone> phones) {
		super();
		this.userName = userName;
		this.password = password;
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.address = address;
		this.phones = phones;
		this.activated=false;
	}

	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	//TODO - protect password
	@XmlTransient
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**, b
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	/**
	 * @return the phones
	 */
	public Set<Phone> getPhones() {
		return phones;
	}

	/**
	 * @param phones the phones to set
	 */
	public void setPhones(Set<Phone> phones) {
		this.phones = phones;
	}
	
	public String generateFullName() {
		return (this.title ==null?"":this.title+" ")
					+this.firstName+" "
					+this.lastName;
	}

	/**
	 * @return the activated
	 */
	public boolean isActivated() {
		return activated;
	}


	/**
	 * @param activated the activated to set
	 */
	public void setActivated(boolean active) {
		this.activated = active;
	}
	
	protected boolean isAdministrator() {
		for (Role r:this.getRoles()) {
			if (r.getRole().equals("admin")) return true;
		}
		return false;
	}

	protected void addRole(Role role) {
		if (!isInRole(role)) this.roles.add(role);
	}
	
	protected boolean isInRole(Role role){
		for (Role r:this.getRoles()) {
			if (r.getRole().equals(role.getRole())) return true;
		}
		return false;
	}

	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        User u = (User) obj;
        
        return ((userName == u.getUserName() || (userName != null && userName.equals(u.getUserName()))) &&
        		(password == u.getPassword() || (password != null && password.equals(u.getPassword()))) &&
        		(title == u.getTitle() || (title != null && title.equals(u.getTitle()))) &&
        		(firstName == u.getFirstName() || (firstName != null && firstName.equals(u.getFirstName()))) &&
        		(lastName == u.getLastName() || (lastName != null && lastName.equals(u.getLastName()))) &&
        		(email == u.getEmail() || (email != null && email.equals(u.getEmail()))) &&
        		(address == u.getAddress() || (address != null && address.equals(u.getAddress()))) &&
        		(phones == u.getPhones() || (phones != null && phones.equals(u.getPhones()))) &&
        		(roles == u.getRoles() || (roles != null && roles.equals(u.getRoles()))));
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	hash = 31 * hash + (null == userName ? 0 : userName.hashCode());
    	hash = 31 * hash + (null == password ? 0 : password.hashCode());
    	hash = 31 * hash + (null == roles ? 0 : roles.hashCode());	
    	hash = 31 * hash + (null == title ? 0 : title.hashCode());
    	hash = 31 * hash + (null == firstName ? 0 : firstName.hashCode());
    	hash = 31 * hash + (null == lastName ? 0 : lastName.hashCode());
    	hash = 31 * hash + (null == email ? 0 : email.hashCode());
    	hash = 31 * hash + (null == address ? 0 : address.hashCode());
    	hash = 31 * hash + (null == phones ? 0 : phones.hashCode());
   	         	
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

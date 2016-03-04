/**
 * 
 */
package com.pinaka.eRental.exception;

/**
 * @author Muchandi
 *
 */
public class InvalidBusinessStateException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	private String thrownBy = "not specified";
	
	public InvalidBusinessStateException() {
	
	}

	/**
	 * @param message
	 */
	public InvalidBusinessStateException(String message) {
		super(message);
		
	}


	public InvalidBusinessStateException(String message, String thrownBy) {
	    super(message);
	    this.thrownBy = thrownBy;
	}
	
	/**
	 * @return the thrownBy
	 */
	public String getThrownBy() {
		return thrownBy;
	}

}

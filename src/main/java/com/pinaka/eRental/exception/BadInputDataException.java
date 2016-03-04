package com.pinaka.eRental.exception;

import java.io.Serializable;

public class BadInputDataException extends Exception implements Serializable {
	
	/**
	 * Custom exception to use whenever bad input data is received by the eRental Application.
	 */
private static final long serialVersionUID = 1L;
private String thrownBy = "not specified";



public BadInputDataException() {
	    }
	    
public BadInputDataException(String msg) {
	        super(msg);
	    }

public BadInputDataException(String msg, String thrownBy) {
    super(msg);
    this.thrownBy = thrownBy;
}

/**
 * @return the thrownBy
 */
public String getThrownBy() {
	return thrownBy;
}
	

}

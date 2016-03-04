package com.pinaka.eRental.model;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity implementation class for Entity: PenaltyTerm
 *
 */
@Embeddable
@XmlRootElement

public class PenaltyTerm implements Serializable{

	private static final long serialVersionUID = 1L;

	@Size(min = 0, max = 1000)
	@NotNull
	private String penaltyTerm;
	
	@Digits(integer=6, fraction=2)
	private double penaltyCharge=0.0;
	
	public PenaltyTerm() {
		super();
	}   
	public String getPenaltyTerm() {
		return this.penaltyTerm;
	}

	public void setPenaltyTerm(String penaltyTerm) {
		this.penaltyTerm = penaltyTerm;
	}   
	public double getPenaltyCharge() {
		return this.penaltyCharge;
	}

	public void setPenaltyCharge(double penaltyCharge) {
		this.penaltyCharge = penaltyCharge;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        PenaltyTerm pt = (PenaltyTerm) obj;
        
        return ((Double.doubleToLongBits(penaltyCharge) == Double.doubleToLongBits(pt.penaltyCharge)) && 
         		(penaltyTerm == pt.getPenaltyTerm() || (penaltyTerm != null && penaltyTerm.equals(pt.getPenaltyTerm()))));
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	long bits = Double.doubleToLongBits(penaltyCharge);
    	int var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;

    	hash = 31 * hash + (null == penaltyTerm ? 0 : penaltyTerm.hashCode());
    	
    	return hash;	
       
    }
   
}

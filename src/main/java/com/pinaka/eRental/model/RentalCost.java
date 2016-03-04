package com.pinaka.eRental.model;

import java.io.Serializable;
import java.lang.String;
import java.util.Calendar;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Entity implementation class for Entity: RentalCost
 *
 */
@Embeddable
@XmlRootElement
public class RentalCost implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Digits(integer=6, fraction=2)
	private double cost;
	
	@Enumerated(EnumType.STRING)
	private RentalPeriodType period = RentalPeriodType.DAY;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar effectiveDate = Calendar.getInstance();
	
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar endDate = null;
	
	@Digits(integer=6, fraction=2)
	private double safetyDeposit = 0.0;
	
	@Size(min = 0, max = 1000)
	private String comments;
	
	private boolean active = true;
	
	public RentalCost() {
		super();
	}   
	public double getCost() {
		return this.cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}   
 
	public double getSafetyDeposit() {
		return this.safetyDeposit;
	}

	public void setSafetyDeposit(double safetyDeposit) {
		this.safetyDeposit = safetyDeposit;
	}   
	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}   
	public boolean getActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	/**
	 * @return the period
	 */
	public RentalPeriodType getPeriod() {
		return period;
	}
	/**
	 * @param period the period to set
	 */
	public void setPeriod(RentalPeriodType period) {
		this.period = period;
	}
	/**
	 * @return the effectiveDate
	 */
	public Calendar getEffectiveDate() {
		return effectiveDate;
	}
	/**
	 * @param effectiveDate the effectiveDate to set
	 */
	public void setEffectiveDate(Calendar effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	/**
	 * @return the endDate
	 */
	public Calendar getEndDate() {
		return endDate;
	}
	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}
   
	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        RentalCost rc = (RentalCost) obj;
        
        return ((Double.doubleToLongBits(cost) == Double.doubleToLongBits(rc.getCost())) && 
        		(Double.doubleToLongBits(safetyDeposit) == Double.doubleToLongBits(rc.getSafetyDeposit())) &&
        		(period == rc.getPeriod() || (period != null && period.equals(rc.getPeriod()))) &&
        		(effectiveDate == rc.getEffectiveDate() || (effectiveDate != null && effectiveDate.equals(rc.getEffectiveDate()))) &&
        		(endDate == rc.getEndDate() || (endDate != null && endDate.equals(rc.getEndDate()))) &&
        		(comments == rc.getComments() || (comments != null && comments.equals(rc.getComments()))) &&
        		(active == rc.getActive()));
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	long bits = Double.doubleToLongBits(cost);
    	int var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	bits = Double.doubleToLongBits(safetyDeposit);
    	var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	
    	hash = 31 * hash + (null == period ? 0 : period.hashCode());
    	hash = 31 * hash + (null == effectiveDate ? 0 : effectiveDate.hashCode());
    	hash = 31 * hash + (null == endDate ? 0 : endDate.hashCode());
    	hash = 31 * hash + (null == comments ? 0 : comments.hashCode());
    	hash = 31 * hash + (active ? 1 : 0);
    	
    	return hash;	
       
    }
	   
}

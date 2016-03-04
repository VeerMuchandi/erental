/**
 * 
 */
package com.pinaka.eRental.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Digits;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Muchandi
 *
 */
@Entity
@XmlRootElement
@XmlType(propOrder = { "id", "plannedStartDate", "plannedReturnDate", 
		"actualRentalStart", "actualRentalReturn",
		"borrower", "rentedItem",
		"initialRentCollected", "safetyDepositCollected",
		"rentalServiceCommission","finalRent", "status","rejectionReason", "otherRejectionReason",
		"rejectionReasonSharable","requestWithdrawalReason"})
@NamedQueries({
	@NamedQuery(name = "RentalInstance.findRentalInstancesByOwner", 
			    query = "SELECT ri FROM RentalInstance ri JOIN ri.rentedItem ritem " +
			    		"WHERE ritem.owner = ANY" +
			    		"(SELECT ru FROM RentalUser ru WHERE ru.userId = :ownerId)")
})
public class RentalInstance implements Serializable, Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private long id;
	
	@ManyToOne
//	@JoinColumn(name="borrowerId")
	private RentalUser borrower;
	
	@ManyToOne
	private RentalItem rentedItem;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar plannedStartDate;
	

	@Temporal(TemporalType.TIMESTAMP)
	private Calendar plannedReturnDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar actualRentalStart;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar actualRentalReturn;
	
	@Digits(integer=6, fraction=2)
	private double initialRentCollected = 0.0;
	
	@Digits(integer=6, fraction=2)
	private double safetyDepositCollected = 0.0;
	
	@Digits(integer=6, fraction=2)
	private double rentalServiceCommission = 0.0; //eRental commission
	
	@Digits(integer=6, fraction=2)
	private double finalRent = 0.0;
	
	@Enumerated(EnumType.STRING)
	private RentalStatusType status;
	//TODO - add audit info on when the status changed
	
	@Enumerated(EnumType.STRING)
	private RejectionReasonType rejectionReason;
	
	private String otherRejectionReason;
	
	private boolean rejectionReasonSharable = false;
	
	private String requestWithdrawalReason;
	
	
	
	/**
	 * 
	 */
	
	public RentalInstance() {
	}


	/**
	 * @return the borrower
	 */
	public RentalUser getBorrower() {
		return borrower;
	}


	/**
	 * @param borrower the borrower to set
	 */
	public void setBorrower(RentalUser renter) {
		this.borrower = renter;
	}


	/**
	 * @return the rentedItem
	 */
	public RentalItem getRentedItem() {
		return rentedItem;
	}


	/**
	 * @param rentedItem the rentedItem to set
	 */
	public void setRentedItem(RentalItem rentedItem) {
		this.rentedItem = rentedItem;
	}

	/**
	 * @return the plannedStartDate
	 */
	public Calendar getPlannedStartDate() {
		return plannedStartDate;
	}


	/**
	 * @param plannedStartDate the plannedStartDate to set
	 */
	public void setPlannedStartDate(Calendar plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
	}


	/**
	 * @return the plannedReturnDate
	 */
	public Calendar getPlannedReturnDate() {
		return plannedReturnDate;
	}


	/**
	 * @param plannedReturnDate the plannedReturnDate to set
	 */
	public void setPlannedReturnDate(Calendar plannedReturnDate) {
		this.plannedReturnDate = plannedReturnDate;
	}


	/**
	 * @return the actualRentalStart
	 */
	public Calendar getActualRentalStart() {
		return actualRentalStart;
	}


	/**
	 * @param actualRentalStart the actualRentalStart to set
	 */
	public void setActualRentalStart(Calendar actualRentalStart) {
		this.actualRentalStart = actualRentalStart;
	}


	/**
	 * @return the actualRentalReturn
	 */
	public Calendar getActualRentalReturn() {
		return actualRentalReturn;
	}


	/**
	 * @param actualRentalReturn the actualRentalReturn to set
	 */
	public void setActualRentalReturn(Calendar actualRentalReturn) {
		this.actualRentalReturn = actualRentalReturn;
	}


	/**
	 * @return the initialRentCollected
	 */
	public double getInitialRentCollected() {
		return initialRentCollected;
	}


	/**
	 * @param initialRentCollected the initialRentCollected to set
	 */
	public void setInitialRentCollected(double initialRentCollected) {
		this.initialRentCollected = initialRentCollected;
	}


	/**
	 * @return the safetyDepositCollected
	 */
	public double getSafetyDepositCollected() {
		return safetyDepositCollected;
	}


	/**
	 * @param safetyDepositCollected the safetyDepositCollected to set
	 */
	public void setSafetyDepositCollected(double safetyDepositCollected) {
		this.safetyDepositCollected = safetyDepositCollected;
	}


	/**
	 * @return the rentalServiceCommission
	 */
	public double getRentalServiceCommission() {
		return rentalServiceCommission;
	}


	/**
	 * @param rentalServiceCommission the rentalServiceCommission to set
	 */
	public void setRentalServiceCommission(double rentalServiceCommission) {
		this.rentalServiceCommission = rentalServiceCommission;
	}


	/**
	 * @return the finalRent
	 */
	public double getFinalRent() {
		return finalRent;
	}


	/**
	 * @param finalRent the finalRent to set
	 */
	public void setFinalRent(double finalRent) {
		this.finalRent = finalRent;
	}


	/**
	 * @return the status
	 */
	public RentalStatusType getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(RentalStatusType status) {
		this.status = status;
	}


	/**
	 * @return the rejectionReason
	 */
	public RejectionReasonType getRejectionReason() {
		return rejectionReason;
	}


	/**
	 * @param rejectionReason the rejectionReason to set
	 */
	public void setRejectionReason(RejectionReasonType rejectionReason) {
		this.rejectionReason = rejectionReason;
	}


	/**
	 * @return the otherRejectionReason
	 */
	public String getOtherRejectionReason() {
		return otherRejectionReason;
	}


	/**
	 * @param otherRejectionReason the otherRejectionReason to set
	 */
	public void setOtherRejectionReason(String otherReason) {
		this.otherRejectionReason = otherReason;
	}





	/**
	 * @return the rejectionReasonSharable
	 */
	public boolean isRejectionReasonSharable() {
		return rejectionReasonSharable;
	}


	/**
	 * @param rejectionReasonSharable the rejectionReasonSharable to set
	 */
	public void setRejectionReasonSharable(boolean rejectionReasonSharable) {
		this.rejectionReasonSharable = rejectionReasonSharable;
	}


	/**
	 * @return the requestWithdrawalReason
	 */
	public String getRequestWithdrawalReason() {
		return requestWithdrawalReason;
	}


	/**
	 * @param requestWithdrawalReason the requestWithdrawalReason to set
	 */
	public void setRequestWithdrawalReason(String requestWithdrawalReason) {
		this.requestWithdrawalReason = requestWithdrawalReason;
	}


	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		      
        RentalInstance ri = (RentalInstance) obj;
        
        return ((Double.doubleToLongBits(initialRentCollected) == Double.doubleToLongBits(ri.getInitialRentCollected())) && 
        		(Double.doubleToLongBits(finalRent) == Double.doubleToLongBits(ri.getFinalRent())) &&
        		(Double.doubleToLongBits(rentalServiceCommission) == Double.doubleToLongBits(ri.getRentalServiceCommission())) &&
        		(Double.doubleToLongBits(safetyDepositCollected) == Double.doubleToLongBits(ri.getSafetyDepositCollected())) &&
        		(plannedStartDate == ri.getPlannedStartDate() || (plannedStartDate != null && plannedStartDate.equals(ri.getPlannedStartDate()))) &&
        		(plannedReturnDate == ri.getPlannedReturnDate() || (plannedReturnDate != null && plannedReturnDate.equals(ri.getPlannedReturnDate()))) &&
        		(actualRentalStart == ri.getActualRentalStart() || (actualRentalStart != null && actualRentalStart.equals(ri.getActualRentalStart()))) &&
        		(actualRentalReturn == ri.getActualRentalReturn() || (actualRentalReturn != null && actualRentalReturn.equals(ri.getActualRentalReturn()))) &&
        		(status == ri.getStatus() || (status != null && status.equals(ri.getStatus()))) &&
        		(rejectionReason == ri.getRejectionReason() || (rejectionReason != null && rejectionReason.equals(ri.getRejectionReason()))) &&
        		(otherRejectionReason == ri.getOtherRejectionReason() || (otherRejectionReason != null && otherRejectionReason.equals(ri.getOtherRejectionReason()))) &&
        		(rejectionReasonSharable == ri.isRejectionReasonSharable())&&
        		(borrower == ri.getBorrower() || (borrower != null && borrower.equals(ri.getBorrower()))) &&
        		(rentedItem == ri.getRentedItem() || (rentedItem != null && rentedItem.equals(ri.getRentedItem())))
        		
        		);
     }

    @Override
    public int hashCode() {
    	int hash = 7;
    	
    	long bits = Double.doubleToLongBits(initialRentCollected);
    	int var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	bits = Double.doubleToLongBits(finalRent);
    	var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	bits = Double.doubleToLongBits(rentalServiceCommission);
    	var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	bits = Double.doubleToLongBits(safetyDepositCollected);
    	var_code = (int)(bits ^ (bits >>> 32));
    	hash = 31 * hash + var_code;
    	
    	hash = 31 * hash + (null == plannedStartDate ? 0 : plannedStartDate.hashCode());
    	hash = 31 * hash + (null == plannedReturnDate ? 0 : plannedReturnDate.hashCode());
    	hash = 31 * hash + (null == actualRentalStart ? 0 : actualRentalStart.hashCode());
    	hash = 31 * hash + (null == actualRentalReturn ? 0 : actualRentalReturn.hashCode());
    	hash = 31 * hash + (null == status ? 0 : status.hashCode());
    	hash = 31 * hash + (null == rejectionReason ? 0 : rejectionReason.hashCode());
    	hash = 31 * hash + (null == otherRejectionReason ? 0 : otherRejectionReason.hashCode());
    	hash = 31 * hash + (null == borrower ? 0 : borrower.hashCode());
    	hash = 31 * hash + (null == rentedItem ? 0 : rentedItem.hashCode());
    	hash = 31 * hash + (rejectionReasonSharable ? 1 : 0);
    	
   	         	
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

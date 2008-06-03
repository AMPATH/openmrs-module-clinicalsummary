/**
 * 
 */
package org.openmrs.module.clinicalsummary;

import java.util.Date;

import org.openmrs.Patient;
import org.openmrs.Location;

/**
 * Object representing a "summary" in the queue waiting to be printed or already printed
 * 
 */
public class ClinicalSummaryQueueItem {
	
	public enum CLINICAL_SUMMARY_QUEUE_STATUS {WAITING_ON_LABS, GENERATED, PENDING, PRINTING, PRINTED, ERROR, CANCELLED}
	
	private Integer clinicalSummaryQueueId;
	private Patient patient;
	private Location location;
	private Date encounterDatetime;
	private String status = CLINICAL_SUMMARY_QUEUE_STATUS.PENDING.name();
	private String errorMessage;
	private Date datePrinted;
	private Date dateCreated;
	private String fileName;
	
	/**
	 * Default empty constructor
	 */
	public ClinicalSummaryQueueItem() {	}

	/**
	 * Convenience constructor taking in all items in this object
	 * 
	 * @param patient
	 * @param encounterDatetime
	 * @param status
	 */
	public ClinicalSummaryQueueItem(Patient patient, Location location, Date encounterDatetime, CLINICAL_SUMMARY_QUEUE_STATUS status) {
		this.patient = patient;
		this.location = location;
		this.encounterDatetime = encounterDatetime;
		this.status = status.name();
	}
	
	/**
	 * @return the clinicalSummaryQueueId
	 */
	public Integer getClinicalSummaryQueueId() {
		return clinicalSummaryQueueId;
	}

	/**
	 * @param clinicalSummaryQueueId the clinicalSummaryQueueId to set
	 */
	public void setClinicalSummaryQueueId(Integer clinicalSummaryQueueId) {
		this.clinicalSummaryQueueId = clinicalSummaryQueueId;
	}

	/**
	 * @return the encounterDatetime
	 */
	public Date getEncounterDatetime() {
		return encounterDatetime;
	}
	
	/**
	 * @param encounterDatetime the encounterDatetime to set
	 */
	public void setEncounterDatetime(Date encounterDatetime) {
		this.encounterDatetime = encounterDatetime;
	}
	
	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * @return the status
	 */
	public CLINICAL_SUMMARY_QUEUE_STATUS getStatus() {
		return CLINICAL_SUMMARY_QUEUE_STATUS.valueOf(status);
	}
	
	private void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * @param status the status to set
	 */
	public void setStatus(CLINICAL_SUMMARY_QUEUE_STATUS status) {
		setStatus(status.name());
	}

	/**
	 * @return the datePrinted
	 */
	public Date getDatePrinted() {
		return datePrinted;
	}

	/**
	 * @param datePrinted the datePrinted to set
	 */
	public void setDatePrinted(Date datePrinted) {
		this.datePrinted = datePrinted;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName( ) {
		return this.fileName;
	}
	
	public boolean hasFileName( ) {
		if (null == getFileName())
			return false;
		return true;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ClinicalSummaryQueueId: " + clinicalSummaryQueueId;
	}
	
}

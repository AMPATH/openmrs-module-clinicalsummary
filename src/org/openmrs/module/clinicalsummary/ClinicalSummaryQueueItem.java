/**
 * 
 */
package org.openmrs.module.clinicalsummary;

import java.util.Date;

import org.openmrs.Patient;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.Encounter;

/**
 * Object representing a "summary" in the queue waiting to be printed or already printed
 *
 * TODO: Remove provider, patient, location, encounterDatetime and use encounter exclusively to obtain these.
 * TODO: The above will require running an sql query to fill in encounter_id for existing queue items that did not previously have encounter.
 */
public class ClinicalSummaryQueueItem {
	
	public enum CLINICAL_SUMMARY_QUEUE_STATUS {WAITING_ON_LABS, GENERATED, PENDING, PRINTING, PRINTED, ERROR, CANCELLED}
	
	private Integer clinicalSummaryQueueId;
    private Encounter encounter;
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
	 * @deprecated Should use ClinicalSummaryQueueItem(Encounter encounter, CLINICAL_SUMMARY_QUEUE_STATUS status)
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
     * Constructor with encounter and status.  
     * @param encounter
     * @param status
     */
    public ClinicalSummaryQueueItem(Encounter encounter, CLINICAL_SUMMARY_QUEUE_STATUS status) {
        setEncounterData(encounter);
        this.status = status.name();
    }
	
	/**
	 * @return the clinicalSummaryQueueId
	 */
	public Integer getClinicalSummaryQueueId() {
		return this.clinicalSummaryQueueId;
	}

	/**
	 * @param clinicalSummaryQueueId the clinicalSummaryQueueId to set
	 */
	public void setClinicalSummaryQueueId(Integer clinicalSummaryQueueId) {
		this.clinicalSummaryQueueId = clinicalSummaryQueueId;
	}

    /**
     * Set the encounter.  This method is NOT backwards compatible with
     * ClinicalSummary Module versions prior to 1.3.1 that did not have
     * ClinicalSummaryQueueItem.encounter.  For backwards compatibility, use
     * setEncounterData(Encounter encounter).
     * @param encounter
     */
    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    /**
     * Sets the encounter, patient, location, encounterDatetime.
     * This method allows backward compatibility with ClinicalSummary Module version 3.0.0
     * and prior - versions that did not have ClinicalSummaryQueueItem.encounter.
     * @param encounter
     */
    public void setEncounterData(Encounter encounter) {
        setEncounter(encounter);
        setPatient(encounter.getPatient());
        setLocation(encounter.getLocation());
        setEncounterDatetime(encounter.getEncounterDatetime());
    }

    /**
     * Get the encounter.
     * @return
     */
    public Encounter getEncounter() {
        return this.encounter;
    }

	/**
	 * @return the encounterDatetime
     * @deprecated Should use getEncounter().getEncounterDatetime();
	 */
	public Date getEncounterDatetime() {
        if (null != getEncounter() && null != getEncounter().getEncounterDatetime()) {
            return getEncounter().getEncounterDatetime();
        }
		return this.encounterDatetime;
	}
	
	/**
	 * @param encounterDatetime the encounterDatetime to set
     * @deprecated Should use getEncounter().setEncounterDatetime(EncounterDatetime encounterDatetime);
	 */
	public void setEncounterDatetime(Date encounterDatetime) {
		this.encounterDatetime = encounterDatetime;
	}
	
	/**
	 * @return the patient
     * @deprecated Should use getEncounter().getPatient()
	 */
	public Patient getPatient() {
        if (null != getEncounter() && null != getEncounter().getPatient()) {
            return getEncounter().getPatient();
        }
		return this.patient;
	}
	
	/**
	 * @param patient the patient to set
     * @deprecated Should use getEncounter().setPatient();
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the location
     * @deprecated Should use getEncounter().getLocation();
	 */
	public Location getLocation() {
        if (null != getEncounter() && null != getEncounter().getLocation()) {
            return getEncounter().getLocation();
        }
		return this.location;
	}
	
	/**
	 * @param location the location to set
     * @deprecated Should use getEncounter().setLocation();
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
		return this.datePrinted;
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
		return this.errorMessage;
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
		return this.dateCreated;
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

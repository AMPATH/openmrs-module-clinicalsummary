package org.openmrs.module.clinicalsummary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

/**
 *
 */
public abstract class ClinicalSummaryPrintingLogic {
	
	private Log log = LogFactory.getLog(getClass());
	
	/**
	 * 
	 * @param e
	 */
	public void queueOrPrintEncounter(Encounter e) {
		log.debug("Queuing or printing encounter: " + e.getEncounterId());
		
		if (isPrintWorthy(e)) {
			// if the summary should be printed ever
			if (delayPrinting(e)) {
				// put this patient in the queue to print later
				log.debug("Queuing encounter: " + e.getEncounterId());
				createQueueItem(e, ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.WAITING_ON_LABS);
			}
			else {
				log.debug("Printing encounter: " + e.getEncounterId());
				print(e);
			}
		}
	}
	
	/**
	 * Subclasses must override this method.  If this encounter should be printed 
	 * right away, return true, if it should be skipped or queue, return false
	 * 
	 * @param e
	 * @return true/false
	 */
	protected abstract Boolean isPrintWorthy(Encounter e);
	
	/**
	 * Subclasses must override this method. 
	 * 
	 * If this method is reached, it is assumed isPrintWorthy already returned true for
	 * this encounter.  [Obsolete: If this encounter should be queued return true.  If this 
	 * encounter should be printed immediately, return false]
	 * 
	 * Update: If this encounter should wait on labs return true.  If this
	 * encounter should be queued for printing, return false.
	 * 
	 * @param e
	 * @return true/false
	 */
	protected abstract Boolean delayPrinting(Encounter e);
	
	/**
	 * @param encounter Encounter to print
	 */
	protected final void print(Encounter encounter) {
		Patient p = encounter.getPatient();
		//PatientSet ps = new PatientSet();
		Cohort ps = new Cohort();
		ps.addMember(p.getPatientId());
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		// Queue up summaries to be printed manually.  Set status to GENERATED.
		ClinicalSummaryQueueItem queueItem = new ClinicalSummaryQueueItem(encounter, ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS.GENERATED);
		Integer id = css.createQueueItem(queueItem);
		ClinicalSummaryQueueItem qItem = css.getQueueItem(id);
        // Generate summary and add any patient reminders to ReminderLog.
		css.generatePatientSummary(qItem, false, true);
	}
	
	/**
	 * 
	 * @param encounter Encounter to queue up
	 */
	protected final void createQueueItem(Encounter encounter) {
		ClinicalSummaryQueueItem queueItem = new ClinicalSummaryQueueItem();
        queueItem.setEncounterData(encounter);
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		css.createQueueItem(queueItem);
	}

	/**
	 * 
	 * @param encounter Encounter to queue up
	 * @param status WAITING_ON_LABS, PENDING, PRINTING, PRINTED, ERROR, CANCELLED
	 */
	protected final void createQueueItem(Encounter encounter, ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS status) {
		ClinicalSummaryQueueItem queueItem = new ClinicalSummaryQueueItem(encounter, status);
		ClinicalSummaryService css = (ClinicalSummaryService)Context.getService(ClinicalSummaryService.class);
		css.createQueueItem(queueItem);
	}

}

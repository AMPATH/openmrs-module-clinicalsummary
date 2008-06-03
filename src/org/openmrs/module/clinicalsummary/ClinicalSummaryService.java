package org.openmrs.module.clinicalsummary;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ClinicalSummaryService {
	
	@Authorized({"Manage Clinical Summaries"})
	public void createClinicalSummary(ClinicalSummary summary);
	
	@Authorized({"Manage Clinical Summaries"})
	public void updateClinicalSummary(ClinicalSummary summary);
	
	@Transactional(readOnly=true)
	@Authorized({"View Clinical Summary"})
	public ClinicalSummary getClinicalSummary(Integer id);
	
	@Transactional(readOnly=true)
	@Authorized({"View Clinical Summary"})
	public List<ClinicalSummary> getClinicalSummaries();
	
	@Transactional(readOnly=true)
	@Authorized({"View Clinical Summary"})
	public ClinicalSummary getPreferredClinicalSummary();
	
	@Transactional(readOnly=true)
	@Authorized({"View Observations"})
	public Map<Integer, List<Object>> getObservationsForEncounters(Collection<Encounter> encounters, Concept c);
	
	/**
	 * If beforeOrEqualToDate is null, all queue items are returned
	 * If status is null, all statuses are returned
	 * 
	 * @param beforeOrEqualToDate
	 * @param status list of CLINICAL_SUMMARY_QUEUE_STATUS statuses to retrieve
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<ClinicalSummaryQueueItem> getQueueItems(Date beforeOrEqualToDate, List<CLINICAL_SUMMARY_QUEUE_STATUS> status);

	/*
	 * If beforeOrEqualToDate is null, all queue items are returned
	 * If status is null, all statuses are returned
	 * 
	 * @param beforeOrEqualToDate
	 * @param status list of CLINICAL_SUMMARY_QUEUE_STATUS statuses to retrieve
	 * @param order ClinicalSummaryUtil.ORDER order to retrieve results 
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<ClinicalSummaryQueueItem> getQueueItems(Date beginDate, Date endDate, List<String> locations, List<String> statuses, ClinicalSummaryUtil.ORDER order, int offset, int limit);
	
	
	/**
	 * @param queueId
	 * @return
	 */
	@Transactional(readOnly=true)
	public ClinicalSummaryQueueItem getQueueItem(Integer queueId);
	
	public Integer createQueueItem(ClinicalSummaryQueueItem item);
	
	public void updateQueueItem(ClinicalSummaryQueueItem item);
	
	public void deleteQueueItem(ClinicalSummaryQueueItem item);
	
	/**
	 * 
	 * return patient ids corresponding to the given queue ids.  If statusToSet is not null, update all these
	 * queue items with this status
	 * 
	 * @param queueIds 
	 * @param statusToSet optional.
	 * @return list of patient ids
	 */
	public List<Integer> getQueuePatientIds(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet);
	
	/**
	 * Update the given queue ids with the given statusToSet
	 * 
	 * @param queueIds
	 * @param statusToSet
	 */
	public void setQueueStatus(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet);
	
	/**
	 * Generates default summaries for the given patient to the file system.
	 * @param patient
	 * @param location
	 * @param interactive
	 * @return
	 */
	public Boolean generatePatientSummary(ClinicalSummaryQueueItem queueItem, boolean interactive);

	/**
	 * Moves generated clinical summary .pdf's to the "to_print" directory in the file system
	 * @param queueItems
	 * @return
	 */
	public Boolean printClinicalSummaryQueueItems(List<Integer> queueItems);
	
}

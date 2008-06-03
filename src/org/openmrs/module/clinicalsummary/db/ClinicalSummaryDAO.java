package org.openmrs.module.clinicalsummary.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.module.clinicalsummary.ClinicalSummary;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem;
import org.openmrs.module.clinicalsummary.ClinicalSummaryUtil;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS;
import org.springframework.transaction.annotation.Transactional;

public interface ClinicalSummaryDAO {
		
	public void createClinicalSummary(ClinicalSummary summary);
	
	public ClinicalSummary getClinicalSummary(Integer id);
	
	public void updateClinicalSummary(ClinicalSummary summary);
	
	public List<ClinicalSummary> getClinicalSummaries();
	
	public Map<Integer, List<Object>> getObservationsForEncounters(Collection<Encounter> encounters, Concept c);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItems(java.util.Date, java.util.List)
	 */
	@Transactional(readOnly=true)
	public List<ClinicalSummaryQueueItem> getQueueItems(Date beforeDateOrEqualTo, List<String> status);

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItems(java.util.Date, java.util.List, ClinicalSummaryUtil.ORDER)
	 * 
	 * @param beforeOrEqualToDate
	 * @param status
	 * @param order
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<ClinicalSummaryQueueItem> getQueueItems(Date beginDate, Date endDate, List<String> locations, List<String> statuses, ClinicalSummaryUtil.ORDER order, int offset, int limit);
		
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItem(Integer)
	 */
	@Transactional(readOnly=true)	
	public ClinicalSummaryQueueItem getQueueItem(Integer queueId);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#createQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public Integer createQueueItem(ClinicalSummaryQueueItem item);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#updateQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public void updateQueueItem(ClinicalSummaryQueueItem item);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#deleteQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public void deleteQueueItem(ClinicalSummaryQueueItem item);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueuePatientIds(java.util.List, org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS)
	 */
	public List<?> getQueuePatientIds(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet);
		
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#setQueueStatus(java.util.List, org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS)
	 */
	public void setQueueStatus(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet);
}

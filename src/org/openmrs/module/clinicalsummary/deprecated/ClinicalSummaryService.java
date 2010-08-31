package org.openmrs.module.clinicalsummary.deprecated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.annotation.Authorized;
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
}

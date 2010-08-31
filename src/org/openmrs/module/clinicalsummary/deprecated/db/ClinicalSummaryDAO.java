package org.openmrs.module.clinicalsummary.deprecated.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.module.clinicalsummary.deprecated.ClinicalSummary;

public interface ClinicalSummaryDAO {
		
	public void createClinicalSummary(ClinicalSummary summary);
	
	public ClinicalSummary getClinicalSummary(Integer id);
	
	public void updateClinicalSummary(ClinicalSummary summary);
	
	public List<ClinicalSummary> getClinicalSummaries();
	
	public Map<Integer, List<Object>> getObservationsForEncounters(Collection<Encounter> encounters, Concept c);
}

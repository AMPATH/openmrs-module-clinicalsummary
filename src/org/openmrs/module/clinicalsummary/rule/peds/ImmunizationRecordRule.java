/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.clinicalsummary.rule.peds;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.cache.SummaryDataSource;
import org.openmrs.module.clinicalsummary.concept.ConceptRegistry;
import org.openmrs.module.clinicalsummary.concept.StandardConceptConstants;
import org.openmrs.module.clinicalsummary.encounter.TypeConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 */
public class ImmunizationRecordRule implements Rule {
	
	private static final Log log = LogFactory.getLog(ImmunizationRecordRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		Result immunizations = new Result();
		
		SummaryService service = Context.getService(SummaryService.class);
		
		List<String> typeNames = Arrays.asList(TypeConstants.ADULT_INITIAL,
		    TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
		    TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN,
		    TypeConstants.PEDS_NONCLINICAL);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(typeNames).last();

		LogicCriteria immunizationHistoryCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.IMMUNIZATION_HISTORY);
		Result immunizationHistoryResults = context.read(patient, service.getLogicDataSource("summary"), immunizationHistoryCriteria.and(encounterCriteria));

		LogicCriteria prevImmunizationCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.PREVIOUS_IMMUNIZATIONS_NAME);
		Result prevImmunizationResults = context.read(patient, service.getLogicDataSource("summary"), prevImmunizationCriteria.and(encounterCriteria));

		LogicCriteria prevDoseCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.PREVIOUS_DOSES_RECEIVED_NAME);
		Result prevDoseResults = context.read(patient, service.getLogicDataSource("summary"), prevDoseCriteria.and(encounterCriteria));
		
		Concept completedImmunization = ConceptRegistry.getCachedConcept(StandardConceptConstants.COMPLETED_IMMUNIZATIONS_NAME);
		
		int[] prevObsVisited = new int[2];
		
		Map<Concept, Double> immunizationMap = new HashMap<Concept, Double>();
		
		// the structure is:
		// Main Result:
		// -- Per Drug Result
		//    -- Drug Name
		//    -- Drug Dose (optional)
		
		for (Result immunizationHistoryResult : immunizationHistoryResults) {
			
			Obs historyObs = (Obs) immunizationHistoryResult.getResultObject();
			
			Obs prevImmunizationObs = null;
			boolean foundDrug = false;
			while (prevObsVisited[0] < prevImmunizationResults.size() && !foundDrug) {
				Result prevImmunizationResult = prevImmunizationResults.get(prevObsVisited[0]);
				prevImmunizationObs = (Obs) prevImmunizationResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(historyObs, prevImmunizationObs.getObsGroup())) {
					// if it's completed then just return one result, the completed result
					if (OpenmrsUtil.nullSafeEquals(prevImmunizationObs.getValueCoded(), completedImmunization)) {
						Result drugResult = new Result();
						drugResult.add(prevImmunizationResult);
						drugResult.add(new Result());
						immunizations.add(drugResult);
						return immunizations;
					}
					// we need to get out of the loop because we already found the drug
					foundDrug = true;
				}
				prevObsVisited[0]++;
			}
			
			// this history result will not be attached to the main result
			// so, we won't get an immunization result that contains only dose
			// we might get immunization result with blank dose but contains drug
			Obs prevDoseObs = null;
			boolean foundDose = false;
			while (prevObsVisited[1] < prevDoseResults.size() && !foundDose) {
				Result prevDoseResult = prevDoseResults.get(prevObsVisited[1]);
				prevDoseObs = (Obs) prevDoseResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(historyObs, prevDoseObs.getObsGroup()))
					// we also found the dose, get out of the loop
					foundDose = true;
				prevObsVisited[1]++;
			}
			
			Concept drug = prevImmunizationObs == null ? null : prevImmunizationObs.getValueCoded();
			Double dose = prevDoseObs == null ? null : prevDoseObs.getValueNumeric();
			if (drug != null)
				immunizationMap.put(drug, dose);
		}
		
		Concept noneConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.NONE);

		LogicCriteria orderedImmunizationCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.IMMUNIZATIONS_ORDERED_DETAILED);
		Result orderedImmunizationResults = context.read(patient, service.getLogicDataSource("summary"), orderedImmunizationCriteria.and(encounterCriteria));

		LogicCriteria orderedCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.IMMUNIZATIONS_ORDERED_NAME);
		Result orderedResults = context.read(patient, service.getLogicDataSource("summary"), orderedCriteria.and(encounterCriteria));

		LogicCriteria orderedDoseCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.IMMUNIZATIONS_DOSES_NAME);
		Result orderedDoseResults = context.read(patient, service.getLogicDataSource("summary"), orderedDoseCriteria.and(encounterCriteria));
		
		int[] orderObsVisited = new int[2];
		
		for (Result orderedImmunizationResult : orderedImmunizationResults) {
			Obs orderedObservation = (Obs) orderedImmunizationResult.getResultObject();
			
			Obs orderedObs = null;
			boolean foundDrug = false;
			while (orderObsVisited[0] < orderedResults.size() && !foundDrug) {
				Result orderedResult = orderedResults.get(orderObsVisited[0]);
				orderedObs = (Obs) orderedResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(orderedObservation, orderedObs.getObsGroup()))
					if (!OpenmrsUtil.nullSafeEquals(orderedObs.getValueCoded(), noneConcept))
						foundDrug = true;
				orderObsVisited[0]++;
			}
			
			Obs doseObs = null;
			boolean foundDose = false;
			while (orderObsVisited[1] < orderedDoseResults.size() && !foundDose) {
				Result orderedDoseResult = orderedDoseResults.get(orderObsVisited[1]);
				doseObs = (Obs) orderedDoseResult.getResultObject();
				if (OpenmrsUtil.nullSafeEquals(orderedObservation, doseObs.getObsGroup()))
					foundDose = true;
				orderObsVisited[1]++;
			}
			
			if (orderedObs != null)
				// we still need to check for none here because none might be in the last observations
				if (!OpenmrsUtil.nullSafeEquals(orderedObs.getValueCoded(), noneConcept))
					immunizationMap.put(orderedObs.getValueCoded(), (doseObs == null ? -1 : doseObs.getValueNumeric()));
		}
		
		for (Concept concept : immunizationMap.keySet()) {
			Result drugResult = new Result();
			String conceptName = concept.getBestName(Context.getLocale()).getName();
			String drugName = ConceptRegistry.getCachedSubstitution(conceptName, null);
			if (StringUtils.isBlank(drugName))
				drugName = concept.getBestName(Context.getLocale()).getName();
			drugResult.add(new Result(drugName));
			
			Double dose = immunizationMap.get(concept);
			Result doseResult = new Result();
			if (dose != null)
				doseResult = new Result(dose);
			drugResult.add(doseResult);
			
			immunizations.add(drugResult);
		}
		
		if (log.isDebugEnabled())
			log.debug("Immunizations for patient: " + patient.getPatientId() + ", immunizations: " + immunizations);
		
		return immunizations;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype() {
		return Datatype.CODED;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	public String[] getDependencies() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getParameterList()
	 */
	public Set<RuleParameterInfo> getParameterList() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getTTL()
	 */
	public int getTTL() {
		return 0;
	}
	
}

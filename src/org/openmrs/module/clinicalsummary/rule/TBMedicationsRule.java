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
package org.openmrs.module.clinicalsummary.rule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
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
public class TBMedicationsRule implements Rule {
	
	private static final Log log = LogFactory.getLog(TBMedicationsRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		
		Result result = new Result();
		
		SummaryService service = Context.getService(SummaryService.class);
		
		List<String> typeNames = Arrays.asList(TypeConstants.ADULT_INITIAL,
		    TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
		    TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN,
		    TypeConstants.PEDS_NONCLINICAL);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(typeNames).last();
		
		Concept startConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.START_DRUGS);
		Concept continueConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.CONTINUE_REGIMEN);

		LogicCriteria plannedCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.TUBERCULOSIS_PROPHYLAXIS_PLAN);
		Result plannedResults = context.read(patient, service.getLogicDataSource("summary"), plannedCriteria.and(encounterCriteria));
		
		if (log.isDebugEnabled())
			log.debug("Planned crypto observations for patient: " + patient.getPatientId() + " is: " + plannedResults);
		
		boolean foundPlan = false;
		int planCounter = 0;
		while (planCounter < plannedResults.size() && !foundPlan) {
			Result plannedResult = plannedResults.get(planCounter);
			Concept valueCoded = plannedResult.toConcept();
			if (OpenmrsUtil.nullSafeEquals(startConcept, valueCoded) || OpenmrsUtil.nullSafeEquals(continueConcept, valueCoded))
				foundPlan = true;
			planCounter++;
		}
		
		Concept isoniazidConcept = ConceptRegistry.getCachedConcept(StandardConceptConstants.ISONIAZID);
		
		if (foundPlan)
			result.add(new Result(StandardConceptConstants.ISONIAZID));
		
		Concept pyrazinamideConcept = Context.getConceptService().getConcept(StandardConceptConstants.PYRAZINAMIDE);
		Concept rifampinConcept = Context.getConceptService().getConcept(StandardConceptConstants.RIFAMPIN);
		
		LogicCriteria addedCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.MEDICATION_ADDED);
		Result addedResults = context.read(patient, service.getLogicDataSource("summary"), addedCriteria.and(encounterCriteria));
		
		if (log.isDebugEnabled())
			log.debug("Added crypto observations for patient: " + patient.getPatientId() + " is: " + addedResults);
		
		boolean foundPyrazinamide = false;

		for (Result addedResult : addedResults) {
			Concept valueCoded = addedResult.toConcept();
			// no need for duplicates
			if (!result.contains(valueCoded)) {
				// complex logic of pyrazinamide
				if (OpenmrsUtil.nullSafeEquals(valueCoded, pyrazinamideConcept)) {
					// add pyrazinamide if we already have rifampin
					// if not, then mark them as "can be added when we see rifampin"
					if (result.contains(rifampinConcept)) {
						result.add(addedResult);
						foundPyrazinamide = false;
					} else {
						foundPyrazinamide = true;
					}
				} else if (OpenmrsUtil.nullSafeEquals(valueCoded, isoniazidConcept)) {
					// just add isoniazid if we found any
					result.add(addedResult);
				} else if (OpenmrsUtil.nullSafeEquals(valueCoded, rifampinConcept)) {
					result.add(addedResult);
					// for rifampin, we need to check if we found pyrazinamide previously
					if (foundPyrazinamide)
						result.add(new Result(StandardConceptConstants.PYRAZINAMIDE));
					
				}
			}
		}
		
		return result;
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
